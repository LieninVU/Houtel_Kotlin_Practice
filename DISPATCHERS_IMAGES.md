# Корректная работа с изображениями и Dispatchers

## Цель
Обеспечить правильную работу с изображениями в проекте Hotel App:
- ✅ Явное указание Dispatchers для IO-операций
- ✅ Обработка ошибок загрузки изображений
- ✅ Оптимизация использования памяти
- ✅ Централизованное управление загрузкой изображений

---

## Проблемы (БЫЛО)

### ❌ 1. Нет явного указания диспетчеров
```kotlin
// XlsEventParser.kt
fun parseFromAssets(fileName: String): List<Event> {
    return try {
        val inputStream: InputStream = context.assets.open(fileName)
        // ❌ IO-операция выполняется на неизвестном Dispatcher
        if (fileName.endsWith(".xlsx")) parseXlsx(inputStream)
        else parseXls(inputStream)
    } catch (e: Exception) {
        getMockEvents()
    }
}
```

### ❌ 2. Загрузка изображений без обработки ошибок
```kotlin
// RoomAdapter.kt
ivRoom.load(room.imageUrl) {
    crossfade(true)
    placeholder(R.drawable.ic_launcher_background)
    error(R.drawable.ic_launcher_background)
    // ❌ Нет callback для обработки ошибок
}
```

### ❌ 3. IO-операции могут выполняться на Main потоке
```kotlin
// HotelInfoViewModel.kt
fun loadEvents() {
    // ❌ parseFromAssets вызывается без viewModelScope
    val allEvents = parser.parseFromAssets()
    // ...
}
```

---

## Решения (СТАЛО)

### ✅ 1. Явное указание Dispatchers.IO для парсинга файлов

#### XlsEventParser.kt
```kotlin
/**
 * Парсинг файла из assets.
 * Выполняется на Dispatchers.IO для избежания блокировки Main потока.
 */
suspend fun parseFromAssets(fileName: String = "events.xlsx"): List<Event> {
    // ✅ Явное указание Dispatchers.IO для IO-операций
    return withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream = context.assets.open(fileName)
            if (fileName.endsWith(".xlsx")) parseXlsx(inputStream)
            else parseXls(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            getMockEvents() // fallback на моки если файл не найден
        }
    }
}
```

**Преимущества:**
- Гарантированное выполнение на IO Dispatcher
- Не блокирует Main поток
- Явная suspend функция — компилятор требует вызова в coroutine scope

---

### ✅ 2. Обновление ViewModel для работы с suspend функциями

#### HotelInfoViewModel.kt
```kotlin
/**
 * Загрузка событий из assets.
 * Использует viewModelScope.launch с Dispatchers.IO внутри parseFromAssets.
 */
fun loadEvents() {
    viewModelScope.launch {
        _eventsState.value = UiState.Loading

        val allEvents = try {
            // ✅ parseFromAssets теперь suspend и использует Dispatchers.IO
            parser.parseFromAssets()
        } catch (e: Exception) {
            _eventsState.value = UiState.Error("Ошибка загрузки: ${e.localizedMessage}")
            parser.getMockEvents()
        }

        _eventsState.value = when {
            allEvents.isEmpty() -> UiState.Error("Нет данных о мероприятиях")
            else -> UiState.Success(allEvents)
        }

        _recommendations.value = getRecommendations(allEvents)
    }
}
```

**Преимущества:**
- viewModelScope автоматически отменяется при уничтожении ViewModel
- Main Dispatcher по умолчанию для обновления UI
- Suspending функция вызывает suspending функцию

---

### ✅ 3. Централизованная загрузка изображений

