# Функции Simple (true) — 0.13.0

## Ядро

| Функция | Аргументы | Результат | Описание |
|---|---|---|---|
| `:math` | expr | `results['math']` | Арифметика |
| `:math:sum/min/max/avg/product/count/sort` | list | `results['math:op']` | Агрегация |
| `:sum/min/max/avg/product/count/sort` | list | `results['op']` | Короткие |
| `:data:*` | data, args | `results['op']` | Строки/списки |
| `:data:empty` | data | `results['empty']` = 1/0 | Пустота |
| `:data:groupby` | data, key[, agg] | `results['groupby']` | Группировка |
| `:json:get/set/names/values` | json, args | `results['json']` | JSON |
| `:xml:get/names/values` | xml, args | `results['xml']` | XML (XXE-safe) |
| `:api:get/post/put/delete/patch` | url, data | `results['api:op']` = 1/-1 | HTTP |
| `:api:websocket` | url | — | WS-клиент |
| `:api:*:retry` | url, data[, count[, delay[, idem]]] | 1/-1 | Retry |
| `:api:mock` | url, response[, code] | 1/-1 | Mock |
| `:api:mock:reset` | — | 1 | Сброс моков |
| `:api:mock:list` | — | `results['api:mock:list']` | Список моков |
| `:file:create/read/update/delete/list/exists/data/origin/lines` | path, content | `results['file']` | Файлы |
| `:file:stream` | path[, charset] | dst = 1/0/'', `results['stream']` | Поток |
| `:file:lines:stream` | path[, charset] | dst = 1/0/'', `results['stream']` | Поток строк |
| `:stream:next/close/reset/limit/list/seek` | args | `results['stream:*']` | Управление потоком |
| `:terminal` | cmd[, title] | `results['terminal']` | PTY-терминал |
| `:terminal:write/read/close/list` | args | `results['terminal:*']` | Управление |
| `:open:url/file/folder` | path | `results['open']` | ОС |
| `:secret:get/set/remove/list` | key, value | `results['secret']` | AES-GCM |
| `:send:telegram/max/email` | args | `results['send']` = 1/-1 | Уведомления |
| `:schedule` | cron, action[, tz] | `results['schedule']` = 1/-1 | cron |
| `:schedule:list/stop/limit` | args | `results['schedule:*']` | Управление |
| `:sound:play/stop` | path | `results['sound']` = 1/-1 | WAV/MP3 |
| `:websocket:listen/send/message/clients/close` | args | `results['websocket:*']` | WS-сервер |
| `:mem:alloc/free/write/read/copy/move/fill/size/dump/reset` | args | `results['mem:*']` | Память |
| `:background` | path[, mode] | `results['background']` = 1/-1 | Фон |
| `:background:reset` | — | `results['background']` = 1 | Сброс фона |
| `:ask` | question[, default[, type]] | `results['ask']`, `results['ask:pending']` | Диалог |
| `:agent:broadcast/send/inbox/inbox:clear/status/wait` | args | `results['agent:*']` | Агенты |
| `:simple:*` | args | `results['simple']` | UI |
| `:error` / `:error:resolve` | — / 0 | `results['error']` | Ошибки |
| `:error:pause` / `:assert:pause` | 1/0 | `results['error:pause']` / `results['assert:pause']` | Флаги паузы |
| `:assert` | cond, msg | `results['assert']` = 1/0 | Проверка |
| `:pinned:add/remove/list/clear` | name | `results['pinned']` = 1/-1 | Закреплённые |
| `:ext:name` | args | `results['ext:name']` | Плагины |
| `:ext:list` | — | `results['ext:list']` | Список плагинов |
| `:run` | — | — | Выполнить инструкцию |
| `:log` | — | — | Лог |
| `:message` | data | `results['message']` | Сообщение |
| `:result` | data | `results['result']` | Результат |
| `:activeTab` | idx[, step] | — | Переключить вкладку |
| `:pauseAll` / `:resumeAll` | — | — | Пауза/резюме всех |

---

## `:data:*` — подробно

| Функция | Аргументы | Результат |
|---|---|---|
| `:data:size` | data[, axis] | `results['size']` |
| `:data:empty` | data | `results['empty']` = 1/0 |
| `:data:substring` | data, start, end | `results['substring']` |
| `:data:indexOf` | data, sub | `results['indexOf']` |
| `:data:replace` | data, old, new | `results['replace']` |
| `:data:lower` / `:data:upper` / `:data:trim` | data | `results['lower']` / `results['upper']` / `results['trim']` |
| `:data:chars` | data, sep | `results['chars']` |
| `:data:charAt` | data, i | `results['charAt']` |
| `:data:reverse` | data | `results['reverse']` |
| `:data:unique` | data | `results['unique']` |
| `:data:contains` | data, v | `results['contains']` = 1/0 |
| `:data:slice` | data, from, to | `results['slice']` |
| `:data:sort` | data | `results['sort']` |
| `:data:join` | data, sep | `results['join']` |
| `:data:push` | data, value | `results['push']` |
| `:data:pop` | data | `results['pop']`, `results['pop_rest']` |
| `:data:remove` | data, i | `results['remove']`, `results['remove_rest']` |
| `:data:swap` | data, i, j | `results['swap']` |
| `:data:deleteRange` | data, from, to | `results['deleteRange']` |
| `:data:replaceRange` | data, from, to, value | `results['replaceRange']` |
| `:data:peek` | data | `results['peek']` |
| `:data:groupby` | data, key[, count/sum/min/max/avg] | `results['groupby']` |

