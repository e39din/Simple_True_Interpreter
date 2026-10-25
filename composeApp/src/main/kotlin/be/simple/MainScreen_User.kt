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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
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
private val CURRENT_LINE = Color(0xFFDDF0E0)
private val NEW_LOG = Color(0xFFE6F4EA)
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

private val CHIP_MIN_WIDTH = 140.dp
private val CHIP_PADDING_H = 12.dp
private val CLOSE_BTN_WIDTH = 20.dp
private val STRIP_WIDTH = 28.dp

// Ограничения для drag-сплиттеров.
private val VALUES_MIN_WIDTH = 200.dp
private val VALUES_MAX_WIDTH = 480.dp
private val ACTIONS_MIN_WIDTH = 200.dp
private val ACTIONS_MAX_WIDTH = 480.dp
private val CYCLE_MIN_WIDTH = 320.dp

private fun logColor(type: String): Color = when (type) {
    "error" -> Color(0xFFE55451)
    "jump" -> Color(0xFF9C27B0)
    "command" -> Color(0xFF2196F3)
    "log" -> Color(0xFFFF9800)
    "string" -> Color(0xFF4CAF50)
    else -> Color(0xFF9AA1AC)
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

        // Пропорции φ: Values и Actions ≈ 0.382/2 от total, Cycle — 0.618. Override — если пользователь тянул.
        val valuesW = Substrate.valuesWidthOverride ?: (total / (phi + 1) / 2).coerceIn(VALUES_MIN_WIDTH, VALUES_MAX_WIDTH)
        val actionsW = Substrate.actionsWidthOverride ?: (total / (phi + 1) / 2).coerceIn(ACTIONS_MIN_WIDTH, ACTIONS_MAX_WIDTH)

        Column(Modifier.fillMaxSize()) {
            MenuBar(app, sys)
            Row(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 12.dp, vertical = 8.dp)) {
                // Values
                if (Substrate.valuesCollapsed) {
                    Box(Modifier.fillMaxHeight(), contentAlignment = Alignment.CenterStart) { ValuesStrip() }
                } else {
                    Box(Modifier.width(valuesW).fillMaxHeight()) { ValuesPanel(app) }
                }
                // Сплиттер Values | Cycle (только если развёрнута)
                if (!Substrate.valuesCollapsed) {
                    DragSplitter(vertical = true) { delta ->
                        val current = Substrate.valuesWidthOverride ?: valuesW
                        val next = (current + (delta / density).dp).coerceIn(VALUES_MIN_WIDTH, VALUES_MAX_WIDTH)
                        Substrate.valuesWidthOverride = next
                    }
                }
                // Cycle
                Column(Modifier.weight(phi).fillMaxHeight()) {
                    CycleArea(app, sys, Modifier.fillMaxWidth().weight(1f))
                }
                // Сплиттер Cycle | Actions
                if (!Substrate.actionsCollapsed) {
                    DragSplitter(vertical = true) { delta ->
                        val current = Substrate.actionsWidthOverride ?: actionsW
                        val next = (current - (delta / density).dp).coerceIn(ACTIONS_MIN_WIDTH, ACTIONS_MAX_WIDTH)
                        Substrate.actionsWidthOverride = next
                    }
                } else {
                    Spacer(Modifier.width(8.dp))
                }
                // Actions
                if (Substrate.actionsCollapsed) {
                    Box(Modifier.fillMaxHeight(), contentAlignment = Alignment.CenterEnd) { ActionsStrip() }
                } else {
                    Box(Modifier.width(actionsW).fillMaxHeight()) { ActionsPanel(app, sys) }
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

/** Вертикальный drag-сплиттер. delta — сдвиг в пикселях по X. */
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
            MenuRow("Exit") { sys.stopAutoRun() }
        }
        MenuItem("Run") {
            MenuRow("Play", shortcut = "Ctrl+R") { app.startPlay() }
            MenuRow("Step", shortcut = "→") { app.step() }
            MenuRow("Step Back", shortcut = "←") { app.stepBack() }
            MenuSeparator()
            MenuRow("Pause", shortcut = "Ctrl+P") { Substrate.status = Status.PAUSED; sys.stopAutoRun() }
            MenuRow("Stop") { Substrate.status = Status.STOPPED; sys.stopAutoRun() }
            MenuSeparator()
            MenuRow("Reset") { app.reset() }
        }
        MenuItem("Settings") {
            MenuRow("Open Settings…") { Substrate.showSettingsDialog = true }
            MenuRow("Reset panel widths") {
                Substrate.valuesWidthOverride = null
                Substrate.actionsWidthOverride = null
            }
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
private fun CycleArea(app: MainScreenApp, sys: MainScreenSystem, modifier: Modifier) {
    var savedValuesCollapsed by remember { mutableStateOf(false) }
    var savedActionsCollapsed by remember { mutableStateOf(false) }

    fun toggleMaximize() {
        val anyExpanded = !Substrate.valuesCollapsed || !Substrate.actionsCollapsed
        if (anyExpanded) {
            savedValuesCollapsed = Substrate.valuesCollapsed
            savedActionsCollapsed = Substrate.actionsCollapsed
            Substrate.valuesCollapsed = true
            Substrate.actionsCollapsed = true
        } else {
            Substrate.valuesCollapsed = savedValuesCollapsed
            Substrate.actionsCollapsed = savedActionsCollapsed
        }
    }

    Column(modifier.background(Color.White, RoundedCornerShape(12.dp)).padding(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            ActiveFileDropdown(app)
            ActionIconButton("▶") { app.startPlay() }
            ActionIconButton("⏭", enabled = app.canStep()) { app.step() }
            ActionIconButton("⏮", enabled = app.canStep()) { app.stepBack() }
            ActionIconButton("⏸") { Substrate.status = Status.PAUSED; sys.stopAutoRun() }
            ActionIconButton("⏹") { Substrate.status = Status.STOPPED; sys.stopAutoRun(); app.stopPlay() }
            ResetButton(app, sys)
            Spacer(Modifier.width(8.dp))
            Text("Speed:", color = TEXT, fontSize = 12.sp)
            Spacer(Modifier.width(4.dp))
            SpeedField(sys)
            Spacer(Modifier.width(4.dp))
            Text("ms", color = TEXT.copy(alpha = 0.6f), fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            ActionIconButton("⛶") { toggleMaximize() }
            if (Substrate.strictMode) {
                Spacer(Modifier.width(8.dp))
                Box(Modifier.background(STRICT_BADGE, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text("STRICT", color = Color(0xFF6D4C00), fontSize = 10.sp)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        FileChipsBar(app, sys)
        Spacer(Modifier.height(6.dp))
        TabsRow()
        Spacer(Modifier.height(6.dp))
        val err = Substrate.logEntries.firstOrNull { it.type == "error" }
        if (err?.lineNumber != null) {
            Box(Modifier.background(ERROR_BADGE, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text("Error on line ${err.lineNumber}: ${err.text}", color = ERROR_TEXT, fontSize = 11.sp)
            }
            Spacer(Modifier.height(4.dp))
        }
        Box(Modifier.fillMaxWidth().weight(1f).background(Color(0xFFF7F8FA), RoundedCornerShape(8.dp)).padding(8.dp)) {
            when (Substrate.activeTabView) {
                "Scheme" -> Placeholder("Scheme (planned)")
                "Table"  -> Placeholder("Table (planned)")
                else -> StripedCodeField(app = app, currentLine = Substrate.currentStep)
            }
        }
    }
}

@Composable
private fun ActiveFileDropdown(app: MainScreenApp) {
    var expanded by remember { mutableStateOf(false) }
    var anchor by remember { mutableStateOf<Rect?>(null) }
    val runningId = Substrate.runningTabId
    val label = buildString {
        if (runningId != null) append("▶ ")
        append(app.fileName())
    }
    Box(
        Modifier.height(28.dp)
            .background(MENU_BG, RoundedCornerShape(6.dp))
            .clickable { expanded = !expanded }
            .padding(horizontal = 8.dp)
            .onGloballyPositioned { anchor = it.boundsInWindow() },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = TEXT,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.width(4.dp))
            Text("▾", color = TEXT.copy(alpha = 0.6f), fontSize = 10.sp)
        }
    }
    if (expanded) {
        Popup(popupPositionProvider = AnchorBelowProvider(anchor),
            onDismissRequest = { expanded = false }, properties = PopupProperties(focusable = true)) {
            Column(Modifier.widthIn(min = 200.dp).background(Color.White, RoundedCornerShape(6.dp))
                .border(1.dp, MENU_SEPARATOR, RoundedCornerShape(6.dp)).padding(4.dp)) {
                TabsModel.tabs.forEachIndexed { idx, tab ->
                    val mark = when {
                        tab.id == runningId -> "▶ "
                        tab.dirty -> "• "
                        else -> ""
                    }
                    MenuRow("$mark${tab.fileName}") {
                        app.setActiveTab(idx); expanded = false
                    }
                }
            }
        }
    }
}

@Composable
private fun FileChipsBar(app: MainScreenApp, sys: MainScreenSystem) {
    val scroll = rememberScrollState()
    Row(Modifier.fillMaxWidth().height(34.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).fillMaxHeight()) {
            Row(Modifier.fillMaxHeight().horizontalScroll(scroll), verticalAlignment = Alignment.CenterVertically) {
                TabsModel.tabs.forEachIndexed { idx, tab ->
                    FileChip(tab, idx == TabsModel.activeIndex, tab.id == Substrate.runningTabId,
                        onSelect = { app.setActiveTab(idx) },
                        onClose = { app.closeTab(idx) })
                    Spacer(Modifier.width(4.dp))
                }
            }
            if (TabsModel.tabs.size > 1) {
                HorizontalScrollbar(
                    adapter = rememberScrollbarAdapter(scroll),
                    modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().height(4.dp)
                )
            }
        }
        Spacer(Modifier.width(6.dp))
        Box(Modifier.size(28.dp).background(MENU_BG, RoundedCornerShape(6.dp))
            .clickable { Substrate.showTabsPicker = true }, contentAlignment = Alignment.Center) {
            Text("+", color = TEXT, fontSize = 16.sp)
        }
    }
}

@Composable
private fun FileChip(tab: TabState, active: Boolean, running: Boolean, onSelect: () -> Unit, onClose: () -> Unit) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val text = (if (tab.dirty) "• " else "") + tab.fileName
    val style = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace)
    val measuredWidth = with(density) { measurer.measure(text, style).size.width.toDp() }
    val chipWidth = (measuredWidth + CHIP_PADDING_H * 2 + CLOSE_BTN_WIDTH).coerceAtLeast(CHIP_MIN_WIDTH)

    val bg = when {
        running -> Color(0xFF2E7D32)
        active -> ACCENT
        else -> MENU_BG
    }
    val fg = if (active || running) Color.White else TEXT

    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    Row(
        Modifier.width(chipWidth).height(30.dp)
            .background(if (hovered && !active && !running) MENU_HOVER else bg, RoundedCornerShape(8.dp))
            .border(1.dp, if (active || running) bg else MENU_BORDER, RoundedCornerShape(8.dp))
            .hoverable(interaction)
            .clickable { onSelect() }
            .padding(start = CHIP_PADDING_H, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, color = fg, fontSize = 12.sp, fontFamily = FontFamily.Monospace,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f))
        Spacer(Modifier.width(4.dp))
        Box(Modifier.size(CLOSE_BTN_WIDTH).clickable { onClose() }, contentAlignment = Alignment.Center) {
            Text("×", color = fg.copy(alpha = 0.75f), fontSize = 14.sp)
        }
    }
}

@Composable
private fun TabsRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        TabChip("Text", Substrate.activeTabView == "Text") { Substrate.activeTabView = "Text" }
        TabChip("Scheme", Substrate.activeTabView == "Scheme") { Substrate.activeTabView = "Scheme" }
        TabChip("Table", Substrate.activeTabView == "Table") { Substrate.activeTabView = "Table" }
    }
}

@Composable
private fun TabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) ACCENT else MENU_BG
    val fg = if (selected) Color.White else TEXT
    Box(Modifier.height(24.dp).background(bg, RoundedCornerShape(6.dp)).clickable { onClick() }
        .padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
        Text(label, color = fg, fontSize = 12.sp)
    }
}

