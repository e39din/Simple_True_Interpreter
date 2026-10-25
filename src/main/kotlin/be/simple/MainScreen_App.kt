package be.simple

class MainScreen_App {
    val interpreter = Interpreter().apply { historyLimit = Substrate.stepBackLimit }

    fun loadDefault() {
        Substrate.fileName = "untitled.true"
        Substrate.code = DEFAULT_CODE
        interpreter.reset()
    }

    fun validateCurrent(): Boolean {
        val errors = interpreter.validate(interpreter.parse(Substrate.code))
        Substrate.validationErrors = errors
        if (errors.isNotEmpty()) {
            Substrate.clearLog()
            errors.forEach { Substrate.log("Line ${it.lineNumber}: ${it.message}") }
            return false
        }
        return true
    }

    fun step(): StepResult? {
        if (!validateCurrent()) return null
        val result = interpreter.executeOneStep(Substrate.code)
        applyResult(result)
        return result
    }

    fun stepBack(): StepResult? {
        val result = interpreter.executeStepBack() ?: run {
            Substrate.log("No steps to go back")
            return null
        }
        applyResult(result)
        return result
    }

    fun reset() {
        interpreter.reset()
        Substrate.currentStep = 1
        Substrate.status = interpreter.status
        Substrate.clearLog()
        Substrate.log("Reset")
    }

    fun setSpeed(ms: Long) { Substrate.speedMs = ms }

    fun rememberProject(name: String) {
        Substrate.history = (Substrate.history + name).distinct().takeLast(5)
    }

    fun autosave() { if (validateCurrent()) rememberProject(Substrate.fileName) }

    private fun applyResult(result: StepResult) {
        Substrate.currentStep = result.currentStep
        Substrate.status = result.status
        Substrate.log(result.log)
        result.changedVariables.forEach { (k, v) -> Substrate.log("  $k = $v") }
        result.removedVariables.forEach { Substrate.log("  removed: $it") }
    }

    companion object {
        private val DEFAULT_CODE = """
            0 = a
            1 = b
            12 = max

            = do

            a + 1 => math => a
            a < max ? ^ do

            => stop
        """.trimIndent()
    }
}