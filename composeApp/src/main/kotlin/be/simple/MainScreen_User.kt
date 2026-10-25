package be.simple

import androidx.compose.foundation.HorizontalScrollbar
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import be.`true`.Autocomplete
import be.`true`.LogEntry
import be.`true`.Status
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import kotlinx.coroutines.delay

private val BG = Color(0xFFEFF1F5)
private val TEXT = Color(0xFF2B364D)
private val ACCENT = Color(0xFF4A90E2)
private val MENU_BG = Color(0xFFF5F6F8)
private val MENU_HOVER = Color(0xFFE3E6EC)
private val MENU_BORDER = Color(0xFFD5D9E0)
private val MENU_TEXT = Color(0xFF2B364D)
private val MENU_TEXT_DISABLED = Color(0xFF9AA1AC)
private val MENU_HOVER_ITEM = Color(0xFFE8EBF0)
private val MENU_SELECTED = Color(0xFFDDE4F0)
private val MENU_SEPARATOR = Color(0xFFE1E4E8)
private val ZEBRA = Color(0xFFF0F2F5)
private val LINE_NUM = Color(0xFF9AA1AC)
private val COPIED = Color(0xFFDFF3E3)
private val BTN_BG = Color(0xFFF5F6F8)
private val BTN_BG_DISABLED = Color(0xFFE9ECF1)
private val BTN_FG = Color(0xFF2B364D)
private val BTN_FG_DISABLED = Color(0xFFB0B6C1)
private val PINNED_COLOR = Color(0xFFE1F5FE)
private val CONST_COLOR = Color(0xFFE8D5A8)
private val ERROR_BADGE = Color(0xFFFFCDD2)
private val ERROR_TEXT = Color(0xFFB71C1C)
private val FINISH_COLOR = Color(0xFF2E7D32)
private val STRICT_BADGE = Color(0xFFFFF59D)
private val DESTROY_COLOR = Color(0xFF9C27B0)
private val DESTROY_BG = Color(0xFFF3E5F5)

private val RUNNING_BG = Color(0xFFDFF0D8)
private val PAUSED_BG = Color(0xFFFFF3CD)
private val STOPPED_BG = Color(0xFFE9ECEF)
private val FINISHED_BG = Color(0xFFD1ECF1)

private val CHIP_MIN_WIDTH = 60.dp
private val CHIP_PADDING_H = 12.dp
private val CLOSE_BTN_WIDTH = 20.dp
private val STRIP_WIDTH = 28.dp

private val VALUES_MIN_WIDTH = 200.dp
private val VALUES_MAX_WIDTH = 480.dp
private val COLUMN_MIN_WIDTH = 320.dp
private val COLUMN_MAX_WIDTH = 480.dp
private val COLUMN_GUTTER = 40.dp

private fun logColor(type: String): Color = when {
    type == "error" -> Color(0xFFE55451)
    type == "jump" -> Color(0xFF9C27B0)
    type == "command" -> Color(0xFF2196F3)
    type == "log" -> Color(0xFFFF9800)
    type == "string" -> Color(0xFF4CAF50)
    type == "destroy" -> DESTROY_COLOR
    type.startsWith("agent:") -> TEXT
    else -> Color(0xFF9AA1AC)
}

private fun agentStateColor(status: Status): Color = when (status) {
    Status.RUNNING -> RUNNING_BG
    Status.PAUSED -> PAUSED_BG
    Status.STOPPED -> STOPPED_BG
    Status.FINISHED -> FINISHED_BG
}

private fun agentStateLabel(status: Status): String = when (status) {
    Status.RUNNING -> "▶ RUNNING"
    Status.PAUSED -> "⏸ PAUSED"
    Status.STOPPED -> "⏹ STOPPED"
    Status.FINISHED -> "✓ FINISHED"
}

private fun agentStateFg(status: Status): Color = when (status) {
    Status.RUNNING -> Color(0xFF2E7D32)
    Status.PAUSED -> Color(0xFF6D4C00)
    Status.STOPPED -> Color(0xFF6C757D)
    Status.FINISHED -> Color(0xFF0C5460)
}

@Composable
fun App() {
    val app = remember { MainScreenApp() }
    val sys = remember { MainScreenSystem(app) }
    val density = LocalDensity.current.density
    app.attachSystem(sys)

    LaunchedEffect(Unit) {
        sys.loadLastOrMain()
        sys.startAutoSave()
        app.refreshVariables()
        if (!Substrate.welcomeShown) Substrate.showWelcome = true
    }

    LaunchedEffect(Substrate.copiedValueText) {
        if (Substrate.copiedValueText != null) { delay(1200); Substrate.copiedValueText = null }
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(BG).onPreviewKeyEvent { ev -> handleHotkey(ev, app) }) {
        val phi = 1.618f
        val total = maxWidth
        val logH = maxHeight * 0.382f
        val valuesW = Substrate.valuesWidthOverride ?: (total / (phi + 1)).coerceIn(VALUES_MIN_WIDTH, VALUES_MAX_WIDTH)

        Column(Modifier.fillMaxSize()) {
            MenuBar(app, sys)
            Row(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp, vertical = 8.dp)) {
                if (Substrate.valuesCollapsed) {
                    Box(Modifier.fillMaxHeight(), contentAlignment = Alignment.CenterStart) { ValuesStrip() }
                } else {
                    Box(Modifier.width(valuesW).fillMaxHeight()) { ValuesPanel(app) }
                }
                if (!Substrate.valuesCollapsed) {
                    DragSplitter(vertical = true) { delta ->
                        val current = Substrate.valuesWidthOverride ?: valuesW
                        val next = (current + (delta / density).dp).coerceIn(VALUES_MIN_WIDTH, VALUES_MAX_WIDTH)
                        Substrate.valuesWidthOverride = next
                    }
                }
                Column(Modifier.weight(phi).fillMaxHeight()) {
                    CyclesArea(app, sys, Modifier.fillMaxWidth().fillMaxHeight())
                }
            }
            HorizontalSplitter { }
            Box(Modifier.fillMaxWidth().height(logH).padding(horizontal = 12.dp, vertical = 8.dp)) {
                LogPanel(Modifier.fillMaxSize())
            }
        }
    }

    if (Substrate.showWelcome) WelcomeDialog(app, sys, onDismiss = { Substrate.showWelcome = false })
    if (Substrate.showTutorial) TutorialDialog(app, sys, onDismiss = { Substrate.showTutorial = false })
    if (Substrate.showSettingsDialog) SettingsDialog(app, sys, onDismiss = { Substrate.showSettingsDialog = false })
    if (Substrate.showSaveStartDialog) SaveStartDialog(
        onDismiss = { Substrate.showSaveStartDialog = false },
        onConfirm = { sys.saveAsStart(); Substrate.showSaveStartDialog = false })
    if (Substrate.showAboutDialog) AboutDialog(onDismiss = { Substrate.showAboutDialog = false })
    if (Substrate.showTabsPicker) TabsPicker(app, sys, onDismiss = { Substrate.showTabsPicker = false })
    if (Substrate.showSearch) SearchDialog(app, onDismiss = { Substrate.showSearch = false })
    if (Substrate.showDocDialog) DocDialog(onDismiss = { Substrate.showDocDialog = false })
    if (Substrate.showSamplesDialog) SamplesDialog(app, onDismiss = { Substrate.showSamplesDialog = false })
    if (Substrate.showTemplatesDialog) TemplatesDialog(onDismiss = { Substrate.showTemplatesDialog = false })
    if (Substrate.showCopyToDialog) CopyToDialog(app, sys, onDismiss = { Substrate.showCopyToDialog = false })
    if (Substrate.showMoveToDialog) MoveToDialog(app, sys, onDismiss = { Substrate.showMoveToDialog = false })
    if (Substrate.showDeleteAgentDialog) DeleteAgentDialog(app, sys, onDismiss = { Substrate.showDeleteAgentDialog = false })
    if (Substrate.showDeleteFileDialog) DeleteFileDialog(app, sys, onDismiss = { Substrate.showDeleteFileDialog = false })

    if (Substrate.pendingNavigateFile != null) {
        val target = Substrate.pendingNavigateFile
        val occupying = target?.let { AgentsModel.agentFor(it) }
        NavigateBehaviorDialog(
            occupiedFileName = target ?: "",
            occupyingAgent = occupying,
            onDismiss = { Substrate.pendingNavigateFile = null },
            onConfirm = { behavior ->
                Substrate.navigateBehavior = behavior
                val t = Substrate.pendingNavigateFile
                if (t != null) {
                    when (behavior) {
                        NavigateBehavior.EVICT -> {
                            occupying?.let { sys.pauseAgent(it.id) }
                            app.setActiveFile(t)
                        }
                        NavigateBehavior.WAIT -> {
                            Substrate.log("Waiting for ${occupying?.name ?: "agent"} on $t…")
                            app.setActiveFile(t)
                        }
                        NavigateBehavior.STAY -> { }
                        NavigateBehavior.ERROR -> {
                            Substrate.log("Cannot open $t: occupied by ${occupying?.name ?: "agent"}")
                        }
                    }
                }
                Substrate.pendingNavigateFile = null
            }
        )
    }
}

