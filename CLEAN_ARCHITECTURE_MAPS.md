# Карта в Data Слое (Clean Architecture Refactoring)

## Цель
Переместить логику работы с картой из presentation слоя в data слой для соблюдения принципов Clean Architecture.

---

## Проблема (БЫЛО)

### ❌ MapsViewModel.kt (НЕПРАВИЛЬНО)

```kotlin
class MapsViewModel : ViewModel() {
    // ❌ Проблема: Данные карты в ViewModel (presentation слой)
    val hotelLocation = Location(55.751244, 37.618423)
    
    // ❌ Моковые данные в presentation слое
    private fun getRestaurantMarkers(): List<RestaurantMarker> = listOf(
        RestaurantMarker(
            id = "1",
            name = "Кафе Пушкинъ",
            cuisine = "Русская",
            // ... данные
        ),
        // ...
    )
}

// ❌ Модели данных в presentation слое
data class RestaurantMarker(
    val id: String,
    val name: String,
    // ...
)

data class Location(
    val latitude: Double,
    val longitude: Double
)
```

**Проблемы:**
1. **Моковые данные в ViewModel** — бизнес-логика смешана с presentation
2. **Модели в presentation слое** — нарушение зависимости (domain должен быть независимым)
3. **Невозможно заменить источник данных** — данные захардкожены в ViewModel
4. **Сложно тестировать** — нужно тестировать ViewModel с моковыми данными
5. **Нарушение Clean Architecture** — presentation слой знает о данных

---

## Решение (СТАЛО)

### ✅ Clean Architecture Layers

```
┌─────────────────────────────────────┐
│     Presentation Layer (UI)         │
│  ┌─────────────┐  ┌──────────────┐ │
│  │ MapsFragment│  │MapsViewModel │ │
│  └─────────────┘  └──────────────┘ │
│         ↓                ↓          │
└─────────┼────────────────┼─────────┘
          │                │ uses
          │         ┌──────┴──────┐
          │         │ Domain Layer│
          │         │ ┌─────────┐ │
          └─────────┼─│Location │ │
                    │ │Restaurant│ │
                    │ │ Marker  │ │
                    │ └─────────┘ │
                    │ ┌─────────┐ │
                    │ │HotelRepo│ │
                    │ └─────────┘ │
                    └──────┬──────┘
                           │ implements
                    ┌──────┴──────┐
                    │  Data Layer │
                    │ ┌─────────┐ │
                    │ │MockHotel│ │
                    │ │Repository│ │
                    │ └─────────┘ │
                    └─────────────┘
```

---

### 1. Domain Layer — Модели

#### RestaurantMarker.kt
```kotlin
package com.example.hotel_app.domain.model

/**
 * Модель местоположения (широта и долгота).
 * Используется для отображения объектов на карте.
 */
data class Location(
    val latitude: Double,
    val longitude: Double
)

/**
 * Маркер ресторана на карте.
 * Содержит всю необходимую информацию для отображения и взаимодействия.
 */
data class RestaurantMarker(
    val id: String,
    val name: String,
    val cuisine: String,
    val rating: Double,
    val distance: Double, // km from hotel
    val coordinates: Location,
    val address: String,
    val phone: String? = null
) {
    /**
     * Форматированная информация для сниппета маркера на карте.
     */
    fun getSnippet(): String = "${cuisine} • ${rating}★ • ${distance} км"

    /**
     * Проверка, доступен ли ресторан (рейтинг > 0).
     */
    fun isAvailable(): Boolean = rating > 0
}
```

**Преимущества:**
- ✅ Модели в domain слое — независимы от UI и фреймворков
- ✅ Бизнес-логика в моделях (`getSnippet()`, `isAvailable()`)
- ✅ Переиспользуемость — могут использоваться из любого слоя

---

### 2. Domain Layer — Repository Interface

#### HotelRepository.kt
```kotlin
interface HotelRepository {
    // ... существующие методы
    
    // ✅ Map & Restaurants — методы в интерфейсе
    fun getHotelLocation(): Location
    fun getRestaurantMarkers(): Flow<List<RestaurantMarker>>
    suspend fun getRouteToRestaurant(markerId: String): String?
    suspend fun callRestaurant(markerId: String): String?
}
```

**Преимущества:**
- ✅ Интерфейс в domain слое — определяет контракт
- ✅ Абстракция от источника данных (сеть, БД, моки)
- ✅ Легко заменить реализацию (Mock → Real)

---

### 3. Data Layer — Repository Implementation

