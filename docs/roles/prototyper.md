# Роль: Экспериментатор / прототипист

## Задача 1: Быстрый API

### Постановка
Быстро проверить API-эндпоинт без кода.

### true
url => :api:get => data

### Python
import requests
data = requests.get(url).text

### Использование интерпретатора
1. Вставить код в `main.cycle`.
2. `▶` — запуск.
3. Log — результат.
4. Визуализация: `rect_N` для `data`.
5. Ошибка: `:error:line` → красная обводка.

---

## Задача 2: JSON

### true
raw, /key => :json:get

### Python
import json
value = json.loads(raw)['key']

### Использование
1. `:json:get` → `results['json']`.
2. Log — значение.
3. Visual — `rect_N` для `json`.

---

## Задача 3: Плагин

### true
args => :ext:my_plugin

### Kotlin
Extensions.register(MyExtension())

### Использование
1. `Extensions.register` — при старте.
2. `args => :ext:my_plugin`.
3. `results['ext:my_plugin']` — результат.
4. `:ext:list` — список.

---

## Задача 4: Визуализация

### true
10, 20 => rect_1.area

### Python
import matplotlib.pyplot as plt
plt.scatter(10, 20)

### Использование
1. `rect_1.area` — позиция.
2. Visual — перетаскивание.
3. `:background` — фон.
4. Table — все `rect_N`.

---

## Задача 5: Retry

### true
url, data => :api:post:retry, 3, 1000

### Python
for i in range(3):
try:
r = requests.post(url, json=data)
if r.status_code < 500: break
except: pass
time.sleep(1)

### Использование
1. `:api:post:retry` — 3 попытки, 1s.
2. `results['api:post']` = 1/-1.
3. Log — попытки.

---

## Задача 6: Mock для прототипа

### true
url, response => :api:mock
url => :api:get => data
data => :message
:api:mock:reset

### Python (responses)
import responses
@responses.activate
def test():
responses.add(responses.GET, url, body='ok', status=200)
assert requests.get(url).text == 'ok'

### Использование
1. `:api:mock` — замокать URL.
2. `:api:get` — вернёт mock.
3. `:message` — вывод.
4. `:api:mock:reset` — сброс.