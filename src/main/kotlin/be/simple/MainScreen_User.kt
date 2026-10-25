package be.simple

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BG = Color(0xFFEFF1F5)
private val TEXT = Color(0xFF2B364D)
private val ACCENT = Color(0xFF4A90E2)

@Composable
fun App() {
    val app = remember { MainScreenApp().also { it.loadDefault() } }
    val phi = 1.618f

    Row(Modifier.fillMaxSize().background(BG).padding(16.dp)) {
        Box(Modifier.weight(phi).fillMaxHeight()) { ValuesPanel(app) }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f).fillMaxHeight()) {
            CodeEditor(app, Modifier.weight(1f))
            Spacer(Modifier.height(16.dp))
            Row(Modifier.weight(0.5f)) {
                Placeholder("Visualization placeholder\nЗдесь будет блок-схема", Modifier.weight(1f))
                Spacer(Modifier.width(16.dp))
                Placeholder("Table placeholder\nЗдесь будет таблица", Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            ActionsPanel()
        }
    }
}

@Composable
private fun ValuesPanel(app: MainScreenApp) {
    Column(Modifier.fillMaxSize().background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp)) {
        Text("Values", color = TEXT, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        Text("Step: ${Substrate.currentStep}   Status: ${Substrate.status}", color = ACCENT)
        Spacer(Modifier.height(8.dp))
        app.interpreter.variables
            .filterKeys { it !in app.interpreter.hiddenVariables }
            .forEach { (k, v) -> Text("$k = $v", color = TEXT) }
        if (app.interpreter.hiddenVariables.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("Hidden:", color = TEXT)
            app.interpreter.hiddenVariables.forEach { k ->
                Text("$k = ${app.interpreter.variables[k]}", color = TEXT.copy(alpha = 0.6f))
            }
        }
        if (app.interpreter.results.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("Results:", color = TEXT)
            app.interpreter.results.forEach { (k, v) -> Text("$k = $v", color = TEXT) }
        }
    }
}

@Composable
private fun CodeEditor(app: MainScreenApp, modifier: Modifier) {
    Column(modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton("Play") { app.autosave(); app.step() }
            ActionButton("Step") { app.step() }
            ActionButton("Step Back") { app.stepBack() }
            ActionButton("Pause") { Substrate.status = Status.PAUSED }
            ActionButton("Stop") { Substrate.status = Status.STOPPED }
            ActionButton("Reset") { app.reset() }
        }
        Spacer(Modifier.height(8.dp))
        var code by remember { mutableStateOf(Substrate.code) }
        BasicTextField(
            value = code,
            onValueChange = { code = it; Substrate.code = it },
            modifier = Modifier.fillMaxSize().background(Color(0xFFF7F8FA), RoundedCornerShape(8.dp)).padding(8.dp),
            textStyle = TextStyle(color = TEXT, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
        )
    }
}

@Composable
private fun ActionButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick) { Text(label, color = Color.White) }
}

@Composable
private fun Placeholder(label: String, modifier: Modifier) {
    Box(modifier.fillMaxSize().background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp)) {
        Text(label, color = TEXT.copy(alpha = 0.6f))
    }
}

@Composable
private fun ActionsPanel() {
    Column(Modifier.fillMaxSize().background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp)) {
        Text("Actions", color = TEXT, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Substrate.logLines.forEach { Text(it, color = TEXT, fontSize = 13.sp) }
        }
    }
}