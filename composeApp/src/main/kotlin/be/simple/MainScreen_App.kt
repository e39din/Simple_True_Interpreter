package be.simple

import be.`true`.AgentAction
import be.`true`.Interpreter
import be.`true`.Status
import be.`true`.StepResult

class MainScreenApp {
    var initialized = false
    private var sys: MainScreenSystem? = null

    fun attachSystem(system: MainScreenSystem) { sys = system }
    fun activeTab(): TabState? = TabsModel.active()
    fun fileName(): String = activeTab()?.fileName ?: ""
    fun code(): String = activeTab()?.code ?: ""

    fun viewingAgent(): AgentState? {
        val id = Substrate.viewingAgentId ?: return null
        return AgentsModel.byId(id)
    }

    fun newInterpreter(callerAgentId: Int = -1): Interpreter = Interpreter(
        tabCaller = { name, data -> callTab(name, data) },
        tabCallerAsync = { name, data -> callTabAsync(name, data) { r -> onAsyncDone(name, r) } },
        tabExists = { name ->
            TabsModel.tabs.any {
                it.fileName.removeSuffix(".cycle") == name ||
                        it.fileName.removeSuffix(".agent") == name
            }
        },
        fileReader = { name -> TabsModel.byFileName(name)?.code },
        funcCaller = { src, fn, data, move -> callFunction(src, fn, data, move) },
        funcExists = { src, fn ->
            val tab = TabsModel.active() ?: return@Interpreter false
            InterpreterCompat.collectFunctions(tab.code).any { it.source == src && it.name == fn }
        },
        agentCaller = { callerId, name, action, arg -> callAgent(callerId, name, action, arg) },
        simpleCaller = { action, arg -> sys?.simpleAction(action, arg) },
        callerAgentId = callerAgentId
    )

    private fun callAgent(callerId: Int, name: String, action: AgentAction, arg: String?) {
        sys?.controlAgent(callerId, name, action, arg)
    }

    // ---------- tabs ----------

    fun ensureMainTab() {
        if (TabsModel.tabs.isEmpty()) {
            val interp = newInterpreter()
            TabsModel.create("main.cycle", Substrate.DEFAULT_CODE, interpreter = interp)
            ensureAgentFor("main.cycle")
            Substrate.addFileToOrder("main.cycle")
        }
        publishActive()
    }

    fun newTab() {
        var n = TabsModel.tabs.size + 1
        while (TabsModel.byFileName("tab$n.cycle") != null) n++
        val interp = newInterpreter()
        TabsModel.create("tab$n.cycle", Substrate.DEFAULT_CODE, interpreter = interp)
        ensureAgentFor("tab$n.cycle")
        Substrate.addFileToOrder("tab$n.cycle")
        publishActive()
    }

    fun newAgentTab() {
        var n = TabsModel.tabs.size + 1
        while (TabsModel.byFileName("agent-$n.agent") != null) n++
        val name = "agent-$n.agent"
        val interp = newInterpreter()
        TabsModel.create(name, Substrate.DEFAULT_CODE, interpreter = interp)
        ensureAgentFor(name)
        Substrate.addFileToOrder(name)
        publishActive()
    }

    fun openTab(name: String, code: String) {
        val interp = newInterpreter()
        TabsModel.open(name, code, interp)
        ensureAgentFor(name)
        Substrate.addFileToOrder(name)
        publishActive()
    }

    fun closeTab(index: Int) {
        val tab = TabsModel.tabs.getOrNull(index) ?: return
        sys?.deleteFileCascade(tab.fileName)
        TabsModel.close(index)
        publishActive()
    }

    fun setActiveTab(index: Int, fromStep: Int? = null) {
        TabsModel.setActive(index)
        val tab = TabsModel.active() ?: return
        if (fromStep != null) tab.currentStep = fromStep.coerceIn(1, tab.code.lines().size)
        Substrate.viewingAgentId = AgentsModel.agentFor(tab.fileName)?.id
        publishActive()
    }

