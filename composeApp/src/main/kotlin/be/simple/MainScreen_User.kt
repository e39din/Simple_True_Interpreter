package be.simple

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
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
private val CONST_COLOR = Color(0xFFE8D5A8)
private val ERROR_BADGE = Color(0xFFFFCDD2)
private val ERROR_TEXT = Color(0xFFB71C1C)
private val RESOLVED_BADGE = Color(0xFFC8E6C9)
private val RESOLVED_TEXT = Color(0xFF2E7D32)
private val FINISH_COLOR = Color(0xFF2E7D32)
private val STRICT_BADGE = Color(0xFFFFF59D)
private val DESTROY_COLOR = Color(0xFF9C27B0)
private val DESTROY_BG = Color(0xFFF3E5F5)
private val MESSAGE_BG = Color(0xFFFFF3E0)
private val MESSAGE_FG = Color(0xFF6D4C00)
private val MESSAGE_CHIP_BG = Color(0xFFFFE0B2)

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
private val COLUMN_GUTTER = 8.dp

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
    }

    DisposableEffect(Unit) {
        onDispose {
            sys.saveSettings()
            sys.stopAllAgents()
            sys.stopAutoSave()
        }
    }

    LaunchedEffect(Substrate.copiedValueText) {
        if (Substrate.copiedValueText != null) { delay(1200); Substrate.copiedValueText = null }
    }
    LaunchedEffect(Substrate.copiedActionText) {
        if (Substrate.copiedActionText != null) { delay(1200); Substrate.copiedActionText = null }
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(BG).onPreviewKeyEvent { ev -> handleHotkey(ev, app) }) {
        val phi = 1.618f
        val total = maxWidth
        val logH = Substrate.logHeightOverride ?: (maxHeight * 0.382f)
        val valuesW = Substrate.valuesWidthOverride ?: (total / (phi + 1)).coerceIn(VALUES_MIN_WIDTH, VALUES_MAX_WIDTH)
        val actionsW = Substrate.actionsWidthOverride ?: (total / (phi + 1)).coerceIn(VALUES_MIN_WIDTH, VALUES_MAX_WIDTH)

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
                    CyclesArea(app, sys, total, Modifier.fillMaxWidth().fillMaxHeight())
                }
                if (!Substrate.actionsCollapsed) {
                    DragSplitter(vertical = true) { delta ->
                        val current = Substrate.actionsWidthOverride ?: actionsW
                        val next = (current + (delta / density).dp).coerceIn(VALUES_MIN_WIDTH, VALUES_MAX_WIDTH)
                        Substrate.actionsWidthOverride = next
                    }
                    Box(Modifier.width(actionsW).fillMaxHeight()) { ActionsPanel(app, total) }
                } else {
                    Box(Modifier.fillMaxHeight(), contentAlignment = Alignment.CenterEnd) { ActionsStrip() }
                }
            }
            HorizontalSplitter { }
            Box(Modifier.fillMaxWidth().height(logH).padding(horizontal = 12.dp, vertical = 8.dp)) {
                LogPanel(Modifier.fillMaxSize())
            }
        }
    }

    if (Substrate.showSettingsDialog) SettingsDialog(app, sys, onDismiss = { Substrate.showSettingsDialog = false })
    if (Substrate.showSaveStartDialog) SaveStartDialog(
        onDismiss = { Substrate.showSaveStartDialog = false },
        onConfirm = { sys.saveAsStart(); Substrate.showSaveStartDialog = false })
    if (Substrate.showAboutDialog) AboutDialog(onDismiss = { Substrate.showAboutDialog = false })
    if (Substrate.showTabsPicker) TabsPicker(app, sys, onDismiss = { Substrate.showTabsPicker = false })
    if (Substrate.showSearch) SearchDialog(app, onDismiss = { Substrate.showSearch = false })
    if (Substrate.showDocDialog) DocDialog(onDismiss = { Substrate.showDocDialog = false })
    if (Substrate.showSamplesDialog) SamplesDialog(app, onDismiss = { Substrate.showSamplesDialog = false })
    if (Substrate.showDeleteFileDialog) DeleteFileDialog(app, sys, onDismiss = { Substrate.showDeleteFileDialog = false })

    if (Substrate.pendingRenameFile != null) {
        RenameFileDialog(
            fileName = Substrate.pendingRenameFile!!,
            onDismiss = { Substrate.pendingRenameFile = null },
            onConfirm = { newName ->
                sys.renameFile(Substrate.pendingRenameFile!!, newName)
                Substrate.pendingRenameFile = null
            }
        )
    }
}

