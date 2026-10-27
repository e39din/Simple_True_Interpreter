package be.simple

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import be.`true`.Error
import be.`true`.Interpreter
import be.`true`.LogEntry
import be.`true`.Status
import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.model.time.ExecutionTime
import com.cronutils.parser.CronParser
import com.pty4j.PtyProcess
import com.pty4j.PtyProcessBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.time.ZonedDateTime
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

data class TemplateGroup(val title: String, val color: Color, val templates: List<String>)

data class Template(
    val id: String,
    val title: String,
    val description: String,
    val fileName: String,
    val code: String
)

enum class FileKind { CYCLE, AGENT, OTHER, INBOX }

fun fileKindOf(fileName: String): FileKind = when {
    fileName.endsWith(".agent", ignoreCase = true) -> FileKind.AGENT
    fileName.endsWith(".cycle", ignoreCase = true) -> FileKind.CYCLE
    fileName.endsWith(".inbox", ignoreCase = true) -> FileKind.INBOX
    else -> FileKind.OTHER
}

data class TabState(
    val id: Int,
    var fileName: String,
    var code: String,
    var kind: FileKind = fileKindOf(fileName),
    var currentStep: Int = 1,
    var status: Status = Status.STOPPED,
    var interpreter: Interpreter = Interpreter(),
    var cursorPosition: Int = 0,
    var dirty: Boolean = false,
    val undoStack: ArrayDeque<String> = ArrayDeque(),
    val redoStack: ArrayDeque<String> = ArrayDeque()
)

class AgentState(
    val id: Int,
    var name: String,
    var color: Color,
    var fileName: String,
    val interpreter: Interpreter
) {
    var status: Status by mutableStateOf(Status.STOPPED)
    var currentStep: Int by mutableStateOf(1)
    var speedMs: Long by mutableStateOf(400L)
    var waiter: Boolean by mutableStateOf(false)
    var lastPressed: String? by mutableStateOf(null)

    var variablesSnapshot: Map<String, String> by mutableStateOf(emptyMap())
    var resultsSnapshot: Map<String, String> by mutableStateOf(emptyMap())
    var constantsSnapshot: Set<String> by mutableStateOf(emptySet())
    var hiddenSnapshot: Set<String> by mutableStateOf(emptySet())

    var inboxFileName: String = ""
    var unreadCount: Int by mutableStateOf(0)

    var scheduleJobs: SnapshotStateList<ScheduleJob> = mutableStateListOf()
    var scheduleLimit: Int = 10
    var columnJob: Job? = null
}

data class ScheduleJob(
    val id: Int,
    val cronExpr: String,
    val actionName: String,
    var job: Job? = null,
    var nextRun: Long = 0L
)

data class StreamState(
    val name: String,
    val reader: BufferedReader,
    val limit: Int,
    var status: String = "1"
)

object TabsModel {
    private var counter = 1
    val tabs = mutableListOf<TabState>()
    var activeIndex: Int by mutableStateOf(-1)

    fun newId(): Int = counter++

    fun create(fileName: String, code: String, makeActive: Boolean = true,
               interpreter: Interpreter = Interpreter()): TabState {
        val tab = TabState(
            id = newId(), fileName = fileName, code = code,
            kind = fileKindOf(fileName), interpreter = interpreter
        )
        tabs += tab
        if (makeActive || tabs.size == 1) activeIndex = tabs.lastIndex
        return tab
    }

    fun open(fileName: String, code: String, interpreter: Interpreter = Interpreter()) {
        val existing = tabs.indexOfFirst { it.fileName == fileName }
        if (existing >= 0) { activeIndex = existing; return }
        create(fileName, code, true, interpreter)
    }

    fun close(index: Int) {
        if (index !in tabs.indices) return
        tabs.removeAt(index)
        if (tabs.isEmpty()) { activeIndex = -1; return }
        activeIndex = activeIndex.coerceIn(0, tabs.lastIndex)
    }

    fun setActive(index: Int) {
        if (index !in tabs.indices) return
        if (activeIndex in tabs.indices && activeIndex != index) tabs[activeIndex].status = Status.PAUSED
        activeIndex = index
    }

    fun active(): TabState? = tabs.getOrNull(activeIndex)
    fun rename(index: Int, newName: String) { tabs.getOrNull(index)?.fileName = newName }
    fun byFileName(name: String): TabState? = tabs.firstOrNull { it.fileName == name }
    fun closeByName(name: String) {
        val idx = tabs.indexOfFirst { it.fileName == name }
        if (idx >= 0) close(idx)
    }
}

object AgentsModel {
    val agents: SnapshotStateList<AgentState> = mutableStateListOf()

    val RAINBOW: List<Color> = listOf(
        Color(0xFFB85C5C), Color(0xFFC49A5C), Color(0xFFB8B85C),
        Color(0xFF5C9E5C), Color(0xFF5B8DB8), Color(0xFF6B6BB8), Color(0xFF9E5CB8)
    )

    fun colorAt(index: Int): Color = RAINBOW[index % RAINBOW.size]
    fun agentFor(fileName: String): AgentState? = agents.firstOrNull { it.fileName == fileName }
    fun byId(id: Int): AgentState? = agents.firstOrNull { it.id == id }
    fun byName(name: String): AgentState? = agents.firstOrNull { it.name == name }

    fun create(name: String, fileName: String, interpreter: Interpreter): AgentState? {
        if (name.isBlank()) return null
        if (agents.any { it.name == name }) return null
        val agent = AgentState(
            id = TabsModel.newId(), name = name, color = colorAt(agents.size),
            fileName = fileName, interpreter = interpreter
        )
        agent.inboxFileName = "$name.inbox"
        val baseDir = File(System.getProperty("user.home"), ".simple").apply { mkdirs() }
        val inboxFile = File(baseDir, agent.inboxFileName)
        if (!inboxFile.exists()) inboxFile.writeText("")
        agents += agent
        return agent
    }

