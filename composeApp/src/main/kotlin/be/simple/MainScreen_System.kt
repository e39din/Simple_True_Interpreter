package be.simple

import androidx.compose.ui.unit.dp
import be.`true`.AgentAction
import be.`true`.Status
import be.`true`.StepResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.awt.Desktop
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Robot
import java.awt.event.InputEvent
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Properties
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import javax.swing.JFileChooser

class MainScreenSystem(private val app: MainScreenApp) {
    private var autoSaveJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private val columnJobs = mutableMapOf<String, Job>()
    private val agentDeferred = mutableMapOf<Int, CompletableDeferred<String>>()

    private val baseDir: File
        get() = File(System.getProperty("user.home"), ".simple").apply { mkdirs() }

    var projectDir: File? = null
        private set

    fun loadLastOrMain() {
        if (app.initialized) return
        app.initialized = true
        try {
            loadSettings()
            loadProgress()
            val startContent = load("start.cycle")
            if (!startContent.isNullOrBlank()) {
                Substrate.startCode = startContent
                Substrate.startFileName = "start.cycle"
            } else {
                Substrate.startCode = Substrate.DEFAULT_CODE
                save("start.cycle", Substrate.startCode)
            }
            refreshRecentFromDisk()
            val main = "main.cycle"
            val content = load(main)
            val code = content ?: Substrate.startCode
            if (content.isNullOrBlank()) save(main, Substrate.startCode)
            TabsModel.create(main, code, interpreter = app.newInterpreter())
            app.ensureAgentFor(main)
            pushRecent(main)
            app.ensureMainTab()
            if (AgentsModel.agents.none { it.fileName.endsWith(".agent") }) {
                val agentFile = File(baseDir, "MrGreen.agent")
                if (!agentFile.exists()) {
                    agentFile.writeText(Substrate.DEFAULT_AGENT_CODE)
                }
                val mrCode = agentFile.readText()
                val mrInterp = app.newInterpreter()
                TabsModel.create("MrGreen.agent", mrCode, makeActive = false, interpreter = mrInterp)
                app.ensureAgentFor("MrGreen.agent")
                Substrate.addFileToOrder("MrGreen.agent")
            }
            if (Substrate.fileOrder.isEmpty()) {
                Substrate.fileOrder = TabsModel.tabs.map { it.fileName }
            } else {
                val existing = TabsModel.tabs.map { it.fileName }.toSet()
                Substrate.fileOrder = Substrate.fileOrder.filter { it in existing } +
                        (existing - Substrate.fileOrder.toSet())
            }
            if (!File(baseDir, "settings.true").exists()) {
                Substrate.showTemplatePicker = true
            }
        } catch (e: Exception) {
            Substrate.log("Init failed: ${e.message}")
            app.applyDefault()
        }
    }

    fun loadSettings() {
        try {
            val f = File(baseDir, "settings.true")
            if (!f.exists()) return
            for (l in f.readLines()) {
                val kv = l.split("=", limit = 2)
                if (kv.size != 2) continue
                when (kv[0].trim()) {
                    "autoSyncDeclarations" -> Substrate.autoSyncDeclarations = kv[1].trim() == "true"
                    "speedMs" -> Substrate.speedMs = kv[1].trim().toLongOrNull() ?: 400L
                    "strictMode" -> Substrate.strictMode = kv[1].trim() == "true"
                    "valuesCollapsed" -> Substrate.valuesCollapsed = kv[1].trim() == "true"
                    "actionsCollapsed" -> Substrate.actionsCollapsed = kv[1].trim() == "true"
                    "valuesWidth" -> Substrate.valuesWidthOverride = kv[1].trim().toFloatOrNull()?.dp
                    "actionsWidth" -> Substrate.actionsWidthOverride = kv[1].trim().toFloatOrNull()?.dp
                    "logHeight" -> Substrate.logHeightOverride = kv[1].trim().toFloatOrNull()?.dp
                    "fileOrder" -> Substrate.fileOrder = kv[1].trim().split(",").filter { it.isNotBlank() }
                    "recentProjects" -> Substrate.recentProjects = kv[1].trim().split(",").filter { it.isNotBlank() }
                    "pinned" -> {
                        Substrate.pinnedList.clear()
                        kv[1].trim().split(",").filter { it.isNotBlank() }.forEach { Substrate.pinnedList.add(it) }
                    }
                    "lang" -> Strings.lang = kv[1].trim()
                }
            }
        } catch (_: Exception) {}
    }

    fun saveSettings() {
        try {
            val activeAgent = app.viewingAgent()
            File(baseDir, "settings.true").writeText(
                "autoSyncDeclarations=${Substrate.autoSyncDeclarations}\n" +
                        "speedMs=${Substrate.speedMs}\n" +
                        "historyLimit=${activeAgent?.interpreter?.historyLimit ?: 100}\n" +
                        "strictMode=${Substrate.strictMode}\n" +
                        "valuesCollapsed=${Substrate.valuesCollapsed}\n" +
                        "actionsCollapsed=${Substrate.actionsCollapsed}\n" +
                        "valuesWidth=${Substrate.valuesWidthOverride?.value ?: ""}\n" +
                        "actionsWidth=${Substrate.actionsWidthOverride?.value ?: ""}\n" +
                        "logHeight=${Substrate.logHeightOverride?.value ?: ""}\n" +
                        "fileOrder=${Substrate.fileOrder.joinToString(",")}\n" +
                        "recentProjects=${Substrate.recentProjects.joinToString(",")}\n" +
                        "pinned=${Substrate.pinnedList.joinToString(",")}\n" +
                        "lang=${Strings.lang}\n"
            )
        } catch (_: Exception) {}
    }

