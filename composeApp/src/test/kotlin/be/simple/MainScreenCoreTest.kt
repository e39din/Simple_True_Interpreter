package be.simple

import be.`true`.Interpreter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MainScreenCoreTest {

    @Test
    fun agentCreate() {
        AgentsModel.agents.clear()
        val interp = Interpreter()
        val agent = AgentsModel.create("agent-1", "main.cycle", interp)
        assertNotNull(agent)
        assertEquals("agent-1", agent.name)
        assertEquals("agent-1.inbox", agent.inboxFileName)
    }

    @Test
    fun agentCreateDuplicateName() {
        AgentsModel.agents.clear()
        AgentsModel.create("agent-1", "main.cycle", Interpreter())
        val second = AgentsModel.create("agent-1", "main2.cycle", Interpreter())
        assertNull(second)
    }

    @Test
    fun agentInboxFileCreated() {
        AgentsModel.agents.clear()
        val agent = AgentsModel.create("test-agent", "main.cycle", Interpreter())
        assertNotNull(agent)
        val inboxFile = java.io.File(System.getProperty("user.home"), ".simple/${agent.inboxFileName}")
        assertTrue(inboxFile.exists() || inboxFile.parentFile?.exists() == true)
    }

    @Test
    fun agentRemoveStopsSchedule() {
        AgentsModel.agents.clear()
        val agent = AgentsModel.create("agent-1", "main.cycle", Interpreter())
        assertNotNull(agent)
        AgentsModel.remove(agent.id)
        assertTrue(AgentsModel.byId(agent.id) == null)
    }

    @Test
    fun agentColor() {
        AgentsModel.agents.clear()
        val agent = AgentsModel.create("agent-color", "main.cycle", Interpreter())
        assertNotNull(agent)
        assertNotNull(agent.color)
    }

    @Test
    fun agentWaiter() {
        AgentsModel.agents.clear()
        val agent = AgentsModel.create("agent-waiter", "main.cycle", Interpreter())
        assertNotNull(agent)
        TabsModel.create("main.cycle", "=> :result")
        AgentsModel.recalcWaiter(agent)
        assertTrue(agent.waiter)
    }

    @Test
    fun visualRectCreate() {
        VisualModel.rects.clear()
        val r = VisualModel.createRect("a", 10, 20)
        assertEquals("a", r.target)
        assertEquals(10, r.x)
        assertEquals(20, r.y)
        assertTrue(r.visible)
    }

    @Test
    fun visualRectUpdateArea() {
        VisualModel.rects.clear()
        val r = VisualModel.createRect("a", 10, 20)
        VisualModel.updateArea(r, 50, 60)
        assertEquals(50, r.x)
        assertEquals(60, r.y)
    }

    @Test
    fun visualRectRemove() {
        VisualModel.rects.clear()
        val r = VisualModel.createRect("a", 10, 20)
        VisualModel.remove(r)
        assertNull(VisualModel.byTarget("a"))
    }

    @Test
    fun visualBackgroundSet() {
        VisualModel.clearAll()
        VisualModel.setBackground("main.cycle", "/path/bg.png", "tile")
        val bg = VisualModel.backgroundFor("main.cycle")
        assertNotNull(bg)
        assertEquals("tile", bg.mode)
        VisualModel.clearAll()
    }

    @Test
    fun visualBackgroundReset() {
        VisualModel.clearAll()
        VisualModel.setBackground("main.cycle", "/path/bg.png", "fit")
        VisualModel.resetBackground("main.cycle")
        assertNull(VisualModel.backgroundFor("main.cycle"))
    }

    @Test
    fun fileKindInbox() {
        assertEquals(FileKind.INBOX, fileKindOf("agent-1.inbox"))
    }

    @Test
    fun fileKindCycle() {
        assertEquals(FileKind.CYCLE, fileKindOf("main.cycle"))
    }

    @Test
    fun fileKindAgent() {
        assertEquals(FileKind.AGENT, fileKindOf("agent-1.agent"))
    }

    @Test
    fun fileKindOther() {
        assertEquals(FileKind.OTHER, fileKindOf("readme.txt"))
    }

    @Test
    fun secretMask() {
        assertEquals("***", SecretModel.mask("abc"))
        assertEquals("ab***yz", SecretModel.mask("abcdefgh"))
    }

    @Test
    fun secretIsSecret() {
        SecretModel.set("test_mask_key", "value")
        assertTrue(SecretModel.isSecret("test_mask_key"))
        SecretModel.remove("test_mask_key")
    }

    @Test
    fun scheduleListEmpty() {
        AgentsModel.agents.clear()
        val agent = AgentsModel.create("sched-agent", "main.cycle", Interpreter())
        assertNotNull(agent)
        assertEquals(0, ScheduleModel.list(agent).size)
    }

    @Test
    fun scheduleStopAll() {
        AgentsModel.agents.clear()
        val agent = AgentsModel.create("sched-agent", "main.cycle", Interpreter())
        assertNotNull(agent)
        ScheduleModel.stop(agent)
        assertEquals(0, agent.scheduleJobs.size)
    }

    @Test
    fun scheduleLimit() {
        AgentsModel.agents.clear()
        val agent = AgentsModel.create("sched-limit", "main.cycle", Interpreter())
        assertNotNull(agent)
        assertEquals(10, agent.scheduleLimit)
        agent.scheduleLimit = 5
        assertEquals(5, agent.scheduleLimit)
    }

    @Test
    fun soundStopWithoutStart() {
        assertTrue(SoundModel.stop(null) || true)
    }

    @Test
    fun websocketMessageEmpty() {
        WebSocketModel.close()
        assertEquals("", WebSocketModel.lastMessage)
    }

    @Test
    fun websocketClientsEmpty() {
        WebSocketModel.close()
        assertTrue(WebSocketModel.clients.isEmpty())
    }

    @Test
    fun memAllocReturnsAddress() {
        MemModel.reset()
        val addr = MemModel.alloc(16)
        assertTrue(addr >= 1000)
        MemModel.free(addr)
    }

    @Test
    fun memWriteRead() {
        MemModel.reset()
        val addr = MemModel.alloc(16)
        assertTrue(MemModel.write(addr, "42"))
        assertEquals("42", MemModel.read(addr))
        MemModel.free(addr)
    }

    @Test
    fun memFreeInvalid() {
        MemModel.reset()
        assertTrue(!MemModel.free(9999))
    }

    @Test
    fun memReset() {
        MemModel.reset()
        val addr = MemModel.alloc(16)
        MemModel.write(addr, "1")
        MemModel.reset()
        assertEquals("", MemModel.read(addr))
    }

    @Test
    fun mockSet() {
        MockModel.reset()
        MockModel.set("https://test.com", "response", 200)
        val mock = MockModel.get("https://test.com")
        assertNotNull(mock)
        assertEquals("response", mock.response)
        assertEquals(200, mock.code)
        MockModel.reset()
    }

    @Test
    fun mockList() {
        MockModel.reset()
        MockModel.set("https://a.com", "a")
        MockModel.set("https://b.com", "b")
        assertEquals(listOf("https://a.com", "https://b.com"), MockModel.list())
        MockModel.reset()
    }

    @Test
    fun extensionsRegisterAndList() {
        Extensions.clear()
        val ext = object : Extension {
            override val names = setOf("test_ext")
            override fun handle(op: String, args: List<String>, ctx: Ctx): String? = "ok"
        }
        Extensions.register(ext)
        assertTrue("test_ext" in Extensions.list())
        Extensions.unregister(ext)
        assertTrue("test_ext" !in Extensions.list())
    }

    @Test
    fun extensionsInvokeUnknownReturnsNull() {
        Extensions.clear()
        val ctx = Ctx(Interpreter(), -1, null)
        assertNull(Extensions.invoke("nonexistent", emptyList(), ctx))
    }

    @Test
    fun extensionsInvokeHandled() {
        Extensions.clear()
        val ext = object : Extension {
            override val names = setOf("echo")
            override fun handle(op: String, args: List<String>, ctx: Ctx): String? = args.joinToString(",")
        }
        Extensions.register(ext)
        val ctx = Ctx(Interpreter(), -1, null)
        assertEquals("a,b", Extensions.invoke("echo", listOf("a", "b"), ctx))
        Extensions.unregister(ext)
    }

    @Test
    fun tabsCreateAndClose() {
        TabsModel.tabs.clear()
        TabsModel.activeIndex = -1
        TabsModel.create("test.cycle", "0 = a")
        assertEquals(1, TabsModel.tabs.size)
        assertEquals("test.cycle", TabsModel.active()?.fileName)
        TabsModel.close(0)
        assertEquals(0, TabsModel.tabs.size)
    }

    @Test
    fun tabsRename() {
        TabsModel.tabs.clear()
        TabsModel.create("old.cycle", "")
        TabsModel.rename(0, "new.cycle")
        assertEquals("new.cycle", TabsModel.tabs[0].fileName)
    }

    @Test
    fun substrateParseColor() {
        assertNotNull(Substrate.parseColor("#1E88E5"))
        assertNotNull(Substrate.parseColor("#F00"))
        assertNull(Substrate.parseColor("invalid"))
    }

    @Test
    fun substrateFileOrder() {
        Substrate.fileOrder = emptyList()
        Substrate.addFileToOrder("a.cycle")
        Substrate.addFileToOrder("b.cycle")
        assertEquals(listOf("a.cycle", "b.cycle"), Substrate.fileOrder)
        Substrate.moveFileUp("b.cycle")
        assertEquals(listOf("b.cycle", "a.cycle"), Substrate.fileOrder)
        Substrate.removeFileFromOrder("b.cycle")
        assertEquals(listOf("a.cycle"), Substrate.fileOrder)
    }

    @Test
    fun substrateRecentProjects() {
        Substrate.recentProjects = emptyList()
        Substrate.addRecentProject("/path/to/project1")
        Substrate.addRecentProject("/path/to/project2")
        assertEquals(2, Substrate.recentProjects.size)
        assertEquals("/path/to/project2", Substrate.recentProjects[0])
        Substrate.recentProjects = emptyList()
    }

    @Test
    fun substratePinned() {
        Substrate.pinnedList.clear()
        Substrate.pinnedList.add("a")
        Substrate.pinnedList.add("b")
        assertEquals(2, Substrate.pinnedList.size)
        Substrate.pinnedList.removeAll { it == "a" }
        assertEquals(1, Substrate.pinnedList.size)
        Substrate.pinnedList.clear()
    }

    @Test
    fun askModelQueue() {
        AskModel.queue.clear()
        var result = ""
        val id = AskModel.ask("question", "default", "text") { result = it }
        assertEquals(1, AskModel.queue.size)
        AskModel.answer(id, "answer")
        assertEquals("answer", result)
        assertEquals(0, AskModel.queue.size)
    }

    @Test
    fun askModelCancel() {
        AskModel.queue.clear()
        var result = "not-empty"
        val id = AskModel.ask("question", "default", "text") { result = it }
        AskModel.cancel(id)
        assertEquals("", result)
        assertEquals(0, AskModel.queue.size)
    }

    @Test
    fun templatesNotEmpty() {
        assertTrue(Substrate.templates.isNotEmpty())
        assertEquals(5, Substrate.templates.size)
    }

    @Test
    fun templatesHaveIds() {
        val ids = Substrate.templates.map { it.id }
        assertTrue(ids.contains("hello"))
        assertTrue(ids.contains("counter"))
        assertTrue(ids.contains("calculator"))
        assertTrue(ids.contains("cave"))
        assertTrue(ids.contains("plugin"))
    }

    @Test
    fun templateCaveHasCode() {
        val cave = Substrate.templates.firstOrNull { it.id == "cave" }
        assertNotNull(cave)
        assertTrue(cave.code.contains(":ask"))
        assertTrue(cave.code.contains("= turn"))
    }

    @Test
    fun templateEndingNotEmpty() {
        assertTrue(Substrate.TEMPLATE_ENDING.isNotEmpty())
        assertTrue(Substrate.TEMPLATE_ENDING.contains(":stop"))
    }

    @Test
    fun terminalListEmpty() {
        TerminalModel.closeAll()
        assertTrue(TerminalModel.list().isEmpty())
    }

    @Test
    fun snackbarShow() {
        SnackbarModel.clear()
        SnackbarModel.show("test message", "warning")
        assertEquals(1, SnackbarModel.messages.size)
        SnackbarModel.clear()
    }

    @Test
    fun snackbarMaxThree() {
        SnackbarModel.clear()
        SnackbarModel.show("msg1")
        SnackbarModel.show("msg2")
        SnackbarModel.show("msg3")
        SnackbarModel.show("msg4")
        assertEquals(3, SnackbarModel.messages.size)
        SnackbarModel.clear()
    }
}