    fun remove(id: Int) {
        val a = byId(id)
        if (a != null) {
            ScheduleModel.stop(a)
            SoundModel.stop(null)
            VisualModel.clear(a.fileName)
            a.columnJob?.cancel()
        }
        agents.removeAll { it.id == id }
    }

    fun rename(id: Int, newName: String): Boolean {
        if (newName.isBlank()) return false
        if (agents.any { it.id != id && it.name == newName }) return false
        val a = byId(id) ?: return false
        a.name = newName
        return true
    }

    fun recalcWaiter(agent: AgentState) {
        val tab = TabsModel.byFileName(agent.fileName) ?: return
        agent.waiter = hasResultMarker(tab.code)
    }

    fun hasResultMarker(code: String): Boolean {
        for (line in code.lines()) {
            if (line.trim() == ":result") return true
        }
        return false
    }
}

object TerminalModel {
    data class TerminalSession(
        val id: Int,
        val title: String,
        val process: Process,
        val stdin: OutputStream,
        val stdout: BufferedReader,
        val stderr: BufferedReader,
        var output: String = "",
        var input: String = "",
        var alive: Boolean = true,
        var running: Boolean = false
    )

    val sessions: SnapshotStateList<TerminalSession> = mutableStateListOf()
    private var counter = 1

    fun open(title: String, scope: CoroutineScope): TerminalSession? {
        return try {
            val isWin = System.getProperty("os.name").lowercase().contains("win")
            val shell = if (isWin) listOf("cmd.exe")
            else listOf("/bin/sh", "-c", "exec bash 2>/dev/null || exec sh")
            val env = System.getenv().toMutableMap()
            env.putIfAbsent("TERM", "xterm-256color")
            val proc: PtyProcess = PtyProcessBuilder(shell.toTypedArray())
                .setEnvironment(env)
                .setConsole(false)
                .setRedirectErrorStream(false)
                .start()
            val stdin = proc.outputStream
            val stdout = BufferedReader(InputStreamReader(proc.inputStream, StandardCharsets.UTF_8))
            val stderr = BufferedReader(InputStreamReader(proc.errorStream, StandardCharsets.UTF_8))
            val session = TerminalSession(counter++, title, proc, stdin, stdout, stderr)
            sessions += session
            scope.launch(Dispatchers.IO) {
                while (session.alive) {
                    val line = try { session.stdout.readLine() } catch (_: Exception) { null } ?: break
                    session.output += line + "\n"
                }
            }
            scope.launch(Dispatchers.IO) {
                while (session.alive) {
                    val line = try { session.stderr.readLine() } catch (_: Exception) { null } ?: break
                    session.output += line + "\n"
                }
            }
            scope.launch(Dispatchers.IO) {
                try { proc.waitFor() } catch (_: Exception) {}
                session.alive = false
            }
            session
        } catch (e: Exception) {
            Substrate.log("Terminal open failed: ${e.message}")
            null
        }
    }

    fun byTitle(title: String): TerminalSession? = sessions.firstOrNull { it.title == title }
    fun byId(id: Int): TerminalSession? = sessions.firstOrNull { it.id == id }

    fun write(title: String, text: String): Boolean {
        val s = byTitle(title) ?: return false
        return try {
            s.stdin.write(text.toByteArray(StandardCharsets.UTF_8))
            s.stdin.flush()
            true
        } catch (_: Exception) { false }
    }

    fun read(title: String): String = byTitle(title)?.output ?: ""

    fun close(id: Int) {
        val s = byId(id) ?: return
        s.alive = false
        try { s.process.destroyForcibly() } catch (_: Exception) {}
        sessions.removeAll { it.id == id }
    }

    fun closeByTitle(title: String) {
        val s = byTitle(title) ?: return
        close(s.id)
    }

    fun closeAll() {
        sessions.toList().forEach { close(it.id) }
    }

    fun list(): List<String> = sessions.map { it.title }
}

data class SnackbarMessage(
    val id: Int,
    val text: String,
    val kind: String = "warning",
    val createdAt: Long = System.currentTimeMillis()
)

object SnackbarModel {
    val messages: SnapshotStateList<SnackbarMessage> = mutableStateListOf()
    private var counter = 1

    fun show(text: String, kind: String = "warning") {
        if (messages.size >= 3) messages.removeAt(0)
        messages += SnackbarMessage(counter++, text, kind)
    }

    fun dismiss(id: Int) { messages.removeAll { it.id == id } }
    fun clear() { messages.clear() }
}

object SecretModel {
    private val lock = Any()

    private val keyFile: File
        get() = File(System.getProperty("user.home"), ".simple/secret.key").apply { parentFile?.mkdirs() }

    private val dataFile: File
        get() = File(System.getProperty("user.home"), ".simple/secrets.enc").apply { parentFile?.mkdirs() }

    private val cache = ConcurrentHashMap<String, String>()

    private fun loadKey(): ByteArray {
        if (!keyFile.exists()) {
            val key = ByteArray(32)
            SecureRandom().nextBytes(key)
            keyFile.writeBytes(Base64.getEncoder().encode(key))
            try {
                keyFile.setReadable(false, false)
                keyFile.setReadable(true, true)
            } catch (_: Exception) {}
        }
        return Base64.getDecoder().decode(keyFile.readText().trim())
    }

