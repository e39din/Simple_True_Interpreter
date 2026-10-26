# True — language specification

Language version: **0.11.0**  
Interpreter version: **1.1.0**  
License: Apache 2.0 (`LICENSE-TRUE`)

## Terminology

- **Container** — a string of data. Everything in the language is a container. A one-line container is a string, a multi-line container is a file. A container may contain any data — plain text, or an action (function) that executes on call.
- **Named container** — a container with a name (`value = name`). The name is a reference to the content.
- **Cycle** — a `.cycle` file (a file-function).
- **Agent** — a `.agent` file (a file-function + agent UI buttons).
- **`.true`** — no longer used (reserved for project settings).

## Principles

1. **Everything is a container.** No numbers, types, or arrays. Only containers and conventions over them. **Quotes are part of the container**, not syntax. `"Hello"` is the container `"Hello"` (7 chars, with quotes). `Hello` is the container `Hello` (5 chars, no quotes). **Empty container — 0 chars.** Literal `""` — 2 chars (`"` and `"`), not an empty container. To get an empty container, use `= name` or no result.
2. **A program is a database.** Data lives in the file text. Changing a value by name changes the declaration text too.
3. **Data first, operations after.** `data, args => :op`.
4. **`=>` copies, `->` moves.** `->` clears the source value, the name stays.
5. **Axes `l/x/y/z`** — the only way to structure a container. Default axis — `x`.
6. **A container is a multi-dimensional array** of data (`l`, `x`, `y`, `z`). Multi-dimensionality via nested axes.
7. **No new types** (`map`, `stack`, `queue`, `tree`, `graph`, pointers).
8. **No `for`/`while`.** Only the implicit program loop + jumps `^` and conditions `?`.
9. **Code must be visual.** Less guessing, fewer mistakes, faster writing.
10. **Name on the right.** `0 = a` — data on the left, name on the right.
11. **`@` — constant, `*` — hidden.**
12. **A program is a switch.** Each line is a branch.
13. **A program is always executed by agents.** 1 agent per `.agent` file. Each agent is visible simultaneously (`Cycles`, `FileChips`, `Log`).
14. **`-> nothing` — universal sink.** `file:x -> nothing`, `<var> -> nothing`. Cascade: file → agent. Purple highlight — sign of irreversibility.
15. **Work with what you have.** Data always has an address — a line number. A name is an additional reference. If a name exists — use it. If not — use the address (`^ N`). A name appears at the moment of naming (`data = name`) and lives as long as the data exists.
16. **Everything is a function with `:`.** System names are reserved. A user file `name.cycle` / `name.agent` — is function `:name`.
17. **`:result` stores, does not return.** Someone will take it, maybe not. Waiter — auto-detected by `:result` presence.
18. **Read/write is symmetric** (except `@`-constants).
19. **`actions` — the agent's program.** Multiline, default `":run"`. `:run` — atomic execution. `:run` without an argument — executes the current line.
20. **Agent containers:** `step` (line number), `cycle` (file name), `speed` (ms per step), `color` (agent color in `#RRGGBB` / `#AARRGGBB` / `#RGB`).
21. **`:error` — the file's error stack.** `:error`, `:error:line`, `:error:message`, `:error:lines`, `:error:messages`, `:error:resolve`. `:error` does **not** pause by default. To pause all agents — `:pauseAll`.
22. **Naming, not creating.** `= name` is naming. `name` is a **name for the value on the left**. The value already exists. `name` receives the value.
23. **`=>` is never naming.** `chain => name` — error if `name` is not declared. Naming — only via `=`.
24. **Modules and imports are not needed.** True + Simple can open any file from code via `:file:read` / `:file:data` / `^ file.cycle` / `:file_name`. No `import` / `require` / `use`.
25. **Any subject starting with `:` is both a function and a container. No exceptions.** `:math`, `:run`, `:log`, `:agent:main:status`, `:simple:search:count` — all. **Function:** `data => :name` — executes. **Container:** `:name` — substitution (empty string if not set). **Naming:** `:name => dst` / `:name = dst` — `dst` receives the value of `:name`. **Forbidden:** `dst = :name` — system name on the right of `=` is forbidden.

