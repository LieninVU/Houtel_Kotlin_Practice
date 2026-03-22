# Анализ MVVM + Clean Architecture

## Текущее состояние проекта

### ✅ Что уже правильно

#### 1. Разделение на слои
```
app/src/main/java/com/example/hotel_app/
├── domain/           ✅ Domain слой (бизнес-логика)
│   ├── model/        ✅ Модели данных
│   └── repository/   ✅ Интерфейсы репозиториев
├── data/             ✅ Data слой (данные)
│   ├── local/        ✅ Локальные данные (Room, Preferences)
│   ├── parser/       ✅ Парсеры
│   ├── preferences/  ✅ Настройки
│   └── repository/   ✅ Реализации репозиториев
└── presentation/     ✅ Presentation слой (UI)
    ├── ui/           ✅ Fragment, Adapter
    └── viewmodel/    ✅ ViewModel
```

#### 2. Модели в domain слое ✅
```kotlin
// domain/model/Models.kt
data class User(...)
data class Room(...)
data class Booking(...)
data class RestaurantMarker(...) // ✅ Перемещено в domain
```

#### 3. Repository интерфейс в domain ✅
```kotlin
// domain/repository/HotelRepository.kt
interface HotelRepository {
    fun getRooms(): Flow<List<Room>>
    suspend fun bookRoom(...): BookingResult
}
```

#### 4. MapsViewModel исправлен ✅
```kotlin
// presentation/viewmodel/MapsViewModel.kt
class MapsViewModel(private val repository: HotelRepository) : ViewModel() {
    val hotelLocation: Location = repository.getHotelLocation() // ✅ Из repository
}
```

---

## ❌ Нарушения Clean Architecture

### ❌ Проблема 1: Моковые данные в ViewModel

**RestaurantViewModel.kt**
```kotlin
class RestaurantViewModel : ViewModel() {
    // ❌ Моковые данные в presentation слое!
    private fun getMenuItems(): List<MenuItem> = listOf(
        MenuItem("1", "Цезарь с курицей", ...),
        MenuItem("2", "Борщ московский", ...)
    )
}
```

**Проблема:**
- Данные должны быть в data слое
- ViewModel должна зависеть от Repository
- Невозможно заменить источник данных

**Решение:**
```kotlin
// 1. Добавить MenuItem в domain/model
// 2. Добавить метод в HotelRepository
// 3. Создать MockRestaurantRepository в data
// 4. Inject Repository в ViewModel
```

---

### ❌ Проблема 2: Бизнес-логика в ViewModel

**PaymentViewModel.kt**
```kotlin
class PaymentViewModel : ViewModel() {
    fun processPayment() {
        // ❌ Бизнес-логика в ViewModel!
        viewModelScope.launch {
            delay(2000)
            _paymentResult.value = PaymentResult.Success("Оплата прошла успешно!")
        }
    }
}
```

**Проблема:**
- ViewModel должна только координировать UI
- Бизнес-логика должна быть в domain UseCase
- Сложно тестировать

**Решение:**
```kotlin
// 1. Создать UseCase в domain/usecase/
class ProcessPaymentUseCase(private val repository: HotelRepository) {
    suspend operator fun invoke(amount: Double): PaymentResult {
        return repository.processPayment(amount)
    }
}

// 2. ViewModel использует UseCase
class PaymentViewModel(
    private val processPaymentUseCase: ProcessPaymentUseCase
) : ViewModel() {
    fun processPayment(amount: Double) {
        viewModelScope.launch {
            val result = processPaymentUseCase(amount)
            _paymentResult.value = result
        }
    }
}
```

---

### ❌ Проблема 3: Нет UseCase слоя

**Текущая структура:**
```
ViewModel → Repository → Data
```

**Правильная структура Clean Architecture:**
```
ViewModel → UseCase → Repository → Data
```

**Преимущества UseCase:**
- Изолированная бизнес-логика
- Легко тестировать
- Переиспользование между ViewModel
- Явные зависимости

---

### ❌ Проблема 4: Repository создаёт модели

**MockHotelRepository.kt**
```kotlin
class MockHotelRepository : HotelRepository {
    // ✅ Хорошо: Данные в data слое
    override fun getRestaurantMarkers(): Flow<List<RestaurantMarker>> = 
        MutableStateFlow(listOf(
            RestaurantMarker(...) // ✅ Domain модель
        ))
}
```

**Это правильно!** Данные в data слое, модели в domain.

---

## План исправлений

### 🔧 Исправление 1: RestaurantViewModel

#### Шаг 1: Добавить MenuItem в domain/model

```kotlin
// domain/model/MenuItem.kt
data class MenuItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val icon: String
)
```

#### Шаг 2: Добавить метод в HotelRepository

```kotlin
// domain/repository/HotelRepository.kt
interface HotelRepository {
    // ... существующие методы
    fun getMenuItems(): Flow<List<MenuItem>>
    suspend fun placeOrder(order: Order): OrderResult
}
```

#### Шаг 3: Создать MockRestaurantRepository

```kotlin
// data/repository/MockRestaurantRepository.kt
class MockRestaurantRepository : HotelRepository {
    override fun getMenuItems(): Flow<List<MenuItem>> = 
        MutableStateFlow(listOf(
            MenuItem("1", "Цезарь с курицей", ...),
            MenuItem("2", "Борщ московский", ...)
        ))
}
```

#### Шаг 4: Обновить RestaurantViewModel

```kotlin
class RestaurantViewModel(
    private val repository: HotelRepository
) : ViewModel() {
    
    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems.asStateFlow()
    
    init {
        loadMenuItems()
    }
    
    private fun loadMenuItems() {
        viewModelScope.launch {
            repository.getMenuItems().collect { items ->
                _menuItems.value = items
            }
        }
    }
}
```