    fun setActiveFile(fileName: String) {
        val idx = TabsModel.tabs.indexOfFirst { it.fileName == fileName }
        if (idx >= 0) setActiveTab(idx)
    }

    fun publishActive() {
        val tab = TabsModel.active() ?: return
        Substrate.activeFileName = tab.fileName
        Substrate.activeCode = tab.code
        Substrate.activeCursor = tab.cursorPosition
        val v = viewingAgent()
        val snapshot = v?.interpreter?.logSnapshot() ?: emptyList()
        Substrate.logEntries.clear(); Substrate.logLines.clear()
        for (e in snapshot) {
            Substrate.logEntries.add(e)
            Substrate.logLines.add(e.text)
        }
    }

    fun ensureAgentFor(fileName: String) {
        if (AgentsModel.agentFor(fileName) != null) return
        val n = AgentsModel.agents.size + 1
        val name = "agent-$n"
        val interp = newInterpreter()
        val a = AgentsModel.create(name, fileName, interp)
        if (a != null) {
            AgentsModel.recalcWaiter(a)
        }
        if (Substrate.viewingAgentId == null) {
            Substrate.viewingAgentId = AgentsModel.agentFor(fileName)?.id
        }
    }

    fun removeAgent(id: Int) {
        val a = AgentsModel.byId(id) ?: return
        sys?.stopAgent(id)
        AgentsModel.remove(id)
        if (Substrate.viewingAgentId == id) Substrate.viewingAgentId = null
        Substrate.log("Removed ${a.name}")
        publishActive()
    }

    fun applyDefault() {
        ensureMainTab()
        val tab = TabsModel.active() ?: return
        tab.fileName = "main.cycle"
        tab.kind = fileKindOf("main.cycle")
        tab.code = Substrate.DEFAULT_CODE
        tab.currentStep = 1
        tab.status = Status.STOPPED
        tab.cursorPosition = 0
        tab.interpreter.reset()
        syncVariablesFromCode(force = true)
        AgentsModel.agentFor(tab.fileName)?.let { a ->
            a.interpreter.reset()
            a.currentStep = 1
            a.status = Status.STOPPED
            a.lastPressed = null
            a.variablesSnapshot = emptyMap()
            a.resultsSnapshot = emptyMap()
            a.constantsSnapshot = emptySet()
            a.hiddenSnapshot = emptySet()
            AgentsModel.recalcWaiter(a)
        }
        publishActive()
    }

    fun applyCode(name: String, code: String) {
        ensureMainTab()
        val tab = TabsModel.active() ?: return
        tab.fileName = name
        tab.kind = fileKindOf(name)
        tab.code = code
        tab.currentStep = 1
        tab.status = Status.STOPPED
        tab.cursorPosition = 0
        tab.interpreter.reset()
        syncVariablesFromCode(force = true)
        publishActive()
    }

    fun openSamplesTab(item: SAMPLES.Item) {
        var n = TabsModel.tabs.size + 1
        while (TabsModel.byFileName("sample$n.cycle") != null) n++
        val interp = newInterpreter()
        TabsModel.create("sample$n.cycle", item.code, interpreter = interp)
        ensureAgentFor("sample$n.cycle")
        Substrate.addFileToOrder("sample$n.cycle")
        publishActive()
    }

    fun resetToStart() {
        val tab = activeTab() ?: return
        resetFile(tab.fileName)
    }

    fun resetFile(fileName: String) {
        val tab = TabsModel.byFileName(fileName) ?: return
        sys?.stopAgentForFile(fileName)
        tab.code = Substrate.startCode
        tab.dirty = false
        tab.currentStep = 1
        tab.status = Status.STOPPED
        AgentsModel.agentFor(fileName)?.let { a ->
            a.interpreter.reset()
            a.currentStep = 1
            a.status = Status.STOPPED
            a.lastPressed = null
            a.variablesSnapshot = emptyMap()
            a.resultsSnapshot = emptyMap()
            a.constantsSnapshot = emptySet()
            a.hiddenSnapshot = emptySet()
            AgentsModel.recalcWaiter(a)
        }
        Substrate.log("Reset file $fileName")
        publishActive()
    }

