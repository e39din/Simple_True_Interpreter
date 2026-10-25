package be.`true`

import java.io.File

// fun main(args: Array<String>) {
//     if (args.isEmpty()) {
//         println("Usage: true-core <file.true>")
//         return
//     }
//     val file = File(args[0])
//     if (!file.exists()) {
//         println("File not found: ${file.path}")
//         return
//     }
//     val interp = Interpreter()
//     var guard = 0
//     while (guard < 100000) {
//         guard++
//         val r = interp.executeOneStep(file.readText())
//         println(r.log)
//         if (r.status == Status.FINISHED || r.status == Status.PAUSED) break
//         if (r.errorLine != null) break
//     }
//     println("---")
//     println("Results:")
//     interp.results.forEach { (k, v) -> println("  $k = $v") }
//     println("Variables:")
//     interp.variables.forEach { (k, v) -> println("  $k = $v") }
// }