private fun handleHotkey(ev: KeyEvent, app: MainScreenApp): Boolean {
    if (ev.type != KeyEventType.KeyDown) return false
    return when {
        ev.isCtrlPressed && ev.key == Key.Z -> { if (ev.isShiftPressed) app.redo() else app.undo(); true }
        ev.isCtrlPressed && ev.key == Key.F -> { Substrate.showSearch = true; true }
        ev.isCtrlPressed && ev.key == Key.H -> { Substrate.showSearch = true; true }
        ev.isCtrlPressed && ev.key == Key.R -> { app.startPlay(); true }
        else -> false
    }
}

@Composable
private fun DragSplitter(vertical: Boolean = true, onDrag: (Float) -> Unit) {
    val current by rememberUpdatedState(onDrag)
    if (vertical) {
        Box(Modifier.width(6.dp).fillMaxHeight().background(Color.Transparent)
            .pointerInput(Unit) { detectDragGestures { change, dragAmount -> change.consume(); current(dragAmount.x) } },
            contentAlignment = Alignment.Center
        ) { Box(Modifier.width(2.dp).fillMaxHeight().background(MENU_BORDER)) }
    } else {
        Box(Modifier.fillMaxWidth().height(6.dp).background(Color.Transparent)
            .pointerInput(Unit) { detectDragGestures { change, dragAmount -> change.consume(); current(dragAmount.y) } },
            contentAlignment = Alignment.Center
        ) { Box(Modifier.fillMaxWidth().height(2.dp).background(MENU_BORDER)) }
    }
}

@Composable
private fun HorizontalSplitter(onDrag: (Float) -> Unit) = DragSplitter(vertical = false, onDrag = onDrag)

@Composable
private fun MenuBar(app: MainScreenApp, sys: MainScreenSystem) {
    Row(Modifier.fillMaxWidth().height(28.dp).background(MENU_BG).padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically) {
        MenuItem("File") {
            MenuRow("New", shortcut = "Ctrl+N") { sys.newFile() }
            MenuRow("Open…", shortcut = "Ctrl+O") { sys.openFileDialog()?.let { sys.openFile(it) } }
            MenuRow("Open Folder…") { sys.openFolderDialog()?.let { sys.openFolder(it) } }
            MenuSeparator()
            MenuRow("Save", shortcut = "Ctrl+S") { sys.saveCurrent() }
            MenuRow("Save As…", shortcut = "Ctrl+Shift+S") { sys.saveDialog()?.let { sys.saveAs(it) } }
            MenuSeparator()
            MenuLabel("Recent")
            if (Substrate.recentFiles.isEmpty()) MenuRowDisabled("(empty)")
            else Substrate.recentFiles.forEach { name ->
                MenuRow(name) { sys.openFile(File(System.getProperty("user.home"), ".simple/$name")) }
            }
            MenuSeparator()
            MenuRow("Exit") { sys.stopAllAgents(); sys.stopAutoSave() }
        }
        MenuItem("Run") {
            MenuRow("Start All", shortcut = "Ctrl+R") { sys.startAllAgents() }
            MenuRow("Stop All") { sys.stopAllAgents() }
            MenuSeparator()
            MenuRow("Step") { app.step() }
            MenuRow("Step Back") { app.stepBack() }
            MenuSeparator()
            MenuRow("Reset file") { app.reset() }
        }
        MenuItem("Settings") {
            MenuRow("Open Settings…") { Substrate.showSettingsDialog = true }
            MenuRow("Reset Values width") { Substrate.valuesWidthOverride = null }
        }
        MenuItem("About") { MenuRow("About…") { Substrate.showAboutDialog = true } }
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun MenuItem(label: String, content: @Composable ColumnScope.() -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    var anchorBounds by remember { mutableStateOf<Rect?>(null) }
    Box {
        Box(Modifier.height(24.dp)
            .background(if (expanded) MENU_SELECTED else if (hovered) MENU_HOVER else Color.Transparent,
                RoundedCornerShape(4.dp))
            .hoverable(interaction).clickable { expanded = !expanded }
            .padding(horizontal = 10.dp)
            .onGloballyPositioned { anchorBounds = it.boundsInWindow() },
            contentAlignment = Alignment.Center
        ) { Text(label, color = MENU_TEXT, fontSize = 12.sp) }
        if (expanded) {
            Popup(popupPositionProvider = AnchorBelowProvider(anchorBounds),
                onDismissRequest = { expanded = false }, properties = PopupProperties(focusable = true)) {
                Column(Modifier.width(280.dp).background(Color.White, RoundedCornerShape(6.dp))
                    .border(1.dp, MENU_SEPARATOR, RoundedCornerShape(6.dp)).padding(bottom = 2.dp)) { content() }
            }
        }
    }
}

private class AnchorBelowProvider(private val anchorRect: Rect?) : PopupPositionProvider {
    override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize,
                                   layoutDirection: LayoutDirection, popupContentSize: IntSize): IntOffset {
        val r = anchorRect ?: Rect.Zero
        return IntOffset(r.left.toInt(), r.bottom.toInt())
    }
}

@Composable
private fun MenuRow(label: String, shortcut: String? = null, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    Row(Modifier.fillMaxWidth().height(28.dp)
        .background(if (hovered) MENU_HOVER_ITEM else Color.Transparent)
        .hoverable(interaction).clickable { onClick() }.padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = MENU_TEXT, fontSize = 13.sp, maxLines = 1,
            overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        if (shortcut != null) { Spacer(Modifier.width(16.dp)); Text(shortcut, color = MENU_TEXT_DISABLED, fontSize = 12.sp) }
    }
}

@Composable
private fun MenuRowDisabled(label: String) {
    Row(Modifier.fillMaxWidth().height(28.dp).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = MENU_TEXT_DISABLED, fontSize = 13.sp)
    }
}

@Composable
private fun MenuLabel(label: String) {
    Box(Modifier.fillMaxWidth().height(22.dp).padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart) {
        Text(label, color = MENU_TEXT_DISABLED, fontSize = 11.sp)
    }
}

@Composable
private fun MenuSeparator() {
    Spacer(Modifier.height(5.dp)); Box(Modifier.fillMaxWidth().height(1.dp).background(MENU_SEPARATOR)); Spacer(Modifier.height(5.dp))
}

