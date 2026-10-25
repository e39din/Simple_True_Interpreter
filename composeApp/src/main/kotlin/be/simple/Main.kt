package be.simple

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Simple True Interpreter 0.18.0",
        state = rememberWindowState(width = 1400.dp, height = 900.dp)
    ) { App() }
}