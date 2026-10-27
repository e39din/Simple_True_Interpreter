package be.simple

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.`true`.Status
import java.io.File
import kotlin.math.abs

private val TEXT = Color(0xFF1E1E1E)
private val TEXT_MUTED = Color(0xFF8A8A8A)
private val ACCENT = Color(0xFF5B8DB8)
private val ERROR = Color(0xFFB85C5C)
private val WARN = Color(0xFFC49A5C)
private val BG_SOFT = Color(0xFFF7F5F2)
private val DIVIDER = Color(0x26000000)
private val ACCENT_LIGHT = Color(0x0F5B8DB8)

@Composable
fun MainScreenUser(app: MainScreenApp) {
    val activeView = Substrate.activeTabView
    Column(Modifier.fillMaxSize().background(Color.White)) {
        Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ViewTab("Text", activeView) { Substrate.activeTabView = "Text" }
            ViewTab("Visual", activeView) { Substrate.activeTabView = "Visual" }
            ViewTab("Table", activeView) { Substrate.activeTabView = "Table" }
        }
        when (activeView) {
            "Visual" -> VisualFeed(app)
            "Table" -> TablePanel(app)
            else -> TextEditorPanel(app)
        }
    }
    AskDialog(app)
    TemplatePickerDialog(app)
}

@Composable
private fun ViewTab(label: String, active: String, onClick: () -> Unit) {
    val selected = label == active
    Box(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selected) ACCENT else BG_SOFT)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            color = if (selected) Color.White else TEXT,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun TextEditorPanel(app: MainScreenApp) {
    val tab = TabsModel.active()
    if (tab == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active file", color = TEXT_MUTED, fontSize = 13.sp)
        }
        return
    }
    Column(Modifier.fillMaxSize().padding(8.dp)) {
        Text(tab.fileName, color = TEXT, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Spacer(Modifier.height(4.dp))
        BasicTextField(
            value = tab.code,
            onValueChange = { app.updateCode(it) },
            modifier = Modifier.fillMaxSize().background(BG_SOFT, RoundedCornerShape(6.dp)).padding(8.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                color = TEXT, fontSize = 11.sp, fontFamily = FontFamily.Monospace
            )
        )
    }
}

@Composable
private fun VisualFeed(app: MainScreenApp) {
    val cycleFiles = Substrate.fileOrder.filter { name ->
        val tab = TabsModel.byFileName(name)
        tab != null && tab.kind == FileKind.CYCLE
    }
    if (cycleFiles.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No .cycle files", color = TEXT_MUTED, fontSize = 13.sp)
        }
        return
    }
    LazyRow(
        Modifier.fillMaxSize().padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        itemsIndexed(cycleFiles) { idx, fileName ->
            if (idx > 0) {
                Box(
                    Modifier
                        .width(1.dp)
                        .fillParentMaxHeight()
                        .padding(vertical = 12.dp)
                        .background(DIVIDER)
                )
                Spacer(Modifier.width(12.dp))
            }
            VisualColumn(app, fileName, Modifier.width(360.dp).fillParentMaxHeight())
        }
    }
}

@Composable
private fun VisualColumn(app: MainScreenApp, fileName: String, modifier: Modifier) {
    val tab = TabsModel.byFileName(fileName) ?: return
    val agent = AgentsModel.agentFor(fileName)
    val isActive = Substrate.activeFileName == fileName
    Column(
        modifier
            .padding(8.dp)
            .background(if (isActive) Color(0xFFF8FBFF) else Color.White, RoundedCornerShape(8.dp))
            .border(1.dp, if (isActive) ACCENT else DIVIDER, RoundedCornerShape(8.dp))
    ) {
        Row(
            Modifier.fillMaxWidth().clickable {
                val idx = TabsModel.tabs.indexOfFirst { it.fileName == fileName }
                if (idx >= 0) app.setActiveTab(idx)
            }.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔄", fontSize = 11.sp)
            Spacer(Modifier.width(4.dp))
            Text(fileName, color = TEXT, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            if (isActive) {
                Spacer(Modifier.width(6.dp))
                Box(Modifier.height(2.dp).width(24.dp).background(ACCENT))
            }
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            VisualCanvas(app, fileName, tab)
        }
        if (agent != null) {
            AgentFooter(app, agent)
        } else {
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("(no agent)", color = TEXT_MUTED, fontSize = 11.sp)
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier.clip(RoundedCornerShape(4.dp)).background(ACCENT).clickable {
                        app.createAgentFor(fileName)
                    }.padding(horizontal = 8.dp, vertical = 4.dp)
                ) { Text("+ Create agent", color = Color.White, fontSize = 10.sp) }
            }
        }
    }
}