@Composable
private fun ResetButton(app: MainScreenApp, sys: MainScreenSystem) {
    var menuOpen by remember { mutableStateOf(false) }
    var anchor by remember { mutableStateOf<Rect?>(null) }
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    Box {
        Box(Modifier.size(28.dp).background(if (hovered) MENU_HOVER else BTN_BG, RoundedCornerShape(6.dp))
            .hoverable(interaction)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Press) {
                            if (event.buttons.isSecondaryPressed) menuOpen = true else app.reset()
                        }
                    }
                }
            }.onGloballyPositioned { anchor = it.boundsInWindow() },
            contentAlignment = Alignment.Center) { Text("⟲", color = BTN_FG, fontSize = 14.sp) }
        if (menuOpen) {
            Popup(popupPositionProvider = AnchorBelowProvider(anchor),
                onDismissRequest = { menuOpen = false }, properties = PopupProperties(focusable = true)) {
                Column(Modifier.width(280.dp).background(Color.White, RoundedCornerShape(6.dp))
                    .border(1.dp, MENU_SEPARATOR, RoundedCornerShape(6.dp)).padding(bottom = 2.dp)) {
                    MenuRow("Save current as start state…") { menuOpen = false; Substrate.showSaveStartDialog = true }
                }
            }
        }
    }
}

