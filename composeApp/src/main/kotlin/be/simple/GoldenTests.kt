package be.simple

import be.`true`.Interpreter
import be.`true`.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException

object GoldenTests {
    data class Result(val id: String, val title: String, val passed: Boolean, val message: String)

    fun runAll(): List<Result> = runBlocking {
        coroutineScope {
            SAMPLES.items.map { item -> async(Dispatchers.Default) { runOne(item) } }.awaitAll()
        }
    }

    private fun runOne(item: SAMPLES.Item): Result {
        return try {
            runBlocking {
                withTimeout(30_000) { runOneInternal(item) }
            }
        } catch (e: TimeoutCancellationException) {
            Result(item.id, item.title, false, "timeout 30s")
        }
    }

    private fun runOneInternal(item: SAMPLES.Item): Result {
        val interp = Interpreter()
        interp.actions = ":run"
        var guard = 0
        var steps = 0
        var failure: String? = null
        while (guard < 10000) {
            guard++
            try {
                interp.waitUntilMs = 0
                interp.timeoutMs = null
                val res = interp.executeOneStep(item.code)
                steps++
                if (res.errorLine != null) { failure = "error on line ${res.errorLine}"; break }
                if (res.status == Status.FINISHED || res.status == Status.PAUSED) break
            } catch (e: Exception) { failure = "exception: ${e.message}"; break }
        }
        if (guard >= 10000 && failure == null) failure = "guard exceeded"
        if (interp.status == Status.RUNNING) failure = failure ?: "left RUNNING"
        if (failure == null) {
            for ((k, v) in item.expectedResults) {
                val actual = interp.results[k] ?: interp.variables[k]
                if (actual != v) { failure = "result '$k' = '$actual', expected '$v'"; break }
            }
        }
        return Result(item.id, item.title, failure == null, failure ?: "ok ($steps steps)")
    }
}