    private fun loadAll() {
        if (cache.isNotEmpty()) return
        if (!dataFile.exists()) return
        try {
            val key = loadKey()
            val raw = Base64.getDecoder().decode(dataFile.readText().trim())
            if (raw.size < 12) return
            val iv = raw.copyOfRange(0, 12)
            val enc = raw.copyOfRange(12, raw.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, iv))
            val plain = cipher.doFinal(enc).toString(StandardCharsets.UTF_8)
            for (line in plain.lines()) {
                val idx = line.indexOf('=')
                if (idx > 0) cache[line.substring(0, idx)] = line.substring(idx + 1)
            }
        } catch (_: Exception) {}
    }

    private fun saveAll() {
        try {
            val key = loadKey()
            val plain = cache.entries.joinToString("\n") { "${it.key}=${it.value}" }
            val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, iv))
            val enc = cipher.doFinal(plain.toByteArray(StandardCharsets.UTF_8))
            dataFile.writeBytes(Base64.getEncoder().encode(iv + enc))
        } catch (_: Exception) {}
    }

    fun get(key: String): String? {
        synchronized(lock) { loadAll() }
        return cache[key]
    }

    fun set(key: String, value: String) {
        synchronized(lock) {
            loadAll()
            cache[key] = value
            saveAll()
        }
    }

    fun remove(key: String) {
        synchronized(lock) {
            loadAll()
            cache.remove(key)
            saveAll()
        }
    }

    fun list(): List<String> {
        synchronized(lock) { loadAll() }
        return cache.keys.sorted()
    }

    fun mask(value: String): String =
        if (value.length <= 4) "***" else value.take(2) + "***" + value.takeLast(2)

    fun isSecret(key: String): Boolean = get(key) != null
}

object ScheduleModel {
    private val parser = CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX))
    private var counter = 1

    fun schedule(agent: AgentState, expr: String, actionName: String,
                 scope: CoroutineScope, tz: String? = null): Boolean {
        return try {
            if (agent.scheduleJobs.size >= agent.scheduleLimit) {
                Substrate.log("Schedule limit reached (${agent.scheduleLimit})")
                return false
            }
            val cron = parser.parse(expr).validate()
            val exec = ExecutionTime.forCron(cron)
            val zone = if (tz != null) java.time.ZoneId.of(tz) else java.time.ZoneId.systemDefault()
            val job = scope.launch(Dispatchers.Default) {
                while (isActive) {
                    val next = exec.nextExecution(ZonedDateTime.now(zone)).orElse(null) ?: break
                    val waitMs = next.toInstant().toEpochMilli() - System.currentTimeMillis()
                    if (waitMs > 0) delay(waitMs) else delay(100)
                    val action = agent.interpreter.variables[actionName]
                    if (action == null) {
                        Substrate.log("Schedule: action '$actionName' not found")
                        continue
                    }
                    try {
                        agent.interpreter.executeInstructionOnce(action, action)
                    } catch (e: Exception) {
                        Substrate.log("Schedule run failed: ${e.message}")
                    }
                }
            }
            agent.scheduleJobs += ScheduleJob(counter++, expr, actionName, job, 0L)
            true
        } catch (e: Exception) {
            Substrate.log("Schedule failed: ${e.message}")
            false
        }
    }

    fun list(agent: AgentState): List<ScheduleJob> = agent.scheduleJobs.toList()

    fun stop(agent: AgentState, id: Int? = null) {
        val toStop = if (id == null) agent.scheduleJobs.toList()
        else agent.scheduleJobs.filter { it.id == id }
        toStop.forEach { it.job?.cancel() }
        agent.scheduleJobs.removeAll(toStop)
    }
}

object WebSocketModel {
    @Volatile var server: org.java_websocket.server.WebSocketServer? = null
    @Volatile var lastMessage: String = ""
    val clients: MutableSet<String> = ConcurrentHashMap.newKeySet()

    fun listen(port: Int): Boolean {
        return try {
            server?.stop()
            val s = object : org.java_websocket.server.WebSocketServer(
                java.net.InetSocketAddress(port)
            ) {
                override fun onOpen(conn: org.java_websocket.WebSocket, handshake: org.java_websocket.handshake.ClientHandshake) {
                    clients += conn.remoteSocketAddress.toString()
                }
                override fun onClose(conn: org.java_websocket.WebSocket, code: Int, reason: String, remote: Boolean) {
                    clients -= conn.remoteSocketAddress.toString()
                }
                override fun onMessage(conn: org.java_websocket.WebSocket, message: String) {
                    lastMessage = message
                }
                override fun onError(conn: org.java_websocket.WebSocket?, ex: Exception) {
                    Substrate.log("WebSocket error: ${ex.message}")
                }
                override fun onStart() {}
            }
            s.start()
            server = s
            true
        } catch (e: Exception) {
            Substrate.log("WebSocket listen failed: ${e.message}")
            false
        }
    }

    fun send(message: String): Boolean {
        val s = server ?: return false
        return try {
            s.broadcast(message)
            true
        } catch (_: Exception) { false }
    }

    fun close() {
        try { server?.stop() } catch (_: Exception) {}
        server = null
        clients.clear()
        lastMessage = ""
    }
}

object SoundModel {
    private val players = ConcurrentHashMap<String, Any>()

    fun play(path: String): Boolean {
        return try {
            val file = File(path)
            if (!file.exists()) return false
            val ext = file.extension.lowercase()
            when (ext) {
                "wav" -> {
                    val clip = javax.sound.sampled.AudioSystem.getClip()
                    clip.open(javax.sound.sampled.AudioSystem.getAudioInputStream(file))
                    clip.start()
                    players[path] = clip
                    true
                }
                "mp3" -> {
                    val player = javazoom.jl.player.Player(file.inputStream())
                    players[path] = player
                    Thread { try { player.play() } catch (_: Exception) {} }.start()
                    true
                }
                else -> false
            }
        } catch (e: Exception) {
            Substrate.log("Sound play failed: ${e.message}")
            false
        }
    }

    fun stop(path: String? = null): Boolean {
        return try {
            if (path == null) {
                players.forEach { (_, p) ->
                    when (p) {
                        is javax.sound.sampled.Clip -> p.stop()
                        is javazoom.jl.player.Player -> p.close()
                    }
                }
                players.clear()
            } else {
                val p = players.remove(path) ?: return false
                when (p) {
                    is javax.sound.sampled.Clip -> p.stop()
                    is javazoom.jl.player.Player -> p.close()
                }
            }
            true
        } catch (_: Exception) { false }
    }
}

object MemModel {
    private val cells = ConcurrentHashMap<Int, String>()
    private val blocks = ConcurrentHashMap<Int, Int>()
    private var nextAddr = 1000
    private val lock = Any()