@Composable
private fun SpeedField(sys: MainScreenSystem) {
    var speedText by remember { mutableStateOf(Substrate.speedMs.toString()) }
    LaunchedEffect(Substrate.speedMs) { speedText = Substrate.speedMs.toString() }
    BasicTextField(value = speedText, onValueChange = { v ->
        val digits = v.filter { it.isDigit() }
        speedText = digits
        digits.toLongOrNull()?.let { ms ->
            Substrate.speedMs = ms.coerceIn(0L, 5000L)
            Substrate.status = Status.PAUSED
            sys.stopAutoRun()
        }
    }, singleLine = true,
        modifier = Modifier.width(64.dp).background(Color(0xFFF7F8FA), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        textStyle = TextStyle(color = TEXT, fontFamily = FontFamily.Monospace, fontSize = 13.sp))
}

@Composable
private fun StripedCodeField(app: MainScreenApp, currentLine: Int) {
    val tab = TabsModel.active() ?: return
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
    val transformation = remember(currentLine) { LineHighlighter(currentLine) }
    Box {
        BasicTextField(value = tfv,
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
            textStyle = TextStyle(color = TEXT, fontFamily = FontFamily.Monospace, fontSize = 14.sp),
            visualTransformation = transformation)
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

/** Использует Substrate.errorLines — O(1) по логу. */
private class LineHighlighter(private val currentLine: Int) : VisualTransformation {

    private var lastCode: String? = null
    private var lastLine: Int = -1
    private var lastErrors: Set<Int> = emptySet()
    private var cached: AnnotatedString? = null
    private var cachedMapping: OffsetMapping? = null

    override fun filter(text: AnnotatedString): TransformedText {
        val code = text.text
        val errorLines = Substrate.errorLines

        if (code == lastCode && currentLine == lastLine && errorLines == lastErrors &&
            cached != null && cachedMapping != null) {
            return TransformedText(cached!!, cachedMapping!!)
        }

        val lines = code.split("\n")
        val width = lines.size.toString().length
        val prefixLen = width + 3

        val annotated = buildAnnotatedString {
            lines.forEachIndexed { idx, line ->
                val numStart = length
                append((idx + 1).toString().padStart(width, ' '))
                val numEnd = length
                append(" │ ")
                append(line)
                val textEnd = length
                addStyle(SpanStyle(color = if (idx + 1 == currentLine) ACCENT else LINE_NUM), numStart, numEnd)
                if (idx + 1 == currentLine) addStyle(SpanStyle(fontWeight = FontWeight.Bold), numStart, numEnd)
                val bg = when {
                    idx + 1 == currentLine -> CURRENT_LINE
                    idx % 2 == 1 -> ZEBRA
                    else -> Color.Transparent
                }
                if (bg != Color.Transparent) addStyle(SpanStyle(background = bg), numStart, textEnd)
                if (idx + 1 in errorLines) addStyle(SpanStyle(textDecoration = TextDecoration.Underline, color = ERROR_TEXT), numStart, textEnd)
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
    Box(Modifier.size(28.dp).background(bg, RoundedCornerShape(6.dp))
        .hoverable(interaction).clickable(enabled = enabled) { onClick() }, contentAlignment = Alignment.Center) {
        Text(glyph, color = fg, fontSize = 14.sp)
    }
}

@Composable
private fun ValuesPanel(app: MainScreenApp) {
    Column(Modifier.fillMaxSize().background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Values", color = TEXT, fontSize = 18.sp)
            Spacer(Modifier.weight(1f))
            SmallIconButton("📋") { copyToClipboard(valuesAsText()) }
            Spacer(Modifier.width(4.dp))
            SmallIconButton("‹") { Substrate.valuesCollapsed = true }
        }
        if (Substrate.copiedValueText != null) {
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxWidth().background(COPIED, RoundedCornerShape(6.dp)).padding(6.dp)) {
                Text("Copied: ${Substrate.copiedValueText}", color = TEXT, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
        SelectionContainer {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                val visible = Substrate.variablesSnapshot.filterKeys { it !in Substrate.hiddenSnapshot }
                val hidden = Substrate.variablesSnapshot.filterKeys { it in Substrate.hiddenSnapshot }
                var rowIndex = 0
                if (visible.isEmpty() && hidden.isEmpty()) Text("(no variables)", color = TEXT.copy(alpha = 0.5f))
                visible.forEach { (k, v) ->
                    val isConst = k in Substrate.constantsSnapshot
                    ValueRow("$k: $v${if (isConst) " (const)" else ""}",
                        if (isConst) CONST_COLOR else if (rowIndex % 2 == 1) ZEBRA else Color.Transparent, TEXT)
                    rowIndex++
                }
                if (hidden.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp)); Text("Hidden:", color = TEXT)
                    hidden.forEach { (k, v) ->
                        ValueRow("$k: $v", if (rowIndex % 2 == 1) ZEBRA else Color.Transparent, TEXT.copy(alpha = 0.6f))
                        rowIndex++
                    }
                }
                if (Substrate.resultsSnapshot.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp)); Text("Results:", color = TEXT)
                    Substrate.resultsSnapshot.forEach { (k, v) ->
                        ValueRow("$k: $v", if (rowIndex % 2 == 1) ZEBRA else Color.Transparent, TEXT)
                        rowIndex++
                    }
                }
            }
        }
    }
}

/** Свёрнутая левая панель. Заголовок повёрнут на -90° и не переносится: requiredWidth + rotate. */
@Composable
private fun ValuesStrip() {
    Box(
        Modifier.width(STRIP_WIDTH).fillMaxHeight()
            .background(Color.White, RoundedCornerShape(12.dp))
            .clickable { Substrate.valuesCollapsed = false },
        contentAlignment = Alignment.Center
    ) {
        Text("Values", color = TEXT, fontSize = 12.sp,
            maxLines = 1,
            modifier = Modifier.rotate(-90f).requiredWidth(120.dp))
    }
}

@Composable
private fun ValueRow(text: String, bg: Color, fg: Color) {
    Box(Modifier.fillMaxWidth().background(bg)
        .clickable { copyToClipboard(text); Substrate.showCopied(text) }
        .padding(vertical = 1.dp)) {
        Text(text, color = fg, fontSize = 13.sp)
    }
}

private fun valuesAsText(): String {
    val sb = StringBuilder()
    Substrate.variablesSnapshot.forEach { (k, v) -> sb.appendLine("$k: $v") }
    Substrate.resultsSnapshot.forEach { (k, v) -> sb.appendLine("$k: $v") }
    return sb.toString()
}

@Composable
private fun ActionsPanel(app: MainScreenApp, sys: MainScreenSystem) {
    Column(Modifier.fillMaxSize().background(Color.White, RoundedCornerShape(12.dp)).padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Actions", color = TEXT, fontSize = 16.sp)
            Spacer(Modifier.weight(1f))
            SmallIconButton("›") { Substrate.actionsCollapsed = true }
        }
        Spacer(Modifier.height(8.dp))
        BasicTextField(value = Substrate.actionsSearchQuery,
            onValueChange = { Substrate.actionsSearchQuery = it }, singleLine = true,
            modifier = Modifier.fillMaxWidth().background(Color(0xFFF7F8FA), RoundedCornerShape(6.dp))
                .padding(6.dp),
            textStyle = TextStyle(color = TEXT, fontSize = 12.sp, fontFamily = FontFamily.Monospace))
        Spacer(Modifier.height(8.dp))
        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
            if (Substrate.pinnedTemplates.isNotEmpty() && Substrate.actionsSearchQuery.isBlank()) {
                Text("Pinned", color = TEXT.copy(alpha = 0.6f), fontSize = 11.sp)
                Substrate.pinnedTemplates.forEach { t -> PinnedChip(t) { app.insertAtCursor(t) } }
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(MENU_SEPARATOR))
                Spacer(Modifier.height(8.dp))
            }
            val q = Substrate.actionsSearchQuery.lowercase()
            Substrate.templateGroups.forEach { group ->
                val filtered = group.templates.filter { q.isBlank() || it.lowercase().contains(q) }
                if (filtered.isNotEmpty()) {
                    Text(group.title, color = TEXT.copy(alpha = 0.6f), fontSize = 11.sp,
                        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp))
                    filtered.forEach { t ->
                        TemplateChip(t, group.color, onClick = { app.insertAtCursor(t) }, onPin = { Substrate.pinTemplate(t) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateChip(text: String, bg: Color, onClick: () -> Unit, onPin: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    var anchor by remember { mutableStateOf<Rect?>(null) }
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    Box {
        Box(Modifier.fillMaxWidth().padding(vertical = 2.dp)
            .background(if (hovered) bg.copy(alpha = 0.8f) else bg, RoundedCornerShape(6.dp))
            .hoverable(interaction)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Press) {
                            if (event.buttons.isSecondaryPressed) menuOpen = true else onClick()
                        }
                    }
                }
            }
            .onGloballyPositioned { anchor = it.boundsInWindow() }
            .padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(text, color = TEXT, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        }
        if (menuOpen) {
            Popup(popupPositionProvider = AnchorBelowProvider(anchor),
                onDismissRequest = { menuOpen = false }, properties = PopupProperties(focusable = true)) {
                Column(Modifier.width(200.dp).background(Color.White, RoundedCornerShape(6.dp))
                    .border(1.dp, MENU_SEPARATOR, RoundedCornerShape(6.dp)).padding(bottom = 2.dp)) {
                    MenuRow("Pin to templates") { menuOpen = false; onPin() }
                }
            }
        }
    }
}

@Composable
private fun PinnedChip(text: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)
        .background(if (hovered) PINNED_COLOR.copy(alpha = 0.8f) else PINNED_COLOR, RoundedCornerShape(6.dp))
        .hoverable(interaction).clickable { onClick() }.padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(text, color = TEXT, fontSize = 12.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
        Box(Modifier.size(16.dp).clickable { Substrate.unpinTemplate(text) },
            contentAlignment = Alignment.Center) {
            Text("×", color = TEXT.copy(alpha = 0.7f), fontSize = 12.sp)
        }
    }
}

/** Свёрнутая правая панель. Заголовок повёрнут на +90° и не переносится. */
@Composable
private fun ActionsStrip() {
    Box(
        Modifier.width(STRIP_WIDTH).fillMaxHeight()
            .background(Color.White, RoundedCornerShape(12.dp))
            .clickable { Substrate.actionsCollapsed = false },
        contentAlignment = Alignment.Center
    ) {
        Text("Actions", color = TEXT, fontSize = 12.sp,
            maxLines = 1,
            modifier = Modifier.rotate(90f).requiredWidth(120.dp))
    }
}

@Composable
private fun LogPanel(modifier: Modifier) {
    Column(modifier.background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Log", color = TEXT, fontSize = 18.sp)
            Spacer(Modifier.weight(1f))
            SmallIconButton("🗑") { Substrate.clearLog() }
            Spacer(Modifier.width(6.dp))
            SmallIconButton("📋") { copyToClipboard(Substrate.logLines.joinToString("\n")) }
        }
        Spacer(Modifier.height(8.dp))
        LogList(Modifier.fillMaxSize())
    }
}

@Composable
private fun LogList(modifier: Modifier) {
    val listState = rememberLazyListState()
    Box(modifier) {
        SelectionContainer {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(end = 12.dp)) {
                itemsIndexed(Substrate.logEntries) { idx, entry -> LogRow(entry, idx) }
            }
        }
        VerticalScrollbar(adapter = rememberScrollbarAdapter(listState),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight())
    }
}

