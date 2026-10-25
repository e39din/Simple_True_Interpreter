package be.`true`

import kotlin.collections.get

object JsonParser {

    private val parseCache = mutableMapOf<String, Any?>()
    private val namesCache = mutableMapOf<String, List<String>>()
    private val valuesCache = mutableMapOf<String, List<String>>()

    fun parse(text: String): Any? = parseCache.getOrPut(text) { parseUncached(text) }

    fun getCached(json: String, path: String): String? = get(json, path)
    fun setCached(json: String, path: String, newValue: String): String = set(json, path, newValue)
    fun namesCached(json: String): List<String> = names(json)
    fun valuesCached(json: String): List<String> = values(json)

    private fun parseUncached(text: String): Any? = Parser(text.trim()).parseValue()

    fun stringify(value: Any?): String = when (value) {
        null -> "null"
        is String -> "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
        is Number -> value.toString()
        is Boolean -> value.toString()
        is Map<*, *> -> value.entries.joinToString(",", "{", "}") { (k, v) ->
            stringify(k.toString()) + ":" + stringify(v)
        }
        is List<*> -> value.joinToString(",", "[", "]") { stringify(it) }
        else -> stringify(value.toString())
    }

    fun get(json: String, path: String): String? {
        val root = parse(json) ?: return null
        var current: Any? = root
        for (part in path.split(":")) {
            current = when (val c = current) {
                is Map<*, *> -> c[part]
                is List<*> -> part.toIntOrNull()?.let { c.getOrNull(it) }
                else -> return null
            } ?: return null
        }
        return valueToString(current)
    }

    fun set(json: String, path: String, newValue: String): String {
        val root = parse(json) ?: return json
        val parts = path.split(":")
        val updated = setRecursive(root, parts, 0, newValue) ?: root
        val s = stringify(updated)
        parseCache[s] = updated
        namesCache.remove(s); valuesCache.remove(s)
        return s
    }

    private fun setRecursive(current: Any?, parts: List<String>, index: Int, newValue: String): Any? {
        if (index >= parts.size) return inferValue(newValue)
        val key = parts[index]
        return when (val c = current) {
            is Map<*, *> -> {
                val map = c.toMutableMap()
                if (index == parts.size - 1) map[key] = inferValue(newValue)
                else map[key] = setRecursive(map[key], parts, index + 1, newValue)
                map
            }
            is List<*> -> {
                val idx = key.toIntOrNull() ?: return c
                val list = c.toMutableList()
                if (index == parts.size - 1) { if (idx in list.indices) list[idx] = inferValue(newValue) }
                else { if (idx in list.indices) list[idx] = setRecursive(list[idx], parts, index + 1, newValue) }
                list
            }
            else -> current
        }
    }

    fun names(json: String): List<String> = namesCache.getOrPut(json) {
        val root = parse(json) as? Map<*, *> ?: return@getOrPut emptyList()
        root.keys.map { it.toString() }
    }

    fun values(json: String): List<String> = valuesCache.getOrPut(json) {
        val root = parse(json) as? Map<*, *> ?: return@getOrPut emptyList()
        root.values.map { valueToString(it) ?: "null" }
    }

    private fun valueToString(v: Any?): String? = when (v) {
        null -> null
        is String -> v
        is Number -> v.toString()
        is Boolean -> v.toString()
        is Map<*, *> -> stringify(v)
        is List<*> -> stringify(v)
        else -> v.toString()
    }

    private fun inferValue(s: String): Any? = when {
        s == "null" -> null
        s == "true" -> true
        s == "false" -> false
        s.toLongOrNull() != null -> s.toLong()
        s.toDoubleOrNull() != null -> s.toDouble()
        else -> s
    }

    private class Parser(private val text: String) {
        private var pos = 0
        fun parseValue(): Any? {
            skipWs()
            if (pos >= text.length) return null
            return when (text[pos]) {
                '{' -> parseObject()
                '[' -> parseArray()
                '"' -> parseString()
                't', 'f' -> parseBoolean()
                'n' -> parseNull()
                else -> parseNumber()
            }
        }
        private fun parseObject(): Map<String, Any?> {
            pos++
            val map = linkedMapOf<String, Any?>()
            skipWs()
            if (pos < text.length && text[pos] == '}') { pos++; return map }
            while (pos < text.length) {
                skipWs()
                val key = parseString()
                skipWs()
                if (pos < text.length && text[pos] == ':') pos++
                map[key] = parseValue()
                skipWs()
                if (pos < text.length && text[pos] == ',') { pos++; continue }
                if (pos < text.length && text[pos] == '}') { pos++; break }
            }
            return map
        }
        private fun parseArray(): List<Any?> {
            pos++
            val list = mutableListOf<Any?>()
            skipWs()
            if (pos < text.length && text[pos] == ']') { pos++; return list }
            while (pos < text.length) {
                list.add(parseValue())
                skipWs()
                if (pos < text.length && text[pos] == ',') { pos++; continue }
                if (pos < text.length && text[pos] == ']') { pos++; break }
            }
            return list
        }
        private fun parseString(): String {
            pos++
            val sb = StringBuilder()
            while (pos < text.length) {
                val c = text[pos]
                when (c) {
                    '"' -> { pos++; return sb.toString() }
                    '\\' -> {
                        pos++
                        if (pos < text.length) {
                            when (text[pos]) {
                                '"' -> sb.append('"'); '\\' -> sb.append('\\'); '/' -> sb.append('/')
                                'n' -> sb.append('\n'); 't' -> sb.append('\t'); 'r' -> sb.append('\r')
                                'b' -> sb.append('\b'); 'f' -> sb.append('\u000C')
                                else -> sb.append(text[pos])
                            }
                            pos++
                        }
                    }
                    else -> { sb.append(c); pos++ }
                }
            }
            return sb.toString()
        }
        private fun parseNumber(): Number {
            val start = pos
            if (pos < text.length && (text[pos] == '-' || text[pos] == '+')) pos++
            while (pos < text.length && (text[pos].isDigit() || text[pos] == '.' || text[pos] == 'e' || text[pos] == 'E' || text[pos] == '+' || text[pos] == '-')) pos++
            val s = text.substring(start, pos)
            return s.toLongOrNull() ?: s.toDoubleOrNull() ?: 0L
        }
        private fun parseBoolean(): Boolean = if (text.startsWith("true", pos)) { pos += 4; true } else { pos += 5; false }
        private fun parseNull(): Any? { pos += 4; return null }
        private fun skipWs() { while (pos < text.length && text[pos].isWhitespace()) pos++ }
    }
}