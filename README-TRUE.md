# True — краткий вход для программиста

Если ты знаешь Python, Kotlin, Rust, Lua или Erlang — эта страница для тебя.
Полная спецификация — `LANGUAGE.md`.

## Терминология

- **Контейнер** — строка данных. Всё в языке — контейнеры.
- **Цикл (cycle)** — файл `.cycle` (файл-функция).
- **Агент (agent)** — файл `.agent` (файл-функция + UI-кнопки).
- **`.true`** — больше нет (зарезервировано для настроек).

## 11 концепций

### 1. Сначала данные, потом операции

В Python/Kotlin/Rust сначала функция, потом аргументы. В True — данные слева,
операция справа.

```python
sum([1, 2, 3])          # Python
```
```true
1, 2, 3 => :sum
```

**Именование — тоже справа:** `данные = имя` (не `имя = данные`).

### 2. Всё есть контейнер

Нет чисел, типов, массивов. Функция сама решает, как трактовать контейнер.

| Python | Kotlin | Rust | Lua | Erlang | True |
|---|---|---|---|---|---|
| `5` | `5` | `5` | `5` | `5` | `5` |
| `"5"` | `"5"` | `"5"` | `"5"` | `"5"` | `5` (или `"5"` — 3 символа) |
| `[1, 2, 3]` | `listOf(1, 2, 3)` | `vec![1, 2, 3]` | `{1, 2, 3}` | `[1, 2, 3]` | `1,2,3` |
| `{"a": 1}` | `mapOf("a" to 1)` | `HashMap` | `{a = 1}` | `#{a => 1}` | `{'a':1}` |

### 3. Агенты исполняют программу (не потоки)

| Python | Kotlin | Rust | Lua | Erlang | True |
|---|---|---|---|---|---|
| `threading.Thread` | `Thread { }` | `std::thread::spawn` | `coroutine.create` | `spawn` | `^ :pause` |
| `asyncio` | `coroutineScope` | `tokio::spawn` | `coroutine.yield` | `receive` | `100 => :agent:wait` |
| `lock` | `Mutex` | `Mutex` | — | — | не нужно (1 `.agent` = 1 агент) |

### 4. Программа меняет саму себя (код — база данных)

```true
0 = a
a + 1 => :math => a
```

После шага: `0 = a` → `1 = a`.

| Python | Kotlin | Rust | Lua | Erlang | True |
|---|---|---|---|---|---|
| `exec("a = 1")` | reflection | `include_str!` | `load` | — | `0 = a` + `:math => a` |

### 5. Наглядность превыше инкапсуляции

| Python | Kotlin | Rust | Lua | Erlang | True |
|---|---|---|---|---|---|
| `_private` | `private` | `pub(crate)` | — | — | нет — всё видно |

### 6. Файл = хранилище + функция

```true
math:fact:
parameters == 0 ? 1 => :result
parameters - 1 => .:math:fact => sub
parameters * sub => :math => :result
:result
```

| Python | Kotlin | Rust | Lua | Erlang | True |
|---|---|---|---|---|---|
| `def fact(n): ...` | `fun fact(n: Int) = ...` | `fn fact(n: i32) -> i32 { ... }` | `function fact(n) ... end` | `fact(N) -> ...` | `:math:fact:` + `:result` |

### 7. Программа — цикл

| Python | Kotlin | Rust | Lua | Erlang | True |
|---|---|---|---|---|---|
| `while True:` | `while (true)` | `loop { }` | `while true do end` | — | (неявно) |
| `break` | `break` | `break` | `break` | — | `^ label` (не `=> :stop`!) |
| — | — | — | — | — | `=> :stop` — **программа**, не цикл |

**Примеры в `SAMPLES.md`** — работают **в середине цикла**. `=> :stop` не
показан — фокус на краткости.

### 8. Программа — switch из явных и неявных условий

| Python | Kotlin | Rust | Lua | Erlang | True |
|---|---|---|---|---|---|
| `if c: ...` | `if (c) { ... }` | `if c { ... }` | `if c then ... end` | `case` | `c ? ...` |
| `elif` | `else if` | `else if` | `elseif` | `;` | `c2 ? ...` |
| `match` | `when` | `match` | — | `case` | `^ N` |