@Composable
private fun LogRow(entry: LogEntry, idx: Int) {
    var menuOpen by remember { mutableStateOf(false) }
    var anchor by remember { mutableStateOf<Rect?>(null) }
    val isError = entry.type == "error"
    val isFinish = entry.text.startsWith("✓")
    val bg = when {
        isError -> ERROR_BADGE
        idx == 0 -> NEW_LOG
        idx % 2 == 1 -> ZEBRA
        else -> Color.Transparent
    }
    Box {
        Row(Modifier.fillMaxWidth().background(bg).padding(vertical = 1.dp)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Press) {
                            if (event.buttons.isSecondaryPressed) menuOpen = true
                            else {
                                Substrate.status = Status.PAUSED
                                entry.lineNumber?.let { Substrate.currentStep = it }
                            }
                        }
                    }
                }
            }.onGloballyPositioned { anchor = it.boundsInWindow() },
            verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(logColor(entry.type), RoundedCornerShape(2.dp)))
            Spacer(Modifier.width(6.dp))
            Text(entry.text,
                color = if (isError) ERROR_TEXT else if (isFinish) FINISH_COLOR else TEXT,
                fontSize = 13.sp, lineHeight = 18.sp)
        }
        if (menuOpen) {
            Popup(popupPositionProvider = AnchorBelowProvider(anchor),
                onDismissRequest = { menuOpen = false }, properties = PopupProperties(focusable = true)) {
                Column(Modifier.width(200.dp).background(Color.White, RoundedCornerShape(6.dp))
                    .border(1.dp, MENU_SEPARATOR, RoundedCornerShape(6.dp)).padding(bottom = 2.dp)) {
                    MenuRow("Copy line") { menuOpen = false; copyToClipboard(entry.text) }
                }
            }
        }
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