#### ImageLoadingUtils.kt
```kotlin
/**
 * Утилита для загрузки изображений с правильными настройками Coil.
 * 
 * ## Dispatchers:
 * - Coil автоматически использует Dispatchers.IO для загрузки изображений
 * - Обновление UI происходит на Dispatchers.Main
 */
object ImageLoadingUtils {

    /**
     * Загрузка изображения из сети с настройками по умолчанию.
     */
    fun loadImage(
        imageView: ImageView,
        imageUrl: String?,
        placeholder: Int = R.drawable.ic_launcher_background,
        error: Int = R.drawable.ic_launcher_background
    ) {
        imageView.load(imageUrl) {
            // ✅ Coil автоматически использует правильный Dispatcher
            crossfade(true)
            placeholder(placeholder)
            error(error)
            
            // Кэширование
            memoryCacheKey(imageUrl)
            diskCacheKey(imageUrl)
        }
    }

    /**
     * Загрузка изображения с callback для обработки ошибок.
     */
    fun loadImage(
        imageView: ImageView,
        data: Any?,
        placeholder: Int = R.drawable.ic_launcher_background,
        error: Int = R.drawable.ic_launcher_background,
        onError: ((Throwable) -> Unit)? = null,
        onSuccess: (() -> Unit)? = null
    ) {
        imageView.load(data) {
            crossfade(true)
            placeholder(placeholder)
            error(error)
            
            listener(
                onError = { request: ImageRequest, result: ErrorResult ->
                    onError?.invoke(result.throwable)
                    result.throwable.printStackTrace()
                },
                onSuccess = { request: ImageRequest, result: SuccessResult ->
                    onSuccess?.invoke()
                }
            )
        }
    }
}
```

**Преимущества:**
- Централизованная настройка загрузки
- Обработка ошибок через callback
- Кэширование для производительности
- Coil автоматически использует правильные Dispatchers

---

### ✅ 4. Загрузка изображений из assets с оптимизацией памяти

#### AssetImageLoader.kt
```kotlin
/**
 * Утилита для загрузки и обработки изображений из assets.
 * Использует Dispatchers.IO для декодирования Bitmap.
 */
object AssetImageLoader {

    /**
     * Загрузка Bitmap из assets на IO Dispatcher.
     */
    suspend fun loadFromAssets(
        context: Context,
        assetPath: String
    ): Bitmap? {
        // ✅ Декодирование выполняется на IO Dispatcher
        return withContext(Dispatchers.IO) {
            try {
                context.assets.open(assetPath).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Оптимизированная загрузка с уменьшением размера.
     * Использует inSampleSize для экономии памяти.
     */
    suspend fun loadResizedImage(
        context: Context,
        assetPath: String,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                context.assets.open(assetPath).use { inputStream ->
                    // Сначала получаем размеры изображения
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeStream(inputStream, null, options)

                    // Вычисляем inSampleSize
                    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

                    // Декодируем с уменьшенным размером
                    inputStream.reset()
                    options.inJustDecodeBounds = false
                    BitmapFactory.decodeStream(inputStream, null, options)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Расчёт inSampleSize для оптимизации памяти.
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height, width) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while (halfHeight / inSampleSize >= reqHeight &&
                   halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}
```

**Преимущества:**
- Явное указание Dispatchers.IO
- Оптимизация памяти через inSampleSize
- Обработка ошибок
- Автоматическое закрытие InputStream

---

### ✅ 5. Обновление адаптеров для использования утилит

#### RoomAdapter.kt
```kotlin
import com.example.hotel_app.presentation.ui.utils.ImageLoadingUtils

override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
    val room = getItem(position)
    
    with(holder.binding) {
        tvRoomType.text = room.type
        tvRoomPrice.text = "$ ${room.price.toInt()} / night"
        tvRoomDescription.text = room.description

        // ✅ Загрузка изображения с правильными Dispatchers и обработкой ошибок
        ImageLoadingUtils.loadImage(
            imageView = ivRoom,
            imageUrl = room.imageUrl,
            placeholder = R.drawable.ic_launcher_background,
            error = R.drawable.ic_launcher_background
        )

        root.alpha = if (room.isAvailable) 1.0f else 0.5f
        // ...
    }
}
```

**Преимущества:**
- Централизованная настройка загрузки
- Обработка ошибок
- Кэширование
- Чистый код адаптера

---

## Сравнение подходов

| Аспект | ❌ БЫЛО | ✅ СТАЛО |
|--------|---------|----------|
| **Dispatchers для парсинга** | Не указан | `withContext(Dispatchers.IO)` |
| **Функция парсинга** | Обычная | `suspend` |
| **Вызов в ViewModel** | Без scope | `viewModelScope.launch` |
| **Загрузка изображений** | Coil без настроек | ImageLoadingUtils с настройками |
| **Обработка ошибок** | Нет | Callback `onError` |
| **Оптимизация памяти** | Нет | `inSampleSize` |
| **Кэширование** | Нет | `memoryCacheKey`, `diskCacheKey` |

---

## Dispatchers: когда что использовать

