# CHANGELOG

## 1.3.0 (2026-10-27)

### Added
- `:data:groupby` — группировка с агрегацией (count/sum/min/max/avg)
- `:api:mock` / `:api:mock:reset` / `:api:mock:list` — моки API
- `:ask` — диалог с вопросом (асинхронный; `:ask:pending`, `:ask:id`)
- `:schedule:limit` — лимит jobs на агента
- `:stream:seek` — перемотка потока
- `:error:pause` / `:assert:pause` — флаги паузы при ошибке
- `:pinned:add/remove/list/clear` — закреплённые контейнеры (заменяет `:watch`)
- Синонимы имён: `12 = twelve, number, a`; goto по alias
- `?` — условие только если `cond ? action`; иначе символ строки
- `,` — не ломает `:message`/`NAMING`
- Шаблоны обучения: `templates/*.cycle` (5 шаблонов: Hello, Счётчик, Калькулятор, Пещера, Плагин)
- `settings.true` / `progress.true` (заменяют `.txt`)
- `recentProjects` — история открытых папок
- Фигура агента на холсте (кружок, кнопки play/pause/step, меню)
- Временная шкала (Timeline) — полоса активности
- Подсветка текущей строки (редактор + таблица)
- `:watch` / `:diff` / `:history` / `:agent:sync` (Autocomplete)
- `:ext` — динамические имена в Autocomplete

### Changed
- `:console` → `:terminal` (deprecated алиас)
- `StreamState` — один класс (Substrate.kt)
- `SecretModel.cache` — ConcurrentHashMap + synchronized
- `scheduleJobs` — SnapshotStateList
- Ошибки — **не пауза** (default); пауза — через флаг
- Поток = столбец (`.cycle`); `columnJobs` вместо `agentJobs`
- UI-синхронизация — `withContext(Dispatchers.Main)`
- Агент — только для `main.cycle`; другие `.cycle` — без агента
- Цвета приглушены (ваби-саби): `ACCENT=#5B8DB8`, `ERROR=#B85C5C`, `WARN=#C49A5C`
- Отступы 4/8/12/20/32; типографика 9/11/13/16/20/26
- Таблица 60/100/160/260; снап-точки 40/64/104/168

### Fixed
- `runFileStreamInternal` — реальное чтение первой порции
- `controlAgent WAIT` — `*wait` → callerId
- `InterpreterCompat.collectFunctions` — коллбэки
- `retryAction` — добавлен
- `occupied` — без паузы (E46)
- `resetFile` → `VisualModel.clear` (R10 E13)

### Removed
- `:empty` (заменено на `:data:empty`)
- `:cycle:stop` (есть `:stop`)
- `:loop`/`:endloop` (программа = цикл)
- `final_gold`/`score` в игре (лишние сущности)
- Кавычки в игре (не нужны)

## 0.12.0 (2026-10-27)

### Added
- `:mem:*` — память asm/C-стиль (`alloc/free/write/read/copy/move/fill/size/dump/reset`), указатели `@addr`
- `:terminal*` — PTY-терминал (`open/write/read/close/list`); `:console` — deprecated алиас
- `:open:url/file/folder` — ОС (`Desktop.browse/open`)
- `:xml:get/names/values` — XML (XXE-safe)
- `:secret:get/set/remove/list` — AES-GCM
- `:send:telegram/max/email` — уведомления (javax.mail)
- `:schedule` — cron-utils
- `:sound:play/stop` — WAV + MP3
- `:websocket:*` — Java-WebSocket
- `:stream:*` — `:file:stream`, `:stream:next/close/reset/limit/list`
- `:data:empty` — пустота
- `:api:*:retry` — retry с backoff, idempotent
- `:background` — фон визуализации (tile/stretch/center/fit)
- `:ext:*` — плагины через `Extensions`
- Визуализация: `rect_N`, `VisualModel`, `VisualFeed`, `TablePanel`, `ContextMenu`, `InlineEditor`, `AgentFooter`
- Примеры по ролям (12 ролей × 5 задач)
- Autocomplete — 61 ключевое слово

### Changed
- `:console` → `:terminal`
- `StreamState` — один класс (Substrate.kt)
- `SecretModel.cache` — ConcurrentHashMap + synchronized
- `scheduleJobs` — SnapshotStateList

### Fixed
- `runFileStreamInternal` — реальное чтение первой порции
- `controlAgent WAIT` — `*wait` → callerId
- `InterpreterCompat.collectFunctions` — коллбэки
- `retryAction` — добавлен

### Removed
- `:empty` (заменено на `:data:empty`)

## 0.11.0 (2026-10-26)

### Added
- Ядро: `:math`, `:data`, `:api`, `:json`, `:simple`, `:agent`, `:error`, `:assert`
- 6 библиотек: cron-utils, jlayer, mp3spi, Java-WebSocket, javax.mail, pty4j
- `:ext` — плагины
- Визуализация — `rect_N`
- Примеры по ролям (12 ролей × 5 задач)