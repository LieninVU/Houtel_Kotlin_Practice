# Исправление ошибки Google Maps API Key

## Проблема

```
android.view.InflateException: Error inflating class androidx.fragment.app.FragmentContainerView
Caused by: java.lang.IllegalStateException: API key not found.
Check that <meta-data android:name="com.google.android.geo.API_KEY" 
android:value="your API key"/> is in the <application> element of AndroidManifest.xml
```

## Решение

### ✅ Шаг 1: Получите Google Maps API Key

1. Перейдите на https://console.cloud.google.com/google/maps-apis/credentials
2. Создайте новый проект или выберите существующий
3. Включите "Maps SDK for Android"
4. Создайте API ключ

**Подробная инструкция:** См. `MAPS_API_KEY_SETUP.md`

### ✅ Шаг 2: Добавьте ключ в local.properties

Откройте файл `local.properties` в корне проекта:

```properties
# Android SDK location
sdk.dir=C\:\\Users\\STAR BUTTERFLY\\AppData\\Local\\Android\\Sdk

# Google Maps API Key
MAPS_API_KEY=AIzaSy...ваш_ключ...
```

**Важно:** Замените `YOUR_API_KEY_HERE` на ваш реальный API ключ!

### ✅ Шаг 3: Пересоберите проект

```bash
# Очистите проект
.\gradlew.bat clean

# Соберите заново
.\gradlew.bat assembleDebug
```

Или в Android Studio:
- `Build` → `Rebuild Project`

### ✅ Шаг 4: Проверьте работу карты

1. Установите приложение на устройство/эмулятор
2. Откройте карту (кнопка "Maps" в навигации)
3. Карта должна загрузиться без ошибок

---

## Что было изменено в проекте

### 1. AndroidManifest.xml

Добавлены разрешения и meta-data для Google Maps:

```xml
<!-- Permissions for Maps -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<application ...>
    <!-- Google Maps API Key -->
    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="${MAPS_API_KEY}" />
</application>
```

### 2. build.gradle.kts

Добавлен placeholder для API ключа:

```kotlin
defaultConfig {
    // ...
    manifestPlaceholders["MAPS_API_KEY"] =
        project.findProperty("MAPS_API_KEY")?.toString() ?: ""
}
```

### 3. local.properties

Добавлена переменная для API ключа:

```properties
MAPS_API_KEY=YOUR_API_KEY_HERE
```

---

## Troubleshooting

### Ошибка: "API key not found"

**Причина:** Ключ не добавлен в `local.properties`

**Решение:**
1. Откройте `local.properties`
2. Добавьте ключ: `MAPS_API_KEY=AIzaSy...`
3. Пересоберите проект

### Ошибка: "Authorization failure"

**Причина:** Неправильный SHA-1 или package name

**Решение:**
1. Получите SHA-1 отпечаток:
   ```cmd
   keytool -list -v -keystore %USERPROFILE%\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
2. Добавьте SHA-1 в Google Cloud Console
3. Убедитесь, что package name = `com.example.hotel_app`

### Карта пустая/серая

**Причина:** Проблемы с ключом или интернетом

**Решение:**
1. Проверьте интернет-соединение
2. Убедитесь, что "Maps SDK for Android" включен
3. Проверьте API ключ в Google Cloud Console

---

## Безопасность

### ⚠️ Не коммитьте API ключ в Git!

Файл `local.properties` уже добавлен в `.gitignore`:

```gitignore
local.properties
```

### 🔒 Ограничьте использование ключа

В Google Cloud Console:
1. Откройте ваш API ключ
2. В "Application restrictions" выберите "Android apps"
3. Добавьте:
   - Package name: `com.example.hotel_app`
   - SHA-1 отпечаток (см. выше)
4. В "API restrictions" выберите "Maps SDK for Android"

---

## Для production (release версии)

### 1. Получите release SHA-1

```cmd
keytool -list -v -keystore /path/to/your/keystore.jks -alias your_alias
```

### 2. Создайте отдельный API ключ для release

- Используйте тот же процесс, но с release SHA-1
- Добавьте в `local.properties`:
  ```properties
  RELEASE_MAPS_API_KEY=AIzaSy...release_ключ...
  ```

### 3. Обновите build.gradle.kts

```kotlin
buildTypes {
    release {
        // ...
        manifestPlaceholders["MAPS_API_KEY"] = 
            project.findProperty("RELEASE_MAPS_API_KEY")?.toString() ?: ""
    }
    debug {
        manifestPlaceholders["MAPS_API_KEY"] = 
            project.findProperty("MAPS_API_KEY")?.toString() ?: ""
    }
}
```

---

## Полезные ссылки

- [Google Maps Platform](https://cloud.google.com/maps-platform/)
- [Maps SDK for Android](https://developers.google.com/maps/documentation/android-sdk/overview)
- [API Key Best Practices](https://developers.google.com/maps/api-key-best-practices)
- [AndroidManifest.xml Meta-Data](https://developer.android.com/guide/topics/manifest/meta-data-element)

---

## Статус

- ✅ AndroidManifest.xml обновлён
- ✅ build.gradle.kts настроен
- ✅ local.properties создан
- ✅ Проект собирается успешно

**Следующий шаг:** Получите API ключ и добавьте его в `local.properties`
