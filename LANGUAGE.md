# Язык True — спецификация

Версия языка: **0.8.0**  
Версия интерпретатора: **1.0.0**  
Лицензия: Apache 2.0 (`LICENSE-TRUE`)

## Принципы

1. **Всё — строка.** Нет чисел, типов, массивов. Только строки и соглашения о них. **Кавычки — часть строки**, не синтаксис. `"Hello"` — строка `"Hello"` (с кавычками). `Hello` — строка `Hello` (без кавычек). Интерпретатор **не удаляет** кавычки — они остаются в значении.
2. **Программа — база данных.** Данные хранятся в тексте `.true`. Изменение значения по имени меняет и текст объявления.
3. **Сначала данные, потом действия.** `data, args => op`.
4. **`=>` копирует, `->` перемещает.** `->` стирает значение в источнике, имя остаётся.
5. **Оси `l/x/y/z`** — единственный способ структурировать строку. Ось по умолчанию — `x`.
6. **Строка — список, массив, структура.** Многомерность — через вложенные оси.
7. **Никаких новых типов** (`map`, `stack`, `queue`, `tree`, `graph`, указателей).
8. **Никаких `for/while`.** Только неявный цикл программы + переходы `^` и условия `?`.
9. **Один `?` на строку.** `&&`/`||`/`!` внутри условия — можно.
10. **Имя справа.** `0 = a` — данные слева, имя справа.
11. **`@` — константа, `*` — скрыть.**
12. **Программа — это switch.** Каждая строка — ветка switch.
13. **Программа всегда исполняется агентами.** 1 агент на файл. Главный (интерпретатор) — за кадром. Все операции — у агента. Каждый агент виден одновременно (`Cycles`, `FileChips`, `Log`); `Cycles`/`Values` — viewing-агент.
14. **`-> nothing` — универсальный сток.** `agent:x -> nothing`, `file:x -> nothing`, `<var> -> nothing`. Каскад: файл → агент. Фиолетовая подсветка — знак необратимости.
15. **Работаем с тем, что есть.** У данных всегда есть адрес — номер строки. Имя — дополнительная ссылка. Если имя есть — по имени. Если нет — по адресу (`^ step:N`). Имя появляется в момент именования (`данные = имя`) и живёт, пока данные существуют.

## Программа

- Нумерация строк с 1.
- Программа по умолчанию — цикл: после последней строки без `=> stop` исполнение возвращается на первую.
- `\` в начале строки — вся строка воспринимается как текст.
- `\` перед символом операции (`=`, `=>`, `^`, `->`, `?`) — символ воспринимается как текст; `\` в выводе отбрасывается.
- Строка без распознанной операции — комментарий.

## Именование

```true
value = name
value = name *
value = name @
```

- `name` — идентификатор `[A-Za-z_]\w*`. `nothing` — зарезервировано.
- `*` — скрыть из основного списка Values.
- `@` — константа: запись и перемещение запрещены.

Изменение значения по имени меняет текст программы в месте объявления (`syncCodeWithVariables`). Константы (`@`) не переписываются.

### Принцип «работаем с тем, что есть»

У данных всегда есть **адрес** — номер строки. **Имя** — дополнительная ссылка. Если имя есть — по имени. Если нет — по адресу (`^ step:N`).

**Имя не «объявляется заранее».** Оно **появляется** в момент именования (`данные = имя`) и живёт, пока данные существуют.

**Пример — имя есть:**

```true
0 = lineCount
input.txt => file:data
"" = empty
empty$file = raw
raw:l => data:length
length = lineCount
lineCount => out
```

`lineCount` появляется на строке 1, используется на строке 7.

**Пример — имени нет, обращение по адресу:**

```true
^ step:5
=> stop
```

**Если имя не создано:**

- Обращение по имени вернёт **литерал** (текст), не значение.
- Обращение по адресу (`^ step:N`) — работает всегда.

## Метки и переходы

```true
= label
^ label
^ step:N
```

- `= label` — именование пустой строки (метка).
- `^ label` — go-to по метке.
- `^ step:N` — go-to по номеру строки.

## Условия

```true
условие ? операция
```

- Слева от `?` — условие; справа — **любая** существующая операция.
- Поддерживаются `==`, `!=`, `<`, `>`, `<=`, `>=`, `&&`, `||`, `!`.
- Сравнение: если обе стороны парсятся как числа — числовое; иначе — лексикографическое.
- Если справа от `?` нет операции — `?` остаётся текстом.
- В строке — **ровно один** `?`. Внутри условия `&&`/`||`/`!` разрешены.

**Примеры:**

```true
a < max ? ^ do
a == b ? ^ step:8
a != b ? => stop
```

## Копирование и перемещение

- `a => b` — **копирование.** Значение `a` передаётся в `b`, `a` сохраняется.
- `a -> b` — **перемещение.** Значение `a` передаётся в `b`, значение в `a` стирается; имя `a` сохраняется.

```true
a => b
a -> b
```

## Команды

```true
=> pause
=> resume
=> stop
=> reset
=> next
=> back
=> activeTab
=> activeTab, step
```

- `pause` / `resume` / `stop` — управление статусом **текущего агента**.
- `reset` — обычный шаг (перезапуск не выполняется этим действием).
- `next` / `back` — **deprecated**, ничего не делают.
- `activeTab` — переключить активную вкладку и продолжить с указанной строки.

## `math` — числовые операции

| Операция | Синтаксис |
|---|---|
| Арифметика | `a + b => math` |
| Битовые | `a & b => math`, `a << 1 => math` |
| Функции | `abs(a) => math`, `popcount(a) => math` |
| Булевы | `a == b => math` → `0/1` |
| Списки (числа) | `1, 2, 3 => math:sum`, `math:min/max/avg/product/count/sort` |

```true
a + b => math
abs(a) => math
1, 2, 3 => math:sum
```

Поддерживаемые операторы и функции:
`+ - * / // % **`, `& | ^ ~ << >>`,
`abs`, `min`, `max`, `round`, `floor`, `ceil`, `sqrt`, `pow`,
`popcount`, `clz`, `ctz`,
`== != < > <= >= && || !` (булевы → `0/1`).

