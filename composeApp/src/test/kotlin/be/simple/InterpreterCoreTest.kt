// ---------- 17. :result naming ----------

@Test fun `result naming math sum`() {
    val i = Interpreter()
    i.executeOneStep("1, 2, 3, 4, 5 => :math:sum")
    i.executeOneStep(":math:sum = sum")
    assertEquals("15", i.variables["sum"])
}

@Test fun `result copy math sum`() {
    val i = Interpreter()
    i.executeOneStep("1, 2, 3, 4, 5 => :math:sum")
    i.executeOneStep(":math:sum => sum")
    assertEquals("15", i.variables["sum"])
}

@Test fun `result naming missing yields empty string`() {
    val i = Interpreter()
    i.executeOneStep(":math:sum = sum")
    assertEquals("", i.variables["sum"])
}

@Test fun `result naming const flag`() {
    val i = Interpreter()
    i.executeOneStep("1, 2 => :math:sum")
    i.executeOneStep(":math:sum = sum @")
    assertTrue("sum" in i.constants)
}

@Test fun `result naming hidden flag`() {
    val i = Interpreter()
    i.executeOneStep("1, 2 => :math:sum")
    i.executeOneStep(":math:sum = sum *")
    assertTrue("sum" in i.hiddenVariables)
}

@Test fun `colon result substitute two-word`() {
    val i = Interpreter()
    i.executeOneStep("1, 2 => :math:sum")
    assertEquals("3", i.substitute(":math:sum"))
}

@Test fun `colon result substitute one-word`() {
    val i = Interpreter()
    i.executeOneStep("hello => :size")
    assertEquals("5", i.substitute(":size"))
}

@Test fun `colon result substitute missing yields empty`() {
    val i = Interpreter()
    assertEquals("", i.substitute(":math:sum"))
    assertEquals("", i.substitute(":size"))
    assertEquals("", i.substitute(":get"))
}

@Test fun `colon fn never substituted`() {
    val i = Interpreter()
    assertEquals(":math", i.substitute(":math"))
    assertEquals(":data", i.substitute(":data"))
    assertEquals(":json", i.substitute(":json"))
    assertEquals(":api", i.substitute(":api"))
}

@Test fun `colon file read not substituted`() {
    val i = Interpreter()
    assertEquals(":file:read", i.substitute(":file:read"))
}

// ---------- 18. data axis semantics ----------

@Test fun `data indexOf default x`() {
    val i = Interpreter()
    i.executeOneStep("hello = s")
    val r = i.executeOneStep("s, ll => :data:indexOf")
    assertNull(r.errorLine)
    assertEquals("3", i.results["indexOf"])
}

@Test fun `data indexOf explicit y`() {
    val i = Interpreter()
    i.executeOneStep("a, b, c = arr")
    val r = i.executeOneStep("arr:y, b => :data:indexOf")
    assertNull(r.errorLine)
    assertEquals("2", i.results["indexOf"])
}

@Test fun `data substring default x`() {
    val i = Interpreter()
    i.executeOneStep("hello = s")
    val r = i.executeOneStep("s, 2, 4 => :data:substring")
    assertNull(r.errorLine)
    assertEquals("ell", i.results["substring"])
}

@Test fun `data substring explicit y errors`() {
    val i = Interpreter()
    i.executeOneStep("a, b, c = arr")
    val r = i.executeOneStep("arr:y, 1, 2 => :data:substring")
    assertNotNull(r.errorLine)
    assertTrue(r.log.contains("only x-axis"))
}

@Test fun `data replace explicit y errors`() {
    val i = Interpreter()
    i.executeOneStep("a, b, c = arr")
    val r = i.executeOneStep("arr:y, a, x => :data:replace")
    assertNotNull(r.errorLine)
}

@Test fun `data chars explicit y errors`() {
    val i = Interpreter()
    i.executeOneStep("a, b, c = arr")
    val r = i.executeOneStep("arr:y, , => :data:chars")
    assertNotNull(r.errorLine)
}

@Test fun `data lower default x`() {
    val i = Interpreter()
    i.executeOneStep("ABC = s")
    val r = i.executeOneStep("s => :data:lower")
    assertNull(r.errorLine)
    assertEquals("abc", i.results["lower"])
}

@Test fun `data lower explicit y`() {
    val i = Interpreter()
    i.executeOneStep("A, B, C = arr")
    val r = i.executeOneStep("arr:y => :data:lower")
    assertNull(r.errorLine)
    assertEquals("a,b,c", i.results["lower"])
}

@Test fun `data charAt default x`() {
    val i = Interpreter()
    i.executeOneStep("hello = s")
    val r = i.executeOneStep("s, 2 => :data:charAt")
    assertNull(r.errorLine)
    assertEquals("e", i.results["charAt"])
}

@Test fun `data charAt explicit y`() {
    val i = Interpreter()
    i.executeOneStep("aa, bb, cc = arr")
    val r = i.executeOneStep("arr:y, 2 => :data:charAt")
    assertNull(r.errorLine)
    assertEquals("bb", i.results["charAt"])
}

@Test fun `data set on x errors`() {
    val i = Interpreter()
    i.executeOneStep("hello = s")
    val r = i.executeOneStep("s, 1, X => :data:set")
    assertNotNull(r.errorLine)
    assertTrue(r.log.contains("only l/y/z"))
}

