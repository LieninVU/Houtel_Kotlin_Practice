# Добавление Intent для телефона и Email

## Цель
Добавить кликабельные intent для телефона (ACTION_DIAL) и email (ACTION_SENDTO) в HotelInfoFragment.

---

## Что было сделано

### ✅ 1. Обновлён HotelInfoFragment.kt

#### Добавлены импорты
```kotlin
import android.content.Intent
import android.net.Uri
import android.widget.TextView
import android.widget.Toast
```

#### Добавлены данные для контактов
```kotlin
// ✅ Данные для контактов
private val hotelPhone = "+7 (800) 555-35-35"
private val hotelEmail = "info@hotel.ru"
private val hotelAddress = "ул. Гостиничная, 1"
```

#### Добавлен метод setupContactClickListeners()
```kotlin
/**
 * Настройка кликов для контактов (телефон и email).
 * ✅ Intent для звонка и отправки email.
 */
private fun setupContactClickListeners() {
    // Находим TextView контактов в layoutContacts
    val phoneTextView = binding.layoutContacts.findViewById<TextView>(R.id.tvPhone)
    val emailTextView = binding.layoutContacts.findViewById<TextView>(R.id.tvEmail)

    // ✅ Телефон - ACTION_DIAL
    phoneTextView.setOnClickListener {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$hotelPhone")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_cannot_call),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // ✅ Email - ACTION_SENDTO
    emailTextView.setOnClickListener {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$hotelEmail")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                getString(R.string.error_cannot_email),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
```

#### Вызов в onViewCreated()
```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    // ...
    setupContactClickListeners() // ✅ Добавляем клики для контактов
}
```

---

### ✅ 2. Обновлён fragment_hotel_info.xml

#### Телефон - кликабельный TextView
```xml
<!-- ✅ Телефон - кликабельный -->
<TextView
    android:id="@+id/tvPhone"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="📞 Телефон: +7 (800) 555-35-35"
    android:textSize="15sp"
    android:paddingBottom="8dp"
    android:paddingTop="8dp"
    android:clickable="true"
    android:focusable="true"
    android:foreground="?attr/selectableItemBackground"
    android:background="?attr/selectableItemBackground" />
```

**Атрибуты для кликабельности:**
- `android:clickable="true"` — делает TextView кликабельным
- `android:focusable="true"` — позволяет получать фокус
- `android:foreground="?attr/selectableItemBackground"` — эффект нажатия (Material)
- `android:background="?attr/selectableItemBackground"` — фон при нажатии

#### Email - кликабельный TextView
```xml
<!-- ✅ Email - кликабельный -->
<TextView
    android:id="@+id/tvEmail"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="📧 Email: info@hotel.ru"
    android:textSize="15sp"
    android:paddingBottom="8dp"
    android:paddingTop="8dp"
    android:clickable="true"
    android:focusable="true"
    android:foreground="?attr/selectableItemBackground"
    android:background="?attr/selectableItemBackground" />
```

---

### ✅ 3. Обновлён fragment_hotel_info.xml (планшетная версия)

Аналогичные изменения внесены в `layout-sw600dp/fragment_hotel_info.xml` для планшетов.

---

### ✅ 4. Добавлены строки в strings.xml

```xml
<string name="error_cannot_call">Не удалось выполнить звонок</string>
<string name="error_cannot_email">Не удалось открыть почтовый клиент</string>
```

---

## Типы Intent

### ✅ ACTION_DIAL — Звонок

```kotlin
val intent = Intent(Intent.ACTION_DIAL).apply {
    data = Uri.parse("tel:+7 (800) 555-35-35")
}
startActivity(intent)
```

**Что делает:**
- Открывает телефонный набор с номером
- Пользователь должен нажать кнопку вызова
- **Не требует разрешений** (в отличие от ACTION_CALL)

**URI формат:**
- `tel:+78005553535` — без пробелов
- `tel:+7 (800) 555-35-35` — с пробелами (работает)

---

### ✅ ACTION_SENDTO — Email

```kotlin
val intent = Intent(Intent.ACTION_SENDTO).apply {
    data = Uri.parse("mailto:info@hotel.ru")
}
startActivity(intent)
```

**Что делает:**
- Открывает почтовый клиент
- Поле "Кому" заполнено email
- Пользователь пишет и отправляет письмо

**URI формат:**
- `mailto:info@hotel.ru` — только email
- `mailto:info@hotel.ru?subject=Запрос&body=Текст` — с темой и текстом

---

