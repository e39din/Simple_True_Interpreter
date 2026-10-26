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
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser

class MainScreenSystem(private val app: MainScreenApp) {
    private var autoSaveJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    private val agentJobs = mutableMapOf<Int, Job>()
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
            if (Substrate.fileOrder.isEmpty()) {
                Substrate.fileOrder = TabsModel.tabs.map { it.fileName }
            } else {
                val existing = TabsModel.tabs.map { it.fileName }.toSet()
                Substrate.fileOrder = Substrate.fileOrder.filter { it in existing } +
                        (existing - Substrate.fileOrder.toSet())
            }
        } catch (e: Exception) {
            Substrate.log("Init failed: ${e.message}")
            app.applyDefault()
        }
    }

    fun loadSettings() {
        try {
            val f = File(baseDir, "settings.txt")
            if (!f.exists()) return
            for (l in f.readLines()) {
                val kv = l.split("=", limit = 2)
                if (kv.size != 2) continue
                when (kv[0].trim()) {
                    "autoSyncDeclarations" -> Substrate.autoSyncDeclarations = kv[1].trim() == "true"
                    "speedMs" -> Substrate.speedMs = kv[1].trim().toLongOrNull() ?: 400L
                    "stepBackLimit" -> Substrate.stepBackLimit = kv[1].trim().toIntOrNull() ?: 12
                    "strictMode" -> Substrate.strictMode = kv[1].trim() == "true"
                    "valuesCollapsed" -> Substrate.valuesCollapsed = kv[1].trim() == "true"
                    "actionsCollapsed" -> Substrate.actionsCollapsed = kv[1].trim() == "true"
                    "valuesWidth" -> Substrate.valuesWidthOverride = kv[1].trim().toFloatOrNull()?.dp
                    "actionsWidth" -> Substrate.actionsWidthOverride = kv[1].trim().toFloatOrNull()?.dp
                    "logHeight" -> Substrate.logHeightOverride = kv[1].trim().toFloatOrNull()?.dp
                    "fileOrder" -> Substrate.fileOrder = kv[1].trim().split(",").filter { it.isNotBlank() }
                }
            }
        } catch (_: Exception) {}
    }

    fun saveSettings() {
        try {
            File(baseDir, "settings.txt").writeText(
                "autoSyncDeclarations=${Substrate.autoSyncDeclarations}\n" +
                        "speedMs=${Substrate.speedMs}\n" +
                        "stepBackLimit=${Substrate.stepBackLimit}\n" +
                        "strictMode=${Substrate.strictMode}\n" +
                        "valuesCollapsed=${Substrate.valuesCollapsed}\n" +
                        "actionsCollapsed=${Substrate.actionsCollapsed}\n" +
                        "valuesWidth=${Substrate.valuesWidthOverride?.value ?: ""}\n" +
                        "actionsWidth=${Substrate.actionsWidthOverride?.value ?: ""}\n" +
                        "logHeight=${Substrate.logHeightOverride?.value ?: ""}\n" +
                        "fileOrder=${Substrate.fileOrder.joinToString(",")}\n"
            )
        } catch (_: Exception) {}
    }

    fun loadProgress() {
        try {
            val f = File(baseDir, "progress.txt")
            if (!f.exists()) return
            Substrate.completedSamples = f.readLines().map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        } catch (_: Exception) {}
    }

    fun saveProgress() {
        try {
            File(baseDir, "progress.txt").writeText(Substrate.completedSamples.joinToString("\n"))
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
        app.ensureAgentFor("tab$n.cycle")
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
        val files = dir.listFiles { f -> f.isFile }?.sortedBy { it.name } ?: emptyList()
        if (files.isEmpty()) { Substrate.log("No files in ${dir.name}"); return }
        TabsModel.tabs.clear()
        AgentsModel.agents.clear()
        Substrate.viewingAgentId = null
        Substrate.fileOrder = emptyList()
        val main = files.firstOrNull { it.name == "main.cycle" } ?: files.first()
        val interp0 = app.newInterpreter()
        TabsModel.create(main.name, main.readText(), interpreter = interp0)
        app.ensureAgentFor(main.name)
        Substrate.addFileToOrder(main.name)
        files.filter { it != main }.forEach {
            val i = app.newInterpreter()
            TabsModel.create(it.name, it.readText(), makeActive = false, interpreter = i)
            app.ensureAgentFor(it.name)
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
        app.ensureAgentFor(file.name)
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

    // ---------- agents ----------

    fun startAgent(agentId: Int) {
        val agent = AgentsModel.byId(agentId) ?: return
        val tab = TabsModel.byFileName(agent.fileName) ?: return
        agentJobs[agentId]?.cancel()
        agent.status = Status.RUNNING
        agentJobs[agentId] = scope.launch {
            while (isActive) {
                val w = agent.interpreter.waitUntilMs
                if (w != null) {
                    val waitMs = w - System.currentTimeMillis()
                    if (waitMs > 0) {
                        agent.status = Status.PAUSED
                        delay(waitMs)
                        continue
                    }
                    agent.interpreter.waitUntilMs = null
                    agent.interpreter.setStatusPublic(Status.RUNNING)
                    agent.status = Status.RUNNING
                }
                if (agent.status != Status.RUNNING) break
                val r = app.stepAgentInternal(agent.id) ?: break
                if (r.status == Status.PAUSED || r.status == Status.FINISHED) break
                if (r.errorLine != null) break
                if (agent.speedMs > 0) delay(agent.speedMs) else yield()
            }
            withContext(Dispatchers.Main) {
                if (agent.status == Status.RUNNING) agent.status = Status.PAUSED
            }
            agent.interpreter.waitUntilMs = null
            if (agent.waiter) {
                agent.status = Status.PAUSED
            }
        }
    }

    fun pauseAgent(agentId: Int) {
        val agent = AgentsModel.byId(agentId) ?: return
        agentJobs[agentId]?.cancel()
        agentJobs.remove(agentId)
        agent.status = Status.PAUSED
        agent.lastPressed = "pause"
    }

    fun stopAgent(agentId: Int) {
        val agent = AgentsModel.byId(agentId) ?: return
        agentJobs[agentId]?.cancel()
        agentJobs.remove(agentId)
        agent.interpreter.waitUntilMs = null
        agent.interpreter.setStatusPublic(Status.STOPPED)
        agent.status = Status.STOPPED
        agent.lastPressed = "stop"
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
        return app.stepAgentInternal(agentId)
    }

    fun stepBackAgent(agentId: Int): StepResult? {
        val agent = AgentsModel.byId(agentId) ?: return null
        if (agent.status == Status.RUNNING) return null
        val r = agent.interpreter.executeStepBack() ?: return null
        app.applyResult(agent, r)
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
                val a = AgentsModel.byName(name) ?: AgentsModel.byId(callerId)
                if (a != null && a.status == Status.PAUSED && a.interpreter.waitUntilMs != null) {
                    startAgent(a.id)
                }
            }
        }
    }

    fun switchAgentFile(agent: AgentState, target: String): Boolean {
        val tab = TabsModel.byFileName(target) ?: run {
            val file = projectDir?.let { File(it, target) } ?: File(baseDir, target)
            if (!file.exists()) { Substrate.log("File not found: $target"); return false }
            val interp = app.newInterpreter()
            TabsModel.create(target, file.readText(), interpreter = interp)
            app.ensureAgentFor(target)
            Substrate.addFileToOrder(target)
            TabsModel.byFileName(target)
        } ?: return false

        agent.fileName = target
        agent.currentStep = 1
        agent.interpreter.reset()
        Substrate.log("Agent ${agent.name} → $target")
        app.publishActive()
        return true
    }

    fun deleteFileCascade(fileName: String) {
        AgentsModel.agentFor(fileName)?.let { agent ->
            agentJobs[agent.id]?.cancel()
            agentJobs.remove(agent.id)
            agentDeferred.remove(agent.id)
            AgentsModel.remove(agent.id)
        }
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

    // ---------- file order ----------

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

    // ---------- save/load ----------

    fun save(name: String, content: String): Boolean = try {
        File(baseDir, name).writeText(content); true
    } catch (e: Exception) { false }

    fun load(name: String): String? = try {
        File(baseDir, name).takeIf { it.exists() }?.readText()
    } catch (e: Exception) { null }

    fun readDoc(name: String): String? {
        val candidates = listOf(File(name), File(baseDir, name), projectDir?.let { File(it, name) }).filterNotNull()
        for (c in candidates) if (c.exists()) return try { c.readText() } catch (_: Exception) { null }
        return null
    }
}