@Test fun `data set on y works`() {
    val i = Interpreter()
    i.executeOneStep("a, b, c = arr")
    val r = i.executeOneStep("arr:y, 2, X => :data:set")
    assertNull(r.errorLine)
    assertEquals("a,X,c", i.results["set"])
}

// ---------- 19. foreach ----------

@Test fun `foreach sums array`() {
    val i = Interpreter()
    i.executeOneStep("1, 2, 3 = arr")
    i.executeOneStep("0 = sum")
    val r = i.executeOneStep("arr:foreach:y ; n :\nsum + n => :math => sum,\n.")
    assertNull(r.errorLine)
    assertEquals("6", i.variables["sum"])
}

@Test fun `foreach body error propagates`() {
    val i = Interpreter()
    i.executeOneStep("1, 2, 3 = arr")
    val r = i.executeOneStep("arr:foreach:y ; n :\nn -> nothing,\n.")
    assertNotNull(r.errorLine, "body error should propagate")
}

@Test fun `foreach changed includes result`() {
    val i = Interpreter()
    i.executeOneStep("1, 2, 3 = arr")
    i.executeOneStep("0 = sum")
    val r = i.executeOneStep("arr:foreach:y ; n :\nsum + n => :math => sum,\n.")
    assertTrue(r.changed.containsKey("sum"))
    assertEquals("6", r.changed["sum"])
}

// ---------- 20. conditions with parens ----------

@Test fun `condition parens or`() {
    val i = Interpreter()
    i.executeOneStep("1 = a")
    i.executeOneStep("0 = b")
    i.executeOneStep("(a == 1 || b == 1) ? 5 = c")
    assertEquals("5", i.variables["c"])
}

@Test fun `condition parens and`() {
    val i = Interpreter()
    i.executeOneStep("1 = a")
    i.executeOneStep("1 = b")
    i.executeOneStep("(a == 1 && b == 1) ? 7 = c")
    assertEquals("7", i.variables["c"])
}

@Test fun `condition not with parens`() {
    val i = Interpreter()
    i.executeOneStep("0 = a")
    i.executeOneStep("!(a == 1) ? 9 = c")
    assertEquals("9", i.variables["c"])
}

@Test fun `condition nested parens`() {
    val i = Interpreter()
    i.executeOneStep("1 = a")
    i.executeOneStep("0 = b")
    i.executeOneStep("1 = c")
    i.executeOneStep("(a == 1 && (b == 1 || c == 1)) ? 11 = d")
    assertEquals("11", i.variables["d"])
}

@Test fun `condition precedence and over or`() {
    val i = Interpreter()
    i.executeOneStep("0 = a")
    i.executeOneStep("1 = b")
    i.executeOneStep("1 = c")
    i.executeOneStep("a == 1 || b == 1 && c == 1 ? 13 = d")
    assertEquals("13", i.variables["d"])
}

// ---------- 21. math booleans with parens ----------

@Test fun `math bool or parens`() {
    val i = Interpreter()
    val r = i.executeOneStep("(1 == 1 || 2 == 2) => :math")
    assertNull(r.errorLine)
    assertEquals("1", i.results["math"])
}

@Test fun `math bool and parens`() {
    val i = Interpreter()
    val r = i.executeOneStep("(1 == 1 && 2 == 2) => :math")
    assertNull(r.errorLine)
    assertEquals("1", i.results["math"])
}

@Test fun `math bool not`() {
    val i = Interpreter()
    val r = i.executeOneStep("!(1 == 1) => :math")
    assertNull(r.errorLine)
    assertEquals("0", i.results["math"])
}

@Test fun `math compare arithmetic`() {
    val i = Interpreter()
    val r = i.executeOneStep("1 + 2 == 3 => :math")
    assertNull(r.errorLine)
    assertEquals("1", i.results["math"])
}

@Test fun `math compare arithmetic false`() {
    val i = Interpreter()
    val r = i.executeOneStep("1 + 2 == 4 => :math")
    assertNull(r.errorLine)
    assertEquals("0", i.results["math"])
}

@Test fun `math nested bool`() {
    val i = Interpreter()
    val r = i.executeOneStep("(1 + 1 == 2 && (2 + 2 == 4 || 1 == 2)) => :math")
    assertNull(r.errorLine)
    assertEquals("1", i.results["math"])
}

@Test fun `math bool precedence`() {
    val i = Interpreter()
    val r = i.executeOneStep("0 == 1 || 1 == 1 && 2 == 2 => :math")
    assertNull(r.errorLine)
    assertEquals("1", i.results["math"])
}

// ---------- 22. misc fixes ----------

@Test fun `detect separator prefers comma over dot`() {
    val i = Interpreter()
    val r = i.executeOneStep("1.5, 2.5, 3 => :math:sum")
    assertNull(r.errorLine)
    assertEquals("7", i.results["math:sum"])
}

@Test fun `dollar chain substitutes multiple`() {
    val i = Interpreter()
    i.executeOneStep("1 = a")
    i.executeOneStep("2 = b")
    i.executeOneStep("3 = c")
    assertEquals("123", i.substitute("a\$b\$c"))
}

@Test fun `dollar chain with results`() {
    val i = Interpreter()
    i.executeOneStep("1 = a")
    i.executeOneStep("hello => :size")
    assertEquals("15", i.substitute("a\$:size"))
}

@Test fun `question mark at position 0 errors`() {
    val i = Interpreter()
    val r = i.executeOneStep("? 5 = a")
    assertNotNull(r.errorLine)
}