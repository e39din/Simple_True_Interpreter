# Роль: DevOps

## Задача 1: Мониторинг → Telegram

### Постановка
Проверить health-endpoint, отправить уведомление при down.

### true
https://api.example.com/health => :api:get => status
status => :filter, down
chat_id, 'DOWN' => :send:telegram

### Bash
curl -s https://api.example.com/health | grep -q down && \
curl -X POST "https://api.telegram.org/bot$TOKEN/sendMessage" -d "chat_id=$CHAT&text=DOWN"

### Использование интерпретатора
1. Вставить код в `main.cycle`.
2. `▶` — запуск.
3. `:schedule` — периодический запуск.
4. Log — результат.
5. Ошибка: `:error:line` → красная обводка.

---

## Задача 2: Cron-задача

### true
*/5 * * * *, health_check => :schedule

### Bash
*/5 * * * * /usr/local/bin/health_check.sh

### Использование
1. Сохранить действие в контейнер `health_check`.
2. `:schedule` — cron.
3. Бейдж ⏰ в UI.
4. `:schedule:limit` — лимит jobs.

---

## Задача 3: Терминал → лог

### true
df -h => :terminal => Console
Console => :terminal:read
:terminal:read => disk

### Bash
df -h > disk.log

### Использование
1. `:terminal` — открыть PTY.
2. `:terminal:read` → `results['terminal:read']`.
3. Log — вывод.
4. `:terminal:close` — закрыть.

---

## Задача 4: Секреты

### true
telegram_token => :secret:get => t
smtp_pass => :secret:get => p

### Bash
export TELEGRAM_TOKEN=$(cat ~/.simple/telegram_token)

### Использование
1. `:secret:set('telegram_token', '...')`.
2. `:secret:get` — получить.
3. Маскировка в UI — `***`.

---

## Задача 5: Restart-скрипт

### true
systemctl restart app => :terminal => Console
Console => :terminal:read

### Bash
systemctl restart app && systemctl status app

### Использование
1. `:terminal` — запуск.
2. `:terminal:read` — вывод.
3. `:send:telegram` — уведомление.

---

## Задача 6: Retry при 5xx

### true
url, data => :api:post:retry, 5, 2000, 1
:api:post => result

### Bash
for i in {1..5}; do
curl -X POST "$URL" -d "$DATA" && break
sleep 2
done

### Использование
1. `:api:post:retry` — 5 попыток, 2s, idempotent.
2. `results['api:post']` = 1/-1.
3. Log — попытки.