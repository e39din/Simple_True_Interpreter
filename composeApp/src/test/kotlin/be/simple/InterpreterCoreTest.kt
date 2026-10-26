package be.simple

import be.`true`.AgentAction
import be.`true`.Interpreter
import be.`true`.Status
import kotlin.test.*

class InterpreterCoreTest {

    private fun stepOnce(code: String): Pair<Interpreter, be.`true`.StepResult> {
        val i = Interpreter()
        i.actions = ":run"
        val r = i.executeOneStep(code)
        return i to r
    }

    // ---------- 1. parse / validate ----------

    @Test fun `parse numbers lines from 1`() {
        val lines = Interpreter().parse("a\nb\nc")
        assertEquals(3, lines.size)
        assertEquals(1, lines[0].number)
        assertEquals(3, lines[2].number)
    }

    @Test fun `validate unknown label`() {
        val interp = Interpreter()
        val errs = interp.validate(interp.parse("^ nope\n=> :stop"))
        assertEquals(1, errs.size)
        assertTrue(errs[0].message.contains("Unknown jump target"))
    }

    @Test fun `validate step out of range`() {
        val interp = Interpreter()
        val errs = interp.validate(interp.parse("^ 99"))
        assertEquals(1, errs.size)
    }

    @Test fun `validate multiple question marks`() {
        val interp = Interpreter()
        val errs = interp.validate(interp.parse("a ? b ? c"))
        assertTrue(errs.any { it.message.contains("Multiple ?") })
    }

    @Test fun `validate return inside function`() {
        val interp = Interpreter()
        val code = "s:f:\nvalue => :result\n:r"
        val errs = interp.validate(interp.parse(code))
        assertTrue(errs.any { it.message.contains("inside function") })
    }

    @Test fun `strict mode forbids system colon`() {
        val interp = Interpreter()
        val errs = interp.validate(interp.parse("a => :math"), strict = true)
        assertTrue(errs.any { it.message.contains("Strict Mode") })
    }

    @Test fun `strict mode allows user file name`() {
        val interp = Interpreter()
        interp.tabExists = { it == "test" }
        val errs = interp.validate(interp.parse("1 => :test"), strict = true)
        assertTrue(errs.isEmpty(), "unexpected: $errs")
    }

    @Test fun `strict mode allows substitution`() {
        val interp = Interpreter()
        val errs = interp.validate(interp.parse("func1"), strict = true)
        assertTrue(errs.isEmpty(), "unexpected: $errs")
    }

    // ---------- 2. naming / const / hidden ----------

    @Test fun `naming assigns value`() {
        val (i, _) = stepOnce("42 = a")
        assertEquals("42", i.variables["a"])
    }

    @Test fun `naming const flag`() {
        val (i, _) = stepOnce("42 = a @")
        assertTrue("a" in i.constants)
    }

    @Test fun `naming hidden flag`() {
        val (i, _) = stepOnce("42 = a *")
        assertTrue("a" in i.hiddenVariables)
    }