private fun handleHotkey(ev: KeyEvent, app: MainScreenApp): Boolean {
    if (ev.type != KeyEventType.KeyDown) return false
    return when {
        ev.isCtrlPressed && ev.key == Key.Z -> { if (ev.isShiftPressed) app.redo() else app.undo(); true }
        ev.isCtrlPressed && ev.key == Key.F -> { Substrate.showSearch = true; Substrate.searchMode = "find"; true }
        ev.isCtrlPressed && ev.key == Key.R -> { Substrate.showSearch = true; Substrate.searchMode = "replace"; true }
        ev.isCtrlPressed && ev.key == Key.P -> { app.togglePlayPause(); true }
        ev.isCtrlPressed && ev.key == Key.DirectionUp -> { app.stepBackAll(); true }
        ev.isCtrlPressed && ev.key == Key.DirectionDown -> { app.stepAll(); true }
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
            MenuRow("New Cycle", shortcut = "Ctrl+N") { sys.newFile() }
            MenuRow("New Agent") { sys.newAgent() }
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
            MenuRow("Exit") { sys.saveSettings(); sys.stopAllAgents(); sys.stopAutoSave() }
        }
        MenuItem("Run") {
            MenuRow("Start All", shortcut = "Ctrl+P") { sys.startAllAgents() }
            MenuRow("Pause All") { sys.pauseAllAgents() }
            MenuRow("Stop All") { sys.stopAllAgents() }
            MenuSeparator()
            MenuRow("Step All", shortcut = "Ctrl+↓") { sys.stepAllAgents() }
            MenuRow("Step Back All", shortcut = "Ctrl+↑") { sys.stepBackAllAgents() }
            MenuSeparator()
            MenuRow("Reset file") { app.reset() }
        }
        MenuItem("Settings") {
            MenuRow("Open Settings…") { Substrate.showSettingsDialog = true }
            MenuRow("Reset Values width") { Substrate.valuesWidthOverride = null }
            MenuRow("Reset Actions width") { Substrate.actionsWidthOverride = null }
            MenuRow("Reset Log height") { Substrate.logHeightOverride = null }
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
private fun ActionsPanel(app: MainScreenApp, maxWidth: Dp) {
    Column(
        Modifier.fillMaxSize()
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Actions", color = TEXT, fontSize = 16.sp)
            Spacer(Modifier.weight(1f))
            SmallIconButton("›") { Substrate.actionsCollapsed = true }
        }
        if (Substrate.copiedActionText != null) {
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxWidth().background(COPIED, RoundedCornerShape(6.dp)).padding(6.dp)) {
                Text("Copied", color = TEXT, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.height(6.dp))
        BasicTextField(
            value = Substrate.actionsSearchQuery,
            onValueChange = { Substrate.actionsSearchQuery = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().background(Color(0xFFF7F8FA), RoundedCornerShape(6.dp)).padding(6.dp),
            textStyle = TextStyle(color = TEXT, fontFamily = FontFamily.Monospace, fontSize = 11.sp),
            decorationBox = { inner ->
                if (Substrate.actionsSearchQuery.isEmpty()) {
                    Text("Search actions…", color = TEXT.copy(alpha = 0.4f), fontSize = 11.sp)
                }
                inner()
            }
        )
        Spacer(Modifier.height(6.dp))
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            val query = Substrate.actionsSearchQuery.lowercase()
            Substrate.templateGroups.forEach { group ->
                val filtered = group.templates.filter { t ->
                    query.isEmpty() || t.lowercase().contains(query) || descriptionFor(t).lowercase().contains(query)
                }
                if (filtered.isEmpty()) return@forEach
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(4.dp).height(16.dp).background(group.color, RoundedCornerShape(2.dp)))
                    Spacer(Modifier.width(6.dp))
                    Text(group.title, color = TEXT, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(2.dp))
                filtered.forEach { template ->
                    ActionRow(template, onCopy = {
                        copyToClipboard(template)
                        Substrate.copiedActionText = template
                    }, onPin = { Substrate.pinTemplate(template) })
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun ActionRow(template: String, onCopy: () -> Unit, onPin: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    var flash by remember { mutableStateOf(false) }
    LaunchedEffect(flash) { if (flash) { delay(300); flash = false } }
    val showCopied = Substrate.copiedActionText == template
    Column(
        Modifier.fillMaxWidth()
            .background(if (flash) COPIED else if (hovered) MENU_HOVER else Color.Transparent, RoundedCornerShape(4.dp))
            .hoverable(interaction)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Press) {
                            if (event.buttons.isSecondaryPressed) onPin()
                            else { onCopy(); flash = true }
                        }
                    }
                }
            }
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        if (showCopied) {
            Text("Copied", color = TEXT, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        } else {
            Text(template, color = TEXT, fontSize = 11.sp, fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        val desc = descriptionFor(template)
        if (desc.isNotEmpty()) {
            Text(desc, color = TEXT.copy(alpha = 0.55f), fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun descriptionFor(template: String): String = when {
    template.contains("=> :math:sum") || template.contains("=> :math:min") ||
            template.contains("=> :math:max") || template.contains("=> :math:avg") ||
            template.contains("=> :math:product") || template.contains("=> :math:count") ||
            template.contains("=> :math:sort") -> "Lists (numbers)"
    template.contains("=> :sum") || template.contains("=> :min") ||
            template.contains("=> :max") || template.contains("=> :avg") ||
            template.contains("=> :product") || template.contains("=> :count") -> "Lists (numbers)"
    template.contains("=> :math") -> "Arithmetic / bitwise / boolean"
    template.contains("=> :data:substring") -> "Substring"
    template.contains("=> :data:indexOf") -> "Index of substring"
    template.contains("=> :data:replace") -> "Replace substring"
    template.contains("=> :data:lower") -> "Lowercase"
    template.contains("=> :data:upper") -> "Uppercase"
    template.contains("=> :data:trim") -> "Trim whitespace"
    template.contains("=> :data:chars") -> "Split to chars"
    template.contains("=> :data:charAt") -> "Char by index"
    template.contains("=> :data:size") -> "Size by axis"
    template.contains("=> :data:reverse") -> "Reverse"
    template.contains("=> :data:unique") -> "Unique"
    template.contains("=> :data:contains") -> "Contains"
    template.contains("=> :data:slice") -> "Slice"
    template.contains("=> :data:join") -> "Join"
    template.contains("=> :data:push") -> "Push to list"
    template.contains("=> :data:pop") -> "Pop from list"
    template.contains("=> :data:remove") -> "Remove by index"
    template.contains("=> :data:swap") -> "Swap"
    template.contains("=> :data:deleteRange") -> "Delete range"
    template.contains("=> :data:replaceRange") -> "Replace range"
    template.contains("=> :data:sort") -> "Sort"
    template.contains("=> :push") -> "Push to list"
    template.contains("=> :pop") -> "Pop from list"
    template.contains("=> :peek") -> "Peek top"
    template.contains("=> :size") -> "Size by axis"
    template.contains("=> :s") -> "Size (short)"
    template.contains("=> :sort") -> "Sort"
    template.contains("=> :lower") -> "Lowercase"
    template.contains("=> :upper") -> "Uppercase"
    template.contains("=> :trim") -> "Trim whitespace"
    template.contains("=> :reverse") -> "Reverse"
    template.contains("=> :contains") -> "Contains"
    template.contains("=> :filter") -> "Filter"
    template.contains("=> :matches") -> "Regex match"
    template.contains("=> :map") -> "Map (functional)"
    template.contains("=> :reduce") -> "Reduce (functional)"
    template.contains("=> :fold") -> "Fold (functional)"
    template.contains("=> :each") -> "Each (functional)"
    template.contains("=> :range") -> "Range of numbers"
    template.contains("=> :repeat") -> "Repeat action"
    template.contains("=> :json:get") -> "JSON: get"
    template.contains("=> :json:set") -> "JSON: set"
    template.contains("=> :json:names") -> "JSON: names"
    template.contains("=> :json:values") -> "JSON: values"
    template.contains("=> :file:data") -> "File: read/write"
    template.contains("=> :file:origin") -> "File: origin"
    template.contains("=> :file:lines") -> "File: lines"
    template.contains("=> :file:create") -> "File: create"
    template.contains("=> :file:read") -> "File: read"
    template.contains("=> :file:update") -> "File: update"
    template.contains("=> :file:delete") -> "File: delete"
    template.contains("=> :file:list") -> "File: list"
    template.contains("=> :file:exists") -> "File: exists"
    template.contains("=> :path") -> "File path"
    template.contains("=> :console") -> "OS command"
    template.contains("=> :run") -> "Run string"
    template.contains("=> :save") -> "Save"
    template.contains("=> :load") -> "Load"
    template.contains("=> :api:get") -> "API: GET"
    template.contains("=> :api:post") -> "API: POST"
    template.contains("=> :api:put") -> "API: PUT"
    template.contains("=> :api:delete") -> "API: DELETE"
    template.contains("=> :api:patch") -> "API: PATCH"
    template.contains("=> :api:websocket") -> "API: WebSocket"
    template.contains("=> :log") -> "Log"
    template.contains("=> :message") -> "User message"
    template.contains("=> :result") -> "Function result"
    template.contains("=> :error:resolve") -> "Resolve errors"
    template.contains("=> :error") -> "Error stack"
    template.contains("=> :source:function") -> "Local call"
    template.contains("=> :test!") -> "Async tab call"
    template.contains("=> :test") -> "Tab call"
    template == ":run" -> "Run string"
    template == ":log" -> "Log string"
    template.contains("^ :stop") -> "Stop agent"
    template.contains("^ :pause") -> "Pause agent"
    template.contains("^ :resume") -> "Resume agent"
    template.contains("^ :delete") -> "Delete agent"
    template.contains("^ :reset") -> "Reset file"
    template.contains(":agent:") -> "Agent property"
    template.contains(":agent:status") -> "All agent statuses"
    template.contains("=> :activeTab") -> "Switch tab"
    template.contains("-> nothing") -> "Destroy"
    template.contains("=> file:") -> "Copy to file"
    template.contains("-> file:") -> "Move to file"
    template.contains("=> file:b") -> "Copy file"
    template.contains("-> file:b") -> "Move file"
    template.contains("=> var") -> "File → var"
    template.contains("=>|") -> "Insert (copy)"
    template.contains("->|") -> "Insert (move)"
    template.contains("^ ") -> "Jump"
    template.contains("->") -> "Move"
    template.contains("=>") -> "Copy / call"
    template.contains("?") -> "Condition"
    template.contains(":splitter") -> "Axis splitter"
    template.contains("=") -> "Naming"
    template.startsWith("\\") -> "Literal"
    else -> ""
}

@Composable
private fun ActionsStrip() {
    Box(
        Modifier.width(STRIP_WIDTH).fillMaxHeight()
            .background(Color.White, RoundedCornerShape(12.dp))
            .clickable { Substrate.actionsCollapsed = false },
        contentAlignment = Alignment.Center
    ) {
        Text("Actions", color = TEXT, fontSize = 12.sp, maxLines = 1,
            modifier = Modifier.rotate(90f).requiredWidth(120.dp))
    }
}

// ---------- Cycles ----------

@Composable
private fun CyclesArea(app: MainScreenApp, sys: MainScreenSystem, maxWidth: Dp, modifier: Modifier) {
    Column(modifier.background(Color.White, RoundedCornerShape(12.dp)).padding(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Cycles", color = TEXT, fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            ActionIconButton("▶", enabled = AgentsModel.agents.isNotEmpty()) { sys.startAllAgents() }
            ActionIconButton("⏸", enabled = AgentsModel.agents.isNotEmpty()) { sys.pauseAllAgents() }
            ActionIconButton("⏹", enabled = AgentsModel.agents.isNotEmpty()) { sys.stopAllAgents() }
            ActionIconButton("⏭", enabled = AgentsModel.agents.isNotEmpty()) { sys.stepAllAgents() }
            ActionIconButton("⏮", enabled = AgentsModel.agents.isNotEmpty()) { sys.stepBackAllAgents() }
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
            CyclesGrid(app, sys, maxWidth)
        }
    }
}

@Composable
private fun CyclesGrid(app: MainScreenApp, sys: MainScreenSystem, maxWidth: Dp) {
    val vScroll = rememberScrollState()
    val hScroll = rememberScrollState()
    val density = LocalDensity.current

    val ordered = buildList {
        for (name in Substrate.fileOrder) {
            TabsModel.byFileName(name)?.let { add(it) }
        }
        for (tab in TabsModel.tabs) {
            if (none { it.fileName == tab.fileName }) add(tab)
        }
    }
    if (ordered.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("(no files)", color = TEXT.copy(alpha = 0.5f))
        }
        return
    }

    val colW = with(density) { COLUMN_MIN_WIDTH.toPx() + COLUMN_GUTTER.toPx() }
    val maxW = with(density) { maxWidth.toPx() }
    val perRow = ((maxW / colW).toInt()).coerceAtLeast(1)

    val rows = ordered.chunked(perRow)

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(vScroll)) {
            rows.forEach { row ->
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(hScroll),
                    horizontalArrangement = Arrangement.spacedBy(COLUMN_GUTTER),
                    verticalAlignment = Alignment.Top
                ) {
                    row.forEach { tab ->
                        val measured = measureMaxLineWidth(tab.code)
                        val w = measured.coerceIn(COLUMN_MIN_WIDTH, maxWidth / 2)
                        Box(Modifier.width(w).fillMaxHeight()) {
                            FileColumn(app, sys, tab, expanded = false, Modifier.fillMaxSize())
                        }
                    }
                }
                Spacer(Modifier.height(COLUMN_GUTTER))
            }
        }
        if (rows.size > 1) {
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(vScroll),
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
            )
        }
        if (ordered.size > perRow) {
            HorizontalScrollbar(
                adapter = rememberScrollbarAdapter(hScroll),
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
            if (tab.kind == FileKind.AGENT && agent != null) {
                Box(Modifier.size(10.dp).background(agent.color, RoundedCornerShape(2.dp)))
                Spacer(Modifier.width(6.dp))
                Text(agent.name, color = TEXT, fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false))
            } else {
                Text(tab.fileName, color = TEXT, fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false))
            }
            Spacer(Modifier.weight(1f))
            ActionIconButton(if (expanded) "⤡" else "⛶") {
                Substrate.expandedFileName = if (expanded) null else tab.fileName
                Substrate.activeFileName = tab.fileName
                Substrate.viewingAgentId = agent?.id
            }
        }
        Spacer(Modifier.height(4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            val idx = Substrate.fileOrder.indexOf(tab.fileName)
            val canUp = idx > 0
            val canDown = idx >= 0 && idx < Substrate.fileOrder.size - 1
            ActionIconButton("⟲") { app.resetFile(tab.fileName) }
            ActionIconButton("↑", enabled = canUp) { sys.moveFileUp(tab.fileName) }
            ActionIconButton("↓", enabled = canDown) { sys.moveFileDown(tab.fileName) }
            ActionIconButton("✎") { Substrate.pendingRenameFile = tab.fileName }
            ActionIconButton("🗑") {
                Substrate.pendingDeleteFile = tab.fileName
                Substrate.showDeleteFileDialog = true
            }
            Spacer(Modifier.weight(1f))
            Box(
                Modifier.height(22.dp).background(MENU_BG, RoundedCornerShape(6.dp))
                    .clickable { app.setActiveFile(tab.fileName) }
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(if (tab.kind == FileKind.AGENT) "agent" else if (tab.kind == FileKind.CYCLE) "cycle" else "file",
                    color = TEXT.copy(alpha = 0.6f), fontSize = 10.sp)
            }
        }
        Spacer(Modifier.height(4.dp))

        Box(Modifier.fillMaxWidth().weight(1f).background(Color.White, RoundedCornerShape(6.dp)).padding(4.dp)) {
            StripedCodeField(app, tab, agent)
        }

        if (tab.kind == FileKind.AGENT && agent != null) {
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                ActionIconButton("▶", enabled = agent.status != Status.RUNNING,
                    highlighted = agent.lastPressed == "play") {
                    sys.startAgent(agent.id); agent.lastPressed = "play"
                }
                ActionIconButton("⏸", enabled = agent.status == Status.RUNNING,
                    highlighted = agent.lastPressed == "pause") {
                    sys.pauseAgent(agent.id); agent.lastPressed = "pause"
                }
                ActionIconButton("⏹", enabled = true,
                    highlighted = agent.lastPressed == "stop") {
                    sys.stopAgent(agent.id); agent.lastPressed = "stop"
                }
                ActionIconButton("⏭", enabled = agent.status != Status.RUNNING,
                    highlighted = false) {
                    sys.stepAgent(agent.id)
                }
                ActionIconButton("⏮", enabled = agent.status != Status.RUNNING,
                    highlighted = false) {
                    sys.stepBackAgent(agent.id)
                }
                Spacer(Modifier.weight(1f))
                Text("step ${agent.currentStep}", color = TEXT.copy(alpha = 0.6f), fontSize = 10.sp)
                Spacer(Modifier.width(4.dp))
                Text(agentStateLabel(agent.status), color = agentStateFg(agent.status), fontSize = 10.sp)
                if (agent.waiter) {
                    Spacer(Modifier.width(4.dp))
                    Box(Modifier.background(Color(0xFFE3F2FD), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)) {
                        Text("waiter", color = Color(0xFF1565C0), fontSize = 9.sp)
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
    val dotW = if (tab.kind == FileKind.AGENT) 10.dp else 0.dp
    val chipWidth = (measuredWidth + dotW + CHIP_PADDING_H * 2 + CLOSE_BTN_WIDTH).coerceAtLeast(CHIP_MIN_WIDTH)

    val bg = when {
        active -> ACCENT
        tab.kind == FileKind.AGENT && agent?.status == Status.RUNNING -> FINISH_COLOR
        else -> MENU_BG
    }
    val fg = if (active || (tab.kind == FileKind.AGENT && agent?.status == Status.RUNNING)) Color.White else TEXT

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
        if (tab.kind == FileKind.AGENT && agent != null) {
            val dotAlpha = if (agent.status == Status.STOPPED) 0.5f else 1f
            Box(Modifier.size(8.dp).background(agent.color.copy(alpha = dotAlpha), CircleShape))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, color = fg, fontSize = 11.sp, fontFamily = FontFamily.Monospace,
            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
        if (agent?.waiter == true) {
            Spacer(Modifier.width(2.dp))
            Text("W", color = fg.copy(alpha = 0.75f), fontSize = 9.sp)
        }
        Spacer(Modifier.width(2.dp))
        Box(Modifier.size(CLOSE_BTN_WIDTH).clickable { onClose() }, contentAlignment = Alignment.Center) {
            Text("×", color = fg.copy(alpha = 0.75f), fontSize = 12.sp)
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
        w + COLUMN_GUTTER
    }
}

// ---------- Code editor ----------

@Composable
private fun StripedCodeField(app: MainScreenApp, tab: TabState, agent: AgentState?) {
    var tfv by remember(tab.id) { mutableStateOf(TextFieldValue(tab.code, TextRange(tab.cursorPosition))) }
    var ctrlHeld by remember { mutableStateOf(false) }
    LaunchedEffect(tab.code) {
        if (tfv.text != tab.code) {
            val pos = tab.cursorPosition.coerceIn(0, tab.code.length)
            tfv = tfv.copy(text = tab.code, selection = TextRange(pos))
        }
    }
    val currentLine = if (agent != null && agent.id == Substrate.viewingAgentId) agent.currentStep else -1
    val statusColor = if (agent != null && agent.id == Substrate.viewingAgentId) agentStateColor(agent.status) else Color.Transparent
    val agentColor = if (agent != null && agent.id == Substrate.viewingAgentId) agent.color else null
    val transformation = remember(currentLine, statusColor, agentColor, Substrate.searchMatches, Substrate.searchCurrent) {
        LineHighlighter(currentLine, statusColor, agentColor, Substrate.searchMatches, Substrate.searchCurrent)
    }
    Box(Modifier.fillMaxSize()) {
        CurrentLineBackground(
            currentLine = currentLine,
            statusColor = statusColor,
            code = tab.code
        )
        SelectionContainer {
            BasicTextField(
                value = tfv,
                onValueChange = { nv ->
                    tfv = nv
                    app.updateCode(nv.text, forceRename = ctrlHeld)
                    app.updateCursor(nv.selection.start)
                },
                modifier = Modifier.fillMaxSize()
                    .onPreviewKeyEvent { ev ->
                        if (ev.type == KeyEventType.KeyDown && ev.key == Key.Tab) {
                            val pos = tfv.selection.start
                            val newText = tfv.text.substring(0, pos) + "  " + tfv.text.substring(pos)
                            tfv = tfv.copy(text = newText, selection = TextRange(pos + 2))
                            app.updateCode(newText, forceRename = ctrlHeld); app.updateCursor(pos + 2)
                            return@onPreviewKeyEvent true
                        }
                        if (ev.key == Key.CtrlLeft || ev.key == Key.CtrlRight) {
                            ctrlHeld = ev.type == KeyEventType.KeyDown
                        }
                        false
                    },
                textStyle = TextStyle(color = TEXT, fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                visualTransformation = transformation
            )
        }
    }
}

@Composable
private fun CurrentLineBackground(currentLine: Int, statusColor: Color, code: String) {
    if (currentLine <= 0) return
    Canvas(Modifier.fillMaxSize()) {
        val lineHeightPx = 16.sp.toPx() * 1.0f
        val top = (currentLine - 1) * lineHeightPx
        val color = if (statusColor != Color.Transparent) statusColor else Color(0x224A90E2)
        drawRect(color = color, topLeft = Offset(0f, top), size = Size(size.width, lineHeightPx))
    }
}

private class LineHighlighter(
    private val currentLine: Int,
    private val statusColor: Color,
    private val agentColor: Color?,
    private val searchMatches: List<Int>,
    private val searchCurrent: Int
) : VisualTransformation {

    private var lastCode: String? = null
    private var lastLine: Int = -1
    private var lastStatusColor: Color? = null
    private var lastAgentColor: Color? = null
    private var lastErrors: Set<Int> = emptySet()
    private var lastResolved: Set<Int> = emptySet()
    private var lastSearch: List<Int> = emptyList()
    private var lastSearchCurrent: Int = -1
    private var cached: AnnotatedString? = null
    private var cachedMapping: OffsetMapping? = null

    override fun filter(text: AnnotatedString): TransformedText {
        val code = text.text
        val errorLines = Substrate.errorLines
        val resolvedLines = Substrate.resolvedLines

        if (code == lastCode && currentLine == lastLine && errorLines == lastErrors &&
            resolvedLines == lastResolved &&
            statusColor == lastStatusColor && agentColor == lastAgentColor &&
            searchMatches == lastSearch && searchCurrent == lastSearchCurrent &&
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

                if (idx % 2 == 1) addStyle(SpanStyle(background = ZEBRA), numStart, textEnd)

                if (idx + 1 in errorLines) {
                    addStyle(SpanStyle(background = ERROR_BADGE, textDecoration = TextDecoration.Underline, color = ERROR_TEXT), numStart, textEnd)
                } else if (idx + 1 in resolvedLines) {
                    addStyle(SpanStyle(background = RESOLVED_BADGE, color = RESOLVED_TEXT), numStart, textEnd)
                }

                val lineMatch = searchMatches.contains(idx + 1)
                if (lineMatch) {
                    val bg = if (searchCurrent == idx + 1) Color(0xFFFFCC80) else Color(0xFFFFF59D)
                    addStyle(SpanStyle(background = bg), numStart, textEnd)
                }

                val destroyMatch = Regex("->\\s*nothing").findAll(line)
                for (m in destroyMatch) {
                    val from = textStart + m.range.first
                    val to = textStart + m.range.last + 1
                    if (from >= 0 && to <= length) {
                        addStyle(SpanStyle(background = DESTROY_BG, color = DESTROY_COLOR, textDecoration = TextDecoration.Underline), from, to)
                    }
                }

                TrueHighlighter.apply(this, line, textStart)

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

        lastCode = code; lastLine = currentLine; lastErrors = errorLines; lastResolved = resolvedLines
        lastStatusColor = statusColor; lastAgentColor = agentColor
        lastSearch = searchMatches; lastSearchCurrent = searchCurrent
        cached = annotated; cachedMapping = mapping
        return TransformedText(annotated, mapping)
    }
}

// ---------- Buttons ----------

@Composable
private fun ActionIconButton(glyph: String, enabled: Boolean = true, highlighted: Boolean = false, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val bg = when { !enabled -> BTN_BG_DISABLED; hovered -> MENU_HOVER; else -> BTN_BG }
    val fg = if (enabled) BTN_FG else BTN_FG_DISABLED
    val borderMod = if (highlighted) Modifier.border(2.dp, ACCENT, RoundedCornerShape(6.dp)) else Modifier
    Box(Modifier.size(24.dp).background(bg, RoundedCornerShape(6.dp)).then(borderMod)
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
private fun ActionButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick) { Text(label, color = Color.White, fontSize = 12.sp) }
}

// ---------- Values Panel ----------

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
                    Text("(no containers)", color = TEXT.copy(alpha = 0.5f))
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

// ---------- Log Panel ----------

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
        "Strings" to "string", "Messages" to "message", "Destroy" to "destroy"
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
    val isMessage = type == "message"
    val bg = when {
        selected && isError -> ERROR_BADGE
        selected && isDestroy -> DESTROY_BG
        selected && isMessage -> MESSAGE_CHIP_BG
        selected -> ACCENT
        isError && count > 0 -> ERROR_BADGE.copy(alpha = 0.5f)
        isDestroy && count > 0 -> DESTROY_BG.copy(alpha = 0.5f)
        isMessage && count > 0 -> MESSAGE_CHIP_BG.copy(alpha = 0.5f)
        else -> MENU_BG
    }
    val fg = when {
        selected && isError -> ERROR_TEXT
        selected && isDestroy -> DESTROY_COLOR
        selected && isMessage -> MESSAGE_FG
        selected -> Color.White
        isError && count > 0 -> ERROR_TEXT
        isDestroy && count > 0 -> DESTROY_COLOR
        isMessage && count > 0 -> MESSAGE_FG
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
private fun LogRow(entry: be.`true`.LogEntry, idx: Int) {
    val isError = entry.type == "error" && !entry.resolved
    val isResolved = entry.type == "error" && entry.resolved
    val isDestroy = entry.type == "destroy"
    val isMessage = entry.type == "message"
    val isFinish = entry.text.startsWith("✓")
    val isAgent = entry.type.startsWith("agent:")
    val agentName = if (isAgent) entry.type.removePrefix("agent:") else null
    val agentColor = agentName?.let { name -> AgentsModel.agents.firstOrNull { it.name == name }?.color }

    val bg = when {
        isError -> ERROR_BADGE
        isResolved -> RESOLVED_BADGE
        isDestroy -> DESTROY_BG
        isMessage -> MESSAGE_BG
        idx == 0 -> Color(0xFFE6F4EA)
        idx % 2 == 1 -> ZEBRA
        else -> Color.Transparent
    }
    val fg = when {
        isError -> ERROR_TEXT
        isResolved -> RESOLVED_TEXT
        isDestroy -> DESTROY_COLOR
        isMessage -> MESSAGE_FG
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

// ---------- Search Dialog ----------

@Composable
private fun SearchDialog(app: MainScreenApp, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf(Substrate.searchQuery) }
    var replace by remember { mutableStateOf(Substrate.replaceQuery) }
    val isReplace = Substrate.searchMode == "replace"

    fun findAll() {
        val code = TabsModel.active()?.code ?: ""
        val list = mutableListOf<Int>()
        if (query.isNotEmpty()) {
            var idx = code.indexOf(query)
            while (idx >= 0) {
                val line = code.substring(0, idx).count { it == '\n' } + 1
                if (line !in list) list += line
                idx = code.indexOf(query, idx + 1)
            }
        }
        Substrate.searchMatches = list
        Substrate.searchCurrent = 0
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
            Column(Modifier.width(520.dp).padding(16.dp)) {
                Text(if (isReplace) "Find / Replace" else "Find", color = TEXT, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                BasicTextField(query, { query = it; Substrate.searchQuery = it }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFF7F8FA), RoundedCornerShape(6.dp)).padding(6.dp),
                    textStyle = TextStyle(color = TEXT, fontFamily = FontFamily.Monospace, fontSize = 12.sp))
                if (isReplace) {
                    Spacer(Modifier.height(6.dp))
                    BasicTextField(replace, { replace = it; Substrate.replaceQuery = it }, singleLine = true,
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFF7F8FA), RoundedCornerShape(6.dp)).padding(6.dp),
                        textStyle = TextStyle(color = TEXT, fontFamily = FontFamily.Monospace, fontSize = 12.sp))
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton("Find all") { findAll() }
                    ActionButton("◀") {
                        if (Substrate.searchCurrent > 0) Substrate.searchCurrent--
                    }
                    ActionButton("▶") {
                        if (Substrate.searchCurrent < Substrate.searchMatches.size - 1) Substrate.searchCurrent++
                    }
                    if (isReplace) {
                        ActionButton("Replace") {
                            val tab = TabsModel.active() ?: return@ActionButton
                            if (query.isBlank()) return@ActionButton
                            val idx = Substrate.searchMatches.getOrNull(Substrate.searchCurrent) ?: return@ActionButton
                            val lines = tab.code.lines().toMutableList()
                            if (idx - 1 in lines.indices) {
                                lines[idx - 1] = lines[idx - 1].replace(query, replace)
                                tab.code = lines.joinToString("\n")
                                tab.dirty = true
                                app.updateCode(tab.code, forceRename = true)
                            }
                            findAll()
                        }
                        ActionButton("Replace all") {
                            val tab = TabsModel.active() ?: return@ActionButton
                            if (query.isBlank()) return@ActionButton
                            tab.code = tab.code.replace(query, replace)
                            tab.dirty = true
                            app.updateCode(tab.code, forceRename = true)
                            findAll()
                        }
                    }
                    ActionButton("Close") { onDismiss() }
                }
                if (Substrate.searchMatches.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text("${Substrate.searchCurrent + 1} of ${Substrate.searchMatches.size}",
                        color = TEXT.copy(alpha = 0.6f), fontSize = 11.sp)
                }
            }
        }
    }
}

// ---------- Misc dialogs ----------

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
private fun DeleteFileDialog(app: MainScreenApp, sys: MainScreenSystem, onDismiss: () -> Unit) {
    val name = Substrate.pendingDeleteFile
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.t("dialog.delete.title"), color = TEXT) },
        text = { Text(Strings.t("dialog.delete.text").replace("{name}", name ?: ""), color = TEXT) },
        confirmButton = {
            ActionButton(Strings.t("button.delete")) {
                if (name != null) sys.deleteFileCascade(name)
                Substrate.pendingDeleteFile = null
                onDismiss()
            }
        },
        dismissButton = { ActionButton(Strings.t("button.cancel")) { Substrate.pendingDeleteFile = null; onDismiss() } }
    )
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
private fun TabsPicker(app: MainScreenApp, sys: MainScreenSystem, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
            Column(Modifier.width(600.dp).heightIn(max = 640.dp).padding(16.dp)) {
                Text("Open", color = TEXT, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton("New Cycle") { app.newTab(); onDismiss() }
                    ActionButton("New Agent") { app.newAgentTab(); onDismiss() }
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
    val agent = app.viewingAgent()
    var history by remember { mutableStateOf((agent?.interpreter?.historyLimit ?: 100).toString()) }
    var logLimit by remember { mutableStateOf((agent?.interpreter?.logLimit ?: 1000).toString()) }
    var speed by remember { mutableStateOf(Substrate.speedMs.toString()) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SettingRow("History limit", history) { history = it }
        SettingRow("Log limit", logLimit) { logLimit = it }
        SettingRow("Speed (ms)", speed) { speed = it }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Auto-update declarations", color = TEXT, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Checkbox(checked = Substrate.autoSyncDeclarations, onCheckedChange = { Substrate.autoSyncDeclarations = it })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Strict Mode (no ':' functions)", color = TEXT, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Checkbox(checked = Substrate.strictMode, onCheckedChange = { Substrate.strictMode = it; app.validateCurrent() })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Language", color = TEXT, fontSize = 12.sp, modifier = Modifier.weight(1f))
            TabChip("EN", Strings.lang == "en") { Strings.lang = "en" }
            Spacer(Modifier.width(4.dp))
            TabChip("RU", Strings.lang == "ru") { Strings.lang = "ru" }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton("Apply") {
                agent?.interpreter?.historyLimit = history.toIntOrNull()?.coerceIn(1, 1000) ?: 100
                agent?.interpreter?.logLimit = logLimit.toIntOrNull()?.coerceIn(100, 10000) ?: 1000
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
private fun TabChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) ACCENT else MENU_BG
    val fg = if (selected) Color.White else TEXT
    Box(Modifier.height(22.dp).background(bg, RoundedCornerShape(6.dp)).clickable { onClick() }
        .padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
        Text(label, color = fg, fontSize = 11.sp)
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

private fun copyToClipboard(text: String) {
    try { val sel = StringSelection(text); Toolkit.getDefaultToolkit().systemClipboard.setContents(sel, sel) }
    catch (e: Exception) { Substrate.log("Copy failed: ${e.message}") }
}

@Composable
private fun RenameFileDialog(
    fileName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(fileName) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(12.dp), color = Color.White) {
            Column(Modifier.width(400.dp).padding(16.dp)) {
                Text("Rename file", color = TEXT, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                BasicTextField(
                    value = name,
                    onValueChange = { name = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().background(Color(0xFFF7F8FA), RoundedCornerShape(6.dp)).padding(6.dp),
                    textStyle = TextStyle(color = TEXT, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionButton("Rename") { if (name.isNotBlank()) onConfirm(name) }
                    ActionButton("Cancel") { onDismiss() }
                }
            }
        }
    }
}