# Роль: Преподаватель

## Задача 1: Визуализация цикла

### Постановка
Показать цикл в визуализации, объяснить goto и condition.

### true
0 = a
1 = b
12 = max

= do
a + 1 => :math => a
a < max ? ^ do

### Использование интерпретатора
1. Вставить код в `main.cycle`.
2. Открыть Visual → `rect_N` для `a`, `b`, `max`.
3. Стрелки goto (сплошные), condition (пунктир).
4. `⏭` — шаг, `⏮` — назад.
5. Log — значения.

---

## Задача 2: Память (asm-стиль)

### true
16 => :mem:alloc => p
p, 5 => :mem:write
p => :mem:read => v

### Сравнение
Simple: `16 => :mem:alloc => p`
ASM: `malloc`
Simple: `p, 5 => :mem:write`
ASM: `mov [p], 5`

### Использование
1. `:mem:alloc` → адрес.
2. `:mem:write/read`.
3. `@1000` — подстановка.
4. `:mem:dump` — hex-дамп.

---

## Задача 3: Пошаговое выполнение

### true
0 = a
a + 1 => :math => a
a < 5 ? ^ 2

### Использование
1. `⏭` — шаг.
2. `⏮` — назад.
3. Log — состояние.
4. Visual — подсветка.

---

## Задача 4: Error

### true
:error => dst
:error:line => line
:error:message => msg

### Использование
1. Ошибка → `:error:line`.
2. `:error:line → dst`.
3. Log — красная строка.
4. `:error:resolve = 0` — сброс.

---

## Задача 5: DeepSeek (демо)

### true
deepseek_token => :secret:get => token
url, body => :api:post
code == 1 ? :json:get => text
:message

### Использование
1. `:secret:set` — токен.
2. `:api:post` — запрос.
3. `:json:get` — ответ.
4. `:message` — вывод.

---

## Задача 6: Игра «Пещера» (демо)

### Постановка
Показать ученикам, как работает `:ask`, условие, goto, move — на примере игры.

### true
См. `docs/SAMPLES.md` §16 (`cave.cycle` + `ending.cycle`).

### Использование интерпретатора
1. Открыть `cave.cycle`.
2. Visual — `rect_N` для `hp`, `gold`, `item`, `potions`.
3. `⏭` — шаг; `⏮` — назад.
4. Log — сообщения.
5. Обсудить: `:ask` — асинхронный; `:ask:pending` — ожидание; `^ wait` — цикл.