#### MockHotelRepository.kt
```kotlin
class MockHotelRepository(
    private val reviewDao: ReviewDao
) : HotelRepository {

    // ✅ Данные в data слое
    override fun getHotelLocation(): Location {
        // Центр Москвы (моковая локация отеля)
        return Location(55.751244, 37.618423)
    }

    override fun getRestaurantMarkers(): Flow<List<RestaurantMarker>> = 
        MutableStateFlow(
            listOf(
                RestaurantMarker(
                    id = "1",
                    name = "Кафе Пушкинъ",
                    cuisine = "Русская",
                    rating = 4.8,
                    distance = 0.5,
                    coordinates = Location(55.760244, 37.605423),
                    address = "Тверской бульвар, 26А",
                    phone = "+7 (495) 123-45-67"
                ),
                // ... другие рестораны
            )
        )

    override suspend fun getRouteToRestaurant(markerId: String): String? {
        delay(500) // Имитация загрузки маршрута
        return "Маршрут построен до ресторана"
    }

    override suspend fun callRestaurant(markerId: String): String? {
        delay(300) // Имитация звонка
        return "Звонок инициирован"
    }
}
```

**Преимущества:**
- ✅ Данные в data слое — правильное разделение ответственности
- ✅ Flow для реактивных обновлений
- ✅ Suspending функции для асинхронных операций
- ✅ Легко заменить на RealHotelRepository с API

---

### 4. Presentation Layer — ViewModel

#### MapsViewModel.kt
```kotlin
/**
 * ViewModel для экрана карты.
 * 
 * ## Clean Architecture:
 * - Данные находятся в data слое (MockHotelRepository)
 * - Модели находятся в domain слое (RestaurantMarker, Location)
 * - ViewModel только управляет UI состоянием
 */
class MapsViewModel(private val repository: HotelRepository) : ViewModel() {

    // ✅ Данные из repository (data слой)
    private val _markers = MutableStateFlow<List<RestaurantMarker>>(emptyList())
    val markers: StateFlow<List<RestaurantMarker>> = _markers.asStateFlow()

    private val _selectedMarker = MutableStateFlow<RestaurantMarker?>(null)
    val selectedMarker: StateFlow<RestaurantMarker?> = _selectedMarker.asStateFlow()

    // ✅ Локация отеля из repository (data слой)
    val hotelLocation: Location = repository.getHotelLocation()

    init {
        loadRestaurantMarkers()
    }

    /**
     * Загрузка маркеров ресторанов из repository.
     */
    private fun loadRestaurantMarkers() {
        viewModelScope.launch {
            repository.getRestaurantMarkers().collect { markers ->
                _markers.value = markers
            }
        }
    }

    /**
     * Выбор маркера ресторана.
     */
    fun selectMarker(marker: RestaurantMarker) {
        _selectedMarker.value = marker
    }

    /**
     * Построение маршрута до ресторана.
     */
    fun buildRouteToRestaurant() {
        viewModelScope.launch {
            selectedMarker.value?.let { marker ->
                repository.getRouteToRestaurant(marker.id)
            }
        }
    }

    /**
     * Звонок в ресторан.
     */
    fun callRestaurant() {
        viewModelScope.launch {
            selectedMarker.value?.let { marker ->
                repository.callRestaurant(marker.id)
            }
        }
    }
}
```

**Преимущества:**
- ✅ Нет моковых данных — только бизнес-логика UI
- ✅ Зависит от абстракции (HotelRepository)
- ✅ Легко тестировать — можно подменить Repository
- ✅ Чистая ответственность — управление UI состоянием

---

### 5. Presentation Layer — Fragment

#### MapsFragment.kt
```kotlin
class MapsFragment : Fragment(R.layout.fragment_maps), OnMapReadyCallback {

    private val viewModel: MapsViewModel by viewModel()

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Add hotel marker
        val hotelLocation = LatLng(viewModel.hotelLocation.latitude, viewModel.hotelLocation.longitude)
        map.addMarker(
            MarkerOptions()
                .position(hotelLocation)
                .title("Grand Hotel")
                .snippet("Ваш отель")
        )

        // Add restaurant markers
        viewModel.markers.value.forEach { marker ->
            val position = LatLng(marker.coordinates.latitude, marker.coordinates.longitude)
            map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(marker.name)
                    .snippet(marker.getSnippet()) // ✅ Метод из domain модели
            )
        }

        // Set map click listener
        map.setOnMarkerClickListener { marker ->
            val clickedMarker = viewModel.markers.value.find {
                it.coordinates.latitude == marker.position.latitude && 
                it.coordinates.longitude == marker.position.longitude
            }
            clickedMarker?.let {
                viewModel.selectMarker(it)
            }
            false
        }
    }

    private fun setupListeners() {
        binding.btnRoute.setOnClickListener {
            viewModel.buildRouteToRestaurant() // ✅ Вызов метода ViewModel
        }

        binding.btnCall.setOnClickListener {
            viewModel.callRestaurant() // ✅ Вызов метода ViewModel
        }
    }
}
```

