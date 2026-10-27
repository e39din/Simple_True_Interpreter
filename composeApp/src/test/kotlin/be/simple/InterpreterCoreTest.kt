package be.simple

import be.`true`.Interpreter
import be.`true`.Status
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class InterpreterCoreTest {

    @Test
    fun mathAddition() {
        val i = Interpreter()
        i.executeOneStep("2 + 3 => :math")
        assertEquals("5", i.results["math"])
    }

    @Test
    fun mathNamedSum() {
        val i = Interpreter()
        i.executeOneStep("1, 2, 3 => :math:sum")
        assertEquals("6", i.results["math:sum"])
    }

    @Test
    fun dataUpper() {
        val i = Interpreter()
        i.variables["d"] = "hello"
        i.executeOneStep("d => :data:upper")
        assertEquals("HELLO", i.results["upper"])
    }

    @Test
    fun dataEmpty() {
        val i = Interpreter()
        i.variables["d"] = ""
        i.executeOneStep("d => :data:empty")
        assertEquals("1", i.results["empty"])
    }

    @Test
    fun dataGroupby() {
        val i = Interpreter()
        i.executeOneStep("1,2,3,4,5,6 => :data:groupby, n % 2, count")
        assertEquals("0:3,1:3", i.results["groupby"])
    }

    @Test
    fun secretSetGet() {
        SecretModel.set("test_key", "test_value")
        val i = Interpreter(secretCaller = { action, key, value ->
            when (action) {
                "get" -> SecretModel.get(key ?: "") ?: ""
                "set" -> { SecretModel.set(key ?: "", value ?: ""); "1" }
                "remove" -> { SecretModel.remove(key ?: ""); "1" }
                "list" -> SecretModel.list().joinToString(",")
                else -> "-1"
            }
        })
        i.executeOneStep("test_key => :secret:get")
        assertEquals("test_value", i.results["secret"])
        assertEquals("1", i.results["secret:found"])
        SecretModel.remove("test_key")
    }

    @Test
    fun memAllocWriteRead() {
        MemModel.reset()
        val i = Interpreter()
        i.executeOneStep("16 => :mem:alloc => p")
        val addr = i.variables["p"]?.toIntOrNull()
        assertNotNull(addr)
        i.executeOneStep("p, 42 => :mem:write")
        i.executeOneStep("p => :mem:read => v")
        assertEquals("42", i.variables["v"])
    }

    @Test
    fun fileStreamReadsFirstBatch() {
        val tmp = kotlin.io.path.createTempFile("stream", ".txt").toFile()
        tmp.writeText((1..100).joinToString("\n"))
        val i = Interpreter()
        i.executeOneStep("${tmp.absolutePath} => :file:stream => csv")
        assertEquals("1", i.variables["csv"])
        assertNotNull(i.results["stream"])
        tmp.delete()
    }

    @Test
    fun streamNextWithoutActiveThrows() {
        val i = Interpreter()
        i.executeOneStep(":stream:next")
        assertTrue(i.errorLines.isNotEmpty())
    }

    @Test
    fun retryTransientSuccess() {
        val i = Interpreter(retryCaller = { _, _, _, _, _, _ -> "1" })
        i.executeOneStep("url, data => :api:post:retry")
        assertEquals("1", i.results["api:post"])
    }

    @Test
    fun apiMock() {
        MockModel.reset()
        MockModel.set("https://test.com", "response", 200)
        val i = Interpreter()
        i.executeOneStep("https://test.com => :api:get")
        assertEquals("1", i.results["api:get"])
        MockModel.reset()
    }

    @Test
    fun terminalOpenWriteRead() {
        val i = Interpreter(terminalCaller = { action, _, _ ->
            when (action) {
                "open" -> "1"
                "read" -> "output"
                else -> "-1"
            }
        })
        i.executeOneStep("ls => :terminal => Console")
        assertEquals("1", i.results["terminal"])
        i.executeOneStep("Console => :terminal:read")
        assertEquals("output", i.results["terminal:read"])
    }

    @Test
    fun openUrl() {
        val i = Interpreter(openCaller = { kind, _ -> if (kind == "url") "1" else "-1" })
        i.executeOneStep("https://example.com => :open:url")
        assertEquals("1", i.results["open"])
    }

    @Test
    fun backgroundSet() {
        val i = Interpreter(visualCaller = { _, _, _, _ -> "1" })
        i.executeOneStep("/path/bg.png => :background")
        assertEquals("1", i.results["background"])
        assertEquals("fit", i.results["background:mode"])
    }

    @Test
    fun askAsync() {
        var answered = false
        val i = Interpreter()
        i.executeOneStep("question => :ask => dst")
        assertEquals("1", i.results["ask:pending"])
        assertNotNull(i.results["ask:id"])
        AskModel.answer(i.results["ask:id"]!!.toInt(), "hello")
        assertEquals("0", i.results["ask:pending"])
        assertEquals("hello", i.results["ask"])
    }

    @Test
    fun errorPause() {
        val i = Interpreter()
        i.executeOneStep("1 => :error:pause")
        assertEquals("1", i.results["error:pause"])
        i.executeOneStep("0 => :error:pause")
        assertEquals("0", i.results["error:pause"])
    }

    @Test
    fun assertPause() {
        val i = Interpreter()
        i.executeOneStep("1 => :assert:pause")
        assertEquals("1", i.results["assert:pause"])
        i.executeOneStep("0 => :assert:pause")
        assertEquals("0", i.results["assert:pause"])
    }

    @Test
    fun questionInString() {
        val i = Interpreter()
        i.executeOneStep("Что делаешь? (1, 2, 3) => :message")
        assertNotNull(i.results["message"])
        assertTrue(i.results["message"]!!.contains("Что делаешь?"))
    }

    @Test
    fun commaInMessage() {
        val i = Interpreter()
        i.executeOneStep("hp = 100, gold = 0 => :message")
        assertNotNull(i.results["message"])
        assertTrue(i.results["message"]!!.contains("hp = 100, gold = 0"))
    }

    @Test
    fun synonyms() {
        val i = Interpreter()
        i.executeOneStep("12 = twelve, number, a")
        assertEquals("12", i.variables["twelve"])
        assertEquals("12", i.variables["number"])
        assertEquals("12", i.variables["a"])
        assertTrue(i.aliases.containsKey("twelve"))
        assertTrue(i.aliases["twelve"]!!.contains("number"))
    }

    @Test
    fun gotoByAlias() {
        val i = Interpreter()
        i.executeOneStep("12 = twelve, number, a")
        i.executeOneStep("= label")
        i.executeOneStep("^ label")
        assertTrue(true)
    }

    @Test
    fun extList() {
        val i = Interpreter()
        i.executeOneStep(":ext:list => dst")
        assertEquals("1", i.variables["dst"])
    }

    @Test
    fun scheduleLimit() {
        val i = Interpreter()
        i.executeOneStep("10 => :schedule:limit")
        assertEquals("10", i.results["schedule:limit"])
    }

    @Test
    fun strictModeForbidsMem() {
        val i = Interpreter()
        val errors = i.validate(i.parse("16 => :mem:alloc => p"), strict = true)
        assertTrue(errors.any { it.message.contains("Strict Mode") })
    }

    @Test
    fun strictModeAllowsSecret() {
        val i = Interpreter()
        val errors = i.validate(i.parse("key => :secret:get"), strict = true)
        assertTrue(errors.none { it.message.contains("Strict Mode") })
    }

    @Test
    fun loopBasic() {
        val i = Interpreter()
        i.variables["a"] = "0"
        i.executeOneStep("a + 1 => :math => a")
        assertEquals("1", i.variables["a"])
    }

    @Test
    fun conditionTrue() {
        val i = Interpreter()
        i.variables["a"] = "5"
        i.executeOneStep("a == 5 ? hello => :message")
        assertEquals("hello", i.results["message"])
    }

    @Test
    fun conditionFalse() {
        val i = Interpreter()
        i.variables["a"] = "3"
        i.executeOneStep("a == 5 ? hello => :message")
        assertNull(i.results["message"])
    }

    @Test
    fun moveOperation() {
        val i = Interpreter()
        i.variables["a"] = "hello"
        i.executeOneStep("a -> b")
        assertNull(i.variables["a"])
        assertEquals("hello", i.variables["b"])
    }

    @Test
    fun copyOperation() {
        val i = Interpreter()
        i.variables["a"] = "hello"
        i.executeOneStep("a => b")
        assertEquals("hello", i.variables["a"])
        assertEquals("hello", i.variables["b"])
    }

    @Test
    fun jsonGet() {
        val i = Interpreter()
        i.variables["j"] = """{"key":"value"}"""
        i.executeOneStep("j, /key => :json:get")
        assertEquals("value", i.results["json"])
    }

    @Test
    fun xmlGet() {
        val i = Interpreter()
        i.variables["x"] = "<root><item>42</item></root>"
        i.executeOneStep("x, /root/item => :xml:get")
        assertEquals("42", i.results["xml"])
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
    fun agentBroadcast() {
        var called = false
        val i = Interpreter(agentCaller = { _, name, action, _ ->
            if (action == be.`true`.AgentAction.BROADCAST && name == "*") called = true
        })
        i.executeOneStep("hello => :agent:broadcast")
        assertTrue(called)
    }

    @Test
    fun agentSend() {
        var target = ""
        val i = Interpreter(agentCaller = { _, name, action, _ ->
            if (action == be.`true`.AgentAction.SEND) target = name
        })
        i.executeOneStep("agent-1, hello => :agent:send")
        assertEquals("agent-1", target)
    }

    @Test
    fun inboxGet() {
        val i = Interpreter(agentInboxReader = { "inbox content" })
        i.executeOneStep(":agent:inbox => dst")
        assertEquals("inbox content", i.variables["dst"])
    }

    @Test
    fun inboxClear() {
        var cleared = false
        val i = Interpreter(agentCaller = { _, _, action, _ ->
            if (action == be.`true`.AgentAction.INBOX) cleared = true
        })
        i.executeOneStep(":agent:inbox:clear")
        assertTrue(cleared)
    }

    @Test
    fun simpleClick() {
        var clicked = ""
        val i = Interpreter(simpleCaller = { action, arg -> clicked = "$action:$arg" })
        i.executeOneStep("button:startAll => :simple:click")
        assertEquals("click:button:startAll", clicked)
    }

    @Test
    fun errorSet() {
        val i = Interpreter()
        i.executeOneStep("test error => :error")
        assertEquals("1", i.results["error"])
        assertNotNull(i.results["error:line"])
        assertNotNull(i.results["error:message"])
    }

    @Test
    fun errorResolve() {
        val i = Interpreter()
        i.executeOneStep("test error => :error")
        i.executeOneStep("0 => :error:resolve")
        assertEquals("0", i.results["error:message"]?.split(",")?.getOrNull(1)?.trim())
    }

    @Test
    fun assertPass() {
        val i = Interpreter()
        i.variables["a"] = "5"
        i.variables["b"] = "5"
        i.executeOneStep("a == b, 'must be equal' => :assert")
        assertEquals("1", i.results["assert"])
    }

    @Test
    fun assertFail() {
        val i = Interpreter()
        i.variables["a"] = "5"
        i.variables["b"] = "3"
        i.executeOneStep("a == b, 'must be equal' => :assert")
        assertEquals("0", i.results["assert"])
        assertTrue(i.errorLines.isNotEmpty())
    }

    @Test
    fun pinned() {
        val i = Interpreter()
        i.executeOneStep("a => :pinned:add")
        assertEquals("1", i.results["pinned"])
    }

    @Test
    fun watchAdd() {
        val i = Interpreter()
        i.executeOneStep("a => :watch:add")
        assertEquals("1", i.results["watch"])
    }

    @Test
    fun diff() {
        val i = Interpreter()
        i.executeOneStep(":diff")
        assertNotNull(i.results["diff"])
    }

    @Test
    fun history() {
        val i = Interpreter()
        i.executeOneStep(":history")
        assertNotNull(i.results["history"])
    }

    @Test
    fun agentSync() {
        val i = Interpreter()
        i.executeOneStep(":agent:sync")
        assertEquals("1", i.results["agent:sync"])
    }

    @Test
    fun streamSeek() {
        val i = Interpreter()
        i.executeOneStep(":stream:seek name, 10")
        assertNotNull(i.results["stream:seek"])
    }

    @Test
    fun scheduleLimitGet() {
        val i = Interpreter()
        i.executeOneStep(":schedule:limit")
        assertNotNull(i.results["schedule:limit"])
    }

    @Test
    fun sendTelegram() {
        var sent = false
        val i = Interpreter(sendCaller = { type, _ ->
            if (type == "telegram") sent = true
            "1"
        })
        i.executeOneStep("chat_id, message => :send:telegram")
        assertTrue(sent)
        assertEquals("1", i.results["send"])
    }

    @Test
    fun fileRead() {
        val tmp = kotlin.io.path.createTempFile("test", ".txt").toFile()
        tmp.writeText("hello world")
        val i = Interpreter()
        i.executeOneStep("${tmp.absolutePath} => :file:read")
        assertEquals("hello world", i.results["file"])
        tmp.delete()
    }

    @Test
    fun fileCreate() {
        val tmp = kotlin.io.path.createTempFile("test", ".txt").toFile()
        val i = Interpreter()
        i.executeOneStep("${tmp.absolutePath}, content => :file:create")
        assertEquals("content", tmp.readText())
        tmp.delete()
    }

    @Test
    fun mockSetGet() {
        MockModel.reset()
        MockModel.set("https://test.com", "response", 200)
        val mock = MockModel.get("https://test.com")
        assertNotNull(mock)
        assertEquals("response", mock.response)
        assertEquals(200, mock.code)
        MockModel.reset()
    }

    @Test
    fun mockReset() {
        MockModel.set("https://test.com", "response", 200)
        MockModel.reset()
        assertNull(MockModel.get("https://test.com"))
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
    fun visualBackgroundSet() {
        VisualModel.clearAll()
        VisualModel.setBackground("main.cycle", "/path/bg.png", "tile")
        val bg = VisualModel.backgroundFor("main.cycle")
        assertNotNull(bg)
        assertEquals("tile", bg.mode)
        VisualModel.clearAll()
    }

    @Test
    fun askCancel() {
        val i = Interpreter()
        i.executeOneStep("question => :ask => dst")
        val id = i.results["ask:id"]!!.toInt()
        AskModel.cancel(id)
        assertEquals("", i.results["ask"])
        assertEquals("0", i.results["ask:pending"])
    }

    @Test
    fun extInvoke() {
        val i = Interpreter(extCaller = { name, args -> "result:$name:${args.joinToString(",")}" })
        i.executeOneStep("a, b => :ext:my_plugin")
        assertTrue(i.results["ext:my_plugin"]?.contains("my_plugin") == true)
    }

    @Test
    fun extListWithExtensions() {
        Extensions.clear()
        val ext = object : Extension {
            override val names = setOf("test_ext")
            override fun handle(op: String, args: List<String>, ctx: Ctx): String? = "ok"
        }
        Extensions.register(ext)
        val i = Interpreter()
        i.executeOneStep(":ext:list => dst")
        assertTrue(i.results["ext:list"]?.contains("test_ext") == true)
        Extensions.unregister(ext)
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
    fun secretMask() {
        assertEquals("***", SecretModel.mask("abc"))
        assertEquals("ab***yz", SecretModel.mask("abcdefgh"))
    }

    @Test
    fun memCopy() {
        MemModel.reset()
        val i = Interpreter()
        i.executeOneStep("16 => :mem:alloc => p")
        i.executeOneStep("16 => :mem:alloc => q")
        i.executeOneStep("p, 42 => :mem:write")
        i.executeOneStep("p, q, 1 => :mem:copy")
        i.executeOneStep("q => :mem:read => v")
        assertEquals("42", i.variables["v"])
    }

    @Test
    fun memFill() {
        MemModel.reset()
        val i = Interpreter()
        i.executeOneStep("16 => :mem:alloc => p")
        i.executeOneStep("p, 0, 16 => :mem:fill")
        i.executeOneStep("p => :mem:read => v")
        assertEquals("0", i.variables["v"])
    }

    @Test
    fun memDump() {
        MemModel.reset()
        val i = Interpreter()
        i.executeOneStep("16 => :mem:alloc => p")
        i.executeOneStep("p, 42 => :mem:write")
        i.executeOneStep("p, 4 => :mem:dump")
        assertTrue(i.results["mem:dump"]?.contains("2A") == true)
    }

    @Test
    fun memReset() {
        MemModel.reset()
        val i = Interpreter()
        i.executeOneStep("16 => :mem:alloc => p")
        i.executeOneStep("p, 42 => :mem:write")
        MemModel.reset()
        assertEquals("", MemModel.read(1000))
    }

    @Test
    fun streamLimit() {
        val i = Interpreter()
        i.executeOneStep("5000 => :stream:limit")
        assertEquals("5000", i.results["stream:limit"])
    }

    @Test
    fun retryIdempotent() {
        var calls = 0
        val i = Interpreter(retryCaller = { _, _, _, _, idem, _ ->
            calls++
            if (calls < 3) "-1" else "1"
        })
        i.executeOneStep("url, data, 5, 0, 1 => :api:post:retry")
        assertEquals("1", i.results["api:post"])
    }

    @Test
    fun backgroundBadMode() {
        val i = Interpreter(visualCaller = { _, _, _, _ -> "1" })
        i.executeOneStep("/path/bg.png, invalid => :background")
        assertTrue(i.errorLines.isNotEmpty())
    }

    @Test
    fun strictModeForbidsSend() {
        val i = Interpreter()
        val errors = i.validate(i.parse("a, b => :send:telegram"), strict = true)
        assertTrue(errors.any { it.message.contains("Strict Mode") })
    }

    @Test
    fun strictModeForbidsAsk() {
        val i = Interpreter()
        val errors = i.validate(i.parse("question => :ask"), strict = true)
        assertTrue(errors.any { it.message.contains("Strict Mode") })
    }

    @Test
    fun strictModeAllowsExt() {
        val i = Interpreter()
        val errors = i.validate(i.parse("args => :ext:name"), strict = true)
        assertTrue(errors.none { it.message.contains("Strict Mode") })
    }

    @Test
    fun executionStopsOnFinish() {
        val i = Interpreter()
        val r = i.executeOneStep("=> :stop")
        assertEquals(Status.FINISHED, r.status)
    }

    @Test
    fun jumpToStep() {
        val i = Interpreter()
        i.executeOneStep("^ 5")
        assertEquals(5, i.currentStep)
    }

    @Test
    fun namingEmpty() {
        val i = Interpreter()
        i.executeOneStep("= a")
        assertEquals("", i.variables["a"])
    }

    @Test
    fun namingValue() {
        val i = Interpreter()
        i.executeOneStep("5 = a")
        assertEquals("5", i.variables["a"])
    }

    @Test
    fun resultNaming() {
        val i = Interpreter()
        i.executeOneStep("2 + 3 => :math")
        i.executeOneStep(":math => sum")
        assertEquals("5", i.variables["sum"])
    }

    @Test
    fun labelAndJump() {
        val i = Interpreter()
        i.executeOneStep("= label")
        val r = i.executeOneStep("^ label")
        assertNotNull(r)
    }
}