@Composable
private fun CyclesArea(app: MainScreenApp, sys: MainScreenSystem, modifier: Modifier) {
    Column(modifier.background(Color.White, RoundedCornerShape(12.dp)).padding(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Cycles", color = TEXT, fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            ActionIconButton("▶", enabled = AgentsModel.agents.isNotEmpty()) { sys.startAllAgents() }
            ActionIconButton("⏹", enabled = AgentsModel.agents.isNotEmpty()) { sys.stopAllAgents() }
            Spacer(Modifier.weight(1f))
            if (Substrate.strictMode) {
                Box(Modifier.background(STRICT_BADGE, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text("STRICT", color = Color(0xFF6D4C00), fontSize = 10.sp)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        FileChipsBar(app, sys)
        Spacer(Modifier.height(6.dp))

        if (TabsModel.tabs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("(no files)", color = TEXT.copy(alpha = 0.5f))
            }
            return@Column
        }

        val expanded = Substrate.expandedFileName
        if (expanded != null) {
            val tab = TabsModel.byFileName(expanded) ?: TabsModel.tabs.first()
            FileColumn(app, sys, tab, expanded = true, Modifier.fillMaxSize())
        } else {
            CyclesFeed(app, sys)
        }
    }
}

@Composable
private fun CyclesFeed(app: MainScreenApp, sys: MainScreenSystem) {
    val scroll = rememberScrollState()
    val tabs = TabsModel.tabs.toList()
    Box(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxSize().horizontalScroll(scroll),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            tabs.forEach { tab ->
                val measured = measureMaxLineWidth(tab.code)
                val colW = measured.coerceIn(COLUMN_MIN_WIDTH, COLUMN_MAX_WIDTH)
                Box(Modifier.width(colW).fillMaxHeight()) {
                    FileColumn(app, sys, tab, expanded = false, Modifier.fillMaxSize())
                }
            }
        }
        if (tabs.size > 1) {
            HorizontalScrollbar(
                adapter = rememberScrollbarAdapter(scroll),
                modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().height(6.dp)
            )
        }
    }
}

@Composable
private fun FileColumn(
    app: MainScreenApp,
    sys: MainScreenSystem,
    tab: TabState,
    expanded: Boolean,
    modifier: Modifier
) {
    val agent = AgentsModel.agentFor(tab.fileName)
    val isActive = Substrate.activeFileName == tab.fileName
    val border = if (isActive) Modifier.border(2.dp, ACCENT, RoundedCornerShape(8.dp)) else Modifier
    Column(
        modifier
            .background(Color(0xFFF7F8FA), RoundedCornerShape(8.dp))
            .then(border)
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (agent != null) {
                Box(Modifier.size(10.dp).background(agent.color, RoundedCornerShape(2.dp)))
                Spacer(Modifier.width(6.dp))
                Text(agent.name, color = TEXT, fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false))
            } else {
                ActionIconButton("▶") { app.createAgentFor(tab.fileName) }
                Spacer(Modifier.width(6.dp))
                Text("Create agent", color = ACCENT, fontSize = 11.sp)
                Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.weight(1f))
            if (agent != null) {
                ActionIconButton("▶", enabled = agent.status != Status.RUNNING) { sys.startAgent(agent.id) }
                ActionIconButton("⏸", enabled = agent.status == Status.RUNNING) { sys.pauseAgent(agent.id) }
                ActionIconButton("⏹", enabled = agent.status != Status.STOPPED) { sys.stopAgent(agent.id) }
                ActionIconButton("⏭", enabled = agent.status != Status.RUNNING) { sys.stepAgent(agent.id) }
                ActionIconButton("⏮", enabled = agent.status != Status.RUNNING) { sys.stepBackAgent(agent.id) }
            }
            ResetButton(app, sys, tab.fileName)
            ColumnMenuButton(app, sys, tab)
            ActionIconButton(if (expanded) "⤡" else "⛶") {
                Substrate.expandedFileName = if (expanded) null else tab.fileName
                Substrate.activeFileName = tab.fileName
                Substrate.viewingAgentId = agent?.id
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.height(22.dp).background(MENU_BG, RoundedCornerShape(6.dp))
                    .clickable { app.setActiveFile(tab.fileName) }
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(tab.fileName, color = TEXT, fontSize = 11.sp, fontFamily = FontFamily.Monospace,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (agent != null) {
                Spacer(Modifier.width(6.dp))
                Text("step ${agent.currentStep}", color = TEXT.copy(alpha = 0.6f), fontSize = 10.sp)
                Spacer(Modifier.width(4.dp))
                Text(agentStateLabel(agent.status), color = agentStateFg(agent.status), fontSize = 10.sp)
            }
        }
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().weight(1f).background(Color.White, RoundedCornerShape(6.dp)).padding(4.dp)) {
            StripedCodeField(app, tab, agent)
        }
    }
}

@Composable
private fun ColumnMenuButton(app: MainScreenApp, sys: MainScreenSystem, tab: TabState) {
    var menuOpen by remember { mutableStateOf(false) }
    var anchor by remember { mutableStateOf<Rect?>(null) }
    val agent = AgentsModel.agentFor(tab.fileName)
    Box {
        Box(
            Modifier.size(24.dp).background(BTN_BG, RoundedCornerShape(6.dp))
                .clickable { menuOpen = !menuOpen }
                .onGloballyPositioned { anchor = it.boundsInWindow() },
            contentAlignment = Alignment.Center
        ) { Text("⋯", color = BTN_FG, fontSize = 14.sp) }
        if (menuOpen) {
            Popup(popupPositionProvider = AnchorBelowProvider(anchor),
                onDismissRequest = { menuOpen = false }, properties = PopupProperties(focusable = true)) {
                Column(Modifier.width(240.dp).background(Color.White, RoundedCornerShape(6.dp))
                    .border(1.dp, MENU_SEPARATOR, RoundedCornerShape(6.dp)).padding(bottom = 2.dp)) {
                    if (agent != null) {
                        MenuRow("Templates…") { menuOpen = false; Substrate.showTemplatesDialog = true }
                        MenuRow("Copy to…") { menuOpen = false; Substrate.showCopyToDialog = true }
                        MenuRow("Move to…") { menuOpen = false; Substrate.showMoveToDialog = true }
                        MenuSeparator()
                        MenuRow("Delete agent") {
                            menuOpen = false
                            Substrate.pendingDeleteAgentId = agent.id
                            Substrate.showDeleteAgentDialog = true
                        }
                    } else {
                        MenuRow("Create agent") { menuOpen = false; app.createAgentFor(tab.fileName) }
                    }
                    MenuSeparator()
                    MenuRow("Delete file") {
                        menuOpen = false
                        Substrate.pendingDeleteFile = tab.fileName
                        Substrate.showDeleteFileDialog = true
                    }
                }
            }
        }
    }
}

@Composable
private fun FileChipsBar(app: MainScreenApp, sys: MainScreenSystem) {
    val scroll = rememberScrollState()
    val tabs = TabsModel.tabs.toList()
    Row(Modifier.fillMaxWidth().height(30.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).fillMaxHeight()) {
            Row(Modifier.fillMaxHeight().horizontalScroll(scroll),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                tabs.forEach { tab ->
                    FileChip(tab, TabsModel.active()?.fileName == tab.fileName,
                        onSelect = { app.setActiveFile(tab.fileName) },
                        onClose = {
                            val idx = TabsModel.tabs.indexOfFirst { it.fileName == tab.fileName }
                            if (idx >= 0) app.closeTab(idx)
                        })
                }
                Box(Modifier.size(28.dp).background(MENU_BG, RoundedCornerShape(6.dp))
                    .clickable { Substrate.showTabsPicker = true },
                    contentAlignment = Alignment.Center
                ) { Text("+", color = TEXT, fontSize = 16.sp) }
            }
            if (tabs.size > 1) {
                HorizontalScrollbar(
                    adapter = rememberScrollbarAdapter(scroll),
                    modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().height(6.dp)
                )
            }
        }
    }
}