## Files

| Extension | Type | UI | Agent |
|---|---|---|---|
| `.cycle` | Cycle (file-function) | File buttons | No |
| `.agent` | Agent | File buttons + agent buttons | Yes |
| `.txt`, `.json`, no extension | File | File buttons | No (agent can jump there) |
| `.true` | **Not used** (project settings) | — | — |

**Any extension** — an agent can jump to any file via `^ file.ext`.

## Program

- Line numbers start at 1.
- Default program — a loop: after the last line without `=> :stop`, execution returns to the first line.
- `\` at line start — the whole line is treated as text (literal).
- `\ ... \` in the middle of a line — the part between `\` and `\` is a **literal**, not executed or substituted.
- `\` before an operation symbol — the symbol is treated as text; `\` is stripped in output.
- `\,` — escapes a single separator (comma); `\;`, `\.` — likewise.
- A line without a recognized operation is a comment.
- **Examples in `SAMPLES.md`** work **in the middle of a loop**.

## Naming

```true
value = name
value = name *
value = name @
```

- `name` — identifier `[A-Za-z_]\w*`. `nothing` — reserved.
- `*` — hide from the main Values list.
- `@` — constant: writing and moving are forbidden.
- **Naming, not creating.** `value = name` gives the name `name` to the value `value`.
- **Naming is always `=`**, never `=>`.
- **Content** — what is **before** the operation or chain of operations.
- **`a + 1 => sum = func1`** ≡ **`\ a + 1 => sum \ = func1`** — both name `func1` as the content `"a + 1 => sum"`. The content is **text**, not executed.

**Examples:**

| Code | What happens |
|---|---|
| `50 = dmg` | Naming: `dmg` = `"50"` |
| `= dmg` | Naming an empty container |
| `12 + 1 => :math = dmg` | Naming the result: `dmg` = `"13"` |
| `12 + 1 => newName` | **Error** — `newName` is not declared |
| `:math => dst` | `dst` receives the value of `:math` |
| `:result = sub` | Naming: `sub` = the value of `:result` |
| `a => b` | **Error** — `b` is not declared (use `= b` first) |
| `a -> b` | **Error** — `b` is not declared (use `= b` first) |
| `data => :fn` | **Not naming** — result in `:fn` |
| `data => :fn => dst` | `dst` receives the value of `:fn` |
| `^ newName` | **Error** — unknown jump target |
| `5 = :math` | **Error** — system name on the right |
| `:math = dst` | Naming: `dst` = the value of `:math` |

### Name before use

**Rule:** before using a name in an operation, the name must be declared:
- `= name` (empty container), or
- `value = name` (named container with a value).

**Exception:** if the name is **already** a variable, `= name` is not needed.

### Everything with `:`

**Any subject starting with `:` is both a function and a container.** No exceptions.

| Form | What |
|---|---|
| `data => :name` | Function — executes |
| `:name` | Container — substitution (empty string if not set) |
| `:name => dst` | Naming — `dst` receives the value of `:name` (empty if not set) |
| `:name = dst` | Naming — same |
| `dst = :name` | **Error** — system name on the right of `=` |

**Examples:**
- `:math` — value of the last `:math` operation.
- `:run` — empty string (if not set).
- `:log` — empty string.
- `:agent:main:status` — empty string.
- `:simple:search:count` — number of matches.

### Literal

To keep a name as text, use `\ name`:

```true
\ func1
```

## Labels and jumps

```true
= label
^ N
^ label
^ file_name.cycle
^ file_name.agent
^ data.txt
^ :stop
^ :pause
^ :resume
^ :delete
^ :reset
```

- `= label` — naming an empty container (a label).
- `^ N` — goto by line number.
- `^ label` — goto by label (no dot, exists in the current file).
- `^ file_name.cycle` / `^ file_name.agent` / `^ data.txt` — goto to a file (dot — extension).
- `^ :stop` / `^ :pause` / `^ :resume` / `^ :delete` — commands to the agent.
- `^ :reset` — reset the file.
- `^ notes` (no dot, no label, not a number) — **error**.

**Priority of `^` parsing:**
1. Number → line N.
2. `:stop` / `:pause` / `:resume` / `:delete` / `:reset` → command.
3. Label (no dot, exists in file) → label.
4. File (has dot) → file.
5. Else → error.

### Agent jump rules

- Jump to a file **without reset**: `agent.fileName = target`, `currentStep = 1`, `variables` are preserved.
- If the target file is occupied by **another agent** — error `occupied, 1, target=..., agent=...`, `PAUSED`.
- The agent can jump to **its own file** — normal transition.

## Conditions

```true
condition ? operation
```

- Left of `?` — the condition; right — any operation.
- Supported: `==`, `!=`, `<`, `>`, `<=`, `>=`, `&&`, `||`, `!`.
- **Parentheses are supported.**
- **Priorities:** `||` < `&&` < `!` < comparison.
- **Exactly one `?` per line.** Nested `?` — error `Multiple ? on one line`.

## Copy and move

- `a => b` — copy (requires `= b` before).
- `a -> b` — move (requires `= b` before).
- `v -> data:y:3` — replace an element.
- `v ->| data:y:3` — insert (move).
- `v =>| data:y:3` — insert (copy).
- `file:a => file:b` — copy file content. `file:a -> file:b` — move.
- `file:a => var` — read file into a container. `var => file:b` — write container to file.

### Chains `=>`

`=>` can be **chained**: `data => :fn1 => :fn2 = dst`. The result is named by `= dst`.

```true
input.txt => :get => raw
raw:l => :size => lineCount
1, 2, 3 => :sum => result
```

## Commands

```true
=> :pause
=> :resume
=> :stop
=> :reset
=> :activeTab
=> :activeTab, step
```

## `:math` — numeric operations

| Operation | Syntax |
|---|---|
| Arithmetic | `a + b => :math` |
| Bitwise | `a & b => :math`, `a << 1 => :math` |
| Functions | `abs(a) => :math`, `popcount(a) => :math` |
| Boolean | `a == b => :math` → `0/1` |
| Lists | `1, 2, 3 => :math:sum`, `:math:min/max/avg/product/count/sort` |

**Boolean operators** — with priorities and parentheses. Comparisons after arithmetic: `1 + 2 == 3 => :math` → `1`.

**Result:** `:math` (substitution), `:math:op`. To name — `:math = dst` / `:math => dst`.

## `:data` — container operations

| Operation | Syntax | Axis |
|---|---|---|
| `substring` | `data, start, end => :data:substring` | only `x` |
| `indexOf` | `data, sub => :data:indexOf` | by axis |
| `replace` | `data, old, new => :data:replace` | only `x` |
| `lower` / `upper` / `trim` | `data => :data:lower` | by axis |
| `chars` | `data, sep => :data:chars` | only `x` |
| `charAt` | `data, i => :data:charAt` | by axis |
| `size` | `data => :data:size`, `data:y => :data:size` | by axis |
| `:s` | `data => :s`, `data:y => :s` | by axis (short) |
| `reverse` / `unique` | `data => :data:reverse` | by axis |
| `contains` | `data, v => :data:contains` | by axis |
| `slice` | `data, from, to => :data:slice` | by axis |
| `join` | `data, axis => :data:join` | by axis |
| `sort` | `data:y => :data:sort` | by axis |
| `push` | `data:y, v => :data:push` | by axis |
| `pop` | `data:y => :data:pop` | by axis |
| `peek` | `data:y => :peek` | by axis |
| `remove` / `swap` | `data:y, ... => :data:<op>` | by axis |
| `deleteRange` / `replaceRange` | `data:y, from, to => :data:<op>` | by axis |
| `filter` | `data, cond => :filter` | by `y` |
| `matches` | `data, regex => :matches` | by `x` |

**Axis is explicit or default `x`.** Exception: `substring`, `replace`, `chars` — only `x`.

## Functional operations

| Operation | Syntax | Result |
|---|---|---|
| `:map` | `arr:y, expr => :map` | `:map` — new container |
| `:reduce` | `arr:y, init, expr => :reduce` | `:reduce` — final accumulator |
| `:fold` | `arr:y, init, expr => :fold` | `:fold` — final accumulator |
| `:each` | `arr:y, expr => :each` | no result (side effects) |

**Variables inside `expr`:**
- `:map` — `n`, `i`
- `:reduce` / `:fold` — `acc`, `n`, `i`
- `:each` — `n`, `i`

**Expression vs operation:**
- `:map` / `:reduce` / `:fold` — `expr` is a **expression**.
- `:each` — `expr` is an **operation**.

**Argument as name:** if `expr` is a name of an existing container — its body is used.

**Axis:** from `arr:y`, otherwise `x`.

## `:json`

| Operation | Syntax |
|---|---|
| `get` | `json, key => :json:get` |
| `set` | `json, key, value => :json:set` |
| `names` | `json => :json:names` |
| `values` | `json => :json:values` |

## `:file`

| Operation | Syntax |
|---|---|
| `file:data` | `path => :file:data` / `path, content => :file:data` |
| `file:origin` | `path => :file:origin` / `path, content => :file:origin` |
| `file:lines` | `path => :file:lines` / `path, N, text => :file:lines` |
| `file:create` | `path, content => :file:create` |
| `file:read` | `path => :file:read` |
| `file:update` | `path, content => :file:update` |
| `file:delete` | `path => :file:delete` |
| `file:list` | `path => :file:list` |
| `file:exists` | `path => :file:exists` |
| `:path` | `:file:a => :path` |

## `:console`, `:run`

```true
command => :console
```

- `:console` — execute an OS command.
- `:run` — **atomic execution of a line** (for `actions`). Without an argument — executes the **current** line.

### `:run` is atomic

| Level | Who moves | What happens |
|---|---|---|
| **Agent step** | `executeOneStep` — advances `currentStep`, executes `actions` | One tick of the agent loop |
| **Line step** | `:run` inside `actions` — executes **one** program line | Atomically |

## `:api`

| Operation | Syntax |
|---|---|
| `api:get` | `url, query => :api:get` |
| `api:post` | `url, data => :api:post` |
| `api:put` | `url, data => :api:put` |
| `api:delete` | `url => :api:delete` |
| `api:patch` | `url, data => :api:patch` |
| `api:websocket` | `url => :api:websocket` |

## `:save` / `:load`

```true
path, data  => :save
=> :save
file_name:save
path, query => :load
```

## `:range` / `:repeat`

```true
1, 5 => :range
5, 1 => :range
a + 1 => a = action
action, 5 => :repeat
```

## `:pauseAll` / `:resumeAll`

```true
:pauseAll
:resumeAll
1 => :pauseAll
0 => :pauseAll
go, go, go => :pauseAll
```

- `:pauseAll` — pause **all** agents.
- `:resumeAll` — resume **all** agents.
- Parameters ignored.

## `:error` — the file's error stack

Errors are **at the file level**, not the agent level. The stack accumulates.

| Function | Result |
|---|---|
| `:error` | `"1"` / `"0"` |
| `:error:line` | last error's line number |
| `:error:message` | `<code>, <1\|0>, <details>` |
| `:error:lines` | all lines, comma-separated |
| `:error:messages` | all messages, `\n`-separated |
| `message => :error` | set an error |
| `0 => :error:resolve` | mark errors as resolved (`1` → `0`) |

**`:error` does NOT pause by default.** To pause — call `:pauseAll`.

### Format of `error:message`

`<code>, <1|0>, <details>`

- `<code>` — `custom`, `occupied`, `notfound`, `recursive`, `assert`, `range`, `const`, `nested`.
- **`1` — active, `0` — resolved.**
- `<details>` — free text.

## `:assert` — condition check

```true
a == b, "msg" => :assert
```

- **Success:** `:assert` = `"1"`, `LogEntry(type="assert", resolved=true)` — green.
- **Failure:** `:assert` = `"0"`, `LogEntry(type="assert", resolved=false)` — red. Also added to `:error` stack with code `assert`.
- **`:assert` does NOT pause by default.** To pause — call `:pauseAll`.

Without message: `a == b => :assert` — snackbar warning `:assert without message`.

## Agents

### Agent containers

| Container | What |
|---|---|
| `step` | Line number |
| `cycle` | File name |
| `speed` | ms per step (`100 = speed`) |
| `color` | Agent color (`#00cc22 = color`) |

