# True — quick start for programmers

If you know Python, Kotlin, Rust, Lua, or Erlang — this page is for you. Full spec — `LANGUAGE.md`.

## Terminology

- **Container** — a string of data. Everything in the language is a container.
- **Cycle** — a `.cycle` file (a file-function).
- **Agent** — a `.agent` file (a file-function + agent UI buttons).
- **`.true`** — no longer used (reserved for settings).

## 11 concepts

### 1. Data first, operations after

```python
sum([1, 2, 3])          # Python
```
```true
1, 2, 3 => :sum
```

**Naming — also on the right:** `data = name` (not `name = data`).

### 2. Everything is a container

| Python | Kotlin | Rust | Lua | Erlang | True |
|---|---|---|---|---|---|
| `5` | `5` | `5` | `5` | `5` | `5` |
| `"5"` | `"5"` | `"5"` | `"5"` | `"5"` | `5` (or `"5"` — 3 chars) |
| `[1, 2, 3]` | `listOf(1, 2, 3)` | `vec![1, 2, 3]` | `{1, 2, 3}` | `[1, 2, 3]` | `1,2,3` |
| `{"a": 1}` | `mapOf("a" to 1)` | `HashMap` | `{a = 1}` | `#{a => 1}` | `{'a':1}` |

### 3. Agents execute the program (not threads)

| Python | Kotlin | Rust | Lua | Erlang | True |
|---|---|---|---|---|---|
| `threading.Thread` | `Thread { }` | `std::thread::spawn` | `coroutine.create` | `spawn` | `^ :pause` |
| `asyncio` | `coroutineScope` | `tokio::spawn` | `coroutine.yield` | `receive` | `100 => :agent:wait` |
| `lock` | `Mutex` | `Mutex` | — | — | not needed (1 `.agent` = 1 agent) |

### 4. The program changes itself (code — database)

```true
0 = a
a + 1 => :math
:math = a
```

After the step: `0 = a` → `1 = a`.

### 5. Visual clarity over encapsulation

No `private`, no `_private`. Everything is visible.

### 6. File = storage + function

```true
math:fact:
parameters == 0 ? 1 => :result
= sub
parameters - 1 => .:math:fact => sub
parameters * sub => :math => :result
:result
```

| Python | Kotlin | Rust | Lua | Erlang | True |
|---|---|---|---|---|---|
| `def fact(n): ...` | `fun fact(n: Int) = ...` | `fn fact(n: i32) -> i32 { ... }` | `function fact(n) ... end` | `fact(N) -> ...` | `:math:fact:` + `:result` |

### 7. Program is a loop

| Python | Kotlin | Rust | Lua | Erlang | True |
|---|---|---|---|---|---|
| `while True:` | `while (true)` | `loop { }` | `while true do end` | — | (implicit) |
| `break` | `break` | `break` | `break` | — | `^ label` (not `=> :stop`!) |
| — | — | — | — | — | `=> :stop` — **program**, not loop |

### 8. Program is a switch

| Python | Kotlin | Rust | Lua | Erlang | True |
|---|---|---|---|---|---|
| `if c: ...` | `if (c) { ... }` | `if c { ... }` | `if c then ... end` | `case` | `c ? ...` |
| `elif` | `else if` | `else if` | `elseif` | `;` | `c2 ? ...` |
| `match` | `when` | `match` | — | `case` | `^ N` |

### 9. Clarity over brevity

| Python | Kotlin | Rust | Lua | Erlang | True |
|---|---|---|---|---|---|
| `[x for x in xs if x > 0]` | `xs.filter { it > 0 }` | `xs.iter().filter(\|x\| *x > 0).collect()` | `for _, x in ipairs(xs) do ...` | list comprehension | `data, it > 0 => :filter` |

### 10. Everything is a container (copy and move)

| Python | Kotlin | Rust | Lua | Erlang | True |
|---|---|---|---|---|---|
| `b = a` | `val b = a` | `let b = a;` | `b = a` | `B = A` | `= b` + `a => b` |
| `b = a; del a` | (no) | `let b = a; drop(a)` | `b = a; a = nil` | — | `= b` + `a -> b` |
| `shutil.copy` | `File.copyTo` | `std::fs::copy` | `io.open` | `file:copy` | `file:a => file:b` |
| `os.rename` | `File.renameTo` | `std::fs::rename` | `os.rename` | `file:rename` | `file:a -> file:b` |