@Composable
private fun VisualCanvas(app: MainScreenApp, fileName: String, tab: TabState) {
    val rects by VisualModel.rects.collectAsStateCompat()
    val visibleRects = rects.filter { it.target.isNotEmpty() || true }
    val bg = VisualModel.backgroundFor(fileName)
    val errorLines = Substrate.errorLines
    val agent = AgentsModel.agentFor(fileName)

    LaunchedEffect(fileName, tab.code) {
        val names = tab.interpreter.variables.keys.filter { it !in tab.interpreter.hiddenVariables }
        for (n in names) {
            if (VisualModel.byTarget(n) == null) {
                VisualModel.createRect(n, 20 + (VisualModel.rects.size % 5) * 70, 20 + (VisualModel.rects.size / 5) * 70)
            }
        }
    }

    var creating by remember { mutableStateOf(false) }
    var createStart by remember { mutableStateOf(Offset.Zero) }
    var createEnd by remember { mutableStateOf(Offset.Zero) }
    var draggingRect by remember { mutableStateOf<VisualRect?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var contextMenuRect by remember { mutableStateOf<VisualRect?>(null) }
    var contextMenuPos by remember { mutableStateOf(Offset.Zero) }
    var editingName by remember { mutableStateOf<VisualRect?>(null) }
    var editingValue by remember { mutableStateOf<VisualRect?>(null) }
    var contextMenuAgent by remember { mutableStateOf(false) }
    var contextMenuAgentPos by remember { mutableStateOf(Offset.Zero) }

    Box(Modifier.fillMaxSize()) {
        Canvas(
            Modifier
                .fillMaxSize()
                .pointerInput(rects) {
                    detectTapGestures(
                        onTap = { pos ->
                            if (contextMenuRect != null) { contextMenuRect = null; return@detectTapGestures }
                            if (contextMenuAgent) { contextMenuAgent = false; return@detectTapGestures }
                            val hit = visibleRects.firstOrNull { abs(it.x - pos.x.toInt()) < 52 && abs(it.y - pos.y.toInt()) < 40 }
                            if (hit == null && agent != null) {
                                val agentX = 20f
                                val agentY = size.height - 40f
                                if (abs(agentX - pos.x) < 12 && abs(agentY - pos.y) < 12) {
                                    contextMenuAgent = true
                                    contextMenuAgentPos = pos
                                }
                            }
                        },
                        onDoubleTap = { pos ->
                            val hit = visibleRects.firstOrNull { abs(it.x - pos.x.toInt()) < 52 && abs(it.y - pos.y.toInt()) < 40 }
                            if (hit != null && hit.target.isEmpty()) {
                                editingName = hit
                            }
                        },
                        onLongPress = { pos ->
                            val hit = visibleRects.firstOrNull { abs(it.x - pos.x.toInt()) < 52 && abs(it.y - pos.y.toInt()) < 40 }
                            if (hit != null) {
                                contextMenuRect = hit
                                contextMenuPos = pos
                            }
                        }
                    )
                }
                .pointerInput(rects) {
                    detectDragGestures(
                        onDragStart = { pos ->
                            val hit = visibleRects.firstOrNull { abs(it.x - pos.x.toInt()) < 52 && abs(it.y - pos.y.toInt()) < 40 }
                            if (hit != null) {
                                draggingRect = hit
                                dragOffset = Offset(pos.x - hit.x, pos.y - hit.y)
                            } else {
                                creating = true
                                createStart = pos
                                createEnd = pos
                            }
                        },
                        onDrag = { change, amount ->
                            change.consume()
                            val pos = change.position
                            if (draggingRect != null) {
                                val r = draggingRect!!
                                r.x = (pos.x - dragOffset.x).toInt()
                                r.y = (pos.y - dragOffset.y).toInt()
                            } else if (creating) {
                                createEnd = pos
                            }
                        },
                        onDragEnd = {
                            if (creating) {
                                val dx = abs(createEnd.x - createStart.x)
                                val dy = abs(createEnd.y - createStart.y)
                                if (dx > 5.dp.value && dy > 5.dp.value) {
                                    val newRect = VisualModel.createRect(
                                        "", createStart.x.toInt(), createStart.y.toInt()
                                    )
                                    editingName = newRect
                                }
                                creating = false
                            }
                            draggingRect = null
                        },
                        onDragCancel = {
                            creating = false
                            draggingRect = null
                        }
                    )
                }
        ) {
            for (r in visibleRects) {
                val center = Offset(r.x.toFloat() + 52, r.y.toFloat() + 26)
                val radius = 52f
                drawCircle(color = r.color, radius = radius, center = center, style = Stroke(width = 2f))
                if (!r.visible) {
                    drawCircle(color = Color.Gray, radius = radius, center = center, style = Stroke(width = 1f))
                }
                if (r.target in errorLines) {
                    drawCircle(color = ERROR, radius = radius + 2f, center = center, style = Stroke(width = 1.5f))
                }
            }
            if (creating) {
                val x = minOf(createStart.x, createEnd.x)
                val y = minOf(createStart.y, createEnd.y)
                val w = abs(createEnd.x - createStart.x)
                val h = abs(createEnd.y - createStart.y)
                drawRect(
                    color = ACCENT.copy(alpha = 0.3f),
                    topLeft = Offset(x, y),
                    size = Size(w, h),
                    style = Stroke(width = 2f)
                )
            }
            if (agent != null) {
                val agentX = 20f
                val agentY = size.height - 40f
                drawCircle(color = agent.color, radius = 8f, center = Offset(agentX, agentY))
            }
        }

        for (r in visibleRects) {
            val name = r.target
            val value = tab.interpreter.variables[name] ?: ""
            Column(
                Modifier
                    .offset(x = r.x.dp, y = r.y.dp)
                    .width(104.dp)
                    .clickable { editingName = r }
            ) {
                Text(name, color = TEXT, fontSize = 11.sp, fontFamily = FontFamily.Monospace,
                    maxLines = 1, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(2.dp))
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        value.take(20),
                        color = TEXT,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                }
            }
        }

        if (agent != null) {
            Row(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 32.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val running = agent.status == Status.RUNNING
                Box(
                    Modifier.clip(RoundedCornerShape(4.dp)).background(ACCENT).clickable {
                        val system = app.systemRef()
                        if (running) system?.pauseAgent(agent.id) else system?.startAgent(agent.id)
                    }.padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(if (running) "⏸" else "▶", color = Color.White, fontSize = 10.sp)
                }
                if (agent.status != Status.RUNNING) {
                    Box(
                        Modifier.clip(RoundedCornerShape(4.dp)).background(BG_SOFT).clickable {
                            app.systemRef()?.stepBackAgent(agent.id)
                        }.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) { Text("⏮", color = TEXT, fontSize = 10.sp) }
                    Box(
                        Modifier.clip(RoundedCornerShape(4.dp)).background(BG_SOFT).clickable {
                            app.systemRef()?.stepAgent(agent.id)
                        }.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) { Text("⏭", color = TEXT, fontSize = 10.sp) }
                }
            }
        }

        contextMenuRect?.let { r ->
            ContextMenu(r, contextMenuPos) {
                contextMenuRect = null
            }
        }

        if (contextMenuAgent && agent != null) {
            Box(
                Modifier
                    .offset(x = contextMenuAgentPos.x.dp, y = (contextMenuAgentPos.y - 60).dp)
                    .width(200.dp)
                    .background(Color.White, RoundedCornerShape(6.dp))
                    .border(1.dp, DIVIDER, RoundedCornerShape(6.dp))
            ) {
                Row(
                    Modifier.fillMaxWidth().clickable {
                        app.setActiveFile(agent.fileName)
                        contextMenuAgent = false
                    }.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✎", fontSize = 11.sp, color = TEXT)
                    Spacer(Modifier.width(8.dp))
                    Text("Перейти к коду агента", fontSize = 11.sp, color = TEXT)
                }
            }
        }

        editingName?.let { r ->
            InlineEditor(
                initial = r.target,
                onCommit = { newName ->
                    val old = r.target
                    if (newName.isNotBlank() && newName != old) {
                        r.target = newName
                        if (old.isNotEmpty()) {
                            val tabCur = TabsModel.active()
                            if (tabCur != null) {
                                val re = Regex("(?<![A-Za-z0-9_:])${Regex.escape(old)}(?![A-Za-z0-9_])")
                                val newCode = re.replace(tabCur.code, newName)
                                app.updateCode(newCode)
                            }
                        }
                    }
                    editingName = null
                },
                onCancel = { editingName = null }
            )
        }

        editingValue?.let { r ->
            val cur = tab.interpreter.variables[r.target] ?: ""
            InlineEditor(
                initial = cur,
                onCommit = { newValue ->
                    if (r.target.isNotEmpty()) {
                        tab.interpreter.variables[r.target] = newValue
                        app.updateCode(tab.code)
                    }
                    editingValue = null
                },
                onCancel = { editingValue = null }
            )
        }
    }
}

