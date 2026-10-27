# Роль: Аналитик

## Задача 1: CSV → фильтр → отчёт

### Постановка
Прочитать CSV, отфильтровать строки > 100, записать результат.

### true
input.csv => :file:stream => csv
:stream:next
data => :filter, n > 100
result => :file:create => out.csv

### Python
import csv
with open('input.csv') as f:
rows = [r for r in csv.reader(f) if int(r[0]) > 100]
open('out.csv','w').write('\n'.join(','.join(r) for r in rows))

### Типовой (awk)
awk -F, '$1 > 100' input.csv > out.csv

### Использование интерпретатора
1. Открыть `main.cycle`, вставить код.
2. `▶` — запуск.
3. `:stream:next` — следующая порция.
4. Визуализация: `rect_N` для `csv`, `data`, `result`.
5. Ошибка: `:error:line` → красная обводка.

---

## Задача 2: JSON → агрегация

### true
data.json => :file:read => raw
raw, /items => :json:get => items
items:y => :data:size

### Python
import json
raw = open('data.json').read()
items = json.loads(raw)['items']
print(len(items))

### Использование
1. `:json:get` → `results['json']`.
2. `:data:size` → `results['size']`.
3. Visual — `rect_N` для `raw`, `items`.

---

## Задача 3: XML → отчёт

### true
data.xml => :file:read => raw
raw, /root/item => :xml:get
raw => :xml:names

### Python
import xml.etree.ElementTree as ET
root = ET.fromstring(open('data.xml').read())
print([c.tag for c in root])

### Использование
1. `:xml:get` → `results['xml']`.
2. `:xml:names` — список имён.
3. Log — результат.

---

## Задача 4: API → CSV

### true
url => :api:get => data
data, ',' => :data:chars
data => :file:create => report.csv

### Python
import requests
data = requests.get(url).text
open('report.csv','w').write(','.join(data))

### Использование
1. `:api:get` → `results['api:get']`.
2. `:data:chars` → `results['chars']`.
3. `:file:create` — запись.

---

## Задача 5: Отчёт в Telegram

### true
url => :api:get => data
data:y => :data:size
:data:size => total
chat_id, total => :send:telegram

### Python
import requests
data = requests.get(url).text
total = len(data.split(','))
requests.post(f'https://api.telegram.org/bot{token}/sendMessage',
data={'chat_id': chat_id, 'text': str(total)})

### Использование
1. `:secret:set('telegram_token', '...')`.
2. `:api:get` → `data`.
3. `:data:size` → `total`.
4. `:send:telegram` — отправка.

---

## Задача 6: Группировка (groupby)

### true
1,2,3,4,5,6 => :data:groupby, n % 2, count
:groupby => parity

### Python
from collections import Counter
data = [1,2,3,4,5,6]
c = Counter(n % 2 for n in data)
print(c)

### Использование
1. `:data:groupby` → `results['groupby']` = `0:3,1:3`.
2. `:groupby => parity` — именование.
3. Visual — `rect_N` для `parity`.