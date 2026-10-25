package be.simple

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class MainScreen_System(private val app: MainScreenApp) {
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun startAutoRun() {
        if (job?.isActive == true) return
        job = scope.launch {
            while (Substrate.status != Status.FINISHED && Substrate.status != Status.STOPPED) {
                val r = app.step() ?: break
                if (r.status == Status.PAUSED || r.status == Status.FINISHED) break
                delay(Substrate.speedMs)
            }
        }
    }

    fun stopAutoRun() {
        job?.cancel()
        job = null
    }

    fun save(name: String, content: String): Boolean = try {
        val dir = File(System.getProperty("user.home"), ".simple").apply { mkdirs() }
        File(dir, name).writeText(content)
        true
    } catch (e: Exception) { false }

    fun load(name: String): String? = try {
        val dir = File(System.getProperty("user.home"), ".simple")
        File(dir, name).takeIf { it.exists() }?.readText()
    } catch (e: Exception) { null }
}