### ✅ ACTION_CALL — Прямой вызов (требует разрешения)

```kotlin
// ❌ Требует разрешения android.permission.CALL_PHONE
val intent = Intent(Intent.ACTION_CALL).apply {
    data = Uri.parse("tel:+78005553535")
}
startActivity(intent)
```

**Важно:**
- Требует разрешения `CALL_PHONE`
- Сразу начинает вызов (без подтверждения)
- **Не используется в этом проекте** (используется ACTION_DIAL)

---

## Обработка ошибок

### ✅ Try-catch для Intent

```kotlin
try {
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:$phone")
    }
    startActivity(intent)
} catch (e: Exception) {
    // Обработка ошибки (нет телефонного приложения)
    Toast.makeText(context, "Не удалось выполнить звонок", Toast.LENGTH_SHORT).show()
}
```

**Возможные ошибки:**
- Нет телефонного приложения
- Нет почтового клиента
- Неправильный URI формат

---

## Сравнение подходов

| Аспект | ❌ БЫЛО | ✅ СТАЛО |
|--------|---------|----------|
| **Телефон** | Текст без клика | ACTION_DIAL Intent |
| **Email** | Текст без клика | ACTION_SENDTO Intent |
| **Обработка ошибок** | Нет | Try-catch + Toast |
| **UI эффект** | Нет | selectableItemBackground |
| **ID для TextView** | Нет | Есть (tvPhone, tvEmail) |

---

## Разрешения

### ✅ Не требуются для ACTION_DIAL и ACTION_SENDTO

```xml
<!-- ✅ НЕ НУЖНО для ACTION_DIAL -->
<!-- <uses-permission android:name="android.permission.CALL_PHONE" /> -->

<!-- ✅ НЕ НУЖНО для ACTION_SENDTO -->
<!-- <uses-permission android:name="android.permission.SEND_SMS" /> -->
```

**ACTION_DIAL и ACTION_SENDTO не требуют разрешений!**

---

## UX улучшения

### ✅ Визуальная обратная связь

```xml
android:foreground="?attr/selectableItemBackground"
android:background="?attr/selectableItemBackground"
```

**Эффект:**
- Подсветка при нажатии (Material Design ripple)
- Пользователь видит, что элемент кликабельный

### ✅ Padding для удобного нажатия

```xml
android:paddingTop="8dp"
android:paddingBottom="8dp"
```

**Зачем:**
- Увеличивает область нажатия
- Удобнее нажимать на сенсорном экране

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
1. `HotelInfoFragment.kt` — добавлен метод `setupContactClickListeners()`
2. `fragment_hotel_info.xml` — добавлены id, clickable атрибуты
3. `fragment_hotel_info.xml (sw600dp)` — аналогично для планшетов
4. `strings.xml` — добавлены строки ошибок

---

## Тестирование

### ✅ Проверка работы телефона

1. Откройте HotelInfoFragment
2. Перейдите на вкладку "Контакты"
3. Нажмите на номер телефона
4. Должен открыться телефонный набор с номером

### ✅ Проверка работы email

1. Откройте HotelInfoFragment
2. Перейдите на вкладку "Контакты"
3. Нажмите на email
4. Должен открыться почтовый клиент с заполненным полем "Кому"

---

## Рекомендации для будущей разработки

1. **Используйте ACTION_DIAL вместо ACTION_CALL** — не требует разрешений
2. **Добавляйте try-catch** — обрабатывайте отсутствие приложений
3. **Добавляйте selectableItemBackground** — визуальная обратная связь
4. **Увеличивайте padding** — удобнее для нажатия
5. **Добавляйте ID для TextView** — для нахождения в коде

---

## Примеры использования

### ✅ С темой и текстом для email

```kotlin
val intent = Intent(Intent.ACTION_SENDTO).apply {
    data = Uri.parse("mailto:info@hotel.ru?subject=Бронирование&body=Здравствуйте!")
}
startActivity(intent)
```

### ✅ С sms (если нужно)

```kotlin
val intent = Intent(Intent.ACTION_SENDTO).apply {
    data = Uri.parse("smsto:+78005553535")
}
startActivity(intent)
```

### ✅ С выбором приложения (для email)

```kotlin
val intent = Intent(Intent.ACTION_SEND).apply {
    type = "message/rfc822"
    putExtra(Intent.EXTRA_EMAIL, arrayOf("info@hotel.ru"))
    putExtra(Intent.EXTRA_SUBJECT, "Запрос")
}
startActivity(Intent.createChooser(intent, "Отправить через"))
```