    fun alloc(size: Int): Int = synchronized(lock) {
        val addr = nextAddr
        nextAddr += size.coerceAtLeast(1)
        blocks[addr] = size.coerceAtLeast(1)
        for (i in addr until addr + size) cells[i] = "0"
        addr
    }

    fun free(addr: Int): Boolean = synchronized(lock) {
        val size = blocks.remove(addr) ?: return false
        for (i in addr until addr + size) cells.remove(i)
        true
    }

    fun write(addr: Int, value: String): Boolean {
        if (blocks.keys.none { addr >= it && addr < it + (blocks[it] ?: 0) }) return false
        cells[addr] = value
        return true
    }

    fun read(addr: Int): String = cells[addr] ?: ""

    fun copy(src: Int, dst: Int, len: Int): Boolean {
        val values = (0 until len).map { cells[src + it] ?: "0" }
        for (i in 0 until len) cells[dst + i] = values[i]
        return true
    }

    fun move(src: Int, dst: Int, len: Int): Boolean {
        val values = (0 until len).map { cells[src + it] ?: "0" }
        for (i in 0 until len) cells[dst + i] = values[i]
        for (i in 0 until len) cells.remove(src + i)
        return true
    }

    fun fill(addr: Int, value: String, len: Int): Boolean {
        for (i in 0 until len) cells[addr + i] = value
        return true
    }

    fun size(addr: Int): Int = blocks[addr] ?: 0

    fun dump(addr: Int, len: Int): String {
        val sb = StringBuilder()
        for (i in 0 until len) {
            if (i % 16 == 0) {
                if (i > 0) sb.append("\n")
                sb.append(String.format("%04X: ", addr + i))
            }
            sb.append(String.format("%02X ", (cells[addr + i] ?: "0").toIntOrNull() ?: 0))
        }
        return sb.toString()
    }

    fun reset() = synchronized(lock) {
        cells.clear()
        blocks.clear()
        nextAddr = 1000
    }
}

object MockModel {
    data class MockResponse(val response: String, val code: Int)
    private val mocks = ConcurrentHashMap<String, MockResponse>()

    fun set(url: String, response: String, code: Int = 200) {
        mocks[url] = MockResponse(response, code)
    }

    fun get(url: String): MockResponse? = mocks[url]

    fun remove(url: String): Boolean = mocks.remove(url) != null

    fun reset() { mocks.clear() }

    fun list(): List<String> = mocks.keys.sorted()
}

data class VisualRect(
    val id: Int,
    var target: String,
    var x: Int,
    var y: Int,
    var visible: Boolean = true,
    var color: Color = Color(0xFF5B8DB8),
    var img: String = "",
    var condition: String = ""
)

data class BackgroundSpec(val path: String, val mode: String = "fit")

object VisualModel {
    val rects: SnapshotStateList<VisualRect> = mutableStateListOf()
    val backgrounds: MutableMap<String, BackgroundSpec> = ConcurrentHashMap()
    private var counter = 1
    private val lock = Any()

    fun clear(fileName: String) {
        backgrounds.remove(fileName)
    }

    fun clearAll() {
        rects.clear()
        backgrounds.clear()
    }

    fun createRect(target: String, x: Int, y: Int): VisualRect = synchronized(lock) {
        val r = VisualRect(counter++, target, x, y)
        rects += r
        r
    }

    fun byTarget(target: String): VisualRect? = rects.firstOrNull { it.target == target }
    fun byId(id: Int): VisualRect? = rects.firstOrNull { it.id == id }

    fun updateArea(rect: VisualRect, x: Int, y: Int) {
        rect.x = x
        rect.y = y
    }

    fun remove(rect: VisualRect) {
        rects.removeAll { it.id == rect.id }
    }

    fun setBackground(fileName: String, path: String, mode: String) {
        backgrounds[fileName] = BackgroundSpec(path, mode)
    }

    fun resetBackground(fileName: String) {
        backgrounds.remove(fileName)
    }

    fun backgroundFor(fileName: String): BackgroundSpec? = backgrounds[fileName]
}

data class AskRequest(
    val id: Int,
    val question: String,
    val default: String,
    val type: String,
    val onResult: (String) -> Unit
)

object AskModel {
    val queue: SnapshotStateList<AskRequest> = mutableStateListOf()
    private var counter = 1

    fun ask(question: String, default: String, type: String, onResult: (String) -> Unit): Int {
        val id = counter++
        queue += AskRequest(id, question, default, type, onResult)
        return id
    }

    fun answer(id: Int, value: String) {
        val req = queue.firstOrNull { it.id == id } ?: return
        queue.removeAll { it.id == id }
        req.onResult(value)
    }

    fun cancel(id: Int) {
        val req = queue.firstOrNull { it.id == id } ?: return
        queue.removeAll { it.id == id }
        req.onResult("")
    }
}

object Substrate {
    val DEFAULT_CODE = """
        0 = a
        1 = b
        12 = max

        = do

        a + 1 => :math
        :math = a
        a < max ? ^ do

        => :stop
    """.trimIndent()

    val DEFAULT_AGENT_CODE = DEFAULT_CODE + "\n#00cc22 = color"

    val GITHUB_README_URL = "https://example.com/simple-true-interpreter/README.md"
    val MAX_URL = "https://example.com/simple-true-interpreter"

