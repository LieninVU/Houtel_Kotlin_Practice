# Инструкция по получению Google Maps API Key

## Шаг 1: Создайте проект в Google Cloud Console

1. Перейдите на https://console.cloud.google.com/
2. Нажмите "Create Project" (Создать проект)
3. Введите название проекта (например, "Hotel App Maps")
4. Нажмите "Create"

## Шаг 2: Включите Maps SDK for Android

1. В Google Cloud Console перейдите в "APIs & Services" → "Library"
2. Найдите "Maps SDK for Android"
3. Нажмите "Enable"

## Шаг 3: Создайте API ключ

1. Перейдите в "APIs & Services" → "Credentials"
2. Нажмите "+ CREATE CREDENTIALS" → "API key"
3. Скопируйте полученный API ключ

## Шаг 4: Ограничьте ключ (рекомендуется)

1. Нажмите на созданный ключ
2. В разделе "Application restrictions" выберите "Android apps"
3. Добавьте package name: `com.example.hotel_app`
4. Добавьте SHA-1 отпечаток (см. ниже как получить)
5. В разделе "API restrictions" выберите "Restrict key"
6. Выберите "Maps SDK for Android"
7. Нажмите "Save"

## Шаг 5: Добавьте ключ в проект

### Вариант A: Через gradle.properties (рекомендуется)

1. Создайте файл `local.properties` в корне проекта (если не существует)
2. Добавьте строку:
   ```
   MAPS_API_KEY=YOUR_API_KEY_HERE
   ```
3. Замените `YOUR_API_KEY_HERE` на ваш API ключ

### Вариант B: Напрямую в AndroidManifest.xml

Замените `${MAPS_API_KEY}` в AndroidManifest.xml на ваш ключ:
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="AIzaSy...ваш_ключ..." />
```

## Как получить SHA-1 отпечаток

### Для debug ключа (разработка):

**Windows:**
```cmd
keytool -list -v -keystore %USERPROFILE%\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android
```

**Mac/Linux:**
```bash
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

### Для release ключа (production):

```cmd
keytool -list -v -keystore /path/to/your/keystore.jks -alias your_alias
```

## Проверка работы

После добавления ключа:

1. Соберите проект:
   ```bash
   .\gradlew.bat assembleDebug
   ```

2. Установите на устройство/эмулятор

3. Откройте карту — должна загрузиться без ошибок

## Troubleshooting

### Ошибка "API key not found"

- Проверьте, что ключ добавлен в `local.properties`
- Пересоберите проект: `Build` → `Rebuild Project`

### Ошибка "Authorization failure"

- Проверьте SHA-1 отпечаток
- Убедитесь, что package name совпадает с `com.example.hotel_app`
- Подождите 1-2 минуты после создания ключа

### Карта пустая/серая

- Проверьте, что включен "Maps SDK for Android"
- Убедитесь, что API ключ не заблокирован
- Проверьте интернет-соединение

## Важные замечания

1. **Не коммитьте API ключ в Git!** Добавьте `local.properties` в `.gitignore`

2. **Используйте разные ключи** для debug и production

3. **Ограничьте ключ** по package name и SHA-1 для безопасности

4. **Мониторинг использования** в Google Cloud Console → "APIs & Services" → "Dashboard"

## Ссылки

- [Google Maps Platform](https://cloud.google.com/maps-platform/)
- [Maps SDK for Android](https://developers.google.com/maps/documentation/android-sdk/overview)
- [API Key Best Practices](https://developers.google.com/maps/api-key-best-practices)
