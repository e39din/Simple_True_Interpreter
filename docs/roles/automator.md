# Роль: AI-автоматизатор

## Задача 1: DeepSeek → Telegram

### Постановка
Вызвать DeepSeek, получить ответ, отправить в Telegram.

### true
deepseek_token => :secret:get => token
url, body => :api:post:retry, 5, 2000, 1
code == 1 ? :json:get => text
chat_id, text => :send:telegram

### Python
import os, requests
token = os.getenv('DEEPSEEK_TOKEN')
r = requests.post('https://api.deepseek.com/chat/completions',
headers={'Authorization': f'Bearer {token}'},
json={'model':'deepseek-chat','messages':[{'role':'user','content':'Hello'}]})
requests.post(f'https://api.telegram.org/bot{token}/sendMessage',
data={'chat_id': chat_id, 'text': r.json()['choices'][0]['message']['content']})

### Использование интерпретатора
1. `:secret:set('deepseek_token', '...')`.
2. Запуск; `:api:post:retry` — до 5 попыток.
3. `:json:get` — парсинг ответа.
4. `:send:telegram` — отправка.
5. Ошибка: `:error:line` → красная обводка.

---

## Задача 2: Claude → Email

### true
claude_token => :secret:get => token
url, body => :api:post
code == 1 ? :json:get => text
to, 'Claude', text => :send:email

### Python
import requests
token = os.getenv('CLAUDE_TOKEN')
r = requests.post('https://api.anthropic.com/v1/messages',
headers={'x-api-key': token, 'anthropic-version': '2023-06-01'},
json={'model':'claude-3-5-sonnet','max_tokens':1024,'messages':[{'role':'user','content':'Hello'}]})
send_email(to, 'Claude', r.json()['content'][0]['text'])

### Использование
1. `:secret:set('claude_token', '...')`.
2. `:api:post` → `code`.
3. `:json:get` → `text`.
4. `:send:email`.

---

## Задача 3: Цепочка DeepSeek → Claude

### true
url1, body1 => :api:post => c1
c1 == 1 ? :json:get => t1
url2, t1 => :api:post => c2
c2 == 1 ? :json:get => t2
chat_id, t2 => :send:telegram

### Python
r1 = requests.post(url1, json=body1).json()['choices'][0]['message']['content']
r2 = requests.post(url2, json={'messages':[{'role':'user','content':r1}]}).json()
requests.post(telegram_url, data={'chat_id': chat_id, 'text': r2['content'][0]['text']})

### Использование
1. `:api:post` → `c1`.
2. `:json:get` → `t1`.
3. `:api:post` с `t1` → `c2`.
4. `:json:get` → `t2`.
5. `:send:telegram`.

---

## Задача 4: Retry с backoff

### true
url, body => :api:post:retry, 10, 5000, 1

### Python
for i in range(10):
try:
r = requests.post(url, json=body, timeout=10)
if r.status_code < 500: break
except: pass
time.sleep(5)

### Использование
1. `:api:post:retry` — 10 попыток, 5s, idempotent.
2. `results['api:post']` = 1/-1.
3. Log — попытки.

---

## Задача 5: Секреты

### true
:secret:list => dst
key => :secret:remove

### Python
import keyring
keyring.get_password('simple', key)
keyring.delete_password('simple', key)

### Использование
1. `:secret:list` — список токенов.
2. `:secret:remove` — удалить.
3. Маскировка в UI — `***`.

---

## Задача 6: Mock для тестов

### true
url, response => :api:mock
url, data => :api:post => code
code == 1 ? PASS
:api:mock:reset

### Python (requests-mock)
import requests_mock
with requests_mock.Mocker() as m:
m.post(url, text='{"status":"ok"}', status_code=200)
assert requests.post(url).status_code == 200

### Использование
1. `:api:mock` — замокать URL.
2. `:api:post` — вернёт mock.
3. `:api:mock:reset` — сброс.