---

## `:math:*` — подробно

| Функция | Аргументы | Результат |
|---|---|---|
| `:math` | expr | `results['math']` |
| `:math:sum` | list | `results['math:sum']` |
| `:math:min` | list | `results['math:min']` |
| `:math:max` | list | `results['math:max']` |
| `:math:avg` | list | `results['math:avg']` |
| `:math:product` | list | `results['math:product']` |
| `:math:count` | list | `results['math:count']` |
| `:math:sort` | list | `results['math:sort']` |
| `:sum` / `:min` / `:max` / `:avg` / `:product` / `:count` / `:sort` | list | `results['op']` |

---

## `:mem:*` — подробно

| Функция | Аргументы | Результат |
|---|---|---|
| `:mem:alloc` | size | `results['mem:alloc']` = адрес |
| `:mem:free` | ptr | `results['mem:free']` = 1/-1 |
| `:mem:write` | ptr, value | `results['mem:write']` = 1/-1 |
| `:mem:read` | ptr | `results['mem:read']` = значение |
| `:mem:copy` | src, dst, len | `results['mem:copy']` = 1/-1 |
| `:mem:move` | src, dst, len | `results['mem:move']` = 1/-1 |
| `:mem:fill` | ptr, value, len | `results['mem:fill']` = 1/-1 |
| `:mem:size` | ptr | `results['mem:size']` = размер |
| `:mem:dump` | ptr, len | `results['mem:dump']` = hex |
| `:mem:reset` | — | `results['mem:reset']` = 1 |

---

## `:agent:*` — подробно

| Функция | Аргументы | Результат |
|---|---|---|
| `:agent:broadcast` | message | `results['agent:broadcast']` = 1 |
| `:agent:send` | name, message | `results['agent:send']` = 1/-1 |
| `:agent:inbox` | — | `results['agent:inbox']` = содержимое |
| `:agent:inbox:clear` | — | `results['agent:inbox:clear']` = 1 |
| `:agent:status` | — | `results['agent:status']` = name:status,... |
| `:agent:wait` | ms | — (пауза) |
| `:agent:wait:timeout` | ms, timeout | — (пауза) |

---

## `:simple:*` — подробно

| Функция | Аргументы |
|---|---|
| `:simple:click` | button:name / tab:name / agent:name:action / file:name:action / menu:name:item |
| `:simple:clickAt` | x, y |
| `:simple:type` | text, target[, delay] |
| `:simple:openTerminal` | title |
| `:simple:sendTerminal` | command, title |
| `:simple:typeTerminal` | text, title[, delay] |
| `:simple:closeTerminal` | title |
| `:simple:search` | query |
| `:simple:search:next` / `:simple:search:prev` / `:simple:search:clear` | — |

---

## `:error:*` — подробно

| Функция | Аргументы | Результат |
|---|---|---|
| `:error` | — | `results['error']` = 1 |
| `:error:line` | — | `results['error:line']` |
| `:error:message` | — | `results['error:message']` |
| `:error:lines` | — | `results['error:lines']` |
| `:error:messages` | — | `results['error:messages']` |
| `:error:resolve` | 0/1 | сброс |
| `:error:pause` | 0/1 | `results['error:pause']` |
| `:assert:pause` | 0/1 | `results['assert:pause']` |

---

## `:pinned:*` — подробно

| Функция | Аргументы | Результат |
|---|---|---|
| `:pinned:add` | name | `results['pinned']` = 1 |
| `:pinned:remove` | name | `results['pinned']` = 1 |
| `:pinned:list` | — | `results['pinned:list']` = name1,name2 |
| `:pinned:clear` | — | `results['pinned']` = 1 |

---

## `:ext:*` — подробно

| Функция | Аргументы | Результат |
|---|---|---|
| `:ext:name` | args | `results['ext:name']` = результат плагина |
| `:ext:list` | — | `results['ext:list']` = name1,name2 |

---

## `:ask` — подробно

| Синтаксис | Результат |
|---|---|
| `question => :ask` | `results['ask']` = ответ |
| `question => :ask => dst` | `dst` = ответ |
| `question, default => :ask` | + default |
| `question, default, number => :ask` | type = number |
| `question, default, choice:1,2,3 => :ask` | type = choice |

**Асинхронный:** `results['ask:pending'] = '1'` (пока нет ответа); после — `'0'`.