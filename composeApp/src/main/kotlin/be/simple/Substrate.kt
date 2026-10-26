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

data class TemplateGroup(val title: String, val color: Color, val templates: List<String>)

enum class FileKind { CYCLE, AGENT, OTHER }

fun fileKindOf(fileName: String): FileKind = when {
    fileName.endsWith(".agent", ignoreCase = true) -> FileKind.AGENT
    fileName.endsWith(".cycle", ignoreCase = true) -> FileKind.CYCLE
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
}

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
        Color(0xFFE53935), Color(0xFFFB8C00), Color(0xFFFDD835),
        Color(0xFF43A047), Color(0xFF1E88E5), Color(0xFF3949AB), Color(0xFF8E24AA)
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
        agents += agent
        return agent
    }

    fun remove(id: Int) { agents.removeAll { it.id == id } }

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

object Substrate {
    val DEFAULT_CODE = """
        0 = a
        1 = b
        12 = max

        = do

        a + 1 => :math => a
        a < max ? ^ do

        => :stop
    """.trimIndent()

    val DEFAULT_AGENT_CODE = DEFAULT_CODE + "\n#00cc22 = color"

    val GITHUB_README_URL = "https://example.com/simple-true-interpreter/README.md"
    val MAX_URL = "https://example.com/simple-true-interpreter"

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
            ":math = result", "a + 1 => :math => a")),
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
            "data => :s", "data:y => :s")),
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
        TemplateGroup("IO", Color(0xFFEDE7F6), listOf(
            "command => :console",
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
            "url => :api:websocket")),
        TemplateGroup("Functions", Color(0xFFFFFDE7), listOf(
            "data => :result", "data => :source:function",
            "data => :test", "data => :test!",
            ":run", ":log",
            "message => :message",
            ":error ? ^ :handle",
            "1 => :error:resolve")),
        TemplateGroup("Error", Color(0xFFFFEBEE), listOf(
            ":error => dst", ":error:line => dst",
            ":error:message => dst", ":error:lines => dst",
            ":error:messages => dst")),
        TemplateGroup("Agents", Color(0xFFE1F5FE), listOf(
            "^ :stop", "^ :pause", "^ :resume", "^ :delete", "^ :reset",
            ":agent:main:status => dst", ":agent:main:waiter => dst",
            ":agent:status => dst",
            "100 => :agent:wait",
            "100, 5000 => :agent:wait:timeout",
            "file:main.cycle -> nothing", "value -> nothing",
            ":get = response", ":math = result", ":size = lineCount",
            ":s = lineCount",
            "1 = step",
            "main.cycle = cycle",
            ":result = out",
            "0 = lineCount",
            "#00cc22 = color",
            "100 = speed")),
        TemplateGroup("Other", Color(0xFFF3E5F5), listOf(
            "1 => :activeTab", "1, 5 => :activeTab", "\\ literal string"))
    )

    var pendingRenameFile: String? by mutableStateOf(null)
    var version: String by mutableStateOf("1.1.0")
    var languageVersion: String by mutableStateOf("0.11.0")
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

    var docContent: String by mutableStateOf("")
    var docTitle: String by mutableStateOf("")
    var completedSamples: Set<String> by mutableStateOf(emptySet())

    var valuesCollapsed: Boolean by mutableStateOf(false)
    var actionsCollapsed: Boolean by mutableStateOf(false)
    var valuesWidthOverride: Dp? by mutableStateOf(null)
    var actionsWidthOverride: Dp? by mutableStateOf(null)
    var logHeightOverride: Dp? by mutableStateOf(null)

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
        Color(0xFFE3F2FD), Color(0xFFE8F5E9), Color(0xFFFFF8E1), Color(0xFFFCE4EC),
        Color(0xFFF3E5F5), Color(0xFFE0F7FA), Color(0xFFFFF3E0), Color(0xFFEDE7F6),
        Color(0xFFFFEBEE), Color(0xFFE8EAF6), Color(0xFFF1F8E9), Color(0xFFFFFDE7)
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