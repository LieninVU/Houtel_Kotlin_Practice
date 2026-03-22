# Рефакторинг Flow в Booking Flow: Hot vs Cold

## Проблема (БЫЛО)

### ❌ Антипаттерны в BookingViewModel.kt

```kotlin
// 6 отдельных MutableStateFlow для простого состояния
private val _rooms = MutableStateFlow<List<Room>>(emptyList())
private val _isLoading = MutableStateFlow(false)
private val _bookingResult = MutableStateFlow<Result<String>?>(null)
private val _selectedRoom = MutableStateFlow<Room?>(null)
private val _checkInDate = MutableStateFlow("")
private val _checkOutDate = MutableStateFlow("")

// Проблемы:
// 1. Смешанные состояния — нужно следить за 6 потоками одновременно
// 2. Нет гарантий согласованности (например, rooms загружены, но isLoading ещё false)
// 3. Business-логика размазана по методам
// 4. Fragment напрямую вызывает методы ViewModel (imperative подход)
```

### ❌ Проблемы в BookingFragment.kt

```kotlin
// 4 отдельных collect для состояния
launch { viewModel.rooms.collect { ... } }
launch { viewModel.isRoomsLoading.collect { ... } }
launch { viewModel.isLoading.collect { ... } }
launch { viewModel.bookingEvent.collect { ... } }

// Проблемы:
// 1. Много подписок — сложно управлять
// 2. Состояние может рассинхронизироваться
// 3. Прямые вызовы методов: viewModel.selectRoom(), viewModel.setCheckInDate()
```

---

## Решение (СТАЛО)

### ✅ MVI-подобный подход с единым состоянием

#### 1. BookingUiState — immutable состояние UI

```kotlin
data class BookingUiState(
    val rooms: List<Room> = emptyList(),
    val selectedRoom: Room? = null,
    val checkInDate: String? = null,
    val checkOutDate: String? = null,
    val guestName: String = "",
    val isRoomsLoading: Boolean = false,
    val isBookingLoading: Boolean = false,
    val error: String? = null
) {
    // ✅ Валидация инкапсулирована в состоянии
    val isFormValid: Boolean
        get() = selectedRoom != null &&
                !checkInDate.isNullOrBlank() &&
                !checkOutDate.isNullOrBlank() &&
                guestName.isNotBlank()

    // ✅ Бизнес-логика расчёта в состоянии
    val totalPrice: Double
        get() { /* расчёт стоимости */ }
}
```

**Преимущества:**
- Все данные в одном месте — согласованность гарантирована
- Immutable — предсказуемые изменения
- Вычисляемые свойства — логика валидации и расчётов в одном месте

---

#### 2. BookingAction — события от UI (Intent)

```kotlin
sealed class BookingAction {
    data class SelectRoom(val room: Room) : BookingAction()
    data class SetCheckInDate(val date: String) : BookingAction()
    data class SetCheckOutDate(val date: String) : BookingAction()
    data class SetGuestName(val name: String) : BookingAction()
    data object CreateBooking : BookingAction()
    data object LoadRooms : BookingAction()
    data object ClearError : BookingAction()
}
```

**Преимущества:**
- Явные намерения UI — что пользователь хотел сделать
- Легко тестировать — просто создать Action и проверить состояние
- Трассировка — можно логировать все действия

---

#### 3. BookingEvent — одноразовые события от ViewModel

```kotlin
sealed class BookingEvent {
    data class BookingSuccess(val booking: Booking, val nfcKey: NfcKey, val message: String) : BookingEvent()
    data class NavigateToPayment(val booking: Booking, val nfcKey: NfcKey, val amount: Double) : BookingEvent()
    data class BookingError(val message: String) : BookingEvent()
    data class ValidationError(val message: String) : BookingEvent()
}
```

**Важно:** Используем `SharedFlow` для событий, которые должны быть обработаны **один раз** (навигация, тоасты).

---

#### 4. Обновлённая BookingViewModel

```kotlin
class BookingViewModel(private val repository: HotelRepository) : ViewModel() {

    // ✅ Единый StateFlow для всего состояния
    private val _state = MutableStateFlow(BookingUiState())
    val state: StateFlow<BookingUiState> = _state.asStateFlow()

    // ✅ SharedFlow только для одноразовых событий
    private val _event = MutableSharedFlow<BookingEvent>()
    val event: SharedFlow<BookingEvent> = _event.asSharedFlow()

    // ✅ Холодный Flow от репозитория — данные создаются по подписке
    val roomsFlow = repository.getAvailableRooms().map { it }

    // ✅ Единый метод обработки всех действий
    fun onAction(action: BookingAction) {
        when (action) {
            is BookingAction.SelectRoom -> _state.value = _state.value.copy(selectedRoom = action.room)
            is BookingAction.SetCheckInDate -> _state.value = _state.value.copy(checkInDate = action.date)
            is BookingAction.CreateBooking -> createBooking()
            // ...
        }
    }

    private fun createBooking() {
        val currentState = _state.value
        // Валидация через вычисляемые свойства состояния
        if (!currentState.isFormValid) { /* ошибка */ }
        // ...
    }
}
```