**Важно:** `math` — только для вычислений. Для склейки строк используйте `$`:

```true
result$math => result
```

**Результат:** `results["math"]`, `results["math:op"]`. В `variables` — только через `math => var` (цепочка).

## `data` — операции над строкой/списком

| Операция | Синтаксис | Ось по умолчанию |
|---|---|---|
| `substring` | `data, start, end => data:substring` | `x` |
| `indexOf` | `data, sub => data:indexOf` | `x` |
| `replace` | `data, old, new => data:replace` | `x` |
| `lower` / `upper` / `trim` | `data => data:lower` | `x` |
| `chars` | `data, sep => data:chars` | `x` |
| `charAt` | `data, i => data:charAt` | `x` |
| `length` | `data => data:length` | `x` |
| `reverse` | `data => data:reverse` | `x` |
| `unique` | `data => data:unique` | `x` |
| `contains` | `data, v => data:contains` | `x` |
| `slice` | `data, from, to => data:slice` | `x` |
| `join` | `data, axis => data:join` | `y` |
| `sort` | `data:y => data:sort` | `y` |
| `push` | `data:y, v => data:push` | `y` |
| `pop` | `data:y => data:pop` | `y` |
| `insert` | `data:y, i, v => data:insert` | `y` |
| `remove` | `data:y, i => data:remove` | `y` |
| `swap` | `data:y, i, j => data:swap` | `y` |
| `set` | `data:y, i, v => data:set` | `y` |
| `deleteRange` | `data:y, from, to => data:deleteRange` | `y` |
| `replaceRange` | `data:y, from, to, v => data:replaceRange` | `y` |
| `filter` | `data, cond => filter` | `y` |
| `matches` | `data, regex => matches` | `x` |

```true
data => data:length
data, 2 => data:charAt
data:y, 3 => data:push
```

**Ось — всегда первая (после `data`):** `data:y, v => data:push`. Если ось не указана — `x` для строковых, `y` для списочных.

**Важно:** `substring`, `indexOf`, `replace`, `lower`, `upper`, `trim`, `chars`, `charAt` — работают **только с осью `x`**.

**Результат:** `results["<op>"]`. В `variables` — **не пишется**. Для `variables` — `$`-мост.

### `<name>:splitter` — сплиттеры для осей любой строки

У **любой** строки (по имени) можно установить свои сплиттеры для осей:

```true
. => raw:splitter:l:;
```

- `<name>` — имя переменной (`raw`, `csv`, `line`), не `data`.
- `<axis>` — `l`, `x`, `y`, `z`.
- `<sep>` — разделитель (строка, без пробелов).

**Пример:**

```true
input.csv => file:data
"" = empty
empty$file = raw
. => raw:splitter:l:;
```

