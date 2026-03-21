# 🏨 Hotel App — Интегрированное Приложение

## ✅ СТАТУС ПРОЕКТА: ГОТОВО К СБОРКЕ

Все 8 функций из технического задания реализованы и интегрированы в ветку `main`.

---

## 📋 ФУНКЦИОНАЛЬНОСТЬ

| № | Функция | Статус | Описание |
|---|---------|--------|----------|
| 1 | **Бронирование** | ✅ | Выбор номера, даты, форма с валидацией |
| 2 | **Dashboard** | ✅ | Главный экран с быстрыми действиями |
| 3 | **NFC Ключ** | ✅ | Электронный ключ с эмуляцией |
| 4 | **Инфо об отеле** | ✅ | Контакты, расписание, удобства |
| 5 | **Каталог услуг** | ✅ | SPA, трансфер, еда, фильтрация |
| 6 | **Карты и ресторан** | ✅ | Google Maps, маркеры, меню |
| 7 | **Оплата** | ✅ | Таймер 5 сек, эмуляция платежа |
| 8 | **Отзывы** | ✅ | Room Database, рейтинг, список |

---

## 🏗️ АРХИТЕКТУРА

- **MVVM** + **Clean Architecture** (data/domain/presentation)
- **Dependency Injection**: Koin
- **Navigation Component**: граф навигации
- **Room Database**: локальное хранение отзывов
- **Google Maps SDK**: карты и маркеры
- **ViewBinding**: типобезопасная работа с views
- **Coroutines & Flow**: асинхронность

---

## 📁 СТРУКТУРА ПРОЕКТА

```
app/src/main/java/com/example/hotel_app/
├── data/
│   ├── local/           # Room Database
│   │   ├── AppDatabase.kt
│   │   ├── ReviewDao.kt
│   │   └── ReviewEntity.kt
│   └── repository/
│       └── MockHotelRepository.kt
├── domain/
│   ├── model/
│   │   └── Models.kt
│   └── repository/
│       └── HotelRepository.kt
├── di/
│   └── AppModule.kt     # Koin DI
├── presentation/
│   ├── ui/
│   │   ├── adapter/     # RecyclerView адаптеры
│   │   └── fragments/   # 8 Fragment
│   └── viewmodel/       # 8 ViewModel
├── HotelApplication.kt
└── MainActivity.kt
```

---

## 🚀 БЫСТРЫЙ СТАРТ

### 1. Требования
- **JDK 21** (обязательно)
- **Android Studio** Arctic Fox или новее
- **MinSDK**: 26 (Android 8.0)
- **TargetSDK**: 35

### 2. Установка JDK
```bash
# Скачать JDK 21
https://www.oracle.com/java/technologies/downloads/

# Или через winget (Windows)
winget install Oracle.JDK.21

# Или через SDKMAN (Linux/Mac)
sdk install java 21.0.1-oracle
```

### 3. Настройка JAVA_HOME
```bash
# Windows
set JAVA_HOME=C:\Program Files\Java\jdk-21

# Linux/Mac
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
```