    fun saveCurrentAsStart() {
        val tab = activeTab() ?: return
        Substrate.startCode = tab.code
        Substrate.startFileName = "start.cycle"
        Substrate.log("Start state updated")
    }

    fun refreshVariables() { syncVariablesFromCode(force = false); publishActive() }

    fun syncVariablesFromCode(force: Boolean = false) {
        val tab = activeTab() ?: return
        val old = LinkedHashMap(tab.interpreter.variables)
        tab.interpreter.variables.clear()
        tab.interpreter.hiddenVariables.clear()
        for (line in tab.interpreter.parse(tab.code)) {
            val m = Regex("""^\s*([^\s=]+)\s*(?<!\\)=\s*([A-Za-z_]\w*)\s*([*@]+)?\s*$""").find(line.text) ?: continue
            val value = m.groupValues[1].trim()
            val name = m.groupValues[2].trim()
            val flags = m.groupValues[3]
            if (name.isEmpty() || name == "nothing") continue
            tab.interpreter.variables[name] =
                if (!force && old.containsKey(name)) old[name]!! else value
            if (flags.contains("*")) tab.interpreter.hiddenVariables += name
            if (flags.contains("@")) tab.interpreter.constants += name
        }
        AgentsModel.agentFor(tab.fileName)?.let { a ->
            a.variablesSnapshot = LinkedHashMap(tab.interpreter.variables)
            a.hiddenSnapshot = LinkedHashSet(tab.interpreter.hiddenVariables)
            a.constantsSnapshot = LinkedHashSet(tab.interpreter.constants)
            tab.interpreter.variables["color"]?.let { c -> Substrate.parseColor(c)?.let { a.color = it } }
            tab.interpreter.variables["speed"]?.toLongOrNull()?.let { a.speedMs = it.coerceIn(0L, 5000L) }
            AgentsModel.recalcWaiter(a)
        }
    }

    fun validateTab(tab: TabState): Boolean {
        val errors = tab.interpreter.validate(tab.interpreter.parse(tab.code), Substrate.strictMode)
        Substrate.validationErrors = errors
        Substrate.errorLines = errors.map { it.lineNumber }.toSet()
        if (errors.isNotEmpty()) {
            errors.forEach {
                tab.interpreter.appendLog("Line ${it.lineNumber}: ${it.message}", "error", it.lineNumber)
            }
            return false
        }
        return true
    }

    fun validateCurrent(): Boolean {
        val tab = activeTab() ?: return false
        if (!validateTab(tab)) { publishActive(); return false }
        syncVariablesFromCode(force = false)
        return true
    }

    fun canStep(): Boolean {
        val a = viewingAgent() ?: return false
        return a.status != Status.RUNNING
    }

    fun stepOn(agent: AgentState): StepResult? {
        val tab = TabsModel.byFileName(agent.fileName) ?: return null
        if (!validateTab(tab)) { publishActive(); return null }
        val r = sys?.stepAgent(agent.id)
        publishActive()
        return r
    }

    fun step(): StepResult? {
        val a = viewingAgent() ?: return null
        return stepOn(a)
    }

    fun stepBack(): StepResult? {
        val a = viewingAgent() ?: return null
        if (a.status == Status.RUNNING) return null
        return sys?.stepBackAgent(a.id)
    }

    fun stepAgentInternal(agentId: Int): StepResult? {
        val agent = AgentsModel.byId(agentId) ?: return null
        val tab = TabsModel.byFileName(agent.fileName) ?: return null
        val result = agent.interpreter.executeOneStep(tab.code)
        applyResult(agent, result)
        return result
    }