`raw` — имя переменной. `. => raw:splitter:l:;` — «у `raw` разделитель оси `l` — `;`».

**По умолчанию:** `l` — `\n`, `x` — пусто, `y` — `,`, `z` — `;`.

**`<name>:splitter` — не системная функция.** Это настройка переменной. `data:push` — функция (пишет в `results["push"]`), `<name>:splitter` — настройка (пишет в `splitters[name][axis]`).

**`data` — не имя.** `data` — системная функция. Для `splitter` — только имя переменной.

## `json` — работа с JSON

| Операция | Синтаксис |
|---|---|
| `get` | `json, key => json:get` |
| `set` | `json, key, value => json:set` |
| `names` | `json => json:names` |
| `values` | `json => json:values` |

```true
json, key => json:get
json, key, value => json:set
```

- `key` — `latitude` или `current_weather:temperature`.
- Массив — `items:0`, `items:1`.
- `json:get` — `results['json']`.

## `file` — работа с файлами

| Операция | Синтаксис |
|---|---|
| `file:data` | `path => file:data` / `path, content => file:data` |
| `file:origin` | `path => file:origin` / `path, content => file:origin` |
| `file:lines` | `path => file:lines` / `path, N, text => file:lines` |
| `file:create` | `path, content => file:create` |
| `file:read` | `path => file:read` |
| `file:update` | `path, content => file:update` |
| `file:delete` | `path => file:delete` |
| `file:list` | `path => file:list` |
| `file:exists` | `path => file:exists` |

```true
input.txt => file:data
```

- Результат чтения — в `results['file']`.
- Результат записи — `1` / `-1`.

## `console`, `run`

```true
command => console
path, flags => run
```

- `console` — выполнить команду ОС.
- `run` — запустить программу.
- Результат — `results['console']` / `results['run']`.

## `api` — сеть

| Операция | Синтаксис |
|---|---|
| `api:get` | `url, query => api:get` |
| `api:post` | `url, data => api:post` |
| `api:put` | `url, data => api:put` |
| `api:delete` | `url => api:delete` |
| `api:patch` | `url, data => api:patch` |
| `api:websocket` | `url => api:websocket` |

```true
https://api.example.com/health => api:get
```

- `api:get` — `results['api']`.
- `api:post/put/delete/patch` — `1` / `-1`.

## `save` / `load` — универсальные I/O

```true
file_path, data  => save
file_path, query => load
```

- `save` — запись в файл или POST (`1` / `-1`).
- `load` — чтение файла или GET (`results['load']`).

## `foreach`

```true
data:foreach:axis1, axis2 ; var1, var2 :
instruction1,
instruction2,
.
```

- Несколько осей — вложенные циклы (до 3).
- Имена переменных — по одному на ось.
- **Одна инструкция на строку**, `,` в конце.
- `.` — конец блока.

**Пример:**

```true
1, 2, 3 = arr
0 = sum
arr:foreach:y ; n :
sum + n => math => sum,
.
=> stop
```

## Функции

```true
source:function:
<инструкции>
:parameters
<инструкции>
:return_value
```

- `:parameters` — необязательный маркер.
- `:return_value` — конец функции.
- `parameters` доступна в теле.
- `=> return` внутри функции — ошибка.

Вызов:

```true
data => .:source:function
data => source:function
```

## Рекурсия

```true
math:fact:
parameters == 0 ? 1 => return
stack, parameters => data:push
parameters - 1 => .:math:fact => sub
stack => data:pop => parameters
parameters * sub => math => return
:return

5 => .:math:fact => result
=> stop
```

## Вкладки

```true
data => tab
data => tab!
data => activeTab
```

## Агенты

Программа **всегда** исполняется агентами. Агент — независимый исполнитель:
своя корутина, свой цвет (радуга, максимум 7), свой файл, свой статус.

**Инвариант:** один агент на файл.

**Главный за кадром** — интерпретатор. `Substrate` — только UI-стейт.

**Каждый агент виден одновременно:** `Cycles` (лента), `FileChips` (точка), `Log` (префикс `[agent-name]`, полоса 2dp).

### Свойства агента

```true
agent:<name>:currentFile => <dst>
agent:<name>:currentStep => <dst>
agent:<name>:status      => <dst>
agent:status             => <dst>
```

### Управление агентом

```true
agent:<name> => stop
agent:<name> => pause
agent:<name> => resume
agent:<name> => delete
agent:<name> -> nothing
```