    fun loadProgress() {
        try {
            val f = File(baseDir, "progress.true")
            if (!f.exists()) return
            Substrate.completedSamples = f.readLines().map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        } catch (_: Exception) {}
    }

    fun saveProgress() {
        try {
            File(baseDir, "progress.true").writeText(Substrate.completedSamples.joinToString("\n"))
        } catch (_: Exception) {}
    }

    fun startAutoSave() {
        if (autoSaveJob?.isActive == true) return
        autoSaveJob = scope.launch {
            while (true) {
                delay(5000)
                TabsModel.tabs.filter { it.dirty }.forEach { tab ->
                    val target = projectDir?.let { File(it, tab.fileName) } ?: File(baseDir, tab.fileName)
                    try {
                        target.parentFile?.mkdirs()
                        target.writeText(tab.code)
                        tab.dirty = false
                    } catch (_: Exception) {}
                }
                saveProgress()
            }
        }
    }

    fun stopAutoSave() { autoSaveJob?.cancel(); autoSaveJob = null }

    fun launchAsync(block: suspend () -> Unit) {
        scope.launch { block() }
    }

    fun newFile() {
        var n = TabsModel.tabs.size + 1
        while (TabsModel.byFileName("tab$n.cycle") != null) n++
        val interp = app.newInterpreter()
        TabsModel.create("tab$n.cycle", Substrate.startCode, interpreter = interp)
        Substrate.addFileToOrder("tab$n.cycle")
        app.publishActive()
    }

    fun newAgent() {
        var n = TabsModel.tabs.size + 1
        while (TabsModel.byFileName("agent-$n.agent") != null) n++
        val name = "agent-$n.agent"
        val interp = app.newInterpreter()
        TabsModel.create(name, Substrate.DEFAULT_CODE, interpreter = interp)
        app.ensureAgentFor(name)
        Substrate.addFileToOrder(name)
        app.publishActive()
    }

    fun openFileDialog(): File? {
        val dialog = FileDialog(null as Frame?, "Open file", FileDialog.LOAD).apply {
            directory = (projectDir ?: baseDir).absolutePath
            isVisible = true
        }
        val dir = dialog.directory ?: return null
        val file = dialog.file ?: return null
        return File(dir, file)
    }