    fun startPlay() {
        val a = viewingAgent() ?: return
        if (!validateTab(TabsModel.byFileName(a.fileName) ?: return)) { publishActive(); return }
        sys?.startAgent(a.id)
    }

    fun stopPlay() {
        val a = viewingAgent() ?: return
        sys?.stopAgent(a.id)
    }

    fun togglePlayPause() {
        val anyRunning = AgentsModel.agents.any { it.status == Status.RUNNING }
        if (anyRunning) sys?.pauseAllAgents() else sys?.startAllAgents()
    }

    fun stepAll() { sys?.stepAllAgents() }
    fun stepBackAll() { sys?.stepBackAllAgents() }

    fun reset() { resetToStart() }
    fun setSpeed(ms: Long) {
        viewingAgent()?.let { agent ->
            agent.speedMs = ms.coerceIn(0L, 5000L)
            agent.interpreter.variables["speed"] = agent.speedMs.toString()
        }
    }
    fun rememberProject(name: String) { Substrate.history = (Substrate.history + name).distinct().takeLast(5) }
    fun autosave() { if (validateCurrent()) rememberProject(fileName()) }

    fun insertAtCursor(template: String) {
        val tab = activeTab() ?: return
        val pos = tab.cursorPosition.coerceIn(0, tab.code.length)
        pushUndo(tab)
        tab.code = tab.code.substring(0, pos) + template + tab.code.substring(pos)
        tab.cursorPosition = pos + template.length
        tab.dirty = true
        syncVariablesFromCode(force = false)
        publishActive()
    }

    fun updateCode(newCode: String, forceRename: Boolean = false) {
        val tab = activeTab() ?: return
        if (newCode == tab.code) return
        val oldCode = tab.code
        pushUndo(tab)
        tab.code = newCode
        tab.dirty = true
        Substrate.activeCode = newCode
        if (!forceRename) {
            val renames = detectRenames(oldCode, newCode)
            for ((oldName, newName) in renames) {
                if (oldName in tab.interpreter.variables && newName !in tab.interpreter.variables) {
                    tab.code = renameInCode(tab.code, oldName, newName)
                    val v = tab.interpreter.variables.remove(oldName) ?: ""
                    tab.interpreter.variables[newName] = v
                    if (oldName in tab.interpreter.constants) {
                        tab.interpreter.constants.remove(oldName)
                        tab.interpreter.constants.add(newName)
                    }
                    if (oldName in tab.interpreter.hiddenVariables) {
                        tab.interpreter.hiddenVariables.remove(oldName)
                        tab.interpreter.hiddenVariables.add(newName)
                    }
                }
            }
        }
        syncVariablesFromCode(force = false)
        publishActive()
    }

    private fun detectRenames(oldCode: String, newCode: String): List<Pair<String, String>> {
        val oldLines = oldCode.lines()
        val newLines = newCode.lines()
        if (oldLines.size != newLines.size) return emptyList()
        val out = mutableListOf<Pair<String, String>>()
        val re = Regex("""^\s*([^\s=]+)\s*(?<!\\)=\s*([A-Za-z_]\w*)\s*([*@]+)?\s*$""")
        for (i in oldLines.indices) {
            val a = re.find(oldLines[i]) ?: continue
            val b = re.find(newLines[i]) ?: continue
            val oldValue = a.groupValues[1].trim()
            val newValue = b.groupValues[1].trim()
            val oldName = a.groupValues[2].trim()
            val newName = b.groupValues[2].trim()
            val oldFlags = a.groupValues[3]
            val newFlags = b.groupValues[3]
            if (oldName != newName && oldValue == newValue && oldFlags == newFlags) {
                out += oldName to newName
            }
        }
        return out
    }

    private fun renameInCode(code: String, oldName: String, newName: String): String {
        val re = Regex("(?<![A-Za-z0-9_:])${Regex.escape(oldName)}(?![A-Za-z0-9_])")
        return re.replace(code, newName)
    }

    fun updateCursor(pos: Int) {
        val tab = activeTab() ?: return
        tab.cursorPosition = pos
        Substrate.activeCursor = pos
    }

