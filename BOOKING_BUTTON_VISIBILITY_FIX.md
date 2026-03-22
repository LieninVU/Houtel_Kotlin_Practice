# Исправление видимости кнопки Book Now

## Проблема

### ❌ БЫЛО

**Поведение:**
- Кнопка "Book Now" отображалась **полупрозрачной** (disabled state)
- Кнопка была видна даже когда **не выбрана комната**
- Кнопка была видна когда **не выбраны даты**
- Кнопка была видна когда **не введено имя гостя**

**Визуально:**
```
┌─────────────────────────────────┐
│  1. Select a Room               │
│  [Room Card 1] [Room Card 2]    │
│                                 │
│  2. Select Dates                │
│  [Check-in] [Check-out]         │
│                                 │
│  3. Guest Information           │
│  [Full Name]                    │
│                                 │
│  [Book Now]  ← Полупрозрачная!  │  ❌ Видна когда не заполнено
└─────────────────────────────────┘
```

**Код (BookingFragment.kt):**
```kotlin
// ❌ Кнопка только disabled, но не скрыта
binding.btnBook.isEnabled = state.isFormValid && !state.isBookingLoading
```

**Проблема:**
- `isEnabled=false` делает кнопку полупрозрачной
- Но кнопка остаётся **видимой**
- Пользователь видит неактивную кнопку и может подумать что это баг

---

## Решение

### ✅ СТАЛО

**Поведение:**
- Кнопка "Book Now" **полностью скрыта** когда форма не заполнена
- Кнопка **появляется** только когда все поля заполнены:
  - ✅ Выбрана комната
  - ✅ Выбраны даты (check-in и check-out)
  - ✅ Введено имя гостя

**Визуально:**
```
До заполнения формы:
┌─────────────────────────────────┐
│  1. Select a Room               │
│  [Room Card 1] [Room Card 2]    │
│                                 │
│  2. Select Dates                │
│  [Check-in] [Check-out]         │
│                                 │
│  3. Guest Information           │
│  [Full Name]                    │
│                                 │
│  ← Кнопки нет!                  │  ✅ Скрыта
└─────────────────────────────────┘

После заполнения формы:
┌─────────────────────────────────┐
│  1. Select a Room               │
│  [Room Card 1] [Room Card 2✓]   │
│                                 │
│  2. Select Dates                │
│  [2026-03-22] [2026-03-25]      │
│                                 │
│  3. Guest Information           │
│  [John Doe]                     │
│                                 │
│  [Book Now]  ← Активная!        │  ✅ Появилась
└─────────────────────────────────┘
```

---

### ✅ Изменения в коде

**BookingFragment.kt:**
```kotlin
// ❌ БЫЛО
binding.btnBook.isEnabled = state.isFormValid && !state.isBookingLoading

// ✅ СТАЛО
// Кнопка видима только когда форма заполнена
binding.btnBook.isVisible = state.isFormValid && !state.isBookingLoading
binding.btnBook.isEnabled = !state.isBookingLoading
```

**Объяснение:**
- `isVisible` — полностью скрывает кнопку (GONE)
- `isEnabled` — только делает неактивной (но видимой)

---

## Логика видимости

### ✅ isFormValid (из BookingUiState)

```kotlin
val isFormValid: Boolean
    get() = selectedRoom != null &&
            !checkInDate.isNullOrBlank() &&
            !checkOutDate.isNullOrBlank() &&
            guestName.isNotBlank()
```

**Условия:**
1. ✅ `selectedRoom != null` — комната выбрана
2. ✅ `!checkInDate.isNullOrBlank()` — дата заезда выбрана
3. ✅ `!checkOutDate.isNullOrBlank()` — дата выезда выбрана
4. ✅ `guestName.isNotBlank()` — имя гостя введено

**Когда кнопка видима:**
```
Комната ✓ + Даты ✓ + Имя ✓ = Кнопка видима
Комната ✗ + Даты ✓ + Имя ✓ = Кнопка скрыта
Комната ✓ + Даты ✗ + Имя ✓ = Кнопка скрыта
Комната ✓ + Даты ✓ + Имя ✗ = Кнопка скрыта
```

---

## Сравнение подходов

