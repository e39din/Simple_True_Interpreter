# Changelog

## 0.11.0 — Substitution, `:map`/`:reduce`/`:fold`/`:each`, `:error` format, `:simple:*`

- **Substitution of named containers.** A name in operation position is substituted and executed. Applies to any container — with value (`5 = a`) or action (`a + 1 => sum = func1`). Recursion guard, max depth 10.
- **`=>` is never naming.** `chain => name` — error if `name` is not declared. Naming — only `= name`.
- **`:math` / `:data:*` / `:error` / `:json:*` / `:file:*` / `:api:*` — result is already in `results[...]`.** To name — `= name` separately, or `:math = name`.
- **`:map` / `:reduce` / `:fold` / `:each`** — new functional operations.
- **`:foreach` removed.** Replaced by `:each`.
- **`:stack:peek`** — new (top element without removal).
- **`:simple:*`** — 21 interpreter functions (async, result in `results["simple"] = "1"`).
- **`:agent:wait:timeout`** — new.
- **Settings:** `:historyLimit` (100), `:logLimit` (1000), `:strictMode` (0), `:autoSync` (1).
- **`:error` format** — `<code>, <1|0>, <details>` (1 — active, 0 — resolved). `:error:resolve`, `LogEntry.resolved`, `errorLines` / `resolvedLines`.
- **`COPY_RE`** — `a => b` copies.
- **Escaping** — `\ ` (line literal), `\,` / `\;` / `\.` (single separator).
- **Strict Mode** — forbids system functions, allows user `:file_name` and substitution.
- **Modules and imports are not needed** — any file can be opened from code.
- **`MrGreen.agent`** — first start (`#00cc22 = color`).
- **Agent containers:** `step`, `cycle`, `speed`, `color`.
- **Jump to a file without reset.** `agent.fileName = target`, `currentStep = 1`, `variables` preserved. `interpreter.reset()` — only on `^ :reset`.
- **`occupied` error** — if the target file is occupied by another agent, status `PAUSED`, error `occupied, 1, target=..., agent=...`.
- **`NavigateBehavior` removed** — no more "occupied" dialog. Agent jumps without reset, or `occupied` error.
- **EN UI + RU localization** — Settings → Language.
- **Syntax highlight** (simple) — `TrueHighlighter`.
- **`MainScreen_User.kt`**: `errorLines` red, `resolvedLines` green in editor and log.
- **`GoldenTests`** — `withTimeout(30_000)`, `waitUntilMs = 0`.
- **`JsonParser`** — LRU 100.
- **`parse(code)` cache** — by reference.
- **`mathResultCache`** — 500 → 2000.
- **`publishActive`** — from Main thread.
- **`AgentsModel.create`** — rejects duplicate names.
- **CLI** — `be.true.Main` restored.
- **`NavigateBehavior` in settings.txt** — removed.
- **Version:** language 0.11.0, interpreter 1.1.0.

### Removed / deprecated

- `:foreach` — removed (replaced by `:each`).
- `:agent:main => :stop` / `:pause` / `:resume` / `:delete` — removed (replaced by `^ :stop` / `:pause` / `:resume` / `:delete`).
- `:agent:main => file:other` / `-> file:other` / `-> nothing` — removed.
- `:agent:main:currentFile` / `:agent:main:currentStep` — removed.
- `NavigateBehavior` (enum, field, dialog, `pendingNavigateFile`) — removed.
- `stepBackLimit` — removed (replaced by `:historyLimit`).
- `:data:insert` / `:data:set` / `:data:length` — deprecated.
- `:instruction` — removed (was in 0.10.0).
- `MAX_AGENTS` — removed.
- `.true` extension — no longer used (reserved for project settings).

---

## 0.10.0 — Agent = `.agent` file, `.cycle` instead of `.true`

- **Terminology:** "container" / "named container" instead of "data string".
- **`.cycle`** — cycle (file-function). **`.agent`** — agent (file-function + UI buttons). **`.true`** — no longer (reserved for project settings).
- **Any extension** — agent can jump to any file via `^`.
- **`:instruction` removed.** `actions` — `":run"` (no `:instruction`). `:run` without argument — executes the current line. `:log` without argument — logs the current line.
- **`^ N`** — jump to line N (was `^ step:N`).
- **`^ label`** — jump to label (no dot, exists in the file).
- **`^ file.cycle` / `^ file.agent` / `^ data.txt`** — jump to file (with dot). **`^ notes`** (no dot) — error.
- **`^ :stop` / `:pause` / `:resume` / `:delete`** — agent commands (was `:agent:main => :stop` / `:pause` / `:resume` / `:delete`).
- **`^ :reset`** — new (reset the file, like `⟲`).
- **Agent containers:** `step` (line number), `cycle` (file name).
- **Removed:** `:agent:main:currentFile`, `:agent:main:currentStep` (duplicates).
- **Removed:** `:agent:main => file:other`, `-> file:other`, `-> nothing` (agent moves only `^`).
- **Left:** `:agent:main:status`, `:agent:main:waiter`, `:agent:status`, `<ms> => :agent:wait`.
- **1 agent per file — removed.** Multiple `.agent` — multiple agents. `MAX_AGENTS` — removed. Order — by Y (top to bottom).
- **UI:** file buttons (`⟲`, `↑`, `↓`, `✎`, `🗑`) — top, all files. Agent buttons (`▶⏸⏹⏭⏮`) — bottom, only `.agent`. `⋯` — **removed**. `Templates…`, `Actions…`, `Copy to…`, `Move to…`, `Delete agent` — **removed**.
- **`TemplatesDialog`** — left in `Settings`.
- **`ActionsDialog` / `CopyToDialog` / `MoveToDialog` / `DeleteAgentDialog`** — **removed**.
- **Docs:** `LANGUAGE.md`, `README-TRUE.md`, `README.md`, `SAMPLES.md`, `PROMPT.md`, `ROADMAP.md`, `CHANGELOG.md` — updated.