private fun copyToClipboard(text: String) {
    try { val sel = StringSelection(text); Toolkit.getDefaultToolkit().systemClipboard.setContents(sel, sel) }
    catch (e: Exception) { Substrate.log("Copy failed: ${e.message}") }
}

@Composable
private fun TabsPicker(app: MainScreenApp, sys: MainScreenSystem, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
            Column(Modifier.width(600.dp).heightIn(max = 640.dp).padding(16.dp)) {
                Text("Open", color = TEXT, fontSize = 18.sp)
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
                Text("SAMPLES (${Substrate.completedSamples.size}/${SAMPLES.items.size})",
                    color = TEXT, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                    SAMPLES.items.forEach { item ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                .background(if (item.id in Substrate.completedSamples) Color(0xFFE6F4EA) else MENU_BG,
                                    RoundedCornerShape(8.dp))
                                .clickable {
                                    app.openSamplesTab(item); onDismiss()
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (item.id in Substrate.completedSamples) {
                                Text("✓", color = Color(0xFF2E7D32), fontSize = 16.sp)
                                Spacer(Modifier.width(8.dp))
                            }
                            Column(Modifier.weight(1f)) {
                                Text(item.title, color = TEXT, fontSize = 14.sp)
                                Spacer(Modifier.height(2.dp))
                                Text(item.code.lines().take(2).joinToString("\n"),
                                    color = TEXT.copy(alpha = 0.55f), fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace)
                            }
                            Text("→", color = ACCENT, fontSize = 18.sp)
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
                Text("Find / Replace", color = TEXT, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                BasicTextField(query, { query = it }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFF7F8FA), RoundedCornerShape(6.dp)).padding(6.dp),
                    textStyle = TextStyle(color = TEXT, fontFamily = FontFamily.Monospace, fontSize = 13.sp))
                Spacer(Modifier.height(6.dp))
                BasicTextField(replace, { replace = it }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFF7F8FA), RoundedCornerShape(6.dp)).padding(6.dp),
                    textStyle = TextStyle(color = TEXT, fontFamily = FontFamily.Monospace, fontSize = 13.sp))
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
                    Text("${cursor + 1} of ${matches.size}", color = TEXT.copy(alpha = 0.6f), fontSize = 12.sp)
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
                Text(Substrate.docTitle, color = TEXT, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                SelectionContainer {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        Text(Substrate.docContent, color = TEXT, fontSize = 13.sp, lineHeight = 18.sp)
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
                Text("Settings", color = TEXT, fontSize = 18.sp)
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
            Text("Auto-update declarations", color = TEXT, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Checkbox(checked = Substrate.autoSyncDeclarations, onCheckedChange = { Substrate.autoSyncDeclarations = it })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Strict Mode (no ':' functions)", color = TEXT, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Checkbox(checked = Substrate.strictMode, onCheckedChange = {
                Substrate.strictMode = it; app.validateCurrent()
            })
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
            ActionButton("Reset panel widths") {
                Substrate.valuesWidthOverride = null
                Substrate.actionsWidthOverride = null
            }
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
                textStyle = TextStyle(color = TEXT, fontFamily = FontFamily.Monospace, fontSize = 12.sp))
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
                        textStyle = TextStyle(color = TEXT, fontFamily = FontFamily.Monospace, fontSize = 12.sp))
                    Spacer(Modifier.width(6.dp))
                    ColorPicker(group.color) { Substrate.setGroupColor(gi, it) }
                    Spacer(Modifier.weight(1f))
                    SmallIconButton("×") { Substrate.removeGroup(gi) }
                }
                Spacer(Modifier.height(4.dp))
                group.templates.forEach { t ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(t, color = TEXT, fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f))
                        SmallIconButton("×") { Substrate.removeTemplate(gi, t) }
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(value = newTemplate, onValueChange = { newTemplate = it }, singleLine = true,
                        modifier = Modifier.weight(1f).background(Color.White, RoundedCornerShape(6.dp)).padding(6.dp),
                        textStyle = TextStyle(color = TEXT, fontFamily = FontFamily.Monospace, fontSize = 12.sp))
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
                textStyle = TextStyle(color = TEXT, fontFamily = FontFamily.Monospace, fontSize = 12.sp))
            Spacer(Modifier.width(6.dp))
            ActionButton("Save current set") { Substrate.saveTemplateSet(newName); newName = "" }
            Spacer(Modifier.width(6.dp))
            ActionButton("Load default") { Substrate.resetTemplatesToDefault() }
        }
        Spacer(Modifier.height(8.dp))
        Substrate.savedTemplateSets.keys.forEach { name ->
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(name, color = TEXT, fontSize = 13.sp, modifier = Modifier.weight(1f))
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
        Text(label, color = TEXT, fontSize = 13.sp, modifier = Modifier.weight(1f))
        BasicTextField(value = value, onValueChange = onChange, singleLine = true,
            modifier = Modifier.width(80.dp).background(Color(0xFFF7F8FA), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            textStyle = TextStyle(color = TEXT, fontFamily = FontFamily.Monospace, fontSize = 13.sp))
    }
}

