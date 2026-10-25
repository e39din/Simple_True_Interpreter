package be.simple

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import be.`true`.Error
import be.`true`.Interpreter
import be.`true`.LogEntry
import be.`true`.Status

data class TemplateGroup(val title: String, val color: Color, val templates: List<String>)

data class TabState(
    val id: Int,
    var fileName: String,
    var code: String,
    var currentStep: Int = 1,
    var status: Status = Status.STOPPED,
    var interpreter: Interpreter = Interpreter(),
    var cursorPosition: Int = 0,
    var dirty: Boolean = false,
    val undoStack: ArrayDeque<String> = ArrayDeque(),
    val redoStack: ArrayDeque<String> = ArrayDeque()
)

object TabsModel {
    private var counter = 1
    val tabs = mutableListOf<TabState>()
    var activeIndex: Int by mutableStateOf(-1)

    fun newId(): Int = counter++

    fun create(fileName: String, code: String, makeActive: Boolean = true,
               interpreter: Interpreter = Interpreter()): TabState {
        val tab = TabState(id = newId(), fileName = fileName, code = code, interpreter = interpreter)
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
        if (activeIndex in tabs.indices && activeIndex != index) {
            tabs[activeIndex].status = Status.PAUSED
        }
        activeIndex = index
    }

    fun active(): TabState? = tabs.getOrNull(activeIndex)
    fun rename(index: Int, newName: String) { tabs.getOrNull(index)?.fileName = newName }
}

object Substrate {
    val DEFAULT_CODE = """
        0 = a
        1 = b
        12 = max

        = do

        a + 1 => math => a
        a < max ? ^ do

        => stop
    """.trimIndent()

    val GITHUB_README_URL = "https://example.com/simple-true-interpreter/README.md"
    val MAX_URL = "https://example.com/simple-true-interpreter"

    val DEFAULT_TEMPLATE_GROUPS = listOf(
        TemplateGroup("Naming", Color(0xFFE3F2FD), listOf(
            "value = name", "value = name *", "value = name @", "= label")),
        TemplateGroup("Goto", Color(0xFFE8F5E9), listOf(
            "^ step:5", "^ label", "1, 2 => activeTab", "1 => activeTab")),
        TemplateGroup("Conditions", Color(0xFFFFF8E1), listOf(
            "cond ? ^ step:5", "a == b ? ^ step:8", "a != b ? ^ step:1",
            "a < max ? ^ do", "a > max ? ^ do", "a == b ? a => b", "a != b ? a -> b",
            "cond ? => pause")),
        TemplateGroup("Math", Color(0xFFFCE4EC), listOf(
            "a + b => math", "a - b => math", "a * b => math", "a / b => math",
            "a // b => math", "a % b => math", "a ** b => math",
            "a & b => math", "a | b => math", "a ^ b => math", "a << 1 => math", "a >> 1 => math",
            "abs(a) => math", "min(a, b) => math", "max(a, b) => math",
            "round(a) => math", "floor(a) => math", "ceil(a) => math",
            "sqrt(a) => math", "pow(a, b) => math",
            "popcount(a) => math", "clz(a) => math", "ctz(a) => math",
            "12 > 0 => math", "a == b => math",
            "1, 2, 3, 4, 5 => math:sum", "1, 2, 3, 4, 5 => math:min",
            "1, 2, 3, 4, 5 => math:max", "1, 2, 3, 4, 5 => math:avg",
            "1, 2, 3, 4, 5 => math:product", "1, 2, 3, 4, 5 => math:count",
            "5, 3, 8, 1, 9 => math:sort",
            "math => result", "a + 1 => math => a")),
        TemplateGroup("Data — string", Color(0xFFC8E6C9), listOf(
            "data, start, end => data:substring",
            "data, sub => data:indexOf",
            "data, old, new => data:replace",
            "data => data:lower", "data => data:upper", "data => data:trim",
            "data, sep => data:chars",
            "data, i => data:charAt",
            "data => data:length",
            "data => data:reverse",
            "data => data:unique",
            "data, v => data:contains",
            "data, from, to => data:slice",
            "data, cond => filter",
            "data, regex => matches",
            "data, axis => data:join")),
        TemplateGroup("Data — list", Color(0xFFA5D6A7), listOf(
            "data:y, value => data:push",
            "data:y => data:pop",
            "data:y, i, value => data:insert",
            "data:y, i => data:remove",
            "data:y, i, j => data:swap",
            "data:y, i, value => data:set",
            "data:y, from, to => data:deleteRange",
            "data:y, from, to, value => data:replaceRange",
            "data:y => data:length",
            "data:y => data:sort",
            "data:y:1 => first", "data:y:last => last")),
        TemplateGroup("Data — multi-axis", Color(0xFF80CBC4), listOf(
            "data:z:first:y:1 => z1y1",
            "data:l:2:x:3 => charL2X3",
            "data:y:i => item",
            "log => data", "log -> data", "log:l:1 => last", "log:type => kind")),
        TemplateGroup("Data — splitter", Color(0xFFB2DFDB), listOf(
            ". => data:splitter:x :",
            "data => data:splitter:x:. ",
            "data => data:splitter:y: ")),
        TemplateGroup("Data — foreach", Color(0xFFB2EBF2), listOf(
            "data:foreach:y ; n : out\$n, => data:push",
            "data:foreach:z, y ; row, col : out\$row\$col, => data:push")),
        TemplateGroup("JSON", Color(0xFFE0F2F1), listOf(
            "json_text, key => json:get",
            "json_text, key, value => json:set",
            "json_text => json:names",
            "json_text => json:values",
            "json_text, current_weather:temperature => json:get",
            "json_text, current_weather:is_day, 0 => json:set")),
        TemplateGroup("IO", Color(0xFFEDE7F6), listOf(
            "command => console", "path, flags => run",
            "file_path => file:data",
            "file_path, content => file:data",
            "file_path => file:origin",
            "file_path, content => file:origin",
            "file_path => file:lines",
            "file_path, index => file:lines",
            "file_path, index, content => file:lines",
            "file_path, data => save", "file_path, query => load",
            "path, content => file:create", "path => file:read",
            "path, content => file:update", "path => file:delete",
            "path => file:list", "path => file:exists",
            "url, query => api:get", "url, data => api:post",
            "url, data => api:put", "url => api:delete", "url, data => api:patch",
            "url => api:websocket")),
        TemplateGroup("Functions", Color(0xFFFFFDE7), listOf(
            "data => return", "data => .:source:function", "data => source:function",
            "data => test", "data => test!")),
        TemplateGroup("Other", Color(0xFFF3E5F5), listOf(
            "1 => activeTab", "1, 5 => activeTab", "\\ literal string"))
    )

