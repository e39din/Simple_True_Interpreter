# Язык Simple (true) — 0.13.0

## §0. Принципы языка True

1. Нет типов, есть разное восприятие данных
2. Все есть инструкция
3. Все есть строка (подставляем `Result:$a` или `Result: a`, экранируем `\=>`, комментируем `\` — if you really need it ;))
4. Все есть список/массив
5. Все есть контейнер с данными
6. Все есть математическое выражение
7. Все есть геометрическая фигура (с осями `l` — строки `\n`, `x` — символы, `y` — через `,`, `z` — через `;`; `list:l:1 = firstLine`)
8. Все есть условие (`a < 12 ? do something`)
9. Если что-то есть — то есть истина (`1` / True)
10. То что есть можем использовать (даже без названия)
11. То что есть можем называть разными именами (`any = name, surname, label`)
12. Программу выполняет агент
13. Разработчик может управлять агентом
14. Агент является частью программы
15. Агент может менять код программы
16. Агент может перемещаться по циклу вверх-вниз (`:step`, `:stepBack`)
17. Агент может переместиться в любое место (`^ 1`, `^ label`, `^ any.cycle`)
18. Агент может открывать/закрывать/изменять другие программы
19. Агент может совершать действия в UI и терминале как пользователь (`:simple:*`, `:terminal`)
20. Программа — то цикл
21. Программа — то switch/when
22. Программа — то набор инструкций
23. Программа — то функция
24. Код программы — источник данных (программа как БД)
25. Данные изменились — данные в логе обновились
26. Если можно сделать просто — делаем просто
27. Счет начинается с 1-го (длина списка и номер последнего элемента совпадают)
28. Работаем с тем что есть (не плодим новых сущностей)
29. С данными можно работать просто
30. Перемещаем одной операцией `->`, копируем другой `=>`
31. Алгоритмические задачи можно решать простым языком
32. Цикл (`.cycle`) исполняется в своем потоке
33. Агент (`.agent`) работает на потоке цикла
34. Папка есть проект (используем то что есть)
35. Файлы в папке есть файлы проекта
36. Скрытое заставляет додумывать чем порождает ошибки
37. Наглядность дает понимание истины (True)

### §0.1. Философия (дополнение)

- **Наглядность → меньше ошибок:** чем нагляднее код и процесс, тем меньше додумывания, ошибок, больше производительности и настроения.

---

## §0.2. Примеры: от простого к игре

### Пример 1: Hello

hello => :message

### Пример 2: Счётчик

0 = a
= loop
a + 1 => :math => a
a => :message
a < 5 ? ^ loop
Готово! => :message
=> :stop

### Пример 3: Калькулятор (`:ask`)

Первое число: => :ask => a
Второе число: => :ask => b
a + b => :math => result
Сумма: => :message
result => :message
=> :stop

### Пример 4: Игра «Пещера» (см. SAMPLES.md §16)

100 = hp
0 = gold
факел = item
3 = potions

Ты в пещере. Выход справа. Слева — дракон. => :message
hp = 100, gold = 0, item = факел, potions = 3 => :message

= turn
Что делаешь? (1 — идти, 2 — драться, 3 — лечиться, 4 — выход) => :ask

= wait
:ask:pending == 1 ? ^ wait
:ask => choice

choice == 1 ? ^ go
choice == 2 ? ^ fight
choice == 3 ? ^ heal
choice == 4 ? ^ exit
Неизвестный выбор. Попробуй снова. => :message
^ turn

= go
Ты идёшь вглубь пещеры... => :message
gold + 10 => :math => gold
Нашёл 10 золота! => :message
gold => :message
^ turn

= fight
Дракон атакует! => :message
hp - 30 => :math => hp
hp > 0 ? Ты жив! => :message
hp > 0 ? hp => :message
hp <= 0 ? ^ dead
^ turn

= heal
potions > 0 ? Пьёшь зелье... => :message
potions > 0 ? potions - 1 => :math => potions
potions > 0 ? hp + 20 => :math => hp
potions > 0 ? hp => :message
potions > 0 ? potions => :message
potions == 0 ? Зелья кончились! => :message
^ turn

= exit
Ты выходишь из пещеры! => :message
gold => :message
hp => :message
item => :message

item -> dragon_item
Факел отдан дракону. => :message
item => :message
dragon_item => :message

^ ending.cycle

= dead
Ты погиб... => :message
=> :stop

---

## §0.3. Шаблоны обучения

При первом запуске — диалог «Выбери шаблон» (5 шаблонов):

| # | Шаблон | Что | Уровень |
|---|---|---|---|
| 1 | Hello | `hello => :message` | Новичок |
| 2 | Счётчик | Цикл с goto | Новичок |
| 3 | Калькулятор | `:ask` + `:math` | Средний |
| 4 | Пещера | Игра (`:ask`, goto, move) | Средний |
| 5 | Плагин | `:ext` | Продвинутый |

Шаблоны — в `templates/*.cycle`; открываются в новой вкладке; не перезаписывают `main.cycle`.

---

## 1. Базовые элементы

| Элемент | Синтаксис | Пример |
|---|---|---|
| Контейнер (переменная) | `value = name` | `5 = a` |
| Синонимы | `value = name1, name2, name3` | `12 = twelve, number, a` |
| Скрытый контейнер | `value = name *` | `5 = a *` |
| Константа | `value = name @` | `5 = a @` |
| Пустой контейнер | `= name` | `= a` |
| Результат | `expr => :op` | `a + 1 => :math` |
| Именование результата | `:op => name` | `:math => b` |
| Метка | `= label` | `= loop` |
| Переход | `^ target` | `^ loop`, `^ 5`, `^ any.cycle` |
| Условие | `cond ? action` | `a < 5 ? ^ loop` |
| Комментарий | `\ text` | `\ это комментарий` |
| Экранирование | `\=>` | `\=> не операция` |
| Команда | `=> :stop` | `=> :pause` |

### Условие `?`

`?` — условный оператор **только** если:
- Слева — валидное условие (`a == b`, `a < 5`, `a`, `1`).
- Справа — валидная операция (`^`, `=>`, `:`, `=`, `->`).

Иначе `?` — символ строки: `Что делаешь? (1, 2, 3)` — не условие.

### Запятые

`,` — разделитель **только** в аргументах функций (`:math`, `:data`, `:json`, ...).
В `:message` и `NAMING` — `,` — часть строки: `hp = 100, gold = 0` — ок.

### Синонимы

12 = twelve, number, a

- Первое имя — canonical; остальные — alias.
- Обращение по любому имени — одно значение.
- `^ twelve`, `^ number`, `^ a` — переход к метке по любому имени.
- Если не используешь — не давай (принцип простоты).

### Кавычки

- **Не нужны** для простых строк: `hello = a`.
- **Не нужны** для строк с `,`/`?`/`=>`: `hp = 100, gold = 0`, `Что делаешь? (1, 2)`.
- `:assert` — единственное исключение: `a == b, 'msg' => :assert`.

---

## 2. Функции ядра

### 2.1. `:math` — арифметика

a + b => :math
a * b => :math
a ** 2 => :math
abs(a) => :math
1, 2, 3 => :math:sum

### 2.2. `:data` — строки, списки, multi-axis

data => :data:size
data => :data:upper
data:y => :data:sort
data:y, value => :data:push
data => :data:empty
data, key => :data:groupby
data, key, sum => :data:groupby

Оси: `x` (символы), `y` (через запятую), `z` (через `;`), `l` (строки).

### 2.3. `:json` / `:xml`

json, key => :json:get
json, key, value => :json:set
json => :json:names
json => :json:values

xml, /root/item => :xml:get
xml => :xml:names
xml => :xml:values

### 2.4. `:api` — HTTP

url, data => :api:post
url => :api:get
url, data => :api:put
url => :api:delete
url, data => :api:patch
url => :api:websocket

### 2.5. Retry

url, data => :api:post:retry
url, data, 5 => :api:post:retry
url, data, 5, 2000 => :api:post:retry
url, data, 5, 2000, 1 => :api:post:retry
url => :api:get:retry

Повторяются только transient-ошибки: 5xx, timeout, IOException. POST — только с `idempotent=1`.

### 2.6. Mock

url, response => :api:mock
url, response, 200 => :api:mock
:api:mock:reset
:api:mock:list => dst

### 2.7. `:file` / Streams

path => :file:read
path, content => :file:create
path => :file:exists
path => :file:list

path => :file:stream => dst
:stream:next => dst
:stream:seek name, pos
:stream:close
:stream:list => dst
data => :data:empty

### 2.8. `:terminal` — PTY

command => :terminal => Console
text, Console => :terminal:write
Console => :terminal:read
Console => :terminal:close
:terminal:list => dst

`:console` — deprecated, алиас `:terminal`.

### 2.9. `:open` — ОС

https://example.com => :open:url
/path/to/file => :open:file
/path/to/folder => :open:folder

### 2.10. `:secret` — AES-GCM

key => :secret:get
key, value => :secret:set
key => :secret:remove
:secret:list => dst

### 2.11. `:send`

chat_id, message => :send:telegram
chat_id, message => :send:max
to, subject, body => :send:email

Токены — только через `:secret:get` (не хардкод).

### 2.12. `:schedule` — cron

*/5 * * * *, action_var => :schedule
0 9 * * 1-5, morning, Europe/Moscow => :schedule
:schedule:list => dst
:schedule:stop
:schedule:stop => 1
10 => :schedule:limit

### 2.13. `:sound`

path => :sound:play
:sound:stop
path => :sound:stop

### 2.14. `:websocket`

8080 => :websocket:listen
message => :websocket:send
:websocket:message => dst
:websocket:clients => dst
:websocket:close

### 2.15. `:mem` — память (asm/C-стиль)

16 => :mem:alloc => p
p, 42 => :mem:write
p => :mem:read => v
@1000
p, 0, 16 => :mem:fill
p, 4 => :mem:dump
p => :mem:free

### 2.16. `:background` — фон визуализации

/path/to/bg.png => :background
/path/to/bg.png, tile => :background
/path/to/bg.png, stretch => :background
/path/to/bg.png, center => :background
/path/to/bg.png, fit => :background
:background:reset

### 2.17. `:ask` — диалог с вопросом

question => :ask
question => :ask => dst
question, default => :ask
question, default, number => :ask
question, default, choice:1,2,3,4 => :ask

**Асинхронный:** `results['ask:pending'] = '1'` (пока нет ответа); агент продолжает.
После ответа — `results['ask'] = value`, `results['ask:pending'] = '0'`.

Ожидание:

= wait
:ask:pending == 1 ? ^ wait
:ask => choice

### 2.18. `:agent` — inbox

message => :agent:broadcast
agent-1, message => :agent:send
:agent:inbox => dst
:agent:inbox:clear
:agent:status => dst
100 => :agent:wait

### 2.19. `:simple` — UI-автоматизация

button:startAll => :simple:click
tab:main.cycle => :simple:click
agent:agent-1:play => :simple:click
100, 200 => :simple:clickAt
hello, editor:current => :simple:type
Console => :simple:openTerminal
ls -la, Console => :simple:sendTerminal
hello => :simple:search

### 2.20. `:error` / `:assert`

:error => dst
0 => :error:resolve
a == b, 'msg' => :assert
1 => :error:pause
0 => :error:pause
1 => :assert:pause
0 => :assert:pause

### 2.21. `:pinned` — закреплённые контейнеры

a => :pinned:add
b => :pinned:add
a => :pinned:remove
:pinned:list => dst
:pinned:clear

Закрепляет столбец контейнера в панели «Pinned»; сохраняется в `settings.true`.

### 2.22. `:ext` — плагины

args => :ext:name
args => :ext:name => dst
:ext:list => dst

---

## 3. Сравнение языков

| Задача | Simple | ASM | C | Python | Kotlin |
|---|---|---|---|---|---|
| Сложить 2 числа | `a + b => :math` | `mov eax, a; add eax, b` | `int c = a + b;` | `c = a + b` | `val c = a + b` |
| Цикл 5 раз | `= loop`<br>`a + 1 => :math => a`<br>`a < 5 ? ^ loop` | `mov ecx, 5; loop:` | `for (i=0;i<5;i++)` | `for i in range(5):` | `repeat(5) { }` |
| Прочитать файл | `input.txt => :file:read` | `open/read/close` | `fopen/fread/fclose` | `open().read()` | `File().readText()` |
| HTTP POST | `url, data => :api:post` | — | `libcurl` | `requests.post()` | `HttpClient` |
| Память | `16 => :mem:alloc => p` | `malloc` | `malloc` | — | `ByteArray` |
| Визуализация | `10, 20 => rect_1.area` | — | — | — | Compose |

---

## 4. StrictMode

В StrictMode запрещены системные функции: `:mem`, `:terminal`, `:send`, `:schedule`, `:sound`, `:websocket`, `:stream`, `:data:empty`, `:background`, `:rect`, `:open`, `:xml`, `:ask`, `:pinned`.
`:secret` — **разрешён** (токены для API).
`:ext` — **разрешён** (плагины).

---

## 5. Потоки

- Поток = столбец (`.cycle`).
- Агент работает на потоке цикла.
- Несколько агентов в одном столбце — последовательно (один поток).
- Агенты в разных столбцах — разные потоки.
- `switchAgentFile` — отмена Job старого столбца → новый Job.

---

## 6. Ошибки

- По умолчанию — **не пауза**: агент продолжает.
- `:error:pause` / `:assert:pause` — флаг: `1` — пауза, `0` — не пауза (default).
- `occupied` (столкновение) — **всегда** без паузы.
- `:pause` / `:stop` — явные команды.

---

## 7. Файлы

| Файл | Что |
|---|---|
| `settings.true` | Настройки (скорость, strictMode, fileOrder, recentProjects, pinned) |
| `progress.true` | Прогресс (completedSamples) |
| `start.cycle` | Стартовый код |
| `templates/*.cycle` | Шаблоны обучения |
| `*.inbox` | Inbox агентов |