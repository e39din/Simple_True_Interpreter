package be.simple

import be.`true`.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class MainScreenCoreTest {

    private lateinit var app: MainScreenApp

    @BeforeTest
    fun clean() {
        TabsModel.tabs.clear()
        TabsModel.activeIndex = -1
        AgentsModel.agents.clear()
        Substrate.viewingAgentId = null
        Substrate.expandedFileName = null
        Substrate.activeFileName = ""
        Substrate.activeCode = ""
        Substrate.logFilter = "All"
        Substrate.autoSyncDeclarations = true
        Substrate.strictMode = false
        Substrate.fileOrder = emptyList()
        Substrate.clearLog()
        Substrate.errorLines = emptySet()
        Substrate.resolvedLines = emptySet()
        TerminalModel.closeAll()
        SnackbarModel.clear()
        app = MainScreenApp()
    }

    // ---------- 1. ensureMainTab / applyDefault / applyCode ----------

    @Test fun `ensureMainTab creates main cycle`() {
        app.ensureMainTab()
        assertNotNull(TabsModel.byFileName("main.cycle"))
        assertNotNull(AgentsModel.agentFor("main.cycle"))
    }

    @Test fun `applyDefault resets code and agent`() {
        app.ensureMainTab()
        app.applyDefault()
        val tab = TabsModel.active()!!
        assertEquals(Substrate.DEFAULT_CODE, tab.code)
        val agent = AgentsModel.agentFor("main.cycle")!!
        assertEquals(Status.STOPPED, agent.status)
        assertEquals(1, agent.currentStep)
    }

    @Test fun `applyCode sets code`() {
        app.ensureMainTab()
        app.applyCode("x.cycle", "1 = a\n=> :stop")
        val tab = TabsModel.active()!!
        assertEquals("x.cycle", tab.fileName)
        assertEquals("1 = a\n=> :stop", tab.code)
        assertEquals(FileKind.CYCLE, tab.kind)
    }

    @Test fun `applyCode sets agent kind`() {
        app.ensureMainTab()
        app.applyCode("x.agent", "1 = a\n=> :stop")
        val tab = TabsModel.active()!!
        assertEquals(FileKind.AGENT, tab.kind)
    }

    // ---------- 2. resetFile ----------

    @Test fun `resetFile resets code to startCode`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "modified"
        app.resetFile("main.cycle")
        assertEquals(Substrate.startCode, tab.code)
    }

    @Test fun `resetFile resets agent currentStep`() {
        app.ensureMainTab()
        val agent = AgentsModel.agentFor("main.cycle")!!
        agent.currentStep = 5
        agent.status = Status.RUNNING
        app.resetFile("main.cycle")
        assertEquals(1, agent.currentStep)
        assertEquals(Status.STOPPED, agent.status)
    }

    @Test fun `resetFile clears snapshots`() {
        app.ensureMainTab()
        val agent = AgentsModel.agentFor("main.cycle")!!
        agent.variablesSnapshot = mapOf("a" to "1")
        agent.resultsSnapshot = mapOf("r" to "2")
        app.resetFile("main.cycle")
        assertTrue(agent.variablesSnapshot.isEmpty())
        assertTrue(agent.resultsSnapshot.isEmpty())
    }

    @Test fun `resetFile clears lastPressed`() {
        app.ensureMainTab()
        val agent = AgentsModel.agentFor("main.cycle")!!
        agent.lastPressed = "play"
        app.resetFile("main.cycle")
        assertNull(agent.lastPressed)
    }

    // ---------- 3. syncVariablesFromCode ----------

    @Test fun `syncVariablesFromCode parses plain naming`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "42 = a\nhello = b"
        app.syncVariablesFromCode(force = true)
        assertEquals("42", tab.interpreter.variables["a"])
        assertEquals("hello", tab.interpreter.variables["b"])
    }

    @Test fun `syncVariablesFromCode respects const flag`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "42 = a @"
        app.syncVariablesFromCode(force = true)
        assertTrue("a" in tab.interpreter.constants)
    }

    @Test fun `syncVariablesFromCode respects hidden flag`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "42 = a *"
        app.syncVariablesFromCode(force = true)
        assertTrue("a" in tab.interpreter.hiddenVariables)
    }

    @Test fun `syncVariablesFromCode skips nothing`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "5 = nothing"
        app.syncVariablesFromCode(force = true)
        assertNull(tab.interpreter.variables["nothing"])
    }

    @Test fun `syncVariablesFromCode ignores math chains`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "a + 1 => :math"
        app.syncVariablesFromCode(force = true)
        assertFalse("math" in tab.interpreter.variables)
    }

    @Test fun `syncVariablesFromCode updates agent snapshot`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "42 = a"
        app.syncVariablesFromCode(force = true)
        val agent = AgentsModel.agentFor("main.cycle")!!
        assertEquals("42", agent.variablesSnapshot["a"])
    }

    @Test fun `syncVariablesFromCode preserves old values when not forced`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.interpreter.variables["a"] = "5"
        tab.code = "0 = a"
        app.syncVariablesFromCode(force = false)
        assertEquals("5", tab.interpreter.variables["a"])
    }

    @Test fun `syncVariablesFromCode parses color container`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "#00cc22 = color"
        app.syncVariablesFromCode(force = true)
        assertEquals("#00cc22", tab.interpreter.variables["color"])
    }

    @Test fun `syncVariablesFromCode parses speed container`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "100 = speed"
        app.syncVariablesFromCode(force = true)
        val agent = AgentsModel.agentFor("main.cycle")!!
        assertEquals(100L, agent.speedMs)
    }

    // ---------- 4. deleteVariable ----------

    @Test fun `deleteVariable removes from interpreter`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "5 = a\n10 = b"
        app.syncVariablesFromCode(force = true)
        Substrate.viewingAgentId = AgentsModel.agentFor("main.cycle")!!.id
        app.deleteVariable("a")
        assertNull(tab.interpreter.variables["a"])
        assertEquals("10", tab.interpreter.variables["b"])
    }

    @Test fun `deleteVariable removes code line`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "5 = a\n10 = b"
        app.syncVariablesFromCode(force = true)
        Substrate.viewingAgentId = AgentsModel.agentFor("main.cycle")!!.id
        app.deleteVariable("a")
        assertFalse(tab.code.contains("5 = a"))
        assertTrue(tab.code.contains("10 = b"))
    }

    @Test fun `deleteVariable updates snapshot`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "5 = a"
        app.syncVariablesFromCode(force = true)
        val agent = AgentsModel.agentFor("main.cycle")!!
        Substrate.viewingAgentId = agent.id
        app.deleteVariable("a")
        assertNull(agent.variablesSnapshot["a"])
    }

    // ---------- 5. undo / redo ----------

    @Test fun `undo restores previous code`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "v1"
        app.updateCode("v2")
        app.undo()
        assertEquals("v1", tab.code)
    }

    @Test fun `redo after undo`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "v1"
        app.updateCode("v2")
        app.undo()
        app.redo()
        assertEquals("v2", tab.code)
    }

    @Test fun `undo stack limited to 100`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "start"
        repeat(150) { app.updateCode("v$it") }
        assertTrue(tab.undoStack.size <= 100)
    }

    @Test fun `new update clears redo stack`() {
        app.ensureMainTab()
        app.updateCode("v1")
        app.undo()
        app.updateCode("v2")
        assertTrue(TabsModel.active()!!.redoStack.isEmpty())
    }

    // ---------- 6. insertAtCursor ----------

    @Test fun `insertAtCursor inserts and moves cursor`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "abc"
        tab.cursorPosition = 1
        app.insertAtCursor("XX")
        assertEquals("aXXbc", tab.code)
        assertEquals(3, tab.cursorPosition)
    }

    @Test fun `insertAtCursor clamps position`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "abc"
        tab.cursorPosition = 999
        app.insertAtCursor("Z")
        assertEquals("abcZ", tab.code)
    }

    // ---------- 7. validateCurrent ----------

    @Test fun `validateCurrent fails on unknown label`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "^ nope\n=> :stop"
        val ok = app.validateCurrent()
        assertFalse(ok)
        assertTrue(Substrate.errorLines.isNotEmpty())
    }

    @Test fun `validateCurrent succeeds on valid code`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "1 = a\n=> :stop"
        val ok = app.validateCurrent()
        assertTrue(ok)
    }

    @Test fun `validateCurrent strict mode forbids system function`() {
        app.ensureMainTab()
        Substrate.strictMode = true
        val tab = TabsModel.active()!!
        tab.code = "a => :math"
        val ok = app.validateCurrent()
        assertFalse(ok)
        Substrate.strictMode = false
    }

    @Test fun `validateCurrent strict mode allows user file name`() {
        app.ensureMainTab()
        Substrate.strictMode = true
        val tab = TabsModel.active()!!
        TabsModel.create("test.cycle", "=> :stop", makeActive = false)
        tab.code = "1 => :test"
        val ok = app.validateCurrent()
        assertTrue(ok, "user file name should be allowed in strict mode")
        Substrate.strictMode = false
    }

    // ---------- 8. viewingAgent ----------

    @Test fun `viewingAgent null if not set`() {
        app.ensureMainTab()
        Substrate.viewingAgentId = null
        assertNull(app.viewingAgent())
    }

    @Test fun `viewingAgent returns agent`() {
        app.ensureMainTab()
        val agent = AgentsModel.agentFor("main.cycle")!!
        Substrate.viewingAgentId = agent.id
        assertEquals(agent, app.viewingAgent())
    }

    @Test fun `setActiveFile sets viewing`() {
        app.ensureMainTab()
        val agent = AgentsModel.agentFor("main.cycle")!!
        app.setActiveFile("main.cycle")
        assertEquals(agent.id, Substrate.viewingAgentId)
    }

    // ---------- 9. stepAgentInternal ----------

    @Test fun `stepAgentInternal advances currentStep`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "1 = a\n2 = b\n=> :stop"
        val agent = AgentsModel.agentFor("main.cycle")!!
        val r = app.stepAgentInternal(agent.id)
        assertNotNull(r)
        assertEquals(1, r!!.currentStep)
        assertEquals("1", agent.interpreter.variables["a"])
    }

    @Test fun `stepAgentInternal returns null for unknown`() {
        assertNull(app.stepAgentInternal(999))
    }

    // ---------- 10. closeTab cascade ----------

    @Test fun `closeTab removes file`() {
        app.ensureMainTab()
        val n = TabsModel.tabs.size
        app.closeTab(0)
        assertEquals(n - 1, TabsModel.tabs.size)
    }

    // ---------- 11. publishActive ----------

    @Test fun `publishActive sets active file`() {
        app.ensureMainTab()
        app.publishActive()
        assertEquals("main.cycle", Substrate.activeFileName)
    }

    @Test fun `publishActive reads log from viewing agent`() {
        app.ensureMainTab()
        val agent = AgentsModel.agentFor("main.cycle")!!
        Substrate.viewingAgentId = agent.id
        agent.interpreter.appendLog("test line", "info")
        app.publishActive()
        assertTrue(Substrate.logEntries.any { it.text == "test line" })
    }

    // ---------- 12. log filter defaults ----------

    @Test fun `logFilter independent of active file`() {
        app.ensureMainTab()
        Substrate.logFilter = "error"
        app.setActiveFile("main.cycle")
        assertEquals("error", Substrate.logFilter)
    }

    // ---------- 13. waiter auto-detection ----------

    @Test fun `agent for file with result is waiter`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "a = 5\n:result"
        app.syncVariablesFromCode(force = true)
        val agent = AgentsModel.agentFor("main.cycle")!!
        assertTrue(agent.waiter)
    }

    @Test fun `agent for file without result is not waiter`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "a = 5\n=> :stop"
        app.syncVariablesFromCode(force = true)
        val agent = AgentsModel.agentFor("main.cycle")!!
        assertFalse(agent.waiter)
    }

    // ---------- 14. updateCode syncs variables ----------

    @Test fun `updateCode syncs variables`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = ""
        app.updateCode("5 = a")
        assertEquals("5", tab.interpreter.variables["a"])
    }

    @Test fun `updateCode preserves old values`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.interpreter.variables["a"] = "5"
        app.updateCode("0 = a")
        assertEquals("5", tab.interpreter.variables["a"])
    }

    @Test fun `updateCode renames`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "5 = a"
        app.syncVariablesFromCode(force = true)
        app.updateCode("5 = b")
        assertEquals("5", tab.interpreter.variables["b"])
        assertNull(tab.interpreter.variables["a"])
    }

    @Test fun `updateCode forceRename no rename`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "5 = a"
        app.syncVariablesFromCode(force = true)
        app.updateCode("5 = b", forceRename = true)
        assertEquals("5", tab.interpreter.variables["a"])
        assertEquals("5", tab.interpreter.variables["b"])
    }

    // ---------- 15. error lines / resolved lines ----------

    @Test fun `error lines are reset on clear log`() {
        Substrate.errorLines = setOf(1, 2)
        Substrate.resolvedLines = setOf(3)
        Substrate.clearLog()
        assertTrue(Substrate.errorLines.isEmpty())
        assertTrue(Substrate.resolvedLines.isEmpty())
    }

    @Test fun `validateCurrent adds error lines`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "^ nope"
        app.validateCurrent()
        assertTrue(Substrate.errorLines.isNotEmpty())
    }

    @Test fun `validateCurrent successful code clears errors`() {
        app.ensureMainTab()
        val tab = TabsModel.active()!!
        tab.code = "1 = a\n=> :stop"
        app.validateCurrent()
        assertTrue(Substrate.errorLines.isEmpty())
    }

    // ---------- 16. activeTab switch ----------

    @Test fun `setActiveTab changes active`() {
        app.ensureMainTab()
        app.newTab()
        val n = TabsModel.tabs.size
        app.setActiveTab(0)
        assertEquals(0, TabsModel.activeIndex)
        assertEquals(n, TabsModel.tabs.size)
    }

    @Test fun `setActiveFile unknown is no-op`() {
        app.ensureMainTab()
        val before = TabsModel.activeIndex
        app.setActiveFile("nonexistent.cycle")
        assertEquals(before, TabsModel.activeIndex)
    }

    // ---------- 17. TerminalModel ----------

    @Test fun `terminal open creates session`() {
        runBlocking {
            val scope = CoroutineScope(Dispatchers.Default)
            val s = TerminalModel.open("Test", scope)
            assertEquals("Test", s.title)
            assertEquals(1, TerminalModel.sessions.size)
            TerminalModel.closeAll()
        }
    }

    @Test fun `terminal close removes session`() {
        runBlocking {
            val scope = CoroutineScope(Dispatchers.Default)
            val s = TerminalModel.open("Test", scope)
            TerminalModel.close(s.id)
            assertTrue(TerminalModel.sessions.isEmpty())
        }
    }

    @Test fun `terminal byTitle finds session`() {
        runBlocking {
            val scope = CoroutineScope(Dispatchers.Default)
            TerminalModel.open("Foo", scope)
            assertNotNull(TerminalModel.byTitle("Foo"))
            assertNull(TerminalModel.byTitle("Bar"))
            TerminalModel.closeAll()
        }
    }

    @Test fun `terminal closeAll clears sessions`() {
        runBlocking {
            val scope = CoroutineScope(Dispatchers.Default)
            TerminalModel.open("A", scope)
            TerminalModel.open("B", scope)
            TerminalModel.closeAll()
            assertTrue(TerminalModel.sessions.isEmpty())
        }
    }

    // ---------- 18. SnackbarModel ----------

    @Test fun `snackbar show adds message`() {
        SnackbarModel.show("hello", "warning")
        assertEquals(1, SnackbarModel.messages.size)
        assertEquals("hello", SnackbarModel.messages[0].text)
        assertEquals("warning", SnackbarModel.messages[0].kind)
    }

    @Test fun `snackbar dismiss removes message`() {
        SnackbarModel.show("hello", "warning")
        val id = SnackbarModel.messages[0].id
        SnackbarModel.dismiss(id)
        assertTrue(SnackbarModel.messages.isEmpty())
    }

    @Test fun `snackbar max 3 messages`() {
        SnackbarModel.show("a")
        SnackbarModel.show("b")
        SnackbarModel.show("c")
        SnackbarModel.show("d")
        assertEquals(3, SnackbarModel.messages.size)
    }

    @Test fun `snackbar clear removes all`() {
        SnackbarModel.show("a")
        SnackbarModel.show("b")
        SnackbarModel.clear()
        assertTrue(SnackbarModel.messages.isEmpty())
    }

    // ---------- 19. pauseAll / resumeAll in applyResult ----------

    @Test fun `applyResult pauseAll command pauses all agents`() {
        app.ensureMainTab()
        app.newAgentTab()
        val result = be.`true`.StepResult(
            currentStep = 1, changedVariables = emptyMap(), removedVariables = emptyList(),
            log = ":pauseAll", status = Status.RUNNING, command = "pauseAll"
        )
        val agent = AgentsModel.agents.first()
        app.applyResult(agent, result)
        assertTrue(AgentsModel.agents.all { it.status == Status.PAUSED })
    }

    @Test fun `applyResult resumeAll command resumes all agents`() {
        app.ensureMainTab()
        app.newAgentTab()
        AgentsModel.agents.forEach { it.status = Status.PAUSED }
        val result = be.`true`.StepResult(
            currentStep = 1, changedVariables = emptyMap(), removedVariables = emptyList(),
            log = ":resumeAll", status = Status.RUNNING, command = "resumeAll"
        )
        val agent = AgentsModel.agents.first()
        app.applyResult(agent, result)
        assertTrue(AgentsModel.agents.all { it.status == Status.RUNNING })
    }
}