    val templates: List<Template> = listOf(
        Template(
            id = "hello",
            title = "Hello",
            description = "Вывести сообщение",
            fileName = "hello.cycle",
            code = "hello => :message"
        ),
        Template(
            id = "counter",
            title = "Счётчик",
            description = "Цикл с goto",
            fileName = "counter.cycle",
            code = """
                0 = a
                = loop
                a + 1 => :math => a
                a => :message
                a < 5 ? ^ loop
                Готово! => :message
                => :stop
            """.trimIndent()
        ),
        Template(
            id = "calculator",
            title = "Калькулятор",
            description = ":ask + :math",
            fileName = "calculator.cycle",
            code = """
                Первое число: => :ask => a
                Второе число: => :ask => b
                a + b => :math => result
                Сумма: => :message
                result => :message
                => :stop
            """.trimIndent()
        ),
        Template(
            id = "cave",
            title = "Пещера",
            description = "Игра (:ask, goto, move)",
            fileName = "cave.cycle",
            code = """
                100 = hp
                0 = gold
                факел = item
                3 = potions

                Ты в пещере. Выход справа. Слева — дракон. => :message
                hp = 100, gold = 0, item = факел, potions = 3 => :message

                = turn
                Что делаешь? (1 — идти, 2 — драться, 3 — лечиться, 4 — выход) => :ask

                = wait
                :ask:pending == 1 ? ^ wait
                :ask => choice

                choice == 1 ? ^ go
                choice == 2 ? ^ fight
                choice == 3 ? ^ heal
                choice == 4 ? ^ exit
                Неизвестный выбор. Попробуй снова. => :message
                ^ turn

                = go
                Ты идёшь вглубь пещеры... => :message
                gold + 10 => :math => gold
                Нашёл 10 золота! => :message
                gold => :message
                ^ turn

                = fight
                Дракон атакует! => :message
                hp - 30 => :math => hp
                hp > 0 ? Ты жив! => :message
                hp > 0 ? hp => :message
                hp <= 0 ? ^ dead
                ^ turn

                = heal
                potions > 0 ? Пьёшь зелье... => :message
                potions > 0 ? potions - 1 => :math => potions
                potions > 0 ? hp + 20 => :math => hp
                potions > 0 ? hp => :message
                potions > 0 ? potions => :message
                potions == 0 ? Зелья кончились! => :message
                ^ turn

                = exit
                Ты выходишь из пещеры! => :message
                gold => :message
                hp => :message
                item => :message

                item -> dragon_item
                Факел отдан дракону. => :message
                item => :message
                dragon_item => :message

                ^ ending.cycle

                = dead
                Ты погиб... => :message
                => :stop
            """.trimIndent()
        ),
        Template(
            id = "plugin",
            title = "Плагин",
            description = ":ext",
            fileName = "plugin.cycle",
            code = """
                args => :ext:my_plugin => result
                result => :message
                :ext:list => dst
                dst => :message
                => :stop
            """.trimIndent()
        )
    )

    val TEMPLATE_ENDING = """
        Ты вернулся домой. => :message

        Победа! => :message

        result = победа
        result => :message

        result -> final_result
        result => :message
        final_result => :message

        => :stop
    """.trimIndent()