**Преимущества:**
- ✅ Fragment знает только о ViewModel
- ✅ Нет прямой зависимости от Repository
- ✅ UI логика во Fragment, бизнес-логика в ViewModel

---

### 6. Dependency Injection

#### AppModule.kt
```kotlin
val appModule = module {
    // Repository
    single<HotelRepository> { MockHotelRepository(get()) }

    // ViewModels
    viewModel { MapsViewModel(get()) } // ✅ Inject HotelRepository
}
```

---

## Сравнение подходов

| Аспект | ❌ БЫЛО | ✅ СТАЛО |
|--------|---------|----------|
| **Модели** | В presentation слое | В domain слое |
| **Данные** | В ViewModel (mocks) | В Repository (data layer) |
| **Зависимости** | ViewModel → данные | ViewModel → Repository → данные |
| **Тестируемость** | Сложно (mocks в ViewModel) | Легко (mock Repository) |
| **Замена источника** | Переписывать ViewModel | Заменить Repository |
| **Clean Architecture** | Нарушена | Соблюдена |

---

## Преимущества нового подхода

### 1. Разделение ответственности
- **Domain**: Модели и бизнес-правила
- **Data**: Источник данных (API, БД, моки)
- **Presentation**: UI логика

### 2. Тестируемость
```kotlin
// ✅ Легко тестировать ViewModel с mock Repository
@Test
fun `load markers from repository`() = runTest {
    val mockRepo = MockHotelRepository()
    val viewModel = MapsViewModel(mockRepo)
    
    viewModel.markers.test {
        expectMostRecentItem().size shouldBe 5
    }
}
```

### 3. Заменяемость
```kotlin
// ✅ Легко заменить на реальный API
class RealHotelRepository(
    private val api: HotelApi
) : HotelRepository {
    
    override fun getRestaurantMarkers(): Flow<List<RestaurantMarker>> = 
        flow {
            val response = api.getRestaurants()
            emit(response.toDomainModels())
        }
}
```

### 4. Переиспользование
```kotlin
// ✅ Модели из domain можно использовать везде
// Domain models в Repository
// Domain models в ViewModel
// Domain models в UI
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

## Изменённые файлы

### Созданные:
1. `domain/model/RestaurantMarker.kt` — модели Location и RestaurantMarker с бизнес-логикой

### Обновлённые:
1. `domain/repository/HotelRepository.kt` — добавлены методы для карты
2. `data/repository/MockHotelRepository.kt` — реализация методов карты + данные
3. `presentation/viewmodel/MapsViewModel.kt` — убраны моковые данные, добавлен Repository
4. `presentation/ui/fragments/MapsFragment.kt` — обновление для использования новых API
5. `di/AppModule.kt` — регистрация MapsViewModel с зависимостью

### Удалённые:
- Модели Location и RestaurantMarker из MapsViewModel.kt

---

## Рекомендации для будущей разработки

1. **Всегда размещайте модели в domain слое** — они независимы от UI и фреймворков
2. **Данные только в data слое** — ViewModel не должна знать о источнике данных
3. **Repository в domain слое** — интерфейс определяет контракт
4. **Реализация Repository в data слое** — API, БД, моки
5. **ViewModel зависит от абстракции** — HotelRepository, не от реализации
6. **Используйте Flow для реактивности** — данные обновляются автоматически
7. **Бизнес-логика в моделях** — `getSnippet()`, `isAvailable()`

---

## Clean Architecture Principles

```
┌─────────────────┐
│   Presentation  │  ← Зависит от Domain
├─────────────────┤
│     Domain      │  ← НЕ ЗАВИСИТ НИ ОТ КОГО
├─────────────────┤
│      Data       │  ← Зависит от Domain
└─────────────────┘

Dependencies point inward!
```

**Правило:** Domain слой НЕ ДОЛЖЕН зависеть от Presentation или Data слоёв.
