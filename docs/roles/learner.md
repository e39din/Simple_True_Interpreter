# Роль: Ученик

## Задача 1: Hello

### Постановка
Вывести сообщение.

### true
hello => :message

### Python
print('hello')

### Использование интерпретатора
1. Вставить код в `main.cycle`.
2. `▶` — запуск.
3. Log — сообщение.
4. Визуализация: `rect_N` для контейнеров.
5. Ошибка: `:error:line` → красная обводка.

---

## Задача 2: Сложение

### true
a + b => :math

### Python
c = a + b

### Использование
1. `a + b => :math` → `results['math']`.
2. Log — результат.
3. `:math => c` — именование.

---

## Задача 3: Цикл

### true
0 = a
= loop
a + 1 => :math => a
a < 5 ? ^ loop

### Python
a = 0
while a < 5:
a = a + 1

### Использование
1. `⏭` — шаг.
2. Visual — визуализация цикла.
3. Log — значения `a`.
4. `⏮` — назад.

---

## Задача 4: Память

### true
16 => :mem:alloc => p
p, 5 => :mem:write
p => :mem:read => v

### Python
p = bytearray(16)
p[0] = 5
v = p[0]

### Использование
1. `:mem:alloc` — адрес.
2. `:mem:write/read`.
3. `@1000` — подстановка.
4. `:mem:dump` — hex.

---

## Задача 5: Ошибка

### true
:error => dst
:error:line => line
:error:message => msg

### Python
try:
run()
except Exception as e:
print(e)

### Использование
1. Ошибка → `:error:line`.
2. `:error:line → dst`.
3. Log — красная строка.
4. `:error:resolve = 0` — сброс.

---

## Задача 6: Игра «Пещера»

### Постановка
Пройти игру: выбрать действие, сразиться с драконом, выйти с золотом.

### true
См. `docs/SAMPLES.md` §16 (`cave.cycle` + `ending.cycle`).

### Использование интерпретатора
1. Открыть `cave.cycle`.
2. `▶` — запуск.
3. Диалог «Что делаешь?» — ввести `1`–`4`.
4. Visual — `rect_N` для `hp`, `gold`, `item`, `potions`.
5. Table — все контейнеры.
6. `⏭` — шаг; `⏮` — назад.
7. Log — сообщения.