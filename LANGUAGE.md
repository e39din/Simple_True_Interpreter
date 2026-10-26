# True — language specification

Language version: **0.11.0**  
Interpreter version: **1.1.0**  
License: Apache 2.0 (`LICENSE-TRUE`)

## Terminology

- **Container** — a string of data. Everything in the language is a container.
- **Named container** — a container with a name (`value = name`).
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
21. **`:error` — the file's error stack.** `:error`, `:error:line`, `:error:message`, `:error:lines`, `:error:messages`. Reaction — `actions`: `":error ? ^ :handle"`. `:error` does not clear `PAUSED`.
22. **Naming, not creating.** `= name` is naming. `name` is a **name for the value on the left**. The value already exists. `name` receives the value. There is no "creation" of a container — the container is the value; `name` is a reference to it.
23. **No silent creation.** `=> name` without a declaration means **naming** — the name receives the value. This is not an error. But **system names are reserved** (`result`, `parameters`, `nothing`, `step`, `cycle`, `speed`, `color`, `error`, `log`, `message`, `size`, `s`, `math`, `data`, `json`, `file`, `api`, `console`, `simple`, `agent`, `run`, `range`, `repeat`, `activeTab`, `path`, `source`, `test`, `return`, `assert`, `call`, `filter`, `matches`, `map`, `reduce`, `fold`, `each`) — you cannot name a container with a system name.
24. **Modules and imports are not needed.** True + Simple can open any file from code via `:file:read` / `:file:data` / `^ file.cycle` / `:file_name`. This **effectively connects** the file — without separate import syntax. A file is a function (`:name`), a call is `data => :name`. No `import` / `require` / `use`.

## Files

| Extension | Type | UI | Agent |
|---|---|---|---|
| `.cycle` | Cycle (file-function) | File buttons | No |
| `.agent` | Agent | File buttons + agent buttons | Yes |
| `.txt`, `.json`, no extension | File | File buttons | No (agent can jump there) |
| `.true` | **Not used** (project settings) | — | — |

**Any extension** — an agent can jump to any file via `^ file.ext`.

**Modules and imports are not needed conceptually** — any file can be opened from code, which effectively connects it.

## Program