    @Test fun `cannot overwrite constant`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("1 = a @")
        val r = i.executeOneStep("2 = a")
        assertNotNull(r.errorLine)
    }

    @Test fun `nothing is reserved`() {
        val i = Interpreter()
        i.actions = ":run"
        val r = i.executeOneStep("5 = nothing")
        assertNotNull(r.errorLine)
        assertTrue(r.log.contains("reserved"))
    }

    @Test fun `system name is reserved`() {
        val i = Interpreter()
        i.actions = ":run"
        val r = i.executeOneStep("5 = result")
        assertNotNull(r.errorLine)
        assertTrue(r.log.contains("reserved"))
    }

    // ---------- 3. copy / move ----------

    @Test fun `copy keeps source`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("5 = a")
        i.executeOneStep("a => b")
        assertEquals("5", i.variables["a"])
        assertEquals("5", i.variables["b"])
    }

    @Test fun `copy from indexed`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("1,2,3 = arr")
        i.executeOneStep("arr:y:2 => b")
        assertEquals("2", i.variables["b"])
    }

    @Test fun `copy does not remove source`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("5 = a")
        i.executeOneStep("a => b")
        assertEquals("5", i.variables["a"])
    }

    @Test fun `copy undeclared source errors`() {
        val i = Interpreter()
        i.actions = ":run"
        val r = i.executeOneStep("unknown => b")
        assertNotNull(r.errorLine)
    }

    @Test fun `move clears source value but keeps name`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("5 = a")
        i.executeOneStep("a -> b")
        assertEquals("", i.variables["a"])
        assertEquals("5", i.variables["b"])
    }

    @Test fun `move undeclared source errors`() {
        val i = Interpreter()
        i.actions = ":run"
        val r = i.executeOneStep("unknown -> b")
        assertNotNull(r.errorLine)
    }

    // ---------- 4. substitute ----------

    @Test fun `substitute word boundary`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("X = a")
        assertEquals("data", i.substitute("data"))
    }

    @Test fun `substitute indexed`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("a, b, c = arr")
        assertEquals("b", i.substitute("arr:y:2"))
    }

    @Test fun `substitute dollar concat`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("1 = a")
        i.executeOneStep("2 = b")
        assertEquals("12", i.substitute("a\$b"))
    }

    // ---------- 5. math ----------

    @Test fun `math simple arithmetic`() {
        val (i, _) = stepOnce("1 + 2 * 3 => :math")
        assertEquals("7", i.results["math"])
    }

    @Test fun `math power right assoc`() {
        val (i, _) = stepOnce("2 ** 3 ** 2 => :math")
        assertEquals("512", i.results["math"])
    }

    @Test fun `math floor divide by zero safe`() {
        val (i, _) = stepOnce("1 // 0 => :math")
        assertEquals("0", i.results["math"])
    }

    @Test fun `math unary minus`() {
        val (i, _) = stepOnce("-3 + 5 => :math")
        assertEquals("2", i.results["math"])
    }

    @Test fun `math list sum`() {
        val (i, _) = stepOnce("1, 2, 3, 4, 5 => :math:sum")
        assertEquals("15", i.results["math:sum"])
    }

    @Test fun `math chain into variable`() {
        val (i, _) = stepOnce("1 + 1 => :math => x")
        assertEquals("2", i.variables["x"])
    }

    @Test fun `math chain to undeclared is naming`() {
        val (i, r) = stepOnce("1 + 1 => :math => x")
        assertNull(r.errorLine)
        assertEquals("2", i.variables["x"])
    }

    // ---------- 6. data ----------

    @Test fun `data push on y`() {
        val (i, _) = stepOnce("1, 2 = arr\narr:y, 3 => :data:push")
        assertEquals("1,2,3", i.results["push"])
    }

    @Test fun `data size on y`() {
        val (i, _) = stepOnce("a, b, c = arr\narr:y => :data:size")
        assertEquals("3", i.results["size"])
    }

    @Test fun `data charAt 1-based`() {
        val (i, _) = stepOnce("hello = s\ns, 2 => :data:charAt")
        assertEquals("e", i.results["charAt"])
    }

    @Test fun `data insert deprecated`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("1, 2 = arr")
        val r = i.executeOneStep("arr:y, 1, 5 => :data:insert")
        assertNotNull(r.errorLine)
        assertTrue(r.log.contains("deprecated"))
    }

    @Test fun `data set deprecated`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("1, 2 = arr")
        val r = i.executeOneStep("arr:y, 1, 5 => :data:set")
        assertNotNull(r.errorLine)
        assertTrue(r.log.contains("deprecated"))
    }

    @Test fun `data length deprecated`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("1, 2 = arr")
        val r = i.executeOneStep("arr:y => :data:length")
        assertNotNull(r.errorLine)
        assertTrue(r.log.contains("deprecated"))
    }

    @Test fun `data peek`() {
        val (i, _) = stepOnce("1,2,3 = arr\narr:y => :peek")
        assertEquals("3", i.results["peek"])
    }

    @Test fun `data stack peek alias`() {
        val (i, _) = stepOnce("1,2,3 = arr\narr:y => :stack:peek")
        assertEquals("3", i.results["peek"])
    }

    // ---------- 7. conditions ----------

    @Test fun `condition true executes action`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("1 = a")
        i.executeOneStep("a == 1 ? 2 = b")
        assertEquals("2", i.variables["b"])
    }

    @Test fun `condition false skips action`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("1 = a")
        i.executeOneStep("a == 2 ? 2 = b")
        assertNull(i.variables["b"])
    }

    @Test fun `question mark without operation stays text`() {
        val (i, _) = stepOnce("hello ? world")
        assertEquals(Status.STOPPED, i.status)
    }

    @Test fun `question mark at position 0 errors`() {
        val i = Interpreter()
        i.actions = ":run"
        val r = i.executeOneStep("? 5 = a")
        assertNotNull(r.errorLine)
    }

    // ---------- 8. jumps ----------

    @Test fun `jump to number`() {
        val code = "=> :pause\n^ 4\n=> :stop\n=> :stop"
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep(code)
        i.setStatusPublic(Status.RUNNING)
        val r = i.executeOneStep(code)
        assertTrue(r.log.contains("Jump"))
        assertEquals(4, r.currentStep)
    }

    @Test fun `jump to label`() {
        val code = "= pause\n=> :pause\n^ pause\n=> :stop"
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep(code)
        val r = i.executeOneStep(code)
        assertTrue(r.log.contains("Jump"))
    }

    @Test fun `jump to file via command`() {
        val i = Interpreter()
        i.actions = ":run"
        val r = i.executeOneStep("^ main.cycle")
        assertEquals("file:main.cycle", r.command)
    }

    @Test fun `jump to missing file errors`() {
        val i = Interpreter()
        i.actions = ":run"
        val r = i.executeOneStep("^ notes")
        assertNotNull(r.errorLine)
    }

    // ---------- 9. commands ----------

    @Test fun `stop command finishes`() {
        val (_, r) = stepOnce("=> :stop")
        assertEquals(Status.FINISHED, r.status)
    }

    @Test fun `pause command pauses`() {
        val (_, r) = stepOnce("=> :pause")
        assertEquals(Status.PAUSED, r.status)
    }

    // ---------- 10. history ----------

    @Test fun `step back restores previous state`() {
        val code = "1 = a\n2 = a\n=> :stop"
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep(code)
        i.executeOneStep(code)
        assertEquals("2", i.variables["a"])
        i.executeStepBack()
        assertEquals("1", i.variables["a"])
    }

    @Test fun `step back on empty history returns null`() {
        val i = Interpreter()
        assertNull(i.executeStepBack())
    }

    @Test fun `reset clears all state`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("1 = a @")
        i.executeOneStep("2 = b *")
        i.reset()
        assertTrue(i.variables.isEmpty())
        assertTrue(i.constants.isEmpty())
        assertTrue(i.hiddenVariables.isEmpty())
        assertTrue(i.results.isEmpty())
        assertEquals(Status.STOPPED, i.status)
        assertEquals(1, i.currentStep)
        assertNull(i.waitUntilMs)
        assertNull(i.timeoutMs)
    }

    // ---------- 11. agent ----------

    @Test fun `agent status regex matches`() {
        val i = Interpreter()
        i.actions = ":run"
        var captured: AgentAction? = null
        i.agentCaller = { _, _, action, _ -> captured = action }
        i.executeOneStep(":agent:main:status => dst")
        assertEquals(AgentAction.STATUS, captured)
    }

    @Test fun `agent waiter regex matches`() {
        val i = Interpreter()
        i.actions = ":run"
        var captured: AgentAction? = null
        i.agentCaller = { _, _, action, _ -> captured = action }
        i.executeOneStep(":agent:main:waiter => dst")
        assertEquals(AgentAction.WAITER, captured)
    }

    @Test fun `agent status all regex matches`() {
        val i = Interpreter()
        i.actions = ":run"
        var captured: AgentAction? = null
        var name: String? = null
        i.agentCaller = { _, n, action, _ -> captured = action; name = n }
        i.executeOneStep(":agent:status => dst")
        assertEquals(AgentAction.STATUS, captured)
        assertEquals("*", name)
    }

    // ---------- 12. agent wait ----------

    @Test fun `agent wait regex matches 100`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("100 => :agent:wait")
        assertNotNull(i.waitUntilMs)
        assertNull(i.timeoutMs)
    }

    @Test fun `wait sets paused`() {
        val i = Interpreter()
        i.actions = ":run"
        val r = i.executeOneStep("100 => :agent:wait")
        assertEquals(Status.PAUSED, r.status)
    }

    @Test fun `wait timeout sets timeoutMs`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("100, 5000 => :agent:wait:timeout")
        assertNotNull(i.waitUntilMs)
        assertEquals(5000L, i.timeoutMs)
    }

    @Test fun `reset clears waitUntilMs`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("100 => :agent:wait")
        assertNotNull(i.waitUntilMs)
        i.reset()
        assertNull(i.waitUntilMs)
        assertNull(i.timeoutMs)
    }

    // ---------- 13. result ----------

    @Test fun `result naming math sum`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("1, 2, 3, 4, 5 => :math:sum")
        i.executeOneStep(":math:sum = sum")
        assertEquals("15", i.variables["sum"])
    }

    @Test fun `result copy math sum`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("1, 2, 3, 4, 5 => :math:sum")
        i.executeOneStep(":math:sum => sum")
        assertEquals("15", i.variables["sum"])
    }

    @Test fun `result naming missing yields empty string`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep(":math:sum = sum")
        assertEquals("", i.variables["sum"])
    }

    @Test fun `result command sets results`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("42 => :result")
        assertEquals("42", i.results["result"])
        assertEquals(Status.FINISHED, i.status)
    }

    @Test fun `result marker in code`() {
        val i = Interpreter()
        i.actions = ":run"
        val r = i.executeOneStep(":result")
        assertEquals(Status.FINISHED, r.status)
    }

    // ---------- 14. data axis semantics ----------

    @Test fun `data indexOf default x`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("hello = s")
        val r = i.executeOneStep("s, ll => :data:indexOf")
        assertNull(r.errorLine)
        assertEquals("3", i.results["indexOf"])
    }

    @Test fun `data indexOf explicit y`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("a, b, c = arr")
        val r = i.executeOneStep("arr:y, b => :data:indexOf")
        assertNull(r.errorLine)
        assertEquals("2", i.results["indexOf"])
    }

    @Test fun `data substring explicit y errors`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("a, b, c = arr")
        val r = i.executeOneStep("arr:y, 1, 2 => :data:substring")
        assertNotNull(r.errorLine)
        assertTrue(r.log.contains("only x-axis"))
    }

    @Test fun `data lower explicit y`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("A, B, C = arr")
        val r = i.executeOneStep("arr:y => :data:lower")
        assertNull(r.errorLine)
        assertEquals("a,b,c", i.results["lower"])
    }

    // ---------- 15. functional ----------

    @Test fun `map basic`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("1, 2, 3 = arr")
        val r = i.executeOneStep("arr:y, n + 1 => :map")
        assertNull(r.errorLine)
        assertEquals("2,3,4", i.results["map"])
    }

    @Test fun `map with axis from arr`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("1,2,3 = arr")
        i.executeOneStep("arr:y, n * 2 => :map")
        assertEquals("2,4,6", i.results["map"])
    }

    @Test fun `map empty`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("= arr")
        i.executeOneStep("arr:y, n + 1 => :map")
        assertEquals("", i.results["map"])
    }

    @Test fun `map with container arg`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("n * 2 => out = dbl")
        i.executeOneStep("1,2,3 = arr")
        i.executeOneStep("arr:y, dbl => :map")
        assertEquals("2,4,6", i.results["map"])
    }

    @Test fun `reduce sum`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("1,2,3 = arr")
        i.executeOneStep("arr:y, 0, acc + n => :reduce")
        assertEquals("6", i.results["reduce"])
    }

    @Test fun `reduce empty returns init`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("= arr")
        i.executeOneStep("arr:y, 42, acc + n => :reduce")
        assertEquals("42", i.results["reduce"])
    }

    @Test fun `fold product`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("1,2,3 = arr")
        i.executeOneStep("arr:y, 1, acc * n => :fold")
        assertEquals("6", i.results["fold"])
    }

    @Test fun `each with log`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("1,2,3 = arr")
        i.executeOneStep("arr:y, n => :log => :each")
        assertTrue(i.log.any { it.text == "1" })
        assertTrue(i.log.any { it.text == "2" })
        assertTrue(i.log.any { it.text == "3" })
    }

    @Test fun `each empty no error`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("= arr")
        val r = i.executeOneStep("arr:y, n => :log => :each")
        assertNull(r.errorLine)
    }

    // ---------- 16. conditions with parens ----------

    @Test fun `condition parens or`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("1 = a")
        i.executeOneStep("0 = b")
        i.executeOneStep("(a == 1 || b == 1) ? 5 = c")
        assertEquals("5", i.variables["c"])
    }

    @Test fun `condition parens and`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("1 = a")
        i.executeOneStep("1 = b")
        i.executeOneStep("(a == 1 && b == 1) ? 7 = c")
        assertEquals("7", i.variables["c"])
    }

    @Test fun `condition not with parens`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("0 = a")
        i.executeOneStep("!(a == 1) ? 9 = c")
        assertEquals("9", i.variables["c"])
    }

    @Test fun `condition nested parens`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("1 = a")
        i.executeOneStep("0 = b")
        i.executeOneStep("1 = c")
        i.executeOneStep("(a == 1 && (b == 1 || c == 1)) ? 11 = d")
        assertEquals("11", i.variables["d"])
    }

    // ---------- 17. math booleans ----------

    @Test fun `math bool or parens`() {
        val (i, r) = stepOnce("(1 == 1 || 2 == 2) => :math")
        assertNull(r.errorLine)
        assertEquals("1", i.results["math"])
    }

    @Test fun `math compare arithmetic`() {
        val (i, r) = stepOnce("1 + 2 == 3 => :math")
        assertNull(r.errorLine)
        assertEquals("1", i.results["math"])
    }

    @Test fun `math bool not`() {
        val (i, r) = stepOnce("!(1 == 1) => :math")
        assertNull(r.errorLine)
        assertEquals("0", i.results["math"])
    }

    // ---------- 18. misc ----------

    @Test fun `detect separator prefers comma over dot`() {
        val (i, r) = stepOnce("1.5, 2.5, 3 => :math:sum")
        assertNull(r.errorLine)
        assertEquals("7", i.results["math:sum"])
    }

    @Test fun `dollar chain substitutes multiple`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("1 = a")
        i.executeOneStep("2 = b")
        i.executeOneStep("3 = c")
        assertEquals("123", i.substitute("a\$b\$c"))
    }

    // ---------- 19. log / message ----------

    @Test fun `log fn writes to log`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("hello => :log")
        assertTrue(i.log.any { it.text == "hello" && it.type == "log" })
    }

    @Test fun `log bare writes current line`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep(":log")
        assertTrue(i.log.any { it.type == "log" })
    }

    @Test fun `message fn writes type message`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("hello => :message")
        assertTrue(i.log.any { it.text == "hello" && it.type == "message" })
        assertEquals("hello", i.results["message"])
    }

    // ---------- 20. actions ----------

    @Test fun `default actions runs instruction`() {
        val i = Interpreter()
        i.executeOneStep("5 = a")
        assertEquals("5", i.variables["a"])
    }

    @Test fun `actions log then run`() {
        val i = Interpreter()
        i.actions = ":log\n:run"
        i.executeOneStep("5 = a")
        assertEquals("5", i.variables["a"])
        assertTrue(i.log.any { it.text == "5 = a" })
    }

    @Test fun `actions no run skips instruction`() {
        val i = Interpreter()
        i.actions = ":log"
        i.executeOneStep("5 = a")
        assertNull(i.variables["a"])
    }

    // ---------- 21. file operations ----------

    @Test fun `file to var errors if missing`() {
        val i = Interpreter()
        i.actions = ":run"
        val r = i.executeOneStep("file:/nonexistent/path/xyz => content")
        assertNotNull(r.errorLine)
    }

    @Test fun `var to file errors if source empty`() {
        val i = Interpreter()
        i.actions = ":run"
        val r = i.executeOneStep("unknownvar => file:/tmp/out.txt")
        assertNotNull(r.errorLine)
    }

    // ---------- 22. insert ----------

    @Test fun `insert into y`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("a, c = arr")
        val r = i.executeOneStep("b ->| arr:y:2")
        assertNull(r.errorLine)
        assertEquals("a,b,c", i.variables["arr"])
    }

    @Test fun `insert errors if container missing`() {
        val i = Interpreter()
        i.actions = ":run"
        val r = i.executeOneStep("5 ->| unknown:y:1")
        assertNotNull(r.errorLine)
    }

    // ---------- 23. short size ----------

    @Test fun `short size x`() {
        val (i, r) = stepOnce("hello => :s")
        assertNull(r.errorLine)
        assertEquals("5", i.results["size"])
    }

    @Test fun `short size y`() {
        val (i, r) = stepOnce("1, 2, 3 = arr\narr:y => :s")
        assertNull(r.errorLine)
        assertEquals("3", i.results["size"])
    }

    @Test fun `short size get`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("hello => :s")
        val r = i.executeOneStep(":s => dst")
        assertNull(r.errorLine)
        assertEquals("5", i.variables["dst"])
    }

    // ---------- 24. range / repeat ----------

    @Test fun `range ascending`() {
        val (i, r) = stepOnce("1, 5 => :range")
        assertNull(r.errorLine)
        assertEquals("1,2,3,4,5", i.results["range"])
    }

    @Test fun `range descending`() {
        val (i, r) = stepOnce("5, 1 => :range")
        assertNull(r.errorLine)
        assertEquals("5,4,3,2,1", i.results["range"])
    }

    @Test fun `repeat action`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("0 = a")
        i.executeOneStep("a + 1 => a = action")
        val r = i.executeOneStep("action, 5 => :repeat")
        assertNull(r.errorLine)
        assertEquals("5", i.variables["a"])
    }

    // ---------- 25. error ----------

    @Test fun `error empty initially`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep(":error => e")
        assertEquals("", i.variables["e"])
    }

    @Test fun `error after error`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("nope -> nothing")
        i.executeOneStep(":error => e")
        assertEquals("1", i.variables["e"])
    }

    @Test fun `error line`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("nope -> nothing")
        i.executeOneStep(":error:line => l")
        assertEquals("1", i.variables["l"])
    }

    @Test fun `reset clears errors`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("nope -> nothing")
        i.executeOneStep(":error => e")
        assertEquals("1", i.variables["e"])
        i.reset()
        i.executeOneStep(":error => e2")
        assertEquals("", i.variables["e2"])
    }

    // ---------- 26. short data / math ----------

    @Test fun `short sum`() {
        val (i, r) = stepOnce("1, 2, 3 => :sum")
        assertNull(r.errorLine)
        assertEquals("6", i.results["math:sum"])
    }

    @Test fun `short pop`() {
        val (i, r) = stepOnce("1, 2, 3 = arr\narr:y => :pop")
        assertNull(r.errorLine)
        assertEquals("3", i.results["pop"])
    }

    @Test fun `short size`() {
        val (i, r) = stepOnce("1, 2, 3 = arr\narr:y => :size")
        assertNull(r.errorLine)
        assertEquals("3", i.results["size"])
    }

    @Test fun `short sort`() {
        val (i, r) = stepOnce("c, a, b = arr\narr:y => :sort")
        assertNull(r.errorLine)
        assertEquals("a,b,c", i.results["sort"])
    }

    @Test fun `math sort`() {
        val (i, r) = stepOnce("3, 1, 2 => :math:sort")
        assertNull(r.errorLine)
        assertEquals("1,2,3", i.results["math:sort"])
    }

    // ---------- 27. chains ----------

    @Test fun `chain math to var`() {
        val (i, _) = stepOnce("1 + 1 => :math => x")
        assertEquals("2", i.variables["x"])
    }

    @Test fun `chain size to var`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("hello = s")
        val r = i.executeOneStep("s => :size => n")
        assertNull(r.errorLine)
        assertEquals("5", i.variables["n"])
    }

    // ---------- 28. jump commands ----------

    @Test fun `jump stop command`() {
        val i = Interpreter()
        i.actions = ":run"
        val r = i.executeOneStep("^ :stop")
        assertEquals(":stop", r.command)
    }

    @Test fun `jump pause command`() {
        val i = Interpreter()
        i.actions = ":run"
        val r = i.executeOneStep("^ :pause")
        assertEquals(":pause", r.command)
    }

    @Test fun `jump resume command`() {
        val i = Interpreter()
        i.actions = ":run"
        val r = i.executeOneStep("^ :resume")
        assertEquals(":resume", r.command)
    }

    @Test fun `jump delete command`() {
        val i = Interpreter()
        i.actions = ":run"
        val r = i.executeOneStep("^ :delete")
        assertEquals(":delete", r.command)
    }

    @Test fun `jump reset command`() {
        val i = Interpreter()
        i.actions = ":run"
        val r = i.executeOneStep("^ :reset")
        assertEquals(":reset", r.command)
    }

    @Test fun `jump unknown bare errors`() {
        val i = Interpreter()
        i.actions = ":run"
        val r = i.executeOneStep("^ notes")
        assertNotNull(r.errorLine)
    }

    // ---------- 29. substitution of named containers (F1) ----------

    @Test fun `named container with action substitutes`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("0 = sum")
        i.executeOneStep("sum + 1 => sum = inc")
        assertEquals("0", i.variables["sum"])
        i.executeOneStep("inc")
        assertEquals("1", i.variables["sum"])
        i.executeOneStep("inc")
        assertEquals("2", i.variables["sum"])
    }

    @Test fun `literal backslash prevents substitution`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("0 = sum")
        i.executeOneStep("sum + 1 => sum = inc")
        val r = i.executeOneStep("\\ inc")
        assertEquals("0", i.variables["sum"])
    }

    // ---------- 30. escaping ----------

    @Test fun `escaped comma in args`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("a\\,b, c => :log")
        assertTrue(i.log.any { it.text.contains("a,b") })
    }

    @Test fun `escaped semicolon`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("a\\;b => :log")
        assertTrue(i.log.any { it.text.contains("a;b") })
    }

    @Test fun `escaped dot`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("a\\.b => :log")
        assertTrue(i.log.any { it.text.contains("a.b") })
    }

    // ---------- 31. foreach removed (F4) ----------

    @Test fun `foreach removed yields unknown op`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("1,2,3 = arr")
        val r = i.executeOneStep("arr:foreach:y ; n :\n n => :log")
        assertNotNull(r.errorLine)
    }

    // ---------- 32. :error format (4) ----------

    @Test fun `error set via message`() {
        val i = Interpreter()
        i.actions = ":run"
        val r = i.executeOneStep("\"fail\" => :error")
        assertEquals(Status.PAUSED, r.status)
        assertEquals("1", i.results["error"])
        assertTrue(i.results["error:message"]!!.startsWith("custom, 1,"))
    }

    @Test fun `error set with condition true`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("!(1 == 2) ? \"x\" => :error")
        assertEquals("1", i.results["error"])
    }

    @Test fun `error set with condition false skips`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("!(1 == 1) ? \"x\" => :error")
        assertNull(i.results["error"])
    }

    @Test fun `error resolve sets zero`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("nope -> nothing")
        i.executeOneStep("0 => :error:resolve")
        val msg = i.results["error:message"] ?: ""
        assertTrue(msg.contains(", 0,"), "got: $msg")
    }

    @Test fun `error set accumulates lines`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("f1 = f1")
        i.executeOneStep("f1")
        assertNotNull(i.results["error:message"])
    }

    @Test fun `error resolve log entry resolved`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("\"fail\" => :error")
        i.executeOneStep("0 => :error:resolve")
        assertTrue(i.log.any { it.type == "error" && it.resolved })
    }

    // ---------- 33. settings (6) ----------

    @Test fun `history limit default`() {
        val i = Interpreter()
        assertEquals(100, i.historyLimit)
    }

    @Test fun `log limit default`() {
        val i = Interpreter()
        assertEquals(1000, i.logLimit)
    }

    @Test fun `strict mode default false`() {
        val i = Interpreter()
        assertFalse(i.strictMode)
    }

    @Test fun `auto sync default true`() {
        val i = Interpreter()
        assertTrue(i.autoSync)
    }

    // ---------- 34. stack peek (REC) ----------

    @Test fun `stack peek top`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("1,2,3 = arr")
        val r = i.executeOneStep("arr:y => :stack:peek")
        assertNull(r.errorLine)
        assertEquals("3", i.results["peek"])
    }

    @Test fun `stack peek empty`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("= arr")
        val r = i.executeOneStep("arr:y => :stack:peek")
        assertNull(r.errorLine)
        assertEquals("", i.results["peek"])
    }

    // ---------- 35. :simple (S) ----------

    @Test fun `simple open invokes caller`() {
        val i = Interpreter()
        i.actions = ":run"
        var captured: String? = null
        i.simpleCaller = { action, _ -> captured = action }
        i.executeOneStep("\"/tmp/x\" => :simple:open")
        assertEquals("open", captured)
    }

    @Test fun `simple save invokes caller`() {
        val i = Interpreter()
        i.actions = ":run"
        var captured: String? = null
        i.simpleCaller = { action, _ -> captured = action }
        i.executeOneStep("=> :simple:save")
        assertEquals("save", captured)
    }

    @Test fun `simple newCycle invokes caller`() {
        val i = Interpreter()
        i.actions = ":run"
        var captured: String? = null
        i.simpleCaller = { action, _ -> captured = action }
        i.executeOneStep("=> :simple:newCycle")
        assertEquals("newCycle", captured)
    }

    // ---------- 36. substitution preserves state ----------

    @Test fun `substitution body sees variables`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("10 = a")
        i.executeOneStep("a + 5 => a = inc")
        i.executeOneStep("inc")
        assertEquals("15", i.variables["a"])
    }

    // ---------- 37. naming rule (N) ----------

    @Test fun `result naming to undeclared is naming`() {
        val i = Interpreter()
        i.actions = ":run"
        i.executeOneStep("1,2,3 => :sum")
        i.executeOneStep(":math:sum = mySum")
        assertEquals("6", i.variables["mySum"])
    }

    @Test fun `math chain to undeclared names`() {
        val i = Interpreter()
        i.actions = ":run"
        val r = i.executeOneStep("5 + 5 => :math => result2")
        assertNull(r.errorLine)
        assertEquals("10", i.variables["result2"])
    }
}