    var version: String by mutableStateOf("0.19.0")
    var languageVersion: String by mutableStateOf("0.7.0")
    var platform: String by mutableStateOf(detectPlatform())

    var activeFileName: String by mutableStateOf("")
    var activeCode: String by mutableStateOf("")
    var activeCursor: Int by mutableStateOf(0)

    var startFileName: String by mutableStateOf("start.true")
    var startCode: String by mutableStateOf(DEFAULT_CODE)

    var activeTabView: String by mutableStateOf("Text")
    var validationErrors: List<Error> by mutableStateOf(emptyList())
    var status: Status by mutableStateOf(Status.STOPPED)
    var currentStep: Int by mutableStateOf(1)
    var stepBackLimit: Int by mutableStateOf(12)

    var variablesSnapshot: Map<String, String> by mutableStateOf(emptyMap())
    var hiddenSnapshot: Set<String> by mutableStateOf(emptySet())
    var resultsSnapshot: Map<String, String> by mutableStateOf(emptyMap())
    var constantsSnapshot: Set<String> by mutableStateOf(emptySet())

    val logEntries = mutableStateListOf<LogEntry>()
    val logLines = mutableStateListOf<String>()

    /** Строки с ошибками — обновляется только в applyResult, чтобы LineHighlighter не парсил лог. */
    var errorLines: Set<Int> by mutableStateOf(emptySet())

    var history: List<String> by mutableStateOf(emptyList())
    var speedMs: Long by mutableStateOf(400L)

    var recentFiles: List<String> by mutableStateOf(emptyList())
    var showSettingsDialog: Boolean by mutableStateOf(false)
    var showSaveStartDialog: Boolean by mutableStateOf(false)
    var showAboutDialog: Boolean by mutableStateOf(false)
    var showTabsPicker: Boolean by mutableStateOf(false)
    var showWelcome: Boolean by mutableStateOf(true)
    var showSearch: Boolean by mutableStateOf(false)
    var showDocDialog: Boolean by mutableStateOf(false)
    var showTutorial: Boolean by mutableStateOf(false)
    var showSamplesDialog: Boolean by mutableStateOf(false)
    var docContent: String by mutableStateOf("")
    var docTitle: String by mutableStateOf("")
    var welcomeShown: Boolean by mutableStateOf(false)
    var tutorialCompleted: Boolean by mutableStateOf(false)

    var autoSyncDeclarations: Boolean by mutableStateOf(true)
    var strictMode: Boolean by mutableStateOf(false)

    var completedSamples: Set<String> by mutableStateOf(emptySet())

    var actionsCollapsed: Boolean by mutableStateOf(false)
    var valuesCollapsed: Boolean by mutableStateOf(false)
    var pinnedTemplates: List<String> by mutableStateOf(emptyList())
    var actionsSearchQuery: String by mutableStateOf("")

    /** Вкладка, которая сейчас исполняется в Play. null — Play не идёт. */
    var runningTabId: Int? by mutableStateOf(null)

    /** Переопределения ширины панелей пользователем (drag-сплиттеры). */
    var valuesWidthOverride: Dp? by mutableStateOf(null)
    var actionsWidthOverride: Dp? by mutableStateOf(null)

    var templateGroups: List<TemplateGroup> by mutableStateOf(DEFAULT_TEMPLATE_GROUPS)
    var savedTemplateSets: Map<String, List<TemplateGroup>> by mutableStateOf(emptyMap())

    val COLOR_PALETTE = listOf(
        Color(0xFFE3F2FD), Color(0xFFE8F5E9), Color(0xFFFFF8E1), Color(0xFFFCE4EC),
        Color(0xFFF3E5F5), Color(0xFFE0F7FA), Color(0xFFFFF3E0), Color(0xFFEDE7F6),
        Color(0xFFFFEBEE), Color(0xFFE8EAF6), Color(0xFFF1F8E9), Color(0xFFFFFDE7)
    )

    var copiedValueText: String? by mutableStateOf(null)

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

    fun clearLog() { logEntries.clear(); logLines.clear(); errorLines = emptySet() }
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

    fun detectPlatform(): String {
        val os = System.getProperty("os.name").lowercase()
        return when {
            os.contains("win") -> "Windows"
            os.contains("mac") -> "macOS"
            else -> "Linux"
        }
    }
}