    val DEFAULT_TEMPLATE_GROUPS = listOf(
        TemplateGroup("Naming", Color(0xFFE3F2FD), listOf(
            "value = name", "value = name *", "value = name @", "= label", "= empty",
            "= result", "value -> nothing")),
        TemplateGroup("Goto", Color(0xFFE8F5E9), listOf(
            "^ 5", "^ label", "^ main.cycle", "1, 2 => :activeTab", "1 => :activeTab")),
        TemplateGroup("Conditions", Color(0xFFFFF8E1), listOf(
            "cond ? ^ 5", "a == b ? ^ 8", "a != b ? ^ 1",
            "a < max ? ^ do", "a > max ? ^ do", "a == b ? a => b", "a != b ? a -> b",
            "cond ? => :pause")),
        TemplateGroup("Math", Color(0xFFFCE4EC), listOf(
            "a + b => :math", "a - b => :math", "a * b => :math", "a / b => :math",
            "a // b => :math", "a % b => :math", "a ** b => :math",
            "a & b => :math", "a | b => :math", "a ^ b => :math", "a << 1 => :math", "a >> 1 => :math",
            "abs(a) => :math", "min(a, b) => :math", "max(a, b) => :math",
            "round(a) => :math", "floor(a) => :math", "ceil(a) => :math",
            "sqrt(a) => :math", "pow(a, b) => :math",
            "popcount(a) => :math", "clz(a) => :math", "ctz(a) => :math",
            "12 > 0 => :math", "a == b => :math",
            "1, 2, 3, 4, 5 => :math:sum", "1, 2, 3, 4, 5 => :math:min",
            "1, 2, 3, 4, 5 => :math:max", "1, 2, 3, 4, 5 => :math:avg",
            "1, 2, 3, 4, 5 => :math:product", "1, 2, 3, 4, 5 => :math:count",
            "5, 3, 8, 1, 9 => :math:sort",
            "1, 2, 3, 4, 5 => :sum", "1, 2, 3, 4, 5 => :min",
            "1, 2, 3, 4, 5 => :max", "1, 2, 3, 4, 5 => :avg",
            "1, 2, 3, 4, 5 => :product", "1, 2, 3, 4, 5 => :count",
            "5, 3, 8, 1, 9 => :sort",
            "a + 1 => :math => a")),
        TemplateGroup("Data — string", Color(0xFFC8E6C9), listOf(
            "data, start, end => :data:substring",
            "data, sub => :data:indexOf",
            "data, old, new => :data:replace",
            "data => :data:lower", "data => :data:upper", "data => :data:trim",
            "data, sep => :data:chars",
            "data, i => :data:charAt",
            "data => :data:size",
            "data:y => :data:size",
            "data => :data:reverse",
            "data => :data:unique",
            "data, v => :data:contains",
            "data, from, to => :data:slice",
            "data, cond => :filter",
            "data, regex => :matches",
            "data, axis => :data:join",
            "data => :s", "data:y => :s",
            "data => :data:empty",
            "data, key => :data:groupby",
            "data, key, count => :data:groupby",
            "data, key, sum => :data:groupby",
            "data, key, avg => :data:groupby")),
        TemplateGroup("Data — list", Color(0xFFA5D6A7), listOf(
            "data:y, value => :data:push",
            "data:y => :data:pop",
            "data:y, i => :data:remove",
            "data:y, i, j => :data:swap",
            "data:y, from, to => :data:deleteRange",
            "data:y, from, to, value => :data:replaceRange",
            "data:y => :data:size",
            "data:y => :data:sort",
            "data:y, value => :push",
            "data:y => :pop",
            "data:y, i => :remove",
            "data:y, i, j => :swap",
            "data:y => :size",
            "data:y => :sort",
            "data:y => :stack:peek",
            "value ->| data:y:3",
            "value =>| data:y:3",
            "value -> data:y:3",
            "data:y:1 => first", "data:y:last => last")),
        TemplateGroup("Data — multi-axis", Color(0xFF80CBC4), listOf(
            "data:z:first:y:1 => z1y1",
            "data:l:2:x:3 => charL2X3",
            "data:y:i => item",
            "log => data", "log -> data", "log:l:1 => last", "log:type => kind")),
        TemplateGroup("Data — splitter", Color(0xFFB2DFDB), listOf(
            ". => raw:splitter:l:;",
            ". => raw:splitter:y:,",
            ". => raw:splitter:z:;",
            ". => raw:splitter:x:.")),
        TemplateGroup("Functional", Color(0xFFB2EBF2), listOf(
            "data:y, n + 1 => :map",
            "data:y, 0, acc + n => :reduce",
            "data:y, 0, acc + n => :fold",
            "data:y, n => :log => :each",
            "data:y, func => :map")),
        TemplateGroup("Data — range/repeat", Color(0xFFB2DFDB), listOf(
            "1, 5 => :range",
            "action, 5 => :repeat")),
        TemplateGroup("JSON", Color(0xFFE0F2F1), listOf(
            "json_text, key => :json:get",
            "json_text, key, value => :json:set",
            "json_text => :json:names",
            "json_text => :json:values")),
        TemplateGroup("XML", Color(0xFFE8F5E9), listOf(
            "xml_text, path => :xml:get",
            "xml_text => :xml:names",
            "xml_text => :xml:values")),
        TemplateGroup("IO", Color(0xFFEDE7F6), listOf(
            "command => :terminal",
            "input.txt => :get",
            "path => :get",
            "url, data => :post",
            "raw:l => :size",
            "file_path => :file:data",
            "file_path, content => :file:data",
            "file_path => :file:origin",
            "file_path, content => :file:origin",
            "file_path => :file:lines",
            "file_path, index => :file:lines",
            "file_path, index, content => :file:lines",
            "file_path, data => :save", "file_path, query => :load",
            "path, content => :file:create", "path => :file:read",
            "path, content => :file:update", "path => :file:delete",
            "path => :file:list", "path => :file:exists",
            "file:a => file:b", "file:a -> file:b",
            "file:a => var", "var => file:b",
            ":file:a => :path",
            "url, query => :api:get", "url, data => :api:post",
            "url, data => :api:put", "url => :api:delete", "url, data => :api:patch",
            "url => :api:websocket",
            "url => :open:url", "path => :open:file", "path => :open:folder")),
        TemplateGroup("Stream", Color(0xFFF0F4C3), listOf(
            "path => :file:stream => dst",
            "path => :file:lines:stream => dst",
            ":stream:next", ":stream:next => dst",
            ":stream:close", ":stream:close => dst",
            ":stream:reset => dst",
            ":stream:limit => dst", "1000 => :stream:limit",
            ":stream:list => dst")),
        TemplateGroup("Retry", Color(0xFFFFEBEE), listOf(
            "url, data => :api:post:retry",
            "url, data, 3 => :api:post:retry",
            "url, data, 3, 1000 => :api:post:retry",
            "url, data, 3, 1000, 1 => :api:post:retry")),
        TemplateGroup("Secrets", Color(0xFFFFF3E0), listOf(
            "key => :secret:get",
            "key, value => :secret:set",
            "key => :secret:remove",
            ":secret:list => dst")),
        TemplateGroup("Send", Color(0xFFE1F5FE), listOf(
            "chat_id, message => :send:telegram",
            "chat_id, message => :send:max",
            "to, subject, body => :send:email")),
        TemplateGroup("Schedule", Color(0xFFFFFDE7), listOf(
            "cron, action => :schedule",
            "cron, action, tz => :schedule",
            ":schedule:list => dst",
            ":schedule:stop", ":schedule:stop => id",
            "10 => :schedule:limit")),
        TemplateGroup("Sound", Color(0xFFF3E5F5), listOf(
            "path => :sound:play",
            ":sound:stop", "path => :sound:stop")),
        TemplateGroup("WebSocket", Color(0xFFE0F2F1), listOf(
            "port => :websocket:listen",
            "message => :websocket:send",
            ":websocket:message => dst",
            ":websocket:close",
            ":websocket:clients => dst")),
        TemplateGroup("Memory", Color(0xFFFFF8E1), listOf(
            "16 => :mem:alloc => p",
            "p, 42 => :mem:write",
            "p => :mem:read",
            "p, 0, 16 => :mem:fill",
            "p, 4 => :mem:dump",
            "p => :mem:free",
            "@1000 => :math")),
        TemplateGroup("Terminal", Color(0xFFE0F2F1), listOf(
            "command => :terminal => Console",
            "text, Console => :terminal:write",
            "Console => :terminal:read",
            "Console => :terminal:close",
            ":terminal:list => dst")),
        TemplateGroup("Ask", Color(0xFFE8EAF6), listOf(
            "question => :ask",
            "question => :ask => dst",
            "question, default => :ask",
            "question, default, number => :ask",
            "question, default, choice:1,2,3 => :ask")),
        TemplateGroup("Visual", Color(0xFFF3E5F5), listOf(
            "path => :background",
            "path, tile => :background",
            ":background:reset",
            "10, 20 => rect_1.area")),
        TemplateGroup("Mock", Color(0xFFFFEBEE), listOf(
            "url, response => :api:mock",
            "url, response, 200 => :api:mock",
            ":api:mock:reset",
            ":api:mock:list => dst")),
        TemplateGroup("AI", Color(0xFFE8EAF6), listOf(
            "key => :secret:get",
            "url, body => :api:post",
            "url, body => :api:post:retry",
            "chat_id, text => :send:telegram",
            "to, subject, body => :send:email")),
        TemplateGroup("Extensions", Color(0xFFF1F8E9), listOf(
            "args => :ext:name",
            "args => :ext:name => dst",
            ":ext:list => dst")),
        TemplateGroup("Functions", Color(0xFFFFFDE7), listOf(
            "data => :result", "data => :source:function",
            "data => :test", "data => :test!",
            ":run", ":log",
            "message => :message",
            ":error ? ^ :handle",
            "0 => :error:resolve",
            "a == b, 'msg' => :assert",
            ":pauseAll", ":resumeAll")),
        TemplateGroup("Error", Color(0xFFFFEBEE), listOf(
            ":error => dst", ":error:line => dst",
            ":error:message => dst", ":error:lines => dst",
            ":error:messages => dst",
            "1 => :error:pause", "0 => :error:pause")),
        TemplateGroup("Agents", Color(0xFFE1F5FE), listOf(
            "^ :stop", "^ :pause", "^ :resume", "^ :delete", "^ :reset",
            ":agent:main:status => dst", ":agent:main:waiter => dst",
            ":agent:status => dst",
            "100 => :agent:wait",
            "100, 5000 => :agent:wait:timeout",
            "message => :agent:broadcast",
            "agent-1, message => :agent:send",
            ":agent:inbox => dst",
            ":agent:inbox:clear",
            "file:main.cycle -> nothing", "value -> nothing",
            ":get = response", ":math = result", ":size = lineCount",
            ":s = lineCount",
            "1 = step",
            "main.cycle = cycle",
            ":result = out",
            "0 = lineCount",
            "#00cc22 = color",
            "100 = speed")),
        TemplateGroup("Simple — UI", Color(0xFFE8EAF6), listOf(
            "button:startAll => :simple:click",
            "button:pauseAll => :simple:click",
            "button:newCycle => :simple:click",
            "button:save => :simple:click",
            "tab:main.cycle => :simple:click",
            "agent:agent-1:play => :simple:click",
            "agent:agent-1:pause => :simple:click",
            "100, 200 => :simple:clickAt",
            "hello, editor:current => :simple:type",
            "hello, editor:current, 100 => :simple:type")),
        TemplateGroup("Simple — Terminal", Color(0xFFE0F2F1), listOf(
            "Console => :simple:openTerminal",
            "ls -la, Console => :simple:sendTerminal",
            "hello, Console => :simple:typeTerminal",
            "Console => :simple:closeTerminal")),
        TemplateGroup("Simple — Search", Color(0xFFF1F8E9), listOf(
            "hello => :simple:search",
            ":simple:search:count => :log",
            ":simple:search:current => :log",
            ":simple:search:next",
            ":simple:search:prev",
            ":simple:search:clear")),
        TemplateGroup("Other", Color(0xFFF3E5F5), listOf(
            "1 => :activeTab", "1, 5 => :activeTab", "\\ literal string"))
    )

