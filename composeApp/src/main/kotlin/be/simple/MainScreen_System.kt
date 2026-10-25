package be.simple

import be.`true`.AgentAction
import be.`true`.Status
import be.`true`.StepResult
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
            val startContent = load("start.true")
            if (!startContent.isNullOrBlank()) {
                Substrate.startCode = startContent
                Substrate.startFileName = "start.true"
            } else {
                Substrate.startCode = Substrate.DEFAULT_CODE
                save("start.true", Substrate.startCode)
            }
            refreshRecentFromDisk()
            val main = "main.true"
            val content = load(main)
            val code = content ?: Substrate.startCode
            if (content.isNullOrBlank()) save(main, Substrate.startCode)
            TabsModel.create(main, code, interpreter = app.newInterpreter())
            app.ensureAgentFor(main)
            pushRecent(main)
            app.ensureMainTab()
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
                    "welcomeShown" -> Substrate.welcomeShown = kv[1].trim() == "true"
                    "tutorialCompleted" -> Substrate.tutorialCompleted = kv[1].trim() == "true"
                    "autoSyncDeclarations" -> Substrate.autoSyncDeclarations = kv[1].trim() == "true"
                    "speedMs" -> Substrate.speedMs = kv[1].trim().toLongOrNull() ?: 400L
                    "stepBackLimit" -> Substrate.stepBackLimit = kv[1].trim().toIntOrNull() ?: 12
                    "strictMode" -> Substrate.strictMode = kv[1].trim() == "true"
                }
            }
        } catch (_: Exception) {}
    }

    fun saveSettings() {
        try {
            File(baseDir, "settings.txt").writeText(
                "welcomeShown=${Substrate.welcomeShown}\n" +
                        "tutorialCompleted=${Substrate.tutorialCompleted}\n" +
                        "autoSyncDeclarations=${Substrate.autoSyncDeclarations}\n" +
                        "speedMs=${Substrate.speedMs}\n" +
                        "stepBackLimit=${Substrate.stepBackLimit}\n" +
                        "strictMode=${Substrate.strictMode}\n"
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
        val n = TabsModel.tabs.size + 1
        val interp = app.newInterpreter()
        TabsModel.create("tab$n.true", Substrate.startCode, interpreter = interp)
        app.ensureAgentFor("tab$n.true")
        app.publishActive()
    }

    fun openFileDialog(): File? {
        val dialog = FileDialog(null as Frame?, "Open .true", FileDialog.LOAD).apply {
            setFilenameFilter { _, name -> name.endsWith(".true") }
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
        val files = dir.listFiles { f -> f.isFile && f.name.endsWith(".true") }?.sortedBy { it.name } ?: emptyList()
        if (files.isEmpty()) { Substrate.log("No .true files in ${dir.name}"); return }
        TabsModel.tabs.clear()
        AgentsModel.agents.clear()
        Substrate.viewingAgentId = null
        val main = files.firstOrNull { it.name == "main.true" } ?: files.first()
        val interp0 = app.newInterpreter()
        TabsModel.create(main.name, main.readText(), interpreter = interp0)
        app.ensureAgentFor(main.name)
        files.filter { it != main }.forEach {
            val i = app.newInterpreter()
            TabsModel.create(it.name, it.readText(), makeActive = false, interpreter = i)
            app.ensureAgentFor(it.name)
        }
        app.publishActive()
        Substrate.log("Opened folder ${dir.name} (${files.size} files)")
    }

    fun saveDialog(): File? {
        val dialog = FileDialog(null as Frame?, "Save .true", FileDialog.SAVE).apply {
            directory = (projectDir ?: baseDir).absolutePath
            file = app.fileName()
            isVisible = true
        }
        val dir = dialog.directory ?: return null
        val name = dialog.file ?: return null
        return File(dir, if (name.endsWith(".true")) name else "$name.true")
    }

    fun openFile(file: File) {
        val content = try { file.readText() } catch (e: Exception) { null }
        if (content.isNullOrBlank()) { Substrate.log("Open failed: ${file.name}"); return }
        val interp = app.newInterpreter()
        TabsModel.open(file.name, content, interp)
        app.ensureAgentFor(file.name)
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
            val agent = AgentsModel.agentFor(tab.fileName)
            tab.fileName = file.name
            tab.dirty = false
            if (agent != null) {
                agent.fileName = file.name
            }
            pushRecent(file.name)
            app.publishActive()
            Substrate.log("Saved as ${file.name}")
        } catch (e: Exception) {
            Substrate.log("Save as failed: ${e.message}")
        }
    }

    fun saveAsStart() {
        app.saveCurrentAsStart()
        val ok = save("start.true", Substrate.startCode)
        if (ok) Substrate.log("Start state saved (start.true)") else Substrate.log("Start state save failed")
    }

    fun renameFile(oldName: String, newName: String) {
        val fixed = if (newName.endsWith(".true")) newName else "$newName.true"
        if (fixed == oldName) return
        if (TabsModel.tabs.any { it.fileName == fixed }) {
            Substrate.log("Rename failed: '$fixed' already exists")
            return
        }
        val tab = TabsModel.tabs.firstOrNull { it.fileName == oldName } ?: return
        AgentsModel.agentFor(oldName)?.fileName = fixed
        tab.fileName = fixed
        Substrate.recentFiles = Substrate.recentFiles.map { if (it == oldName) fixed else it }
        if (Substrate.activeFileName == oldName) Substrate.activeFileName = fixed
        Substrate.log("Renamed $oldName → $fixed")
        app.publishActive()
    }

    private fun pushRecent(name: String) {
        Substrate.recentFiles = (listOf(name) + Substrate.recentFiles.filter { it != name }).take(10)
        Substrate.history = (Substrate.history + name).distinct().takeLast(5)
    }

    private fun refreshRecentFromDisk() {
        val files = baseDir.listFiles { f -> f.isFile && f.name.endsWith(".true") }?.map { it.name }?.sorted().orEmpty()
        if (Substrate.recentFiles.isEmpty() && files.isNotEmpty()) Substrate.recentFiles = files.take(10)
    }

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
        }
    }

    fun pauseAgent(agentId: Int) {
        val agent = AgentsModel.byId(agentId) ?: return
        agentJobs[agentId]?.cancel()
        agentJobs.remove(agentId)
        agent.status = Status.PAUSED
    }

    fun stopAgent(agentId: Int) {
        val agent = AgentsModel.byId(agentId) ?: return
        agentJobs[agentId]?.cancel()
        agentJobs.remove(agentId)
        agent.interpreter.waitUntilMs = null
        agent.interpreter.setStatusPublic(Status.STOPPED)
        agent.status = Status.STOPPED
    }

    fun startAllAgents() { AgentsModel.agents.forEach { startAgent(it.id) } }
    fun stopAllAgents() { AgentsModel.agents.forEach { stopAgent(it.id) } }

    fun stepAgent(agentId: Int): StepResult? {
        val agent = AgentsModel.byId(agentId) ?: return null
        if (agent.status == Status.RUNNING) return null
        return app.stepAgentInternal(agentId)
    }

    fun stepBackAgent(agentId: Int): StepResult? {
        val agent = AgentsModel.byId(agentId) ?: return null
        if (agent.status == Status.RUNNING) return null
        val r = agent.interpreter.executeStepBack() ?: return null
        scope.launch(Dispatchers.Main) { app.applyResult(agent, r) }
        return r
    }

    fun stopAgentForFile(fileName: String) {
        AgentsModel.agentFor(fileName)?.let { stopAgent(it.id) }
    }

    fun controlAgent(callerId: Int, name: String, action: AgentAction, arg: String?) {
        when (action) {
            AgentAction.CURRENT_FILE -> {
                val a = AgentsModel.byName(name) ?: return
                val dst = arg ?: return
                a.interpreter.results[dst] = a.fileName
            }
            AgentAction.CURRENT_STEP -> {
                val a = AgentsModel.byName(name) ?: return
                val dst = arg ?: return
                a.interpreter.results[dst] = a.currentStep.toString()
            }
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
            AgentAction.STOP -> AgentsModel.byName(name)?.let { stopAgent(it.id) }
            AgentAction.PAUSE -> AgentsModel.byName(name)?.let { pauseAgent(it.id) }
            AgentAction.RESUME -> AgentsModel.byName(name)?.let { startAgent(it.id) }
            AgentAction.MOVE_TO_FILE -> moveAgentTo(AgentsModel.byName(name)?.id ?: return, arg ?: return)
            AgentAction.COPY_TO_FILE -> copyAgentTo(AgentsModel.byName(name)?.id ?: return, arg ?: return)
            AgentAction.DELETE -> {
                if (name.startsWith("*file:")) {
                    val fname = name.removePrefix("*file:")
                    deleteFileCascade(fname)
                } else {
                    AgentsModel.byName(name)?.let { deleteAgent(it.id) }
                }
            }
            AgentAction.WAIT -> {
                val a = AgentsModel.byName(name) ?: AgentsModel.byId(callerId)
                if (a != null && a.status == Status.PAUSED && a.interpreter.waitUntilMs != null) {
                    startAgent(a.id)
                }
            }
        }
    }

    fun moveAgentTo(agentId: Int, targetFile: String): Boolean {
        val agent = AgentsModel.byId(agentId) ?: return false
        if (agent.fileName == targetFile) {
            Substrate.log("Warning: ${agent.name} is already on $targetFile")
            return true
        }
        val occupied = AgentsModel.agentFor(targetFile)
        if (occupied != null) {
            Substrate.log("Cannot move ${agent.name} to $targetFile: occupied by ${occupied.name}")
            return false
        }
        val oldFile = agent.fileName
        AgentsModel.remove(agent.id)
        val a = AgentsModel.create(agent.name, targetFile, agent.interpreter)
        if (a != null) {
            a.status = agent.status
            a.currentStep = agent.currentStep
            a.variablesSnapshot = agent.variablesSnapshot
            a.resultsSnapshot = agent.resultsSnapshot
            a.constantsSnapshot = agent.constantsSnapshot
            a.hiddenSnapshot = agent.hiddenSnapshot
            if (Substrate.viewingAgentId == agent.id) Substrate.viewingAgentId = a.id
            Substrate.log("Moved ${agent.name} from $oldFile to $targetFile")
        }
        app.publishActive()
        return true
    }

    fun copyAgentTo(agentId: Int, targetFile: String): Boolean {
        val agent = AgentsModel.byId(agentId) ?: return false
        if (AgentsModel.agentFor(targetFile) != null) {
            Substrate.log("Cannot copy ${agent.name} to $targetFile: occupied")
            return false
        }
        if (AgentsModel.agents.size >= AgentsModel.MAX_AGENTS) {
            Substrate.log("Cannot copy: max agents reached")
            return false
        }
        val n = AgentsModel.agents.size + 1
        val interp = app.newInterpreter()
        agent.interpreter.variables.forEach { (k, v) -> interp.variables[k] = v }
        agent.interpreter.results.forEach { (k, v) -> interp.results[k] = v }
        agent.interpreter.constants.forEach { interp.constants.add(it) }
        agent.interpreter.hiddenVariables.forEach { interp.hiddenVariables.add(it) }
        interp.forceStep(agent.currentStep)
        val a = AgentsModel.create("agent-$n", targetFile, interp)
        if (a != null) {
            a.currentStep = agent.currentStep
            Substrate.log("Copied ${agent.name} to $targetFile as ${a.name}")
        }
        app.publishActive()
        return a != null
    }

    fun deleteAgent(agentId: Int) {
        val agent = AgentsModel.byId(agentId) ?: return
        agentJobs[agentId]?.cancel()
        agentJobs.remove(agentId)
        AgentsModel.remove(agentId)
        if (Substrate.viewingAgentId == agentId) Substrate.viewingAgentId = null
        Substrate.log("Deleted agent ${agent.name}")
        app.publishActive()
    }

    fun deleteFileCascade(fileName: String) {
        AgentsModel.agentFor(fileName)?.let { deleteAgent(it.id) }
        TabsModel.closeByName(fileName)
        if (Substrate.expandedFileName == fileName) Substrate.expandedFileName = null
        if (Substrate.activeFileName == fileName) {
            val next = TabsModel.active()
            Substrate.activeFileName = next?.fileName ?: ""
            Substrate.viewingAgentId = next?.let { AgentsModel.agentFor(it.fileName)?.id }
        }
        Substrate.log("Deleted file $fileName")
        app.publishActive()
    }

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