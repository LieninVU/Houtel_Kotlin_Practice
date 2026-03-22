# Вынос строк в ресурсы (strings.xml)

## Цель
Вынести все hardcoded строки из кода и layout-файлов в `res/values/strings.xml` для:
- ✅ Поддержки локализации (многоязычности)
- ✅ Централизованного управления текстом
- ✅ Упрощения перевода приложения
- ✅ Следования best practices Android

---

## Что было сделано

### 1. strings.xml — полный список строк

Все строки проекта теперь находятся в `res/values/strings.xml` и сгруппированы по категориям:

#### Категории строк:
- **App Name** — название приложения
- **NFC Key** — строки для NFC ключей
- **Booking Fragment** — экран бронирования
- **Dashboard Fragment** — главный экран
- **Payment Fragment** — экран оплаты
- **Services Fragment** — услуги отеля
- **Reviews Fragment** — отзывы
- **Maps Fragment** — карта и рестораны
- **Hotel Info Fragment** — информация об отеле
- **Room Adapter** — адаптер номеров
- **Nfc Key Adapter** — адаптер NFC ключей
- **Paid Services Adapter** — адаптер оплаченных услуг
- **Restaurant Menu** — меню ресторанов
- **Restaurant Markers** — маркеры ресторанов на карте
- **Events** — мероприятия отеля
- **Mock Data** — тестовые данные
- **Mock Services** — тестовые услуги
- **NFC Notification** — уведомления NFC
- **Date/Time Formats** — форматы дат и времени
- **User Preferences** — настройки пользователя
- **XLS Parser** — парсер Excel
- **Package Name** — имя пакета (для тестов)

**Всего:** ~150 строк

---

### 2. Layout-файлы — обновление

#### fragment_booking.xml
```xml
<!-- БЫЛО -->
app:title="Book a Room"
android:text="1. Select a Room"
android:hint="Check-in"
android:text="Book Now"

<!-- СТАЛО -->
app:title="@string/booking_toolbar_title"
android:text="@string/booking_step_select_room"
android:hint="@string/booking_hint_check_in"
android:text="@string/booking_button_book"
```

#### fragment_payment.xml
```xml
<!-- БЫЛО -->
app:title="Payment"
android:text="Оплата через: 5с"
android:text="Сумма: $0"
android:text="Оплатить"

<!-- СТАЛО -->
app:title="@string/payment_toolbar_title"
android:text="@string/payment_timer_format"
android:text="@string/payment_amount_format"
android:text="@string/payment_button_pay"
```

#### fragment_dashboard.xml
```xml
<!-- БЫЛО -->
android:text="Welcome Home,"
android:text="My Key"
android:text="Hotel information"
android:text="Recommended for you"

<!-- СТАЛО -->
android:text="@string/dashboard_welcome"
android:text="@string/dashboard_quick_action_key"
android:text="@string/dashboard_hotel_info_button"
android:text="@string/dashboard_recommended_title"
```

---

### 3. Kotlin-файлы — обновление

#### BookingFragment.kt
```kotlin
// БЫЛО
binding.tvSummaryRoom.text = "Room: ${room.type} #$roomNumber"
binding.tvSummaryDates.text = "Dates: $checkIn - $checkOut"
binding.tvSummaryPrice.text = "Total: $${state.totalPrice.toInt()}"
.setTitle("Booking Confirmed!")
.setPositiveButton("View My Key") { _, _ -> ... }

// СТАЛО
binding.tvSummaryRoom.text = getString(R.string.booking_summary_room_format, room.type, roomNumber)
binding.tvSummaryDates.text = getString(R.string.booking_summary_dates_format, checkIn, checkOut)
binding.tvSummaryPrice.text = getString(R.string.booking_summary_total_format, state.totalPrice.toInt())
.setTitle(R.string.booking_dialog_confirmed_title)
.setPositiveButton(R.string.booking_dialog_view_key) { _, _ -> ... }
```

#### BookingViewModel.kt
```kotlin
// БЫЛО
_event.emit(BookingEvent.ValidationError("Please select a room"))
_event.emit(BookingEvent.ValidationError("Please select check-in and check-out dates"))
_event.emit(BookingEvent.ValidationError("Please enter guest name"))

// СТАЛО
_event.emit(BookingEvent.ValidationError(ResourceProvider.getString(R.string.booking_error_no_room)))
_event.emit(BookingEvent.ValidationError(ResourceProvider.getString(R.string.booking_error_no_dates)))
_event.emit(BookingEvent.ValidationError(ResourceProvider.getString(R.string.booking_error_no_guest_name)))
```

---

### 4. ResourceProvider — утилита для доступа к строкам

Создан объект `ResourceProvider` для получения строк из ViewModel и других non-UI классов:

```kotlin
// app/src/main/java/com/example/hotel_app/ResourceProvider.kt
object ResourceProvider {

    private val context: Context
        get() = HotelApplication.instance

    fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }

    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }
}
```

