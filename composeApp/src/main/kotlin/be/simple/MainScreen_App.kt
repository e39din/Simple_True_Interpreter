package be.simple

import be.`true`.Status
import be.`true`.StepResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainScreenApp {
    var initialized = false
    private val asyncScope = CoroutineScope(Dispatchers.Default)
    private var sys: MainScreenSystem? = null

    fun attachSystem(system: MainScreenSystem) { sys = system }
    fun activeTab(): TabState? = TabsModel.active()
    fun fileName(): String = activeTab()?.fileName ?: ""
    fun code(): String = activeTab()?.code ?: ""

    fun newInterpreter(): be.`true`.Interpreter = be.`true`.Interpreter(
        tabCaller = { name, data -> callTab(name, data) },
        tabCallerAsync = { name, data -> callTabAsync(name, data) { r -> onAsyncDone(name, r) } },
        tabExists = { name -> TabsModel.tabs.any { it.fileName.removeSuffix(".true") == name } },
        funcCaller = { src, fn, data, move -> callFunction(src, fn, data, move) },
        funcExists = { src, fn ->
            val tab = TabsModel.active() ?: return@Interpreter false
            InterpreterCompat.collectFunctions(tab.code).any { it.source == src && it.name == fn }
        }
    )

    fun ensureMainTab() {
        if (TabsModel.tabs.isEmpty()) {
            TabsModel.create("main.true", Substrate.DEFAULT_CODE, interpreter = newInterpreter())
        }
        publishActive()
    }

    fun newTab() {
        val n = TabsModel.tabs.size + 1
        TabsModel.create("tab$n.true", Substrate.DEFAULT_CODE, interpreter = newInterpreter())
        publishActive()
    }

    fun openTab(name: String, code: String) { TabsModel.open(name, code, newInterpreter()); publishActive() }
    fun closeTab(index: Int) {
        if (Substrate.runningTabId == TabsModel.tabs.getOrNull(index)?.id) {
            stopPlay(); sys?.stopAutoRun()
        }
        TabsModel.close(index)
        publishActive()
    }

    fun setActiveTab(index: Int, fromStep: Int? = null) {
        TabsModel.setActive(index)
        val tab = TabsModel.active() ?: return
        if (fromStep != null) tab.currentStep = fromStep.coerceIn(1, tab.code.lines().size)
        publishActive()
    }

    fun publishActive() {
        val tab = TabsModel.active() ?: return
        Substrate.activeFileName = tab.fileName
        Substrate.activeCode = tab.code
        Substrate.activeCursor = tab.cursorPosition
        Substrate.status = tab.status
        Substrate.currentStep = tab.currentStep
        Substrate.constantsSnapshot = LinkedHashSet(tab.interpreter.constants)
        Substrate.variablesSnapshot = LinkedHashMap(tab.interpreter.variables)
        Substrate.hiddenSnapshot = LinkedHashSet(tab.interpreter.hiddenVariables)
        Substrate.resultsSnapshot = LinkedHashMap(tab.interpreter.results)
        val snapshot = tab.interpreter.logSnapshot()
        Substrate.logEntries.clear(); Substrate.logLines.clear()
        for (e in snapshot) {
            Substrate.logEntries.add(e)
            Substrate.logLines.add(e.text)
        }
    }

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
        TabsModel.create("sample$n.true", item.code, interpreter = newInterpreter())
        publishActive()
    }

    fun resetToStart() {
        ensureMainTab()
        val tab = TabsModel.active() ?: return
        tab.fileName = Substrate.startFileName
        tab.code = Substrate.startCode
        tab.currentStep = 1
        tab.status = Status.STOPPED
        tab.cursorPosition = 0
        tab.interpreter.reset()
        syncVariablesFromCode()
        publishActive()
    }

    fun saveCurrentAsStart() {
        Substrate.startCode = code()
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
            if (name.isEmpty()) continue
            tab.interpreter.variables[name] = value
            if (flags.contains("*")) tab.interpreter.hiddenVariables += name
            if (flags.contains("@")) tab.interpreter.constants += name
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

    fun canStep(): Boolean = Substrate.status != Status.RUNNING

    fun stepOn(tab: TabState): StepResult? {
        if (!validateTab(tab)) { publishActive(); return null }
        val result = tab.interpreter.executeOneStep(tab.code)
        if (result.errorLine != null) {
            tab.status = Status.PAUSED
            tab.interpreter.forceStep(result.errorLine)
            tab.currentStep = result.errorLine
            Substrate.errorLines = Substrate.errorLines + result.errorLine
        }
        applyResult(tab, result)
        return result
    }

    fun step(): StepResult? {
        val tab = activeTab() ?: return null
        return stepOn(tab)
    }

    fun stepBack(): StepResult? {
        if (!canStep()) return null
        val tab = activeTab() ?: return null
        val result = tab.interpreter.executeStepBack() ?: run {
            tab.interpreter.appendLog("No steps to go back", "info")
            publishActive()
            return null
        }
        applyResult(tab, result)
        return result
    }

    fun startPlay() {
        val tab = activeTab() ?: return
        if (!validateTab(tab)) { publishActive(); return }
        Substrate.runningTabId = tab.id
        tab.status = Status.RUNNING
        Substrate.status = Status.RUNNING
        sys?.startAutoRun(tab.id)
    }

    fun stopPlay() { Substrate.runningTabId = null }

    fun reset() { stopPlay(); resetToStart() }
    fun setSpeed(ms: Long) { Substrate.speedMs = ms.coerceIn(0L, 5000L) }
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

    fun applyResult(tab: TabState, result: StepResult) {
        if (result.command != null && result.command.startsWith("activeTab:")) {
            val idx = result.command.removePrefix("activeTab:").toIntOrNull() ?: 1
            setActiveTab(idx - 1, result.currentStep)
            return
        }
        tab.currentStep = result.currentStep
        tab.status = when {
            tab.status == Status.RUNNING && result.status == Status.STOPPED -> Status.RUNNING
            else -> result.status
        }
        val type = when {
            result.errorLine != null -> "error"
            result.log.startsWith("Jump") -> "jump"
            result.log.contains("Command") -> "command"
            else -> "step"
        }
        tab.interpreter.appendLog(result.log, type, result.errorLine ?: (result.currentStep - 1))
        result.changedVariables.forEach { (k, v) -> tab.interpreter.appendLog("  $k = $v", "info") }
        result.removedVariables.forEach { tab.interpreter.appendLog("  removed: $it", "info") }

        if (result.errorLine != null) {
            tab.interpreter.appendLog("✗ Paused at step ${result.errorLine}: ${result.log}", "error", result.errorLine)
            Substrate.errorLines = Substrate.errorLines + result.errorLine
        } else if (result.status == Status.FINISHED) {
            tab.interpreter.appendLog("✓ Program finished at step ${result.currentStep}", "info")
            markSampleDone(tab)
        }

        syncCodeWithVariables(tab, result.changedVariables)
        if (tab.id == activeTab()?.id) syncVariablesFromCode()
        publishActive()
    }

    private fun syncCodeWithVariables(tab: TabState, changed: Map<String, String>) {
        if (changed.isEmpty()) return
        if (!Substrate.autoSyncDeclarations) return
        var newCode = tab.code
        for ((name, value) in changed) {
            if (name in tab.interpreter.constants) continue
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
                tab.interpreter.appendLog("Rewrote '$name' on $rewritten declaration(s)", "info")
            }
        }
        if (newCode != tab.code) {
            tab.code = newCode
            if (tab.id == activeTab()?.id) Substrate.activeCode = newCode
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
        asyncScope.launch {
            val result = callTab(name, params)
            withContext(Dispatchers.Main) { onDone(result) }
        }
    }

    private fun onAsyncDone(name: String, result: String) {
        val tab = activeTab() ?: return
        tab.interpreter.results[name] = result
        tab.interpreter.appendLog("Async $name completed → ${result.take(40)}", "info")
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

/** Тонкая обёртка над Interpreter.collectFunctions; сами функции уже в Interpreter. */
private object InterpreterCompat {
    fun collectFunctions(code: String): List<be.`true`.FunctionDef> =
        be.`true`.Interpreter().collectFunctions(code)
}