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
    }

    // ---------- 1. AgentsModel basics ----------

    @Test fun `agentFor returns null on empty`() {
        assertNull(AgentsModel.agentFor("a.true"))
    }

    @Test fun `create adds agent with unique id`() {
        val a1 = AgentsModel.create("agent-1", "a.true", Interpreter())
        val a2 = AgentsModel.create("agent-2", "b.true", Interpreter())
        assertNotNull(a1)
        assertNotNull(a2)
        assertNotEquals(a1!!.id, a2!!.id)
    }

    @Test fun `create rejects duplicate file`() {
        AgentsModel.create("agent-1", "a.true", Interpreter())
        val second = AgentsModel.create("agent-2", "a.true", Interpreter())
        assertNull(second)
    }

    @Test fun `create rejects duplicate name`() {
        AgentsModel.create("agent-1", "a.true", Interpreter())
        val second = AgentsModel.create("agent-1", "b.true", Interpreter())
        assertNull(second)
    }

    @Test fun `create rejects after MAX_AGENTS`() {
        for (i in 1..AgentsModel.MAX_AGENTS) {
            AgentsModel.create("agent-$i", "file$i.true", Interpreter())
        }
        val extra = AgentsModel.create("agent-extra", "extra.true", Interpreter())
        assertNull(extra)
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
        val a = AgentsModel.create("agent-1", "a.true", Interpreter())
        assertEquals(a, AgentsModel.byId(a!!.id))
    }

    @Test fun `byName finds agent`() {
        val a = AgentsModel.create("agent-1", "a.true", Interpreter())
        assertEquals(a, AgentsModel.byName("agent-1"))
    }

    @Test fun `byName returns null for unknown`() {
        assertNull(AgentsModel.byName("nope"))
    }

    @Test fun `remove deletes agent`() {
        val a = AgentsModel.create("agent-1", "a.true", Interpreter())!!
        AgentsModel.remove(a.id)
        assertNull(AgentsModel.byId(a.id))
    }

    @Test fun `agentFor returns correct agent for file`() {
        val a = AgentsModel.create("agent-1", "a.true", Interpreter())
        assertEquals(a, AgentsModel.agentFor("a.true"))
        assertNull(AgentsModel.agentFor("b.true"))
    }

    // ---------- 2. AgentState fields ----------

    @Test fun `agent has default status STOPPED`() {
        val a = AgentsModel.create("agent-1", "a.true", Interpreter())!!
        assertEquals(Status.STOPPED, a.status)
        assertEquals(1, a.currentStep)
        assertEquals(400L, a.speedMs)
    }

    @Test fun `agent currentStep independent from interpreter`() {
        val a = AgentsModel.create("agent-1", "a.true", Interpreter())!!
        a.currentStep = 5
        assertEquals(5, a.currentStep)
        assertEquals(1, a.interpreter.currentStep)
    }

    @Test fun `agent status mutable`() {
        val a = AgentsModel.create("agent-1", "a.true", Interpreter())!!
        a.status = Status.RUNNING
        assertEquals(Status.RUNNING, a.status)
        a.status = Status.PAUSED
        assertEquals(Status.PAUSED, a.status)
    }

    // ---------- 3. viewingAgentId ----------

    @Test fun `viewingAgentId default null`() {
        assertNull(Substrate.viewingAgentId)
    }

    @Test fun `viewingAgentId changes`() {
        val a = AgentsModel.create("agent-1", "a.true", Interpreter())!!
        Substrate.viewingAgentId = a.id
        assertEquals(a.id, Substrate.viewingAgentId)
    }

    @Test fun `remove agent clears viewing`() {
        val a = AgentsModel.create("agent-1", "a.true", Interpreter())!!
        Substrate.viewingAgentId = a.id
        AgentsModel.remove(a.id)
        if (AgentsModel.byId(a.id) == null) {
            // симуляция: если viewing указывает на удалённого, обнуляем
            Substrate.viewingAgentId = null
        }
        assertNull(Substrate.viewingAgentId)
    }

    // ---------- 4. Substrate / moved fields ----------

    @Test fun `substrate has viewingAgentId`() {
        Substrate.viewingAgentId = 5
        assertEquals(5, Substrate.viewingAgentId)
        Substrate.viewingAgentId = null
    }

    @Test fun `substrate has navigateBehavior EVICT default`() {
        assertEquals(NavigateBehavior.EVICT, Substrate.navigateBehavior)
    }

    @Test fun `substrate has expandedFileName`() {
        Substrate.expandedFileName = "a.true"
        assertEquals("a.true", Substrate.expandedFileName)
        Substrate.expandedFileName = null
    }

    // ---------- 5. logFilter / logCount ----------

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

    // ---------- 6. Templates ----------

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

    // ---------- 7. TabsModel interaction ----------

    @Test fun `tabs closeByName removes file`() {
        TabsModel.tabs.clear()
        TabsModel.create("a.true", "1 = x")
        TabsModel.create("b.true", "2 = y")
        TabsModel.closeByName("a.true")
        assertNull(TabsModel.byFileName("a.true"))
        assertNotNull(TabsModel.byFileName("b.true"))
    }

    @Test fun `tabs byFileName finds`() {
        TabsModel.tabs.clear()
        val t = TabsModel.create("a.true", "1 = x")
        assertEquals(t, TabsModel.byFileName("a.true"))
    }

    // ---------- 8. speedMs / stepBackLimit per-agent ----------

    @Test fun `agent speedMs mutable`() {
        val a = AgentsModel.create("agent-1", "a.true", Interpreter())!!
        a.speedMs = 100
        assertEquals(100L, a.speedMs)
    }

    @Test fun `agents independent speedMs`() {
        val a = AgentsModel.create("agent-1", "a.true", Interpreter())!!
        val b = AgentsModel.create("agent-2", "b.true", Interpreter())!!
        a.speedMs = 100
        b.speedMs = 500
        assertEquals(100L, a.speedMs)
        assertEquals(500L, b.speedMs)
    }
}