package be.simple

import be.`true`.Interpreter
import be.`true`.Status
import kotlin.test.*

class AgentsCoreTest {

    @BeforeTest
    fun clean() {
        TabsModel.tabs.clear()
        TabsModel.activeIndex = -1
        AgentsModel.agents.clear()
        Substrate.viewingAgentId = null
        Substrate.expandedFileName = null
        Substrate.activeFileName = ""
        Substrate.fileOrder = emptyList()
    }

    // ---------- 1. AgentsModel basics ----------

    @Test fun `agentFor returns null on empty`() {
        assertNull(AgentsModel.agentFor("a.agent"))
    }

    @Test fun `create adds agent with unique id`() {
        val a1 = AgentsModel.create("agent-1", "a.agent", Interpreter())
        val a2 = AgentsModel.create("agent-2", "b.agent", Interpreter())
        assertNotNull(a1)
        assertNotNull(a2)
        assertNotEquals(a1!!.id, a2!!.id)
    }

    @Test fun `create allows multiple agents on same file`() {
        val a1 = AgentsModel.create("agent-1", "a.agent", Interpreter())
        val a2 = AgentsModel.create("agent-2", "a.agent", Interpreter())
        assertNotNull(a1)
        assertNotNull(a2)
    }

    @Test fun `create rejects duplicate name`() {
        AgentsModel.create("agent-1", "a.agent", Interpreter())
        val second = AgentsModel.create("agent-1", "b.agent", Interpreter())
        assertNull(second)
    }

    @Test fun `colorAt returns rainbow by index`() {
        assertEquals(AgentsModel.RAINBOW[0], AgentsModel.colorAt(0))
        assertEquals(AgentsModel.RAINBOW[3], AgentsModel.colorAt(3))
    }

    @Test fun `colorAt wraps at 7`() {
        assertEquals(AgentsModel.RAINBOW[0], AgentsModel.colorAt(7))
        assertEquals(AgentsModel.RAINBOW[1], AgentsModel.colorAt(8))
    }

    @Test fun `RAINBOW has 7 colors`() {
        assertEquals(7, AgentsModel.RAINBOW.size)
    }

    @Test fun `byId finds agent`() {
        val a = AgentsModel.create("agent-1", "a.agent", Interpreter())
        assertEquals(a, AgentsModel.byId(a!!.id))
    }

    @Test fun `byName finds agent`() {
        val a = AgentsModel.create("agent-1", "a.agent", Interpreter())
        assertEquals(a, AgentsModel.byName("agent-1"))
    }

    @Test fun `byName returns null for unknown`() {
        assertNull(AgentsModel.byName("nope"))
    }

    @Test fun `remove deletes agent`() {
        val a = AgentsModel.create("agent-1", "a.agent", Interpreter())!!
        AgentsModel.remove(a.id)
        assertNull(AgentsModel.byId(a.id))
    }

    @Test fun `agentFor returns correct agent for file`() {
        val a = AgentsModel.create("agent-1", "a.agent", Interpreter())
        assertEquals(a, AgentsModel.agentFor("a.agent"))
        assertNull(AgentsModel.agentFor("b.agent"))
    }

    // ---------- 2. AgentState fields ----------

    @Test fun `agent has default status STOPPED`() {
        val a = AgentsModel.create("agent-1", "a.agent", Interpreter())!!
        assertEquals(Status.STOPPED, a.status)
        assertEquals(1, a.currentStep)
        assertEquals(400L, a.speedMs)
    }

    @Test fun `agent currentStep independent from interpreter`() {
        val a = AgentsModel.create("agent-1", "a.agent", Interpreter())!!
        a.currentStep = 5
        assertEquals(5, a.currentStep)
        assertEquals(1, a.interpreter.currentStep)
    }

    @Test fun `agent status mutable`() {
        val a = AgentsModel.create("agent-1", "a.agent", Interpreter())!!
        a.status = Status.RUNNING
        assertEquals(Status.RUNNING, a.status)
        a.status = Status.PAUSED
        assertEquals(Status.PAUSED, a.status)
    }

    @Test fun `agent has default waiter false`() {
        val a = AgentsModel.create("agent-1", "a.agent", Interpreter())!!
        assertFalse(a.waiter)
    }

    @Test fun `agent has default lastPressed null`() {
        val a = AgentsModel.create("agent-1", "a.agent", Interpreter())!!
        assertNull(a.lastPressed)
    }

