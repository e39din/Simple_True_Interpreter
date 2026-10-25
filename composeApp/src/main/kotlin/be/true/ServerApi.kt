package be.`true`

import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.util.concurrent.CompletionStage

object ServerApi {
    fun request(method: String, url: String, body: String?, headers: Map<String, String>): Pair<Int, String> = try {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method.uppercase()
            connectTimeout = 10000
            readTimeout = 10000
            headers.forEach { (k, v) -> setRequestProperty(k, v) }
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "text/plain; charset=utf-8")
            }
        }
        if (body != null) conn.outputStream.use { it.write(body.toByteArray()) }
        val code = conn.responseCode
        val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
            ?.bufferedReader()?.readText() ?: ""
        conn.disconnect()
        code to text
    } catch (e: Exception) {
        -1 to (e.message ?: "error")
    }

    fun websocket(url: String, onMessage: (String) -> Unit): AutoCloseable {
        val client = HttpClient.newHttpClient()
        val listener = object : WebSocket.Listener {
            override fun onText(webSocket: WebSocket, data: CharSequence, last: Boolean): CompletionStage<*>? {
                onMessage(data.toString())
                webSocket.request(1)
                return null
            }
        }
        val ws = client.newWebSocketBuilder().buildAsync(URI(url), listener).join()
        return AutoCloseable { ws.sendClose(WebSocket.NORMAL_CLOSURE, "bye") }
    }
}