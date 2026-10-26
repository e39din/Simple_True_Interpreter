# PROMPT — instructions for AI assistant

## Role

You work on the **Simple True Interpreter** — the interpreter of the **True** language (Kotlin + Compose Desktop). Package `be.simple`, core — `be.true`.

## Sources of truth

- **`LANGUAGE.md`** — full spec of the True language. **Read before any change to the interpreter.** Do not duplicate it here.
- **`FUNCTIONS.md`** — system functions reference.
- **`SAMPLES.md`** — examples.
- **Code** — `true-core/` (interpreter), `composeApp/` (UI).

## Terminology

- **Container** — a string of data. A one-line container is a string, a multi-line container is a file.
- **Named container** — a container with a name (`value = name`). The name is a reference to the content.
- **Cycle** — a `.cycle` file (a file-function).
- **Agent** — a `.agent` file (a file-function + agent UI buttons).
- **`.true`** — **no longer used** (reserved for project settings).

## Key model

1. **Everything is a function with `:`.** System (`:math`, `:data:*`, `:json:*`, `:file:*`, `:api:*`, `:console`, `:save`, `:load`, `:size`, `:s`, `:get`, `:post`, `:log`, `:message`, `:result`, `:parameters`, `:range`, `:repeat`, `:error`, `:agent:*`, `:run`, `:map`, `:reduce`, `:fold`, `:each`, `:simple:*`, `:pauseAll`, `:resumeAll`, `:assert`) and user (`:filename` — file `filename.cycle` / `filename.agent`). **System names are reserved.**
2. **`:result`** — stores, **does not return**. `=> :result` — set. `:result` — end marker. Waiter — auto.
3. **`:parameters`** — always system. Entry of the function.
4. **`:instruction`** — **removed**. `:run` (no args) executes the current line.
5. **`actions`** — `":run"` by default.
6. **Interpreter moves the agent**, agent executes `actions`.
7. **Read/write is symmetric** (except `@`-constants).
8. **`file:a => file:b`** — copy, **`->`** — move. **`->|`** — insert (move), **`=>|`** — insert (copy).
9. **`:data:insert`, `:data:set`, `:data:length`** — deprecated.
10. **`.cycle`** — cycle. **`.agent`** — agent. **`.true`** — no longer used. Any extension — agent can jump.
11. **Empty — `""` (0 chars).** Literal `""` — 2 chars. Errors — explicit (`errorLine` → `PAUSED`).
12. **Vertical files** — Y axis for files (`fileOrder`). Buttons `↑` / `↓` in the file button row.
13. **`:error` — error stack.** Format `<code>, <1|0>, <details>` (1 — active, 0 — resolved). `:error`, `:error:line`, `:error:message`, `:error:lines`, `:error:messages`, `:error:resolve`. **`:error` does NOT pause by default.** To pause all agents — `:pauseAll`.
14. **`:each` replaces `:foreach`.** `:map` / `:reduce` / `:fold` / `:each` — new.
15. **Prefix `:data` / `:math` is optional.** `:sum`, `:pop`, `:size`, `:s`. `:math:sort` (numbers) — mandatory.
16. **Chains `=>`.** `a => :f => :g = dst` — left-associative.
17. **Name before use.** `= name` or `value = name` — **before** `:f = name`. **Exception:** name is already a variable — `= name` not needed.
18. **Examples — in the middle of a loop.** `SAMPLES.md` does not contain `=> :stop`.
19. **`^` — jump.** `^ N` (number → line), `^ :stop` / `:pause` / `:resume` / `:delete` / `:reset` (commands), `^ label` (label), `^ file.cycle` / `^ file.agent` / `^ data.txt` (file, **with dot**). `^ notes` (no dot) — **error**.
20. **Agent containers:** `step`, `cycle`, `speed`, `color`.
21. **1 agent per file — removed.** Multiple `.agent` — multiple agents. Order — by Y.
22. **UI:** file buttons (`⟲`, `↑`, `↓`, `✎`, `🗑`) — top, all files. Agent buttons (`▶⏸⏹⏭⏮`) — bottom, only `.agent`. `⋯` — **removed**.
23. **`:agent:main => file:other` / `-> file:other` / `-> nothing`** — **removed**.
24. **`:agent:main => :stop` / `:pause` / `:resume` / `:delete`** — **removed**, replaced by `^ :stop` / `:pause` / `:resume` / `:delete`. **`^ :reset`** — new.
25. **Left:** `:agent:main:status` / `:agent:main:waiter` / `:agent:status` / `<ms> => :agent:wait` / `<ms>, <timeout> => :agent:wait:timeout`.
26. **`=>` is never naming.** `chain => name` — error if `name` is not declared. Naming — only `= name`.
27. **Substitution of named containers.** Name in operation position — substituted and executed.
28. **Any subject starting with `:` is both a function and a container. No exceptions.** `:name` — substitution (empty string if not set). `:name => dst` / `:name = dst` — naming. `dst = :name` — error.
29. **`:simple:*`** — 36 functions, async, result in `:simple` = `"1"`.
30. **Escaping** — `\ ` (line literal), `\ ... \` (mid-line literal), `\,` / `\;` / `\.` (single separator).
31. **Strict Mode** — forbids system functions, allows user `:file_name` and substitution.
32. **Modules and imports are not needed.** Any file can be opened from code.
33. **Settings** — `:historyLimit` (100), `:logLimit` (1000), `:strictMode` (0), `:autoSync` (1).
34. **`:stack:peek`** — top element without removal.
35. **`MrGreen.agent`** — first start, `#00cc22 = color`.
36. **`:assert`** — `a == b, "msg" => :assert`. Success — green, failure — red. **Does NOT pause by default.** To pause — call `:pauseAll`.
37. **`:pauseAll` / `:resumeAll`** — pause / resume all agents. Parameters ignored.
38. **`pauseAll` (without `:`) — not a system name.** Only `:pauseAll`.
39. **`:simple:type`** — character by character, like a human. `delay` 30 ms by default.
40. **`:simple:typeTerminal`** — character by character into stdin of a real process.
41. **`:simple:openTerminal` / `:simple:sendTerminal` / `:simple:closeTerminal`** — real OS terminal.
42. **`:simple:click`** — click a UI element (`button:*`, `tab:*`, `agent:*`, `menu:*`, `file:*`).
43. **`:simple:clickAt`** — click at absolute screen coordinates (AWT Robot). **Dangerous, runtime only, no headless.**
44. **`:simple:search:*`** — search results (8 functions).

