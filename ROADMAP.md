# Roadmap

## Done

### Language
- [x] `math` / `data` separation.
- [x] `filter`, `matches`.
- [x] `api:websocket`.
- [x] `json:get/set/names/values`.
- [x] Parentheses in conditions and `math`.
- [x] `->|` / `=>|` — insert.
- [x] `file:a => file:b`, `file:a -> file:b`.
- [x] `:result` (instead of `:return`).
- [x] `:instruction` — **removed**.
- [x] `actions` — `":run"`.
- [x] `:run` — atomic execution.
- [x] `:log` — log the current line.
- [x] Waiter — auto by `:result`.
- [x] All functions — with `:`.
- [x] `:s` — short for `:size`.
- [x] `:range`, `:repeat`.
- [x] `:message` — type `"message"`.
- [x] `:save` (`=> :save`, `file_name:save`).
- [x] `:file:a => :path`.
- [x] `:data:y:size` (instead of `:data:length`).
- [x] Action naming.
- [x] Prefix `:data` / `:math` is optional.
- [x] `:error` — error stack, format `<code>, <1|0>, <details>`, `:error:resolve`.
- [x] Chains `=>`.
- [x] Name before use (`= name`).
- [x] **Terminology:** container / named container.
- [x] **`.cycle`** — cycle. **`.agent`** — agent. **`.true`** — no longer used.
- [x] **`^ N`** — jump to line N.
- [x] **`^ file.cycle`** / **`^ file.agent`** / **`^ data.txt`** — jump to file.
- [x] **`^ :stop`** / **`:pause`** / **`:resume`** / **`:delete`** / **`:reset`** — commands.
- [x] **Agent containers:** `step`, `cycle`, `speed`, `color`.
- [x] **1 agent per file — removed.**
- [x] **Movement order** — by Y.
- [x] **`copy`** (`COPY_RE`).
- [x] **Substitution of named containers** — a name in operation position is substituted and executed.
- [x] **`=>` is never naming** — only `= name`.
- [x] **`:map` / `:reduce` / `:fold` / `:each`** — new.
- [x] **`:foreach` removed.**
- [x] **`:stack:peek`** — new.
- [x] **`:simple:*`** — 21 functions.
- [x] **`:agent:wait:timeout`**.
- [x] **Settings:** `:historyLimit`, `:logLimit`, `:strictMode`, `:autoSync`.
- [x] **Escaping:** `\ ` (line), `\,` / `\;` / `\.` (single separator).
- [x] **Strict Mode** — forbids system functions, allows user `:file_name` and substitution.
- [x] **Modules and imports are not needed.**
- [x] **`MrGreen.agent`** — first start.
- [x] **EN UI + RU localization.**

### UI
- [x] Undo/Redo, auto-save.
- [x] SearchDialog (`find` / `replace`).
- [x] Actions by families, search, description, Pin.
- [x] Strict Mode.
- [x] Golden tests.
- [x] Tabs → chips.
- [x] Values and Actions collapse.
- [x] `⛶` — expand.
- [x] `SamplesDialog`.
- [x] `TabsPicker`.
- [x] `lastPressed` — button highlight.
- [x] `Ctrl+P` — play/pause.
- [x] `Ctrl+↑` / `Ctrl+↓` — step back/forward.
- [x] `Ctrl+F` / `Ctrl+R` — find/replace.
- [x] Expandable editor.
- [x] Mouse selection.
- [x] Variable renaming.
- [x] Values when code changes.
- [x] Vertical files (`fileOrder`, `↑` / `↓`).
- [x] Panel state persistence.
- [x] **File buttons** (`⟲`, `↑`, `↓`, `✎`, `🗑`) — top, all files.
- [x] **Agent buttons** (`▶⏸⏹⏭⏮`) — bottom, only `.agent`.
- [x] **`⋯` removed** — everything in the button row.
- [x] **`NavigateBehavior` removed** — no more "occupied" dialog (agent jumps without reset, or `occupied` error).
- [x] **`:error` red/green highlight** in editor and log.
- [x] **Syntax highlight** (simple) — `TrueHighlighter`.
- [x] **EN/RU switcher** (Settings → Language).

### Infrastructure
- [x] `SAMPLES.md` with `!!`.
- [x] `GoldenTests` (parallel).
- [x] `true-core` — separate JVM module.
- [x] Unit tests `Interpreter`.
- [x] `FUNCTIONS.md` (short format, 12 languages, no Datalog).

## In progress

- [ ] Dark theme.
- [ ] CI under Windows/macOS/Linux.
- [ ] `:agent:name:actions` — read/write `actions`.

## Plans

- [ ] **Rust + egui.** Port `Interpreter` to Rust. Goal: remove JVM, cold start ~5–50 ms.
- [ ] **Full syntax highlight** — tokenization of all keywords and containers.
- [ ] **MD renderer** in `DocDialog` — highlight for `true` / `python` / etc.
- [ ] `Scheme` / `Table` — real views.
- [ ] Interactive Tutorial (advanced).
- [ ] JSON Schema (subset).
- [ ] Publish `true-core` to Maven.
- [ ] **Rust port of `true-core`.**
- [ ] Multilingual (Lua via LuaJ, JS via Rhino).
- [ ] External interpreter via WebSocket.
- [ ] `.true` — project settings file.

## What we won't do (and why)

### Compilation — not our path

- **No compilation.** Compilation is yesterday. Today is **on-the-fly execution** (JIT, interpretation, dynamic substitution).
- **We have everything for it:** powerful hardware, ready-to-use software (JVM, Rust) that can execute instructions on the fly.
- **Compilation breaks the main thing:** a program that changes itself. In True, code is a database, `syncCodeWithVariables` rewrites the declaration text. With compilation, this is impossible.
- **Compilation kills interactivity:** step-by-step mode, `Step Back`, real-time `Values`, agent `actions` on each step — all of that is only interpretation.
- **Speed is not a problem:** `:math`, `:data:*`, `:file:*` — these are built-in functions, already "compiled" (JVM/Rust). Interpretation of True is dispatching containers, not loops over millions of elements.

### What else we won't do

- **Types.** Everything is a container. No `int`, `float`, `bool`, `list`, `map`. Axes `l/x/y/z` — the only structure.
- **`for` / `while`.** Only the implicit program loop + `^` + `?`.
- **Classes, inheritance, OOP.** File-functions, agents, `actions` — instead of classes.
- **Multithreading with shared memory.** Agents are isolated coroutines. Communication — via files and `:agent:*`, not `Mutex`.
- **Package managers, dependencies.** One file — one program. `:file:*`, `:api:*` — instead of libraries.
- **Garbage collection as a problem.** Containers are immutable, interned. JVM/Rust handle it.
- **"Syntactic sugar".** `a + b => :math` is already nice. `=` / `=>` / `->` / `^` / `?` — 5 operators. No more needed.
- **`.true` extensions.** No longer used (reserved for project settings).
- **Modules / imports.** Any file can be opened from code — `:file:read` / `^ file.cycle` / `:file_name`. No `import` / `require` / `use`.

### Where to invest

- **Rust + egui** — fast cold start, native binary, no JVM.
- **Multilingual** — Lua (LuaJ), JS (Rhino) as "file-functions".
- **External interpreter via WebSocket** — for heavy languages (Python, Erlang).
- **Dark theme**, **CI**, **publishing** `true-core`.
- **Interactive Tutorial** — advanced, interactive.