- Line numbers start at 1.
- Default program — a loop: after the last line without `=> :stop`, execution returns to the first line.
- `\` at line start — the whole line is treated as text (literal).
- `\ ` (backslash + space) at line start — same as `\` (literal).
- `\` before an operation symbol (`=`, `=>`, `^`, `->`, `?`, `->|`, `=>|`) — the symbol is treated as text; `\` is stripped in output.
- `\,` — escapes a single separator symbol (comma) in args; `\;`, `\.` — likewise.
- A line without a recognized operation is a comment.
- **Examples in `SAMPLES.md`** work **in the middle of a loop**. Termination (`=> :stop`) is a separate matter.

## Naming

```true
value = name
value = name *
value = name @
```

- `name` — identifier `[A-Za-z_]\w*`. `nothing` — reserved.
- `*` — hide from the main Values list.
- `@` — constant: writing and moving are forbidden.
- **Naming, not creating.** `value = name` gives the name `name` to the value `value`. The value already exists; `name` is a reference. Nothing is "created".
- **No silent creation.** `=> name` / `:math => name` / `:error => name` — these are **naming** operations; they give `name` the result on the left. `requireDeclared` is applied only to:
    - the **source** of `->` / `=>` (must already exist);
    - the **base container** of a path (`arr:y:1` — `arr` must exist);
    - the **system name check** in `runNaming` (`5 = result` — error).
- **Action:** if `value` contains `=>`/`->`/`^`/`?` — it is an **action** (not substituted, stored as text). `a + 1 => a = action` — `action` = `"a + 1 => a"`.

Changing a value by name changes the declaration text in the program (`syncCodeWithVariables`). Constants (`@`) are not rewritten.

### Name before use

**Rule:** before using a name in an operation, the name must be declared:
- `= name` (empty container), or
- `value = name` (named container with a value).

**Exception:** if the name is **already** a variable (declared earlier), `= name` is not needed.

**Chains:** `a => :f => dst` — `dst` is **named** by the result; `dst` does not need to be declared first.

```true
= result
1, 2, 3 => :sum => result
```

Both forms are valid:
```true
= result
1, 2, 3 => :sum => result
```
```true
1, 2, 3 => :sum => result
```

### Substitution of named containers

**Rule.** In any position **except the name part of an assignment** (`= name`), the name of a container (`name`) or a file-function (`:name`) is **substituted** with its value during interpretation.

- **Left of `=`** — substitution: `:result = sub` → `sub = results["result"]`.
- **Right of `=`** — name (not substituted): `5 = a` → `a` is a new name.
- **In operations** — substitution: `a + 1 => :math` → `a` → value.
- **In operation position** — substitution + execution: `func1` → its body, executed.

**Exception:** the name part of `= name` is always **a name**, not a substitution.

### Substitution example

```true
0 = sum
a + 1 => sum = func1
func1
```

`func1` — named container with the action `a + 1 => sum`. Every `func1` in operation position substitutes and executes the body. `sum` increases by 1 each time.

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
- `^ N` — goto by line number (N is a number).
- `^ label` — goto by label (no dot, exists in the current file).
- `^ file_name.cycle` / `^ file_name.agent` / `^ data.txt` — goto to a file (dot — extension).
- `^ :stop` / `^ :pause` / `^ :resume` / `^ :delete` — commands to the agent (like UI buttons).
- `^ :reset` — reset the file (like `⟲`).
- `^ notes` (no dot, no label, not a number) — **error**.

**Priority of `^` parsing:**
1. Number → line N.
2. `:stop` / `:pause` / `:resume` / `:delete` / `:reset` → command.
3. Label (no dot, exists in file) → label.
4. File (has dot) → file.
5. Else → error.

**File — only with a dot (extension).** No dot — label (if it exists) or number.

### Agent jump rules

- Jump to a file **without reset**: `agent.fileName = target`, `currentStep = 1`, `variables` are preserved. `interpreter.reset()` is **not called** — only on `^ :reset`.
- If the target file is occupied by **another agent** — error `occupied, 1, target=..., agent=...`, status `PAUSED`. The agent does not move.
- The agent can jump to **its own file** — this is a normal transition (not recursion).

## Conditions

```true
condition ? operation
```

- Left of `?` — the condition; right — any operation.
- Supported: `==`, `!=`, `<`, `>`, `<=`, `>=`, `&&`, `||`, `!`.
- **Parentheses are supported:** `(a == 1 || b == 1) && c == 2`.
- **Priorities:** `||` < `&&` < `!` < comparison.
- Comparison: numeric if both sides are numbers; otherwise lexical.
- If there is no operation to the right of `?`, `?` stays text.
- **Exactly one `?` per line.** Nested `?` — error `Multiple ? on one line`.

**Examples:**

```true
a < max ? ^ do
(a == 1 || b == 1) && c == 2 ? 5 = d
!(a == 1) ? 9 = e
```

## Copy and move

- `a => b` — copy.
- `a -> b` — move.
- `v -> data:y:3` — **replace** an element.
- `v ->| data:y:3` — **insert** (move).
- `v =>| data:y:3` — **insert** (copy).
- `file:a => file:b` — copy file content. `file:a -> file:b` — move.
- `file:a => var` — read file into a container. `var => file:b` — write container to file.

### Chains `=>`

`=>` can be **chained**: `data => :fn1 => :fn2 => dst`. The result of each function is the input to the next. **No intermediate names needed.**

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

- `:pause` / `:resume` / `:stop` — current agent's status.
- `:reset` — a normal step.
- `:activeTab` — switch tab.
- **`:stop` ≠ `break`.** `:stop` finishes the **program**, not a loop. To exit a loop — jump `^ label` outside.

## `:math` — numeric operations

| Operation | Syntax |
|---|---|
| Arithmetic | `a + b => :math` |
| Bitwise | `a & b => :math`, `a << 1 => :math` |
| Functions | `abs(a) => :math`, `popcount(a) => :math` |
| Boolean | `a == b => :math` → `0/1` |
| Lists | `1, 2, 3 => :math:sum`, `:math:min/max/avg/product/count/sort` |

**Boolean operators** — with priorities and parentheses. Comparisons after arithmetic: `1 + 2 == 3 => :math` → `1`.

**Result:** `results["math"]`, `results["math:op"]`. Into `variables` via `:math => var` or `:math:sum = sum` / `:math:sum => sum`. If there is no result — empty container.

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
| `pop` | `data:y => :data:pop` | by axis (stack: `pop` — top, `pop_rest` — rest) |
| `peek` | `data:y => :peek` | by axis (top, no removal) |
| `remove` / `swap` | `data:y, ... => :data:<op>` | by axis |
| `deleteRange` / `replaceRange` | `data:y, from, to => :data:<op>` | by axis |
| `filter` | `data, cond => :filter` | by `y` |
| `matches` | `data, regex => :matches` | by `x` |

**Removed (deprecated):** `:data:insert` (→ `->|`), `:data:set` (→ `->`), `:data:length` (→ `:data:y:size` / `:s`).

**Axis is explicit or default `x`.** Exception: `substring`, `replace`, `chars` — only `x`.

### Prefix `:data` / `:math` is optional

If an operation **does not conflict** with another — the prefix may be omitted:

```true
1, 2, 3 => :sum        \ same as :math:sum
arr:y => :pop          \ same as :data:pop
arr:y => :size         \ same as :data:size
arr:y => :s            \ short for :data:size
arr:y => :sort         \ same as :data:sort (strings)
```

**Mandatory prefix:**
- `:math:sort` — sort **numbers** (`:sort` — strings).
- `:json:*`, `:file:*`, `:api:*` — groups.

## Functional operations

`:map`, `:reduce`, `:fold`, `:each` — new operations in 0.11.0.

| Operation | Syntax | Result |
|---|---|---|
| `:map` | `arr:y, expr => :map` | `results["map"]` — new container, same axis |
| `:reduce` | `arr:y, init, expr => :reduce` | `results["reduce"]` — final accumulator |
| `:fold` | `arr:y, init, expr => :fold` | `results["fold"]` — final accumulator (synonym of `:reduce`) |
| `:each` | `arr:y, expr => :each` | nothing (side effects only) |

**Variables inside `expr`:**
- `:map` — `n` (element), `i` (1-based index)
- `:reduce` / `:fold` — `acc` (accumulator), `n` (element), `i` (1-based index)
- `:each` — `n`, `i`

**Expression vs operation:**
- `:map` / `:reduce` / `:fold` — `expr` is a **expression** evaluated via `evaluateMath(substitute(expr))`.
- `:each` — `expr` is an **operation** executed via `executeOperation`.

**Argument as name:** if `expr` is a name of an existing container — its body is used.

**Axis:** from `arr:y`, otherwise `x`.

**Empty container:** `:map` → `""`, `:reduce` / `:fold` → `init`, `:each` → nothing.

**Recursion:** `:map` inside `:map` is allowed, depth limit 10.

### `<name>:splitter` — splitters for any axis of any string

```true
. => raw:splitter:l:;
```

- `<name>` — variable name (`raw`, `csv`, `line`), not `data`.
- `<axis>` — `l`, `x`, `y`, `z`.
- `<sep>` — separator (string, no spaces).

**Default:** `l` — `\n`, `x` — empty, `y` — `,`, `z` — `;`.

## `:json` — JSON

| Operation | Syntax |
|---|---|
| `get` | `json, key => :json:get` |
| `set` | `json, key, value => :json:set` |
| `names` | `json => :json:names` |
| `values` | `json => :json:values` |

## `:file` — files

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
| `:path` | `:file:a => :path` — path to file |

- Result of read — in `results['file']`.
- Result of write — `1` / `-1`.
- If the file is missing — `errorLine` (except `:file:exists` → `"0"`).

**Short forms:** `file:a => var` (read), `var => file:b` (write), `file:a => file:b` (copy), `file:a -> file:b` (move).

## `:console`, `:run`

```true
command => :console
```

- `:console` — execute an OS command.
- `:run` — **atomic execution of a line** (for `actions`). Without an argument — executes the **current** line.

### `:run` is atomic

Two-level execution model:

| Level | Who moves | What happens |
|---|---|---|
| **Agent step** | `executeOneStep` — advances `currentStep`, executes `actions` | One tick of the agent loop |
| **Line step** | `:run` inside `actions` — executes **one** program line | Atomically, no recursion into `actions`, no re-`executeOneStep` |

`actions` default — `":run"`. That means: "on each agent step, execute **one** program line, nothing more". If `actions` = `":log\n:run"` — log the current line first, then execute.

**Atomicity of `:run`:**

| Property | Meaning |
|---|---|
| Not recursive | `:run` inside `:run` is not invoked — `executeLine` directly, without `runActions` |
| Does not move `currentStep` itself | `applyOp` → `stepForward` moves it after `executeOperation` |
| Single pass | `executeOperation` once, not a loop |
| Does not call `actions` | `actions` — only at the top level of `executeOneStep` |

## `:api` — network

| Operation | Syntax |
|---|---|
| `api:get` | `url, query => :api:get` |
| `api:post` | `url, data => :api:post` |
| `api:put` | `url, data => :api:put` |
| `api:delete` | `url => :api:delete` |
| `api:patch` | `url, data => :api:patch` |
| `api:websocket` | `url => :api:websocket` |

## `:save` / `:load` — universal I/O

```true
path, data  => :save
=> :save
file_name:save
path, query => :load
```

## `:range` / `:repeat`

```true
1, 5 => :range        \ "1,2,3,4,5"
5, 1 => :range        \ "5,4,3,2,1"
a + 1 => a = action
action, 5 => :repeat  \ execute action 5 times
```

## `:error` — the file's error stack

Errors are **at the file level**, not the agent level. The stack accumulates.

| Function | Result |
|---|---|
| `:error` | `"1"` / `"0"` — are there any errors |
| `:error:line` | last error's line number |
| `:error:message` | `<code>, <1\|0>, <details>` |
| `:error:lines` | all lines, comma-separated |
| `:error:messages` | all messages, `\n`-separated |
| `message => :error` | set an error with the given message |
| `0 => :error:resolve` | mark errors as resolved (`1` → `0`) |

### Format of `error:message`

`<code>, <1|0>, <details>`

- `<code>` — error type: `custom`, `occupied`, `notfound`, `recursive`, `assert`, `range`, `const`, `nested`.
- **`1` — active, `0` — resolved (inactive).**
- `<details>` — free text.

**Examples:**
- `custom, 1, fail`
- `occupied, 1, target=main.cycle, agent=agent-2`
- `range, 1, index=99, size=5`
- `occupied, 0, target=main.cycle, agent=agent-2` (resolved)

### Setting an error

```true
message => :error
```

`message` is substituted; written to the error stack; `PAUSED`; `errorLine = currentStep`.

### Resolving errors

```true
0 => :error:resolve
```

Replaces `, 1,` with `, 0,` in `error:message`, marks `LogEntry.resolved = true`, clears red in editor and log, sets green.

### Assert via `?` + `:error`

```true
!(a == b) ? "a != b: $a vs $b" => :error
```

When `a == b` — skip; when `a != b` — error with the message. No separate `:assert` is needed.

### Reaction via `actions`

```
:error ? ^ :handle
:run
```

**Reset:** `reset()` — clears the error stack.

## Agents

A program is **always** executed by agents. 1 agent per `.agent` file. Multiple `.agent` — multiple agents.

### Agent containers

| Container | What |
|---|---|
| `step` | Line number (updated on move) |
| `cycle` | File name (updated on move) |
| `speed` | Speed in ms per step (`100 = speed`) — synchronized with `AgentState.speedMs` |
| `color` | Agent color (`#00cc22 = color`) — synchronized with `AgentState.color` |