---

### 🔧 Исправление 2: PaymentViewModel

#### Шаг 1: Создать UseCase

```kotlin
// domain/usecase/ProcessPaymentUseCase.kt
class ProcessPaymentUseCase(
    private val repository: HotelRepository
) {
    suspend operator fun invoke(amount: Double): PaymentResult {
        return repository.processPayment(amount)
    }
}
```

#### Шаг 2: Добавить метод в HotelRepository

```kotlin
// domain/repository/HotelRepository.kt
interface HotelRepository {
    // ...
    suspend fun processPayment(amount: Double): PaymentResult
}
```

#### Шаг 3: Реализовать в MockHotelRepository

```kotlin
// data/repository/MockHotelRepository.kt
override suspend fun processPayment(amount: Double): PaymentResult {
    delay(1000) // Имитация обработки
    return PaymentResult.Success("Оплата прошла успешно!")
}
```

#### Шаг 4: Обновить PaymentViewModel

```kotlin
class PaymentViewModel(
    private val processPaymentUseCase: ProcessPaymentUseCase
) : ViewModel() {
    
    fun processPayment(amount: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = processPaymentUseCase(amount)
            _paymentResult.value = result
            _isLoading.value = false
        }
    }
}
```

---

## Рекомендуемая структура

### Полная структура Clean Architecture

```
app/src/main/java/com/example/hotel_app/
│
├── domain/
│   ├── model/
│   │   ├── User.kt
│   │   ├── Room.kt
│   │   ├── Booking.kt
│   │   ├── MenuItem.kt          🔧 Добавить
│   │   └── RestaurantMarker.kt  ✅ Уже есть
│   │
│   ├── repository/
│   │   └── HotelRepository.kt   🔧 Добавить методы
│   │
│   └── usecase/                 🔧 Создать
│       ├── ProcessPaymentUseCase.kt
│       ├── GetMenuItemsUseCase.kt
│       ├── PlaceOrderUseCase.kt
│       └── LoadRoomsUseCase.kt
│
├── data/
│   ├── repository/
│   │   ├── MockHotelRepository.kt  🔧 Добавить методы
│   │   └── MockRestaurantRepository.kt 🔧 Создать
│   └── ...
│
└── presentation/
    ├── viewmodel/
    │   ├── PaymentViewModel.kt     🔧 Использовать UseCase
    │   ├── RestaurantViewModel.kt  🔧 Использовать Repository
    │   └── ...
    └── ...
```

---

## Приоритеты исправлений

### 🔴 Критично (нарушение архитектуры)

1. **RestaurantViewModel** — данные в ViewModel
2. **PaymentViewModel** — бизнес-логика в ViewModel
3. **Нет UseCase слоя** — сложно масштабировать

### 🟡 Желательно (улучшение)

4. **Добавить UseCase для всех операций**
5. **Разделить Repository на специализированные**
6. **Добавить маппинг Entity → Domain**

### 🟢 Опционально (best practices)

7. **Добавить Result тип для ошибок**
8. **Добавить тесты для UseCase**
9. **Добавить кэширование в Data слое**

---

## Минимальные изменения (Quick Fix)

Если нужно быстро исправить без полной переработки:

### 1. RestaurantViewModel — переместить данные

```kotlin
// ❌ БЫЛО
class RestaurantViewModel : ViewModel() {
    private fun getMenuItems(): List<MenuItem> = listOf(...) // В ViewModel
}

// ✅ СТАЛО (минимальное изменение)
class RestaurantViewModel(
    private val repository: HotelRepository // Inject
) : ViewModel() {
    // Данные загружаются из Repository
    init {
        viewModelScope.launch {
            repository.getMenuItems().collect { items ->
                _menuItems.value = items
            }
        }
    }
}
```

### 2. PaymentViewModel — вынести логику

```kotlin
// ❌ БЫЛО
class PaymentViewModel : ViewModel() {
    fun processPayment() {
        // Логика в ViewModel
        delay(2000)
        _paymentResult.value = PaymentResult.Success(...)
    }
}

// ✅ СТАЛО (минимальное изменение)
class PaymentViewModel(
    private val repository: HotelRepository // Inject
) : ViewModel() {
    fun processPayment() {
        viewModelScope.launch {
            // Логика в Repository
            val result = repository.processPayment()
            _paymentResult.value = result
        }
    }
}
```

---

## Сборка проекта

```bash
# Windows (PowerShell)
$env:JAVA_HOME='C:\Program Files\Java\jdk-21'
.\gradlew.bat assembleDebug

# Статус: ✅ BUILD SUCCESSFUL
```

---

## Выводы

### ✅ Что уже хорошо

1. **Слои разделены** — domain, data, presentation
2. **Модели в domain** — RestaurantMarker, User, Room
3. **Repository интерфейс в domain** — HotelRepository
4. **MapsViewModel исправлен** — данные из Repository

### ❌ Что нужно исправить

1. **RestaurantViewModel** — переместить данные в Repository
2. **PaymentViewModel** — вынести логику в UseCase/Repository
3. **Добавить UseCase слой** — для бизнес-логики

### 📋 План действий

1. **Создать MenuItem в domain** — 5 минут
2. **Добавить методы в HotelRepository** — 10 минут
3. **Реализовать в MockHotelRepository** — 15 минут
4. **Обновить ViewModel** — 20 минут
5. **Создать UseCase** — 30 минут
6. **Протестировать** — 15 минут

**Итого:** ~1.5 часа на полный рефакторинг
