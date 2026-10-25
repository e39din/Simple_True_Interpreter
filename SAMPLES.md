# Simple True Interpreter — примеры программ

Версия языка: **0.8.0**. Все функции начинаются с `:`. Результат функции — в `results['<op>']`. Копирование в `variables` — через `<op> = имя`.

## Алгоритмические

### 1. Сумма массива

```true 
1, 2, 3, 4, 5 => :math:sum
math:sum = sum
sum = result
=> stop
```

### 2. Максимум
```true 
3, 7, 2, 9, 4 => :math:max
math:max = mx
mx = result
=> stop
```
### 3. Hello, world
```true 
Hello, world = greeting
greeting = result
=> stop
```
### 4. Палиндром
```true 
racecar = s
0 = ok
s:x:1 = a
s:x:7 = a2
a == a2 ? 1 = ok
ok = result
=> stop
```
### 5. Подсчёт строк
```true 
0 = lineCount
input.txt => :get
:get = raw
raw:l => :size
:size = lineCount
lineCount = result
=> stop
```
### 6. Реверс строки
```true
hello = s
s => :size
:size = len
s:x:5 = c5
s:x:4 = c4
s:x:3 = c3
s:x:2 = c2
s:x:1 = c1
c5 = r1
c4 = r2
c3 = r3
c2 = r4
c1 = r5
result = r1
r1 = result
=> stop
```
## CI / ETL / embedded

### 7. Smoke test API
```true 
https://api.example.com/health => :get
:get = response
0 = ok_found
response, ok => :data:contains => ok_found
=> stop
```
### 8. embedded калькулятор
```true 
3 = a
4 = b
a + b * 2 => :math
:math = result
=> stop
```
### 9. ETL подсчёт строк в CSV
```true 
0 = rows
input.csv => :get
:get = raw
raw:l => :size
:size = rows
rows = result
=> stop
```
## Агенты

### 10. Переключение агента на другой файл
```true 
agent:main -> file:other
=> stop
```
### 11. Копирование агента
```true 
agent:main => file:other
=> stop
```
### 12. Удаление агента
```true 
agent:main -> nothing
=> stop
```
### 13. Пауза агента
```true 
agent:main => pause
=> stop
```
### 14. Статус агента
```true 
agent:main:status => st
st = result
=> stop
```
### 15. Все статусы
```true 
agent:status => all
all = result
=> stop
```
### 16. Ожидание агента
```true 
100 => agent:wait
=> stop
```
## Уничтожение

### 17. Удаление переменной
```true 
a = 5
a -> nothing
=> stop
```
### 18. Удаление файла
```true 
file:other -> nothing
=> stop
```
## Данные

### 19. Разбиение по оси
```true 
1, 2, 3 = arr
arr:y => :data:length
:data:length = len
len = result
=> stop
```
### 20. Push
```true 
1, 2 = arr
arr:y, 3 => :data:push
:data:push = pushed
pushed = result
=> stop
```
### 21. Splitter
```true 
input.csv => :get
:get = raw
. => raw:splitter:l:;
raw:l => :size
:size = rows
rows = result
=> stop
```
## JSON

### 22. JSON get
```true 
{'name':'test','value':42} = json
json, name => :json:get
:json:get = name_val
name_val = result
=> stop
```
### 23. JSON names
```true 
{'a':1,'b':2} = json
json => :json:names
:json:names = keys
keys = result
=> stop
```
## Условия и переходы

### 24. FizzBuzz
```true 
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
=> stop
```