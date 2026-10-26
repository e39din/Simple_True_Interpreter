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
            val header = Regex("""^###\s+(\d+[a-z]?)\.\s+(.+)$""").find(line.trim())
            if (header != null) {
                flush()
                curId = "sample${header.groupValues[1].replace(Regex("[^0-9a-z]"), "")}"
                curTitle = "${header.groupValues[1]}. ${header.groupValues[2].trim()}"
                curExpected = mutableMapOf()
                inCode = false
                continue
            }
            val expect = Regex("""^!!\s*([A-Za-z_]\w*)\s*:\s*(.+)$""").find(line.trim())
            if (expect != null && curId != null) {
                curExpected?.put(expect.groupValues[1], expect.groupValues[2])
                continue
            }
            if (line.trim() == "```true") { inCode = true; continue }
            if (line.trim() == "```") { inCode = false; continue }
            if (curId != null && inCode) buf.appendLine(line)
        }
        flush()
        return items
    }

    private fun builtin(): List<Item> = listOf(
        Item("sample1", "1. Sum of array",
            "= result\n1, 2, 3, 4, 5 => :sum => result",
            mapOf("result" to "15")),
        Item("sample2", "2. Maximum",
            "= result\n3, 7, 2, 9, 4 => :max => result",
            mapOf("result" to "9")),
        Item("sample3", "3. Hello",
            "Hello, world = result",
            mapOf("result" to "Hello, world"))
    )
}