@Composable
private fun FileChip(tab: TabState, active: Boolean, onSelect: () -> Unit, onClose: () -> Unit) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val agent = AgentsModel.agentFor(tab.fileName)
    val text = tab.fileName
    val style = TextStyle(fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    val measuredWidth = with(density) { measurer.measure(text, style).size.width.toDp() }
    val dotW = if (agent != null) 10.dp else 0.dp
    val chipWidth = (measuredWidth + dotW + CHIP_PADDING_H * 2 + CLOSE_BTN_WIDTH).coerceAtLeast(CHIP_MIN_WIDTH)

    val bg = when {
        active -> ACCENT
        agent?.status == Status.RUNNING -> FINISH_COLOR
        else -> MENU_BG
    }
    val fg = if (active || agent?.status == Status.RUNNING) Color.White else TEXT

    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    Row(
        Modifier.width(chipWidth).height(26.dp)
            .background(if (hovered && !active) MENU_HOVER else bg, RoundedCornerShape(6.dp))
            .border(1.dp, if (active) bg else MENU_BORDER, RoundedCornerShape(6.dp))
            .hoverable(interaction)
            .clickable { onSelect() }
            .padding(start = 6.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (agent != null) {
            val dotAlpha = if (agent.status == Status.STOPPED) 0.5f else 1f
            Box(Modifier.size(8.dp).background(agent.color.copy(alpha = dotAlpha), CircleShape))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, color = fg, fontSize = 11.sp, fontFamily = FontFamily.Monospace,
            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
        Spacer(Modifier.width(2.dp))
        Box(Modifier.size(CLOSE_BTN_WIDTH).clickable { onClose() }, contentAlignment = Alignment.Center) {
            Text("×", color = fg.copy(alpha = 0.75f), fontSize = 12.sp)
        }
    }
}

@Composable
private fun ResetButton(app: MainScreenApp, sys: MainScreenSystem, fileName: String) {
    var menuOpen by remember { mutableStateOf(false) }
    var anchor by remember { mutableStateOf<Rect?>(null) }
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    Box {
        Box(
            Modifier.size(24.dp).background(if (hovered) MENU_HOVER else BTN_BG, RoundedCornerShape(6.dp))
                .hoverable(interaction)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Press) {
                                if (event.buttons.isSecondaryPressed) menuOpen = true
                                else app.resetFile(fileName)
                            }
                        }
                    }
                }
                .onGloballyPositioned { anchor = it.boundsInWindow() },
            contentAlignment = Alignment.Center
        ) { Text("⟲", color = BTN_FG, fontSize = 12.sp) }
        if (menuOpen) {
            Popup(popupPositionProvider = AnchorBelowProvider(anchor),
                onDismissRequest = { menuOpen = false }, properties = PopupProperties(focusable = true)) {
                Column(Modifier.width(280.dp).background(Color.White, RoundedCornerShape(6.dp))
                    .border(1.dp, MENU_SEPARATOR, RoundedCornerShape(6.dp)).padding(bottom = 2.dp)) {
                    MenuRow("Save current as start state…") {
                        menuOpen = false
                        Substrate.showSaveStartDialog = true
                    }
                }
            }
        }
    }
}

