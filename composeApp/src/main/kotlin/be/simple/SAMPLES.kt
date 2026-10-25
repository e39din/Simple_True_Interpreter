package be.simple

import java.io.File

object SAMPLES {
    data class Item(
        val id: String,
        val title: String,
        val code: String,
        val expectedResults: Map<String, String> = emptyMap()
    )

    private var cache: List<Item>? = null

    val items: List<Item>
        get() {
            cache?.let { return it }
            val parsed = loadFromTxt() ?: builtin()
            cache = parsed
            return parsed
        }

    fun invalidate() { cache = null }

    private fun loadFromTxt(): List<Item>? {
        val candidates = listOf(
            File(System.getProperty("user.home"), ".simple/SAMPLES.md"),
            File(System.getProperty("user.home"), ".simple/SAMPLES.txt"),
            File("SAMPLES.md"),
            File("SAMPLES.txt")
        )
        val file = candidates.firstOrNull { it.exists() } ?: return null
        return try { parse(file.readText()) } catch (_: Exception) { null }
    }

    fun parse(text: String): List<Item> {
        val items = mutableListOf<Item>()
        var curId: String? = null
        var curTitle: String? = null
        var curExpected: MutableMap<String, String>? = null
        val buf = StringBuilder()
        var inCode = false

        fun flush() {
            val id = curId ?: return
            items += Item(id, curTitle ?: id, buf.toString().trimEnd(), curExpected ?: mutableMapOf())
            curId = null; curTitle = null; curExpected = null; buf.clear()
        }

        for (line in text.lines()) {
            val header = Regex("""^###\s+(\d+)\.\s+(.+)$""").find(line.trim())
            if (header != null) {
                flush()
                curId = "sample${header.groupValues[1]}"
                curTitle = "${header.groupValues[1]}. ${header.groupValues[2].trim()}"
                curExpected = mutableMapOf()
                inCode = false
                continue
            }
            val legacyHeader = Regex("""^##\s*([A-Za-z_]\w*)\s*\|\s*(.+)$""").find(line.trim())
            if (legacyHeader != null) {
                flush()
                curId = legacyHeader.groupValues[1]
                curTitle = legacyHeader.groupValues[2].trim()
                curExpected = mutableMapOf()
                inCode = false
                continue
            }
            val expect = Regex("""^!!\s*([A-Za-z_]\w*)\s*=\s*(.+)$""").find(line.trim())
            if (expect != null && curId != null) {
                curExpected?.put(expect.groupValues[1], expect.groupValues[2])
                continue
            }
            if (line.trim() == "```true") { inCode = true; continue }
            if (line.trim() == "```") { inCode = false; continue }
            if (curId != null && inCode) buf.appendLine(line)
            else if (curId != null && !line.trim().startsWith("#") && !line.trim().startsWith("**") && line.trim().isNotEmpty()) {
                if (buf.isEmpty()) buf.appendLine(line)
            }
        }
        flush()
        return items
    }

    private fun builtin(): List<Item> = listOf(
        Item("sample1", "1. Сумма массива",
            "1, 2, 3, 4, 5 => :math:sum\nmath:sum = sum\n=> stop",
            mapOf("math:sum" to "15")),
        Item("sample2", "2. Максимум",
            "3, 7, 2, 9, 4 => :math:max\nmath:max = mx\n=> stop",
            mapOf("math:max" to "9")),
        Item("sample3", "3. Hello",
            "Hello, world = out\n=> stop")
    )
}