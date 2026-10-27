# PROMPT для Simple (true) — 0.13.0

## Правила

1. Контейнер — строка: `value = name`.
2. Синонимы — `value = name1, name2, name3`; только если используешь (принцип простоты).
3. Результат — `expr => :op`; именование — `:op => dst`.
4. Токены — только через `:secret:get`, **никогда** хардкод.
5. Плагины — через `:ext:name`; список — `:ext:list`.
6. StrictMode запрещает системные функции (см. LANGUAGE.md §4).
7. Память — `:mem:*`, указатели — `@addr`.
8. Терминал — `:terminal` (не `:console`).
9. Визуализация — `rect_N` авто; `:background` — фон.
10. `:data:empty` — в namespace `:data` (не `:empty`).
11. `:stream:next` без активного потока — **ошибка**.
12. `?` — условие только если слева условие, справа операция; иначе символ строки.
13. `,` — разделитель только в функциях; в `:message`/`NAMING` — часть строки.
14. `:ask` — асинхронный; ждать через `:ask:pending`.
15. Ошибки — не пауза (default); пауза — через `:error:pause`/`:assert:pause`.
16. Один агент на `main.cycle`; другие `.cycle` — без агента (создать по кнопке).
17. Поток = столбец (`.cycle`); агент работает на потоке цикла.
18. Кавычки — не нужны; `:assert` — исключение (`'msg'`).
19. Файлы — `settings.true`, `progress.true`, `start.cycle`, `templates/*.cycle`, `*.inbox`.
20. Простота: если можно сделать просто — делаем просто.

---

## Пример для DeepSeek

deepseek_token => :secret:get => token
url, body => :api:post:retry, 5, 2000, 1
code == 1 ? :json:get => text
chat_id, text => :send:telegram

---

## Пример для Claude

claude_token => :secret:get => token
url, body => :api:post
code == 1 ? :json:get => text
to, 'Claude', text => :send:email

---

## Пример игры (диалог)

100 = hp
0 = gold
факел = item
3 = potions

= turn
Что делаешь? (1 — идти, 2 — драться, 3 — лечиться, 4 — выход) => :ask

= wait
:ask:pending == 1 ? ^ wait
:ask => choice

choice == 1 ? ^ go
choice == 2 ? ^ fight
choice == 3 ? ^ heal
choice == 4 ? ^ exit
^ turn

---

## Пример памяти (asm-стиль)

16 => :mem:alloc => p
p, 42 => :mem:write
p => :mem:read => v
@1000

---

## Пример API с mock (тесты)

url, response => :api:mock
url, data => :api:post => code
code == 1 ? PASS
:api:mock:reset

---

## Пример плагина

args => :ext:my_plugin => result
result => :message
:ext:list => dst

---

## Что нельзя

- Хардкод токенов: `telegram_token = 123:ABC` — **нельзя**.
- `:console` — deprecated, использовать `:terminal`.
- `:empty` — не существует, использовать `:data:empty`.
- Кавычки для простых строк — не нужны.
- Пауза при ошибке — только через флаг.
- `while` — нет; программа циклична по умолчанию.
- `:loop`/`:endloop` — нет; новый цикл — новый `.cycle`.