    var pendingRenameFile: String? by mutableStateOf(null)
    var version: String by mutableStateOf("1.3.0")
    var languageVersion: String by mutableStateOf("0.13.0")
    var platform: String by mutableStateOf(detectPlatform())

    var activeFileName: String by mutableStateOf("")
    var activeCode: String by mutableStateOf("")
    var activeCursor: Int by mutableStateOf(0)

    var startFileName: String by mutableStateOf("start.cycle")
    var startCode: String by mutableStateOf(DEFAULT_CODE)

    var activeTabView: String by mutableStateOf("Text")
    var validationErrors: List<Error> by mutableStateOf(emptyList())

    var viewingAgentId: Int? by mutableStateOf(null)
    var expandedFileName: String? by mutableStateOf(null)

    var fileOrder: List<String> by mutableStateOf(emptyList())

    val logEntries = mutableStateListOf<LogEntry>()
    val logLines = mutableStateListOf<String>()
    var errorLines: Set<Int> by mutableStateOf(emptySet())
    var resolvedLines: Set<Int> by mutableStateOf(emptySet())
    var logFilter: String by mutableStateOf("All")

    fun logCount(type: String): Int =
        if (type == "All") logEntries.size
        else if (type == "agent") logEntries.count { it.type.startsWith("agent:") }
        else logEntries.count { it.type == type }

    var speedMs: Long by mutableStateOf(400L)
    var strictMode: Boolean by mutableStateOf(false)
    var autoSyncDeclarations: Boolean by mutableStateOf(true)

    var recentFiles: List<String> by mutableStateOf(emptyList())
    var recentProjects: List<String> by mutableStateOf(emptyList())
    var history: List<String> by mutableStateOf(emptyList())

    var showSettingsDialog: Boolean by mutableStateOf(false)
    var showSaveStartDialog: Boolean by mutableStateOf(false)
    var showAboutDialog: Boolean by mutableStateOf(false)
    var showTabsPicker: Boolean by mutableStateOf(false)
    var showSearch: Boolean by mutableStateOf(false)
    var showDocDialog: Boolean by mutableStateOf(false)
    var showSamplesDialog: Boolean by mutableStateOf(false)
    var showDeleteFileDialog: Boolean by mutableStateOf(false)
    var pendingDeleteFile: String? by mutableStateOf(null)
    var showTemplatePicker: Boolean by mutableStateOf(false)

    var docContent: String by mutableStateOf("")
    var docTitle: String by mutableStateOf("")
    var completedSamples: Set<String> by mutableStateOf(emptySet())

    var valuesCollapsed: Boolean by mutableStateOf(false)
    var actionsCollapsed: Boolean by mutableStateOf(false)
    var valuesWidthOverride: Dp? by mutableStateOf(null)
    var actionsWidthOverride: Dp? by mutableStateOf(null)
    var logHeightOverride: Dp? by mutableStateOf(null)

    var pinnedList: SnapshotStateList<String> = mutableStateListOf()
    var pinnedTemplates: List<String> by mutableStateOf(emptyList())
    var actionsSearchQuery: String by mutableStateOf("")

