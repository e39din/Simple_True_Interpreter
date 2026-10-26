# Simple True Interpreter — примеры программ

Версия языка: **0.10.0**. Все функции — с `:`. Цепочки `=>` позволяют
сократить код. **Имя должно быть присвоено до использования** — `= name`
(пустой контейнер) или `value = name`. Примеры работают **в середине
цикла** — завершение (`=> :stop`) не показано.

## Алгоритмические

### 1. Сумма массива

```true
= result
1, 2, 3, 4, 5 => :sum => result
```
!! result: 15

### 2. Максимум

```true
= result
3, 7, 2, 9, 4 => :max => result
```
!! result: 9

### 3. Hello, world

```true
Hello, world = result
```
!! result: Hello, world

### 4. Палиндром

```true
racecar = s
s:x:1 = a
s:x:7 = b
= result
a == b ? 1 = result
```
!! result: 1

### 5. Подсчёт строк

```true
= raw
input.txt => :get => raw
raw:l => :size => lineCount
```

### 6. Реверс строки

```true
= result
hello => :reverse => result
```
!! result: olleh

### 6b. Длина через `:s`

```true
= result
hello => :s => result
```
!! result: 5

## CI / ETL / embedded

### 7. Smoke test API

```true
= response
= ok_found
https://api.example.com/health => :get => response
response, ok => :contains => ok_found
ok_found = result
```

### 8. embedded калькулятор

```true
3 = a
4 = b
a + b * 2 => :math => result
```
!! result: 11

### 9. ETL подсчёт строк в CSV

```true
= raw
input.csv => :get => raw
raw:l => :size => rows
```

## Агенты

### 10. Переход агента на файл

```true
^ other.cycle
```

### 11. Остановка агента

```true
^ :stop
```

### 12. Удаление агента

```true
^ :delete
```

### 13. Пауза агента

```true
^ :pause
```

### 14. Продолжение агента

```true
^ :resume
```

### 15. Сброс файла

```true
^ :reset
```

### 16. Статус агента

```true
= result
:agent:main:status => result
```

### 17. Все статусы

```true
= result
:agent:status => result
```

### 18. Ожидание агента

```true
100 => :agent:wait
```

### 19. Контейнер step

```true
= result
step => result
```

### 20. Контейнер cycle

```true
= result
cycle => result
```

## Уничтожение

### 21. Удаление контейнера

```true
a = 5
a -> nothing
```

### 22. Удаление файла

```true
file:other.cycle -> nothing
```

## Данные

### 23. Размер по оси

```true
1, 2, 3 = arr
arr:y => :size => result
```
!! result: 3

### 23b. Размер через `:s`

```true
1, 2, 3 = arr
arr:y => :s => result
```
!! result: 3

### 24. Push

```true
1, 2 = arr
arr:y, 3 => :push => result
```
!! result: 1,2,3

### 25. Splitter

```true
= raw
input.csv => :get => raw
. => raw:splitter:l:;
raw:l => :size => rows
```

### 26. Range

```true
1, 5 => :range => result
```
!! result: 1,2,3,4,5

### 27. Repeat

```true
0 = a
a + 1 => a = action
action, 5 => :repeat
```

### 28. Foreach без `.`

```true
1, 2, 3 = arr
0 = sum
arr:foreach:y ; n :
sum + n => :math => sum,
n => :log
```
!! sum: 6

### 29. Error stack

```true
1 // 0 => :math
:error:lines => result
```
!! result: 1

### 30. Error handler (actions)

```true
actions = ":error ? ^ :handle\n:run"
1 // 0 => :math
= handle
error => :log
```
(В `actions` — `:error ? ^ :handle`.)

## JSON

### 31. JSON get

```true
{'name':'test','value':42} = json
json, name => :json:get => result
```
!! result: test

### 32. JSON names

```true
{'a':1,'b':2} = json
json => :json:names => result
```
!! result: a,b

## Условия и переходы

### 33. Переход на строку

```true
^ 5
```

### 34. Условие с переходом

```true
a < max ? ^ do
```

### 35. FizzBuzz (классический, через `^`/`?`)

```true
= out
1 = i
15 = n
= loop
i > n ? ^ done
i % 15 == 0 ? ^ fizzbuzz
i % 3 == 0 ? ^ fizz
i % 5 == 0 ? ^ buzz
^ num
= fizzbuzz
FizzBuzz = out
i + 1 => :math => i
^ loop
= fizz
Fizz = out
i + 1 => :math => i
^ loop
= buzz
Buzz = out
i + 1 => :math => i
^ loop
= num
i = out
i + 1 => :math => i
^ loop
= done
```

### 36. FizzBuzz (короткая версия через `:range` + `foreach`)

```true
= ns
= out
1, 15 => :range => ns
ns:foreach:y ; n :
n % 15 == 0 ? FizzBuzz = out,
n % 3 == 0 ? Fizz = out,
n % 5 == 0 ? Buzz = out,
n = out
```