### 9. Ясность превыше краткости

| Python | Kotlin | Rust | Lua | Erlang | True |
|---|---|---|---|---|---|
| `[x for x in xs if x > 0]` | `xs.filter { it > 0 }` | `xs.iter().filter(\|x\| *x > 0).collect()` | `for _, x in ipairs(xs) do ...` | list comprehension | `data, x > 0 => :filter` |

### 10. Всё есть контейнер (копирование и перемещение)

| Python | Kotlin | Rust | Lua | Erlang | True |
|---|---|---|---|---|---|
| `b = a` | `val b = a` | `let b = a;` | `b = a` | `B = A` | `a => b` |
| `b = a; del a` | (нет) | `let b = a; drop(a)` | `b = a; a = nil` | — | `a -> b` |
| `shutil.copy` | `File.copyTo` | `std::fs::copy` | `io.open` | `file:copy` | `file:a => file:b` |
| `os.rename` | `File.renameTo` | `std::fs::rename` | `os.rename` | `file:rename` | `file:a -> file:b` |

### 11. Если данные есть — с ними можно работать

| Python | Kotlin | Rust | Lua | Erlang | True |
|---|---|---|---|---|---|
| `x = 5` | `val x = 5` | `let x = 5;` | `x = 5` | `X = 5` | `5 = x` |
| `goto` (нет) | (нет) | (нет) | `goto` | (нет) | `^ N` / `^ label` |
| — | — | — | — | — | `^ file.cycle` (переход на файл) |

## Ключевые конструкции

| Python | Kotlin | Rust | Lua | Erlang | True |
|---|---|---|---|---|---|
| `x = 5` | `val x = 5` | `let x = 5;` | `x = 5` | `X = 5` | `5 = x` |
| `x = 5; y = x` | `val y = x` | `let y = x;` | `y = x` | `Y = X` | `x => y` |
| `x = 5; y = x; del x` | (нет) | `let y = x; drop(x)` | `x = nil` | — | `x -> y` |
| `if c: f()` | `if (c) f()` | `if c { f() }` | `if c then f() end` | `case C of true -> f() end` | `c ? f` |
| `if not c: f()` | `if (!c) f()` | `if !c { f() }` | `if not c then f() end` | `case C of false -> f() end` | `!c ? f` |
| `while True: ... break` | `while (true) { ... break }` | `loop { ... break }` | `while true do ... break end` | — | (неявно) + `^ label` |
| `for x in xs: f(x)` | `for (x in xs) f(x)` | `for x in xs { f(x) }` | `for _, x in ipairs(xs) do f(x) end` | `lists:foreach(fun(X) -> f(X) end, Xs)` | `xs:foreach:y ; x : f` (без `.`) |
| `def f(n): return n` | `fun f(n: Int) = n` | `fn f(n: i32) -> i32 { n }` | `function f(n) return n end` | `f(N) -> N.` | `:f:` + `:result` |
| `f(5)` | `f(5)` | `f(5)` | `f(5)` | `f(5)` | `5 => :f` |
| `[1,2,3]` | `listOf(1,2,3)` | `vec![1,2,3]` | `{1,2,3}` | `[1,2,3]` | `1,2,3` |
| `[1,2,3][1]` | `listOf(1,2,3)[1]` | `vec![1,2,3][1]` | `t[1]` | `lists:nth(1, L)` | `arr:y:2` |
| `len([1,2,3])` | `listOf(1,2,3).size` | `vec![1,2,3].len()` | `#t` | `length(L)` | `1,2,3 => :size` (или `:s`) |
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
| `[f() for _ in range(5)]` | `repeat(5) { f() }` | `for _ in 0..5 { f() }` | `for i=1,5 do f() end` | `lists:foreach(fun(_) -> f() end, lists:seq(1,5))` | `action, 5 => :repeat` |
| `try/except` | `try/catch` | `Result<T, E>` | `pcall` | `try ... catch` | `:error ? ^ :handle` |
| `None` / `null` | `null` | `None` | `nil` | `undefined` | (0 символов) |
| `True` / `False` | `true` / `false` | `true` / `false` | `true` / `false` | `true` / `false` | `1` / `0` |