## Language docs format

- **All docs in English** (`LANGUAGE.md`, `FUNCTIONS.md`, `SAMPLES.md`, `README.md`, `README-TRUE.md`, `ROADMAP.md`).
- `CHANGELOG.md` — history, new entries in English.
- `PROMPT.md` — instructions (Russian acceptable), rule «all docs in English».
- **FUNCTIONS.md format:**
    - **Description** — one sentence.
    - **True** — the sample. The result is in the container `:name`.
    - **Inline comment** (only if True is 1 line **and** py/random are 1-line operations):
        - **short** — `\ py: \`...\` | <random>: \`...\`` (same line)
        - **long** — `\ py: \`...\`` on the **next line**
        - **2+ operations** or **multi-line** in py/random — comment omitted.
        - **random** — not py/rust/bash.
        - **no imports**.
    - **`<details>`** — 12 languages (Python, Rust, Bash, Elixir, C, Kotlin, Haskell, Lua, Erlang, JavaScript, Go, Clojure).
    - **No Datalog.**

## Working principles

1. **Language is containers.** Everything is a container (0 chars — empty).
2. **Function names — with `:`.** Without `:` — not a function.
3. **`=>` copies, `->` moves.** **`=>` is never naming.**
4. **Axes `l/x/y/z`.** Explicit axis — by it; not specified — `x`. Exception: `substring` / `replace` / `chars` — only `x`.
5. **`nothing` — sink.**
6. **Program is a loop + switch.**
7. **Program changes itself.** `syncCodeWithVariables`.
8. **Agents execute the program.** 1 `.agent` = 1 agent.
9. **Visual clarity > encapsulation.**
10. **Clarity > brevity.**
11. **File = storage + function.** `:parameters` (in), `:result` (out).
12. **If data exists — you can work with it.**

## Prompt rules (mandatory)

- **All UI changes — coordinate with the designer.** Colors, icons, button placement, panels, dialogs — **before** implementation.
- **Update documentation and examples on every language change.** `LANGUAGE.md`, `FUNCTIONS.md`, `SAMPLES.md`, `README.md`, `README-TRUE.md`, `ROADMAP.md`, `CHANGELOG.md` — **synchronously**.
- **Validate MD before output:** no broken ```, tables intact, links exist.
- **Priority of simple operations:** if a variant with `=>` / `->` / `=` / `^` / `?` is roughly equal to a variant with a special function — choose simple.
- **If the user asks for a file — output as one block convenient for copying.**
- **All language changes — first blocks (plan), then code.**
- **When doing a task — output the result as a short table.** Questions — also as a short table.
- **Don't output the code** until all steps are complete. Output the full code at the end when all tasks are done.
- **`FUNCTIONS.md` format:** description + True + inline (only if 1-line) + `<details>` (12 languages, no Datalog).

## What NOT to do

- Do not introduce numbers/types/arrays as entities — only containers and axes.
- Do not add `for` / `while` — only implicit loop + `^` + `?`.
- Do not duplicate `LANGUAGE.md` in this file — reference it.
- Do not change `PROMPT.md` without explicit user request.
- Do not return "properties" — only functions with `:`.
- Do not use `.true` — only `.cycle` / `.agent`.
- Do not use "data string" — only "container".
- Do not use `=>` for naming — only `= name`.
- Do not include imports in examples.
- Do not add Datalog to `FUNCTIONS.md`.
- Do not use `pauseAll` without `:` — only `:pauseAll`.

## Project structure

```
true-core/          — interpreter (Kotlin, no UI)
  Interpreter.kt    — parsing, execution, substitution, math, data, functional
  JsonParser.kt     — JSON
  OsApi.kt          — OS (files, processes)
  ServerApi.kt      — HTTP/WS
composeApp/         — UI (Compose Desktop)
  Substrate.kt      — shared state (TabState, AgentState, FileKind, TerminalModel, SnackbarModel)
  MainScreen_*.kt   — App/User/System/Server
  TrueHighlighter.kt, Strings*.kt — syntax highlight, localization
  PROMPT.md, LANGUAGE.md, FUNCTIONS.md, README-TRUE.md, README.md,
  CHANGELOG.md, SAMPLES.md, ROADMAP.md
```

## Tone

- Concise, to the point, no fluff.
- If bug — first diagnosis, then patch.
- If unsure — ask.