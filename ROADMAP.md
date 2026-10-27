# ROADMAP

## Плагины (через Extensions)

### GPIO / Serial

- `:gpio:read/write`, `:serial:open/read/write/close`
- Реализация: `Extensions.register(GpioExtension())`
- Зависимости: `com.fazecast:jSerialComm`

### Sandbox для `:ext`

- Изоляция плагинов (SecurityManager / отдельный classloader)
- Ограничение доступа к файлам/сети

---

## Виртуализация визуализации

- 100+ фигур: LazyRow + Canvas + виртуализация по viewport
- Кэш изображений (ограничение 10)

---

## Звук

- `:sound:channel` — каналы (несколько звуков одновременно)
- `:sound:volume`

---

## JSON

- `:json:get:deep` — вложенный JSON
- `:json:path` — JSONPath

---

## Терминал

- `:terminal:read:wait` — интерактивное чтение
- `:terminal:powershell` — Windows

---

## Память

- `:mem` shared (между агентами)
- `:mem` ByteArray (asm-точность)

---

## WebSocket

- `:websocket:messages` — история
- `:websocket:auth` — токен

---

## Отправка

- `:send:email:attach` — вложения
- `:send:telegram` — markdown/HTML (`parse_mode`)
- `:send:max` — обновление API

---

## XML

- `:xml:attrs` — атрибуты
- `:xml:xpath` — полный XPath

---

## Расписание

- `:schedule:limit` — реализовано (1.3.0)
- `:schedule:tz` — реализовано

---

## Визуализация

- `:background` слои
- `:rect` condition из кода (ручное)
- `:table` — 5 чисел area (z,x,y,w,h)
- Виртуализация 100+ фигур
- Анимация (`:anim:set/play/stop`, `AnimationSpec`, спрайты)

---

## Секреты

- Windows ACL (`secret.key`)

---

## AI

- DeepSeek/Claude — стриминг ответов
- `:api` rate limit
- `:api:model` — выбор модели

---

## UI

- Quick Run panel / REPL
- Виртуализация 100+ фигур
- Анимация (спрайты, состояния)

---

## Документация

- `docs/roles/*.md` — 12 ролей (реализовано)
- `docs/compare/*.md` — сравнение с Python/asm/C

---

## Тесты

- `:schedule` — реальное время (flaky)
- `:websocket` — port=0
- `:sound` — mock AudioSystem
- PTY — mock + native