| Аспект | ❌ isEnabled | ✅ isVisible |
|--------|--------------|--------------|
| **Видимость** | Полупрозрачная | Полностью скрыта |
| **Layout** | Занимает место | Не занимает место |
| **UX** | Пользователь видит неактивную | Пользователь не видит пока не готова |
| **Восприятие** | "Почему кнопка серая?" | "Кнопка появилась когда заполнил" |

---

## Анимация появления (опционально)

Можно добавить плавную анимацию появления:

```kotlin
// С анимацией
if (state.isFormValid && !state.isBookingLoading) {
    binding.btnBook.animate()
        .alpha(1f)
        .setDuration(200)
        .withStartAction { binding.btnBook.isVisible = true }
        .start()
} else {
    binding.btnBook.animate()
        .alpha(0f)
        .setDuration(200)
        .withEndAction { binding.btnBook.isVisible = false }
        .start()
}
```

---

## Material Design рекомендации

### ✅ Кнопки должны быть видны когда нужны

> "Buttons should be visible when users can take action. Hide buttons that aren't relevant yet."

**Правильно:**
- Скрывать кнопки пока форма не заполнена
- Показывать кнопки когда действие доступно

**Неправильно:**
- Показывать полупрозрачные неактивные кнопки
- Пользователь может подумать что это баг

---

## UX улучшения

### ✅ Прогрессивное раскрытие

```
Шаг 1: Пользователь выбирает комнату
       ↓
       Карточка комнаты подсвечивается
       ↓
       Появляется summary карточка

Шаг 2: Пользователь выбирает даты
       ↓
       Даты отображаются в summary
       ↓
       Появляется цена

Шаг 3: Пользователь вводит имя
       ↓
       Все поля заполнены ✓
       ↓
       Появляется кнопка "Book Now"
```

**Результат:**
- ✅ Пользователь видит прогресс
- ✅ Кнопка появляется в нужный момент
- ✅ Нет визуального шума

---

## Сборка проекта

```bash
# Windows (PowerShell)
$env:JAVA_HOME='C:\Program Files\Java\jdk-21'
.\gradlew.bat assembleDebug

# Статус: ✅ BUILD SUCCESSFUL
```

---

## Изменённые файлы

### Обновлённые:
1. `BookingFragment.kt` — замена `isEnabled` на `isVisible`

---

## Тестирование

### ✅ Сценарии для проверки

**Сценарий 1: Пустая форма**
1. Открыть BookingFragment
2. ✅ Кнопка "Book Now" скрыта

**Сценарий 2: Выбрана только комната**
1. Выбрать комнату из списка
2. ✅ Кнопка "Book Now" скрыта
3. ✅ Summary карточка видима

**Сценарий 3: Выбрана комната + даты**
1. Выбрать комнату
2. Выбрать даты
3. ✅ Кнопка "Book Now" скрыта
4. ✅ Summary карточка видима с ценой

**Сценарий 4: Форма полностью заполнена**
1. Выбрать комнату
2. Выбрать даты
3. Ввести имя гостя
4. ✅ Кнопка "Book Now" появилась
5. ✅ Кнопка активна (не серая)

**Сценарий 5: Очистка формы**
1. Заполнить всю форму
2. ✅ Кнопка видима
3. Изменить дату (триггер обновления состояния)
4. ✅ Кнопка остаётся видимой (форма всё ещё заполнена)

---

## Рекомендации

### ✅ Используйте isVisible для кнопок

```kotlin
// ✅ Правильно
binding.btnBook.isVisible = state.isFormValid
binding.btnBook.isEnabled = true

// ❌ Неправильно
binding.btnBook.isVisible = true
binding.btnBook.isEnabled = state.isFormValid
```

### ✅ Проверяйте все условия валидности

```kotlin
val isFormValid: Boolean
    get() = selectedRoom != null &&      // Комната
            checkInDate != null &&       // Дата заезда
            checkOutDate != null &&      // Дата выезда
            guestName.isNotBlank()       // Имя
```

### ✅ Документируйте логику

```kotlin
// ✅ Обновляем прогресс бронирования и кнопку
// Кнопка видима только когда форма заполнена
binding.btnBook.isVisible = state.isFormValid && !state.isBookingLoading
```