    // ---------- 3. waiter auto-detection ----------

    @Test fun `hasResultMarker detects result marker`() {
        assertTrue(AgentsModel.hasResultMarker("a = 5\n:result"))
        assertFalse(AgentsModel.hasResultMarker("a = 5\n=> :stop"))
    }

    @Test fun `recalcWaiter sets waiter true`() {
        TabsModel.create("f.agent", "a = 5\n:result")
        val a = AgentsModel.create("agent-1", "f.agent", Interpreter())!!
        AgentsModel.recalcWaiter(a)
        assertTrue(a.waiter)
    }

    @Test fun `recalcWaiter sets waiter false`() {
        TabsModel.create("f.agent", "a = 5\n=> :stop")
        val a = AgentsModel.create("agent-1", "f.agent", Interpreter())!!
        AgentsModel.recalcWaiter(a)
        assertFalse(a.waiter)
    }

    // ---------- 4. viewingAgentId ----------

    @Test fun `viewingAgentId default null`() {
        assertNull(Substrate.viewingAgentId)
    }

    @Test fun `viewingAgentId changes`() {
        val a = AgentsModel.create("agent-1", "a.agent", Interpreter())!!
        Substrate.viewingAgentId = a.id
        assertEquals(a.id, Substrate.viewingAgentId)
    }

    // ---------- 5. AgentsModel rename ----------

    @Test fun `rename changes name`() {
        val a = AgentsModel.create("agent-1", "a.agent", Interpreter())!!
        assertTrue(AgentsModel.rename(a.id, "agent-new"))
        assertEquals("agent-new", a.name)
    }

    @Test fun `rename rejects duplicate`() {
        AgentsModel.create("agent-1", "a.agent", Interpreter())
        val b = AgentsModel.create("agent-2", "b.agent", Interpreter())!!
        assertFalse(AgentsModel.rename(b.id, "agent-1"))
    }

    @Test fun `rename rejects blank`() {
        val a = AgentsModel.create("agent-1", "a.agent", Interpreter())!!
        assertFalse(AgentsModel.rename(a.id, ""))
    }

    // ---------- 6. Substrate fields ----------

    @Test fun `substrate has viewingAgentId`() {
        Substrate.viewingAgentId = 5
        assertEquals(5, Substrate.viewingAgentId)
        Substrate.viewingAgentId = null
    }

    @Test fun `substrate has navigateBehavior EVICT default`() {
        assertEquals(NavigateBehavior.EVICT, Substrate.navigateBehavior)
    }

    @Test fun `substrate has expandedFileName`() {
        Substrate.expandedFileName = "a.agent"
        assertEquals("a.agent", Substrate.expandedFileName)
        Substrate.expandedFileName = null
    }

    // ---------- 7. logFilter / logCount ----------

    @Test fun `logFilter defaults to All`() {
        assertEquals("All", Substrate.logFilter)
    }

    @Test fun `logCount All equals size`() {
        Substrate.clearLog()
        Substrate.appendLogEntry(be.`true`.LogEntry("a", "step"))
        Substrate.appendLogEntry(be.`true`.LogEntry("b", "error"))
        assertEquals(2, Substrate.logCount("All"))
    }

    @Test fun `logCount error matches entries`() {
        Substrate.clearLog()
        Substrate.appendLogEntry(be.`true`.LogEntry("a", "step"))
        Substrate.appendLogEntry(be.`true`.LogEntry("b", "error"))
        Substrate.appendLogEntry(be.`true`.LogEntry("c", "error"))
        assertEquals(2, Substrate.logCount("error"))
    }

    @Test fun `clearLog resets filter`() {
        Substrate.logFilter = "error"
        Substrate.clearLog()
        assertEquals("All", Substrate.logFilter)
    }

    // ---------- 8. Templates ----------

    @Test fun `pinTemplate deduplicates`() {
        Substrate.pinnedTemplates = emptyList()
        Substrate.pinTemplate("x")
        Substrate.pinTemplate("x")
        assertEquals(1, Substrate.pinnedTemplates.size)
    }

    @Test fun `unpinTemplate removes`() {
        Substrate.pinnedTemplates = listOf("x", "y")
        Substrate.unpinTemplate("x")
        assertEquals(listOf("y"), Substrate.pinnedTemplates)
    }