@Composable
private fun StripedCodeField(app: MainScreenApp, tab: TabState, agent: AgentState?) {
    var tfv by remember(tab.id) { mutableStateOf(TextFieldValue(tab.code, TextRange(tab.cursorPosition))) }
    var showSuggest by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf(emptyList<String>()) }
    var suggestionAnchor by remember { mutableStateOf<Rect?>(null) }
    LaunchedEffect(tab.code) {
        if (tfv.text != tab.code) {
            val pos = tab.cursorPosition.coerceIn(0, tab.code.length)
            tfv = tfv.copy(text = tab.code, selection = TextRange(pos))
        }
    }
    val currentLine = if (agent != null && agent.id == Substrate.viewingAgentId) agent.currentStep else -1
    val statusColor = if (agent != null && agent.id == Substrate.viewingAgentId) agentStateColor(agent.status) else Color.Transparent
    val agentColor = if (agent != null && agent.id == Substrate.viewingAgentId) agent.color else null
    val statusLabel = if (agent != null && agent.id == Substrate.viewingAgentId) agentStateLabel(agent.status) else null
    val transformation = remember(currentLine, statusColor, agentColor, statusLabel) {
        LineHighlighter(currentLine, statusColor, agentColor, statusLabel)
    }
    Box {
        BasicTextField(
            value = tfv,
            onValueChange = { nv -> tfv = nv; app.updateCode(nv.text); app.updateCursor(nv.selection.start) },
            modifier = Modifier.fillMaxSize()
                .onGloballyPositioned { suggestionAnchor = it.boundsInWindow() }
                .onPreviewKeyEvent { ev ->
                    if (ev.type == KeyEventType.KeyDown && ev.key == Key.Tab) {
                        val pos = tfv.selection.start
                        val newText = tfv.text.substring(0, pos) + "  " + tfv.text.substring(pos)
                        tfv = tfv.copy(text = newText, selection = TextRange(pos + 2))
                        app.updateCode(newText); app.updateCursor(pos + 2); return@onPreviewKeyEvent true
                    }
                    if (ev.type == KeyEventType.KeyDown && ev.isCtrlPressed && ev.key == Key.Spacebar) {
                        val pos = tfv.selection.start
                        val prefix = tfv.text.substring(0, pos).takeLastWhile { !it.isWhitespace() }
                        val list = Autocomplete.suggestions(prefix)
                        if (list.isNotEmpty()) { suggestions = list; showSuggest = true; return@onPreviewKeyEvent true }
                    }
                    if (showSuggest && ev.type == KeyEventType.KeyDown && ev.key == Key.Escape) {
                        showSuggest = false; return@onPreviewKeyEvent true
                    }
                    if (showSuggest && ev.type == KeyEventType.KeyDown && ev.key == Key.Enter) {
                        val pick = suggestions.firstOrNull() ?: return@onPreviewKeyEvent false
                        val pos = tfv.selection.start
                        val prefix = tfv.text.substring(0, pos).takeLastWhile { !it.isWhitespace() }
                        val newText = tfv.text.substring(0, pos - prefix.length) + pick + tfv.text.substring(pos)
                        tfv = tfv.copy(text = newText, selection = TextRange(pos - prefix.length + pick.length))
                        app.updateCode(newText); app.updateCursor(pos - prefix.length + pick.length)
                        showSuggest = false; return@onPreviewKeyEvent true
                    }
                    false
                },
            textStyle = TextStyle(color = TEXT, fontFamily = FontFamily.Monospace, fontSize = 12.sp),
            visualTransformation = transformation
        )
        if (showSuggest) {
            Popup(popupPositionProvider = AnchorBelowProvider(suggestionAnchor),
                onDismissRequest = { showSuggest = false }, properties = PopupProperties(focusable = true)) {
                Column(Modifier.width(240.dp).background(Color.White, RoundedCornerShape(6.dp))
                    .border(1.dp, MENU_SEPARATOR, RoundedCornerShape(6.dp)).padding(4.dp)) {
                    suggestions.take(8).forEach { s ->
                        MenuRow(s) {
                            val pos = tfv.selection.start
                            val prefix = tfv.text.substring(0, pos).takeLastWhile { !it.isWhitespace() }
                            val newText = tfv.text.substring(0, pos - prefix.length) + s + tfv.text.substring(pos)
                            tfv = tfv.copy(text = newText, selection = TextRange(pos - prefix.length + s.length))
                            app.updateCode(newText); app.updateCursor(pos - prefix.length + s.length)
                            showSuggest = false
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun measureMaxLineWidth(code: String): Dp {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val style = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace)
    return remember(code) {
        val maxLine = code.lines().maxByOrNull { it.length } ?: ""
        val w = with(density) { measurer.measure(maxLine, style).size.width.toDp() }
        (w + COLUMN_GUTTER).coerceIn(COLUMN_MIN_WIDTH, COLUMN_MAX_WIDTH)
    }
}

private class LineHighlighter(
    private val currentLine: Int,
    private val statusColor: Color,
    private val agentColor: Color?,
    private val statusLabel: String?
) : VisualTransformation {

    private var lastCode: String? = null
    private var lastLine: Int = -1
    private var lastStatusColor: Color? = null
    private var lastAgentColor: Color? = null
    private var lastStatusLabel: String? = null
    private var lastErrors: Set<Int> = emptySet()
    private var cached: AnnotatedString? = null
    private var cachedMapping: OffsetMapping? = null

    override fun filter(text: AnnotatedString): TransformedText {
        val code = text.text
        val errorLines = Substrate.errorLines

        if (code == lastCode && currentLine == lastLine && errorLines == lastErrors &&
            statusColor == lastStatusColor && agentColor == lastAgentColor && statusLabel == lastStatusLabel &&
            cached != null && cachedMapping != null) {
            return TransformedText(cached!!, cachedMapping!!)
        }

        val lines = code.split("\n")
        val width = 3
        val prefixLen = width + 3

        val annotated = buildAnnotatedString {
            lines.forEachIndexed { idx, line ->
                val numStart = length
                append((idx + 1).toString().padStart(width, ' '))
                val numEnd = length
                append(" │ ")
                val textStart = length
                append(line)
                val textEnd = length
                addStyle(SpanStyle(color = if (idx + 1 == currentLine) ACCENT else LINE_NUM), numStart, numEnd)
                if (idx + 1 == currentLine) addStyle(SpanStyle(fontWeight = FontWeight.Bold), numStart, numEnd)

                val bg = when {
                    idx + 1 == currentLine && statusColor != Color.Transparent -> statusColor
                    idx % 2 == 1 -> ZEBRA
                    else -> Color.Transparent
                }
                if (bg != Color.Transparent) addStyle(SpanStyle(background = bg), numStart, textEnd)

                if (idx + 1 in errorLines) {
                    addStyle(SpanStyle(textDecoration = TextDecoration.Underline, color = ERROR_TEXT), numStart, textEnd)
                }

                val destroyMatch = Regex("->\\s*nothing").findAll(line)
                for (m in destroyMatch) {
                    val from = textStart + m.range.first
                    val to = textStart + m.range.last + 1
                    if (from >= 0 && to <= length) {
                        addStyle(SpanStyle(background = DESTROY_BG, color = DESTROY_COLOR, textDecoration = TextDecoration.Underline), from, to)
                    }
                }

                if (idx < lines.size - 1) append('\n')
            }
        }

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                var acc = 0; var lineIdx = 0
                for ((i, line) in lines.withIndex()) {
                    if (offset <= acc + line.length) { lineIdx = i; break }
                    acc += line.length + 1
                }
                return offset + prefixLen * (lineIdx + 1)
            }
            override fun transformedToOriginal(offset: Int): Int {
                var acc = 0; var lineIdx = 0
                for ((i, line) in lines.withIndex()) {
                    val lineStartT = acc + prefixLen * i
                    val lineEndT = lineStartT + prefixLen + line.length
                    if (offset <= lineEndT) { lineIdx = i; break }
                    acc += line.length + 1
                }
                return (offset - prefixLen * (lineIdx + 1)).coerceIn(0, code.length)
            }
        }

        lastCode = code; lastLine = currentLine; lastErrors = errorLines
        lastStatusColor = statusColor; lastAgentColor = agentColor; lastStatusLabel = statusLabel
        cached = annotated; cachedMapping = mapping
        return TransformedText(annotated, mapping)
    }
}

@Composable
private fun ActionIconButton(glyph: String, enabled: Boolean = true, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val bg = when { !enabled -> BTN_BG_DISABLED; hovered -> MENU_HOVER; else -> BTN_BG }
    val fg = if (enabled) BTN_FG else BTN_FG_DISABLED
    Box(Modifier.size(24.dp).background(bg, RoundedCornerShape(6.dp))
        .hoverable(interaction).clickable(enabled = enabled) { onClick() }, contentAlignment = Alignment.Center) {
        Text(glyph, color = fg, fontSize = 12.sp)
    }
}

@Composable
private fun SmallIconButton(glyph: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    Box(Modifier.size(24.dp).background(if (hovered) MENU_HOVER else BTN_BG, RoundedCornerShape(6.dp))
        .hoverable(interaction).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text(glyph, color = BTN_FG, fontSize = 12.sp)
    }
}

@Composable
private fun ValuesPanel(app: MainScreenApp) {
    val agent = app.viewingAgent()
    Column(Modifier.fillMaxSize().background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Values", color = TEXT, fontSize = 16.sp)
            Spacer(Modifier.weight(1f))
            SmallIconButton("📋") { copyToClipboard(valuesAsText(agent)) }
            Spacer(Modifier.width(4.dp))
            SmallIconButton("‹") { Substrate.valuesCollapsed = true }
        }
        if (Substrate.copiedValueText != null) {
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxWidth().background(COPIED, RoundedCornerShape(6.dp)).padding(6.dp)) {
                Text("Copied: ${Substrate.copiedValueText}", color = TEXT, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        if (agent == null) {
            Text("(no agent on this file)", color = TEXT.copy(alpha = 0.5f), fontSize = 12.sp)
            return@Column
        }
        SelectionContainer {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                val vars = agent.variablesSnapshot.filterKeys { it !in agent.hiddenSnapshot }
                val hidden = agent.variablesSnapshot.filterKeys { it in agent.hiddenSnapshot }
                var rowIndex = 0
                if (vars.isEmpty() && hidden.isEmpty() && agent.resultsSnapshot.isEmpty()) {
                    Text("(no variables)", color = TEXT.copy(alpha = 0.5f))
                }
                vars.forEach { (k, v) ->
                    val isConst = k in agent.constantsSnapshot
                    ValueRow(k, v, isConst, rowIndex % 2 == 1, onClick = {
                        copyToClipboard("$k: $v"); Substrate.showCopied("$k: $v")
                    }, onDelete = { app.deleteVariable(k) })
                    rowIndex++
                }
                if (hidden.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp)); Text("Hidden:", color = TEXT, fontSize = 12.sp)
                    hidden.forEach { (k, v) ->
                        ValueRow(k, v, false, rowIndex % 2 == 1, muted = true, onClick = {
                            copyToClipboard("$k: $v"); Substrate.showCopied("$k: $v")
                        }, onDelete = { app.deleteVariable(k) })
                        rowIndex++
                    }
                }
                if (agent.resultsSnapshot.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp)); Text("Results:", color = TEXT, fontSize = 12.sp)
                    agent.resultsSnapshot.forEach { (k, v) ->
                        ValueRow(k, v, false, rowIndex % 2 == 1, onClick = {
                            copyToClipboard("$k: $v"); Substrate.showCopied("$k: $v")
                        }, onDelete = null)
                        rowIndex++
                    }
                }
            }
        }
    }
}

@Composable
private fun ValuesStrip() {
    Box(
        Modifier.width(STRIP_WIDTH).fillMaxHeight()
            .background(Color.White, RoundedCornerShape(12.dp))
            .clickable { Substrate.valuesCollapsed = false },
        contentAlignment = Alignment.Center
    ) {
        Text("Values", color = TEXT, fontSize = 12.sp, maxLines = 1,
            modifier = Modifier.rotate(-90f).requiredWidth(120.dp))
    }
}

