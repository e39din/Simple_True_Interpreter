package be.simple

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object Strings {
    var lang: String by mutableStateOf("en")

    fun t(key: String): String {
        val map = if (lang == "ru") ru else en
        return map[key] ?: en[key] ?: key
    }

    fun t(key: String, vararg args: Pair<String, String>): String {
        var s = t(key)
        for ((k, v) in args) s = s.replace("{$k}", v)
        return s
    }

    internal val en: Map<String, String> get() = Strings_en.data
    internal val ru: Map<String, String> get() = Strings_ru.data
}