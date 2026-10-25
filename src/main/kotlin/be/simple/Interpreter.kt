package be.simple

data class Line(val number: Int, val text: String)
data class Error(val lineNumber: Int, val message: String)
enum class Status { STOPPED, RUNNING, PAUSED, FINISHED }

data class StepResult(
    val currentStep: Int,
    val changedVariables: Map<String, String>,
    val removedVariables: List<String>,
    val log: String,
    val status: Status
)

data class Snapshot(
    val currentStep: Int,
    val variables: Map<String, String>,
    val hidden: Set<String>,
    val results: Map<String, String>,
    val status: Status
)

class Interpreter {
    var currentStep: Int = 1
        private set
    var status: Status = Status.STOPPED
        private set

    val variables = linkedMapOf<String, String>()
    val hiddenVariables = linkedSetOf<String>()
    val results = linkedMapOf<String, String>()

    private val history = ArrayDeque<Snapshot>()
    var historyLimit: Int = 12

    fun parse(code: String): List<Line> =
        code.lines().mapIndexed { i, t -> Line(i + 1, t) }

    fun validate(lines: List<Line>): List<Error> {
        val labels = collectLabels(lines)
        val errors = mutableListOf<Error>()
        for (line in lines) {
            val t = line.text.trim()
            if (t.isEmpty() || t.startsWith("\\")) continue
            val target = extractJumpTarget(t) ?: continue
            if (!targetExists(target, labels, lines.size)) {
                errors += Error(line.number, "Unknown jump target: $target")
            }
        }
        return errors
    }

    private fun collectLabels(lines: List<Line>): Set<String> {
        val set = mutableSetOf<String>()
        for (l in lines) {
            LABEL_RE.find(l.text.trim())?.let { set += it.groupValues[1] }
        }
        return set
    }

    private fun extractJumpTarget(text: String): String? =
        JUMP_RE.find(text)?.groupValues?.get(1)

    private fun targetExists(target: String, labels: Set<String>, lineCount: Int): Boolean {
        if (target.startsWith("step:")) {
            val n = target.removePrefix("step:").toIntOrNull() ?: return false
            return n in 1..lineCount
        }
        return target in labels
    }

    fun executeOneStep(code: String): StepResult {
        if (status == Status.STOPPED || status == Status.FINISHED) status = Status.RUNNING
        saveSnapshot()
        val lines = parse(code)
        val line = lines.getOrNull(currentStep - 1)
            ?: return finishStep("End of program", Status.FINISHED)
        return executeLine(line, lines)
    }

    private fun executeLine(line: Line, lines: List<Line>): StepResult {
        val raw = line.text.trim()
        if (raw.isEmpty()) return advance("Step ${line.number}: empty line")
        if (raw.startsWith("\\")) return advance("Step ${line.number}: literal string")

        val qIdx = findUnescaped(raw, '?')
        if (qIdx > 0) {
            val cond = raw.substring(0, qIdx).trim()
            val action = raw.substring(qIdx + 1).trim()
            if (action.isEmpty()) return advance("Step ${line.number}: ? is text")
            if (!evalCondition(cond)) return advance("Condition false → continue")
            return executeOperation(action, line, lines, "Condition true → ")
        }
        return executeOperation(raw, line, lines, "")
    }

    private fun executeOperation(op: String, line: Line, lines: List<Line>, prefix: String): StepResult {
        JUMP_RE.matchEntire(op)?.let {
            val target = it.groupValues[1]
            currentStep = resolveTarget(target, lines)
            return StepResult(currentStep, emptyMap(), emptyList(), "${prefix}Jump to $target", status)
        }
        COMMAND_RE.matchEntire(op)?.let { return runCommand(it.groupValues[1], prefix) }
        MOVE_RE.matchEntire(op)?.let { return runMove(it, prefix) }
        MATH_CHAIN_RE.matchEntire(op)?.let { return runMathChain(it, prefix) }
        MATH_RE.matchEntire(op)?.let { return runMath(it, prefix) }
        SIZE_RE.matchEntire(op)?.let { return runSize(it, prefix) }
        NAMING_RE.matchEntire(op)?.let { return runNaming(it, prefix) }
        return advance("Step ${line.number} executed: ${substitute(op)}")
    }