@Composable
private fun ValueRow(
    name: String, value: String, isConst: Boolean, zebra: Boolean,
    muted: Boolean = false, onClick: () -> Unit, onDelete: (() -> Unit)?
) {
    var hovered by remember { mutableStateOf(false) }
    val bg = when {
        hovered -> MENU_HOVER
        isConst -> CONST_COLOR
        zebra -> ZEBRA
        else -> Color.Transparent
    }
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()
    LaunchedEffect(isHovered) { hovered = isHovered }
    Row(Modifier.fillMaxWidth().background(bg).clickable { onClick() }
        .hoverable(interaction).padding(vertical = 1.dp), verticalAlignment = Alignment.CenterVertically) {
        if (isConst) Text("🔒 ", fontSize = 11.sp)
        Text("$name: ", color = TEXT.copy(alpha = if (muted) 0.6f else 1f),
            fontSize = 12.sp, fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(100.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, color = TEXT.copy(alpha = if (muted) 0.6f else 1f), fontSize = 12.sp, modifier = Modifier.weight(1f))
        if (onDelete != null && hovered) {
            Box(Modifier.size(16.dp).clickable { onDelete() }, contentAlignment = Alignment.Center) {
                Text("×", color = TEXT.copy(alpha = 0.6f), fontSize = 12.sp)
            }
        }
    }
}

private fun valuesAsText(agent: AgentState?): String {
    if (agent == null) return ""
    val sb = StringBuilder()
    agent.variablesSnapshot.forEach { (k, v) -> sb.appendLine("$k: $v") }
    agent.resultsSnapshot.forEach { (k, v) -> sb.appendLine("$k: $v") }
    return sb.toString()
}

@Composable
private fun LogPanel(modifier: Modifier) {
    Column(modifier.background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Log", color = TEXT, fontSize = 16.sp)
            Spacer(Modifier.weight(1f))
            SmallIconButton("🗑") { Substrate.clearLog() }
            Spacer(Modifier.width(6.dp))
            SmallIconButton("📋") { copyToClipboard(Substrate.logLines.joinToString("\n")) }
        }
        Spacer(Modifier.height(6.dp))
        LogFilterBar()
        Spacer(Modifier.height(6.dp))
        LogList(Modifier.fillMaxSize())
    }
}

@Composable
private fun LogFilterBar() {
    val filters = listOf(
        "All" to "All", "Errors" to "error", "Agents" to "agent",
        "Jumps" to "jump", "Commands" to "command", "Steps" to "step",
        "Strings" to "string", "Destroy" to "destroy"
    )
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        filters.forEach { (label, type) -> LogFilterChip(label, type) }
    }
}

@Composable
private fun LogFilterChip(label: String, type: String) {
    val selected = Substrate.logFilter == type || (type == "agent" && Substrate.logFilter.startsWith("agent:"))
    val count = if (type == "All") Substrate.logCount("All")
    else if (type == "agent") Substrate.logEntries.count { it.type.startsWith("agent:") }
    else Substrate.logCount(type)
    val isError = type == "error"
    val isDestroy = type == "destroy"
    val bg = when {
        selected && isError -> ERROR_BADGE
        selected && isDestroy -> DESTROY_BG
        selected -> ACCENT
        isError && count > 0 -> ERROR_BADGE.copy(alpha = 0.5f)
        isDestroy && count > 0 -> DESTROY_BG.copy(alpha = 0.5f)
        else -> MENU_BG
    }
    val fg = when {
        selected && isError -> ERROR_TEXT
        selected && isDestroy -> DESTROY_COLOR
        selected -> Color.White
        isError && count > 0 -> ERROR_TEXT
        isDestroy && count > 0 -> DESTROY_COLOR
        else -> TEXT
    }
    Row(Modifier.height(22.dp).background(bg, RoundedCornerShape(6.dp))
        .clickable { Substrate.logFilter = if (label == "All") "All" else type }
        .padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = fg, fontSize = 10.sp)
        if (count > 0) { Spacer(Modifier.width(3.dp)); Text("($count)", color = fg.copy(alpha = 0.7f), fontSize = 9.sp) }
    }
}

@Composable
private fun LogList(modifier: Modifier) {
    val listState = rememberLazyListState()
    val all = Substrate.logEntries.toList()
    val filter = Substrate.logFilter
    val snapshot = when {
        filter == "All" -> all
        filter == "agent" -> all.filter { it.type.startsWith("agent:") }
        else -> all.filter { it.type == filter }
    }
    Box(modifier) {
        SelectionContainer {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(end = 12.dp)) {
                items(count = snapshot.size, key = { idx -> snapshot.getOrNull(idx) ?: idx }) { idx ->
                    val entry = snapshot.getOrNull(idx) ?: return@items
                    LogRow(entry, idx)
                }
            }
        }
        VerticalScrollbar(adapter = rememberScrollbarAdapter(listState),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight())
    }
}

@Composable
private fun LogRow(entry: LogEntry, idx: Int) {
    val isError = entry.type == "error"
    val isDestroy = entry.type == "destroy"
    val isFinish = entry.text.startsWith("✓")
    val isAgent = entry.type.startsWith("agent:")
    val agentName = if (isAgent) entry.type.removePrefix("agent:") else null
    val agentColor = agentName?.let { name -> AgentsModel.agents.firstOrNull { it.name == name }?.color }

    val bg = when {
        isError -> ERROR_BADGE
        isDestroy -> DESTROY_BG
        idx == 0 -> Color(0xFFE6F4EA)
        idx % 2 == 1 -> ZEBRA
        else -> Color.Transparent
    }
    val fg = when {
        isError -> ERROR_TEXT
        isDestroy -> DESTROY_COLOR
        isFinish -> FINISH_COLOR
        else -> TEXT
    }
    Row(Modifier.fillMaxWidth().background(bg).padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(2.dp).height(16.dp).background(agentColor ?: Color.Transparent, RoundedCornerShape(1.dp)))
        Spacer(Modifier.width(6.dp))
        if (agentName != null) {
            Text("[$agentName] ", color = agentColor ?: TEXT, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
        Text(entry.text, color = fg, fontSize = 12.sp, lineHeight = 16.sp)
    }
}

private fun copyToClipboard(text: String) {
    try { val sel = StringSelection(text); Toolkit.getDefaultToolkit().systemClipboard.setContents(sel, sel) }
    catch (e: Exception) { Substrate.log("Copy failed: ${e.message}") }
}

@Composable
private fun TemplatesDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
            Column(Modifier.width(640.dp).heightIn(max = 640.dp).padding(16.dp)) {
                Text("Templates", color = TEXT, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Box(Modifier.weight(1f)) { TemplatesEditor() }
                Spacer(Modifier.height(8.dp))
                ActionButton("Close") { onDismiss() }
            }
        }
    }
}

