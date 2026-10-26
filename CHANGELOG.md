# Changelog

## 0.10.0 — Агент = `.agent`-файл, `.cycle` вместо `.true`
- **Терминология:** «контейнер» / «именованный контейнер» вместо «строка данных».
- **`.cycle`** — цикл (файл-функция). **`.agent`** — агент (файл-функция +
  UI-кнопки). **`.true`** — больше нет (зарезервировано для настроек проекта).
- **Любое расширение** — агент может ходить по любому файлу через `^`.
- **`:instruction` удалён.** `actions` — `":run"` (без `:instruction`). `:run`
  без аргумента — выполняет текущую строку. `:log` без аргумента — логирует
  текущую строку.
- **`^ N`** — переход на строку N (было `^ step:N`).
- **`^ label`** — переход на метку (без точки, есть в файле).
- **`^ file.cycle` / `^ file.agent` / `^ data.txt`** — переход на файл (с
  точкой). **`^ notes`** (без точки) — ошибка.
- **`^ :stop` / `:pause` / `:resume` / `:delete`** — команды агенту (было
  `:agent:main => :stop` / `:pause` / `:resume` / `:delete`).
- **`^ :reset`** — новое (сброс файла, как `⟲`).
- **Контейнеры агента:** `step` (номер строки), `cycle` (имя файла).
- **Удалены:** `:agent:main:currentFile`, `:agent:main:currentStep` (дублируют
  `cycle` / `step`).
- **Удалены:** `:agent:main => file:other`, `-> file:other`, `-> nothing`
  (агент перемещается только `^`).
- **Оставлены:** `:agent:main:status`, `:agent:main:waiter`, `:agent:status`,
  `<ms> => :agent:wait`.
- **1 агент на файл — снято.** Несколько `.agent` — несколько агентов.
  `MAX_AGENTS` — удалён. Порядок движения — по Y (сверху вниз).
- **UI:** кнопки **файла** (`⟲`, `↑`, `↓`, `✎`, `🗑`) — сверху, для **всех**
  файлов. Кнопки **агента** (`▶⏸⏹⏭⏮`) — снизу, **только** для `.agent`.
  `⋯` — **удалён**. `Templates…`, `Actions…`, `Copy to…`, `Move to…`,
  `Delete agent` — **удалены**.
- **`TemplatesDialog`** — оставлен в `Settings`.
- **`ActionsDialog` / `CopyToDialog` / `MoveToDialog` / `DeleteAgentDialog`** —
  **удалены**.
- **Документация:** `LANGUAGE.md`, `README-TRUE.md`, `README.md`, `SAMPLES.md`,
  `PROMPT.md`, `ROADMAP.md`, `CHANGELOG.md` — обновлены.

## 0.9.1 — `foreach` без `.`, `:error`, `:sum`/`:pop` без префикса
- **`foreach` без `.`.** Конец тела — отсутствие `,` в конце строки.
- **Префикс `:data`/`:math` необязателен.** `:sum`, `:min`, `:max`, `:avg`,
  `:product`, `:count`, `:push`, `:pop`, `:reverse`, `:unique`, `:contains`,
  `:slice`, `:join`, `:charAt`, `:substring`, `:indexOf`, `:replace`,
  `:lower`, `:upper`, `:trim`, `:chars`, `:remove`, `:swap`, `:deleteRange`,
  `:replaceRange`, `:sort`, `:size`, `:s` — без префикса.
  `:math:sort` (числа), `:json:*`, `:file:*`, `:api:*` — обязательны.
- **`:error` — стек ошибок файла.** `:error`, `:error:line`, `:error:message`,
  `:error:lines`, `:error:messages`. Сброс — `reset()`.
- **Реакция на ошибки — `actions`:** `":error ? ^ :handle"`. `:error` не
  отменяет `PAUSED`.
- **Цепочки `=>`** — `a => :f => :g => dst`.
- **Имя до использования** — `= name` до `:f => name`.
- **Примеры без `=> :stop`** — работают в середине цикла.

## 0.9.0 — Всё — функция с `:`
- **Всё — функция с `:`.** `:agent:name:currentFile`, `:agent:name:waiter`,
  `:return` → `:result`, `:parameters`, `:instruction`, `:run`, `:log`,
  `:message`.
- **`:result`** — хранит, не возвращает. Waiter — авто по `:result`.
- **`:instruction`** — текущая строка агента, доступна везде.
- **`actions`** — программа агента (многострочная, `:instruction => :run`).
- **`:run`** — атомарное выполнение (без `actions`, без рекурсии). Без
  аргумента — no-op.
- **`file:a => file:b`** — копия. **`file:a -> file:b`** — перемещение.
- **`file:a => var`**, **`var => file:b`**.
- **`v ->| data:y:3`** — вставка (перемещением). **`v =>| data:y:3`** —
  вставка (копированием). **`v -> data:y:3`** — замена.
- **Удалены:** `:data:insert` (→ `->|`), `:data:set` (→ `->`), `:data:length`
  (→ `:data:y:size` / `:s`).
- **Новые:** `:s`, `:range`, `:repeat`, `:message`, `:save` (`=> :save`,
  `file_name:save`), `:file:a => :path`, `:main:l:1`.
- **Именование действий:** `a + 1 => a = action` — не подставляется.
- **Системные имена зарезервированы.**
- **`main.true`** — обычная функция, запускается первой.
- **Запись/чтение — симметрично** (кроме `@`).
- **Вертикальные файлы** — `fileOrder`, `↑`/`↓`.
- **`lastPressed`** — подсветка активной кнопки агента.
- **Поиск/замена** (`Ctrl+F`/`Ctrl+R`), `searchMode`.
- **`Ctrl+P`** — play/pause. **`Ctrl+↑`/`Ctrl+↓`** — шаг назад/вперёд.
- **`ActionsPanel`** — копирование по клику, поиск, описание, Pin, свёрнутая.
- **Сохранение состояния панелей**.
- **Расширяемый редактор** (до половины экрана), выделение мышью.
- **Ренейминг переменных** (diff + Ctrl).
- **Values при изменении кода** (Q9).

## 0.8.0 — Агенты
- 1 агент на файл. Радуга (7).
- `:agent:<name> => :stop/:pause/:resume/:delete`.
- `:agent:<name> -> file:<target>` (move), `=> file:<target>` (copy).
- `:agent:<name> -> nothing`, `file:<name> -> nothing`, `<var> -> nothing`.
- `<ms> => :agent:wait`.
- `Cycles` — лента колонок.
- `<name>:splitter`.

## 0.7.0 — data:join, file:data/origin/lines
- `data:join`.
- `file:data`, `file:origin`, `file:lines`.
- `=> :next` / `=> :back` — deprecated.
- Принцип 12.

## 0.6.1 — пометки
- `data:substring`/`indexOf`/`replace`/`lower`/`upper`/`trim`/`chars`/`charAt` —
  только `x`.
- `math` — только для вычислений; для склейки — `$`.

## 0.6.0 — math/data, foreach, функции
- `math` / `data` разделение.
- `foreach` с осями, переменными, блоком.
- `.:function`, `:return` (теперь `:result`).
- `filter`, `matches`.
- `data:set` (теперь `->`).
- `api:websocket`.
- `json:get`, `json:set`, `json:names`, `json:values`.
- Один `?`.
- `:parameters`.