### 4. Google Maps API Key
1. Откройте [Google Cloud Console](https://console.cloud.google.com/)
2. Создайте новый проект
3. Включите **Maps SDK for Android**
4. Создайте API ключ
5. Откройте `app/src/main/AndroidManifest.xml`
6. Замените:
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_GOOGLE_MAPS_API_KEY_HERE" />
```
на:
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="ВАШ_КЛЮЧ" />
```

### 5. Сборка
```bash
# Откройте проект в Android Studio
# Или через командную строку:

# Сборка отладочной версии
gradlew assembleDebug

# Установка на устройство
gradlew installDebug

# Сборка release версии
gradlew assembleRelease
```

---

## 📱 ЭКРАНЫ ПРИЛОЖЕНИЯ

### 1. Главная (Dashboard)
- Приветствие пользователя
- Быстрые действия: Ключ, Услуги, Поддержка
- Карточка текущего бронирования
- Спецпредложения

### 2. Бронирование
- Список доступных номеров
- Выбор дат заезда/выезда (DatePicker)
- Валидация формы
- Подтверждение бронирования

### 3. NFC Ключ
- Список электронных ключей
- Кнопка запроса нового ключа
- Эмуляция NFC (открыть/закрыть дверь)
- Статус ключа

### 4. Услуги
- Каталог услуг по категориям
- Фильтрация: SPA, Трансфер, Еда, Другое
- Поиск по названию и описанию
- Карточки услуг с иконками

### 5. Информация об отеле
- Контакты (адрес, телефон, email)
- Время заезда/выезда
- Расписание (завтрак, обед, ужин, SPA, gym)
- Список удобств

### 6. Карта
- Google Maps с маркерами
- Маркер отеля
- Маркеры ресторанов nearby
- Информация о ресторане
- Кнопки: Маршрут, Позвонить

### 7. Оплата
- Сумма платежа
- Таймер обратного отсчёта (5 сек)
- ProgressBar
- Результат оплаты

### 8. Отзывы
- Список отзывов из Room
- Форма нового отзыва
- RatingBar (1-5 звёзд)
- Сохранение в базу

---

## 🎯 НАВИГАЦИЯ

```
Dashboard (Главный экран)
├── KeyFragment (NFC Ключ)
├── ServicesFragment (Услуги)
├── BookingFragment (Бронирование)
├── HotelInfoFragment (Инфо об отеле)
├── MapsFragment (Карта)
├── PaymentFragment (Оплата)
└── ReviewsFragment (Отзывы)
```

**Bottom Navigation:**
- Главная
- Услуги
- Ключ
- Карта
- Инфо

---

## 🧪 ТЕСТИРОВАНИЕ

### Ручное тестирование
1. Запустите приложение
2. Проверьте навигацию через bottom menu
3. Протестируйте каждый экран:
   - Бронирование: выберите номер, даты, нажмите "Забронировать"
   - NFC: нажмите "Add Key", проверьте появление ключа
   - Услуги: примените фильтры, поиск
   - Оплата: нажмите "Оплатить", дождитесь таймера
   - Отзывы: заполните форму, отправьте
   - Карта: проверьте отображение маркеров

### Автоматические тесты
```bash
# Запуск unit-тестов
gradlew test

# Запуск instrumented-тестов
gradlew connectedAndroidTest
```

---

## 📊 СТАТИСТИКА КОДА

| Метрика | Значение |
|---------|----------|
| Всего файлов | 60+ |
| Строк кода | ~3500 |
| ViewModel | 8 |
| Fragment | 8 |
| Adapter | 4 |
| Layout XML | 15 |
| Room Entities | 1 |
| DAO | 1 |

---

## 🔧 ТЕХНОЛОГИИ

### Основные
- **Kotlin** 2.0.21
- **Android Gradle Plugin** 8.7.3

### Архитектура
- **MVVM** (Model-View-ViewModel)
- **Clean Architecture** (data/domain/presentation)
- **Repository Pattern**

### Библиотеки
- **Koin** 4.0.0 — Dependency Injection
- **Room** 2.6.1 — Local Database
- **Navigation Component** 2.8.5 — Навигация
- **Lifecycle & ViewModel** 2.8.7
- **Coroutines** 1.9.0 — Асинхронность
- **Flow** — Reactive streams
- **ViewBinding** — Работа с UI
- **Material Components** — UI компоненты

### Карты
- **Google Maps SDK** 18.2.0
- **Google Play Services Location** 21.0.1

### Утилиты
- **Retrofit** 2.11.0 — HTTP клиент (готов к API)
- **Gson** — JSON парсинг
- **Kotlin Faker** — Mock данные

---

## 📝 ОТЧЁТЫ

В проекте доступны два отчёта:

1. **ANALYSIS_REPORT.md** — анализ исходных feature-веток
2. **INTEGRATION_REPORT.md** — детальный отчёт об интеграции

---

## ⚠️ ИЗВЕСТНЫЕ ОГРАНИЧЕНИЯ

1. **Mock данные**: Все данные генерируются через Faker
2. **NFC эмуляция**: Кнопка вместо реального NFC
3. **Оплата**: Эмуляция без реального платежа
4. **Карты**: Требуется API ключ Google Maps

---

## 🎯 СЛЕДУЮЩИЕ УЛУЧШЕНИЯ

- [ ] Интеграция с реальным API
- [ ] Реальная NFC поддержка
- [ ] Платёжный шлюз (Stripe/CloudPayments)
- [ ] Push-уведомления
- [ ] Offline режим
- [ ] Unit-тесты (JUnit, MockK)
- [ ] UI-тесты (Espresso)
- [ ] CI/CD (GitHub Actions)

---

## 👥 КОМАНДА

Интеграция выполнена в рамках учебного проекта «Отель».

---

## 📄 ЛИЦЕНЗИЯ

Учебный проект. Все права защищены.

---

## 📞 ПОДДЕРЖКА

При возникновении проблем:
1. Проверьте наличие JDK 21
2. Настройте JAVA_HOME
3. Добавьте Google Maps API Key
4. Очистите проект: `Build → Clean Project`
5. Пересоберите: `Build → Rebuild Project`