    @Test fun `addTemplate skips duplicates`() {
        Substrate.resetTemplatesToDefault()
        val before = Substrate.templateGroups[0].templates.size
        Substrate.addTemplate(0, Substrate.templateGroups[0].templates.firstOrNull() ?: "x")
        val after = Substrate.templateGroups[0].templates.size
        assertEquals(before, after)
    }

    @Test fun `resetTemplatesToDefault restores`() {
        Substrate.templateGroups = emptyList()
        Substrate.resetTemplatesToDefault()
        assertEquals(Substrate.DEFAULT_TEMPLATE_GROUPS.size, Substrate.templateGroups.size)
    }

    // ---------- 9. TabsModel interaction ----------

    @Test fun `tabs closeByName removes file`() {
        TabsModel.tabs.clear()
        TabsModel.create("a.cycle", "1 = x")
        TabsModel.create("b.cycle", "2 = y")
        TabsModel.closeByName("a.cycle")
        assertNull(TabsModel.byFileName("a.cycle"))
        assertNotNull(TabsModel.byFileName("b.cycle"))
    }

    @Test fun `tabs byFileName finds`() {
        TabsModel.tabs.clear()
        val t = TabsModel.create("a.cycle", "1 = x")
        assertEquals(t, TabsModel.byFileName("a.cycle"))
    }

    // ---------- 10. speedMs per-agent ----------

    @Test fun `agent speedMs mutable`() {
        val a = AgentsModel.create("agent-1", "a.agent", Interpreter())!!
        a.speedMs = 100
        assertEquals(100L, a.speedMs)
    }

    @Test fun `agents independent speedMs`() {
        val a = AgentsModel.create("agent-1", "a.agent", Interpreter())!!
        val b = AgentsModel.create("agent-2", "b.agent", Interpreter())!!
        a.speedMs = 100
        b.speedMs = 500
        assertEquals(100L, a.speedMs)
        assertEquals(500L, b.speedMs)
    }

    // ---------- 11. fileOrder ----------

    @Test fun `addFileToOrder adds`() {
        Substrate.fileOrder = emptyList()
        Substrate.addFileToOrder("a.cycle")
        assertEquals(listOf("a.cycle"), Substrate.fileOrder)
    }

    @Test fun `addFileToOrder skips duplicate`() {
        Substrate.fileOrder = listOf("a.cycle")
        Substrate.addFileToOrder("a.cycle")
        assertEquals(listOf("a.cycle"), Substrate.fileOrder)
    }

    @Test fun `moveFileUp swaps`() {
        Substrate.fileOrder = listOf("a.cycle", "b.cycle")
        Substrate.moveFileUp("b.cycle")
        assertEquals(listOf("b.cycle", "a.cycle"), Substrate.fileOrder)
    }

    @Test fun `moveFileDown swaps`() {
        Substrate.fileOrder = listOf("a.cycle", "b.cycle")
        Substrate.moveFileDown("a.cycle")
        assertEquals(listOf("b.cycle", "a.cycle"), Substrate.fileOrder)
    }

    @Test fun `moveFileUp first noop`() {
        Substrate.fileOrder = listOf("a.cycle", "b.cycle")
        Substrate.moveFileUp("a.cycle")
        assertEquals(listOf("a.cycle", "b.cycle"), Substrate.fileOrder)
    }

    @Test fun `moveFileDown last noop`() {
        Substrate.fileOrder = listOf("a.cycle", "b.cycle")
        Substrate.moveFileDown("b.cycle")
        assertEquals(listOf("a.cycle", "b.cycle"), Substrate.fileOrder)
    }

    @Test fun `removeFileFromOrder removes`() {
        Substrate.fileOrder = listOf("a.cycle", "b.cycle")
        Substrate.removeFileFromOrder("a.cycle")
        assertEquals(listOf("b.cycle"), Substrate.fileOrder)
    }

    // ---------- 12. FileKind ----------

    @Test fun `fileKindOf agent`() {
        assertEquals(FileKind.AGENT, fileKindOf("main.agent"))
    }

    @Test fun `fileKindOf cycle`() {
        assertEquals(FileKind.CYCLE, fileKindOf("main.cycle"))
    }

    @Test fun `fileKindOf other`() {
        assertEquals(FileKind.OTHER, fileKindOf("data.txt"))
    }

    @Test fun `fileKindOf no extension`() {
        assertEquals(FileKind.OTHER, fileKindOf("notes"))
    }
}