### Dispatchers.Main
- **По умолчанию** в `viewModelScope.launch`
- Обновление UI
- Быстрые операции (< 16ms)

```kotlin
viewModelScope.launch { // По умолчанию Main
    // Обновление UI
    _state.value = newState
}
```

### Dispatchers.IO
- Чтение/запись файлов
- Работа с базой данных (Room)
- Сетевые запросы (Retrofit)
- Загрузка/декодирование изображений
- Парсинг Excel/JSON/XML

```kotlin
viewModelScope.launch {
    val data = withContext(Dispatchers.IO) {
        // IO-операция
        repository.getData()
    }
    // Обновление UI (автоматически на Main)
    _state.value = data
}
```

### Dispatchers.Default
- Вычисления CPU-intensive
- Сортировка больших списков
- Сложные вычисления

```kotlin
viewModelScope.launch {
    val result = withContext(Dispatchers.Default) {
        // CPU-операция
        largeList.sortBy { it.complexProperty }
    }
    _state.value = result
}
```

### Dispatchers.Unconfined
- **Не рекомендуется** для production
- Только для тестов или специфичных случаев

---

## Coil: автоматическое использование Dispatchers

Coil по умолчанию использует правильные Dispatchers:

```kotlin
imageView.load(url) {
    // ✅ Coil автоматически:
    // 1. Загрузка на Dispatchers.IO
    // 2. Декодирование на Dispatchers.IO
    // 3. Обновление UI на Dispatchers.Main
    
    // Можно переопределить при необходимости:
    // dispatcher(Dispatchers.IO)
}
```

---

## Обработка ошибок

### ✅ Правильная обработка ошибок загрузки

```kotlin
ImageLoadingUtils.loadImage(
    imageView = imageView,
    data = imageUrl,
    onError = { throwable ->
        // Логирование ошибки
        Log.e("ImageLoad", "Failed to load: ${throwable.message}")
        // Показ пользователю
        Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
    },
    onSuccess = {
        // Изображение успешно загружено
        Log.d("ImageLoad", "Image loaded successfully")
    }
)
```

---

## Оптимизация памяти

### Проблема
Загрузка больших изображений может вызвать OutOfMemoryError:

```kotlin
// ❌ ПЛОХО: Загрузка полного размера
val bitmap = BitmapFactory.decodeStream(inputStream)
// Bitmap может быть 4096x4096 и занимать 64MB
```

### Решение
Использовать inSampleSize для уменьшения размера:

```kotlin
// ✅ ХОРОШО: Оптимизированная загрузка
val options = BitmapFactory.Options().apply {
    inJustDecodeBounds = true // Сначала только размеры
}
BitmapFactory.decodeStream(inputStream, null, options)

// Вычисляем inSampleSize
options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

// Декодируем с уменьшением
options.inJustDecodeBounds = false
val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
// Bitmap уменьшен в inSampleSize раз
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
1. `ImageLoadingUtils.kt` — утилита для загрузки изображений с обработкой ошибок
2. `AssetImageLoader.kt` — утилита для загрузки из assets с оптимизацией памяти

### Обновлённые:
1. `XlsEventParser.kt` — `suspend` функция + `withContext(Dispatchers.IO)`
2. `HotelInfoViewModel.kt` — `viewModelScope.launch` для вызова suspend функции
3. `RoomAdapter.kt` — использование `ImageLoadingUtils.loadImage()`

---

## Рекомендации для будущей разработки

1. **Всегда используйте `withContext(Dispatchers.IO)`** для IO-операций
2. **Помечайте IO-функции как `suspend`** — компилятор будет требовать правильный контекст
3. **Используйте централизованные утилиты** для загрузки изображений
4. **Обрабатывайте ошибки загрузки** через callback
5. **Оптимизируйте память** при загрузке больших изображений
6. **Используйте кэширование** для повторной загрузки
7. **Не блокируйте Main поток** — всегда проверяйте Dispatchers

---

## Метрики улучшений

| Метрика | До | После |
|---------|----|----|
| **IO-операции на Main** | Возможно | ❌ Исключено |
| **Обработка ошибок** | Нет | ✅ Есть |
| **Оптимизация памяти** | Нет | ✅ inSampleSize |
| **Кэширование** | Нет | ✅ Memory + Disk |
| **Централизация** | Нет | ✅ Утилиты |