    fun undo() {
        val tab = activeTab() ?: return
        val prev = tab.undoStack.removeLastOrNull() ?: return
        tab.redoStack.addLast(tab.code)
        tab.code = prev
        tab.dirty = true
        Substrate.activeCode = prev
        syncVariablesFromCode(force = false)
        publishActive()
    }

    fun redo() {
        val tab = activeTab() ?: return
        val next = tab.redoStack.removeLastOrNull() ?: return
        tab.undoStack.addLast(tab.code)
        tab.code = next
        tab.dirty = true
        Substrate.activeCode = next
        syncVariablesFromCode(force = false)
        publishActive()
    }

    private fun pushUndo(tab: TabState) {
        tab.undoStack.addLast(tab.code)
        while (tab.undoStack.size > 100) tab.undoStack.removeFirst()
        tab.redoStack.clear()
    }

    fun renameActive(newName: String) {
        val tab = activeTab() ?: return
        if (newName.isBlank()) return
        val old = tab.fileName
        tab.fileName = newName
        tab.kind = fileKindOf(newName)
        Substrate.activeFileName = newName
        Substrate.fileOrder = Substrate.fileOrder.map { if (it == old) newName else it }
    }

    fun deleteVariable(name: String) {
        val agent = viewingAgent() ?: return
        val tab = TabsModel.byFileName(agent.fileName) ?: return
        agent.interpreter.variables.remove(name)
        agent.interpreter.hiddenVariables.remove(name)
        agent.interpreter.constants.remove(name)
        agent.interpreter.results.remove(name)
        val regex = Regex(
            """^([^\s=]+)[ \t]*=[ \t]*${Regex.escape(name)}([ \t]*[*@]*)?[ \t]*$""",
            RegexOption.MULTILINE
        )
        tab.code = regex.replace(tab.code) { "" }.replace(Regex("""\n{3,}"""), "\n\n")
        tab.dirty = true
        agent.variablesSnapshot = LinkedHashMap(agent.interpreter.variables)
        agent.hiddenSnapshot = LinkedHashSet(agent.interpreter.hiddenVariables)
        agent.constantsSnapshot = LinkedHashSet(agent.interpreter.constants)
        Substrate.log("Destroyed variable '$name' (UI)")
        publishActive()
    }

    fun markSampleDone(tab: TabState) {
        val match = SAMPLES.items.firstOrNull { it.code.trim() == tab.code.trim() }
        if (match != null && match.id !in Substrate.completedSamples) {
            Substrate.markSampleDone(match.id)
            sys?.saveProgress()
            Substrate.log("✓ Sample '${match.title}' completed")
            if (Substrate.completedSamples.size == SAMPLES.items.size) {
                Substrate.log("🎉 Все ${SAMPLES.items.size} примера пройдены!")
            }
        }
    }