### Перемещение и копирование

```true
agent:<name> -> file:<target>
agent:<name> => file:<target>
```

### Уничтожение (единый сток `nothing`)

```true
agent:<name> -> nothing
file:<name>  -> nothing
<variable>   -> nothing
```

- **Каскад:** `file:x -> nothing` → сначала `agent:x -> nothing`, потом файл.
- **Подсветка:** `-> nothing` — фиолетовый фон + underline + цвет `#9C27B0`.
- **Лог:** `type = "destroy"`, `🗑 Destroyed <entity>`.

### Пауза себя

```true
<ms> => agent:wait
```

### Короткие формы (внутри агента)

```true
=> stop
=> pause
=> resume
```

### Пример: агент удаляет переменную

```true
a = 5
a -> nothing
```

### Пример: агент копирует себя на другой файл

```true
agent:main => file:other
```

### Лента Cycles

Все открытые файлы — **горизонтальная лента вертикальных колонок**. Каждая
колонка: агент сверху (цвет, имя, `▶⏸⏹⏭⏮`, `⟲`, `⋯`, `⛶`), редактор снизу.

- **`▶ Start All` / `⏹ Stop All`** — глобальные кнопки.
- **`⟲`** — Reset **этого файла**.
- **`⛶`** — раскрыть файл на всю `Cycles`.
- **`⋯`** — `Templates…`, `Copy to…`, `Move to…`, `Delete agent`, `Rename file…`, `Delete file`.
- **Ширина колонки** — по содержимому, `clamp(320..480)dp`.
- **Активная колонка** — рамка `ACCENT` 2dp.

### Навигация

Клик по файлу:
- Свободен → переход сразу.
- Агент `PAUSED/STOPPED/FINISHED` → переход сразу.
- Агент `RUNNING` → диалог `Ждать / Вытеснить / Остаться / Ошибка`.

## Ошибки

- **Валидация:** неизвестная метка, `Multiple ?`, `=> return inside function`.
- **Strict Mode:** запрет `:` функций; разрешены `:parameters`, `:return`, оси, `:currentFile`, `:currentStep`, `:status`, `:wait`, `:nothing`.
- **Выполнение:** запись в константу, выход за диапазон → `PAUSED`.
- `=> next` / `=> back` — no-op.
- **Агенты:** `occupied` → `error` (`PAUSED`).
- `nothing = ...` → `error: 'nothing' is reserved`.
- `agent:<name> => currentFile` → не парсится (свойство через `:`).

## Версионирование

- Язык: **0.8.0**.
- Интерпретатор: **1.0.0**.
- `PATCH` — исправления. `MINOR` — новые возможности. `MAJOR` — ломающие.

## Что нового в 0.8.0

- **Агенты.** 1 агент на файл. Радуга (7).
- `agent:<name> => stop/pause/resume/delete`.
- `agent:<name> -> file:<target>` — move, `=> file:<target>` — copy.
- `agent:<name> -> nothing` — удаление.
- `file:<name> -> nothing` — каскад.
- `<variable> -> nothing` — удаление + строки в коде.
- `agent:<name>:prop => <dst>`.
- `agent:status => <dst>`.
- `<ms> => agent:wait`.
- `Cycles` — лента колонок.
- `Start All` / `Stop All`.
- `Reset` — у файла.
- `⛶` — раскрытие.
- Подсветка `-> nothing`.
- `Log` — фильтр + полоса агента.
- **Принцип 15** — «работаем с тем, что есть».
- **`<name>:splitter`** — сплиттер оси для любой строки.

## Что нового в 0.7.0

- `data:join`.
- `file:data`, `file:origin`, `file:lines`.
- `=> next` / `=> back` — deprecated.
- Устранение утечки WebSocket.
- Принцип 12.

## Что нового в 0.6.1

- Пометки: `data:substring`/`indexOf`/`replace`/`lower`/`upper`/`trim`/`chars`/`charAt` — только `x`.
- `math` — только для вычислений; для склейки — `$`.
- `json:names`/`values` при корне-массиве — пустой список.
- `foreach` — только в `validate`.

## Что нового в 0.6.0

- `math` / `data` разделение.
- `foreach` с осями, переменными, блоком.
- `.:function`, `:return`.
- `filter`, `matches`.
- `data:set`.
- `api:websocket`.
- `json:get`, `json:set`, `json:names`, `json:values`.
- Один `?`.
- `:parameters`.