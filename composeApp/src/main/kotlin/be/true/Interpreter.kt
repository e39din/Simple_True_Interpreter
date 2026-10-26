package be.`true`

import java.io.File
import kotlin.collections.get
import kotlin.collections.iterator

data class Line(val number: Int, val text: String)
data class Error(val lineNumber: Int, val message: String)
data class FunctionDef(val source: String, val name: String, val body: List<Line>)
enum class Status { STOPPED, RUNNING, PAUSED, FINISHED }

data class StepResult(
    val currentStep: Int,
    val changedVariables: Map<String, String>,
    val removedVariables: List<String>,
    val log: String,
    val status: Status,
    val command: String? = null,
    val errorLine: Int? = null
)

data class Snapshot(
    val currentStep: Int,
    val variables: Map<String, String>,
    val hidden: Set<String>,
    val results: Map<String, String>,
    val constants: Set<String>,
    val splitters: Map<String, Map<Char, String>>,
    val log: List<LogEntry>,
    val status: Status,
    val returnValue: String?,
    val hasReturn: Boolean,
    val waitUntilMs: Long?
)

data class LogEntry(val text: String, val type: String, val lineNumber: Int? = null, val resolved: Boolean = false)

enum class AgentAction { STATUS, WAITER, WAIT }

class Interpreter(
    var tabCaller: ((String, String) -> String)? = null,
    var tabCallerAsync: ((String, String) -> Unit)? = null,
    var tabExists: ((String) -> Boolean)? = null,
    var fileReader: ((String) -> String?)? = null,
    var funcCaller: ((String, String, String, Boolean) -> String)? = null,
    var funcExists: ((String, String) -> Boolean)? = null,
    var agentCaller: ((callerAgentId: Int, name: String, action: AgentAction, arg: String?) -> Unit)? = null,
    var simpleCaller: ((action: String, arg: String?) -> Unit)? = null,
    var callerAgentId: Int = -1
) {
    var currentStep: Int = 1; private set
    var status: Status = Status.STOPPED; private set

    val variables = linkedMapOf<String, String>()
    val hiddenVariables = linkedSetOf<String>()
    val results = linkedMapOf<String, String>()
    val constants = linkedSetOf<String>()
    val splitters = linkedMapOf<String, MutableMap<Char, String>>()

    val errorLines = mutableListOf<Int>()
    val errorMessages = mutableListOf<String>()

    var actions: String = DEFAULT_ACTIONS
    private var instruction: String = ""

    private val webSockets = mutableMapOf<String, AutoCloseable>()

    var log: List<LogEntry> = emptyList(); private set
    var returnValue: String? = null; private set
    var hasReturn: Boolean = false; private set
    var waitUntilMs: Long? = null
    var timeoutMs: Long? = null

    var historyLimit: Int = 100
    var logLimit: Int = 1000
    var strictMode: Boolean = false
    var autoSync: Boolean = true

    private val history = ArrayDeque<Snapshot>()
    private val expansionStack = ArrayDeque<String>()
    private var mapDepth = 0

    private var parsedCacheCode: String? = null
    private var parsedCache: List<Line> = emptyList()

    private var combinedRegexVar: Regex? = null
    private var combinedRegexVarDirty = true
    private var combinedRegexRes: Regex? = null
    private var combinedRegexResDirty = true
    private val rpnCache = mutableMapOf<String, List<String>>()
    private val mathResultCache = mutableMapOf<String, String>()

    private fun getCombinedRegexVar(): Regex? {
        if (combinedRegexVarDirty) {
            val names = variables.keys.filter { it.isNotBlank() && it != variables[it] }
                .sortedByDescending { it.length }
            combinedRegexVar = if (names.isEmpty()) null
            else Regex("(?<![A-Za-z0-9_:])(${names.joinToString("|") { Regex.escape(it) }})(?![A-Za-z0-9_])")
            combinedRegexVarDirty = false
        }
        return combinedRegexVar
    }

    private fun getCombinedRegexRes(): Regex? {
        if (combinedRegexResDirty) {
            val names = results.keys.filter { it.isNotBlank() }
                .sortedByDescending { it.length }
            combinedRegexRes = if (names.isEmpty()) null
            else Regex("(?<![A-Za-z0-9_:])(${names.joinToString("|") { Regex.escape(it) }})(?![A-Za-z0-9_])")
            combinedRegexResDirty = false
        }
        return combinedRegexRes
    }

    private fun invalidateSubstituteCache() {
        combinedRegexVarDirty = true
        combinedRegexResDirty = true
    }

    fun forceStep(n: Int) { currentStep = n.coerceAtLeast(1) }
    fun setStatusPublic(s: Status) { status = s }
    private fun setStatus(s: Status) { status = s }

    fun parse(code: String): List<Line> {
        if (code === parsedCacheCode && parsedCache.isNotEmpty()) return parsedCache
        val lines = code.lines().mapIndexed { i, t -> Line(i + 1, t) }
        parsedCacheCode = code
        parsedCache = lines
        return lines
    }

    fun validate(lines: List<Line>, strict: Boolean = false): List<Error> {
        val labels = collectLabels(lines)
        val errors = mutableListOf<Error>()
        var inFunction = false
        for (line in lines) {
            val t = line.text.trim()
            if (t.isEmpty() || t.startsWith("\\")) continue
            if (Regex("""^[A-Za-z_]\w*:[A-Za-z_]\w*:$""").matches(t)) { inFunction = true; continue }
            if (Regex("""^:[A-Za-z_]\w*\s*$""").matches(t)) { inFunction = false; continue }
            val qCount = t.count { it == '?' } - t.count { it == '\\' }
            if (qCount > 1) errors += Error(line.number, "Multiple ? on one line")
            if (inFunction && RESULT_RE.matches(t)) {
                errors += Error(line.number, "=> :result inside function (use => :result at end)")
                continue
            }
            if (strict) {
                if (Regex("""^[A-Za-z_]\w*:[A-Za-z_]\w*:$""").matches(t)) {
                    errors += Error(line.number, "Strict Mode: function declarations are forbidden")
                    continue
                }
                val forbidden = Regex("""(?<!\\):([A-Za-z_]\w*)""")
                val m = forbidden.find(t)
                if (m != null) {
                    val name = m.groupValues[1]
                    if (name in SYSTEM_NAMES && tabExists?.invoke(name) != true) {
                        errors += Error(line.number, "Strict Mode: ':$name' is forbidden")
                        continue
                    }
                }
            }
            val target = extractJumpTarget(t) ?: continue
            if (!targetExists(target, labels, lines.size)) {
                errors += Error(line.number, "Unknown jump target: $target")
            }
        }
        return errors
    }

    private fun collectLabels(lines: List<Line>): Set<String> {
        val set = mutableSetOf<String>()
        for (l in lines) LABEL_RE.find(l.text.trim())?.let { set += it.groupValues[1] }
        return set
    }

    private fun hasLabel(target: String, lines: List<Line>): Boolean {
        for (l in lines) LABEL_RE.find(l.text.trim())?.let { if (it.groupValues[1] == target) return true }
        return false
    }

    private fun extractJumpTarget(text: String): String? = JUMP_RE.find(text)?.groupValues?.get(1)

    private fun targetExists(target: String, labels: Set<String>, lineCount: Int): Boolean {
        val n = target.toIntOrNull()
        if (n != null) return n in 1..lineCount
        if (target.startsWith(":")) return target in setOf(":stop", ":pause", ":resume", ":delete", ":reset")
        if (target.contains(".")) return true
        return target in labels
    }

    fun collectFunctions(code: String): List<FunctionDef> {
        val lines = code.lines()
        val out = mutableListOf<FunctionDef>()
        var i = 0
        while (i < lines.size) {
            val header = Regex("""^([A-Za-z_]\w*):([A-Za-z_]\w*):$""").find(lines[i].trim())
            if (header == null) { i++; continue }
            val src = header.groupValues[1]; val fn = header.groupValues[2]
            val start = i + 1
            var end = lines.size - 1
            for (j in start until lines.size) {
                if (Regex("""^:[A-Za-z_]\w*\s*$""").matches(lines[j].trim())) { end = j; break }
            }
            val bodyLines = (start..end).map { idx -> Line(idx + 1, lines[idx]) }
            out += FunctionDef(src, fn, bodyLines)
            i = end + 1
        }
        return out
    }

    fun executeOneStep(code: String): StepResult {
        saveSnapshot()
        val lines = parse(code)
        if (lines.isEmpty()) return finish("End of program", Status.FINISHED)
        if (currentStep > lines.size) {
            currentStep = 1
            return StepResult(currentStep, emptyMap(), emptyList(), "Loop → step 1", status)
        }
        val line = lines[currentStep - 1]
        instruction = line.text
        return runActions(line, lines)
    }

    private fun runActions(line: Line, lines: List<Line>): StepResult {
        val actionLines = actions.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        if (actionLines.isEmpty()) {
            return stepForward("Step ${line.number}: no actions")
        }
        var lastResult: StepResult? = null
        for (actionText in actionLines) {
            val opResult = executeOperation(actionText, line, lines, "")
            val step = applyOp(opResult)
            lastResult = step
            if (step.status == Status.FINISHED || step.errorLine != null) break
        }
        return lastResult ?: stepForward("Step ${line.number}: empty actions")
    }

    fun executeInstructionOnce(text: String, code: String): StepResult {
        val lines = parse(code)
        val line = Line(currentStep, text)
        return executeLine(line, lines)
    }

    private fun executeLine(line: Line, lines: List<Line>): StepResult {
        val raw = line.text.trim()
        if (raw.isEmpty()) return stepForward("Step ${line.number}: empty line")
        if (raw.startsWith("\\")) return stepForward("Step ${line.number}: literal string")

        val qIdx = findUnescaped(raw, '?')
        if (qIdx >= 0) {
            val cond = raw.substring(0, qIdx).trim()
            val action = raw.substring(qIdx + 1).trim()
            if (cond.isEmpty()) return fail(line.number, "Empty condition before '?'")
            if (action.isEmpty()) return stepForward("Step ${line.number}: ? is text")
            val ok = try { evalCondition(cond) } catch (e: Exception) {
                return fail(line.number, "Condition error: ${e.message}")
            }
            if (!ok) return stepForward("Condition false → continue")
            return applyOp(executeOperation(action, line, lines, "Condition true → "))
        }
        return applyOp(executeOperation(raw, line, lines, ""))
    }

    private fun applyOp(op: OpResult): StepResult {
        if (op.errorLine != null) {
            errorLines.add(op.errorLine)
            errorMessages.add(op.log)
            results["error"] = "1"
            results["error:line"] = op.errorLine.toString()
            results["error:message"] = op.log
            results["error:lines"] = errorLines.joinToString(",")
            results["error:messages"] = errorMessages.joinToString("\n")
            setStatus(Status.PAUSED)
            return StepResult(currentStep, emptyMap(), emptyList(), op.log, Status.PAUSED, null, op.errorLine)
        }
        if (op.newStatus != null) setStatus(op.newStatus)
        if (op.step != null) {
            currentStep = op.step
            return StepResult(currentStep, op.changed, op.removed,
                "${op.log} (step $currentStep)", status, op.command)
        }
        if (status == Status.FINISHED)
            return StepResult(currentStep, op.changed, op.removed, op.log, status, op.command)
        return stepForward(op.log, op.changed, op.removed, op.command)
    }

    private fun stepForward(log: String, changed: Map<String, String> = emptyMap(),
                            removed: List<String> = emptyList(), command: String? = null): StepResult {
        currentStep += 1
        return StepResult(currentStep, changed, removed, log, status, command)
    }

    private fun finish(log: String, s: Status): StepResult {
        setStatus(s)
        return StepResult(currentStep, emptyMap(), emptyList(), log, s)
    }

    private fun fail(lineNumber: Int, message: String): StepResult {
        setStatus(Status.PAUSED)
        errorLines.add(lineNumber)
        errorMessages.add(message)
        results["error"] = "1"
        results["error:line"] = lineNumber.toString()
        results["error:message"] = "custom, 1, $message"
        results["error:lines"] = errorLines.joinToString(",")
        results["error:messages"] = errorMessages.joinToString("\n")
        return StepResult(currentStep, emptyMap(), emptyList(),
            "Error on line $lineNumber: $message", Status.PAUSED, null, lineNumber)
    }

    private data class OpResult(
        val step: Int? = null,
        val changed: Map<String, String> = emptyMap(),
        val removed: List<String> = emptyList(),
        val log: String,
        val newStatus: Status? = null,
        val command: String? = null,
        val errorLine: Int? = null
    )

    private fun executeOperation(op: String, line: Line, lines: List<Line>, prefix: String): OpResult {
        RUN_FN_RE.matchEntire(op)?.let { return runRunFn(it, line, lines, prefix) }
        RUN_BARE_RE.matchEntire(op)?.let { return runRunBare(line, lines, prefix) }
        LOG_FN_RE.matchEntire(op)?.let { return runLogFn(it, prefix) }
        LOG_BARE_RE.matchEntire(op)?.let { return runLogBare(prefix) }

        FILE_TO_VAR_RE.matchEntire(op)?.let { return runFileToVar(it, line, prefix) }
        VAR_TO_FILE_RE.matchEntire(op)?.let { return runVarToFile(it, line, prefix) }
        FILE_TO_FILE_RE.matchEntire(op)?.let { return runFileToFile(it, line, prefix) }
        INSERT_ATOM_RE.matchEntire(op)?.let { return runInsertAtom(it, line, prefix) }

        AGENT_STATUS_RE.matchEntire(op)?.let { return runAgentStatus(it, prefix) }
        AGENT_WAITER_RE.matchEntire(op)?.let { return runAgentWaiter(it, prefix) }
        AGENT_STATUS_ALL_RE.matchEntire(op)?.let { return runAgentStatusAll(it, prefix) }
        AGENT_WAIT_TIMEOUT_RE.matchEntire(op)?.let { return runAgentWaitTimeout(it, prefix) }
        AGENT_WAIT_RE.matchEntire(op)?.let { return runAgentWait(it, prefix) }

        MESSAGE_RE.matchEntire(op)?.let { return runMessage(it, prefix) }
        RANGE_RE.matchEntire(op)?.let { return runRange(it, line, prefix) }
        REPEAT_RE.matchEntire(op)?.let { return runRepeat(it, line, lines, prefix) }

        SAVE_ACTIVE_RE.matchEntire(op)?.let { return runSaveActive(it, prefix) }
        FILE_SAVE_RE.matchEntire(op)?.let { return runFileSave(it, prefix) }
        FILE_PATH_RE.matchEntire(op)?.let { return runFilePath(it, prefix) }

        ERROR_GET_RE.matchEntire(op)?.let { return runErrorGet(it, line, prefix) }
        ERROR_RESOLVED_RE.matchEntire(op)?.let { return runErrorResolved(it, prefix) }
        ERROR_SET_RE.matchEntire(op)?.let { return runErrorSet(it, line, prefix) }

        SIMPLE_RE.matchEntire(op)?.let { return runSimple(it, line, prefix) }

        SHORT_SIZE_GET_RE.matchEntire(op)?.let { return runShortSizeGet(it, line, prefix) }
        SHORT_SIZE_RE.matchEntire(op)?.let { return runShortSize(it, line, prefix) }

        SHORT_DATA_CHAIN_RE.matchEntire(op)?.let { return runShortDataChain(it, line, prefix) }
        SHORT_DATA_RE.matchEntire(op)?.let { return runShortData(it, line, prefix) }
        SHORT_MATH_NAMED_RE.matchEntire(op)?.let { return runShortMathNamed(it, prefix) }

        MAP_RE.matchEntire(op)?.let { return runMap(it, line, prefix) }
        REDUCE_RE.matchEntire(op)?.let { return runReduce(it, line, prefix) }
        FOLD_RE.matchEntire(op)?.let { return runFold(it, line, prefix) }
        EACH_RE.matchEntire(op)?.let { return runEach(it, line, prefix) }

        DATA_CHAIN_RE.matchEntire(op)?.let { return runDataOpChain(it, line, prefix) }
        MATH_CHAIN_RE.matchEntire(op)?.let { return runMathChain(it, line, prefix) }
        MATH_NAMED_RE.matchEntire(op)?.let { return runMathNamed(it, prefix) }
        MATH_RE.matchEntire(op)?.let { return runMath(it, line, prefix) }

        JSON_GET_RE.matchEntire(op)?.let { return runJsonGet(it, prefix) }
        JSON_SET_RE.matchEntire(op)?.let { return runJsonSet(it, prefix) }
        JSON_NAMES_RE.matchEntire(op)?.let { return runJsonNames(it, prefix) }
        JSON_VALUES_RE.matchEntire(op)?.let { return runJsonValues(it, prefix) }

        DATA_RE.matchEntire(op)?.let { return runDataOp(it, line, prefix) }
        FILTER_RE.matchEntire(op)?.let { return runFilter(it, prefix) }
        MATCHES_RE.matchEntire(op)?.let { return runMatches(it, prefix) }

        SPLITTER_RE.matchEntire(op)?.let { m ->
            val src = m.groupValues[1].trim(); val axis = m.groupValues[2][0]; val sep = m.groupValues[3].trim()
            splitters.getOrPut(src) { mutableMapOf() }[axis] = sep
            return OpResult(log = "${prefix}Splitter '$src' axis '$axis' = '$sep'")
        }

        FILE_RE.matchEntire(op)?.let { return runFile(it, prefix) }
        API_WS_RE.matchEntire(op)?.let { return runApiWs(it, prefix) }
        API_RE.matchEntire(op)?.let { return runApi(it, prefix) }

        CONSOLE_RE.matchEntire(op)?.let { return runConsole(it, prefix) }
        SAVE_RE.matchEntire(op)?.let { return runSave(it, prefix) }
        LOAD_RE.matchEntire(op)?.let { return runLoad(it, prefix) }
        GET_RE.matchEntire(op)?.let { return runGet(it, prefix) }
        POST_RE.matchEntire(op)?.let { return runPost(it, prefix) }

        SIZE_AXIS_RE.matchEntire(op)?.let { return runSizeAxis(it, line, prefix) }
        SIZE_RE.matchEntire(op)?.let { return runSize(it, line, prefix) }

        FUNC_RETURN_RE.matchEntire(op)?.let { m ->
            val name = m.groupValues[1]
            val raw = variables[name] ?: results[name] ?: substitute(name)
            returnValue = raw; hasReturn = true
            return OpResult(log = "Func :return '$name' → '${raw.take(40)}'", newStatus = Status.FINISHED)
        }

        RESULT_RE.matchEntire(op)?.let { m ->
            val raw = substitute(m.groupValues[1].trim())
            returnValue = raw; hasReturn = true
            results["result"] = raw
            return OpResult(log = "Command => :result → '${raw.take(40)}'", newStatus = Status.FINISHED)
        }

        ACTIVE_TAB_RE.matchEntire(op)?.let { m ->
            val idx = (substitute(m.groupValues[1].trim())).toIntOrNull() ?: 1
            val step = m.groupValues[2].trim().toIntOrNull() ?: currentStep
            return OpResult(step = step, command = "activeTab:$idx",
                log = "Command activeTab → $idx (step $step)")
        }

        COMMAND_RE.matchEntire(op)?.let { return runCommand(it.groupValues[1], prefix) }

        JUMP_RE.matchEntire(op)?.let {
            val target = it.groupValues[1].trim()
            return runJump(target, line, lines, prefix)
        }

        LOG_MOVE_RE.matchEntire(op)?.let { return runLogMove(it, prefix) }
        LOG_GET_RE.matchEntire(op)?.let { return runLogGet(it, prefix) }

        FUNC_CALL_LOCAL_CHAIN_RE.matchEntire(op)?.let { return runFuncCallLocalChain(it, line, prefix) }
        FUNC_CALL_LOCAL_RE.matchEntire(op)?.let { m ->
            val src = m.groupValues[2].trim(); val fn = m.groupValues[3].trim()
            if (funcExists?.invoke(src, fn) == true) {
                val data = substitute(m.groupValues[1].trim())
                val result = funcCaller?.invoke(src, fn, data, false) ?: ""
                results["$src:$fn"] = result
                return OpResult(log = "Local call $src:$fn('$data') → ${result.take(40)}")
            }
        }
        TAB_CALL_CHAIN_RE.matchEntire(op)?.let { return runTabCallChain(it, line, prefix) }
        TAB_CALL_ASYNC_RE.matchEntire(op)?.let { m ->
            val name = m.groupValues[2].trim().removeSuffix("!")
            if (tabExists?.invoke(name) == true) {
                tabCallerAsync?.invoke(name, substitute(m.groupValues[1].trim()))
                return OpResult(log = "Async tab call $name started")
            }
        }
        TAB_CALL_RE.matchEntire(op)?.let { m ->
            val name = m.groupValues[2].trim()
            if (tabExists?.invoke(name) == true) {
                val data = substitute(m.groupValues[1].trim())
                val result = tabCaller?.invoke(name, data) ?: ""
                results[name] = result
                return OpResult(log = "Tab call $name('$data') → ${result.take(40)}")
            }
        }

        MOVE_RE.matchEntire(op)?.let { return runMove(it, line, prefix) }
        COPY_RE.matchEntire(op)?.let { return runCopy(it, line, prefix) }

        NAMING_EMPTY_RE.matchEntire(op)?.let { m ->
            val name = m.groupValues[1].trim()
            if (name == "nothing") return OpResult(log = "${prefix}'nothing' is reserved", errorLine = currentStep)
            if (name in SYSTEM_NAMES) return OpResult(log = "${prefix}Cannot name container '$name': reserved", errorLine = currentStep)
            variables[name] = ""
            invalidateSubstituteCache()
            return OpResult(changed = mapOf(name to ""), log = "${prefix}Named empty as '$name'")
        }

        RESULT_NAMING_RE.matchEntire(op)?.let { return runResultNaming(it, line, prefix) }

        NAMING_RE.matchEntire(op)?.let { return runNaming(it, line, prefix) }

        if (op.matches(BARE_NAME_RE) && op in variables) {
            return runSubstitution(op, line, lines, prefix)
        }

        return OpResult(log = "Step ${line.number} executed: ${substitute(op)}")
    }

    // ---------- :run / :log ----------

    private fun runRunFn(m: MatchResult, line: Line, lines: List<Line>, prefix: String): OpResult {
        val raw = m.groupValues[1].trim()
        val target = if (raw == ":instruction") instruction else substitute(raw)
        if (target.isEmpty()) return OpResult(log = "${prefix}:run (empty)")
        val result = executeLine(Line(line.number, target), lines)
        return OpResult(
            log = "${prefix}:run → '${target.take(40)}'",
            step = result.currentStep,
            changed = result.changedVariables,
            removed = result.removedVariables,
            newStatus = result.status,
            errorLine = result.errorLine
        )
    }

    private fun runRunBare(line: Line, lines: List<Line>, prefix: String): OpResult {
        if (instruction.isEmpty()) return OpResult(log = "${prefix}:run (no instruction)")
        val result = executeLine(Line(line.number, instruction), lines)
        return OpResult(
            log = "${prefix}:run → '${instruction.take(40)}'",
            step = result.currentStep,
            changed = result.changedVariables,
            removed = result.removedVariables,
            newStatus = result.status,
            errorLine = result.errorLine
        )
    }

    private fun runLogFn(m: MatchResult, prefix: String): OpResult {
        val raw = m.groupValues[1].trim()
        val data = if (raw == ":instruction") instruction else substitute(raw)
        log = listOf(LogEntry(data, "log")) + log
        return OpResult(log = "${prefix}:log → '${data.take(40)}'")
    }

    private fun runLogBare(prefix: String): OpResult {
        log = listOf(LogEntry(instruction, "log")) + log
        return OpResult(log = "${prefix}:log → '${instruction.take(40)}'")
    }

    // ---------- jump ----------

    private fun runJump(target: String, line: Line, lines: List<Line>, prefix: String): OpResult {
        val n = target.toIntOrNull()
        if (n != null) {
            return OpResult(step = n.coerceIn(1, lines.size), log = "${prefix}Jump to step $n")
        }
        if (target.startsWith(":")) {
            return OpResult(command = target, log = "${prefix}Jump to command $target")
        }
        if (!target.contains(".") && hasLabel(target, lines)) {
            return OpResult(step = resolveLabel(target, lines), log = "${prefix}Jump to $target")
        }
        if (target.contains(".")) {
            return OpResult(step = 1, command = "file:$target", log = "${prefix}Jump to file:$target")
        }
        return OpResult(log = "${prefix}Unknown jump target: $target", errorLine = line.number)
    }

    private fun resolveLabel(target: String, lines: List<Line>): Int {
        val candidates = mutableListOf<Int>()
        for (l in lines) LABEL_RE.find(l.text.trim())?.let { if (it.groupValues[1] == target) candidates += l.number }
        if (candidates.isEmpty()) return currentStep
        val after = candidates.filter { it > currentStep }
        return if (after.isNotEmpty()) after.min() else candidates.max()
    }

    // ---------- file operations ----------

    private fun runFileToVar(m: MatchResult, line: Line, prefix: String): OpResult {
        val path = substitute(m.groupValues[1].trim())
        val dst = m.groupValues[2].trim()
        if (!File(path).exists()) {
            return OpResult(log = "${prefix}File '$path' not found", errorLine = line.number)
        }
        val content = try { File(path).readText() } catch (e: Exception) {
            return OpResult(log = "${prefix}File read error: ${e.message}", errorLine = line.number)
        }
        if (dst in constants) return OpResult(log = "${prefix}Cannot write to constant '$dst'", errorLine = line.number)
        variables[dst] = content
        invalidateSubstituteCache()
        return OpResult(changed = mapOf(dst to content), log = "${prefix}file:$path => $dst (${content.length} chars)")
    }

    private fun runVarToFile(m: MatchResult, line: Line, prefix: String): OpResult {
        val src = substituteLeft(m.groupValues[1].trim())
        val path = substitute(m.groupValues[2].trim())
        if (src.isEmpty()) {
            return OpResult(log = "${prefix}Source is empty or unknown", errorLine = line.number)
        }
        val ok = try { File(path).parentFile?.mkdirs(); File(path).writeText(src); true } catch (e: Exception) {
            return OpResult(log = "${prefix}File write error: ${e.message}", errorLine = line.number)
        }
        results["file"] = if (ok) "1" else "-1"
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}$src => file:$path (${src.length} chars)")
    }

    private fun runFileToFile(m: MatchResult, line: Line, prefix: String): OpResult {
        val a = substitute(m.groupValues[1].trim())
        val op = m.groupValues[2].trim()
        val b = substitute(m.groupValues[3].trim())
        if (!File(a).exists()) {
            return OpResult(log = "${prefix}File '$a' not found", errorLine = line.number)
        }
        val content = try { File(a).readText() } catch (e: Exception) {
            return OpResult(log = "${prefix}File read error: ${e.message}", errorLine = line.number)
        }
        val ok = try { File(b).parentFile?.mkdirs(); File(b).writeText(content); true } catch (e: Exception) {
            return OpResult(log = "${prefix}File write error: ${e.message}", errorLine = line.number)
        }
        if (!ok) return OpResult(log = "${prefix}File write failed", errorLine = line.number)
        if (op == "->") {
            try { File(a).delete() } catch (_: Exception) {}
        }
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}file:$a $op file:$b (${content.length} chars)")
    }

    private fun runInsertAtom(m: MatchResult, line: Line, prefix: String): OpResult {
        val value = substituteLeft(m.groupValues[1].trim())
        val op = m.groupValues[2].trim()
        val target = m.groupValues[3].trim()
        val path = parsePath(target)
            ?: return OpResult(log = "${prefix}Insert target must have axis: $target", errorLine = line.number)
        val (name, axes) = path
        if (name in constants) return OpResult(log = "${prefix}Cannot insert into constant '$name'", errorLine = line.number)
        if (name !in variables) return OpResult(log = "${prefix}'$name' is not declared", errorLine = line.number)
        val container = variables[name] ?: ""
        val inserted = insertAt(container, axes, value)
            ?: return OpResult(log = "${prefix}Insert out of range: $target", errorLine = line.number)
        variables[name] = inserted
        if (op == "->|") {
            val srcName = m.groupValues[1].trim()
            if (srcName.matches(Regex("""^[A-Za-z_]\w*$""")) && srcName in variables) {
                variables[srcName] = ""
            }
        }
        invalidateSubstituteCache()
        return OpResult(changed = mapOf(name to inserted),
            log = "${prefix}$op $target ← '$value'")
    }

    // ---------- agent ----------

    private fun runAgentStatus(m: MatchResult, prefix: String): OpResult {
        val dst = m.groupValues[1].trim()
        agentCaller?.invoke(callerAgentId, "*", AgentAction.STATUS, dst)
        return OpResult(log = "${prefix}:agent:status → $dst")
    }

    private fun runAgentWaiter(m: MatchResult, prefix: String): OpResult {
        val name = substitute(m.groupValues[1].trim())
        val dst = m.groupValues[2].trim()
        agentCaller?.invoke(callerAgentId, name, AgentAction.WAITER, dst)
        return OpResult(log = "${prefix}:agent:$name:waiter → $dst")
    }

    private fun runAgentStatusAll(m: MatchResult, prefix: String): OpResult {
        val dst = m.groupValues[1].trim()
        agentCaller?.invoke(callerAgentId, "*", AgentAction.STATUS, dst)
        return OpResult(log = "${prefix}:agent:status → $dst")
    }

    private fun runAgentWait(m: MatchResult, prefix: String): OpResult {
        val raw = substitute(m.groupValues[1].trim())
        val ms = raw.toLongOrNull() ?: 0L
        waitUntilMs = System.currentTimeMillis() + ms
        timeoutMs = null
        setStatus(Status.PAUSED)
        agentCaller?.invoke(callerAgentId, "*wait", AgentAction.WAIT, ms.toString())
        return OpResult(log = "${prefix}:agent:self => wait, $ms ms", newStatus = Status.PAUSED)
    }

    private fun runAgentWaitTimeout(m: MatchResult, prefix: String): OpResult {
        val raw = substitute(m.groupValues[1].trim())
        val timeout = m.groupValues[2].trim().toLongOrNull() ?: 0L
        val ms = raw.toLongOrNull() ?: 0L
        waitUntilMs = System.currentTimeMillis() + ms
        timeoutMs = timeout
        setStatus(Status.PAUSED)
        agentCaller?.invoke(callerAgentId, "*wait", AgentAction.WAIT, ms.toString())
        return OpResult(log = "${prefix}:agent:self => wait, $ms ms (timeout $timeout)", newStatus = Status.PAUSED)
    }

    private fun runCommand(cmd: String, prefix: String): OpResult = when (cmd) {
        "pause" -> OpResult(log = "${prefix}Command pause", newStatus = Status.PAUSED)
        "resume" -> OpResult(log = "${prefix}Command resume", newStatus = Status.RUNNING)
        "stop" -> OpResult(log = "${prefix}Command stop", newStatus = Status.FINISHED)
        "reset" -> OpResult(log = "${prefix}Command reset")
        "next" -> OpResult(log = "${prefix}Command 'next' is deprecated (no-op); use Step in UI")
        "back" -> OpResult(log = "${prefix}Command 'back' is deprecated; use Step Back in UI")
        else -> OpResult(log = "${prefix}unknown command")
    }

    // ---------- message / range / repeat ----------

    private fun runMessage(m: MatchResult, prefix: String): OpResult {
        val data = substitute(m.groupValues[1].trim())
        log = listOf(LogEntry(data, "message")) + log
        results["message"] = data
        return OpResult(log = "${prefix}:message → '${data.take(40)}'")
    }

    private fun runRange(m: MatchResult, line: Line, prefix: String): OpResult {
        val from = m.groupValues[1].trim().toIntOrNull()
        val to = m.groupValues[2].trim().toIntOrNull()
        if (from == null || to == null) {
            return OpResult(log = "${prefix}:range needs numbers", errorLine = line.number)
        }
        val result = if (from <= to) (from..to).joinToString(",")
        else (from downTo to).joinToString(",")
        results["range"] = result
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}:range $from..$to → '$result'")
    }

    private fun runRepeat(m: MatchResult, line: Line, lines: List<Line>, prefix: String): OpResult {
        val actionName = substitute(m.groupValues[1].trim())
        val n = m.groupValues[2].trim().toIntOrNull()
            ?: return OpResult(log = "${prefix}:repeat needs count", errorLine = line.number)
        val actionText = variables[actionName]
            ?: return OpResult(log = "${prefix}:repeat: action '$actionName' not found", errorLine = line.number)
        val changed = LinkedHashMap<String, String>()
        var guard = 0
        while (guard < n) {
            guard++
            val r = executeOperation(actionText, line, lines, "")
            if (r.errorLine != null) {
                return OpResult(log = "${prefix}:repeat error on line ${r.errorLine}: ${r.log}", errorLine = r.errorLine)
            }
            r.changed.forEach { (k, v) -> variables[k] = v; changed[k] = v }
            r.removed.forEach { variables.remove(it); changed.remove(it) }
            if (r.newStatus == Status.FINISHED) break
        }
        results["repeat"] = "done"
        invalidateSubstituteCache()
        return OpResult(changed = changed, log = "${prefix}:repeat $actionName × $n")
    }

    // ---------- :save ----------

    private fun runSaveActive(m: MatchResult, prefix: String): OpResult {
        agentCaller?.invoke(callerAgentId, "*save", AgentAction.STATUS, null)
        return OpResult(log = "${prefix}:save (active)")
    }

    private fun runFileSave(m: MatchResult, prefix: String): OpResult {
        val name = m.groupValues[1].trim()
        agentCaller?.invoke(callerAgentId, "*save", AgentAction.STATUS, name)
        return OpResult(log = "${prefix}$name:save")
    }

    private fun runFilePath(m: MatchResult, prefix: String): OpResult {
        val name = m.groupValues[1].trim()
        agentCaller?.invoke(callerAgentId, "*path", AgentAction.STATUS, name)
        return OpResult(log = "${prefix}:file:$name => :path")
    }

    // ---------- :error ----------

    private fun runErrorGet(m: MatchResult, line: Line, prefix: String): OpResult {
        val suffix = m.groupValues[1]
        val dst = m.groupValues[2].trim()
        if (dst in constants) return OpResult(log = "${prefix}Cannot write to constant '$dst'", errorLine = line.number)
        val key = if (suffix.isEmpty()) "error" else "error:$suffix"
        val value = results[key] ?: ""
        variables[dst] = value
        invalidateSubstituteCache()
        return OpResult(changed = mapOf(dst to value), log = "${prefix}:$key → $dst")
    }

    private fun runErrorSet(m: MatchResult, line: Line, prefix: String): OpResult {
        val msg = substitute(m.groupValues[1].trim())
        errorLines.add(line.number)
        errorMessages.add(msg)
        results["error"] = "1"
        results["error:line"] = line.number.toString()
        results["error:message"] = "custom, 1, $msg"
        results["error:lines"] = errorLines.joinToString(",")
        results["error:messages"] = errorMessages.joinToString("\n")
        log = listOf(LogEntry("Error: $msg", "error", line.number, false)) + log
        setStatus(Status.PAUSED)
        return OpResult(
            log = "${prefix}:error → '$msg'",
            newStatus = Status.PAUSED,
            errorLine = line.number
        )
    }

    private fun runErrorResolved(m: MatchResult, prefix: String): OpResult {
        val arg = m.groupValues[1].trim()
        val value = arg.toIntOrNull() ?: 1
        if (value == 0) {
            val msg = results["error:message"] ?: ""
            if (msg.isNotEmpty()) {
                val parts = msg.split(",", limit = 3)
                if (parts.size >= 2) {
                    results["error:message"] = "${parts[0].trim()}, 0, ${if (parts.size > 2) parts[2].trim() else ""}"
                }
            }
            errorLines.clear()
            errorMessages.clear()
            log = log.map { if (it.type == "error") it.copy(resolved = true) else it }
        }
        return OpResult(log = "${prefix}:error:resolve = $value")
    }

    // ---------- :simple ----------

    private fun runSimple(m: MatchResult, line: Line, prefix: String): OpResult {
        val raw = m.groupValues[1].trim()
        val action = m.groupValues[2].trim()
        val arg = if (raw.isEmpty()) null else substitute(raw)
        simpleCaller?.invoke(action, arg)
        results["simple"] = "1"
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}:simple:$action${if (arg != null) " ('$arg')" else ""}")
    }

    // ---------- :s ----------

    private fun runShortSize(m: MatchResult, line: Line, prefix: String): OpResult {
        val raw = substitute(m.groupValues[1].trim())
        val axis = if (m.groupValues[2].isNotBlank()) m.groupValues[2][0] else 'x'
        val len = countAxis(raw, axis).toString()
        results["size"] = len
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}:s ($axis) → $len")
    }

    private fun runShortSizeGet(m: MatchResult, line: Line, prefix: String): OpResult {
        val dst = m.groupValues[1].trim()
        if (dst in constants) return OpResult(log = "${prefix}Cannot write to constant '$dst'", errorLine = line.number)
        val value = results["size"] ?: ""
        variables[dst] = value
        invalidateSubstituteCache()
        return OpResult(changed = mapOf(dst to value), log = "${prefix}:s → $dst")
    }

    // ---------- short data / math ----------

    private fun runShortData(m: MatchResult, line: Line, prefix: String): OpResult {
        val remapped = StringBuilder()
        remapped.append(m.groupValues[1])
        if (m.groupValues[2].isNotBlank()) { remapped.append(":"); remapped.append(m.groupValues[2]) }
        remapped.append(" , "); remapped.append(m.groupValues[3])
        remapped.append(" => :data:"); remapped.append(m.groupValues[4])
        return runDataOpInternal(remapped.toString(), line, prefix)
    }

    private fun runShortDataChain(m: MatchResult, line: Line, prefix: String): OpResult {
        val remapped = StringBuilder()
        remapped.append(m.groupValues[1])
        if (m.groupValues[2].isNotBlank()) { remapped.append(":"); remapped.append(m.groupValues[2]) }
        remapped.append(" , "); remapped.append(m.groupValues[3])
        remapped.append(" => :data:"); remapped.append(m.groupValues[4])
        val r = runDataOpInternal(remapped.toString(), line, prefix)
        if (r.errorLine != null) return r
        val op = m.groupValues[4].trim()
        val dst = m.groupValues[5].trim()
        val value = results[op] ?: ""
        if (dst in constants) return OpResult(log = "${prefix}Cannot write to constant '$dst'", errorLine = line.number)
        variables[dst] = value
        invalidateSubstituteCache()
        return OpResult(changed = mapOf(dst to value), log = "${prefix}:data:$op → $dst")
    }

    private fun runShortMathNamed(m: MatchResult, prefix: String): OpResult {
        val expr = substitute(m.groupValues[1].trim())
        val op = m.groupValues[2].trim()
        val sep = detectSeparator(expr)
        val items = expr.split(sep).map { it.trim() }.filter { it.isNotEmpty() }
        val result = applyListOp(op, items, sep)
        results["math:$op"] = result
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}:$op → $result")
    }

    // ---------- json ----------

    private fun runJsonGet(m: MatchResult, prefix: String): OpResult {
        val json = substitute(m.groupValues[1].trim())
        val path = substitute(m.groupValues[2].trim())
        val value = JsonParser.getCached(json, path)
            ?: return OpResult(log = "${prefix}:json:get '$path' not found", errorLine = currentStep)
        results["json"] = value
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}:json:get '$path' → '$value'")
    }

    private fun runJsonSet(m: MatchResult, prefix: String): OpResult {
        val json = substitute(m.groupValues[1].trim())
        val path = substitute(m.groupValues[2].trim())
        val value = substitute(m.groupValues[3].trim())
        val updated = JsonParser.setCached(json, path, value)
        results["json"] = updated
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}:json:set '$path' = '$value'")
    }

    private fun runJsonNames(m: MatchResult, prefix: String): OpResult {
        val json = substitute(m.groupValues[1].trim())
        val names = JsonParser.namesCached(json).joinToString(",")
        results["json"] = names
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}:json:names → '$names'")
    }

    private fun runJsonValues(m: MatchResult, prefix: String): OpResult {
        val json = substitute(m.groupValues[1].trim())
        val values = JsonParser.valuesCached(json).joinToString(",")
        results["json"] = values
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}:json:values → '$values'")
    }

    // ---------- data ----------

    private fun runDataOp(m: MatchResult, line: Line, prefix: String): OpResult =
        runDataOpInternal(m, line, prefix)

    private fun runDataOpInternal(remapped: String, line: Line, prefix: String): OpResult {
        val m = DATA_RE.find(remapped) ?: DATA_CHAIN_RE.find(remapped)
        ?: return OpResult(log = "${prefix}bad data op: $remapped", errorLine = line.number)
        return runDataOpInternal(m, line, prefix)
    }

    private fun runDataOpInternal(m: MatchResult, line: Line, prefix: String): OpResult {
        val dataExpr = m.groupValues[1].trim()
        val axisRaw = m.groupValues[2]
        val axisExplicit = axisRaw.isNotBlank()
        val axis = if (axisExplicit) axisRaw[0] else 'x'
        val extra = m.groupValues[3].trim()
        val op = m.groupValues[4].trim()
        val data = substitute(dataExpr)

        if (op in DATA_X_ONLY && axisExplicit && axis != 'x') {
            return OpResult(log = "${prefix}:data:$op supports only x-axis (got '$axis')", errorLine = line.number)
        }
        if (op == "insert" || op == "set") {
            return OpResult(log = "${prefix}:data:$op is deprecated (use ->| or ->)", errorLine = line.number)
        }
        if (op == "length") {
            return OpResult(log = "${prefix}:data:length is deprecated (use :data:y:size or :s)", errorLine = line.number)
        }

        val ax = axis
        val args = if (extra.isBlank()) emptyList() else splitArgs(extra)

        if (op == "size") {
            val count = countAxis(data, ax).toString()
            results["size"] = count
            invalidateSubstituteCache()
            return OpResult(log = "${prefix}:data:size ($ax) → $count")
        }

        val parts = splitBy(if (dataExpr.contains(":")) dataExpr.substringBefore(":") else dataExpr, data, ax)

        val result = when (op) {
            "join" -> {
                val sep = args.getOrNull(0) ?: sepOf(ax)
                val r = parts.joinToString(sep)
                results["join"] = r
                OpResult(log = "${prefix}:data:join ($ax, '$sep') → '${r.take(40)}'")
            }
            "substring" -> {
                val s = args.getOrNull(0)?.toIntOrNull() ?: 1
                val e = args.getOrNull(1)?.toIntOrNull() ?: data.length
                val start = s.coerceIn(1, data.length + 1); val end = e.coerceIn(start - 1, data.length)
                val sub = if (start <= end) data.substring(start - 1, end) else ""
                results["substring"] = sub
                OpResult(log = "${prefix}:data:substring [$start..$end] → '${sub.take(40)}'")
            }
            "indexOf" -> {
                val sub = args.getOrNull(0) ?: ""
                val idx = when (axis) {
                    'x' -> data.indexOf(sub)
                    else -> parts.indexOf(sub)
                }
                results["indexOf"] = (if (idx >= 0) idx + 1 else -1).toString()
                OpResult(log = "${prefix}:data:indexOf ($axis) '$sub' → ${results["indexOf"]}")
            }
            "replace" -> {
                val old = args.getOrNull(0) ?: ""; val new = args.getOrNull(1) ?: ""
                val r = if (old.isEmpty()) data else data.replace(old, new)
                results["replace"] = r
                OpResult(log = "${prefix}:data:replace '$old'→'$new' → '${r.take(40)}'")
            }
            "lower" -> {
                val r = if (axis == 'x') data.lowercase()
                else parts.joinToString(sepOf(axis)) { it.lowercase() }
                results["lower"] = r
                OpResult(log = "${prefix}:data:lower ($axis) → '${r.take(40)}'")
            }
            "upper" -> {
                val r = if (axis == 'x') data.uppercase()
                else parts.joinToString(sepOf(axis)) { it.uppercase() }
                results["upper"] = r
                OpResult(log = "${prefix}:data:upper ($axis) → '${r.take(40)}'")
            }
            "trim" -> {
                val r = if (axis == 'x') data.trim()
                else parts.joinToString(sepOf(axis)) { it.trim() }
                results["trim"] = r
                OpResult(log = "${prefix}:data:trim ($axis) → '${r.take(40)}'")
            }
            "chars" -> {
                val sep = args.getOrNull(0) ?: ","
                val r = buildString { data.forEachIndexed { i, c -> if (i > 0) append(sep); append(c) } }
                results["chars"] = r
                OpResult(log = "${prefix}:data:chars → '$r'")
            }
            "charAt" -> {
                val i = args.getOrNull(0)?.toIntOrNull() ?: 1
                val r = when (axis) {
                    'x' -> data.getOrNull(i - 1)?.toString() ?: ""
                    else -> parts.getOrNull(i - 1) ?: ""
                }
                results["charAt"] = r
                OpResult(log = "${prefix}:data:charAt ($axis) $i → '$r'")
            }
            "reverse" -> {
                val r = buildString {
                    for (i in parts.indices.reversed()) {
                        if (i < parts.lastIndex) append(sepOf(ax))
                        append(parts[i])
                    }
                }
                results["reverse"] = r
                OpResult(log = "${prefix}:data:reverse ($ax) → '${r.take(40)}'")
            }
            "unique" -> {
                val seen = mutableSetOf<String>()
                val r = buildString {
                    var first = true
                    for (p in parts) {
                        if (seen.add(p)) {
                            if (!first) append(sepOf(ax))
                            append(p); first = false
                        }
                    }
                }
                results["unique"] = r
                OpResult(log = "${prefix}:data:unique ($ax) → '${r.take(40)}'")
            }
            "contains" -> {
                val v = args.getOrNull(0) ?: ""
                results["contains"] = if (parts.contains(v)) "1" else "0"
                OpResult(log = "${prefix}:data:contains '$v' → ${results["contains"]}")
            }
            "slice" -> {
                val from = args.getOrNull(0)?.toIntOrNull() ?: 1
                val to = args.getOrNull(1)?.toIntOrNull() ?: parts.size
                val f = (from - 1).coerceIn(0, parts.size); val t = to.coerceIn(f, parts.size)
                val sb = StringBuilder()
                for (i in f until t) { if (i > f) sb.append(sepOf(ax)); sb.append(parts[i]) }
                results["slice"] = sb.toString()
                OpResult(log = "${prefix}:data:slice $from..$to → '${sb.toString().take(40)}'")
            }
            "sort" -> {
                val allNums = parts.all { it.toDoubleOrNull() != null } && parts.isNotEmpty()
                val sorted = if (allNums) parts.sortedBy { it.toDouble() } else parts.sorted()
                val sb = StringBuilder()
                for (i in sorted.indices) { if (i > 0) sb.append(sepOf(ax)); sb.append(sorted[i]) }
                results["sort"] = sb.toString()
                OpResult(log = "${prefix}:data:sort ($ax) → '${sb.toString().take(40)}'")
            }
            "push" -> {
                val v = args.getOrNull(0) ?: ""
                val r = if (data.isEmpty()) v else data + sepOf(ax) + v
                results["push"] = r
                OpResult(log = "${prefix}:data:push $ax '$v' → '${r.take(40)}'")
            }
            "pop" -> {
                if (parts.isEmpty()) { results["pop"] = ""; OpResult(log = "${prefix}:data:pop empty") }
                else {
                    val last = parts.last()
                    val rest = parts.dropLast(1).joinToString(sepOf(ax))
                    results["pop"] = last; results["pop_rest"] = rest
                    OpResult(log = "${prefix}:data:pop → '${last.take(20)}'")
                }
            }
            "remove" -> {
                val i = args.getOrNull(0)?.toIntOrNull() ?: 1
                val list = parts.toMutableList(); val idx = i - 1
                if (idx in list.indices) {
                    val removed = list.removeAt(idx)
                    val sb = StringBuilder()
                    for (j in list.indices) { if (j > 0) sb.append(sepOf(ax)); sb.append(list[j]) }
                    results["remove"] = removed; results["remove_rest"] = sb.toString()
                    OpResult(log = "${prefix}:data:remove $i → '${removed.take(20)}'")
                } else OpResult(log = "${prefix}:data:remove $i out of range", errorLine = line.number)
            }
            "swap" -> {
                val i = args.getOrNull(0)?.toIntOrNull() ?: 1
                val j = args.getOrNull(1)?.toIntOrNull() ?: 1
                val list = parts.toMutableList(); val a = i - 1; val b = j - 1
                if (a in list.indices && b in list.indices) {
                    val t = list[a]; list[a] = list[b]; list[b] = t
                    val sb = StringBuilder()
                    for (k in list.indices) { if (k > 0) sb.append(sepOf(ax)); sb.append(list[k]) }
                    results["swap"] = sb.toString()
                    OpResult(log = "${prefix}:data:swap $i $j")
                } else OpResult(log = "${prefix}:data:swap out of range", errorLine = line.number)
            }
            "deleteRange" -> {
                val from = args.getOrNull(0)?.toIntOrNull() ?: 1
                val to = args.getOrNull(1)?.toIntOrNull() ?: from
                val list = parts.toMutableList()
                val range = (from - 1).coerceAtLeast(0)..(to - 1).coerceAtMost(list.size - 1)
                if (range.first <= range.last) repeat(range.last - range.first + 1) { list.removeAt(range.first) }
                val sb = StringBuilder()
                for (k in list.indices) { if (k > 0) sb.append(sepOf(ax)); sb.append(list[k]) }
                results["deleteRange"] = sb.toString()
                OpResult(log = "${prefix}:data:deleteRange $from..$to → '${sb.toString().take(40)}'")
            }
            "replaceRange" -> {
                val from = args.getOrNull(0)?.toIntOrNull() ?: 1
                val to = args.getOrNull(1)?.toIntOrNull() ?: from
                val v = args.getOrNull(2) ?: ""
                val list = parts.toMutableList()
                val range = (from - 1).coerceAtLeast(0)..(to - 1).coerceAtMost(list.size - 1)
                if (range.first <= range.last) {
                    repeat(range.last - range.first + 1) { list.removeAt(range.first) }
                    list.add(range.first.coerceIn(0, list.size), v)
                }
                val sb = StringBuilder()
                for (k in list.indices) { if (k > 0) sb.append(sepOf(ax)); sb.append(list[k]) }
                results["replaceRange"] = sb.toString()
                OpResult(log = "${prefix}:data:replaceRange $from..$to '$v'")
            }
            "peek" -> {
                if (parts.isEmpty()) { results["peek"] = ""; OpResult(log = "${prefix}:data:peek empty") }
                else {
                    val last = parts.last()
                    results["peek"] = last
                    OpResult(log = "${prefix}:data:peek → '${last.take(20)}'")
                }
            }
            else -> OpResult(log = "${prefix}unknown :data op: $op", errorLine = line.number)
        }
        invalidateSubstituteCache()
        return result
    }

    private fun runFilter(m: MatchResult, prefix: String): OpResult {
        val data = substitute(m.groupValues[1].trim())
        val cond = m.groupValues[2].trim()
        val parts = data.split(",").map { it.trim() }
        val sb = StringBuilder(); var first = true
        for (item in parts) {
            val oldIt = variables["it"]
            variables["it"] = item
            val ok = evalCondition(cond)
            if (oldIt != null) variables["it"] = oldIt else variables.remove("it")
            if (ok) { if (!first) sb.append(","); sb.append(item); first = false }
        }
        results["filter"] = sb.toString()
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}:filter → '${sb.toString().take(40)}'")
    }

    private fun runMatches(m: MatchResult, prefix: String): OpResult {
        val data = substitute(m.groupValues[1].trim())
        val regex = m.groupValues[2].trim().toRegex()
        val r = if (regex.matches(data)) "1" else "0"
        results["matches"] = r
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}:matches '$data' ~ '$regex' → $r")
    }

    // ---------- functional ----------

    private fun resolveExprArg(arg: String): String {
        val name = arg.trim()
        return if (name.matches(BARE_NAME_RE) && name in variables) variables[name]!! else arg
    }

    private fun runMap(m: MatchResult, line: Line, prefix: String): OpResult {
        if (mapDepth >= MAX_EXPANSION_DEPTH) {
            return OpResult(log = "${prefix}:map depth exceeded ($MAX_EXPANSION_DEPTH)", errorLine = line.number)
        }
        val dataExpr = m.groupValues[1].trim()
        val axis = if (m.groupValues[2].isNotBlank()) m.groupValues[2][0] else 'x'
        val exprRaw = m.groupValues[3].trim()
        val expr = resolveExprArg(exprRaw)
        val data = substitute(dataExpr)
        val parts = splitBy(if (dataExpr.contains(":")) dataExpr.substringBefore(":") else dataExpr, data, axis)
        val out = mutableListOf<String>()
        mapDepth++
        try {
            for ((idx, item) in parts.withIndex()) {
                val oldN = variables["n"]; val oldI = variables["i"]
                variables["n"] = item; variables["i"] = (idx + 1).toString()
                val value = evaluateMath(substitute(expr))
                if (oldN != null) variables["n"] = oldN else variables.remove("n")
                if (oldI != null) variables["i"] = oldI else variables.remove("i")
                out += value
            }
        } finally { mapDepth-- }
        val result = out.joinToString(sepOf(axis))
        results["map"] = result
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}:map ($axis) → '${result.take(40)}'")
    }

    private fun runReduce(m: MatchResult, line: Line, prefix: String): OpResult {
        if (mapDepth >= MAX_EXPANSION_DEPTH) {
            return OpResult(log = "${prefix}:reduce depth exceeded ($MAX_EXPANSION_DEPTH)", errorLine = line.number)
        }
        val dataExpr = m.groupValues[1].trim()
        val axis = if (m.groupValues[2].isNotBlank()) m.groupValues[2][0] else 'x'
        val initRaw = m.groupValues[3].trim()
        val exprRaw = m.groupValues[4].trim()
        val expr = resolveExprArg(exprRaw)
        val data = substitute(dataExpr)
        val init = substitute(initRaw)
        val parts = splitBy(if (dataExpr.contains(":")) dataExpr.substringBefore(":") else dataExpr, data, axis)
        var acc = init
        mapDepth++
        try {
            for ((idx, item) in parts.withIndex()) {
                val oldN = variables["n"]; val oldI = variables["i"]; val oldAcc = variables["acc"]
                variables["n"] = item; variables["i"] = (idx + 1).toString(); variables["acc"] = acc
                acc = evaluateMath(substitute(expr))
                if (oldN != null) variables["n"] = oldN else variables.remove("n")
                if (oldI != null) variables["i"] = oldI else variables.remove("i")
                if (oldAcc != null) variables["acc"] = oldAcc else variables.remove("acc")
            }
        } finally { mapDepth-- }
        results["reduce"] = acc
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}:reduce ($axis) → '${acc.take(40)}'")
    }

    private fun runFold(m: MatchResult, line: Line, prefix: String): OpResult {
        if (mapDepth >= MAX_EXPANSION_DEPTH) {
            return OpResult(log = "${prefix}:fold depth exceeded ($MAX_EXPANSION_DEPTH)", errorLine = line.number)
        }
        val dataExpr = m.groupValues[1].trim()
        val axis = if (m.groupValues[2].isNotBlank()) m.groupValues[2][0] else 'x'
        val initRaw = m.groupValues[3].trim()
        val exprRaw = m.groupValues[4].trim()
        val expr = resolveExprArg(exprRaw)
        val data = substitute(dataExpr)
        var acc = substitute(initRaw)
        val parts = splitBy(if (dataExpr.contains(":")) dataExpr.substringBefore(":") else dataExpr, data, axis)
        mapDepth++
        try {
            for ((idx, item) in parts.withIndex()) {
                val oldN = variables["n"]; val oldI = variables["i"]; val oldAcc = variables["acc"]
                variables["n"] = item; variables["i"] = (idx + 1).toString(); variables["acc"] = acc
                acc = evaluateMath(substitute(expr))
                if (oldN != null) variables["n"] = oldN else variables.remove("n")
                if (oldI != null) variables["i"] = oldI else variables.remove("i")
                if (oldAcc != null) variables["acc"] = oldAcc else variables.remove("acc")
            }
        } finally { mapDepth-- }
        results["fold"] = acc
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}:fold ($axis) → '${acc.take(40)}'")
    }

    private fun runEach(m: MatchResult, line: Line, prefix: String): OpResult {
        if (mapDepth >= MAX_EXPANSION_DEPTH) {
            return OpResult(log = "${prefix}:each depth exceeded ($MAX_EXPANSION_DEPTH)", errorLine = line.number)
        }
        val dataExpr = m.groupValues[1].trim()
        val axis = if (m.groupValues[2].isNotBlank()) m.groupValues[2][0] else 'x'
        val exprRaw = m.groupValues[3].trim()
        val expr = resolveExprArg(exprRaw)
        val data = substitute(dataExpr)
        val parts = splitBy(if (dataExpr.contains(":")) dataExpr.substringBefore(":") else dataExpr, data, axis)
        mapDepth++
        try {
            for ((idx, item) in parts.withIndex()) {
                val oldN = variables["n"]; val oldI = variables["i"]
                variables["n"] = item; variables["i"] = (idx + 1).toString()
                val r = executeOperation(expr, line, listOf(line), "$prefix[each:$idx] ")
                if (oldN != null) variables["n"] = oldN else variables.remove("n")
                if (oldI != null) variables["i"] = oldI else variables.remove("i")
                if (r.errorLine != null) return r
            }
        } finally { mapDepth-- }
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}:each ($axis) × ${parts.size}")
    }

    // ---------- :file ----------

    private fun runFile(m: MatchResult, prefix: String): OpResult {
        val path = substitute(m.groupValues[1].trim())
        val content = substitute(m.groupValues[2].trim())
        val op = m.groupValues[3].trim()
        val result = when (op) {
            "create" -> OpResult(log = "${prefix}:file:create → ${OsApi.fileCreate(path, content)}")
            "update" -> OpResult(log = "${prefix}:file:update → ${OsApi.fileUpdate(path, content)}")
            "read" -> { val s = OsApi.fileRead(path); results["file"] = s; OpResult(log = "${prefix}:file:read → ${s.length} chars") }
            "delete" -> OpResult(log = "${prefix}:file:delete → ${OsApi.fileDelete(path)}")
            "list" -> { val s = OsApi.fileList(path); results["file"] = s; OpResult(log = "${prefix}:file:list → $s") }
            "exists" -> { val r = OsApi.fileExists(path); results["file"] = r.toString(); OpResult(log = "${prefix}:file:exists → $r") }
            "data" -> {
                if (content.isBlank()) {
                    val s = OsApi.fileRead(path); results["file"] = s
                    OpResult(log = "${prefix}:file:data read '$path' → ${s.length} chars")
                } else {
                    val r = OsApi.fileUpdate(path, content); results["file"] = r.toString()
                    OpResult(log = "${prefix}:file:data write '$path' → $r")
                }
            }
            "origin" -> {
                val originPath = path + ".orig"
                if (content.isBlank()) {
                    val s = OsApi.fileRead(originPath); results["file"] = s
                    OpResult(log = "${prefix}:file:origin read '$originPath' → ${s.length} chars")
                } else {
                    val r = OsApi.fileUpdate(originPath, content); results["file"] = r.toString()
                    OpResult(log = "${prefix}:file:origin write '$originPath' → $r")
                }
            }
            "lines" -> {
                val current = OsApi.fileRead(path)
                if (content.isBlank()) {
                    results["file"] = current
                    OpResult(log = "${prefix}:file:lines read '$path' → ${current.count { it == '\n' } + 1} lines")
                } else {
                    val parts = splitArgs(content)
                    val idx = parts.getOrNull(0)?.toIntOrNull() ?: run {
                        invalidateSubstituteCache()
                        return OpResult(log = "${prefix}:file:lines: bad index '$content'", errorLine = currentStep)
                    }
                    val newLine = parts.getOrNull(1) ?: ""
                    val lines = current.split("\n").toMutableList()
                    if (idx in 1..lines.size) {
                        lines[idx - 1] = newLine
                        val updated = lines.joinToString("\n")
                        val r = OsApi.fileUpdate(path, updated)
                        results["file"] = r.toString()
                        OpResult(log = "${prefix}:file:lines '$path'[$idx] = '$newLine'")
                    } else OpResult(log = "${prefix}:file:lines: index $idx out of range", errorLine = currentStep)
                }
            }
            else -> OpResult(log = "${prefix}unknown :file op: $op", errorLine = currentStep)
        }
        invalidateSubstituteCache()
        return result
    }

    // ---------- :api ----------

    private fun runApi(m: MatchResult, prefix: String): OpResult {
        val url = substitute(m.groupValues[1].trim())
        val data = substitute(m.groupValues[2].trim())
        val op = m.groupValues[3].trim()
        val result = when (op) {
            "get" -> { val (_, s) = ServerApi.request("GET", url, null, emptyMap()); results["api:get"] = s; OpResult(log = "${prefix}:api:get → ${s.length}") }
            "post" -> { val (code, _) = ServerApi.request("POST", url, data, emptyMap()); val r = if (code in 200..299) 1 else -1; results["api:post"] = r.toString(); OpResult(log = "${prefix}:api:post → $r") }
            "put" -> { val (code, _) = ServerApi.request("PUT", url, data, emptyMap()); val r = if (code in 200..299) 1 else -1; results["api:put"] = r.toString(); OpResult(log = "${prefix}:api:put → $r") }
            "delete" -> { val (code, _) = ServerApi.request("DELETE", url, null, emptyMap()); val r = if (code in 200..299) 1 else -1; results["api:delete"] = r.toString(); OpResult(log = "${prefix}:api:delete → $r") }
            "patch" -> { val (code, _) = ServerApi.request("PATCH", url, data, emptyMap()); val r = if (code in 200..299) 1 else -1; results["api:patch"] = r.toString(); OpResult(log = "${prefix}:api:patch → $r") }
            else -> OpResult(log = "${prefix}unknown :api op: $op", errorLine = currentStep)
        }
        invalidateSubstituteCache()
        return result
    }

    private fun runApiWs(m: MatchResult, prefix: String): OpResult {
        val url = substitute(m.groupValues[1].trim())
        try { webSockets[url]?.close() } catch (_: Exception) {}
        val handle = ServerApi.websocket(url) { msg -> results["ws"] = msg }
        webSockets[url] = handle
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}:api:websocket '$url' connected")
    }

    private fun runConsole(m: MatchResult, prefix: String): OpResult {
        val cmd = substitute(m.groupValues[1].trim())
        val r = OsApi.console(cmd)
        results["console"] = r.stdout.ifBlank { r.stderr }
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}:console exit=${r.exitCode}")
    }

    private fun runSave(m: MatchResult, prefix: String): OpResult {
        val path = substitute(m.groupValues[1].trim())
        val data = substitute(m.groupValues[2].trim())
        val (code, _) = ServerApi.request("POST", path, data, emptyMap())
        val r = if (code in 200..299) 1 else -1
        results["save"] = r.toString()
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}:save → $r")
    }

    private fun runLoad(m: MatchResult, prefix: String): OpResult {
        val path = substitute(m.groupValues[1].trim())
        val query = m.groupValues[2].trim()
        val (_, s) = ServerApi.request("GET", if (query.isBlank()) path else "$path?$query", null, emptyMap())
        results["load"] = s
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}:load → ${s.length} chars")
    }

    private fun runGet(m: MatchResult, prefix: String): OpResult {
        val path = substitute(m.groupValues[1].trim())
        val query = m.groupValues[2].trim()
        val (_, s) = ServerApi.request("GET", if (query.isBlank()) path else "$path?$query", null, emptyMap())
        results["get"] = s
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}:get → ${s.length} chars")
    }

    private fun runPost(m: MatchResult, prefix: String): OpResult {
        val path = substitute(m.groupValues[1].trim())
        val data = substitute(m.groupValues[2].trim())
        val (code, _) = ServerApi.request("POST", path, data, emptyMap())
        val r = if (code in 200..299) 1 else -1
        results["post"] = r.toString()
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}:post → $r")
    }

    // ---------- math ----------

    private fun runMathChain(m: MatchResult, line: Line, prefix: String): OpResult {
        val expr = substitute(m.groupValues[1].trim())
        val value = evaluateMath(expr)
        val dst = m.groupValues[2].trim()
        val path = parsePath(dst)
        if (path != null) {
            val (name, axes) = path
            if (name in constants) return OpResult(log = "${prefix}Cannot write to constant '$name'", errorLine = line.number)
            if (name !in variables) return OpResult(log = "${prefix}'$name' is not declared", errorLine = line.number)
            val updated = setAt(variables[name] ?: "", axes, value, true)
            variables[name] = updated
            invalidateSubstituteCache()
            return OpResult(changed = mapOf(name to updated), log = "${prefix}:math: $expr = $value → $dst")
        }
        if (dst in constants) return OpResult(log = "${prefix}Cannot write to constant '$dst'", errorLine = line.number)
        variables[dst] = value
        results["math"] = value
        invalidateSubstituteCache()
        return OpResult(changed = mapOf(dst to value), log = "${prefix}:math: $expr = $value → $dst")
    }

    private fun runMathNamed(m: MatchResult, prefix: String): OpResult {
        val expr = substitute(m.groupValues[1].trim())
        val op = m.groupValues[2].trim()
        val sep = detectSeparator(expr)
        val items = expr.split(sep).map { it.trim() }.filter { it.isNotEmpty() }
        val result = applyListOp(op, items, sep)
        results["math:$op"] = result
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}:math:$op → $result")
    }

    private fun applyListOp(op: String, items: List<String>, sep: String): String {
        val nums = items.mapNotNull { it.toDoubleOrNull() }
        val allNums = nums.size == items.size && items.isNotEmpty()
        return when (op) {
            "sum" -> formatNumber(nums.sum())
            "min" -> if (nums.isEmpty()) "" else formatNumber(nums.min())
            "max" -> if (nums.isEmpty()) "" else formatNumber(nums.max())
            "avg" -> if (nums.isEmpty()) "" else formatNumber(nums.average())
            "product" -> if (nums.isEmpty()) "" else formatNumber(nums.fold(1.0) { a, b -> a * b })
            "count" -> items.size.toString()
            "sort" -> if (allNums) nums.sorted().joinToString(sep) { formatNumber(it) } else items.sorted().joinToString(sep)
            else -> items.joinToString(sep)
        }
    }

    private fun runMath(m: MatchResult, line: Line, prefix: String): OpResult {
        val expr = substitute(m.groupValues[1].trim())
        val value = evaluateMath(expr)
        results["math"] = value
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}:math: $expr = $value")
    }

    private fun runSize(m: MatchResult, line: Line, prefix: String): OpResult {
        val raw = substitute(m.groupValues[1].trim())
        val axis = if (m.groupValues[2].isNotBlank()) m.groupValues[2][0] else 'x'
        val len = countAxis(raw, axis).toString()
        results["size"] = len
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}:size ($axis): $len")
    }

    private fun runSizeAxis(m: MatchResult, line: Line, prefix: String): OpResult {
        val raw = substitute(m.groupValues[1].trim())
        val axis = m.groupValues[3][0]
        val len = countAxis(raw, axis).toString()
        results["size"] = len
        invalidateSubstituteCache()
        return OpResult(log = "${prefix}:size:$axis: $len")
    }

    // ---------- naming ----------

    private fun runNaming(m: MatchResult, line: Line, prefix: String): OpResult {
        val rawValue = m.groupValues[1].trim()
        val name = m.groupValues[2].trim()
        val flags = m.groupValues[3]
        val hidden = flags.contains("*"); val isConst = flags.contains("@")
        val isAction = rawValue.contains("=>") || rawValue.contains("->") ||
                rawValue.contains("^") || rawValue.contains("?")
        val value = if (isAction) rawValue else substituteLeft(rawValue)
        if (name.isEmpty()) return OpResult(log = "${prefix}Naming: empty name", errorLine = line.number)
        if (name == "nothing") return OpResult(log = "${prefix}'nothing' is reserved", errorLine = line.number)
        if (name in SYSTEM_NAMES) return OpResult(log = "${prefix}Cannot name container '$name': reserved", errorLine = line.number)
        val path = parsePath(name)
        if (path != null) {
            val (base, axes) = path
            if (base in constants) return OpResult(log = "${prefix}Cannot write to constant '$base'", errorLine = line.number)
            if (base !in variables) return OpResult(log = "${prefix}'$base' is not declared", errorLine = line.number)
            val updated = setAt(variables[base] ?: "", axes, value, true)
            variables[base] = updated
            invalidateSubstituteCache()
            return OpResult(changed = mapOf(base to updated), log = "${prefix}Named element '$value' at $name")
        }
        if (name in constants) return OpResult(log = "${prefix}Cannot overwrite constant '$name'", errorLine = line.number)
        variables[name] = value
        if (isConst) constants += name
        if (hidden) hiddenVariables += name else hiddenVariables -= name
        invalidateSubstituteCache()
        return OpResult(changed = mapOf(name to value),
            log = "${prefix}Named '$value' as '$name'${if (isConst) " (const)" else ""}${if (hidden) " (hidden)" else ""}")
    }

    private fun runResultNaming(m: MatchResult, line: Line, prefix: String): OpResult {
        val lhs = m.groupValues[1]
        val name = m.groupValues[3].trim()
        val flags = m.groupValues[4]
        val key = lhs.removePrefix(":")
        if (name.isEmpty()) return OpResult(log = "${prefix}Result naming: empty name", errorLine = line.number)
        if (name == "nothing") return OpResult(log = "${prefix}'nothing' is reserved", errorLine = line.number)
        if (name in SYSTEM_NAMES) return OpResult(log = "${prefix}Cannot name container '$name': reserved", errorLine = line.number)
        val value = results[key] ?: ""
        if (name in constants) return OpResult(log = "${prefix}Cannot overwrite constant '$name'", errorLine = line.number)
        val hidden = flags.contains('*'); val isConst = flags.contains('@')
        variables[name] = value
        if (isConst) constants += name
        if (hidden) hiddenVariables += name else hiddenVariables -= name
        invalidateSubstituteCache()
        return OpResult(changed = mapOf(name to value),
            log = "${prefix}Named result ':$key' as '$name'${if (isConst) " (const)" else ""}${if (hidden) " (hidden)" else ""}")
    }

    private fun runMove(m: MatchResult, line: Line, prefix: String): OpResult {
        val lhs = m.groupValues[1].trim()
        val dst = m.groupValues[2].trim()
        val lhsPath = parsePath(lhs)
        if (lhsPath != null) {
            val (name, path) = lhsPath
            if (name in constants) return OpResult(log = "${prefix}Cannot move from constant '$name'", errorLine = line.number)
            if (name !in variables) return OpResult(log = "${prefix}'$name' is not declared", errorLine = line.number)
            val container = variables[name] ?: ""
            val (picked, remainder) = extractAt(container, path) ?: return OpResult(log = "${prefix}Move: bad path $lhs", errorLine = line.number)
            variables[name] = remainder
            val dstPath = parsePath(dst)
            if (dstPath != null) {
                val (dn, dp) = dstPath
                if (dn in constants) return OpResult(log = "${prefix}Cannot move into constant '$dn'", errorLine = line.number)
                if (dn !in variables) return OpResult(log = "${prefix}'$dn' is not declared", errorLine = line.number)
                variables[dn] = setAt(variables[dn] ?: "", dp, picked, true)
            } else {
                if (dst in constants) return OpResult(log = "${prefix}Cannot move into constant '$dst'", errorLine = line.number)
                variables[dst] = picked
            }
            invalidateSubstituteCache()
            return OpResult(changed = mapOf(dst to picked, name to remainder),
                log = "${prefix}Moved element '$picked' from $lhs → $dst")
        }
        if (lhs !in variables) return OpResult(log = "${prefix}'$lhs' is not declared", errorLine = line.number)
        val src = substituteLeft(lhs)
        if (src.isEmpty()) return OpResult(log = "${prefix}Move: empty source")
        if (lhs in constants) return OpResult(log = "${prefix}Cannot move from constant '$lhs'", errorLine = line.number)
        if (dst in constants) return OpResult(log = "${prefix}Cannot move into constant '$dst'", errorLine = line.number)
        variables[dst] = src
        variables.remove(lhs)
        invalidateSubstituteCache()
        return OpResult(changed = mapOf(dst to src), removed = listOf(lhs), log = "${prefix}Moved '$src' → $dst")
    }

    private fun runCopy(m: MatchResult, line: Line, prefix: String): OpResult {
        val lhs = m.groupValues[1].trim()
        val dst = m.groupValues[2].trim()
        val lhsPath = parsePath(lhs)
        if (lhsPath != null) {
            val (name, path) = lhsPath
            if (name !in variables) return OpResult(log = "${prefix}'$name' is not declared", errorLine = line.number)
            val container = variables[name] ?: ""
            val picked = extractAtValue(container, path)
                ?: return OpResult(log = "${prefix}Copy: bad path $lhs", errorLine = line.number)
            val dstPath = parsePath(dst)
            if (dstPath != null) {
                val (dn, dp) = dstPath
                if (dn in constants) return OpResult(log = "${prefix}Cannot copy into constant '$dn'", errorLine = line.number)
                if (dn !in variables) return OpResult(log = "${prefix}'$dn' is not declared", errorLine = line.number)
                variables[dn] = setAt(variables[dn] ?: "", dp, picked, true)
            } else {
                if (dst in constants) return OpResult(log = "${prefix}Cannot copy into constant '$dst'", errorLine = line.number)
                variables[dst] = picked
            }
            invalidateSubstituteCache()
            return OpResult(changed = mapOf(dst to picked), log = "${prefix}Copied element '$picked' from $lhs → $dst")
        }
        if (lhs !in variables) return OpResult(log = "${prefix}'$lhs' is not declared", errorLine = line.number)
        val src = substituteLeft(lhs)
        if (lhs in constants) return OpResult(log = "${prefix}Cannot copy from constant '$lhs'", errorLine = line.number)
        if (dst in constants) return OpResult(log = "${prefix}Cannot copy into constant '$dst'", errorLine = line.number)
        variables[dst] = src
        invalidateSubstituteCache()
        return OpResult(changed = mapOf(dst to src), log = "${prefix}Copied '$src' → $dst")
    }

    private fun runLogGet(m: MatchResult, prefix: String): OpResult {
        val src = m.groupValues[1].trim(); val dst = m.groupValues[2].trim()
        val value = readLogSource(src) ?: return OpResult(log = "${prefix}Log read failed", errorLine = currentStep)
        if (dst in constants) return OpResult(log = "${prefix}Cannot write to constant '$dst'", errorLine = currentStep)
        variables[dst] = value
        invalidateSubstituteCache()
        return OpResult(changed = mapOf(dst to value), log = "${prefix}Log → $dst")
    }

    private fun runLogMove(m: MatchResult, prefix: String): OpResult {
        val dst = m.groupValues[1].trim()
        if (dst in constants) return OpResult(log = "${prefix}Cannot write to constant '$dst'", errorLine = currentStep)
        val joined = log.joinToString("\n") { it.text }
        log = emptyList()
        variables[dst] = joined
        invalidateSubstituteCache()
        return OpResult(changed = mapOf(dst to joined), log = "${prefix}Log -> $dst")
    }

    private fun runDataOpChain(m: MatchResult, line: Line, prefix: String): OpResult {
        val r = runDataOpInternal(m, line, prefix)
        if (r.errorLine != null) return r
        val op = m.groupValues[4].trim()
        val dst = m.groupValues[5].trim()
        val value = results[op] ?: ""
        if (dst in constants) return OpResult(log = "${prefix}Cannot write to constant '$dst'", errorLine = line.number)
        variables[dst] = value
        invalidateSubstituteCache()
        return OpResult(changed = mapOf(dst to value), log = "${prefix}:data:$op → $dst")
    }

    private fun runFuncCallLocalChain(m: MatchResult, line: Line, prefix: String): OpResult {
        val src = m.groupValues[2].trim(); val fn = m.groupValues[3].trim(); val dst = m.groupValues[4].trim()
        if (funcExists?.invoke(src, fn) != true) {
            return OpResult(log = "${prefix}Local function $src:$fn not found", errorLine = line.number)
        }
        val data = substitute(m.groupValues[1].trim())
        val result = funcCaller?.invoke(src, fn, data, false) ?: ""
        if (dst in constants) return OpResult(log = "${prefix}Cannot write to constant '$dst'", errorLine = line.number)
        variables[dst] = result
        invalidateSubstituteCache()
        return OpResult(changed = mapOf(dst to result), log = "${prefix}Local call $src:$fn('$data') → $dst = '${result.take(40)}'")
    }

    private fun runTabCallChain(m: MatchResult, line: Line, prefix: String): OpResult {
        val name = m.groupValues[2].trim(); val dst = m.groupValues[3].trim()
        if (tabExists?.invoke(name) != true) {
            return OpResult(log = "${prefix}Tab $name not found", errorLine = line.number)
        }
        val data = substitute(m.groupValues[1].trim())
        val result = tabCaller?.invoke(name, data) ?: ""
        if (dst in constants) return OpResult(log = "${prefix}Cannot write to constant '$dst'", errorLine = line.number)
        variables[dst] = result
        invalidateSubstituteCache()
        return OpResult(changed = mapOf(dst to result), log = "${prefix}Tab call $name('$data') → $dst = '${result.take(40)}'")
    }

    private fun runSubstitution(name: String, line: Line, lines: List<Line>, prefix: String): OpResult {
        if (name in expansionStack) {
            return OpResult(log = "${prefix}Recursive substitution '$name'", errorLine = line.number)
        }
        if (expansionStack.size >= MAX_EXPANSION_DEPTH) {
            return OpResult(log = "${prefix}Substitution depth exceeded ($MAX_EXPANSION_DEPTH)", errorLine = line.number)
        }
        val body = variables[name]
            ?: return OpResult(log = "${prefix}'$name' is not a container", errorLine = line.number)
        expansionStack.addLast(name)
        try {
            return executeOperation(body, line, lines, "$prefix[$name] ")
        } finally {
            expansionStack.removeLast()
        }
    }

    // ---------- substitute ----------

    fun substitute(input: String): String = substituteInternal(input, includeResults = true)

    private fun substituteLeft(input: String): String = substituteInternal(input, includeResults = false)

    private fun substituteInternal(input: String, includeResults: Boolean): String {
        var result = substituteIndexed(input)
        result = substituteDollar(result)
        result = substituteColonResults(result)
        var depth = 0
        while (depth < 10) {
            val replaced = substituteFast(result, includeResults)
            if (replaced == result) break
            result = replaced
            depth++
        }
        return result
    }

    private fun substituteFast(input: String, includeResults: Boolean = true): String {
        var result = input
        if (variables.isNotEmpty()) {
            val combined = getCombinedRegexVar() ?: return result
            result = combined.replace(result) { m ->
                val name = m.groupValues[1]
                variables[name] ?: name
            }
        }
        if (includeResults && results.isNotEmpty()) {
            val combined = getCombinedRegexRes() ?: return result
            result = combined.replace(result) { m ->
                val name = m.groupValues[1]
                variables[name] ?: results[name] ?: name
            }
        }
        return result
    }

    private fun substituteColonResults(input: String): String {
        if (!input.contains(':')) return input
        var out = input
        out = COLON_RESULT_TWO_RE.replace(out) { m ->
            val full = "${m.groupValues[1]}:${m.groupValues[2]}"
            if (full in KNOWN_RESULT_KEYS) results[full] ?: "" else m.value
        }
        out = COLON_RESULT_ONE_RE.replace(out) { m ->
            val name = m.groupValues[1]
            if (name in RESULT_NAMES_SUBST) results[name] ?: "" else m.value
        }
        return out
    }

    private fun substituteIndexed(input: String): String {
        if (!input.contains(':')) return input
        return INDEX_RE.replace(input) { m ->
            val name = m.groupValues[1]; val pathStr = m.groupValues[2]
            if (name == "log") readLogSource("log$pathStr") ?: m.value
            else {
                val base = variables[name] ?: fileReader?.invoke(name) ?: return@replace m.value
                val axes = parseAxes(pathStr)
                indexAt(base, axes, name) ?: m.value
            }
        }
    }

    private fun substituteDollar(input: String): String {
        if (!input.contains('$')) return input
        var result = input
        var guard = 0
        while (result.contains('$') && guard < 100) {
            guard++
            val next = DOLLAR_RE.replace(result) { m ->
                val left = variables[m.groupValues[1]] ?: results[m.groupValues[1]] ?: m.groupValues[1]
                val right = variables[m.groupValues[2]] ?: results[m.groupValues[2]] ?: m.groupValues[2]
                left + right
            }
            if (next == result) break
            result = next
        }
        return result
    }

    // ---------- conditions ----------

    private fun evalCondition(cond: String): Boolean {
        val c = substitute(cond).trim()
        if (c.isEmpty()) return false
        return evalCondExpr(c, 0, c.length).first
    }

    private fun evalCondExpr(s: String, from: Int, to: Int): Pair<Boolean, Int> {
        if (from >= to) return false to to
        val orSplit = findTopLevel(s, from, to, "||")
        if (orSplit >= 0) {
            val left = evalCondExpr(s, from, orSplit).first
            val right = evalCondExpr(s, orSplit + 2, to).first
            return (left || right) to to
        }
        val andSplit = findTopLevel(s, from, to, "&&")
        if (andSplit >= 0) {
            val left = evalCondExpr(s, from, andSplit).first
            val right = evalCondExpr(s, andSplit + 2, to).first
            return (left && right) to to
        }
        if (from < to && s[from] == '!') {
            return (!evalCondExpr(s, from + 1, to).first) to to
        }
        if (from < to && s[from] == '(' && s[to - 1] == ')' && isBalanced(s, from, to)) {
            return evalCondExpr(s, from + 1, to - 1).first to to
        }
        val cmp = findTopLevelCompare(s, from, to)
        if (cmp != null) {
            val (opIdx, op) = cmp
            val l = substitute(s.substring(from, opIdx).trim())
            val r = substitute(s.substring(opIdx + op.length, to).trim())
            return compare(l, r, op) to to
        }
        val atom = s.substring(from, to).trim()
        if (atom.startsWith("(") && atom.endsWith(")") && isBalanced(atom, 0, atom.length)) {
            return evalCondExpr(atom, 1, atom.length - 1).first to to
        }
        val b = atom.toBooleanStrictOrNull() ?: atom.isNotEmpty()
        return b to to
    }

    private fun findTopLevel(s: String, from: Int, to: Int, op: String): Int {
        var depth = 0
        var i = from
        while (i <= to - op.length) {
            when (s[i]) {
                '(' -> depth++
                ')' -> depth--
            }
            if (depth == 0 && s.startsWith(op, i) && i + op.length <= to) return i
            i++
        }
        return -1
    }

    private fun findTopLevelCompare(s: String, from: Int, to: Int): Pair<Int, String>? {
        var depth = 0
        var i = from
        while (i < to) {
            when (s[i]) {
                '(' -> depth++
                ')' -> depth--
            }
            if (depth == 0) {
                for (op in COMPARE_OPS) {
                    if (i + op.length <= to && s.startsWith(op, i)) return i to op
                }
            }
            i++
        }
        return null
    }

    private fun isBalanced(s: String, from: Int, to: Int): Boolean {
        var depth = 0
        for (i in from until to) {
            when (s[i]) {
                '(' -> depth++
                ')' -> { depth--; if (depth < 0) return false }
            }
        }
        return depth == 0
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

    // ---------- math ----------

    private fun evaluateMath(expr: String): String {
        val hasNames = variables.keys.any { it.isNotEmpty() && expr.contains(it) }
        if (!hasNames) {
            mathResultCache[expr]?.let { return it }
            val r = evaluateMathUncached(expr)
            if (mathResultCache.size > 2000) mathResultCache.clear()
            mathResultCache[expr] = r
            return r
        }
        return evaluateMathUncached(expr)
    }

    private fun evaluateMathUncached(expr: String): String {
        val t = expr.trim()
        if (t.isEmpty()) return ""
        return evalMathExpr(t, 0, t.length)
    }

    private fun evalMathExpr(s: String, from: Int, to: Int): String {
        if (from >= to) return ""
        val orSplit = findTopLevel(s, from, to, "||")
        if (orSplit >= 0) {
            val l = evalMathExpr(s, from, orSplit)
            val r = evalMathExpr(s, orSplit + 2, to)
            return if (l.trim() == "1" || r.trim() == "1") "1" else "0"
        }
        val andSplit = findTopLevel(s, from, to, "&&")
        if (andSplit >= 0) {
            val l = evalMathExpr(s, from, andSplit)
            val r = evalMathExpr(s, andSplit + 2, to)
            return if (l.trim() == "1" && r.trim() == "1") "1" else "0"
        }
        if (from < to && s[from] == '!') {
            val v = evalMathExpr(s, from + 1, to)
            return if (v.trim() == "1") "0" else "1"
        }
        if (from < to && s[from] == '(' && s[to - 1] == ')' && isBalanced(s, from, to)) {
            return evalMathExpr(s, from + 1, to - 1)
        }
        val cmp = findTopLevelCompare(s, from, to)
        if (cmp != null) {
            val (opIdx, op) = cmp
            val l = evalMathExpr(s, from, opIdx)
            val r = evalMathExpr(s, opIdx + op.length, to)
            return if (compare(l, r, op)) "1" else "0"
        }
        val atom = s.substring(from, to).trim()
        val rpn = rpnCache.getOrPut(atom) {
            try { toRpn(tokenizeMath(atom)) } catch (e: Exception) { emptyList() }
        }
        if (rpn.isEmpty()) return atom
        return try { formatNumber(evalRpn(rpn)) } catch (e: Exception) { atom }
    }

    private fun tokenizeMath(input: String): List<String> {
        val out = mutableListOf<String>(); var i = 0
        while (i < input.length) {
            val c = input[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() || (c == '.' && i + 1 < input.length && input[i + 1].isDigit()) -> {
                    val s = i; while (i < input.length && (input[i].isDigit() || input[i] == '.')) i++
                    out += input.substring(s, i)
                }
                c.isLetter() -> {
                    val s = i; while (i < input.length && (input[i].isLetter() || input[i] == '_')) i++
                    out += input.substring(s, i).lowercase()
                }
                input.startsWith("**", i) -> { out += "**"; i += 2 }
                input.startsWith("//", i) -> { out += "//"; i += 2 }
                input.startsWith("<<", i) -> { out += "<<"; i += 2 }
                input.startsWith(">>", i) -> { out += ">>"; i += 2 }
                c in "+-*/%(),&|^~" -> { out += c.toString(); i++ }
                else -> { out += c.toString(); i++ }
            }
        }
        return out
    }

    private fun toRpn(tokens: List<String>): List<String> {
        val prec = mapOf("**" to 5, "*" to 4, "/" to 4, "//" to 4, "%" to 4, "+" to 3, "-" to 3,
            "<<" to 2, ">>" to 2, "&" to 1, "|" to 1, "^" to 0, "u-" to 6, "u+" to 6, "u~" to 6)
        val rightAssoc = setOf("**", "u-", "u+", "u~")
        val out = mutableListOf<String>(); val stack = ArrayDeque<String>(); var prev: String? = null
        for (t in tokens) {
            when {
                t.toDoubleOrNull() != null -> out += t
                t in FUNCS -> stack.addFirst(t)
                t == "(" -> stack.addFirst(t)
                t == ")" -> {
                    while (stack.isNotEmpty() && stack.first() != "(") out += stack.removeFirst()
                    if (stack.isNotEmpty()) stack.removeFirst()
                    while (stack.isNotEmpty() && stack.first() in FUNCS) out += stack.removeFirst()
                }
                t in prec -> {
                    val op = if ((t == "-" || t == "+" || t == "~") && (prev == null || prev in prec || prev == "(" || prev == ",")) "u$t" else t
                    val p = prec[op] ?: 0
                    while (stack.isNotEmpty() && stack.first() !in FUNCS && stack.first() != "(" &&
                        stack.first() != "," && ((prec[stack.first()] ?: 0) > p ||
                                ((prec[stack.first()] ?: 0) == p && op !in rightAssoc))) out += stack.removeFirst()
                    stack.addFirst(op)
                }
                t == "," -> while (stack.isNotEmpty() && stack.first() != "(") out += stack.removeFirst()
                else -> throw IllegalArgumentException("bad token $t")
            }
            prev = t
        }
        while (stack.isNotEmpty()) out += stack.removeFirst()
        return out
    }

    private fun evalRpn(rpn: List<String>): Double {
        val st = ArrayDeque<Double>()
        for (t in rpn) when {
            t.toDoubleOrNull() != null -> st.addFirst(t.toDouble())
            t == "u-" -> st.addFirst(-st.removeFirst())
            t == "u+" -> {}
            t == "u~" -> {
                val v = st.removeFirst().toLong()
                st.addFirst(v.inv().toDouble())
            }
            t in BIN_OPS -> { val b = st.removeFirst(); val a = st.removeFirst(); st.addFirst(applyBin(a, b, t)) }
            t in FUNCS -> {
                val n = FUNC_ARITY[t] ?: 1
                val args = (1..n).map { st.removeFirst() }.reversed()
                st.addFirst(applyFunc(t, args))
            }
        }
        return st.firstOrNull() ?: 0.0
    }

    private fun applyBin(a: Double, b: Double, op: String): Double = when (op) {
        "+" -> a + b; "-" -> a - b; "*" -> a * b
        "/" -> if (b != 0.0) a / b else 0.0
        "//" -> if (b != 0.0) Math.floor(a / b) else 0.0
        "%" -> if (b != 0.0) a % b else 0.0
        "**" -> Math.pow(a, b)
        "<<" -> (a.toLong() shl b.toInt()).toDouble()
        ">>" -> (a.toLong() shr b.toInt()).toDouble()
        "&" -> (a.toLong() and b.toLong()).toDouble()
        "|" -> (a.toLong() or b.toLong()).toDouble()
        "^" -> (a.toLong() xor b.toLong()).toDouble()
        else -> 0.0
    }

    private fun applyFunc(name: String, args: List<Double>): Double = when (name) {
        "abs" -> Math.abs(args[0])
        "min" -> Math.min(args[0], args[1])
        "max" -> Math.max(args[0], args[1])
        "round" -> Math.round(args[0]).toDouble()
        "floor" -> Math.floor(args[0])
        "ceil" -> Math.ceil(args[0])
        "sqrt" -> Math.sqrt(args[0])
        "pow" -> Math.pow(args[0], args[1])
        "popcount" -> args[0].toLong().countOneBits().toDouble()
        "clz" -> args[0].toLong().countLeadingZeroBits().toDouble()
        "ctz" -> args[0].toLong().countTrailingZeroBits().toDouble()
        else -> 0.0
    }

    private fun formatNumber(d: Double): String =
        if (d == d.toLong().toDouble()) d.toLong().toString() else d.toString()

    // ---------- paths ----------

    private fun parsePath(atom: String): Pair<String, List<AxisIdx>>? {
        val m = PATH_RE.matchEntire(atom) ?: return null
        val name = m.groupValues[1]; val axesStr = m.groupValues[2]
        if (axesStr.isBlank()) return null
        return name to parseAxes(axesStr)
    }

    private data class AxisIdx(val axis: Char, val idx: Int, val name: String? = null)

    private fun parseAxes(s: String): List<AxisIdx> {
        val out = mutableListOf<AxisIdx>()
        val re = Regex(""":([lxyz]):(first|last|\d+|[A-Za-z_]\w*)""")
        for (m in re.findAll(s)) {
            val axis = m.groupValues[1][0]; val v = m.groupValues[2]
            val (idx, name) = when {
                v == "first" -> -1 to null
                v == "last" -> -2 to null
                v.toIntOrNull() != null -> v.toInt() to null
                else -> -3 to v
            }
            out += AxisIdx(axis, idx, name)
        }
        return out
    }

    private fun normalize(a: AxisIdx, size: Int): Int = when {
        a.idx == -1 -> 1; a.idx == -2 -> size
        a.idx == -3 -> (variables[a.name] ?: "").toIntOrNull() ?: 1
        else -> a.idx
    }

    private fun indexAt(value: String, axes: List<AxisIdx>, name: String): String? {
        var current = value
        for (a in axes) {
            val parts = splitBy(name, current, a.axis)
            val real = normalize(a, parts.size); val i = real - 1
            if (i !in parts.indices) return ""
            current = parts[i]
        }
        return current
    }

    private fun setAt(container: String, axes: List<AxisIdx>, newValue: String, autoCreate: Boolean): String {
        if (axes.isEmpty()) return newValue
        val head = axes.first(); val tail = axes.drop(1)
        if (head.axis == 'x') {
            val parts = if (container.isEmpty() && autoCreate) mutableListOf() else container.map { it.toString() }.toMutableList()
            val real = normalize(head, parts.size); val i = real - 1
            if (autoCreate) while (parts.size <= i) parts += ""
            if (i !in parts.indices) return container
            parts[i] = if (tail.isEmpty()) newValue else setAt(parts[i], tail, newValue, autoCreate)
            return parts.joinToString("")
        }
        val sep = sepOf(head.axis)
        val parts = if (container.isEmpty() && autoCreate) mutableListOf() else container.split(sep).toMutableList()
        val real = normalize(head, parts.size); val i = real - 1
        if (autoCreate) while (parts.size <= i) parts += ""
        if (i !in parts.indices) return container
        parts[i] = if (tail.isEmpty()) newValue else setAt(parts[i], tail, newValue, autoCreate)
        return parts.joinToString(sep)
    }

    private fun insertAt(container: String, axes: List<AxisIdx>, newValue: String): String? {
        if (axes.isEmpty()) return null
        val head = axes.first(); val tail = axes.drop(1)
        if (head.axis == 'x') {
            val parts = container.map { it.toString() }.toMutableList()
            val real = normalize(head, parts.size); val i = (real - 1).coerceIn(0, parts.size)
            parts.add(i, if (tail.isEmpty()) newValue else "")
            return parts.joinToString("")
        }
        val sep = sepOf(head.axis)
        val parts = if (container.isEmpty()) mutableListOf() else container.split(sep).toMutableList()
        val real = normalize(head, parts.size); val i = (real - 1).coerceIn(0, parts.size)
        if (tail.isEmpty()) parts.add(i, newValue) else return null
        return parts.joinToString(sep)
    }

    private fun extractAt(container: String, axes: List<AxisIdx>): Pair<String, String>? {
        if (axes.isEmpty()) return null
        val head = axes.first(); val tail = axes.drop(1)
        val sep = sepOf(head.axis)
        val parts = if (head.axis == 'x') container.map { it.toString() }.toMutableList() else container.split(sep).toMutableList()
        val real = normalize(head, parts.size); val i = real - 1
        if (i !in parts.indices) return null
        return if (tail.isEmpty()) {
            val picked = parts.removeAt(i)
            val rem = if (head.axis == 'x') parts.joinToString("") else parts.joinToString(sep)
            picked to rem
        } else {
            val sub = extractAt(parts[i], tail) ?: return null
            parts[i] = sub.second
            val rem = if (head.axis == 'x') parts.joinToString("") else parts.joinToString(sep)
            sub.first to rem
        }
    }

    private fun extractAtValue(container: String, axes: List<AxisIdx>): String? {
        if (axes.isEmpty()) return container
        val head = axes.first(); val tail = axes.drop(1)
        val sep = sepOf(head.axis)
        val parts = if (head.axis == 'x') container.map { it.toString() } else container.split(sep)
        val real = normalize(head, parts.size); val i = real - 1
        if (i !in parts.indices) return null
        return if (tail.isEmpty()) parts[i] else extractAtValue(parts[i], tail)
    }

    // ---------- snapshot ----------

    private fun saveSnapshot() {
        val splittersCopy = LinkedHashMap<String, Map<Char, String>>()
        for ((k, v) in splitters) splittersCopy[k] = LinkedHashMap(v)
        history.addLast(Snapshot(
            currentStep, LinkedHashMap(variables), LinkedHashSet(hiddenVariables),
            LinkedHashMap(results), LinkedHashSet(constants), splittersCopy,
            ArrayList(log), status, returnValue, hasReturn, waitUntilMs
        ))
        while (history.size > historyLimit) history.removeFirst()
    }

    fun executeStepBack(): StepResult? {
        val snap = history.removeLastOrNull() ?: return null
        currentStep = snap.currentStep
        variables.clear(); variables.putAll(snap.variables)
        hiddenVariables.clear(); hiddenVariables.addAll(snap.hidden)
        results.clear(); results.putAll(snap.results)
        constants.clear(); constants.addAll(snap.constants)
        splitters.clear()
        for ((k, v) in snap.splitters) splitters[k] = LinkedHashMap(v)
        log = snap.log
        returnValue = snap.returnValue
        hasReturn = snap.hasReturn
        waitUntilMs = snap.waitUntilMs
        setStatus(snap.status)
        invalidateSubstituteCache()
        return StepResult(currentStep, LinkedHashMap(variables), emptyList(),
            "Step back → step $currentStep", status)
    }

    fun reset() {
        currentStep = 1; setStatus(Status.STOPPED)
        variables.clear(); hiddenVariables.clear(); results.clear(); constants.clear(); splitters.clear()
        try { webSockets.values.forEach { it.close() } } catch (_: Exception) {}
        webSockets.clear()
        log = emptyList(); returnValue = null; hasReturn = false; waitUntilMs = null; timeoutMs = null
        history.clear()
        instruction = ""
        errorLines.clear(); errorMessages.clear()
        rpnCache.clear(); mathResultCache.clear(); invalidateSubstituteCache()
        expansionStack.clear()
        mapDepth = 0
        parsedCacheCode = null
        parsedCache = emptyList()
    }

    fun appendLog(text: String, type: String = "info", lineNumber: Int? = null) {
        log = listOf(LogEntry(text, type, lineNumber)) + log
    }

    fun logSnapshot(): List<LogEntry> = log

    private fun findUnescaped(s: String, ch: Char): Int {
        var i = 0
        while (i < s.length) {
            if (s[i] == '\\') { i += 2; continue }
            if (s[i] == ch) return i
            i++
        }
        return -1
    }

    private fun detectSeparator(expr: String): String = when {
        expr.contains(";") -> ";"
        expr.contains(",") -> ","
        expr.contains(".") -> "."
        else -> ","
    }

    private fun splitArgs(s: String): List<String> {
        val placeholder = "\u0000"
        val safe = s.replace("\\,", placeholder)
            .replace("\\;", "\u0001")
            .replace("\\.", "\u0002")
        return safe.split(",").map {
            it.trim().replace(placeholder, ",").replace("\u0001", ";").replace("\u0002", ".")
        }.filter { it.isNotEmpty() }
    }

    private fun sepOf(axis: Char): String = when (axis) {
        'l' -> "\n"; 'y' -> ","; 'z' -> ";"; 'x' -> ""; else -> ","
    }

    private fun countAxis(value: String, axis: Char): Int = when (axis) {
        'l' -> value.count { it == '\n' } + 1
        'y' -> value.count { it == ',' } + 1
        'z' -> value.count { it == ';' } + 1
        'x' -> value.length
        else -> 1
    }

    private fun readLogSource(src: String): String? {
        val parts = src.split(":")
        return when {
            parts.size == 1 -> log.joinToString("\n") { it.text }
            parts.getOrNull(1) == "type" -> log.joinToString(",") { it.type }
            parts.getOrNull(1) == "l" -> {
                val idxRaw = parts.getOrNull(2) ?: return null
                val lines = log.map { it.text }
                val idx = when {
                    idxRaw == "first" -> 0
                    idxRaw == "last" -> lines.size - 1
                    else -> (idxRaw.toIntOrNull() ?: 1) - 1
                }
                lines.getOrNull(idx)
            }
            else -> null
        }
    }

    private fun splitBy(name: String, value: String, axis: Char): List<String> {
        splitters[name]?.get(axis)?.let { return value.split(it) }
        return when (axis) {
            'l' -> value.split("\n")
            'x' -> if (value.isEmpty()) emptyList() else value.map { it.toString() }
            'y' -> splitArgs(value)
            'z' -> {
                val placeholder = "\u0000"
                value.replace("\\;", placeholder).split(";").map { it.replace(placeholder, ";") }
            }
            else -> listOf(value)
        }
    }

    companion object {
        const val DEFAULT_ACTIONS = ":run"
        private const val MAX_EXPANSION_DEPTH = 10

        private val BARE_NAME_RE = Regex("""^[A-Za-z_]\w*$""")
        private val LABEL_RE = Regex("""^=\s*(\S+)\s*$""")
        private val JUMP_RE = Regex("""^\^\s*(\S+)\s*$""")
        private val COMMAND_RE = Regex("""^=>\s*(pause|resume|stop|reset)\s*$""")
        private const val ATOM = """[A-Za-z_]\w*(?::[lxyz]:(?:first|last|\d+|[A-Za-z_]\w*))*"""
        private const val LOG_SRC = """log(?::type|:l:(?:first|last|\d+))?"""
        private val NAMING_RE = Regex("""^(.+?)\s*(?<!\\)=\s*($ATOM)\s*([*@]+)?\s*$""")
        private val NAMING_EMPTY_RE = Regex("""^=\s*([A-Za-z_]\w*)\s*$""")
        private val MOVE_RE = Regex("""^($ATOM)\s*->\s*($ATOM)\s*$""")
        private val COPY_RE = Regex("""^($ATOM)\s*=>\s*($ATOM)\s*$""")
        private val COMPARE_OPS = listOf("==", "!=", "<=", ">=", "<", ">")
        private val BIN_OPS = setOf("+", "-", "*", "/", "//", "%", "**", "<<", ">>", "&", "|", "^")
        private val FUNCS = setOf("abs", "min", "max", "round", "floor", "ceil", "sqrt", "pow", "popcount", "clz", "ctz")
        private val FUNC_ARITY = mapOf(
            "abs" to 1, "round" to 1, "floor" to 1, "ceil" to 1, "sqrt" to 1,
            "popcount" to 1, "clz" to 1, "ctz" to 1,
            "min" to 2, "max" to 2, "pow" to 2
        )
        private val SYSTEM_NAMES = setOf(
            "result", "parameters", "nothing", "step", "cycle", "speed", "color",
            "error", "log", "message", "size", "s", "math", "data", "json", "file",
            "api", "console", "simple", "agent", "run", "range", "repeat",
            "activeTab", "path", "source", "test", "return", "assert", "call",
            "filter", "matches", "map", "reduce", "fold", "each"
        )
        private val RUN_FN_RE = Regex("""^(.+?)\s*=>\s*:run\s*$""")
        private val RUN_BARE_RE = Regex("""^:run\s*$""")
        private val MATH_CHAIN_RE = Regex("""^(.+?)\s*=>\s*:math\s*=>\s*($ATOM)\s*$""")
        private val MATH_NAMED_RE = Regex("""^(.+?)\s*=>\s*:math:(\w+)\s*$""")
        private val MATH_RE = Regex("""^(.+?)\s*=>\s*:math\s*$""")
        private val DATA_CHAIN_RE = Regex("""^(.+?)(?::([lxyz]))?\s*,\s*(.*?)\s*=>\s*:data:(\w+)\s*=>\s*([A-Za-z_]\w*)\s*$""")
        private val DATA_RE = Regex("""^(.+?)(?::([lxyz]))?\s*,\s*(.*?)\s*=>\s*:data:(\w+)\s*$""")
        private val FILTER_RE = Regex("""^(.+?)\s*,\s*(.+?)\s*=>\s*:filter\s*$""")
        private val MATCHES_RE = Regex("""^(.+?)\s*,\s*(.+?)\s*=>\s*:matches\s*$""")
        private val MAP_RE = Regex("""^(.+?)(?::([lxyz]))?\s*,\s*(.+?)\s*=>\s*:map\s*$""")
        private val REDUCE_RE = Regex("""^(.+?)(?::([lxyz]))?\s*,\s*(.+?)\s*,\s*(.+?)\s*=>\s*:reduce\s*$""")
        private val FOLD_RE = Regex("""^(.+?)(?::([lxyz]))?\s*,\s*(.+?)\s*,\s*(.+?)\s*=>\s*:fold\s*$""")
        private val EACH_RE = Regex("""^(.+?)(?::([lxyz]))?\s*,\s*(.+?)\s*=>\s*:each\s*$""")
        private val JSON_GET_RE = Regex("""^(.+?)\s*,\s*(.+?)\s*=>\s*:json:get\s*$""")
        private val JSON_SET_RE = Regex("""^(.+?)\s*,\s*(.+?)\s*,\s*(.+?)\s*=>\s*:json:set\s*$""")
        private val JSON_NAMES_RE = Regex("""^(.+?)\s*=>\s*:json:names\s*$""")
        private val JSON_VALUES_RE = Regex("""^(.+?)\s*=>\s*:json:values\s*$""")
        private val FILE_RE = Regex("""^(.+?)(?:\s*,\s*(.+?))?\s*=>\s*:file:(\w+)\s*$""")
        private val API_WS_RE = Regex("""^(.+?)\s*=>\s*:api:websocket\s*$""")
        private val API_RE = Regex("""^(.+?)(?:\s*,\s*(.+?))?\s*=>\s*:api:(\w+)\s*$""")
        private val CONSOLE_RE = Regex("""^(.+?)\s*=>\s*:console\s*$""")
        private val SAVE_RE = Regex("""^(.+?)\s*,\s*(.+?)\s*=>\s*:save\s*$""")
        private val LOAD_RE = Regex("""^(.+?)(?:\s*,\s*(.+?))?\s*=>\s*:load\s*$""")
        private val GET_RE = Regex("""^(.+?)(?:\s*,\s*(.+?))?\s*=>\s*:get\s*$""")
        private val POST_RE = Regex("""^(.+?)\s*,\s*(.+?)\s*=>\s*:post\s*$""")
        private val SIZE_AXIS_RE = Regex("""^(.+?)(?::([lxyz]))?\s*=>\s*:size:([lxyz])\s*$""")
        private val SIZE_RE = Regex("""^(.+?)(?::([lxyz]))?\s*=>\s*:size\s*$""")
        private val SHORT_SIZE_RE = Regex("""^(.+?)(?::([lxyz]))?\s*=>\s*:s\s*$""")
        private val SHORT_SIZE_GET_RE = Regex("""^:s\s*=>\s*([A-Za-z_]\w*)\s*$""")
        private val SPLITTER_RE = Regex("""^\.\s*=>\s*([A-Za-z_]\w*):splitter:([lxyz])\s*:\s*(\S+)\s*$""")
        private val LOG_GET_RE = Regex("""^($LOG_SRC)\s*=>\s*($ATOM)\s*$""")
        private val LOG_MOVE_RE = Regex("""^log\s*->\s*($ATOM)\s*$""")
        private val LOG_FN_RE = Regex("""^(.+?)\s*=>\s*:log\s*$""")
        private val LOG_BARE_RE = Regex("""^:log\s*$""")
        private val MESSAGE_RE = Regex("""^(.+?)\s*=>\s*:message\s*$""")
        private val RANGE_RE = Regex("""^(.+?)\s*,\s*(.+?)\s*=>\s*:range\s*$""")
        private val REPEAT_RE = Regex("""^(.+?)\s*,\s*(.+?)\s*=>\s*:repeat\s*$""")
        private val SAVE_ACTIVE_RE = Regex("""^=>\s*:save\s*$""")
        private val FILE_SAVE_RE = Regex("""^([A-Za-z_]\w*):save\s*$""")
        private val FILE_PATH_RE = Regex("""^:file:([A-Za-z_]\w*)\s*=>\s*:path\s*$""")
        private val ERROR_GET_RE = Regex("""^:error(?::(line|message|lines|messages))?\s*=>\s*([A-Za-z_]\w*)\s*$""")
        private val ERROR_SET_RE = Regex("""^(.+?)\s*=>\s*:error\s*$""")
        private val ERROR_RESOLVED_RE = Regex("""^(\d+)\s*=>\s*:error:resolve\s*$""")
        private val FUNC_RETURN_RE = Regex("""^:([A-Za-z_]\w*)\s*$""")
        private val RESULT_RE = Regex("""^(.+?)\s*=>\s*:result\s*$""")
        private val ACTIVE_TAB_RE = Regex("""^(\S+)(?:\s*,\s*(\S+))?\s*=>\s*:activeTab\s*$""")
        private val FUNC_CALL_LOCAL_CHAIN_RE = Regex("""^(.+?)\s*=>\s*\.:([A-Za-z_]\w*):([A-Za-z_]\w*)\s*=>\s*([A-Za-z_]\w*)\s*$""")
        private val FUNC_CALL_LOCAL_RE = Regex("""^(.+?)\s*=>\s*:([A-Za-z_]\w*):([A-Za-z_]\w*)\s*$""")
        private val TAB_CALL_CHAIN_RE = Regex("""^(.+?)\s*=>\s*:([A-Za-z_]\w*)\s*=>\s*([A-Za-z_]\w*)\s*$""")
        private val TAB_CALL_RE = Regex("""^(.+?)\s*=>\s*:([A-Za-z_]\w*)\s*$""")
        private val TAB_CALL_ASYNC_RE = Regex("""^(.+?)\s*=>\s*:([A-Za-z_]\w*!)\s*$""")
        private val PATH_RE = Regex("""^([A-Za-z_]\w*)((?::[lxyz]:(?:first|last|\d+|[A-Za-z_]\w*))+)$""")
        private val INDEX_RE = Regex("""\b([A-Za-z_]\w*)((?::[lxyz]:(?:first|last|\d+|[A-Za-z_]\w*))+)\b""")
        private val DOLLAR_RE = Regex("""([A-Za-z_]\w*)\$([A-Za-z_]\w*)""")
        private val FILE_TO_VAR_RE = Regex("""^file:(\S+)\s*=>\s*($ATOM)\s*$""")
        private val VAR_TO_FILE_RE = Regex("""^($ATOM)\s*=>\s*file:(\S+)\s*$""")
        private val FILE_TO_FILE_RE = Regex("""^file:(\S+)\s*(->|=>)\s*file:(\S+)\s*$""")
        private val INSERT_ATOM_RE = Regex("""^(.+?)\s*(->\||=>\|)\s*($ATOM)\s*$""")
        private val AGENT_STATUS_RE = Regex("""^:agent:([A-Za-z_]\w*):status\s*=>\s*([A-Za-z_]\w*)\s*$""")
        private val AGENT_WAITER_RE = Regex("""^:agent:([A-Za-z_]\w*):waiter\s*=>\s*([A-Za-z_]\w*)\s*$""")
        private val AGENT_STATUS_ALL_RE = Regex("""^:agent:status\s*=>\s*([A-Za-z_]\w*)\s*$""")
        private val AGENT_WAIT_TIMEOUT_RE = Regex("""^(.+?)\s*,\s*(.+?)\s*=>\s*:agent:wait:timeout\s*$""")
        private val AGENT_WAIT_RE = Regex("""^(.+?)\s*=>\s*:agent:wait\s*$""")
        private val SIMPLE_RE = Regex("""^(.*?)\s*=>\s*:simple:([A-Za-z_]\w*)\s*$""")
        private val SHORT_MATH_NAMED_RE = Regex("""^(.+?)\s*=>\s*:(sum|min|max|avg|product|count)\s*$""")
        private val SHORT_DATA_NAMES = "push|pop|peek|reverse|unique|contains|slice|join|charAt|substring|indexOf|replace|lower|upper|trim|chars|remove|swap|deleteRange|replaceRange|size"
        private val SHORT_DATA_CHAIN_RE = Regex("""^(.+?)(?::([lxyz]))?\s*,\s*(.*?)\s*=>\s*:($SHORT_DATA_NAMES)\s*=>\s*([A-Za-z_]\w*)\s*$""")
        private val SHORT_DATA_RE = Regex("""^(.+?)(?::([lxyz]))?\s*,\s*(.*?)\s*=>\s*:($SHORT_DATA_NAMES)\s*$""")
        private val DATA_X_ONLY = setOf("substring", "replace", "chars")
        private val RESULT_NAMES_ONE = setOf(
            "math", "length", "join", "substring", "indexOf", "replace",
            "lower", "upper", "trim", "chars", "charAt", "reverse", "unique",
            "contains", "slice", "sort", "push", "pop", "pop_rest", "peek",
            "insert", "remove", "remove_rest", "swap", "set",
            "deleteRange", "replaceRange", "json", "file", "console", "run",
            "save", "load", "get", "post", "filter", "matches", "size", "ws",
            "result", "log", "parameters", "message", "range",
            "repeat", "s", "path", "error", "error:line", "error:message",
            "error:lines", "error:messages", "map", "reduce", "fold", "each", "simple"
        )
        private val RESULT_NAMES_SUBST = RESULT_NAMES_ONE - setOf("math", "data", "json", "api")
        private val KNOWN_RESULT_KEYS = setOf(
            "math:sum", "math:min", "math:max", "math:avg", "math:product",
            "math:count", "math:sort", "math:popcount", "math:clz", "math:ctz",
            "api:get", "api:post", "api:put", "api:delete", "api:patch", "api:websocket"
        )
        private val COLON_RESULT_ONE_RE = Regex(""":([A-Za-z_]\w*)""")
        private val COLON_RESULT_TWO_RE = Regex(""":([A-Za-z_]\w*):([A-Za-z_]\w*)""")
        private val RESULT_NAMING_RE = Regex(
            """^(:[A-Za-z_]\w*(?::[A-Za-z_]\w*)?)\s*(=>|=)\s*([A-Za-z_]\w*)\s*([*@]+)?\s*$"""
        )
    }
}