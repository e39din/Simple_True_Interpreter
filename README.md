# Simple True Interpreter 1.1.0

Cross-platform interpreter for the **True** language (Kotlin + Compose).

## What is True

**True** is a minimalist language where everything is a container. No numbers, types, or arrays. Only containers and conventions over them.

### 5 principles

1. **Everything is a container.** `"Hello"` — 7 chars (with quotes), `Hello` — 5. Empty container — 0 chars (not `""`).
2. **Naming, not creating.** `data = name` gives the name `name` to the value `data`. `=>` is never naming. Changing a value by name changes the program text.
3. **`=>` copies, `->` moves.** `->` clears the source, the name stays.
4. **Axes `l/x/y/z`** — structure of a container. Default axis — `x`.
5. **`nothing` — universal sink.** `file:x -> nothing`, `<var> -> nothing`.

### 6 constructs

| Construct | Syntax |
|---|---|
| Naming | `data = name` / `= name` / `data = name *` / `data = name @` |
| Label / jump | `= label` / `^ N` / `^ label` / `^ file.cycle` / `^ :stop` |
| Condition | `condition ? operation` (parentheses, `&&` / `\|\|` / `!`) |
| Copy / move | `a => b` / `a -> b` / `v ->\| data:y:3` / `v =>\| data:y:3` |
| Commands | `^ :stop` / `^ :pause` / `^ :resume` / `^ :delete` / `^ :reset` |
| Functions | `<data> => :<fn>` (all names — with `:`) |
| Name before use | `= name` (empty container) / `value = name` — **before** `:f = name` |

### 1 example

```true
0 = a
1 = b
12 = max

= do

a + 1 => :math
:math = a
a < max ? ^ do

=> :stop
```

### Functions (brief)

`:math` (`:sum`, `:min`, `:max`, `:avg`, `:product`, `:count`, `:math:sort`),
`:data:*` (`:push`, `:pop`, `:size`, `:s`, `:sort`, ...), `:map` / `:reduce` / `:fold` / `:each`,
`:json:*`, `:file:*`, `:api:*`, `:console`, `:run`, `:save`, `:load`, `:get`, `:post`,
`:log`, `:message`, `:result`, `:parameters`, `:range`, `:repeat`, `:error`, `:agent:*`, `:simple:*`.

Details — `FUNCTIONS.md`. Examples — `SAMPLES.md`.

## Application

- Menu `File` / `Run` / `Settings` / `About`.
- Step-by-step forward/backward (history up to 100).
- Auto-loop with configurable speed.
- Editor with line numbers, current-line highlight, zebra, mouse selection.
- Values: containers + hidden + results.
- Log: bottom, newest on top, filters (`Messages`).
- Actions: right, search, description, copy on click, Pin (right click), collapsed.
- Files: `.cycle` (cycle), `.agent` (agent), `.txt` / `.json` / no extension.
- Agents: 1 `.agent` = 1 agent. Multiple `.agent` — multiple agents.
- `Cycles` — vertical rows (`fileOrder`), `↑` / `↓` in the file button row.
- Search / replace (`Ctrl+F` / `Ctrl+R`).
- Highlight of the active agent button (`lastPressed`).
- Expandable editor (up to half of the screen).
- `:map` / `:reduce` / `:fold` / `:each` — functional operations.
- `:stack:peek` — top element without removal.
- `:error` — error stack, format `<code>, <1|0>, <details>`, `:error:resolve`.
- `^ N` / `^ label` / `^ file.cycle` / `^ :stop` / `:pause` / `:resume` / `:delete` / `:reset`.
- Agent containers: `step`, `cycle`, `speed`, `color`.
- `MrGreen.agent` — first start.
- EN UI + RU localization (Settings → Language).
- `:simple:*` — 21 interpreter functions.

### Buttons

**File buttons** (top, all files): `⟲` (Reset), `↑` / `↓` (Move up/down), `✎` (Rename), `🗑` (Delete), `⛶` (Expand).

**Agent buttons** (bottom, only `.agent`): `▶` (Play), `⏸` (Pause), `⏹` (Stop), `⏭` (Step), `⏮` (Step Back).

### Hotkeys

- `Ctrl+F` — search.
- `Ctrl+R` — replace.
- `Ctrl+P` — play/pause (toggle).
- `Ctrl+↑` / `Ctrl+↓` — step back / forward (all agents).
- `Ctrl+Z` / `Ctrl+Shift+Z` — undo / redo.

### What we don't have and won't have

- **Compilation.** True is an interpreter. Code changes itself on the fly (`syncCodeWithVariables`), step-by-step mode, agent `actions` — all of that requires interpretation.
- **Types.** Everything is a container. Axes `l/x/y/z` — the only structure.
- **`for` / `while`.** Only the implicit program loop + `^` + `?`.
- **Classes / OOP.** File-functions, agents, `actions`.
- **Multithreading with shared memory.** Agents are isolated coroutines.
- **`.true`.** No longer used (reserved for project settings).
- **Modules / imports.** Any file can be opened from code — `:file:read` / `^ file.cycle` / `:file_name`.

Details — `ROADMAP.md`.

## Files

`~/.simple/main.cycle` — first file, `~/.simple/start.cycle` — start state, others — user files. `.agent` — agents. `.true` — no longer used.

## Build

JDK 21. `./gradlew :composeApp:run`.

## Version

1.1.0 (language 0.11.0).