---

#### 5. Обновлённый BookingFragment

```kotlin
class BookingFragment : Fragment(R.layout.fragment_booking) {

    private val roomAdapter = RoomAdapter { room ->
        // ✅ MVI: отправляем действие, а не вызываем метод
        viewModel.onAction(BookingAction.SelectRoom(room))
        updateSelectedRoomInAdapter(room.id)
        scrollToBookingForm()
    }

    private fun setupGuestNameInput() {
        binding.etGuestName.doAfterTextChanged { text ->
            // ✅ MVI: отправляем действие
            viewModel.onAction(BookingAction.SetGuestName(text?.toString() ?: ""))
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // ✅ Холодный Flow для данных от репозитория
                launch { viewModel.roomsFlow.collect { rooms -> ... } }

                // ✅ Один collect для всего UI-состояния
                launch {
                    viewModel.state.collect { state ->
                        binding.progressRooms.isVisible = state.isRoomsLoading
                        binding.btnBook.isEnabled = state.isFormValid && !state.isBookingLoading
                        updateSummary(state)
                    }
                }

                // ✅ SharedFlow для одноразовых событий
                launch { viewModel.event.collect { event -> ... } }
            }
        }
    }
}
```

---

## Сравнение подходов

| Аспект | ❌ БЫЛО | ✅ СТАЛО |
|--------|---------|----------|
| **Состояние** | 6 отдельных StateFlow | 1 StateFlow<BookingUiState> |
| **Изменения** | Прямые вызовы методов | onAction(BookingAction) |
| **Валидация** | Отдельный метод `isFormValid(guestName)` | Свойство `state.isFormValid` |
| **Расчёт цены** | Отдельный метод `calculateTotalPrice()` | Свойство `state.totalPrice` |
| **События UI** | `bookingEvent: SharedFlow<BookingUiEvent>` | `event: SharedFlow<BookingEvent>` |
| **Flow от репозитория** | Не использовался напрямую | `roomsFlow: Flow<List<Room>>` (холодный) |
| **Подписки во Fragment** | 4 отдельных collect | 3 collect (roomsFlow, state, event) |

---

## Горячие vs Холодные Flow: когда что использовать

### ❌ НЕПРАВИЛЬНО (было)

```kotlin
// MutableStateFlow для данных, которые можно сделать холодным Flow
private val _rooms = MutableStateFlow<List<Room>>(emptyList())

init {
    viewModelScope.launch {
        repository.getAvailableRooms().collect { rooms ->
            _rooms.value = rooms  // Лишняя прослойка
        }
    }
}
```

### ✅ ПРАВИЛЬНО (стало)

```kotlin
// Холодный Flow от репозитория — данные создаются по подписке
val roomsFlow = repository.getAvailableRooms()

// В UI:
lifecycleScope.launch {
    viewModel.roomsFlow.collect { rooms ->
        adapter.submitList(rooms)
    }
}
```

### Когда использовать StateFlow (горячий)

- **Состояние UI** (загрузка, ошибка, данные формы)
- **Кэшированные данные**, которые должны быть всегда в памяти
- **Общее состояние**, которое должно переживать пересоздание collector

### Когда использовать Flow (холодный)

- **Данные от репозитория** (БД, сеть)
- **Поток событий**, который начинается по подписке
- **Трансформации данных** (map, filter, combine)

### Когда использовать SharedFlow

- **Одноразовые события** (навигация, тоасты, диалоги)
- **События, которые не должны повторяться** при переподписке

---

## Преимущества нового подхода

1. **Предсказуемость** — состояние immutable, изменения только через copy
2. **Согласованность** — все данные в одном объекте, нет рассинхронизации
3. **Тестируемость** — легко создать состояние и проверить реакцию
4. **Читаемость** — явно видно, какие действия поддерживает UI
5. **Масштабируемость** — легко добавить новое поле в состояние
6. **Разделение ответственности** — ViewModel обрабатывает действия, Fragment рендерит состояние

---

## Сборка проекта

```bash
# Windows (PowerShell)
$env:JAVA_HOME='C:\Program Files\Java\jdk-21'
.\gradlew.bat assembleDebug

# Или с коротким путём
set JAVA_HOME=C:\PROGRA~1\Java\jdk-21
.\gradlew.bat assembleDebug
```

**Статус:** ✅ BUILD SUCCESSFUL

---

## Файлы изменений

- `app/src/main/java/com/example/hotel_app/presentation/viewmodel/BookingViewModel.kt` — полный рефакторинг
- `app/src/main/java/com/example/hotel_app/presentation/ui/fragments/BookingFragment.kt` — обновлён для работы с новым API