    fun applyResult(agent: AgentState, result: StepResult) {
        if (result.command != null) {
            when {
                result.command == ":stop" -> { sys?.stopAgent(agent.id); return }
                result.command == ":pause" -> { sys?.pauseAgent(agent.id); return }
                result.command == ":resume" -> { sys?.startAgent(agent.id); return }
                result.command == ":delete" -> { removeAgent(agent.id); return }
                result.command == ":reset" -> { resetFile(agent.fileName); return }
                result.command.startsWith("file:") -> {
                    val target = result.command.removePrefix("file:")
                    sys?.switchAgentFile(agent, target)
                    return
                }
                result.command.startsWith("activeTab:") -> {
                    val idx = result.command.removePrefix("activeTab:").toIntOrNull() ?: 1
                    setActiveTab(idx - 1, result.currentStep)
                    return
                }
            }
        }
        agent.currentStep = result.currentStep
        agent.status = when {
            agent.status == Status.RUNNING && result.status == Status.STOPPED -> Status.RUNNING
            else -> result.status
        }
        agent.variablesSnapshot = LinkedHashMap(agent.interpreter.variables)
        agent.hiddenSnapshot = LinkedHashSet(agent.interpreter.hiddenVariables)
        agent.resultsSnapshot = LinkedHashMap(agent.interpreter.results)
        agent.constantsSnapshot = LinkedHashSet(agent.interpreter.constants)

        agent.interpreter.variables["color"]?.let { c -> Substrate.parseColor(c)?.let { agent.color = it } }
        agent.interpreter.variables["speed"]?.toLongOrNull()?.let { agent.speedMs = it.coerceIn(0L, 5000L) }

        val agentPrefix = "agent:${agent.name}"
        Substrate.appendLogEntry(be.`true`.LogEntry(
            "[${agent.name}] ${result.log}",
            agentPrefix,
            result.errorLine ?: (result.currentStep - 1)
        ))
        result.changedVariables.forEach { (k, v) ->
            Substrate.appendLogEntry(be.`true`.LogEntry("  $k = $v", agentPrefix))
        }
        result.removedVariables.forEach {
            Substrate.appendLogEntry(be.`true`.LogEntry("  removed: $it", agentPrefix))
        }

        if (result.errorLine != null) {
            Substrate.errorLines = Substrate.errorLines + result.errorLine
        } else if (result.status == Status.FINISHED) {
            TabsModel.byFileName(agent.fileName)?.let { markSampleDone(it) }
            sys?.onAgentFinished(agent)
        }

        syncCodeWithVariables(agent, result.changedVariables)
        if (agent.id == Substrate.viewingAgentId) syncVariablesFromCode(force = false)
    }

    private fun syncCodeWithVariables(agent: AgentState, changed: Map<String, String>) {
        if (changed.isEmpty()) return
        if (!Substrate.autoSyncDeclarations) return
        val tab = TabsModel.byFileName(agent.fileName) ?: return
        var newCode = tab.code
        for ((name, value) in changed) {
            if (name in agent.interpreter.constants) continue
            val regex = Regex(
                """^([^\s=]+)[ \t]*=[ \t]*${Regex.escape(name)}([ \t]*[*@]*)?[ \t]*$""",
                RegexOption.MULTILINE
            )
            var rewritten = 0
            newCode = regex.replace(newCode) { m ->
                rewritten++
                "$value = $name${m.groupValues[2]}"
            }
            if (rewritten > 0) {
                Substrate.log("Rewrote '$name' on $rewritten declaration(s)")
            }
        }
        if (newCode != tab.code) {
            tab.code = newCode
            if (tab.id == TabsModel.active()?.id) Substrate.activeCode = newCode
        }
    }

    fun callTab(name: String, params: String): String {
        return sys?.callFile(name, params) ?: ""
    }

    fun callTabAsync(name: String, params: String, onDone: (String) -> Unit) {
        sys?.launchAsync { onDone(callTab(name, params)) }
    }

    private fun onAsyncDone(name: String, result: String) {
        val a = viewingAgent() ?: return
        a.interpreter.results[name] = result
        Substrate.log("Async $name completed → ${result.take(40)}")
        publishActive()
    }

    fun callFunction(source: String, name: String, data: String, move: Boolean): String {
        val tab = activeTab() ?: return ""
        val fn = InterpreterCompat.collectFunctions(tab.code).firstOrNull { it.source == source && it.name == name } ?: return ""
        val local = newInterpreter()
        local.variables["parameters"] = data
        var guard = 0
        val body = fn.body.joinToString("\n") { it.text }
        while (guard < 10000) {
            guard++
            val res = local.executeOneStep(body)
            if (res.status == Status.FINISHED) break
            if (res.errorLine != null) break
        }
        if (move) Substrate.log("Func call (move) $source:$name")
        return local.results["result"] ?: ""
    }
}

private object InterpreterCompat {
    fun collectFunctions(code: String): List<be.`true`.FunctionDef> =
        be.`true`.Interpreter().collectFunctions(code)
}