### Agent functions

```true
:agent:<name>:status => dst
:agent:<name>:waiter => dst
:agent:status => dst
```

### Agent control

```true
^ :stop
^ :pause
^ :resume
^ :delete
^ :reset
```

### Waiting

```true
<ms> => :agent:wait
<ms>, <timeout> => :agent:wait:timeout
```

### Waiter — auto-detection

- `:result` in the file → waiter.
- No `:result` → cyclic.

### Vertical files

- `fileOrder` — vertical order (Y axis).
- `↑` / `↓` buttons in the file button row.

### UI

**File buttons** (`⟲`, `↑`, `↓`, `✎`, `🗑`) — top, **all** files.
**Agent buttons** (`▶⏸⏹⏭⏮`) — bottom, **only** `.agent`.
**`⛶`** — in the header, all files.

## `:simple` — interpreter functions

`:simple:*` — 36 functions, async, result in `:simple` = `"1"`. Destructive actions are logged.

### Base (21)

| Function | What |
|---|---|
| `:simple:open` | Open a file (absolute or relative to projectDir/baseDir) |
| `:simple:save` | Save current |
| `:simple:saveAs` | Save as |
| `:simple:newCycle` | New cycle |
| `:simple:newAgent` | New agent |
| `:simple:openFolder` | Open folder |
| `:simple:rename` | Rename current |
| `:simple:delete` | Delete current (logged) |
| `:simple:reset` | Reset current file |
| `:simple:close` | Close current tab (logged) |
| `:simple:moveUp` | Move file up |
| `:simple:moveDown` | Move file down |
| `:simple:startAll` | Start all agents |
| `:simple:pauseAll` | Pause all |
| `:simple:stopAll` | Stop all (logged) |
| `:simple:stepAll` | Step all |
| `:simple:stepBackAll` | Step back all |
| `:simple:settings` | Open settings |
| `:simple:about` | About |
| `:simple:samples` | Samples |
| `:simple:activeTab` | Switch tab |

