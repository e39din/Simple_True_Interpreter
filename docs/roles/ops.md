# Роль: Оператор

## Задача 1: Терминал

### Постановка
Выполнить команду в PTY, прочитать вывод.

### true
ls -la => :terminal => Console
Console => :terminal:read
:terminal:read => output

### Bash
ls -la

### Использование интерпретатора
1. Вставить код в `main.cycle`.
2. `▶` — запуск.
3. `:terminal:read` → `results['terminal:read']`.
4. Log — вывод.
5. Ошибка: `:error:line` → красная обводка.

---

## Задача 2: Звук

### true
/path/alarm.mp3 => :sound:play
:sound:stop

### Bash
mpg123 /path/alarm.mp3 &
kill %1

### Использование
1. `:sound:play` — воспроизведение.
2. `:sound:stop` — остановка.
3. Log — результат.

---

## Задача 3: Открыть URL

### true
https://example.com => :open:url

### Bash
xdg-open https://example.com

### Использование
1. `:open:url` — браузер по умолчанию.
2. `:open:file` — файл в ОС.
3. `:open:folder` — папка в ОС.

---

## Задача 4: Cron

### true
0 9 * * *, morning => :schedule

### Bash
0 9 * * * /usr/local/bin/morning.sh

### Использование
1. Действие в контейнер `morning`.
2. `:schedule` — cron.
3. Бейдж ⏰ в UI.
4. `:schedule:limit` — лимит jobs.

---

## Задача 5: Секреты

### true
telegram_token => :secret:get
:secret:list => dst

### Bash
export TELEGRAM_TOKEN=$(cat ~/.simple/telegram_token)

### Использование
1. `:secret:set('telegram_token', '...')`.
2. `:secret:get` — получить.
3. `:secret:list` — список.
4. Маскировка в UI — `***`.

---

## Задача 6: Автоматизация UI

### true
button:startAll => :simple:click
agent:agent-1:play => :simple:click
hello, editor:current => :simple:type

### Python (pyautogui)
import pyautogui
pyautogui.click(100, 200)
pyautogui.typewrite('hello')

### Использование
1. `:simple:click` — клик.
2. `:simple:type` — ввод.
3. `:simple:clickAt` — координаты.
4. Log — результат.