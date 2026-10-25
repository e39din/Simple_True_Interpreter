## Язык

- [x] `math` / `data` разделение: `math` — числа, `data` — оси.
- [x] `foreach` с несколькими осями, именованными переменными, блоком.
- [x] `.:function` — вызов функции в текущем файле.
- [x] `:return` — конец функции.
- [x] `filter`, `matches`.
- [x] `data:set` — быстрая запись.
- [x] `api:websocket` — с корректным закрытием соединений.
- [x] `json:get`, `json:set`, `json:names`, `json:values`.
- [x] Оптимизации: кэш Regex/RPN/JSON, `StringBuilder`, `withIndex`.
- [x] `=> next` / `=> back` помечены как deprecated (no-op, лог с предупреждением).
- [ ] **Удалить `=> next` / `=> back` из языка** — они дублируют UI Step/Step Back и не несут ценности в программе. Заменить на `=> step` / `=> step:back` как осмысленные команды (или удалить совсем).
- [ ] **Runtime-проверка вложенного `foreach`** — сейчас только в `validate`. Добавить в `runForeach` проверку `if (inForeach) error`, чтобы защитить CLI-использование.
- [ ] **Предупреждение при незакрытом `foreach`** — если нет `.` до конца файла, писать warning в лог.
- [ ] **`json:names` для массива** — сейчас возвращает пустой список. Добавить предупреждение в лог, если корень не Map.
- [ ] **Версия языка на Rust + egui.**
  Полный порт `Interpreter` на Rust. UI — `egui`. Цель: убрать JVM,
  cold start ~5–50 мс, без GC-пауз.
- [ ] `true-core` — отдельный модуль (JVM) без UI. **Готово.**
- [ ] `Scheme` / `Table` — реальные просмотры.
- [ ] Interactive Tutorial (расширенный).
- [ ] JSON Schema (подмножество).

## UI

- [x] Welcome с `SAMPLES (N/24)`, кликабельный.
- [x] Undo/Redo, auto-save, dirty-флаг.
- [x] SearchDialog с Next/Prev.
- [x] Actions по семействам, поиск, `Other`.
- [x] Strict Mode.
- [x] Golden-тесты в Settings.
- [x] Tutorial (4 шага).
- [x] JSON-группа в Actions.
- [x] **Вкладки → кнопки в горизонтальной полосе** с прокруткой.
- [x] **Values и Actions сворачиваются** в полоску с повёрнутым текстом.
- [x] **Кнопка `⛶` на Cycle** — сворачивает обе панели.
- [x] **Выпадающий список активной программы** рядом с Play.
- [x] **`Play` исполняет зафиксированную вкладку** (устойчиво к переключению).
- [x] **`SamplesDialog`** — 24 кликабельных карточки.
- [x] **`TabsPicker` без SAMPLES** — только New/Open File/Open Folder/Recent.
- [ ] `Scheme` / `Table` — реальные.
- [ ] Тёмная тема.
- [ ] **Rust + egui UI.**

## Инфраструктура

- [x] `SAMPLES.txt` с `!!` эталонами.
- [x] `GoldenTests` (параллельно).
- [x] `true-core` как отдельный JVM-модуль.
- [ ] Unit-тесты `Interpreter`.
- [ ] CI под Windows / macOS / Linux.
- [ ] Публикация `true-core` в Maven.
- [ ] **Rust-порт `true-core`.**