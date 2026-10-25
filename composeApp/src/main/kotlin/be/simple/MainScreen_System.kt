package be.simple

import be.`true`.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser

class MainScreenSystem(private val app: MainScreenApp) {
    private var job: Job? = null
    private var autoSaveJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

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
            if (!content.isNullOrBlank()) {
                TabsModel.create(main, content, interpreter = app.newInterpreter())
            } else {
                TabsModel.create(main, Substrate.startCode, interpreter = app.newInterpreter())
                save(main, Substrate.startCode)
            }
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

    fun newFile() {
        val n = TabsModel.tabs.size + 1
        TabsModel.create("tab$n.true", Substrate.startCode, interpreter = app.newInterpreter())
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
        val main = files.firstOrNull { it.name == "main.true" } ?: files.first()
        TabsModel.create(main.name, main.readText(), interpreter = app.newInterpreter())
        files.filter { it != main }.forEach {
            TabsModel.create(it.name, it.readText(), makeActive = false, interpreter = app.newInterpreter())
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
        TabsModel.open(file.name, content, app.newInterpreter())
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
            tab.interpreter.appendLog("Saved ${tab.fileName}", "info")
            app.publishActive()
        } else tab.interpreter.appendLog("Save failed: ${tab.fileName}", "error")
    }

    fun saveAs(file: File) {
        val tab = TabsModel.active() ?: return
        try {
            file.writeText(tab.code)
            tab.fileName = file.name
            tab.dirty = false
            pushRecent(file.name)
            app.publishActive()
            tab.interpreter.appendLog("Saved as ${file.name}", "info")
        } catch (e: Exception) {
            tab.interpreter.appendLog("Save as failed: ${e.message}", "error")
        }
    }

    fun saveAsStart() {
        app.saveCurrentAsStart()
        val ok = save("start.true", Substrate.startCode)
        if (ok) Substrate.log("Start state saved (start.true)") else Substrate.log("Start state save failed")
    }

    fun renameFile(oldName: String, newName: String) {
        val fixed = if (newName.endsWith(".true")) newName else "$newName.true"
        val tab = TabsModel.tabs.firstOrNull { it.fileName == oldName } ?: return
        tab.fileName = fixed
        Substrate.recentFiles = Substrate.recentFiles.map { if (it == oldName) fixed else it }
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

    /** Play исполняет конкретную вкладку по id — устойчиво к переключению вкладок. */
    fun startAutoRun(tabId: Int) {
        job?.cancel()
        job = scope.launch {
            while (isActive) {
                val tab = TabsModel.tabs.firstOrNull { it.id == tabId } ?: break
                if (tab.status != Status.RUNNING) break
                val r = app.stepOn(tab) ?: break
                if (r.status == Status.PAUSED || r.status == Status.FINISHED) break
                val pureJump = r.log.startsWith("Loop") ||
                        r.log.contains("empty line") ||
                        r.log.contains("literal string") ||
                        r.log.startsWith("Command activeTab")
                if (!pureJump) {
                    if (Substrate.speedMs > 0) delay(Substrate.speedMs) else yield()
                }
            }
            // Play завершился — снимаем runningTabId, если он всё ещё наш.
            if (Substrate.runningTabId == tabId) {
                Substrate.runningTabId = null
                Substrate.status = Status.PAUSED
            }
        }
    }

    fun stopAutoRun() { job?.cancel(); job = null }

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