### 11. If data exists — you can work with it

| Python | Kotlin | Rust | Lua | Erlang | True |
|---|---|---|---|---|---|
| `x = 5` | `val x = 5` | `let x = 5;` | `x = 5` | `X = 5` | `5 = x` |
| `goto` (no) | (no) | (no) | `goto` | (no) | `^ N` / `^ label` |
| — | — | — | — | — | `^ file.cycle` (jump to file) |

## Key constructs

| Python | Kotlin | Rust | Lua | Erlang | True |
|---|---|---|---|---|---|
| `x = 5` | `val x = 5` | `let x = 5;` | `x = 5` | `X = 5` | `5 = x` |
| `x = 5; y = x` | `val y = x` | `let y = x;` | `y = x` | `Y = X` | `= y` + `x => y` |
| `x = 5; y = x; del x` | (no) | `let y = x; drop(x)` | `x = nil` | — | `= y` + `x -> y` |
| `if c: f()` | `if (c) f()` | `if c { f() }` | `if c then f() end` | `case C of true -> f() end` | `c ? f` |
| `if not c: f()` | `if (!c) f()` | `if !c { f() }` | `if not c then f() end` | `case C of false -> f() end` | `!c ? f` |
| `while True: ... break` | `while (true) { ... break }` | `loop { ... break }` | `while true do ... break end` | — | (implicit) + `^ label` |
| `for x in xs: f(x)` | `for (x in xs) f(x)` | `for x in xs { f(x) }` | `for _, x in ipairs(xs) do f(x) end` | `lists:foreach(...)` | `xs:y, x => :each` |
| `def f(n): return n` | `fun f(n: Int) = n` | `fn f(n: i32) -> i32 { n }` | `function f(n) return n end` | `f(N) -> N.` | `:f:` + `:result` |
| `f(5)` | `f(5)` | `f(5)` | `f(5)` | `f(5)` | `5 => :f` |
| `[1,2,3]` | `listOf(1,2,3)` | `vec![1,2,3]` | `{1,2,3}` | `[1,2,3]` | `1,2,3` |
| `[1,2,3][1]` | `listOf(1,2,3)[1]` | `vec![1,2,3][1]` | `t[1]` | `lists:nth(1, L)` | `arr:y:2` |
| `len([1,2,3])` | `listOf(1,2,3).size` | `vec![1,2,3].len()` | `#t` | `length(L)` | `1,2,3 => :size` |
| `sum([1,2,3])` | `listOf(1,2,3).sum()` | `[1,2,3].iter().sum()` | `sum` | `lists:sum(L)` | `1,2,3 => :sum` |
| `sorted(xs)` | `xs.sorted()` | `xs.sort()` | `table.sort(xs)` | `lists:sort(Xs)` | `xs:y => :sort` |
| `xs.append(4)` | `xs.add(4)` | `xs.push(4)` | `table.insert(xs, 4)` | `Xs ++ [4]` | `xs:y, 4 => :push` |
| `xs.pop()` | `xs.removeLast()` | `xs.pop()` | `table.remove(xs)` | `lists:last(Xs)` | `xs:y => :pop` |
| `xs.insert(1, 4)` | `xs.add(1, 4)` | `xs.insert(1, 4)` | `table.insert(xs, 1, 4)` | `lists:sublist` | `4 ->\| xs:y:1` |
| `xs[1] = 4` | `xs[1] = 4` | `xs[1] = 4` | `xs[1] = 4` | — | `4 -> xs:y:1` |
| `d["k"]` | `d["k"]` | `d["k"]` | `d.k` | `maps:get(k, D)` | `json, k => :json:get` |
| `open(f).read()` | `File(f).readText()` | `fs::read_to_string(f)` | `io.open(f):read("*a")` | `file:read_file(F)` | `f => :file:read` |
| `shutil.copy(a, b)` | `File(a).copyTo(b)` | `fs::copy(a, b)` | `os.execute("cp a b")` | `file:copy(A, B)` | `file:a => file:b` |
| `os.rename(a, b)` | `File(a).renameTo(b)` | `fs::rename(a, b)` | `os.rename(a, b)` | `file:rename(A, B)` | `file:a -> file:b` |
| `print(x)` | `println(x)` | `println!("{}", x)` | `print(x)` | `io:format("~p~n", [X])` | `x => :log` |
| `range(1, 6)` | `1..5` | `1..=5` | `for i=1,5 do end` | `lists:seq(1, 5)` | `1, 5 => :range` |
| `[f() for _ in range(5)]` | `repeat(5) { f() }` | `for _ in 0..5 { f() }` | `for i=1,5 do f() end` | `lists:foreach(...)` | `action, 5 => :repeat` |
| `try/except` | `try/catch` | `Result<T, E>` | `pcall` | `try ... catch` | `:error ? ^ :handle` |
| `None` / `null` | `null` | `None` | `nil` | `undefined` | (0 chars) |
| `True` / `False` | `true` / `false` | `true` / `false` | `true` / `false` | `true` / `false` | `1` / `0` |

