package be.simple

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun WelcomeDialog(app: MainScreenApp, sys: MainScreenSystem, onDismiss: () -> Unit) {
    val BG = Color(0xFFEFF1F5); val TEXT = Color(0xFF2B364D); val ACCENT = Color(0xFF4A90E2)
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White,
            modifier = Modifier.onPreviewKeyEvent { ev ->
                if (ev.type == KeyEventType.KeyDown && ev.key == Key.Escape) { onDismiss(); true } else false
            }) {
            Row(Modifier.width(880.dp).height(560.dp).padding(24.dp)) {
                Column(Modifier.weight(0.382f).fillMaxHeight().verticalScroll(rememberScrollState())) {
                    Text("Simple True Interpreter", color = TEXT, fontSize = 22.sp)
                    Text("Версия ${Substrate.version} · язык ${Substrate.languageVersion}",
                        color = TEXT.copy(alpha = 0.6f), fontSize = 12.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Начать", color = TEXT, fontSize = 16.sp); Spacer(Modifier.height(8.dp))
                    WelcomeAction("＋ Новый файл", ACCENT) { app.newTab(); onDismiss() }
                    WelcomeAction("📂 Открыть файл…", ACCENT) { sys.openFileDialog()?.let { sys.openFile(it) }; onDismiss() }
                    WelcomeAction("📁 Открыть папку…", ACCENT) { sys.openFolderDialog()?.let { sys.openFolder(it) }; onDismiss() }
                    Spacer(Modifier.height(16.dp))
                    Text("Путь", color = TEXT, fontSize = 16.sp); Spacer(Modifier.height(8.dp))
                    WelcomeAction("📘 Simple — примеры", Color(0xFF2E7D32)) {
                        Substrate.showSamplesDialog = true; onDismiss()
                    }
                    WelcomeAction("🎓 Advanced — пустой файл", Color(0xFF6A1B9A)) { app.applyDefault(); onDismiss() }
                    Spacer(Modifier.height(16.dp))
                    Text("Документация", color = TEXT, fontSize = 16.sp); Spacer(Modifier.height(8.dp))
                    WelcomeAction("LANGUAGE.md", Color(0xFF607D8B)) {
                        val doc = sys.readDoc("LANGUAGE.md")
                        if (doc != null) { Substrate.docTitle = "LANGUAGE.md"; Substrate.docContent = doc; Substrate.showDocDialog = true }
                        else Substrate.showAboutDialog = true
                        onDismiss()
                    }
                    WelcomeAction("README.md", Color(0xFF607D8B)) {
                        val doc = sys.readDoc("README.md")
                        if (doc != null) { Substrate.docTitle = "README.md"; Substrate.docContent = doc; Substrate.showDocDialog = true }
                        else Substrate.showAboutDialog = true
                        onDismiss()
                    }
                    WelcomeAction("Не показывать при старте", Color(0xFF9AA1AC)) {
                        Substrate.welcomeShown = true; sys.saveSettings(); onDismiss()
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(
                    Modifier.weight(0.618f).fillMaxHeight()
                        .background(BG, RoundedCornerShape(12.dp)).padding(16.dp)
                        .clickable { Substrate.showSamplesDialog = true; onDismiss() }
                ) {
                    Text("SAMPLES (${Substrate.completedSamples.size}/${SAMPLES.items.size}) — нажмите, чтобы открыть",
                        color = TEXT, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        SAMPLES.items.forEach { item ->
                            Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                .background(Color.White, RoundedCornerShape(6.dp)).padding(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (item.id in Substrate.completedSamples) {
                                        Text("✓", color = Color(0xFF2E7D32), fontSize = 14.sp)
                                        Spacer(Modifier.width(6.dp))
                                    }
                                    Text(item.title, color = TEXT, fontSize = 12.sp)
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(item.code.lines().take(3).joinToString("\n"),
                                    color = TEXT.copy(alpha = 0.55f), fontSize = 10.sp, lineHeight = 12.sp)
                            }
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeAction(label: String, color: Color, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(vertical = 3.dp)
        .background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
        .clickable { onClick() }.padding(horizontal = 10.dp, vertical = 8.dp)
    ) { Text(label, color = color, fontSize = 13.sp) }
}