### Agent functions

```true
:agent:<name>:status      => <dst>
:agent:<name>:waiter      => <dst>
:agent:status             => <dst>
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

`timeout` — maximum wait time. If exceeded — auto-`PAUSED`, `results["error"] = "timeout"`.

### Waiter — auto-detection

- `:result` in the file → waiter. After `:result` — waits for `parameters` again.
- No `:result` → cyclic.

### Vertical files

- `fileOrder` — vertical order (Y axis).
- `↑` / `↓` buttons in the file button row.
- Agent movement order — by Y (top to bottom).

### UI

**File buttons** (`⟲`, `↑`, `↓`, `✎`, `🗑`) — top, **all** files.
**Agent buttons** (`▶⏸⏹⏭⏮`) — bottom, **only** `.agent`.
**`⛶`** — in the header, all files.

## `:simple` — interpreter functions

`:simple:*` — 21 functions, async, log destructive actions.

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

**Result:** `results["simple"] = "1"` (accepted, async).

**Path:** absolute — as is; relative — from `projectDir`, fallback `baseDir`.

## Settings

| Function | Syntax | Default |
|---|---|---|
| `:historyLimit` | `100 => :historyLimit`, `:historyLimit => dst` | 100 |
| `:logLimit` | `1000 => :logLimit`, `:logLimit => dst` | 1000 |
| `:strictMode` | `1 => :strictMode`, `:strictMode => dst` | 0 |
| `:autoSync` | `1 => :autoSync`, `:autoSync => dst` | 1 |

## Functions — files

**File = function.** `math.cycle` — function `:math`.

```true
math:fact:
parameters == 0 ? 1 => :result
parameters - 1 => .:math:fact => sub
parameters * sub => :math => :result
:result
```

- `:parameters` — entry marker.
- `=> :result` — set the value. **Does not return**, stores.
- `:result` at the end — end marker. Waiter — auto.
- `:run` — atomic execution.
- `:log` — log.

Call: `data => :math`, `data => :main`.

**System names are reserved.**

**Local call:** `data => .:file:fn` — call function `fn` from the current file. `data => .:file:fn => dst` — same with result assigned to `dst`.

## `actions` — the agent's program

- `AgentState.actions: String` (multiline), default `":run"`.
- `executeOneStep` advances `currentStep`, executes `actions`.
- `:run` — atomic execution (no `actions`, no recursion).
- `:log` — log the current line.
- If there is no `:run` — the line is not executed (only `actions`).

## `:stack` — explicit stack (recursion alternative)

`:stack:push` = `:push`, `:stack:pop` = `:pop`, `:stack:peek` — top element without removal.

```true
1, 2, 3 = stack
stack:y, 4 => :stack:push     \ "1,2,3,4"
stack:y => :stack:peek        \ "4"
stack:y => :stack:pop         \ "4", rest "1,2,3"
```

**Recursion via explicit stack** — instead of native call stack:

```true
= stack
5 => stack:y => :push
= loop
stack:y => :size => sz
sz == 0 ? ^ done
stack:y => :pop => cur
cur => :log
^ loop
= done
```

This is **iteration** with an explicit stack, not a recursive call. Any recursive algorithm can be rewritten this way.

## Strict Mode

Strict Mode **forbids** calls to system functions. User `:file_name` (for `file_name.cycle`) — **allowed**. Substitution of named containers — **allowed**.

**System names (forbidden in Strict Mode):**

```
math, data, json, file, api, console, save, load, get, post, size, s,
sum, min, max, avg, product, count, sort, map, reduce, fold, each,
call, filter, matches, error, message, agent, run, log, repeat, range,
parameters, result, activeTab, path, source, test, return, assert,
simple, stack
```

**Allowed:**
- `:file_name` — user files (`file_name.cycle` / `file_name.agent`)
- `^ :stop` / `:pause` / `:resume` / `:delete` / `:reset` — commands
- `=`, `=>`, `->`, `?`, `\`, `func1` — substitution

## Errors

- **Validation:** unknown label, `Multiple ?`, `=> :result inside function`.
- **Strict Mode:** see above.
- **Execution:** write to a constant, out of range, file missing → `PAUSED` + `errorLine`.
- **Agents:** `occupied` → error (`PAUSED`).
- `nothing = ...` → `error: 'nothing' is reserved`.
- `5 = result` → `error: 'result' is reserved`.

## Versioning

- Language: **0.11.0**. Interpreter: **1.1.0**.
- `PATCH` — fixes. `MINOR` — new features. `MAJOR` — breaking.

History — `CHANGELOG.md`.

## Version history (0.11.0)

- **Substitution of named containers** — a name in operation position is substituted and executed.
- **`:map` / `:reduce` / `:fold` / `:each`** — new functional operations.
- **`:foreach` removed** — replaced by `:each`.
- **`:error` format** — `<code>, <1|0>, <details>`; `:error:resolve`; `LogEntry.resolved`.
- **Settings** — `:historyLimit`, `:logLimit`, `:strictMode`, `:autoSync`.
- **`:simple:*`** — 21 interpreter functions.
- **`:stack:peek`** — new (top element without removal).
- **`speed` and `color`** — agent containers.
- **`MrGreen.agent`** — first start (`#00cc22 = color`).
- **Escaping** — `\ ` (line), `\,` / `\;` / `\.` (single separator).
- **Strict Mode** — forbids system functions, allows user `:file_name` and substitution.
- **Modules and imports are not needed** — any file can be opened from code.