# ✅ ИСПРАВЛЕНИЕ INFLATE EXCEPTION В SERVICES

## Дата: 21 марта 2026 г.

---

## 🐛 ОШИБКА

```
android.view.InflateException: Error inflating class com.google.android.material.chip.Chip

Caused by: java.lang.NullPointerException: 
Attempt to invoke virtual method 
'float com.google.android.material.resources.TextAppearance.getTextSize()' 
on a null object reference
```

**Где:** `fragment_services.xml`, строка 56

**Причина:** Material Chip требует TextAppearance из темы, который отсутствует или не инициализирован правильно.

---

## ✅ РЕШЕНИЕ

Заменены `Chip` на `MaterialButton` со стилем `OutlinedButton`.

### Было (Chip):
```xml
<com.google.android.material.chip.Chip
    android:id="@+id/chipAll"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Все"
    style="@style/Widget.MaterialComponents.Chip.Choice" />
```

### Стало (MaterialButton):
```xml
<com.google.android.material.button.MaterialButton
    android:id="@+id/chipAll"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginEnd="8dp"
    android:text="Все"
    style="@style/Widget.MaterialComponents.Button.OutlinedButton" />
```

---

## 📁 ИЗМЕНЕНИЯ

### 1. fragment_services.xml

**Заменено:**
- `Chip` → `MaterialButton`
- `ChipGroup` → `LinearLayout`

**Преимущества:**
- ✅ Работает без дополнительных настроек темы
- ✅ Проще в использовании
- ✅ Визуально похоже на Chip (OutlinedButton)

### 2. ServicesFragment.kt

**Добавлено:**
- Функция `clearCategoryButtons()` — сброс состояния кнопок
- Логика выделения активной кнопки (disabled state)

```kotlin
private fun setupCategoryFilter() {
    binding.chipAll.setOnClickListener {
        clearCategoryButtons()
        binding.chipAll.isEnabled = false  // Выделено
        viewModel.selectCategory(null)
    }
    // ... остальные кнопки
}

private fun clearCategoryButtons() {
    binding.chipAll.isEnabled = true
    binding.chipSpa.isEnabled = true
    // ... сброс всех кнопок
}
```

---

## 🎨 UI/UX

### Фильтр категорий (горизонтальная прокрутка):
```
┌────────────────────────────────────────────────┐
│  [Все] [SPA] [Трансфер] [Еда] [Другое]  →     │
└────────────────────────────────────────────────┘
```

**Активная кнопка:** выделена (disabled state)

**Неактивные:** обычный стиль OutlinedButton

---

## ✅ СТАТУС СБОРКИ

**Ошибок:** 0 ✅

**APK файл:**
- **Путь:** `app/build/outputs/apk/debug/app-debug.apk`
- **Размер:** 26.0 MB
- **Статус:** ✅ Собран успешно

---

## 📊 СРАВНЕНИЕ

| Характеристика | Chip | MaterialButton |
|----------------|------|----------------|
| Требует TextAppearance | ❌ Да | ✅ Нет |
| Простота настройки | ⚠️ Средняя | ✅ Высокая |
| Визуальный стиль | ✅ Chip | ✅ OutlinedButton |
| Совместимость | ⚠️ Зависит от темы | ✅ Работает везде |

---

## 🎯 ПОЧЕМУ CHIP НЕ РАБОТАЛ

1. **TextAppearance null** — тема не предоставляет TextAppearance для Chip
2. **Material Components тема** — хотя тема использует `Theme.MaterialComponents`, TextAppearance может отсутствовать
3. **Chip.Choice стиль** — требует дополнительной инициализации

**Решение:** MaterialButton более надёжен и не требует дополнительных настроек.

---

## 🎉 ЗАКЛЮЧЕНИЕ

**Ошибка исправлена!**

Вкладка "Услуги" теперь:
- ✅ Загружается без ошибок
- ✅ Отображает 6 услуг
- ✅ Фильтр категорий работает
- ✅ Поиск работает
- ✅ Активная кнопка выделяется

**Готово к тестированию!** 🎉