    private fun runCommand(cmd: String, prefix: String): StepResult = when (cmd) {
        "pause" -> { status = Status.PAUSED; advance("${prefix}Command pause") }
        "resume" -> { status = Status.RUNNING; advance("${prefix}Command resume") }
        "stop" -> finishStep("${prefix}Command stop", Status.FINISHED)
        else -> advance("unknown command")
    }

    private fun runMove(m: MatchResult, prefix: String): StepResult {
        val src = substitute(m.groupValues[1].trim())
        val dst = m.groupValues[2].trim()
        if (src.isEmpty()) return advance("${prefix}Move: empty source")
        variables[dst] = src
        variables.remove(m.groupValues[1].trim())
        return StepResult(currentStep, mapOf(dst to src),
            listOf(m.groupValues[1].trim()), "${prefix}Moved '$src' → $dst", status)
    }

    private fun runMathChain(m: MatchResult, prefix: String): StepResult {
        val expr = substitute(m.groupValues[1].trim())
        val value = evaluateMath(expr)
        val dst = m.groupValues[2].trim()
        variables[dst] = value
        return StepResult(currentStep, mapOf(dst to value), emptyList(),
            "${prefix}Action math performed: $expr = $value → $dst", status)
    }

    private fun runMath(m: MatchResult, prefix: String): StepResult {
        val expr = substitute(m.groupValues[1].trim())
        val value = evaluateMath(expr)
        results["math"] = value
        return StepResult(currentStep, emptyMap(), emptyList(),
            "${prefix}Action math performed: $expr = $value", status)
    }

    private fun runSize(m: MatchResult, prefix: String): StepResult {
        val raw = substitute(m.groupValues[1].trim())
        val len = raw.length.toString()
        results["size"] = len
        return StepResult(currentStep, emptyMap(), emptyList(),
            "${prefix}Action size performed: '$raw' length = $len", status)
    }

    private fun runNaming(m: MatchResult, prefix: String): StepResult {
        val rawValue = m.groupValues[1].trim()
        val name = m.groupValues[2].trim()
        val hidden = m.groupValues[3].isNotEmpty()
        val value = substitute(rawValue)
        if (name.isEmpty()) return advance("${prefix}Naming: empty name")
        variables[name] = value
        if (hidden) hiddenVariables += name else hiddenVariables -= name
        return StepResult(currentStep, mapOf(name to value), emptyList(),
            "${prefix}Named '$value' as '$name'${if (hidden) " (hidden)" else ""}", status)
    }

    fun substitute(input: String): String = substituteRec(input, 0)

    private fun substituteRec(input: String, depth: Int): String {
        if (depth >= 10) return input
        var result = input
        var changed = false
        for ((name, value) in variables) {
            if (name.isEmpty() || name == value) continue
            val r = result.replace(name, value)
            if (r != result) { result = r; changed = true }
        }
        return if (changed) substituteRec(result, depth + 1) else result
    }

    private fun evalCondition(cond: String): Boolean {
        val c = substitute(cond).trim()
        OR_RE.split(c, 2).let { if (it.size == 2) return evalCondition(it[0]) || evalCondition(it[1]) }
        AND_RE.split(c, 2).let { if (it.size == 2) return evalCondition(it[0]) && evalCondition(it[1]) }
        if (c.startsWith("!")) return !evalCondition(c.substring(1))
        for (op in BINARY_OPS) {
            val idx = c.indexOf(op)
            if (idx > 0) return compare(c.substring(0, idx).trim(), c.substring(idx + op.length).trim(), op)
        }
        return c.toBooleanStrictOrNull() ?: c.isNotEmpty()
    }

