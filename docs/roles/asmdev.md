# Роль: Ассемблер-разработчик

## Задача 1: alloc/write/read

### Постановка
Выделить память, записать значение, прочитать.

### true
16 => :mem:alloc => p
p, 42 => :mem:write
p => :mem:read => v

### ASM
mov eax, 42
mov [p], eax
mov ebx, [p]

### C
int* p = malloc(16);
*p = 42;
int v = *p;

### Использование интерпретатора
1. Вставить код в `main.cycle`.
2. `▶` — запуск.
3. `:mem:alloc` → адрес (`results['mem:alloc']`).
4. Log — значения.
5. Ошибка: `:error:line` → красная обводка.

---

## Задача 2: copy

### true
p, q, 16 => :mem:copy

### ASM
mov ecx, 16
rep movsb

### C
memcpy(q, p, 16);

### Использование
1. `:mem:alloc` — два блока.
2. `:mem:write` — значения.
3. `:mem:copy` — копирование.
4. `:mem:read` — проверка.

---

## Задача 3: dump

### true
p, 4 => :mem:dump

### ASM
; hex dump 4 bytes at p

### C
for (int i = 0; i < 4; i++) printf("%02X ", ((unsigned char*)p)[i]);

### Использование
1. `:mem:alloc` — блок.
2. `:mem:write` — значения.
3. `:mem:dump` → `results['mem:dump']` (hex).
4. Log — дамп.

---

## Задача 4: fill

### true
p, 0, 16 => :mem:fill

### ASM
mov ecx, 16
xor eax, eax
rep stosb

### C
memset(p, 0, 16);

### Использование
1. `:mem:alloc` — блок.
2. `:mem:fill` — заполнить.
3. `:mem:read` — проверка.
4. `:mem:dump` — hex.

---

## Задача 5: @addr

### true
@1000 => :math => v

### ASM
mov eax, [1000]

### C
int v = *(int*)1000;

### Использование
1. `@1000` — подстановка значения по адресу.
2. `:math` — операция.
3. `v` — результат.
4. Log — значение.

---

## Задача 6: move + size

### true
16 => :mem:alloc => p
16 => :mem:alloc => q
p, 42 => :mem:write
p, q, 1 => :mem:move
q => :mem:read => v
p => :mem:size => s

### ASM
mov ecx, 1
rep movsb
; size via block header

### C
memmove(q, p, 1);
size_t s = 16;

### Использование
1. `:mem:alloc` — два блока.
2. `:mem:write` — значение.
3. `:mem:move` — перемещение.
4. `:mem:read` — проверка.
5. `:mem:size` — размер блока.