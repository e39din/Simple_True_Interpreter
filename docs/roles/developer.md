# Роль: Разработчик

## Задача 1: Плагин `:ext`

### Постановка
Подключить стороннюю логику через Extension.

### true
args => :ext:my_plugin
:ext:list => dst

### Kotlin
object MyExtension : Extension {
override val names = setOf("my_plugin")
override fun handle(op: String, args: List<String>, ctx: Ctx): String? =
"result:${args.joinToString(",")}"
}
Extensions.register(MyExtension())

### Использование интерпретатора
1. `Extensions.register` — при старте.
2. `:ext:list` → список.
3. `args => :ext:name` → `results['ext:name']`.
4. Ошибка плагина → `:error:line`.

---

## Задача 2: Память

### true
16 => :mem:alloc => p
p, 42 => :mem:write
p => :mem:read => v

### ASM
mov eax, 42
mov [p], eax
mov ebx, [p]

### C
void* p = malloc(16);
*(int*)p = 42;
int v = *p;

### Использование
1. `:mem:alloc` → адрес.
2. `:mem:write/read`.
3. `@1000` — подстановка.
4. `:mem:dump` — hex.

---

## Задача 3: PTY

### true
vim => :terminal => Editor

### Kotlin
val proc = PtyProcessBuilder(arrayOf("vim")).start()

### Использование
1. `:terminal` → PTY.
2. `:terminal:write` — ввод.
3. `:terminal:read` — вывод.
4. `:terminal:close` — закрыть.

---

## Задача 4: WebSocket

### true
8080 => :websocket:listen
hello => :websocket:send
:websocket:message => dst

### Kotlin
val server = object : WebSocketServer(InetSocketAddress(8080)) { ... }
server.start()

### Использование
1. `:websocket:listen` — сервер.
2. `:websocket:send` — отправка.
3. `:websocket:message` → `dst`.
4. `:websocket:close` — закрыть.

---

## Задача 5: Retry

### true
url, data => :api:post:retry, 5, 1000, 1

### Kotlin
repeat(5) {
try { if (post(url, data).code < 500) return; } catch (_: IOException) {}
delay(1000)
}

### Использование
1. `:api:post:retry` — 5 попыток, 1s, idempotent.
2. `results['api:post']` = 1/-1.
3. Log — попытки.

---

## Задача 6: Синонимы + goto

### true
12 = twelve, number, a
= twelve
a => :message
^ number

### Kotlin
// аналог: enum + map
val aliases = mapOf("twelve" to 12, "number" to 12, "a" to 12)

### Использование
1. `12 = twelve, number, a` — синонимы.
2. `= twelve` — метка.
3. `^ number` — переход по alias.
4. Visual — `rect_N` для `twelve`.