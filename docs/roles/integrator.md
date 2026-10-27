# Роль: Интегратор

## Задача 1: REST → XML

### Постановка
Получить XML по HTTP, извлечь значение.

### true
url => :api:get => data
data, /root/item => :xml:get

### Python
import requests
import xml.etree.ElementTree as ET
data = requests.get(url).text
root = ET.fromstring(data)
value = root.find('item').text

### Использование интерпретатора
1. Вставить код в `main.cycle`.
2. `▶` — запуск.
3. `:xml:get` → `results['xml']`.
4. Log — значение.
5. Ошибка: `:error:line` → красная обводка.

---

## Задача 2: JSON → API

### true
json => :file:read => raw
raw, /key => :json:get => value
url, value => :api:post

### Python
import json, requests
raw = open('json').read()
value = json.loads(raw)['key']
requests.post(url, json={'value': value})

### Использование
1. `:file:read` → `raw`.
2. `:json:get` → `value`.
3. `:api:post` — отправка.
4. Log — результат.

---

## Задача 3: WebSocket → JSON

### true
8080 => :websocket:listen
:websocket:message => dst
dst, /key => :json:get

### Python
import asyncio, websockets, json
async def handler(ws):
msg = await ws.recv()
value = json.loads(msg)['key']

### Использование
1. `:websocket:listen` — сервер.
2. `:websocket:message` → `dst`.
3. `:json:get` — парсинг.
4. Log — результат.

---

## Задача 4: Email → API

### true
to, 'Subject', body => :send:email

### Python
import smtplib
smtp = smtplib.SMTP(host, port)
smtp.sendmail(user, [to], f'Subject: Subject\n\n{body}')

### Использование
1. `:secret:set` — SMTP-настройки.
2. `:send:email` — отправка.
3. Log — результат.

---

## Задача 5: Плагин

### true
args => :ext:my_integration

### Kotlin
object MyIntegration : Extension {
override val names = setOf("my_integration")
override fun handle(op: String, args: List<String>, ctx: Ctx): String? =
callExternal(args)
}
Extensions.register(MyIntegration())

### Использование
1. `Extensions.register` — при старте.
2. `args => :ext:my_integration`.
3. `results['ext:my_integration']` — результат.
4. `:ext:list` — список.

---

## Задача 6: Retry при интеграции

### true
url, data => :api:post:retry, 5, 2000, 1
code == 1 ? :json:get => result

### Python
import requests, time
for i in range(5):
try:
r = requests.post(url, json=data)
if r.status_code < 500:
result = r.json()
break
except: pass
time.sleep(2)

### Использование
1. `:api:post:retry` — 5 попыток, 2s, idempotent.
2. `code == 1` — проверка.
3. `:json:get` → `result`.
4. Log — попытки.