@Composable
private fun CopyToDialog(app: MainScreenApp, sys: MainScreenSystem, onDismiss: () -> Unit) {
    val agent = app.viewingAgent()
    if (agent == null) { LaunchedEffect(Unit) { onDismiss() }; return }
    val candidates = TabsModel.tabs.filter { it.fileName != agent.fileName && AgentsModel.agentFor(it.fileName) == null }
    var selected by remember { mutableStateOf(candidates.firstOrNull()?.fileName) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
            Column(Modifier.width(400.dp).padding(16.dp)) {
                Text("Copy ${agent.name} to file:", color = TEXT, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                if (candidates.isEmpty()) Text("(no free files)", color = TEXT.copy(alpha = 0.5f), fontSize = 12.sp)
                else Column(Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState())) {
                    candidates.forEach { tab ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            .background(if (selected == tab.fileName) MENU_SELECTED else Color.Transparent, RoundedCornerShape(4.dp))
                            .clickable { selected = tab.fileName }.padding(8.dp)) {
                            Text(tab.fileName, color = TEXT, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton("Copy") {
                        val target = selected
                        if (target != null) sys.copyAgentTo(agent.id, target)
                        onDismiss()
                    }
                    ActionButton("Cancel") { onDismiss() }
                }
            }
        }
    }
}

@Composable
private fun MoveToDialog(app: MainScreenApp, sys: MainScreenSystem, onDismiss: () -> Unit) {
    val agent = app.viewingAgent()
    if (agent == null) { LaunchedEffect(Unit) { onDismiss() }; return }
    val candidates = TabsModel.tabs.filter { it.fileName != agent.fileName && AgentsModel.agentFor(it.fileName) == null }
    var selected by remember { mutableStateOf(candidates.firstOrNull()?.fileName) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
            Column(Modifier.width(400.dp).padding(16.dp)) {
                Text("Move ${agent.name} to file:", color = TEXT, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                if (candidates.isEmpty()) Text("(no free files)", color = TEXT.copy(alpha = 0.5f), fontSize = 12.sp)
                else Column(Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState())) {
                    candidates.forEach { tab ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            .background(if (selected == tab.fileName) MENU_SELECTED else Color.Transparent, RoundedCornerShape(4.dp))
                            .clickable { selected = tab.fileName }.padding(8.dp)) {
                            Text(tab.fileName, color = TEXT, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton("Move") {
                        val target = selected
                        if (target != null) sys.moveAgentTo(agent.id, target)
                        onDismiss()
                    }
                    ActionButton("Cancel") { onDismiss() }
                }
            }
        }
    }
}

@Composable
private fun DeleteAgentDialog(app: MainScreenApp, sys: MainScreenSystem, onDismiss: () -> Unit) {
    val id = Substrate.pendingDeleteAgentId
    val agent = id?.let { AgentsModel.byId(it) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete agent?", color = TEXT) },
        text = { Text("Удалить ${agent?.name ?: "агента"}? Состояние будет потеряно.", color = TEXT) },
        confirmButton = {
            ActionButton("Delete") {
                if (id != null) sys.deleteAgent(id)
                Substrate.pendingDeleteAgentId = null
                onDismiss()
            }
        },
        dismissButton = { ActionButton("Cancel") { Substrate.pendingDeleteAgentId = null; onDismiss() } }
    )
}

@Composable
private fun DeleteFileDialog(app: MainScreenApp, sys: MainScreenSystem, onDismiss: () -> Unit) {
    val name = Substrate.pendingDeleteFile
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete file?", color = TEXT) },
        text = { Text("Удалить $name? Файл и агент будут удалены.", color = TEXT) },
        confirmButton = {
            ActionButton("Delete") {
                if (name != null) sys.deleteFileCascade(name)
                Substrate.pendingDeleteFile = null
                onDismiss()
            }
        },
        dismissButton = { ActionButton("Cancel") { Substrate.pendingDeleteFile = null; onDismiss() } }
    )
}

@Composable
private fun NavigateBehaviorDialog(
    occupiedFileName: String,
    occupyingAgent: AgentState?,
    onDismiss: () -> Unit,
    onConfirm: (NavigateBehavior) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
            Column(Modifier.width(440.dp).padding(16.dp)) {
                Text("Файл занят агентом", color = TEXT, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text("$occupiedFileName — ${occupyingAgent?.name ?: "agent"}", color = TEXT.copy(alpha = 0.7f), fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                Text("Что делать?", color = TEXT.copy(alpha = 0.6f), fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ActionButton("Ждать") { onConfirm(NavigateBehavior.WAIT) }
                    ActionButton("Вытеснить") { onConfirm(NavigateBehavior.EVICT) }
                    ActionButton("Остаться") { onConfirm(NavigateBehavior.STAY) }
                    ActionButton("Ошибка") { onConfirm(NavigateBehavior.ERROR) }
                }
            }
        }
    }
}

@Composable
private fun TabsPicker(app: MainScreenApp, sys: MainScreenSystem, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
            Column(Modifier.width(600.dp).heightIn(max = 640.dp).padding(16.dp)) {
                Text("Open", color = TEXT, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton("New") { app.newTab(); onDismiss() }
                    ActionButton("Open File…") { sys.openFileDialog()?.let { sys.openFile(it) }; onDismiss() }
                    ActionButton("Open Folder…") { sys.openFolderDialog()?.let { sys.openFolder(it) }; onDismiss() }
                }
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(MENU_SEPARATOR))
                Spacer(Modifier.height(8.dp))
                Text("Recent", color = TEXT.copy(alpha = 0.6f), fontSize = 11.sp)
                Column(Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState())) {
                    if (Substrate.recentFiles.isEmpty()) MenuRowDisabled("(empty)")
                    else Substrate.recentFiles.forEach { name ->
                        MenuRow(name) { sys.openFile(File(System.getProperty("user.home"), ".simple/$name")); onDismiss() }
                    }
                }
                Spacer(Modifier.height(8.dp))
                ActionButton("Close") { onDismiss() }
            }
        }
    }
}

@Composable
private fun SamplesDialog(app: MainScreenApp, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
            Column(Modifier.width(760.dp).heightIn(max = 700.dp).padding(16.dp)) {
                Text("SAMPLES (${Substrate.completedSamples.size}/${SAMPLES.items.size})", color = TEXT, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                    SAMPLES.items.forEach { item ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            .background(if (item.id in Substrate.completedSamples) Color(0xFFE6F4EA) else MENU_BG, RoundedCornerShape(8.dp))
                            .clickable { app.openSamplesTab(item); onDismiss() }.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            if (item.id in Substrate.completedSamples) {
                                Text("✓", color = Color(0xFF2E7D32), fontSize = 14.sp); Spacer(Modifier.width(8.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(item.title, color = TEXT, fontSize = 13.sp)
                                Spacer(Modifier.height(2.dp))
                                Text(item.code.lines().take(2).joinToString("\n"), color = TEXT.copy(alpha = 0.55f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                            }
                            Text("→", color = ACCENT, fontSize = 16.sp)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                ActionButton("Close") { onDismiss() }
            }
        }
    }
}

@Composable
private fun SearchDialog(app: MainScreenApp, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var replace by remember { mutableStateOf("") }
    var matches by remember { mutableStateOf<List<Int>>(emptyList()) }
    var cursor by remember { mutableStateOf(0) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
            Column(Modifier.width(520.dp).padding(16.dp)) {
                Text("Find / Replace", color = TEXT, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                BasicTextField(query, { query = it }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFF7F8FA), RoundedCornerShape(6.dp)).padding(6.dp),
                    textStyle = TextStyle(color = TEXT, fontFamily = FontFamily.Monospace, fontSize = 12.sp))
                Spacer(Modifier.height(6.dp))
                BasicTextField(replace, { replace = it }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFF7F8FA), RoundedCornerShape(6.dp)).padding(6.dp),
                    textStyle = TextStyle(color = TEXT, fontFamily = FontFamily.Monospace, fontSize = 12.sp))
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton("Find all") {
                        val code = TabsModel.active()?.code ?: ""
                        val list = mutableListOf<Int>()
                        var idx = code.indexOf(query)
                        while (idx >= 0) { list += idx; idx = code.indexOf(query, idx + 1) }
                        matches = list; cursor = 0
                    }
                    ActionButton("◀") { if (cursor > 0) cursor-- }
                    ActionButton("▶") { if (cursor < matches.size - 1) cursor++ }
                    ActionButton("Replace all") {
                        val tab = TabsModel.active() ?: return@ActionButton
                        if (query.isBlank()) return@ActionButton
                        tab.code = tab.code.replace(query, replace)
                        tab.dirty = true; app.publishActive()
                    }
                    ActionButton("Close") { onDismiss() }
                }
                if (matches.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("${cursor + 1} of ${matches.size}", color = TEXT.copy(alpha = 0.6f), fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun DocDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
            Column(Modifier.width(800.dp).heightIn(max = 700.dp).padding(16.dp)) {
                Text(Substrate.docTitle, color = TEXT, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                SelectionContainer {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        Text(Substrate.docContent, color = TEXT, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                ActionButton("Close") { onDismiss() }
            }
        }
    }
}

@Composable
private fun SettingsDialog(app: MainScreenApp, sys: MainScreenSystem, onDismiss: () -> Unit) {
    var tab by remember { mutableStateOf("General") }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
            Column(Modifier.width(640.dp).heightIn(max = 640.dp).padding(16.dp)) {
                Text("Settings", color = TEXT, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TabChip("General", tab == "General") { tab = "General" }
                    TabChip("Templates", tab == "Templates") { tab = "Templates" }
                    TabChip("Sets", tab == "Sets") { tab = "Sets" }
                }
                Spacer(Modifier.height(8.dp))
                Box(Modifier.weight(1f)) {
                    when (tab) {
                        "Templates" -> TemplatesEditor()
                        "Sets" -> SetsEditor(sys)
                        else -> GeneralEditor(app)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton("Close") { onDismiss() }
                }
            }
        }
    }
}

@Composable
private fun GeneralEditor(app: MainScreenApp) {
    var limit by remember { mutableStateOf(Substrate.stepBackLimit.toString()) }
    var speed by remember { mutableStateOf(Substrate.speedMs.toString()) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SettingRow("History steps", limit) { limit = it }
        SettingRow("Speed (ms)", speed) { speed = it }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Auto-update declarations", color = TEXT, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Checkbox(checked = Substrate.autoSyncDeclarations, onCheckedChange = { Substrate.autoSyncDeclarations = it })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Strict Mode (no ':' functions)", color = TEXT, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Checkbox(checked = Substrate.strictMode, onCheckedChange = { Substrate.strictMode = it; app.validateCurrent() })
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton("Apply") {
                Substrate.stepBackLimit = limit.toIntOrNull()?.coerceIn(1, 100) ?: 12
                Substrate.speedMs = speed.toLongOrNull()?.coerceIn(0L, 5000L) ?: 400L
            }
            ActionButton("Run golden tests") {
                val results = GoldenTests.runAll()
                val passed = results.count { it.passed }
                Substrate.log("Golden tests: $passed/${results.size}")
                results.filterNot { it.passed }.forEach { Substrate.log("  ✗ ${it.title}: ${it.message}") }
            }
            ActionButton("Reset Values width") { Substrate.valuesWidthOverride = null }
        }
    }
}

@Composable
private fun TemplatesEditor() {
    var newTitle by remember { mutableStateOf("") }
    var newColor by remember { mutableStateOf(Substrate.COLOR_PALETTE.first()) }
    var newTemplate by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(value = newTitle, onValueChange = { newTitle = it }, singleLine = true,
                modifier = Modifier.width(140.dp).background(Color(0xFFF7F8FA), RoundedCornerShape(6.dp)).padding(6.dp),
                textStyle = TextStyle(color = TEXT, fontFamily = FontFamily.Monospace, fontSize = 11.sp))
            Spacer(Modifier.width(6.dp))
            ColorPicker(newColor) { newColor = it }
            Spacer(Modifier.width(6.dp))
            ActionButton("Add group") { Substrate.addGroup(newTitle, newColor); newTitle = "" }
            Spacer(Modifier.width(6.dp))
            ActionButton("Reset to default") { Substrate.resetTemplatesToDefault() }
        }
        Spacer(Modifier.height(8.dp))
        Substrate.templateGroups.forEachIndexed { gi, group ->
            Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)
                .background(group.color.copy(alpha = 0.35f), RoundedCornerShape(6.dp)).padding(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    var title by remember(group.title) { mutableStateOf(group.title) }
                    BasicTextField(value = title, onValueChange = { title = it; Substrate.renameGroup(gi, it) },
                        singleLine = true,
                        modifier = Modifier.width(140.dp).background(Color.White, RoundedCornerShape(6.dp)).padding(6.dp),
                        textStyle = TextStyle(color = TEXT, fontFamily = FontFamily.Monospace, fontSize = 11.sp))
                    Spacer(Modifier.width(6.dp))
                    ColorPicker(group.color) { Substrate.setGroupColor(gi, it) }
                    Spacer(Modifier.weight(1f))
                    SmallIconButton("×") { Substrate.removeGroup(gi) }
                }
                Spacer(Modifier.height(4.dp))
                group.templates.forEach { t ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(t, color = TEXT, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                        SmallIconButton("×") { Substrate.removeTemplate(gi, t) }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(value = newTemplate, onValueChange = { newTemplate = it }, singleLine = true,
                        modifier = Modifier.weight(1f).background(Color.White, RoundedCornerShape(6.dp)).padding(6.dp),
                        textStyle = TextStyle(color = TEXT, fontFamily = FontFamily.Monospace, fontSize = 11.sp))
                    Spacer(Modifier.width(6.dp))
                    ActionButton("+") { Substrate.addTemplate(gi, newTemplate); newTemplate = "" }
                }
            }
        }
    }
}