    private fun compare(l: String, r: String, op: String): Boolean {
        val ln = l.toDoubleOrNull(); val rn = r.toDoubleOrNull()
        val cmp = if (ln != null && rn != null) ln.compareTo(rn) else l.compareTo(r)
        return when (op) {
            "==" -> cmp == 0; "!=" -> cmp != 0
            "<" -> cmp < 0; ">" -> cmp > 0
            "<=" -> cmp <= 0; ">=" -> cmp >= 0
            else -> false
        }
    }

    private fun evaluateMath(expr: String): String {
        val tokens = expr.split(" ")
        var acc = tokens.firstOrNull()?.toDoubleOrNull() ?: return expr.trim()
        var i = 1
        while (i < tokens.size - 1) {
            val op = tokens[i]
            val n = tokens[i + 1].toDoubleOrNull() ?: return expr.trim()
            acc = when (op) {
                "+" -> acc + n; "-" -> acc - n
                "*" -> acc * n; "/" -> if (n != 0.0) acc / n else return expr.trim()
                else -> return expr.trim()
            }
            i += 2
        }
        return if (acc == acc.toLong().toDouble()) acc.toLong().toString() else acc.toString()
    }

    private fun resolveTarget(target: String, lines: List<Line>): Int {
        if (target.startsWith("step:")) return target.removePrefix("step:").toIntOrNull() ?: 1
        for (l in lines) {
            LABEL_RE.find(l.text.trim())?.let { if (it.groupValues[1] == target) return l.number }
        }
        return 1
    }

    private fun saveSnapshot() {
        history.addLast(Snapshot(currentStep, LinkedHashMap(variables),
            LinkedHashSet(hiddenVariables), LinkedHashMap(results), status))
        while (history.size > historyLimit) history.removeFirst()
    }

    fun executeStepBack(): StepResult? {
        val snap = history.removeLastOrNull() ?: return null
        currentStep = snap.currentStep
        variables.clear(); variables.putAll(snap.variables)
        hiddenVariables.clear(); hiddenVariables.addAll(snap.hidden)
        results.clear(); results.putAll(snap.results)
        status = snap.status
        return StepResult(currentStep, LinkedHashMap(variables), emptyList(),
            "Step back → step $currentStep", status)
    }

    fun reset() {
        currentStep = 1; status = Status.STOPPED
        variables.clear(); hiddenVariables.clear(); results.clear(); history.clear()
    }

    private fun advance(log: String): StepResult {
        currentStep += 1
        return StepResult(currentStep, emptyMap(), emptyList(), log, status)
    }

    private fun finishStep(log: String, s: Status): StepResult {
        status = s
        return StepResult(currentStep, emptyMap(), emptyList(), log, s)
    }

    private fun findUnescaped(s: String, ch: Char): Int {
        var i = 0
        while (i < s.length) {
            if (s[i] == '\\') { i += 2; continue }
            if (s[i] == ch) return i
            i++
        }
        return -1
    }

    companion object {
        private val LABEL_RE = Regex("""^=\s*(\S+)\s*$""")
        private val JUMP_RE = Regex("""^\^\s*(\S+)\s*$""")
        private val COMMAND_RE = Regex("""^=>\s*(pause|resume|stop)\s*$""")
        private val MOVE_RE = Regex("""^(.+?)\s*->\s*(\S+)\s*$""")
        private val MATH_CHAIN_RE = Regex("""^(.+?)\s*=>\s*math\s*=>\s*(\S+)\s*$""")
        private val MATH_RE = Regex("""^(.+?)\s*=>\s*math\s*$""")
        private val SIZE_RE = Regex("""^(.+?)\s*=>\s*size\s*$""")
        private val NAMING_RE = Regex("""^(.+?)\s*(?<!\\)=\s*([A-Za-z_][\w]*)\s*(\*)?\s*$""")
        private val OR_RE = Regex("""\s*\|\|\s*""")
        private val AND_RE = Regex("""\s*&&\s*""")
        private val BINARY_OPS = listOf("==", "!=", "<=", ">=", "<", ">")
    }
}