    fun openFolderDialog(): File? {
        val chooser = JFileChooser(projectDir ?: baseDir).apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            dialogTitle = "Open Folder as Project"
        }
        val result = chooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) return chooser.selectedFile
        return null
    }

    fun openFolder(dir: File) {
        projectDir = dir
        Substrate.addRecentProject(dir.absolutePath)
        saveSettings()
        val files = dir.listFiles { f -> f.isFile }?.sortedBy { it.name } ?: emptyList()
        if (files.isEmpty()) { Substrate.log("No files in ${dir.name}"); return }
        TabsModel.tabs.clear()
        AgentsModel.agents.clear()
        VisualModel.clearAll()
        Substrate.viewingAgentId = null
        Substrate.fileOrder = emptyList()
        val main = files.firstOrNull { it.name == "main.cycle" } ?: files.first()
        val interp0 = app.newInterpreter()
        TabsModel.create(main.name, main.readText(), interpreter = interp0)
        if (main.name == "main.cycle") app.ensureAgentFor(main.name)
        Substrate.addFileToOrder(main.name)
        files.filter { it != main }.forEach {
            val i = app.newInterpreter()
            TabsModel.create(it.name, it.readText(), makeActive = false, interpreter = i)
            Substrate.addFileToOrder(it.name)
        }
        app.publishActive()
        Substrate.log("Opened folder ${dir.name} (${files.size} files)")
    }

    fun saveDialog(): File? {
        val dialog = FileDialog(null as Frame?, "Save", FileDialog.SAVE).apply {
            directory = (projectDir ?: baseDir).absolutePath
            file = app.fileName()
            isVisible = true
        }
        val dir = dialog.directory ?: return null
        val name = dialog.file ?: return null
        return File(dir, name)
    }

    fun openFile(file: File) {
        val content = try { file.readText() } catch (e: Exception) { null }
        if (content.isNullOrBlank()) { Substrate.log("Open failed: ${file.name}"); return }
        val interp = app.newInterpreter()
        TabsModel.open(file.name, content, interp)
        if (file.name == "main.cycle") app.ensureAgentFor(file.name)
        Substrate.addFileToOrder(file.name)
        pushRecent(file.name)
        app.publishActive()
        Substrate.log("Opened ${file.name}")
    }

    fun saveCurrent() {
        val tab = TabsModel.active() ?: return
        val target = projectDir?.let { File(it, tab.fileName) } ?: File(baseDir, tab.fileName)
        val ok = try { target.parentFile?.mkdirs(); target.writeText(tab.code); true } catch (e: Exception) { false }
        if (ok) {
            tab.dirty = false
            pushRecent(tab.fileName)
            Substrate.log("Saved ${tab.fileName}")
            app.publishActive()
        } else Substrate.log("Save failed: ${tab.fileName}")
    }

    fun saveAs(file: File) {
        val tab = TabsModel.active() ?: return
        try {
            file.writeText(tab.code)
            val oldName = tab.fileName
            val agent = AgentsModel.agentFor(oldName)
            tab.fileName = file.name
            tab.kind = fileKindOf(file.name)
            tab.dirty = false
            if (agent != null) {
                agent.fileName = file.name
            }
            Substrate.fileOrder = Substrate.fileOrder.map { if (it == oldName) file.name else it }
            pushRecent(file.name)
            app.publishActive()
            Substrate.log("Saved as ${file.name}")
        } catch (e: Exception) {
            Substrate.log("Save as failed: ${e.message}")
        }
    }

    fun saveAsStart() {
        app.saveCurrentAsStart()
        val ok = save("start.cycle", Substrate.startCode)
        if (ok) Substrate.log("Start state saved (start.cycle)") else Substrate.log("Start state save failed")
    }

    fun renameFile(oldName: String, newName: String) {
        if (newName.isBlank()) return
        if (oldName == newName) return
        if (TabsModel.tabs.any { it.fileName == newName }) {
            Substrate.log("Rename failed: '$newName' already exists")
            return
        }
        val tab = TabsModel.tabs.firstOrNull { it.fileName == oldName } ?: return
        AgentsModel.agentFor(oldName)?.fileName = newName
        tab.fileName = newName
        tab.kind = fileKindOf(newName)
        Substrate.recentFiles = Substrate.recentFiles.map { if (it == oldName) newName else it }
        Substrate.fileOrder = Substrate.fileOrder.map { if (it == oldName) newName else it }
        if (Substrate.activeFileName == oldName) Substrate.activeFileName = newName
        Substrate.log("Renamed $oldName → $newName")
        app.publishActive()
    }

    private fun pushRecent(name: String) {
        Substrate.recentFiles = (listOf(name) + Substrate.recentFiles.filter { it != name }).take(10)
        Substrate.history = (Substrate.history + name).distinct().takeLast(5)
    }

    private fun refreshRecentFromDisk() {
        val files = baseDir.listFiles { f -> f.isFile }?.map { it.name }?.sorted().orEmpty()
        if (Substrate.recentFiles.isEmpty() && files.isNotEmpty()) Substrate.recentFiles = files.take(10)
    }

    fun loadTemplate(templateId: String) {
        val template = Substrate.templates.firstOrNull { it.id == templateId } ?: return
        val tab = TabsModel.tabs.firstOrNull { it.fileName == template.fileName }
        if (tab != null) {
            app.setActiveFile(template.fileName)
            return
        }
        val interp = app.newInterpreter()
        TabsModel.create(template.fileName, template.code, interpreter = interp)
        if (template.fileName == "main.cycle") app.ensureAgentFor(template.fileName)
        Substrate.addFileToOrder(template.fileName)
        if (template.id == "cave") {
            val ending = TabsModel.tabs.firstOrNull { it.fileName == "ending.cycle" }
            if (ending == null) {
                val endingInterp = app.newInterpreter()
                TabsModel.create("ending.cycle", Substrate.TEMPLATE_ENDING, makeActive = false, interpreter = endingInterp)
                Substrate.addFileToOrder("ending.cycle")
            }
        }
        Substrate.showTemplatePicker = false
        saveSettings()
        app.publishActive()
        Substrate.log("Template '${template.title}' opened")
    }

    // ---------- agents ----------

    fun startAgent(agentId: Int) {
        val agent = AgentsModel.byId(agentId) ?: return
        val fileName = agent.fileName
        columnJobs[fileName]?.cancel()
        agent.status = Status.RUNNING
        columnJobs[fileName] = scope.launch {
            while (isActive) {
                agent.interpreter.variables["speed"]?.toLongOrNull()?.let {
                    agent.speedMs = it.coerceIn(0L, 5000L)
                }
                val w = agent.interpreter.waitUntilMs
                if (w != null) {
                    val waitMs = w - System.currentTimeMillis()
                    if (waitMs > 0) {
                        withContext(Dispatchers.Main) { agent.status = Status.PAUSED }
                        delay(waitMs)
                        continue
                    }
                    agent.interpreter.waitUntilMs = null
                    agent.interpreter.timeoutMs = null
                    agent.interpreter.setStatusPublic(Status.RUNNING)
                    withContext(Dispatchers.Main) { agent.status = Status.RUNNING }
                }
                if (agent.status != Status.RUNNING) break
                val r = app.stepAgentInternal(agent.id) ?: break
                withContext(Dispatchers.Main) { app.publishActive() }
                if (r.status == Status.PAUSED || r.status == Status.FINISHED) break
                if (r.errorLine != null) break
                if (agent.speedMs > 0) delay(agent.speedMs) else yield()
            }
            withContext(Dispatchers.Main) {
                if (agent.status == Status.RUNNING) agent.status = Status.PAUSED
            }
            agent.interpreter.waitUntilMs = null
            agent.interpreter.timeoutMs = null
            if (agent.waiter) {
                withContext(Dispatchers.Main) { agent.status = Status.PAUSED }
            }
        }
    }

    fun pauseAgent(agentId: Int) {
        val agent = AgentsModel.byId(agentId) ?: return
        columnJobs[agent.fileName]?.cancel()
        columnJobs.remove(agent.fileName)
        agent.status = Status.PAUSED
        agent.lastPressed = "pause"
    }

    fun stopAgent(agentId: Int) {
        val agent = AgentsModel.byId(agentId) ?: return
        columnJobs[agent.fileName]?.cancel()
        columnJobs.remove(agent.fileName)
        agent.interpreter.waitUntilMs = null
        agent.interpreter.timeoutMs = null
        agent.interpreter.setStatusPublic(Status.STOPPED)
        agent.status = Status.STOPPED
        agent.lastPressed = "stop"
        ScheduleModel.stop(agent)
    }

    fun startAllAgents() { AgentsModel.agents.forEach { startAgent(it.id) } }
    fun pauseAllAgents() { AgentsModel.agents.forEach { pauseAgent(it.id) } }
    fun stopAllAgents() { AgentsModel.agents.forEach { stopAgent(it.id) } }

    fun stepAllAgents() {
        AgentsModel.agents.forEach { agent ->
            if (agent.status != Status.RUNNING) stepAgent(agent.id)
        }
    }

    fun stepBackAllAgents() {
        AgentsModel.agents.forEach { agent ->
            if (agent.status != Status.RUNNING) stepBackAgent(agent.id)
        }
    }

    fun stepAgent(agentId: Int): StepResult? {
        val agent = AgentsModel.byId(agentId) ?: return null
        if (agent.status == Status.RUNNING) return null
        val r = app.stepAgentInternal(agentId)
        scope.launch(Dispatchers.Main) { app.publishActive() }
        return r
    }

    fun stepBackAgent(agentId: Int): StepResult? {
        val agent = AgentsModel.byId(agentId) ?: return null
        if (agent.status == Status.RUNNING) return null
        val r = agent.interpreter.executeStepBack() ?: return null
        app.applyResult(agent, r)
        scope.launch(Dispatchers.Main) { app.publishActive() }
        return r
    }

    fun stopAgentForFile(fileName: String) {
        AgentsModel.agentFor(fileName)?.let { stopAgent(it.id) }
    }

    fun controlAgent(callerId: Int, name: String, action: AgentAction, arg: String?) {
        when (action) {
            AgentAction.STATUS -> {
                if (name == "*") {
                    val dst = arg ?: return
                    val caller = AgentsModel.byId(callerId) ?: return
                    val s = AgentsModel.agents.joinToString(",") { "${it.name}:${it.status}" }
                    caller.interpreter.results[dst] = s
                } else {
                    val a = AgentsModel.byName(name) ?: return
                    val dst = arg ?: return
                    a.interpreter.results[dst] = a.status.name
                }
            }
            AgentAction.WAITER -> {
                val a = AgentsModel.byName(name) ?: return
                val dst = arg ?: return
                a.interpreter.results[dst] = if (a.waiter) "1" else "0"
            }
            AgentAction.WAIT -> {
                val a = if (name == "*wait") AgentsModel.byId(callerId) else AgentsModel.byName(name)
                if (a != null && a.status == Status.PAUSED && a.interpreter.waitUntilMs != null) {
                    startAgent(a.id)
                }
            }
            AgentAction.BROADCAST -> {
                val msg = arg ?: return
                val caller = AgentsModel.byId(callerId) ?: return
                AgentsModel.agents.forEach { agent ->
                    if (agent.id != callerId) appendToInbox(agent, msg, caller.name)
                }
            }
            AgentAction.SEND -> {
                val msg = arg ?: return
                val target = AgentsModel.byName(name) ?: run {
                    Substrate.log("Send failed: agent '$name' not found"); return
                }
                val caller = AgentsModel.byId(callerId) ?: return
                appendToInbox(target, msg, caller.name)
            }
            AgentAction.INBOX -> {
                val caller = AgentsModel.byId(callerId) ?: return
                val file = projectDir?.let { File(it, caller.inboxFileName) }
                    ?: File(baseDir, caller.inboxFileName)
                if (file.exists()) file.writeText("")
                caller.unreadCount = 0
                app.publishActive()
            }
        }
    }

    private fun appendToInbox(agent: AgentState, msg: String, from: String) {
        val file = projectDir?.let { File(it, agent.inboxFileName) }
            ?: File(baseDir, agent.inboxFileName)
        val line = "[${java.time.Instant.now()}] [$from] $msg\n"
        try {
            file.appendText(line)
        } catch (e: Exception) {
            Substrate.log("Inbox write failed: ${e.message}")
            return
        }
        agent.unreadCount++
        Substrate.log("Inbox ${agent.name} ← $from: ${msg.take(40)}")
    }

    fun readInbox(agentId: Int): String {
        val agent = AgentsModel.byId(agentId) ?: return ""
        val file = projectDir?.let { File(it, agent.inboxFileName) }
            ?: File(baseDir, agent.inboxFileName)
        return if (file.exists()) try { file.readText() } catch (_: Exception) { "" } else ""
    }

    fun switchAgentFile(agent: AgentState, target: String): Boolean {
        val occupying = AgentsModel.agentFor(target)
        if (occupying != null && occupying.id != agent.id) {
            agent.interpreter.appendLog(
                "occupied, 1, target=$target, agent=${occupying.name}", "error", agent.currentStep
            )
            agent.interpreter.results["error"] = "occupied"
            agent.interpreter.results["error:message"] = "occupied, 1, target=$target, agent=${occupying.name}"
            return false
        }
        val tab = TabsModel.byFileName(target) ?: run {
            val file = projectDir?.let { File(it, target) } ?: File(baseDir, target)
            if (!file.exists()) { Substrate.log("File not found: $target"); return false }
            val interp = app.newInterpreter()
            TabsModel.create(target, file.readText(), interpreter = interp)
            Substrate.addFileToOrder(target)
            TabsModel.byFileName(target)
        } ?: return false

        columnJobs[agent.fileName]?.cancel()
        columnJobs.remove(agent.fileName)
        agent.fileName = target
        agent.currentStep = 1
        Substrate.log("Agent ${agent.name} → $target")
        app.publishActive()
        return true
    }

    fun deleteFileCascade(fileName: String) {
        AgentsModel.agentFor(fileName)?.let { agent ->
            columnJobs[fileName]?.cancel()
            columnJobs.remove(fileName)
            agentDeferred.remove(agent.id)
            AgentsModel.remove(agent.id)
        }
        VisualModel.clear(fileName)
        TabsModel.closeByName(fileName)
        Substrate.removeFileFromOrder(fileName)
        if (Substrate.expandedFileName == fileName) Substrate.expandedFileName = null
        if (Substrate.activeFileName == fileName) {
            val next = TabsModel.active()
            Substrate.activeFileName = next?.fileName ?: ""
            Substrate.viewingAgentId = next?.let { AgentsModel.agentFor(it.fileName)?.id }
        }
        Substrate.log("Deleted file $fileName")
        app.publishActive()
    }

    // ---------- file-functions ----------

    fun callFile(name: String, parameters: String): String {
        val tab = TabsModel.tabs.firstOrNull { it.fileName.removeSuffix(".cycle") == name }
            ?: TabsModel.tabs.firstOrNull { it.fileName.removeSuffix(".agent") == name }
            ?: return ""
        val existing = AgentsModel.agentFor(tab.fileName)
        if (existing != null && existing.waiter) {
            val deferred = CompletableDeferred<String>()
            agentDeferred[existing.id] = deferred
            existing.interpreter.variables["parameters"] = parameters
            startAgent(existing.id)
            return runBlockingAwait(deferred)
        }
        return callFileLocal(tab, parameters)
    }

    private fun callFileLocal(tab: TabState, parameters: String): String {
        val local = app.newInterpreter()
        local.variables["parameters"] = parameters
        var guard = 0
        while (guard < 10000) {
            guard++
            val res = local.executeOneStep(tab.code)
            if (res.status == Status.FINISHED) break
            if (res.errorLine != null) break
        }
        return local.results["result"] ?: ""
    }

    private fun runBlockingAwait(deferred: CompletableDeferred<String>): String {
        var result = ""
        var waited = 0L
        while (!deferred.isCompleted && waited < 30_000L) {
            Thread.sleep(10)
            waited += 10
        }
        if (deferred.isCompleted) result = deferred.getCompleted()
        return result
    }

    fun onAgentFinished(agent: AgentState) {
        if (!agent.waiter) return
        val deferred = agentDeferred.remove(agent.id) ?: return
        val value = agent.interpreter.results["result"] ?: ""
        deferred.complete(value)
    }

    fun moveFileUp(fileName: String) {
        Substrate.moveFileUp(fileName)
        saveSettings()
        app.publishActive()
    }

    fun moveFileDown(fileName: String) {
        Substrate.moveFileDown(fileName)
        saveSettings()
        app.publishActive()
    }

    // ---------- stream ----------

    fun streamAction(action: String, name: String?, arg: String?): String {
        val agent = app.viewingAgent() ?: return "-1"
        return when (action) {
            "next" -> {
                val key = name ?: agent.interpreter.results["stream:current"] ?: return "-1"
                val state = agent.interpreter.streams[key] ?: return "-1"
                if (state.status != "1") return "0"
                val out = mutableListOf<String>()
                try {
                    while (out.size < state.limit) {
                        val line = state.reader.readLine() ?: break
                        out += line
                    }
                    if (out.size < state.limit) state.status = "0"
                } catch (_: Exception) { state.status = "0" }
                agent.interpreter.results["stream"] = out.joinToString("\n")
                agent.interpreter.results["stream:status"] = state.status
                return state.status
            }
            "close" -> {
                if (name != null) {
                    val st = agent.interpreter.streams.remove(name)
                    try { st?.reader?.close() } catch (_: Exception) {}
                } else {
                    agent.interpreter.streams.values.forEach { try { it.reader.close() } catch (_: Exception) {} }
                    agent.interpreter.streams.clear()
                }
                "1"
            }
            "list" -> {
                agent.interpreter.streams.entries.joinToString(",") { "${it.key}:${it.value.status}" }
            }
            else -> "-1"
        }
    }

    fun secretAction(action: String, key: String?, value: String?): String {
        return when (action) {
            "get" -> if (key != null) (SecretModel.get(key) ?: "") else ""
            "set" -> if (key != null && value != null) { SecretModel.set(key, value); "1" } else "-1"
            "remove" -> if (key != null) { SecretModel.remove(key); "1" } else "-1"
            "list" -> SecretModel.list().joinToString(",")
            else -> "-1"
        }
    }

    fun sendAction(type: String, args: List<String>): String {
        return try {
            when (type) {
                "telegram" -> {
                    val token = SecretModel.get("telegram_token") ?: return "-1"
                    val chatId = args.getOrNull(0) ?: return "-1"
                    val message = args.getOrNull(1) ?: return "-1"
                    val url = "https://api.telegram.org/bot$token/sendMessage"
                    val body = "chat_id=$chatId&text=${java.net.URLEncoder.encode(message, "UTF-8")}"
                    val (code, _) = ServerApi.request("POST", url, body, mapOf(
                        "Content-Type" to "application/x-www-form-urlencoded"))
                    if (code in 200..299) "1" else "-1"
                }
                "max" -> {
                    val token = SecretModel.get("max_token") ?: return "-1"
                    val chatId = args.getOrNull(0) ?: return "-1"
                    val message = args.getOrNull(1) ?: return "-1"
                    val url = "https://platform-api2.max.ru/messages"
                    val body = """{"chat_id":"$chatId","text":"${message.replace("\"", "\\\"")}"}"""
                    val (code, _) = ServerApi.request("POST", url, body, mapOf(
                        "Authorization" to token,
                        "Content-Type" to "application/json"))
                    if (code in 200..299) "1" else "-1"
                }
                "email" -> {
                    val to = args.getOrNull(0) ?: return "-1"
                    val subject = args.getOrNull(1) ?: return "-1"
                    val body = args.getOrNull(2) ?: return "-1"
                    val host = SecretModel.get("smtp_host") ?: return "-1"
                    val port = SecretModel.get("smtp_port")?.toIntOrNull() ?: 587
                    val user = SecretModel.get("smtp_user") ?: return "-1"
                    val pass = SecretModel.get("smtp_pass") ?: return "-1"
                    val props = Properties().apply {
                        put("mail.smtp.host", host)
                        put("mail.smtp.port", port.toString())
                        put("mail.smtp.auth", "true")
                        put("mail.smtp.starttls.enable", "true")
                    }
                    val session = Session.getInstance(props, object : javax.mail.Authenticator() {
                        override fun getPasswordAuthentication() =
                            javax.mail.PasswordAuthentication(user, pass)
                    })
                    val msg = MimeMessage(session).apply {
                        setFrom(InternetAddress(user))
                        setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                        this.subject = subject
                        setText(body)
                    }
                    Transport.send(msg)
                    "1"
                }
                else -> "-1"
            }
        } catch (e: Exception) {
            Substrate.log("Send $type failed: ${e.message}")
            "-1"
        }
    }

    fun scheduleAction(action: String, a1: String?, a2: String?, a3: String?): String {
        val agent = app.viewingAgent() ?: return "-1"
        return when (action) {
            "schedule" -> {
                if (a1 == null || a2 == null) return "-1"
                if (ScheduleModel.schedule(agent, a1, a2, scope, a3)) "1" else "-1"
            }
            "list" -> ScheduleModel.list(agent).joinToString(",") { "${it.id}:${it.cronExpr}:${it.actionName}" }
            "stop" -> {
                val id = a1?.toIntOrNull()
                ScheduleModel.stop(agent, id)
                "1"
            }
            "limit" -> {
                if (a1 == null) agent.scheduleLimit.toString()
                else { agent.scheduleLimit = a1.toIntOrNull()?.coerceIn(1, 100) ?: 10; "1" }
            }
            else -> "-1"
        }
    }

    fun soundAction(action: String, path: String?): String {
        return when (action) {
            "play" -> if (path != null) (if (SoundModel.play(path)) "1" else "-1") else "-1"
            "stop" -> if (SoundModel.stop(path)) "1" else "-1"
            else -> "-1"
        }
    }

    fun websocketAction(action: String, arg: String?): String {
        return when (action) {
            "listen" -> {
                val port = arg?.toIntOrNull() ?: return "-1"
                if (WebSocketModel.listen(port)) "1" else "-1"
            }
            "send" -> if (arg != null) (if (WebSocketModel.send(arg)) "1" else "-1") else "-1"
            "message" -> WebSocketModel.lastMessage
            "clients" -> WebSocketModel.clients.joinToString(",")
            "close" -> { WebSocketModel.close(); "1" }
            else -> "-1"
        }
    }

    fun retryAction(url: String, data: String, count: Int, delayMs: Int, idempotent: Int, op: String): String {
        val method = when (op) {
            "get" -> "GET"; "post" -> "POST"; "put" -> "PUT"
            "delete" -> "DELETE"; "patch" -> "PATCH"
            else -> return "-1"
        }
        val isIdempotent = idempotent == 1 || method == "GET" || method == "DELETE"
        if (method == "POST" && idempotent == 0) {
            Substrate.log("retry on non-idempotent POST — may duplicate")
        }
        val mock = MockModel.get(url)
        if (mock != null) {
            return if (mock.code in 200..299) "1" else "-1"
        }
        var attempts = 0        val maxCount = count.coerceIn(1, 10)
        val waitMs = delayMs.coerceIn(0, 30000)
        while (attempts < maxCount) {
            attempts++
            try {
                val (code, _) = ServerApi.request(method, url, if (method == "GET" || method == "DELETE") null else data, emptyMap())
                if (code in 200..299) return "1"
                if (code in 400..499) return "-1"
                if (code in 500..599 && isIdempotent && attempts < maxCount) {
                    Thread.sleep(waitMs.toLong())
                    continue
                }
                return "-1"
            } catch (e: java.io.IOException) {
                if (isIdempotent && attempts < maxCount) {
                    Thread.sleep(waitMs.toLong())
                    continue
                }
                return "-1"
            } catch (e: Exception) {
                return "-1"
            }
        }
        return "-1"
    }

    fun terminalAction(action: String, a1: String?, a2: String?): String {
        return when (action) {
            "open" -> {
                val command = a1 ?: return "-1"
                val title = a2 ?: "Console"
                val session = TerminalModel.open(title, scope) ?: return "-1"
                if (command.isNotBlank()) {
                    TerminalModel.write(title, command + "\n")
                }
                "1"
            }
            "write" -> {
                val text = a1 ?: return "-1"
                val title = a2 ?: "Console"
                if (TerminalModel.write(title, text)) "1" else "-1"
            }
            "read" -> {
                val title = a1 ?: "Console"
                TerminalModel.read(title)
            }
            "close" -> {
                val title = a1 ?: return "-1"
                TerminalModel.closeByTitle(title)
                "1"
            }
            "list" -> TerminalModel.list().joinToString(",")
            else -> "-1"
        }
    }

    fun openAction(kind: String, path: String): String {
        return try {
            if (!Desktop.isDesktopSupported()) return "-1"
            val desktop = Desktop.getDesktop()
            when (kind) {
                "url" -> { desktop.browse(java.net.URI(path)); "1" }
                "file" -> { desktop.open(File(path)); "1" }
                "folder" -> { desktop.open(File(path)); "1" }
                else -> "-1"
            }
        } catch (e: Exception) {
            Substrate.log("Open $kind failed: ${e.message}")
            "-1"
        }
    }

    fun visualAction(action: String, a1: String?, a2: String?, a3: String?): String {
        val tab = TabsModel.active() ?: return "-1"
        val fileName = tab.fileName
        return when (action) {
            "background" -> {
                val path = a1 ?: return "-1"
                val mode = a2 ?: "fit"
                val file = File(path)
                if (!file.exists()) {
                    Substrate.log("Background not found: $path")
                    return "-1"
                }
                VisualModel.setBackground(fileName, path, mode)
                "1"
            }
            "background:reset" -> {
                VisualModel.resetBackground(fileName)
                "1"
            }
            else -> "-1"
        }
    }

    fun askAction(question: String, default: String, type: String): String {
        val id = AskModel.ask(question, default, type) { _ -> }
        return id.toString()
    }

    fun extAction(name: String, args: List<String>): String? {
        val agent = app.viewingAgent() ?: return null
        val ctx = Ctx(agent.interpreter, agent.id, projectDir)
        return Extensions.invoke(name, args, ctx)
    }

    fun simpleAction(action: String, arg: String?) {
        scope.launch(Dispatchers.Main) {
            when (action) {
                "open" -> {
                    if (arg != null) {
                        val f = File(arg)
                        val target = if (f.isAbsolute) f else File(projectDir ?: baseDir, arg)
                        if (target.exists()) openFile(target)
                        else Substrate.log("File not found: ${target.path}")
                    }
                }
                "save" -> saveCurrent()
                "saveAs" -> { if (arg != null) saveAs(File(arg)) }
                "newCycle" -> newFile()
                "newAgent" -> newAgent()
                "openFolder" -> { if (arg != null) openFolder(File(arg)) }
                "rename" -> {
                    val t = TabsModel.active()
                    if (t != null && arg != null) renameFile(t.fileName, arg)
                }
                "delete" -> {
                    val t = TabsModel.active()
                    if (t != null) { Substrate.log("Simple delete: ${t.fileName}"); deleteFileCascade(t.fileName) }
                }
                "reset" -> {
                    val t = TabsModel.active()
                    if (t != null) app.resetFile(t.fileName)
                }
                "close" -> {
                    val t = TabsModel.active()
                    if (t != null) { Substrate.log("Simple close: ${t.fileName}"); app.closeTab(TabsModel.activeIndex) }
                }
                "moveUp" -> { val t = TabsModel.active(); if (t != null) moveFileUp(t.fileName) }
                "moveDown" -> { val t = TabsModel.active(); if (t != null) moveFileDown(t.fileName) }
                "startAll" -> startAllAgents()
                "pauseAll" -> pauseAllAgents()
                "stopAll" -> { Substrate.log("Simple stopAll"); stopAllAgents() }
                "stepAll" -> stepAllAgents()
                "stepBackAll" -> stepBackAllAgents()
                "settings" -> { Substrate.showSettingsDialog = true }
                "about" -> { Substrate.showAboutDialog = true }
                "samples" -> { Substrate.showSamplesDialog = true }
                "templates" -> { Substrate.showTemplatePicker = true }
                "activeTab" -> {
                    val idx = arg?.toIntOrNull() ?: return@launch
                    app.setActiveTab(idx - 1)
                }
                "click" -> if (arg != null) {
                    val parts = arg.split(":")
                    when (parts[0]) {
                        "button" -> when (parts.getOrNull(1)) {
                            "startAll" -> startAllAgents()
                            "pauseAll" -> pauseAllAgents()
                            "stopAll" -> stopAllAgents()
                            "stepAll" -> stepAllAgents()
                            "stepBackAll" -> stepBackAllAgents()
                            "newCycle" -> newFile()
                            "newAgent" -> newAgent()
                            "save" -> saveCurrent()
                            "open" -> openFileDialog()?.let { openFile(it) }
                            "settings" -> Substrate.showSettingsDialog = true
                            "samples" -> Substrate.showSamplesDialog = true
                            "templates" -> Substrate.showTemplatePicker = true
                            else -> Substrate.log("Unknown button: $arg")
                        }
                        "tab" -> {
                            val name = parts.getOrNull(1) ?: return@launch
                            val idx = TabsModel.tabs.indexOfFirst { it.fileName == name }
                            if (idx >= 0) app.setActiveTab(idx)
                            else Substrate.log("Tab not found: $name")
                        }
                        "agent" -> {
                            val name = parts.getOrNull(1) ?: return@launch
                            val act = parts.getOrNull(2) ?: return@launch
                            val agent = AgentsModel.byName(name) ?: run {
                                Substrate.log("Agent not found: $name"); return@launch
                            }
                            when (act) {
                                "play" -> { startAgent(agent.id); agent.lastPressed = "play" }
                                "pause" -> { pauseAgent(agent.id); agent.lastPressed = "pause" }
                                "stop" -> { stopAgent(agent.id); agent.lastPressed = "stop" }
                                "step" -> stepAgent(agent.id)
                                "stepBack" -> stepBackAgent(agent.id)
                                else -> Substrate.log("Unknown agent action: $act")
                            }
                        }
                        "menu" -> {
                            val menu = parts.getOrNull(1) ?: return@launch
                            val item = parts.getOrNull(2)
                            if (item == null) Substrate.log("Menu opened: $menu")
                            else Substrate.log("Menu click: $menu → $item (simulated)")
                        }
                        "file" -> {
                            val fileName = parts.getOrNull(1) ?: return@launch
                            val act = parts.getOrNull(2) ?: return@launch
                            when (act) {
                                "reset" -> app.resetFile(fileName)
                                "up" -> moveFileUp(fileName)
                                "down" -> moveFileDown(fileName)
                                "rename" -> { Substrate.pendingRenameFile = fileName }
                                "delete" -> { Substrate.pendingDeleteFile = fileName; Substrate.showDeleteFileDialog = true }
                                "expand" -> {
                                    Substrate.expandedFileName = if (Substrate.expandedFileName == fileName) null else fileName
                                    Substrate.activeFileName = fileName
                                }
                                else -> Substrate.log("Unknown file action: $act")
                            }
                        }
                        else -> Substrate.log("Unknown click target: $arg")
                    }
                }
                "clickAt" -> if (arg != null) {
                    val parts = arg.split(",").map { it.trim() }
                    val x = parts.getOrNull(0)?.toIntOrNull() ?: return@launch
                    val y = parts.getOrNull(1)?.toIntOrNull() ?: return@launch
                    try {
                        val robot = Robot()
                        robot.mouseMove(x, y)
                        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
                        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
                    } catch (e: Exception) {
                        Substrate.log("clickAt failed: ${e.message}")
                    }
                }
                "type" -> if (arg != null) {
                    val parts = arg.split(",", limit = 3).map { it.trim() }
                    val text = parts.getOrNull(0) ?: return@launch
                    val target = parts.getOrNull(1) ?: return@launch
                    val delayMs = parts.getOrNull(2)?.toLongOrNull() ?: 30L
                    scope.launch(Dispatchers.Default) {
                        for (ch in text) {
                            withContext(Dispatchers.Main) {
                                when (target) {
                                    "editor:current" -> {
                                        val tab = TabsModel.active() ?: return@withContext
                                        tab.code += ch
                                        tab.dirty = true
                                        app.publishActive()
                                    }
                                    "editor:cursor" -> {
                                        val tab = TabsModel.active() ?: return@withContext
                                        val pos = tab.cursorPosition.coerceIn(0, tab.code.length)
                                        tab.code = tab.code.substring(0, pos) + ch + tab.code.substring(pos)
                                        tab.cursorPosition = pos + 1
                                        tab.dirty = true
                                        app.publishActive()
                                    }
                                    "editor:replace" -> {
                                        val tab = TabsModel.active() ?: return@withContext
                                        if (ch == text.first()) tab.code = ""
                                        tab.code += ch
                                        tab.dirty = true
                                        app.publishActive()
                                    }
                                    "search:query" -> { Substrate.searchQuery += ch }
                                    "search:replace" -> { Substrate.replaceQuery += ch }
                                    "dialog:rename:name" -> {
                                        Substrate.pendingRenameFile = (Substrate.pendingRenameFile ?: "") + ch
                                    }
                                    else -> {}
                                }
                            }
                            if (delayMs > 0) delay(delayMs)
                        }
                    }
                }
                "openTerminal" -> {
                    val title = arg ?: "Console"
                    TerminalModel.open(title, scope)
                }
                "closeTerminal" -> {
                    val title = arg ?: "Console"
                    TerminalModel.byTitle(title)?.let { TerminalModel.close(it.id) }
                }
                "sendTerminal" -> if (arg != null) {
                    val parts = arg.split(",", limit = 2).map { it.trim() }
                    val command = parts.getOrNull(0) ?: return@launch
                    val title = parts.getOrNull(1) ?: "Console"
                    val session = TerminalModel.byTitle(title) ?: run {
                        Substrate.log("sendTerminal: terminal not found: $title"); return@launch
                    }
                    session.running = true
                    scope.launch(Dispatchers.IO) {
                        try {
                            session.stdin.write((command + "\n").toByteArray(StandardCharsets.UTF_8))
                            session.stdin.flush()
                        } catch (e: Exception) {
                            Substrate.log("sendTerminal error: ${e.message}")
                        } finally {
                            session.running = false
                        }
                    }
                }
                "typeTerminal" -> if (arg != null) {
                    val parts = arg.split(",", limit = 3).map { it.trim() }
                    val text = parts.getOrNull(0) ?: return@launch
                    val title = parts.getOrNull(1) ?: run {
                        Substrate.log("typeTerminal: missing title"); return@launch
                    }
                    val delayMs = parts.getOrNull(2)?.toLongOrNull() ?: 30L
                    val session = TerminalModel.byTitle(title) ?: run {
                        Substrate.log("typeTerminal: terminal not found: $title"); return@launch
                    }
                    session.running = true
                    scope.launch(Dispatchers.Default) {
                        for (ch in text) {
                            try {
                                session.stdin.write(ch.toString().toByteArray(StandardCharsets.UTF_8))
                                session.stdin.flush()
                            } catch (_: Exception) { break }
                            session.input += ch
                            withContext(Dispatchers.Main) { app.publishActive() }
                            if (delayMs > 0) delay(delayMs)
                        }
                        try {
                            session.stdin.write("\n".toByteArray(StandardCharsets.UTF_8))
                            session.stdin.flush()
                        } catch (_: Exception) {}
                        session.input += "\n"
                        session.running = false
                        withContext(Dispatchers.Main) { app.publishActive() }
                    }
                }
                "search" -> if (arg != null) {
                    Substrate.searchQuery = arg
                    Substrate.searchMatches = searchInActiveTab(arg)
                    Substrate.searchCurrent = 0
                }
                "search:next" -> {
                    if (Substrate.searchCurrent < Substrate.searchMatches.size - 1) Substrate.searchCurrent++
                }
                "search:prev" -> {
                    if (Substrate.searchCurrent > 0) Substrate.searchCurrent--
                }
                "search:clear" -> {
                    Substrate.searchQuery = ""
                    Substrate.searchMatches = emptyList()
                    Substrate.searchCurrent = 0
                }
                else -> Substrate.log("Unknown :simple:$action")
            }
        }
    }

    private fun searchInActiveTab(query: String): List<Int> {
        val code = TabsModel.active()?.code ?: return emptyList()
        if (query.isEmpty()) return emptyList()
        val lines = mutableListOf<Int>()
        var idx = code.indexOf(query)
        while (idx >= 0) {
            val line = code.substring(0, idx).count { it == '\n' } + 1
            if (line !in lines) lines += line
            idx = code.indexOf(query, idx + 1)
        }
        return lines
    }

    fun save(fileName: String, content: String): Boolean {
        return try {
            val target = projectDir?.let { File(it, fileName) } ?: File(baseDir, fileName)
            target.parentFile?.mkdirs()
            target.writeText(content)
            true
        } catch (_: Exception) { false }
    }

    fun load(fileName: String): String? {
        val target = projectDir?.let { File(it, fileName) } ?: File(baseDir, fileName)
        return if (target.exists()) try { target.readText() } catch (_: Exception) { null } else null
    }
}