### UI automation (3)

| Function | What |
|---|---|
| `:simple:click` | Click a UI element (`button:*`, `tab:*`, `agent:*`, `menu:*`, `file:*`) |
| `:simple:clickAt` | Click at absolute screen coordinates (AWT Robot) — **dangerous, runtime only, no headless** |
| `:simple:type` | Type text into a UI element, character by character (like a human) |

### Terminal (4)

| Function | What |
|---|---|
| `:simple:openTerminal` | Open a real OS terminal (`ProcessBuilder`), start reading stdout/stderr |
| `:simple:closeTerminal` | Close a terminal (`process.destroy()`) |
| `:simple:sendTerminal` | Send a command to terminal (write to stdin + `\n`) |
| `:simple:typeTerminal` | Type text into terminal, character by character (write to stdin) |

### Search (8)

| Function | What |
|---|---|
| `:simple:search` | Set query + run search |
| `:simple:search:query` | Current query |
| `:simple:search:matches` | Lines with matches |
| `:simple:search:current` | Current match (1-based) |
| `:simple:search:count` | Number of matches |
| `:simple:search:next` | Next match |
| `:simple:search:prev` | Previous match |
| `:simple:search:clear` | Clear search |

**Result:** `:simple` = `"1"` (accepted, async). To use — `:simple` (substitution) or `:simple = name` (naming). **Never** `name = :simple` — system name on the right is forbidden.