    var searchQuery: String by mutableStateOf("")
    var replaceQuery: String by mutableStateOf("")
    var searchMatches: List<Int> by mutableStateOf(emptyList())
    var searchCurrent: Int by mutableStateOf(0)
    var searchMode: String by mutableStateOf("find")

    var templateGroups: List<TemplateGroup> by mutableStateOf(DEFAULT_TEMPLATE_GROUPS)
    var savedTemplateSets: Map<String, List<TemplateGroup>> by mutableStateOf(emptyMap())

    val COLOR_PALETTE = listOf(
        Color(0xFFB8C9D9), Color(0xFFB8D9B8), Color(0xFFD9D9B8), Color(0xFFD9B8C9),
        Color(0xFFC9B8D9), Color(0xFFB8D9D9), Color(0xFFD9C9B8), Color(0xFFC9C9D9),
        Color(0xFFD9B8B8), Color(0xFFB8B8D9), Color(0xFFC9D9B8), Color(0xFFD9D9C9)
    )

    var copiedValueText: String? by mutableStateOf(null)
    var copiedActionText: String? by mutableStateOf(null)

    fun log(line: String) {
        logLines.add(0, line)
        if (logLines.size > 1000) logLines.removeAt(logLines.lastIndex)
    }

    fun appendLogEntry(entry: LogEntry) {
        logEntries.add(0, entry)
        logLines.add(0, entry.text)
        while (logEntries.size > 1000) logEntries.removeAt(logEntries.lastIndex)
        while (logLines.size > 1000) logLines.removeAt(logLines.lastIndex)
    }

    fun clearLog() {
        logEntries.clear()
        logLines.clear()
        errorLines = emptySet()
        resolvedLines = emptySet()
        logFilter = "All"
    }

    fun showCopied(text: String) { copiedValueText = text }
    fun markSampleDone(id: String) { completedSamples = completedSamples + id }

    fun pinTemplate(text: String) {
        if (text.isBlank()) return
        pinnedTemplates = (listOf(text) + pinnedTemplates.filter { it != text }).take(50)
    }
    fun unpinTemplate(text: String) { pinnedTemplates = pinnedTemplates.filterNot { it == text } }

    fun addGroup(title: String, color: Color) {
        val name = title.trim().ifBlank { "Group ${templateGroups.size + 1}" }
        templateGroups = templateGroups + TemplateGroup(name, color, emptyList())
    }
    fun removeGroup(index: Int) { templateGroups = templateGroups.filterIndexed { i, _ -> i != index } }
    fun renameGroup(index: Int, newTitle: String) {
        templateGroups = templateGroups.mapIndexed { i, g ->
            if (i == index) g.copy(title = newTitle.ifBlank { g.title }) else g
        }
    }
    fun setGroupColor(index: Int, color: Color) {
        templateGroups = templateGroups.mapIndexed { i, g -> if (i == index) g.copy(color = color) else g }
    }
    fun addTemplate(groupIndex: Int, template: String) {
        if (template.isBlank()) return
        templateGroups = templateGroups.mapIndexed { i, g ->
            if (i == groupIndex && !g.templates.contains(template)) g.copy(templates = g.templates + template) else g
        }
    }
    fun removeTemplate(groupIndex: Int, template: String) {
        templateGroups = templateGroups.mapIndexed { i, g ->
            if (i == groupIndex) g.copy(templates = g.templates.filterNot { it == template }) else g
        }
    }
    fun resetTemplatesToDefault() { templateGroups = DEFAULT_TEMPLATE_GROUPS }
    fun saveTemplateSet(name: String) {
        val key = name.trim().ifBlank { "set-${savedTemplateSets.size + 1}" }
        savedTemplateSets = savedTemplateSets + (key to templateGroups)
    }
    fun loadTemplateSet(name: String) { savedTemplateSets[name]?.let { templateGroups = it } }
    fun deleteTemplateSet(name: String) { savedTemplateSets = savedTemplateSets - name }

    fun addFileToOrder(fileName: String) {
        if (fileOrder.contains(fileName)) return
        fileOrder = fileOrder + fileName
    }

    fun removeFileFromOrder(fileName: String) {
        fileOrder = fileOrder - fileName
    }

    fun moveFileUp(fileName: String) {
        val idx = fileOrder.indexOf(fileName)
        if (idx <= 0) return
        val list = fileOrder.toMutableList()
        val tmp = list[idx - 1]
        list[idx - 1] = list[idx]
        list[idx] = tmp
        fileOrder = list
    }

    fun moveFileDown(fileName: String) {
        val idx = fileOrder.indexOf(fileName)
        if (idx < 0 || idx >= fileOrder.size - 1) return
        val list = fileOrder.toMutableList()
        val tmp = list[idx + 1]
        list[idx + 1] = list[idx]
        list[idx] = tmp
        fileOrder = list
    }

    fun addRecentProject(path: String) {
        recentProjects = (listOf(path) + recentProjects.filter { it != path }).take(10)
    }

    fun parseColor(s: String): Color? {
        val t = s.trim().removePrefix("#")
        return try {
            when (t.length) {
                3 -> Color(
                    red = t[0].digitToInt(16) * 17,
                    green = t[1].digitToInt(16) * 17,
                    blue = t[2].digitToInt(16) * 17
                )
                6 -> Color(
                    red = t.substring(0, 2).toInt(16),
                    green = t.substring(2, 4).toInt(16),
                    blue = t.substring(4, 6).toInt(16)
                )
                8 -> Color(
                    red = t.substring(2, 4).toInt(16),
                    green = t.substring(4, 6).toInt(16),
                    blue = t.substring(6, 8).toInt(16),
                    alpha = t.substring(0, 2).toInt(16)
                )
                else -> null
            }
        } catch (_: Exception) { null }
    }

    fun detectPlatform(): String {
        val os = System.getProperty("os.name").lowercase()
        return when {
            os.contains("win") -> "Windows"
            os.contains("mac") -> "macOS"
            else -> "Linux"
        }
    }
}