@Composable
private fun ContextMenu(rect: VisualRect, pos: Offset, onClose: () -> Unit) {
    val app = remember { MainScreenApp() }
    Box(
        Modifier
            .offset(x = pos.x.dp, y = pos.y.dp)
            .width(200.dp)
            .background(Color.White, RoundedCornerShape(6.dp))
            .border(1.dp, DIVIDER, RoundedCornerShape(6.dp))
    ) {
        Column {
            val enabled = rect.target.isNotEmpty()
            Row(
                Modifier.fillMaxWidth().clickable(enabled = enabled) {
                    if (enabled) {
                        val tab = TabsModel.active()
                        if (tab != null) tab.cursorPosition = 0
                    }
                    onClose()
                }.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("✎", fontSize = 11.sp, color = if (enabled) TEXT else TEXT_MUTED)
                Spacer(Modifier.width(8.dp))
                Text("Перейти к коду", fontSize = 11.sp, color = if (enabled) TEXT else TEXT_MUTED)
            }
            HorizontalDivider(color = DIVIDER)
            Row(
                Modifier.fillMaxWidth().clickable {
                    if (rect.target.isNotEmpty()) {
                        app.deleteVariable(rect.target)
                    }
                    VisualModel.remove(rect)
                    onClose()
                }.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🗑", fontSize = 11.sp, color = ERROR)
                Spacer(Modifier.width(8.dp))
                Text("Удалить", fontSize = 11.sp, color = ERROR)
            }
        }
    }
}