**Prefix `:data` / `:math` is optional:**

| Old | New |
|---|---|
| `1, 2, 3 => :math:sum` | `1, 2, 3 => :sum` |
| `arr:y => :data:pop` | `arr:y => :pop` |
| `arr:y => :data:size` | `arr:y => :size` |
| `arr:y => :data:sort` | `arr:y => :sort` |

**Mandatory prefix:** `:math:sort` (numbers), `:json:*`, `:file:*`, `:api:*`.

## Chains `=>`

| Python | Kotlin | Rust | Lua | Erlang | True |
|---|---|---|---|---|---|
| `y = len(open(f).read())` | `val y = File(f).readText().length` | `let y = fs::read_to_string(f)?.len()` | `local y = #io.open(f):read("*a")` | `Y = length(...)` | `f => :file:read => :size = y` |

```true
input.txt => :get = raw
raw:l => :size = lineCount
1, 2, 3 => :sum = result
```

## Name before use

**Rule:** `= name` — declare an empty container **before** use.

```true
= result
1, 2, 3 => :sum = result
```

**Exception:** if the name is **already** a variable — `= name` is not needed.

**Without `= result`:** `result` — a literal `"result"`, not a variable.

## `:error` — error stack

| Python | Kotlin | Rust | Lua | Erlang | True |
|---|---|---|---|---|---|
| `try/except` | `try/catch` | `Result<T, E>` | `pcall` | `try ... catch` | `:error ? ^ :handle` |

```true
:error ? ^ :handle
:run
```

- `:error` → `"1"` / `"0"`.
- `:error:line` / `:error:message` / `:error:lines` / `:error:messages`.
- Format of `error:message`: `<code>, <1|0>, <details>` (1 — active, 0 — resolved).
- `0 => :error:resolve` — resolve.
- `message => :error` — set an error.

## `^` — jump

| Syntax | What it does |
|---|---|
| `^ N` | Line N (N is a number) |
| `^ label` | Label (no dot, exists in the file) |
| `^ file.cycle` / `^ file.agent` / `^ data.txt` | File (with dot) |
| `^ :stop` / `:pause` / `:resume` / `:delete` / `:reset` | Command to the agent |
| `^ notes` (no dot) | **Error** |

## Agent containers

| Container | What |
|---|---|
| `step` | Line number (updated on move) |
| `cycle` | File name (updated on move) |
| `speed` | Speed in ms per step (`100 = speed`) |
| `color` | Agent color (`#00cc22 = color`) |

## FizzBuzz

**Python:**
```python
for i in range(1, 16):
    if i % 15 == 0: print("FizzBuzz")
    elif i % 3 == 0: print("Fizz")
    elif i % 5 == 0: print("Buzz")
    else: print(i)
```

**True (classic, `^`/`?`):**
```true
= out
1 = i
15 = n
= loop
i > n ? ^ done
i % 15 == 0 ? ^ fizzbuzz
i % 3 == 0 ? ^ fizz
i % 5 == 0 ? ^ buzz
^ num
= fizzbuzz
FizzBuzz = out
i + 1 => :math = i
^ loop
= fizz
Fizz = out
i + 1 => :math = i
^ loop
= buzz
Buzz = out
i + 1 => :math = i
^ loop
= num
i = out
i + 1 => :math = i
^ loop
= done
```

**True (short, `:range` + `:each`):**
```true
1, 15 => :range = ns
ns:y, n => n % 15 == 0 ? FizzBuzz = out => :each
```

Full examples — `SAMPLES.md`. Spec — `LANGUAGE.md`.