@Composable
private fun SaveStartDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text("Save as start state?", color = TEXT) },
        text = { Text("This will replace the current start state with the current code. Reset will return to this new state. Continue?", color = TEXT) },
        confirmButton = { ActionButton("Save as start") { onConfirm() } },
        dismissButton = { ActionButton("Cancel") { onDismiss() } })
}

@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text("About Simple True Interpreter", color = TEXT) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Version: ${Substrate.version} (${Substrate.platform})", color = TEXT, fontSize = 13.sp)
                Text("Language: ${Substrate.languageVersion}", color = TEXT, fontSize = 13.sp)
                Text("Developed by Александр Кундрюков", color = TEXT, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                Text("Links:", color = TEXT, fontSize = 13.sp)
                Text("README: ${Substrate.GITHUB_README_URL}", color = ACCENT, fontSize = 12.sp)
                Text("Email: a.kundryukov@gmail.com", color = ACCENT, fontSize = 12.sp)
                Text("Telegram: @e16din", color = ACCENT, fontSize = 12.sp)
                Text("MAX: ${Substrate.MAX_URL}", color = ACCENT, fontSize = 12.sp)
            }
        },
        confirmButton = { ActionButton("Close") { onDismiss() } })
}

@Composable
private fun Placeholder(label: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(label, color = TEXT.copy(alpha = 0.6f))
    }
}

@Composable
private fun ActionButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick) { Text(label, color = Color.White) }
}