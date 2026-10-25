package be.simple

import java.net.HttpURLConnection
import java.net.URL

interface ServerApi {
    fun post(url: String, data: String): Int
    fun get(url: String, query: String): String
}

class HttpServerApi : ServerApi {
    override fun post(url: String, data: String): Int = try {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "text/plain")
        }
        conn.outputStream.use { it.write(data.toByteArray()) }
        val code = conn.responseCode
        conn.disconnect()
        if (code in 200..299) 1 else -1
    } catch (e: Exception) { -1 }

    override fun get(url: String, query: String): String = try {
        val full = if (query.isBlank()) url else "$url?$query"
        val conn = (URL(full).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
        }
        val text = conn.inputStream.bufferedReader().readText()
        conn.disconnect()
        text
    } catch (e: Exception) { "" }
}

class MainScreenServer(private val api: ServerApi = HttpServerApi()) {
    fun post(url: String, data: String): Int = api.post(url, data)
    fun get(url: String, query: String): String = api.get(url, query)
}