---

## 0.9.1 — `foreach` without `.`, `:error`, `:sum`/`:pop` without prefix

- **`foreach` without `.`.** End of body — no `,` at the end of the line.
- **Prefix `:data` / `:math` optional.** `:sum`, `:min`, `:max`, `:avg`, `:product`, `:count`, `:push`, `:pop`, `:reverse`, `:unique`, `:contains`, `:slice`, `:join`, `:charAt`, `:substring`, `:indexOf`, `:replace`, `:lower`, `:upper`, `:trim`, `:chars`, `:remove`, `:swap`, `:deleteRange`, `:replaceRange`, `:sort`, `:size`, `:s` — without prefix. `:math:sort` (numbers), `:json:*`, `:file:*`, `:api:*` — mandatory.
- **`:error` — error stack.** `:error`, `:error:line`, `:error:message`, `:error:lines`, `:error:messages`. Reset — `reset()`.
- **Reaction to errors — `actions`:** `":error ? ^ :handle"`. `:error` does not clear `PAUSED`.
- **Chains `=>`** — `a => :f => :g => dst`.
- **Name before use** — `= name` before `:f => name`.
- **Examples without `=> :stop`** — work in the middle of a loop.

---

## 0.9.0 — Everything is a function with `:`

- **Everything is a function with `:`.** `:agent:name:currentFile`, `:agent:name:waiter`, `:return` → `:result`, `:parameters`, `:instruction`, `:run`, `:log`, `:message`.
- **`:result`** — stores, does not return. Waiter — auto by `:result`.
- **`:instruction`** — current line of the agent.
- **`actions`** — the agent's program (multiline, `:instruction => :run`).
- **`:run`** — atomic execution (no `actions`, no recursion). No argument — no-op.
- **`file:a => file:b`** — copy. **`file:a -> file:b`** — move.
- **`file:a => var`**, **`var => file:b`**.
- **`v ->| data:y:3`** — insert (move). **`v =>| data:y:3`** — insert (copy). **`v -> data:y:3`** — replace.
- **Removed:** `:data:insert` (→ `->|`), `:data:set` (→ `->`), `:data:length` (→ `:data:y:size` / `:s`).
- **New:** `:s`, `:range`, `:repeat`, `:message`, `:save`, `:file:a => :path`, `:main:l:1`.
- **Naming of actions:** `a + 1 => a = action` — not substituted.
- **System names reserved.**
- **`main.true`** — a normal function, runs first.
- **Read/write — symmetric** (except `@`).
- **Vertical files** — `fileOrder`, `↑` / `↓`.
- **`lastPressed`** — highlight of the active agent button.
- **Search / replace** (`Ctrl+F` / `Ctrl+R`), `searchMode`.
- **`Ctrl+P`** — play/pause. **`Ctrl+↑` / `Ctrl+↓`** — step back/forward.
- **`ActionsPanel`** — copy on click, search, description, Pin, collapsed.
- **Panel state persistence**.
- **Expandable editor** (up to half of the screen), mouse selection.
- **Renaming of variables** (diff + Ctrl).
- **Values when code changes.**

---

## 0.8.0 — Agents

- 1 agent per file. Rainbow (7).
- `:agent:<name> => :stop/:pause/:resume/:delete`.
- `:agent:<name> -> file:<target>` (move), `=> file:<target>` (copy).
- `:agent:<name> -> nothing`, `file:<name> -> nothing`, `<var> -> nothing`.
- `<ms> => :agent:wait`.
- `Cycles` — column ribbon.
- `<name>:splitter`.

---

## 0.7.0 — data:join, file:data/origin/lines

- `data:join`.
- `file:data`, `file:origin`, `file:lines`.
- `=> :next` / `=> :back` — deprecated.
- Principle 12.

---

## 0.6.1 — Marks

- `data:substring` / `indexOf` / `replace` / `lower` / `upper` / `trim` / `chars` / `charAt` — only `x`.
- `math` — only for calculations; for concatenation — `$`.

---

## 0.6.0 — math/data, foreach, functions

- `math` / `data` separation.
- `foreach` with axes, variables, body.
- `.:function`, `:return` (now `:result`).
- `filter`, `matches`.
- `data:set` (now `->`).
- `api:websocket`.
- `json:get`, `json:set`, `json:names`, `json:values`.
- One `?`.
- `:parameters`.