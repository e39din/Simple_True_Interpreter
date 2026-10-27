# Примеры Simple (true) — 0.13.0

## 1. Hello world

hello => :message

---

## 2. Сумма списка

1, 2, 3, 4, 5 => :math:sum
:math:sum => total

---

## 3. Строки и списки

data => :data:upper
data:y => :data:sort
data:y, value => :data:push
data => :data:size

---

## 4. JSON

json, key => :json:get
json, key, value => :json:set
json => :json:names
json => :json:values

---

## 5. XML

xml, /root/item => :xml:get
xml => :xml:names
xml => :xml:values

---

## 6. HTTP + retry

url, data => :api:post:retry
url, data, 5 => :api:post:retry
url, data, 5, 2000, 1 => :api:post:retry
url => :api:get:retry

---

## 7. Mock API

url, response => :api:mock
url, response, 200 => :api:mock
:api:mock:reset
:api:mock:list => dst

---

## 8. DeepSeek → Telegram

deepseek_token => :secret:get => token
url, body => :api:post:retry, 5, 2000, 1
code == 1 ? :json:get => text
chat_id, text => :send:telegram

---

## 9. Claude → Email

claude_token => :secret:get => token
url, body => :api:post
code == 1 ? :json:get => text
to, 'Claude', text => :send:email

---

## 10. Терминал

ls -la => :terminal => Console
Console => :terminal:read
:terminal:read => output

---

## 11. WebSocket-сервер

8080 => :websocket:listen
hello => :websocket:send
:websocket:message => dst
:websocket:clients => dst
:websocket:close

---

## 12. Расписание (cron)

*/5 * * * *, action => :schedule
0 9 * * 1-5, morning, Europe/Moscow => :schedule
:schedule:list => dst
:schedule:stop
10 => :schedule:limit

---

## 13. Память (asm-стиль)

16 => :mem:alloc => p
p, 42 => :mem:write
p => :mem:read => v
@1000
p, 0, 16 => :mem:fill
p, 4 => :mem:dump
p => :mem:free

---

## 14. Визуализация

/path/to/bg.png => :background
/path/to/bg.png, tile => :background
/path/to/bg.png, stretch => :background
/path/to/bg.png, center => :background
/path/to/bg.png, fit => :background
:background:reset
10, 20 => rect_1.area

---

## 15. Плагин

args => :ext:my_plugin
args => :ext:my_plugin => dst
:ext:list => dst

---

## 16. Игра: Пещера

Простая текстовая игра. Демонстрирует: `:ask`, `:message`, `:math`, условие, goto, метки, move, `^ file.cycle`.

### `cave.cycle`

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

### `ending.cycle`

Ты вернулся домой. => :message

Победа! => :message

result = победа
result => :message

result -> final_result
result => :message
final_result => :message

=> :stop

### Как играть

1. Открыть `cave.cycle` в редакторе.
2. `▶` — запуск.
3. Появится диалог «Что делаешь?» — ввести `1`–`4`.
4. Смотреть Log — сообщения.
5. Visual — `rect_N` для `hp`, `gold`, `item`, `potions`.
6. Table — все контейнеры.
7. `⏭` — шаг; `⏮` — назад.

### Что демонстрирует

| # | Конструкция | Где |
|---|---|---|
| 1 | `:ask` | `Что делаешь? => :ask` |
| 2 | `:ask:pending` | `:ask:pending == 1 ? ^ wait` |
| 3 | `:message` | `... => :message` |
| 4 | `:math` | `gold + 10 => :math => gold` |
| 5 | Условие `?` | `hp > 0 ? ...` |
| 6 | Метка `=` | `= turn`, `= go`, ... |
| 7 | Goto `^` | `^ turn`, `^ go`, ... |
| 8 | Move `->` | `item -> dragon_item` |
| 9 | `^ ending.cycle` | Переход в другой цикл |
| 10 | `=> :stop` | Конец |

---

## 17. Калькулятор

Первое число: => :ask => a
Второе число: => :ask => b
a + b => :math => result
Сумма: => :message
result => :message
=> :stop

---

## 18. Счётчик

0 = a
= loop
a + 1 => :math => a
a => :message
a < 5 ? ^ loop
Готово! => :message
=> :stop

---

## 19. Звук

/path/to/alarm.mp3 => :sound:play
:sound:stop
/path/to/alarm.mp3 => :sound:stop

---

## 20. Открыть URL

https://example.com => :open:url
/path/to/file => :open:file
/path/to/folder => :open:folder

---

## 21. Секреты

:secret:list => dst
key => :secret:get
key, value => :secret:set
key => :secret:remove

---

## 22. Send (уведомления)

chat_id, message => :send:telegram
chat_id, message => :send:max
to, subject, body => :send:email

---

## 23. Streams

path => :file:stream => dst
:stream:next => dst
:stream:seek name, pos
:stream:close
:stream:list => dst
data => :data:empty

---

## 24. Error + Assert

a == b, 'must be equal' => :assert
:error => dst
0 => :error:resolve
1 => :error:pause
1 => :assert:pause

---

## 25. Pinned

a => :pinned:add
b => :pinned:add
a => :pinned:remove
:pinned:list => dst
:pinned:clear

---

## 26. Агенты (inbox)

message => :agent:broadcast
agent-1, message => :agent:send
:agent:inbox => dst
:agent:inbox:clear
:agent:status => dst
100 => :agent:wait

---

## 27. UI-автоматизация

button:startAll => :simple:click
tab:main.cycle => :simple:click
agent:agent-1:play => :simple:click
100, 200 => :simple:clickAt
hello, editor:current => :simple:type
Console => :simple:openTerminal
ls -la, Console => :simple:sendTerminal
hello => :simple:search