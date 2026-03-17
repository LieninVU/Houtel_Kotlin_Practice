# Android-3: Документация структуры данных

## Модель Event

```kotlin
data class Event(
    val title: String,       // Название события
    val description: String, // Описание
    val date: String,        // Дата: "DD.MM.YYYY"
    val time: String,        // Время: "HH:MM"
    val location: String,    // Место проведения
    val category: String     // Категория (Концерт, Экскурсия, Спорт и т.д.)
)
```

## Структура XLS-файла

Файл `assets/events.xlsx` — первая строка заголовки, данные с 1-й строки:

| Колонка | Индекс | Пример |
|---------|--------|--------|
| title | 0 | Вечер живой музыки |
| description | 1 | Джазовый концерт в лобби |
| date | 2 | 15.06.2024 |
| time | 3 | 19:00 |
| location | 4 | Лобби |
| category | 5 | Концерт |

Поддерживаемые форматы: `.xlsx` (OOXML), `.xls` (HSSF).
Если файл не найден — автоматически используются моки.

## UserPreferences

Хранит пользовательские предпочтения в SharedPreferences (`hotel_user_prefs`):

| Ключ | Тип | Описание |
|------|-----|----------|
| `last_category` | String | Последняя просмотренная категория |
| `viewed_events` | Set\<String\> | Заголовки просмотренных событий |

## Алгоритм рекомендаций

1. Читаем `last_category` из `UserPreferences`
2. Если не пусто — фильтруем события по этой категории, берём до 5
3. Если совпадений нет или история пуста — возвращаем 3 случайных события

## UiState

```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

При ошибке загрузки XLS — рекомендации показываются из моков (не пустой экран).