## Settings

| Function | Syntax | Default |
|---|---|---|
| `:historyLimit` | `100 => :historyLimit` / `:historyLimit = dst` | 100 |
| `:logLimit` | `1000 => :logLimit` / `:logLimit = dst` | 1000 |
| `:strictMode` | `1 => :strictMode` / `:strictMode = dst` | 0 |
| `:autoSync` | `1 => :autoSync` / `:autoSync = dst` | 1 |

## Functions — files

**File = function.** `math.cycle` — function `:math`.

```true
math:fact:
parameters == 0 ? 1 => :result
= sub
parameters - 1 => .:math:fact => sub
parameters * sub => :math => :result
:result
```

- `:parameters` — entry marker.
- `=> :result` — set the value.
- `:result` at the end — end marker. Waiter — auto.

Call: `data => :math = result`.

**System names are reserved.**

## `actions` — the agent's program

- `AgentState.actions: String` (multiline), default `":run"`.
- `executeOneStep` advances `currentStep`, executes `actions`.
- `:run` — atomic execution.
- `:log` — log the current line.

## `:stack` — explicit stack

`:stack:push` = `:push`, `:stack:pop` = `:pop`, `:stack:peek` — top element without removal.

```true
1, 2, 3 = stack
stack:y, 4 => :stack:push
stack:y => :stack:peek = top
stack:y => :stack:pop = top
```

