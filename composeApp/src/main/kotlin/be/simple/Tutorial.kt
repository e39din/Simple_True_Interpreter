package be.simple

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import be.`true`.Status

data class TutorialStep(
    val title: String,
    val explanation: String,
    val code: String,
    val expectedValues: Map<String, String>
)

object Tutorial {
    val steps = listOf(
        TutorialStep(
            "1. Именование",
            "Данные слева, имя справа. `12 = a` — теперь у нас есть `a`.",
            "12 = a",
            mapOf("a" to "12")
        ),
        TutorialStep(
            "2. math",
            "`a + 1 => :math => a` — взять `a`, прибавить 1, положить обратно.",
            "12 = a\na + 1 => :math => a",
            mapOf("a" to "13")
        ),
        TutorialStep(
            "3. Условие",
            "`a < 5 ? ^ done` — если `a < 5`, перейти к метке `done`.",
            "12 = a\na < 5 ? ^ done\n=> stop\n= done\n=> stop",
            mapOf("a" to "12")
        ),
        TutorialStep(
            "4. Остановка",
            "`=> stop` — остановить программу. Без него — цикл.",
            "12 = a\na + 1 => :math => a\n=> stop",
            mapOf("a" to "13")
        )
    )
}

@Composable
fun TutorialDialog(app: MainScreenApp, sys: MainScreenSystem, onDismiss: () -> Unit) {
    val BG = Color(0xFFEFF1F5)
    val TEXT = Color(0xFF2B364D)
    val ACCENT = Color(0xFF4A90E2)
    val ZEBRA = Color(0xFFF0F2F5)
    val CURRENT = Color(0xFFDDF0E0)

    var stepIndex by remember { mutableIntStateOf(0) }
    var stepResults by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var logLines by remember { mutableStateOf<List<String>>(emptyList()) }
    val step = Tutorial.steps[stepIndex]
    val total = Tutorial.steps.size

    fun resetStep() {
        stepResults = emptyMap()
        logLines = emptyList()
        val tab = TabsModel.active()
        if (tab != null) {
            tab.code = step.code
            tab.interpreter.reset()
            tab.currentStep = 1
            tab.status = Status.STOPPED
            app.publishActive()
        }
    }

    fun stepOnce() {
        val tab = TabsModel.active() ?: return
        tab.code = step.code
        tab.interpreter.reset()
        tab.currentStep = 1
        tab.status = Status.STOPPED
        val res = tab.interpreter.executeOneStep(tab.code)
        logLines = logLines + res.log
        stepResults = LinkedHashMap(tab.interpreter.variables)
        app.publishActive()
    }

    fun finishAndContinue() {
        app.applyCode(step.code, step.code)
        Substrate.tutorialCompleted = true
        sys.saveSettings()
        onDismiss()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp), color = Color.White,
            modifier = Modifier.onPreviewKeyEvent { ev ->
                if (ev.type == KeyEventType.KeyDown && ev.key == Key.Escape) { onDismiss(); true } else false
            }
        ) {
            Column(Modifier.width(880.dp).height(560.dp).padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Interactive Tutorial", color = TEXT, fontSize = 22.sp)
                    Spacer(Modifier.weight(1f))
                    Tutorial.steps.indices.forEach { i ->
                        Box(Modifier.size(10.dp).background(
                            if (i <= stepIndex) ACCENT else ZEBRA, RoundedCornerShape(5.dp)))
                        Spacer(Modifier.width(4.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("${stepIndex + 1}/$total", color = TEXT.copy(alpha = 0.6f), fontSize = 12.sp)
                }
                Spacer(Modifier.height(16.dp))

                Row(Modifier.weight(1f)) {
                    Column(Modifier.weight(0.382f).fillMaxHeight().verticalScroll(rememberScrollState())) {
                        Text(step.title, color = TEXT, fontSize = 18.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(step.explanation, color = TEXT, fontSize = 14.sp, lineHeight = 20.sp)
                        Spacer(Modifier.height(16.dp))
                        Text("Значения после Step:", color = TEXT.copy(alpha = 0.6f), fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        if (stepResults.isEmpty()) Text("(пусто)", color = TEXT.copy(alpha = 0.5f), fontSize = 13.sp)
                        else stepResults.forEach { (k, v) -> Text("$k: $v", color = TEXT, fontSize = 13.sp, fontFamily = FontFamily.Monospace) }
                        Spacer(Modifier.height(16.dp))
                        Text("Log:", color = TEXT.copy(alpha = 0.6f), fontSize = 12.sp)
                        Spacer(Modifier.height(4.dp))
                        logLines.takeLast(5).forEach { l ->
                            Text(l, color = TEXT.copy(alpha = 0.7f), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(0.618f).fillMaxHeight()
                        .background(BG, RoundedCornerShape(12.dp)).padding(12.dp)) {
                        Text("Код:", color = TEXT.copy(alpha = 0.6f), fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.fillMaxWidth().weight(1f).background(Color(0xFFF7F8FA), RoundedCornerShape(8.dp)).padding(8.dp)) {
                            Column(Modifier.verticalScroll(rememberScrollState())) {
                                step.code.lines().forEachIndexed { idx, line ->
                                    val isCurrent = idx + 1 == (TabsModel.active()?.currentStep ?: 1)
                                    Box(Modifier.fillMaxWidth().background(
                                        if (isCurrent) CURRENT else Color.Transparent, RoundedCornerShape(4.dp)
                                    ).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                        Text(line, color = TEXT, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TutorialButton("Step", ACCENT) { stepOnce() }
                    TutorialButton("Reset", Color(0xFF9AA1AC)) { resetStep() }
                    Spacer(Modifier.weight(1f))
                    if (stepIndex < total - 1) {
                        TutorialButton("Next", ACCENT) { stepIndex++; resetStep() }
                    } else {
                        TutorialButton("Готово → SAMPLES", Color(0xFF2E7D32)) { finishAndContinue() }
                    }
                    TutorialButton("Skip", Color(0xFF9AA1AC)) { onDismiss() }
                }
            }
        }
    }
}

@Composable
private fun TutorialButton(label: String, color: Color, onClick: () -> Unit) {
    Box(Modifier.background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
        .clickable { onClick() }.padding(horizontal = 14.dp, vertical = 8.dp)) {
        Text(label, color = color, fontSize = 13.sp)
    }
}