**Использование:**
```kotlin
// В ViewModel
val message = ResourceProvider.getString(R.string.booking_error_no_room)
val formatted = ResourceProvider.getString(R.string.booking_summary_total_format, price)
```

---

### 5. HotelApplication — добавлен instance

```kotlin
class HotelApplication : Application() {

    companion object {
        lateinit var instance: HotelApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // ...
    }
}
```

---

## Примеры использования строк

### Форматирование строк

```xml
<string name="booking_summary_room_format">Room: %1$s #%2$s</string>
<string name="booking_summary_total_format">Total: $%1$d</string>
<string name="payment_timer_format">Оплата через: %1$dс</string>
```

```kotlin
// Kotlin
getString(R.string.booking_summary_room_format, room.type, roomNumber)
getString(R.string.booking_summary_total_format, state.totalPrice.toInt())
```

### Многострочные сообщения

```xml
<string name="booking_dialog_confirmed_message">%1$s\n\nYour digital NFC key has been automatically created and is ready to use.</string>
```

```kotlin
// Kotlin
setMessage(getString(R.string.booking_dialog_confirmed_message, message))
```

---

## Структура strings.xml

```
res/values/strings.xml
├── App Name (1 строка)
├── NFC Key (10 строк)
├── Booking Fragment (18 строк)
├── Dashboard Fragment (13 строк)
├── Payment Fragment (8 строк)
├── Services Fragment (7 строк)
├── Reviews Fragment (5 строк)
├── Maps Fragment (8 строк)
├── Hotel Info Fragment (2 строки)
├── Room Adapter (4 строки)
├── Nfc Key Adapter (3 строки)
├── Paid Services Adapter (2 строки)
├── Restaurant Menu (18 строк)
├── Restaurant Markers (15 строк)
├── Events (30 строк)
├── Mock Data (12 строк)
├── Mock Services (12 строк)
├── NFC Notification (5 строк)
├── Date/Time Formats (4 строки)
├── User Preferences (3 строки)
├── XLS Parser (1 строка)
└── Package Name (1 строка)

Итого: ~182 строки
```

---

## Преимущества нового подхода

| Аспект | ❌ БЫЛО | ✅ СТАЛО |
|--------|---------|----------|
| **Локализация** | Невозможна без переписывания кода | Достаточно добавить strings.xml для другого языка |
| **Поиск строк** | Нужно искать по всему коду | Все строки в одном файле |
| **Изменение текста** | Правка в коде, перекомпиляция | Правка в XML, иногда без перекомпиляции |
| **Форматирование** | Ручная конкатенация | Стандартные placeholder `%1$s`, `%2$d` |
| **Консистентность** | Одинаковые строки могут отличаться | Одна строка — одно определение |
| **Доступ из ViewModel** | Требует Context | ResourceProvider.getString() |

---

## Поддержка многоязычности

Для добавления нового языка:

1. Создать папку с локалью: `res/values-es/` (испанский)
2. Создать `strings.xml` с тем же набором строк:

```xml
<!-- res/values-es/strings.xml -->
<resources>
    <string name="app_name">Hotel_App</string>
    <string name="booking_toolbar_title">Reservar una habitación</string>
    <string name="booking_button_book">Reservar ahora</string>
    <!-- ... остальные строки ... -->
</resources>
```

3. Android автоматически выберет нужный файл в зависимости от языка устройства.

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

### Созданные:
- `app/src/main/java/com/example/hotel_app/ResourceProvider.kt` — утилита для доступа к строкам

### Обновлённые:
- `app/src/main/res/values/strings.xml` — ~182 строки
- `app/src/main/res/layout/fragment_booking.xml` — 10 строк вынесено
- `app/src/main/res/layout/fragment_payment.xml` — 5 строк вынесено
- `app/src/main/res/layout/fragment_dashboard.xml` — 12 строк вынесено
- `app/src/main/java/com/example/hotel_app/HotelApplication.kt` — добавлен instance
- `app/src/main/java/com/example/hotel_app/presentation/viewmodel/BookingViewModel.kt` — 3 строки вынесено
- `app/src/main/java/com/example/hotel_app/presentation/ui/fragments/BookingFragment.kt` — 6 строк вынесено

---

## Рекомендации для будущей разработки

1. **Все новые строки сразу добавлять в strings.xml**
2. **Использовать формат именования:**
   - `<экран>_<элемент>_<назначение>` (например: `booking_button_book`)
   - `<категория>_<тип>_<формат>` (например: `payment_amount_format`)
3. **Избегать дублирования строк** — если строка используется в нескольких местах, определить один раз
4. **Использовать placeholder для переменных** — `%1$s`, `%2$d` вместо конкатенации
5. **Для многострочных текстов использовать `\n\n`** для разделения абзацев
