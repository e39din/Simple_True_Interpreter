package be.`true`

import java.io.File
import java.util.concurrent.TimeUnit

object OsApi {
    data class ExecResult(val exitCode: Int, val stdout: String, val stderr: String)

    private fun shellCommand(): List<String> {
        val os = System.getProperty("os.name").lowercase()
        return if (os.contains("win")) listOf("cmd.exe", "/c") else listOf("/bin/sh", "-c")
    }

    fun console(command: String, timeoutSec: Long = 15): ExecResult = try {
        val proc = ProcessBuilder(shellCommand() + command).redirectErrorStream(false).start()
        val finished = proc.waitFor(timeoutSec, TimeUnit.SECONDS)
        if (!finished) {
            proc.destroyForcibly()
            ExecResult(-1, "", "timeout")
        } else {
            ExecResult(proc.exitValue(),
                proc.inputStream.bufferedReader().readText(),
                proc.errorStream.bufferedReader().readText())
        }
    } catch (e: Exception) {
        ExecResult(-1, "", e.message ?: "error")
    }

    fun run(path: String, flags: String?): ExecResult = try {
        val args = mutableListOf(path)
        if (!flags.isNullOrBlank()) args += flags.split(" ").filter { it.isNotBlank() }
        val proc = ProcessBuilder(args).redirectErrorStream(false).start()
        proc.waitFor(30, TimeUnit.SECONDS)
        ExecResult(proc.exitValue(),
            proc.inputStream.bufferedReader().readText(),
            proc.errorStream.bufferedReader().readText())
    } catch (e: Exception) {
        ExecResult(-1, "", e.message ?: "error")
    }

    fun fileCreate(path: String, content: String): Int = try {
        val f = File(path)
        if (f.exists()) -1 else { f.parentFile?.mkdirs(); f.writeText(content); 1 }
    } catch (e: Exception) { -1 }

    fun fileRead(path: String): String = try { File(path).readText() } catch (e: Exception) { "" }

    fun fileUpdate(path: String, content: String): Int = try {
        File(path).parentFile?.mkdirs(); File(path).writeText(content); 1
    } catch (e: Exception) { -1 }

    fun fileDelete(path: String): Int = try {
        if (File(path).delete()) 1 else -1
    } catch (e: Exception) { -1 }

    fun fileList(path: String): String = try {
        File(path).listFiles()?.joinToString(",") { it.name } ?: ""
    } catch (e: Exception) { "" }

    fun fileExists(path: String): Int = if (File(path).exists()) 1 else 0
}