## Strict Mode

Strict Mode **forbids** calls to system functions. User `:file_name` — **allowed**. Substitution of named containers — **allowed**.

**System names (forbidden in Strict Mode):**

```
math, data, json, file, api, console, save, load, get, post, size, s,
sum, min, max, avg, product, count, sort, map, reduce, fold, each,
call, filter, matches, error, message, agent, run, log, repeat, range,
parameters, result, activeTab, path, source, test, return, assert,
simple, stack, pauseAll, resumeAll
```

**Allowed:**
- `:file_name` — user files
- `^ :stop` / `:pause` / `:resume` / `:delete` / `:reset` — commands
- `=`, `=>`, `->`, `?`, `\`, `func1` — substitution

## Errors

- **Validation:** unknown label, `Multiple ?`, `=> :result inside function`.
- **Execution:** write to a constant, out of range, file missing → `PAUSED` + `errorLine`.
- **Agents:** `occupied` → error.
- `nothing = ...` → `error: 'nothing' is reserved`.
- `5 = result` → `error: 'result' is reserved`.

## Versioning

- Language: **0.11.0**. Interpreter: **1.1.0**.
- `PATCH` — fixes. `MINOR` — new features. `MAJOR` — breaking.

## Version history (0.11.0)

- **Substitution of named containers.**
- **`:map` / `:reduce` / `:fold` / `:each`** — new.
- **`:foreach` removed.**
- **`:error` format** — `<code>, <1|0>, <details>`; `:error:resolve`; `LogEntry.resolved`. `:error` does not pause by default.
- **`:pauseAll` / `:resumeAll`** — new.
- **`:assert`** — new.
- **Settings** — `:historyLimit`, `:logLimit`, `:strictMode`, `:autoSync`.
- **`:simple:*`** — 36 functions.
- **`:stack:peek`** — new.
- **`speed` and `color`** — agent containers.
- **`MrGreen.agent`** — first start.
- **Escaping** — `\ ` (line), `\ ... \` (mid-line literal), `\,` / `\;` / `\.` (single separator).
- **Strict Mode** — forbids system functions, allows user `:file_name` and substitution.
- **Modules and imports are not needed.**
- **`=>` is never naming.** Naming — only `= name`.
- **Any subject starting with `:` is both a function and a container. No exceptions.**