**Префикс `:data` / `:math` необязателен:**

| Было | Стало |
|---|---|
| `1, 2, 3 => :math:sum` | `1, 2, 3 => :sum` |
| `arr:y => :data:pop` | `arr:y => :pop` |
| `arr:y => :data:size` | `arr:y => :size` |
| `arr:y => :data:sort` | `arr:y => :sort` |

**Обязательный префикс:** `:math:sort` (числа), `:json:*`, `:file:*`, `:api:*`.

## Цепочки `=>`

| Python | Kotlin | Rust | Lua | Erlang | True |
|---|---|---|---|---|---|
| `y = len(open(f).read())` | `val y = File(f).readText().length` | `let y = fs::read_to_string(f)?.len()` | `local y = #io.open(f):read("*a")` | `Y = length(element(2, file:read_file(F)))` | `f => :file:read => :size => y` |

```true
input.txt => :get => raw
raw:l => :size => lineCount
1, 2, 3 => :sum => result
```

## Имя до использования

**Правило:** `= name` — присвоить пустой контейнер **до** использования.

```true
= result
1, 2, 3 => :sum => result   \ result = "6"
```

**Исключение:** если имя — уже переменная (присвоено ранее) — `= name` не
нужно. `lineCount` после `raw:l => :size => lineCount` — уже переменная,
`lineCount = result` — **избыточно**.

**Без `= result`:** `result` — литерал `"result"`, не переменная.

## `:error` — стек ошибок файла

| Python | Kotlin | Rust | Lua | Erlang | True |
|---|---|---|---|---|---|
| `try/except` | `try/catch` | `Result<T, E>` | `pcall` | `try ... catch` | `:error ? ^ :handle` |

```true
:error ? ^ :handle
:run
```

- `:error` → `"1"`/`"0"`.
- `:error:line` / `:error:message` / `:error:lines` / `:error:messages`.
- Сброс — `reset()`.
- `:error` не отменяет `PAUSED`.

## `^` — переход

| Синтаксис | Что делает |
|---|---|
| `^ N` | Строка N (N — число) |
| `^ label` | Метка (без точки, есть в файле) |
| `^ file.cycle` / `^ file.agent` / `^ data.txt` | Файл (с точкой) |
| `^ :stop` / `:pause` / `:resume` / `:delete` / `:reset` | Команда агенту |
| `^ notes` (без точки) | **Ошибка** |

```true
^ 5
^ done
^ main.cycle
^ :stop
```

## Контейнеры агента

| Контейнер | Что |
|---|---|
| `step` | Номер строки (обновляется при движении) |
| `cycle` | Имя файла (обновляется при перемещении) |

**Удалены:** `:agent:main:currentFile`, `:agent:main:currentStep` (дублируют).
**Оставлены:** `:agent:main:status`, `:agent:main:waiter`, `:agent:status`, `<ms> => :agent:wait`.

## Пример: FizzBuzz

**Python:**
```python
for i in range(1, 16):
    if i % 15 == 0: print("FizzBuzz")
    elif i % 3 == 0: print("Fizz")
    elif i % 5 == 0: print("Buzz")
    else: print(i)
```

**True (простой, `^`/`?`):**
```true
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
i + 1 => :math => i
^ loop
= fizz
Fizz = out
i + 1 => :math => i
^ loop
= buzz
Buzz = out
i + 1 => :math => i
^ loop
= num
i = out
i + 1 => :math => i
^ loop
= done
```

**True (короткая версия через `:range` + `foreach`):**
```true
= ns
= out
1, 15 => :range => ns
ns:foreach:y ; n :
n % 15 == 0 ? FizzBuzz = out,
n % 3 == 0 ? Fizz = out,
n % 5 == 0 ? Buzz = out,
n = out
```

Полные примеры — `SAMPLES.md`. Спецификация — `LANGUAGE.md`.