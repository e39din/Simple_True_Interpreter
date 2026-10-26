package be.simple

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import be.`true`.Interpreter

object TrueHighlighter {
    private val OPERATOR_COLOR = Color(0xFFE67E22)
    private val KEYWORD_COLOR = Color(0xFF2C6EAD)
    private val SYSTEM_COLOR = Color(0xFF8E44AD)
    private val FUNCTION_COLOR = Color(0xFF2E7D32)
    private val NUMBER_COLOR = Color(0xFF1565C0)
    private val STRING_COLOR = Color(0xFF8B4513)
    private val COMMENT_COLOR = Color(0xFF9AA1AC)

    private val SYSTEM_NAMES = setOf(
        "math", "data", "json", "file", "api", "console", "simple",
        "error", "agent", "run", "log", "message", "result", "parameters",
        "range", "repeat", "activeTab", "path", "source", "test", "return",
        "stack", "map", "reduce", "fold", "each"
    )

    private val FUNCTION_NAMES = setOf(
        "sum", "min", "max", "avg", "product", "count", "sort",
        "substring", "indexOf", "replace", "lower", "upper", "trim",
        "chars", "charAt", "size", "s", "reverse", "unique", "contains",
        "slice", "join", "push", "pop", "peek", "remove", "swap",
        "deleteRange", "replaceRange", "filter", "matches",
        "get", "set", "names", "values",
        "create", "read", "update", "delete", "list", "exists", "origin", "lines",
        "post", "put", "patch", "websocket", "save", "load",
        "resolve", "line", "wait", "timeout", "open", "saveAs", "newCycle",
        "newAgent", "openFolder", "rename", "reset", "close", "moveUp", "moveDown",
        "startAll", "pauseAll", "stopAll", "stepAll", "stepBackAll",
        "settings", "about", "samples", "historyLimit", "logLimit",
        "strictMode", "autoSync", "popcount", "clz", "ctz"
    )

    private val OPERATORS_RE = Regex("""(=>\|)|(->\|)|(=>)|(->)|(\^)|(\?)|(?<!\\)=""")
    private val SYSTEM_RE = Regex(""":([A-Za-z_]\w*)""")
    private val NUMBER_RE = Regex("""\b\d+(\.\d+)?\b""")
    private val STRING_RE = Regex(""""[^"]*"|'[^']*'""")

    fun apply(builder: AnnotatedString.Builder, line: String, offset: Int) {
        if (line.isBlank()) return
        val trimmed = line.trimStart()
        val leading = line.length - trimmed.length

        // Comment: line without any operation
        if (!containsOperation(line)) {
            builder.addStyle(
                SpanStyle(color = COMMENT_COLOR, fontStyle = FontStyle.Italic),
                offset, offset + line.length
            )
            return
        }

        // Literal string: starts with \
        if (trimmed.startsWith("\\")) {
            builder.addStyle(
                SpanStyle(color = STRING_COLOR, fontStyle = FontStyle.Italic),
                offset, offset + line.length
            )
            return
        }

        // Strings
        for (m in STRING_RE.findAll(line)) {
            builder.addStyle(SpanStyle(color = STRING_COLOR), offset + m.range.first, offset + m.range.last + 1)
        }

        // Numbers
        for (m in NUMBER_RE.findAll(line)) {
            builder.addStyle(SpanStyle(color = NUMBER_COLOR), offset + m.range.first, offset + m.range.last + 1)
        }

        // Operators
        for (m in OPERATORS_RE.findAll(line)) {
            builder.addStyle(SpanStyle(color = OPERATOR_COLOR, fontWeight = FontWeight.Bold),
                offset + m.range.first, offset + m.range.last + 1)
        }

        // System/function names
        for (m in SYSTEM_RE.findAll(line)) {
            val name = m.groupValues[1]
            val color = when {
                name in FUNCTION_NAMES -> FUNCTION_COLOR
                name in SYSTEM_NAMES -> SYSTEM_COLOR
                else -> SYSTEM_COLOR
            }
            builder.addStyle(SpanStyle(color = color), offset + m.range.first, offset + m.range.last + 1)
        }
    }

    private fun containsOperation(line: String): Boolean {
        return line.contains("=>") || line.contains("->") || line.contains("->|") ||
                line.contains("=>|") || line.contains("^") || line.contains("?") ||
                Regex("""(?<!\\)=""").containsMatchIn(line)
    }
}