@Composable
private fun InlineEditor(initial: String, onCommit: (String) -> Unit, onCancel: () -> Unit) {
    var text by remember { mutableStateOf(initial) }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x66000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .width(320.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().background(BG_SOFT, RoundedCornerShape(4.dp)).padding(8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = TEXT, fontSize = 13.sp, fontFamily = FontFamily.Monospace
                ),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier.clip(RoundedCornerShape(4.dp)).background(ACCENT).clickable { onCommit(text) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) { Text("OK", color = Color.White, fontSize = 11.sp) }
                Box(
                    Modifier.clip(RoundedCornerShape(4.dp)).background(BG_SOFT).clickable { onCancel() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) { Text("Cancel", color = TEXT, fontSize = 11.sp) }
            }
        }
    }
}

@Composable
private fun AgentFooter(app: MainScreenApp, agent: AgentState) {
    Column(Modifier.fillMaxWidth().padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(8.dp).clip(CircleShape).background(
                    when (agent.status) {
                        Status.RUNNING -> Color(0xFF5C9E5C)
                        Status.PAUSED -> WARN
                        Status.FINISHED -> ACCENT
                        else -> TEXT_MUTED
                    }
                )
            )
            Spacer(Modifier.width(6.dp))
            Text(agent.name, color = TEXT, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            if (agent.waiter) {
                Spacer(Modifier.width(4.dp))
                Text("⏳", fontSize = 10.sp)
            }
            if (agent.scheduleJobs.isNotEmpty()) {
                Spacer(Modifier.width(4.dp))
                Text("⏰ ${agent.scheduleJobs.size}", color = WARN, fontSize = 10.sp)
            }
            Spacer(Modifier.weight(1f))
            Text(
                when (agent.status) {
                    Status.RUNNING -> "running"
                    Status.PAUSED -> "paused"
                    Status.FINISHED -> "finished"
                    else -> "stopped"
                },
                color = TEXT_MUTED, fontSize = 10.sp
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            val running = agent.status == Status.RUNNING
            val canStep = agent.status != Status.RUNNING
            Box(
                Modifier.clip(RoundedCornerShape(4.dp)).background(ACCENT).clickable {
                    val sys = app.systemRef()
                    if (running) sys?.pauseAgent(agent.id) else sys?.startAgent(agent.id)
                }.padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(if (running) "⏸" else "▶", color = Color.White, fontSize = 11.sp)
            }
            if (canStep) {
                Box(
                    Modifier.clip(RoundedCornerShape(4.dp)).background(BG_SOFT).clickable {
                        app.systemRef()?.stepBackAgent(agent.id)
                    }.padding(horizontal = 8.dp, vertical = 4.dp)
                ) { Text("⏮", color = TEXT, fontSize = 11.sp) }
                Box(
                    Modifier.clip(RoundedCornerShape(4.dp)).background(BG_SOFT).clickable {
                        app.systemRef()?.stepAgent(agent.id)
                    }.padding(horizontal = 8.dp, vertical = 4.dp)
                ) { Text("⏭", color = TEXT, fontSize = 11.sp) }
            }
        }
    }
}

