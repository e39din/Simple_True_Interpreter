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
        tabExists = { name -> TabsModel.tabs.any { it.fileName.removeSuffix(".true") == name } },
        funcCaller = { src, fn, data, move -> callFunction(src, fn, data, move) },
        funcExists = { src, fn ->
            val tab = TabsModel.active() ?: return@Interpreter false
            InterpreterCompat.collectFunctions(tab.code).any { it.source == src && it.name == fn }
        },
        agentCaller = { callerId, name, action, arg -> callAgent(callerId, name, action, arg) },
        callerAgentId = callerAgentId
    )

    private fun callAgent(callerId: Int, name: String, action: AgentAction, arg: String?) {
        sys?.controlAgent(callerId, name, action, arg)
    }

    fun ensureMainTab() {
        if (TabsModel.tabs.isEmpty()) {
            val interp = newInterpreter()
            TabsModel.create("main.true", Substrate.DEFAULT_CODE, interpreter = interp)
            ensureAgentFor("main.true")
        }
        publishActive()
    }

    fun newTab() {
        val n = TabsModel.tabs.size + 1
        val interp = newInterpreter()
        TabsModel.create("tab$n.true", Substrate.DEFAULT_CODE, interpreter = interp)
        ensureAgentFor("tab$n.true")
        publishActive()
    }

    fun openTab(name: String, code: String) {
        val interp = newInterpreter()
        TabsModel.open(name, code, interp)
        ensureAgentFor(name)
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
        val agent = AgentsModel.agentFor(fileName)
        if (agent != null && agent.status == Status.RUNNING && fileName != Substrate.activeFileName) {
            Substrate.pendingNavigateFile = fileName
            return
        }
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
        AgentsModel.create(name, fileName, interp)
        if (Substrate.viewingAgentId == null) {
            Substrate.viewingAgentId = AgentsModel.agentFor(fileName)?.id
        }
    }

    fun createAgentFor(fileName: String) {
        if (AgentsModel.agentFor(fileName) != null) {
            Substrate.log("Agent already exists on $fileName")
            return
        }
        if (AgentsModel.agents.size >= AgentsModel.MAX_AGENTS) {
            Substrate.log("Max ${AgentsModel.MAX_AGENTS} agents reached")
            return
        }
        val n = AgentsModel.agents.size + 1
        val interp = newInterpreter()
        val a = AgentsModel.create("agent-$n", fileName, interp)
        if (a != null) {
            Substrate.viewingAgentId = a.id
            Substrate.log("Created ${a.name} on $fileName")
        }
        publishActive()
    }

    fun removeAgent(id: Int) {
        val a = AgentsModel.byId(id) ?: return
        sys?.stopAgent(id)
        AgentsModel.remove(id)
        if (Substrate.viewingAgentId == id) Substrate.viewingAgentId = null
        Substrate.log("Removed ${a.name}")
        publishActive()
    }

    fun viewingAgentOrNull(): AgentState? = viewingAgent()

    fun applyDefault() {
        ensureMainTab()
        val tab = TabsModel.active() ?: return
        tab.fileName = "main.true"
        tab.code = Substrate.DEFAULT_CODE
        tab.currentStep = 1
        tab.status = Status.STOPPED
        tab.cursorPosition = 0
        tab.interpreter.reset()
        syncVariablesFromCode()
        AgentsModel.agentFor(tab.fileName)?.let { a ->
            a.interpreter.reset()
            a.currentStep = 1
            a.status = Status.STOPPED
            a.variablesSnapshot = emptyMap()
            a.resultsSnapshot = emptyMap()
            a.constantsSnapshot = emptySet()
            a.hiddenSnapshot = emptySet()
        }
        publishActive()
    }

    fun applyCode(name: String, code: String) {
        ensureMainTab()
        val tab = TabsModel.active() ?: return
        tab.fileName = name
        tab.code = code
        tab.currentStep = 1
        tab.status = Status.STOPPED
        tab.cursorPosition = 0
        tab.interpreter.reset()
        syncVariablesFromCode()
        publishActive()
    }

    fun openSamplesTab(item: SAMPLES.Item) {
        val n = TabsModel.tabs.size + 1
        val interp = newInterpreter()
        TabsModel.create("sample$n.true", item.code, interpreter = interp)
        ensureAgentFor("sample$n.true")
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
            a.variablesSnapshot = emptyMap()
            a.resultsSnapshot = emptyMap()
            a.constantsSnapshot = emptySet()
            a.hiddenSnapshot = emptySet()
        }
        Substrate.log("Reset file $fileName")
        publishActive()
    }

    fun saveCurrentAsStart() {
        val tab = activeTab() ?: return
        Substrate.startCode = tab.code
        Substrate.startFileName = "start.true"
        Substrate.log("Start state updated")
    }

    fun refreshVariables() { syncVariablesFromCode(); publishActive() }

    fun syncVariablesFromCode() {
        val tab = activeTab() ?: return
        tab.interpreter.variables.clear()
        tab.interpreter.hiddenVariables.clear()
        for (line in tab.interpreter.parse(tab.code)) {
            val m = Regex("""^\s*([^\s=]+)\s*(?<!\\)=\s*([A-Za-z_]\w*)\s*([*@]+)?\s*$""").find(line.text) ?: continue
            val value = m.groupValues[1].trim()
            val name = m.groupValues[2].trim()
            val flags = m.groupValues[3]
            if (name.isEmpty() || name == "nothing") continue
            tab.interpreter.variables[name] = value
            if (flags.contains("*")) tab.interpreter.hiddenVariables += name
            if (flags.contains("@")) tab.interpreter.constants += name
        }
        AgentsModel.agentFor(tab.fileName)?.let { a ->
            a.variablesSnapshot = LinkedHashMap(tab.interpreter.variables)
            a.hiddenSnapshot = LinkedHashSet(tab.interpreter.hiddenVariables)
            a.constantsSnapshot = LinkedHashSet(tab.interpreter.constants)
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
        syncVariablesFromCode()
        return true
    }

    fun canStep(): Boolean {
        val a = viewingAgent() ?: return false
        return a.status != Status.RUNNING
    }

    fun stepOn(agent: AgentState): StepResult? {
        val tab = TabsModel.byFileName(agent.fileName) ?: return null
        if (!validateTab(tab)) { publishActive(); return null }
        return sys?.stepAgent(agent.id)
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

    fun reset() { resetToStart() }
    fun setSpeed(ms: Long) { viewingAgent()?.speedMs = ms.coerceIn(0L, 5000L) }
    fun rememberProject(name: String) { Substrate.history = (Substrate.history + name).distinct().takeLast(5) }
    fun autosave() { if (validateCurrent()) rememberProject(fileName()) }

    fun insertAtCursor(template: String) {
        val tab = activeTab() ?: return
        val pos = tab.cursorPosition.coerceIn(0, tab.code.length)
        pushUndo(tab)
        tab.code = tab.code.substring(0, pos) + template + tab.code.substring(pos)
        tab.cursorPosition = pos + template.length
        tab.dirty = true
        publishActive()
    }

    fun updateCode(newCode: String) {
        val tab = activeTab() ?: return
        if (newCode == tab.code) return
        pushUndo(tab)
        tab.code = newCode
        tab.dirty = true
        Substrate.activeCode = newCode
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
        publishActive()
    }

    fun redo() {
        val tab = activeTab() ?: return
        val next = tab.redoStack.removeLastOrNull() ?: return
        tab.undoStack.addLast(tab.code)
        tab.code = next
        tab.dirty = true
        Substrate.activeCode = next
        publishActive()
    }

    private fun pushUndo(tab: TabState) {
        tab.undoStack.addLast(tab.code)
        while (tab.undoStack.size > 100) tab.undoStack.removeFirst()
        tab.redoStack.clear()
    }

    fun renameActive(newName: String) {
        val tab = activeTab() ?: return
        val fixed = if (newName.endsWith(".true")) newName else "$newName.true"
        tab.fileName = fixed
        Substrate.activeFileName = fixed
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
        if (result.command != null && result.command.startsWith("activeTab:")) {
            val idx = result.command.removePrefix("activeTab:").toIntOrNull() ?: 1
            setActiveTab(idx - 1, result.currentStep)
            return
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

        val type = when {
            result.errorLine != null -> "error"
            result.log.startsWith("Jump") -> "jump"
            result.log.contains("Command") -> "command"
            result.command == "destroy" -> "destroy"
            else -> "step"
        }
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
        }

        syncCodeWithVariables(agent, result.changedVariables)
        if (agent.id == Substrate.viewingAgentId) syncVariablesFromCode()
        publishActive()
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
        val tab = TabsModel.tabs.firstOrNull { it.fileName.removeSuffix(".true") == name } ?: return ""
        val local = newInterpreter()
        local.variables["parameters"] = params
        var guard = 0
        while (guard < 10000) {
            guard++
            val res = local.executeOneStep(tab.code)
            if (res.status == Status.FINISHED) break
            if (res.errorLine != null) break
        }
        return local.returnValue ?: ""
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
        return local.returnValue ?: ""
    }
}

private object InterpreterCompat {
    fun collectFunctions(code: String): List<be.`true`.FunctionDef> =
        be.`true`.Interpreter().collectFunctions(code)
}