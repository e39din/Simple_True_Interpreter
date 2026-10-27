# Роль: Геймдизайнер

## Задача 1: Уровень на холсте

### Постановка
Нарисовать уровень, разместить объекты на холсте.

### true
/path/level.png => :background
10, 20 => rect_1.area
50, 80 => rect_2.area

### Python (pygame)
import pygame
screen = pygame.display.set_mode((800, 600))
bg = pygame.image.load('level.png')
screen.blit(bg, (0, 0))

### Использование интерпретатора
1. Вставить код в `main.cycle`.
2. Открыть Visual.
3. Перетаскивать фигуры → `rect_N.area`.
4. `:background` — фон.
5. Ошибка: `:error:line` → красная обводка.

---

## Задача 2: Анимация (состояние)

### true
/path/sprite.png => rect_1.img
1 => rect_1.visible
0 => rect_1.visible

### Python
sprite = pygame.image.load('sprite.png')
sprite.set_alpha(0)  # invisible

### Использование
1. `rect_1.img` — картинка.
2. `rect_1.visible` — видимость.
3. Visual — переключение.
4. Log — состояние.

---

## Задача 3: Звук

### true
/path/step.wav => :sound:play
:sound:stop

### Python
pygame.mixer.Sound('step.wav').play()

### Использование
1. `:sound:play` — WAV/MP3.
2. `:sound:stop` — остановка.
3. Log — результат.

---

## Задача 4: Состояние

### true
0 = hp
hp - 1 => :math => hp
hp < 0 ? => :stop

### Python
hp = 0
hp -= 1
if hp < 0:
game_over()

### Использование
1. `0 = hp` — контейнер.
2. `:math` — изменение.
3. Условие — конец.
4. Visual — `rect_N` для `hp`.

---

## Задача 5: Таймер

### true
*/1 * * * *, tick => :schedule

### Python
import time
while True:
tick()
time.sleep(60)

### Использование
1. Действие в контейнер `tick`.
2. `:schedule` — cron.
3. Бейдж ⏰.
4. `:schedule:stop` — остановка.

---

## Задача 6: Игра «Пещера» (демо)

### Постановка
Простая текстовая игра с уровнями.

### true
См. `docs/SAMPLES.md` §16 (`cave.cycle` + `ending.cycle`).

### Использование интерпретатора
1. Открыть `cave.cycle`.
2. Visual — `rect_N` для `hp`, `gold`, `item`, `potions`.
3. `:ask` — диалог.
4. `^ ending.cycle` — переход между уровнями.
5. Log — сообщения.