@Composable
private fun TablePanel(app: MainScreenApp) {
    val rects by VisualModel.rects.collectAsStateCompat()
    val tab = TabsModel.active() ?: return
    val defaultWidths = mapOf(
        "number" to 100, "area" to 120, "visible" to 60, "color" to 80,
        "img" to 120, "condition" to 120
    )
    val columns = defaultWidths.keys.toList()
    val currentStep = tab.currentStep

    Column(Modifier.fillMaxSize().padding(8.dp)) {
        Row(Modifier.fillMaxWidth().background(BG_SOFT, RoundedCornerShape(4.dp))) {
            for (col in columns) {
                Box(Modifier.width(defaultWidths[col]!!.dp).padding(8.dp)) {
                    Text(col, color = TEXT, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        HorizontalDivider()
        LazyColumn(Modifier.fillMaxSize()) {
            itemsIndexed(rects.toList()) { idx, r ->
                val error = r.target in Substrate.errorLines
                Row(
                    Modifier.fillMaxWidth()
                        .background(if (error) Color(0x1FB85C5C) else Color.Transparent)
                ) {
                    Cell(r.target.ifEmpty { "rect_${r.id}" }, defaultWidths["number"]!!.dp)
                    Cell("${r.x}, ${r.y}", defaultWidths["area"]!!.dp)
                    Box(Modifier.width(defaultWidths["visible"]!!.dp).padding(8.dp)) {
                        Checkbox(checked = r.visible, onCheckedChange = { r.visible = it })
                    }
                    Box(Modifier.width(defaultWidths["color"]!!.dp).padding(8.dp)) {
                        Box(Modifier.size(16.dp).background(r.color, CircleShape))
                    }
                    Cell(r.img, defaultWidths["img"]!!.dp)
                    Cell(r.condition, defaultWidths["condition"]!!.dp)
                }
                HorizontalDivider(color = DIVIDER)
            }
        }
    }
}

@Composable
private fun Cell(text: String, width: androidx.compose.ui.unit.Dp) {
    Box(Modifier.width(width).padding(8.dp)) {
        Text(text, color = TEXT, fontSize = 11.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
    }
}

@Composable
fun InboxPanel(app: MainScreenApp, agent: AgentState) {
    val content = remember(agent.unreadCount) { app.readInboxContent(agent.id) }
    Column(
        Modifier.fillMaxSize()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, agent.color, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("📥 ${agent.name}.inbox", color = TEXT, fontSize = 13.sp)
            Spacer(Modifier.weight(1f))
            if (agent.unreadCount > 0) {
                Box(Modifier.background(ACCENT, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp)) {
                    Text("${agent.unreadCount} new", color = Color.White, fontSize = 10.sp)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        SelectionContainer {
            Text(content, color = TEXT, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun FileColumn(app: MainScreenApp, fileName: String) {
    val tab = TabsModel.byFileName(fileName)
    val agent = AgentsModel.agentFor(fileName)
    when (tab?.kind) {
        FileKind.INBOX -> {
            val owner = AgentsModel.agents.firstOrNull { it.inboxFileName == fileName }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📥", fontSize = 11.sp)
                Spacer(Modifier.width(4.dp))
                Text(fileName, color = TEXT, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                if (owner != null && owner.unreadCount > 0) {
                    Spacer(Modifier.width(4.dp))
                    Box(
                        Modifier.background(ACCENT, RoundedCornerShape(6.dp))
                            .padding(horizontal = 4.dp)
                    ) {
                        Text("${owner.unreadCount}", color = Color.White, fontSize = 9.sp)
                    }
                }
            }
        }
        FileKind.AGENT -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (agent != null) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(agent.color))
                    Spacer(Modifier.width(6.dp))
                }
                Text(fileName, color = TEXT, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            }
        }
        else -> {
            Text(fileName, color = TEXT, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun FileChip(app: MainScreenApp, fileName: String, active: Boolean) {
    val tab = TabsModel.byFileName(fileName)
    val icon = when (tab?.kind) {
        FileKind.INBOX -> "📥 "
        FileKind.AGENT -> "🤖 "
        FileKind.CYCLE -> "🔄 "
        else -> ""
    }
    Box(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (active) ACCENT else BG_SOFT)
            .clickable {
                val idx = TabsModel.tabs.indexOfFirst { it.fileName == fileName }
                if (idx >= 0) app.setActiveTab(idx)
            }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            "$icon$fileName",
            color = if (active) Color.White else TEXT,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun ValuesPanel(app: MainScreenApp) {
    val agent = app.viewingAgent() ?: return
    val vars = agent.variablesSnapshot
    val hidden = agent.hiddenSnapshot
    Column(Modifier.fillMaxSize().padding(8.dp)) {
        Text("Values", color = TEXT, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        LazyColumn {
            items(vars.keys.filter { it !in hidden }.sorted()) { key ->
                ValueRow(key, vars[key] ?: "", agent)
            }
        }
    }
}

@Composable
private fun ValueRow(key: String, rawValue: String, agent: AgentState) {
    val isSecret = SecretModel.isSecret(key)
    val display = if (isSecret) "***" else rawValue
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(key, color = TEXT, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        Spacer(Modifier.width(6.dp))
        Text("=", color = TEXT_MUTED, fontSize = 11.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            display,
            color = if (isSecret) WARN else TEXT,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 1
        )
    }
}

@Composable
fun ActionsPanel(app: MainScreenApp) {
    val groups = Substrate.templateGroups
    Column(Modifier.fillMaxSize().padding(8.dp)) {
        Text("Actions", color = TEXT, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        LazyColumn {
            items(groups) { group ->
                Text(group.title, color = TEXT, fontSize = 11.sp, fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 4.dp))
                for (t in group.templates.take(5)) {
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))
                            .clickable { app.insertAtCursor(t) }
                            .padding(vertical = 2.dp, horizontal = 4.dp)
                    ) {
                        Text(t, color = TEXT_MUTED, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
fun LogPanel(app: MainScreenApp) {
    val entries = Substrate.logEntries
    val filter = Substrate.logFilter
    Column(Modifier.fillMaxSize().padding(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (f in listOf("All", "info", "error", "assert", "agent")) {
                val active = f == filter
                Box(
                    Modifier.clip(RoundedCornerShape(4.dp))
                        .background(if (active) ACCENT else BG_SOFT)
                        .clickable { Substrate.logFilter = f }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(f, color = if (active) Color.White else TEXT, fontSize = 10.sp)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        LazyColumn {
            items(entries.filter {
                when (filter) {
                    "All" -> true
                    "agent" -> it.type.startsWith("agent:")
                    else -> it.type == filter
                }
            }) { e ->
                Text(
                    e.text,
                    color = when (e.type) {
                        "error" -> ERROR
                        "assert" -> if (e.resolved) Color(0xFF5C9E5C) else WARN
                        else -> TEXT
                    },
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
fun SnackbarHost() {
    val messages = SnackbarModel.messages
    Column(
        Modifier.fillMaxWidth().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (m in messages) {
            Row(
                Modifier.fillMaxWidth()
                    .background(
                        when (m.kind) {
                            "error" -> ERROR
                            "warning" -> WARN
                            else -> ACCENT
                        },
                        RoundedCornerShape(6.dp)
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(m.text, color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                Box(Modifier.clickable { SnackbarModel.dismiss(m.id) }.padding(4.dp)) {
                    Text("✕", color = Color.White, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(app: MainScreenApp) {
    if (!Substrate.showSettingsDialog) return
    Dialog(onDismissRequest = { Substrate.showSettingsDialog = false }) {
        Column(
            Modifier.width(520.dp).background(Color.White, RoundedCornerShape(8.dp)).padding(16.dp)
        ) {
            Text("Settings", color = TEXT, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = Substrate.autoSyncDeclarations,
                    onCheckedChange = { Substrate.autoSyncDeclarations = it })
                Text("Auto-sync declarations", color = TEXT, fontSize = 11.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = Substrate.strictMode,
                    onCheckedChange = { Substrate.strictMode = it })
                Text("Strict Mode", color = TEXT, fontSize = 11.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text("Speed: ${Substrate.speedMs} ms", color = TEXT, fontSize = 11.sp)
            Slider(
                value = Substrate.speedMs.toFloat(),
                onValueChange = { Substrate.speedMs = it.toLong() },
                valueRange = 0f..2000f
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier.clip(RoundedCornerShape(4.dp)).background(ACCENT)
                        .clickable { Substrate.showSettingsDialog = false }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) { Text("Close", color = Color.White, fontSize = 11.sp) }
            }
        }
    }
}

@Composable
fun AboutDialog() {
    if (!Substrate.showAboutDialog) return
    Dialog(onDismissRequest = { Substrate.showAboutDialog = false }) {
        Column(
            Modifier.width(320.dp).background(Color.White, RoundedCornerShape(8.dp)).padding(16.dp)
        ) {
            Text("Simple — true interpreter", color = TEXT, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text("Version ${Substrate.version}", color = TEXT_MUTED, fontSize = 11.sp)
            Text("Language ${Substrate.languageVersion}", color = TEXT_MUTED, fontSize = 11.sp)
            Text("Platform ${Substrate.platform}", color = TEXT_MUTED, fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier.clip(RoundedCornerShape(4.dp)).background(ACCENT)
                    .clickable { Substrate.showAboutDialog = false }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) { Text("Close", color = Color.White, fontSize = 11.sp) }
        }
    }
}

@Composable
fun DeleteFileDialog(app: MainScreenApp) {
    if (!Substrate.showDeleteFileDialog) return
    val target = Substrate.pendingDeleteFile ?: return
    Dialog(onDismissRequest = {
        Substrate.showDeleteFileDialog = false
        Substrate.pendingDeleteFile = null
    }) {
        Column(
            Modifier.width(320.dp).background(Color.White, RoundedCornerShape(8.dp)).padding(16.dp)
        ) {
            Text("Delete '$target'?", color = TEXT, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text("This will also remove the agent and visualization.", color = TEXT_MUTED, fontSize = 11.sp)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier.clip(RoundedCornerShape(4.dp)).background(ERROR)
                        .clickable {
                            MainScreenSystem(app).deleteFileCascade(target)
                            Substrate.showDeleteFileDialog = false
                            Substrate.pendingDeleteFile = null
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) { Text("Delete", color = Color.White, fontSize = 11.sp) }
                Box(
                    Modifier.clip(RoundedCornerShape(4.dp)).background(BG_SOFT)
                        .clickable {
                            Substrate.showDeleteFileDialog = false
                            Substrate.pendingDeleteFile = null
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) { Text("Cancel", color = TEXT, fontSize = 11.sp) }
            }
        }
    }
}

@Composable
fun TabsPicker(app: MainScreenApp) {
    if (!Substrate.showTabsPicker) return
    Dialog(onDismissRequest = { Substrate.showTabsPicker = false }) {
        Column(
            Modifier.width(520.dp).background(Color.White, RoundedCornerShape(8.dp)).padding(16.dp)
        ) {
            Text("Tabs", color = TEXT, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            LazyColumn {
                items(TabsModel.tabs) { tab ->
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            val idx = TabsModel.tabs.indexOf(tab)
                            if (idx >= 0) app.setActiveTab(idx)
                            Substrate.showTabsPicker = false
                        }.padding(vertical = 4.dp)
                    ) {
                        Text(tab.fileName, color = TEXT, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(app: MainScreenApp) {
    if (!Substrate.showSearch) return
    Row(
        Modifier.fillMaxWidth().background(BG_SOFT).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BasicTextField(
            value = Substrate.searchQuery,
            onValueChange = {
                Substrate.searchQuery = it
                Substrate.searchMatches = emptyList()
            },
            modifier = Modifier.weight(1f).background(Color.White, RoundedCornerShape(4.dp)).padding(6.dp),
            textStyle = androidx.compose.ui.text.TextStyle(color = TEXT, fontSize = 11.sp)
        )
        Spacer(Modifier.width(4.dp))
        Box(
            Modifier.clip(RoundedCornerShape(4.dp)).background(ACCENT).clickable {
                Substrate.showSearch = false
            }.padding(horizontal = 8.dp, vertical = 6.dp)
        ) { Text("✕", color = Color.White, fontSize = 11.sp) }
    }
}

@Composable
fun AskDialog(app: MainScreenApp) {
    val requests = AskModel.queue
    if (requests.isEmpty()) return
    val req = requests.first()
    var text by remember(req.id) { mutableStateOf(req.default) }
    Dialog(onDismissRequest = { AskModel.cancel(req.id) }) {
        Column(
            Modifier.width(400.dp).background(Color.White, RoundedCornerShape(8.dp)).padding(16.dp)
        ) {
            Text(req.question, color = TEXT, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().background(BG_SOFT, RoundedCornerShape(4.dp)).padding(8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(color = TEXT, fontSize = 13.sp, fontFamily = FontFamily.Monospace),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier.clip(RoundedCornerShape(4.dp)).background(ACCENT)
                        .clickable { AskModel.answer(req.id, text) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) { Text("OK", color = Color.White, fontSize = 11.sp) }
                Box(
                    Modifier.clip(RoundedCornerShape(4.dp)).background(BG_SOFT)
                        .clickable { AskModel.cancel(req.id) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) { Text("Cancel", color = TEXT, fontSize = 11.sp) }
            }
        }
    }
}

@Composable
fun TemplatePickerDialog(app: MainScreenApp) {
    if (!Substrate.showTemplatePicker) return
    Dialog(onDismissRequest = {
        Substrate.showTemplatePicker = false
    }) {
        Column(
            Modifier.width(520.dp).background(Color.White, RoundedCornerShape(8.dp)).padding(16.dp)
        ) {
            Text("Добро пожаловать в True!", color = TEXT, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text("Выбери шаблон для старта:", color = TEXT_MUTED, fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (t in Substrate.templates) {
                    Row(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(BG_SOFT)
                            .clickable {
                                MainScreenSystem(app).loadTemplate(t.id)
                                Substrate.showTemplatePicker = false
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(t.title, color = TEXT, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text(t.description, color = TEXT_MUTED, fontSize = 10.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier.clip(RoundedCornerShape(4.dp)).background(BG_SOFT)
                    .clickable { Substrate.showTemplatePicker = false }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) { Text("Пропустить", color = TEXT, fontSize = 11.sp) }
        }
    }
}

@Composable
private fun Dialog(onDismissRequest: () -> Unit, content: @Composable () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismissRequest) {
        content()
    }
}

@Composable
private fun <T> androidx.compose.runtime.snapshots.SnapshotStateList<T>.collectAsStateCompat(): State<List<T>> {
    val list = remember { mutableStateOf(this.toList()) }
    LaunchedEffect(this) {
        snapshotFlow { this.toList() }.collect { list.value = it }
    }
    return list
}

@Composable
fun PinnedPanel(app: MainScreenApp) {
    val agent = app.viewingAgent() ?: return
    val pinned = Substrate.pinnedList
    if (pinned.isEmpty()) return
    Column(Modifier.fillMaxWidth().padding(8.dp)) {
        Text("Pinned", color = TEXT, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        for (name in pinned) {
            val value = agent.interpreter.variables[name] ?: ""
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(name, color = TEXT, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                Spacer(Modifier.width(6.dp))
                Text("=", color = TEXT_MUTED, fontSize = 11.sp)
                Spacer(Modifier.width(6.dp))
                Text(value, color = TEXT, fontSize = 11.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
                Spacer(Modifier.weight(1f))
                Box(Modifier.clickable {
                    Substrate.pinnedList.removeAll { it == name }
                    app.systemRef()?.saveSettings()
                }.padding(4.dp)) {
                    Text("✕", color = TEXT_MUTED, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun TimelinePanel(app: MainScreenApp) {
    val cycleFiles = Substrate.fileOrder.filter { name ->
        val tab = TabsModel.byFileName(name)
        tab != null && tab.kind == FileKind.CYCLE
    }
    if (cycleFiles.isEmpty()) return
    Column(Modifier.fillMaxWidth().background(BG_SOFT).padding(8.dp)) {
        Text("Timeline (last 60s)", color = TEXT_MUTED, fontSize = 10.sp)
        Spacer(Modifier.height(4.dp))
        for (fileName in cycleFiles) {
            val agent = AgentsModel.agentFor(fileName)
            val active = agent?.status == Status.RUNNING
            val paused = agent?.status == Status.PAUSED
            Row(Modifier.fillMaxWidth().padding(vertical = 1.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(fileName, color = TEXT, fontSize = 10.sp, fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(120.dp), maxLines = 1)
                Box(Modifier.weight(1f).height(8.dp).background(Color.White, RoundedCornerShape(4.dp))) {
                    val fraction = if (active) 1f else if (paused) 0.5f else 0f
                    if (fraction > 0f) {
                        Box(
                            Modifier.fillMaxWidth(fraction).fillMaxHeight()
                                .background(
                                    if (agent != null) agent.color else ACCENT,
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BackgroundImage(fileName: String) {
    val bg = VisualModel.backgroundFor(fileName) ?: return
    val file = File(bg.path)
    if (!file.exists()) return
    androidx.compose.foundation.Image(
        painter = androidx.compose.ui.res.painterResource(bg.path),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = when (bg.mode) {
            "tile" -> androidx.compose.ui.layout.ContentScale.None
            "stretch" -> androidx.compose.ui.layout.ContentScale.FillBounds
            "center" -> androidx.compose.ui.layout.ContentScale.None
            else -> androidx.compose.ui.layout.ContentScale.Fit
        }
    )
}

@Composable
private fun AgentMenu(agent: AgentState, pos: Offset, onClose: () -> Unit) {
    val app = remember { MainScreenApp() }
    Box(
        Modifier
            .offset(x = pos.x.dp, y = (pos.y - 60).dp)
            .width(200.dp)
            .background(Color.White, RoundedCornerShape(6.dp))
            .border(1.dp, DIVIDER, RoundedCornerShape(6.dp))
    ) {
        Row(
            Modifier.fillMaxWidth().clickable {
                app.setActiveFile(agent.fileName)
                onClose()
            }.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("✎", fontSize = 11.sp, color = TEXT)
            Spacer(Modifier.width(8.dp))
            Text("Перейти к коду агента", fontSize = 11.sp, color = TEXT)
        }
    }
}

@Composable
fun PinnedAddDialog(app: MainScreenApp) {
    if (!Substrate.showPinnedAddDialog) return
    val agent = app.viewingAgent() ?: return
    val names = agent.interpreter.variables.keys.sorted()
    Dialog(onDismissRequest = { Substrate.showPinnedAddDialog = false }) {
        Column(
            Modifier.width(320.dp).background(Color.White, RoundedCornerShape(8.dp)).padding(16.dp)
        ) {
            Text("Pinned containers", color = TEXT, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            LazyColumn(Modifier.heightIn(max = 400.dp)) {
                items(names) { name ->
                    val selected = Substrate.pinnedList.contains(name)
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            if (selected) Substrate.pinnedList.removeAll { it == name }
                            else Substrate.pinnedList.add(name)
                            app.systemRef()?.saveSettings()
                        }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = selected, onCheckedChange = {
                            if (selected) Substrate.pinnedList.removeAll { it == name }
                            else Substrate.pinnedList.add(name)
                            app.systemRef()?.saveSettings()
                        })
                        Text(name, color = TEXT, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier.clip(RoundedCornerShape(4.dp)).background(ACCENT)
                    .clickable { Substrate.showPinnedAddDialog = false }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) { Text("Close", color = Color.White, fontSize = 11.sp) }
        }
    }
}

@Composable
fun RecentProjectsMenu(app: MainScreenApp) {
    if (!Substrate.showRecentProjects) return
    Dialog(onDismissRequest = { Substrate.showRecentProjects = false }) {
        Column(
            Modifier.width(520.dp).background(Color.White, RoundedCornerShape(8.dp)).padding(16.dp)
        ) {
            Text("Recent projects", color = TEXT, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            if (Substrate.recentProjects.isEmpty()) {
                Text("No recent projects", color = TEXT_MUTED, fontSize = 11.sp)
            } else {
                LazyColumn {
                    items(Substrate.recentProjects) { path ->
                        Row(
                            Modifier.fillMaxWidth().clickable {
                                MainScreenSystem(app).openFolder(File(path))
                                Substrate.showRecentProjects = false
                            }.padding(vertical = 4.dp)
                        ) {
                            Text(path, color = TEXT, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier.clip(RoundedCornerShape(4.dp)).background(BG_SOFT)
                    .clickable { Substrate.showRecentProjects = false }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) { Text("Close", color = TEXT, fontSize = 11.sp) }
        }
    }
}

private fun MainScreenApp.systemRef(): MainScreenSystem? {
    return try {
        val field = MainScreenApp::class.java.getDeclaredField("sys")
        field.isAccessible = true
        field.get(this) as? MainScreenSystem
    } catch (_: Exception) { null }
}