@Composable
private fun SetsEditor(sys: MainScreenSystem) {
    var newName by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(value = newName, onValueChange = { newName = it }, singleLine = true,
                modifier = Modifier.width(180.dp).background(Color(0xFFF7F8FA), RoundedCornerShape(6.dp)).padding(6.dp),
                textStyle = TextStyle(color = TEXT, fontFamily = FontFamily.Monospace, fontSize = 11.sp))
            Spacer(Modifier.width(6.dp))
            ActionButton("Save current set") { Substrate.saveTemplateSet(newName); newName = "" }
            Spacer(Modifier.width(6.dp))
            ActionButton("Load default") { Substrate.resetTemplatesToDefault() }
        }
        Spacer(Modifier.height(8.dp))
        Substrate.savedTemplateSets.keys.forEach { name ->
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(name, color = TEXT, fontSize = 12.sp, modifier = Modifier.weight(1f))
                SmallIconButton("▶") { Substrate.loadTemplateSet(name) }
                Spacer(Modifier.width(4.dp))
                SmallIconButton("×") { Substrate.deleteTemplateSet(name) }
            }
        }
    }
}

@Composable
private fun ColorPicker(current: Color, onPick: (Color) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        Box(Modifier.size(24.dp).background(current, RoundedCornerShape(6.dp))
            .border(1.dp, MENU_BORDER, RoundedCornerShape(6.dp)).clickable { open = !open })
        if (open) {
            Popup(onDismissRequest = { open = false }) {
                Column(Modifier.background(Color.White, RoundedCornerShape(6.dp))
                    .border(1.dp, MENU_SEPARATOR, RoundedCornerShape(6.dp)).padding(6.dp)) {
                    Substrate.COLOR_PALETTE.chunked(6).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            row.forEach { c ->
                                Box(Modifier.size(20.dp).background(c, RoundedCornerShape(4.dp))
                                    .border(1.dp, MENU_BORDER, RoundedCornerShape(4.dp))
                                    .clickable { onPick(c); open = false })
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingRow(label: String, value: String, onChange: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TEXT, fontSize = 12.sp, modifier = Modifier.weight(1f))
        BasicTextField(value = value, onValueChange = onChange, singleLine = true,
            modifier = Modifier.width(80.dp).background(Color(0xFFF7F8FA), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            textStyle = TextStyle(color = TEXT, fontFamily = FontFamily.Monospace, fontSize = 12.sp))
    }
}

@Composable
private fun SaveStartDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text("Save as start state?", color = TEXT) },
        text = { Text("This will replace the current start state with the current code. Continue?", color = TEXT) },
        confirmButton = { ActionButton("Save as start") { onConfirm() } },
        dismissButton = { ActionButton("Cancel") { onDismiss() } })
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text("About Simple True Interpreter", color = TEXT) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Version: ${Substrate.version} (${Substrate.platform})", color = TEXT, fontSize = 12.sp)
                Text("Language: ${Substrate.languageVersion}", color = TEXT, fontSize = 12.sp)
                Text("Developed by Александр Кундрюков", color = TEXT, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Text("Links:", color = TEXT, fontSize = 12.sp)
                Text("README: ${Substrate.GITHUB_README_URL}", color = ACCENT, fontSize = 11.sp)
                Text("Email: a.kundryukov@gmail.com", color = ACCENT, fontSize = 11.sp)
                Text("Telegram: @e16din", color = ACCENT, fontSize = 11.sp)
                Text("MAX: ${Substrate.MAX_URL}", color = ACCENT, fontSize = 11.sp)
            }
        },
        confirmButton = { ActionButton("Close") { onDismiss() } })
}

@Composable
private fun TabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) ACCENT else MENU_BG
    val fg = if (selected) Color.White else TEXT
    Box(Modifier.height(22.dp).background(bg, RoundedCornerShape(6.dp)).clickable { onClick() }
        .padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
        Text(label, color = fg, fontSize = 11.sp)
    }
}

@Composable
private fun ActionButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick) { Text(label, color = Color.White, fontSize = 12.sp) }
}

