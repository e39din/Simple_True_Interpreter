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
            File(System.getProperty("user.home"), ".simple/SAMPLES.txt"),
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

        fun flush() {
            val id = curId ?: return
            items += Item(id, curTitle ?: id, buf.toString().trimEnd(), curExpected ?: mutableMapOf())
            curId = null; curTitle = null; curExpected = null; buf.clear()
        }

        for (line in text.lines()) {
            val header = Regex("""^##\s*([A-Za-z_]\w*)\s*\|\s*(.+)$""").find(line.trim())
            if (header != null) {
                flush()
                curId = header.groupValues[1]
                curTitle = header.groupValues[2].trim()
                curExpected = mutableMapOf()
                continue
            }
            val expect = Regex("""^!!\s*([A-Za-z_]\w*)\s*=\s*(.+)$""").find(line.trim())
            if (expect != null && curId != null) {
                curExpected?.put(expect.groupValues[1], expect.groupValues[2])
                continue
            }
            if (curId != null) buf.appendLine(line)
        }
        flush()
        return items
    }

    private fun builtin(): List<Item> = listOf(
        Item("sum", "1. Сумма массива", "1, 2, 3, 4, 5 => math:sum\n=> stop", mapOf("math" to "15")),
        Item("max", "2. Максимум", "3, 7, 2, 9, 4 => math:max\n=> stop", mapOf("math" to "9")),
        Item("hello", "6. Hello", "\"Hello, world\" => out\n=> stop")
    )
}