# Роль: Тестировщик

## Задача 1: Assert

### Постановка
Проверить равенство, зафиксировать ошибку при провале.

### true
a == b, 'must be equal' => :assert
:error => dst

### Python (pytest)
def test_equal():
assert a == b, 'must be equal'

### Использование интерпретатора
1. Вставить код в `main.cycle`.
2. `▶` — запуск.
3. Log — assert ✓/✗.
4. Ошибка: `:error:line` → красная обводка.
5. `:error:resolve = 0` — сброс.

---

## Задача 2: API-тест

### true
url, data => :api:post:retry
code == 1 ? PASS

### Python
import requests
r = requests.post(url, json=data)
assert r.status_code in range(200, 300)

### Использование
1. `:api:post:retry` → `code`.
2. `code == 1` — проверка.
3. Log — PASS/FAIL.

---

## Задача 3: Error-обработка

### true
:error => line
line == 0 ? PASS

### Python
try:
run()
except Exception as e:
print(e.line)

### Использование
1. `:error:line` → `results['error:line']`.
2. `:error => line` — именование.
3. `line == 0` — проверка.

---

## Задача 4: Mock HTTP

### true
url, response => :api:mock
url, data => :api:post => code
code == 1 ? PASS
:api:mock:reset

### Python (requests-mock)
import requests_mock
with requests_mock.Mocker() as m:
m.post(url, status_code=200)
assert requests.post(url).status_code == 200

### Использование
1. `:api:mock` — замокать URL.
2. `:api:post` — вернёт mock.
3. `:api:mock:reset` — сброс.
4. Log — PASS/FAIL.

---

## Задача 5: Регрессия

### true
:assert
0 => :error:resolve

### Python
assert True

### Использование
1. `:assert` — проверка.
2. `:error:resolve = 0` — сброс.
3. Log — результат.

---

## Задача 6: Пауза при assert fail

### true
1 => :assert:pause
a == b, 'must be equal' => :assert
0 => :assert:pause

### Python (pytest -x)
pytest -x  # stop on first failure

### Использование
1. `1 => :assert:pause` — включить паузу.
2. `:assert` — fail → пауза.
3. `0 => :assert:pause` — выключить.
4. `⏭` — шаг после паузы.