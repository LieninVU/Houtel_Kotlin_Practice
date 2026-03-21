# 🛠️ Инструкция по настройке JDK для сборки

## Проблема
При сборке проекта возникает ошибка:
```
ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
```

## Решение

### Вариант 1: Установка JDK через winget (Windows) - РЕКОМЕНДУЕТСЯ

1. Откройте PowerShell от имени администратора
2. Выполните команду:
```powershell
winget install Oracle.JDK.21
```

3. После установки перезапустите терминал

### Вариант 2: Ручная установка JDK

1. Скачайте JDK 21:
   - [Oracle JDK 21](https://www.oracle.com/java/technologies/downloads/#jdk21-windows)
   - [OpenJDK 21](https://jdk.java.net/21/)
   - [Adoptium Temurin 21](https://adoptium.net/temurin/releases/?version=21)

2. Установите JDK в папку по умолчанию (например, `C:\Program Files\Java\jdk-21`)

3. Настройте переменную окружения JAVA_HOME:
   
   **Windows:**
   - Откройте "Свойства системы" → "Дополнительные параметры системы"
   - Нажмите "Переменные среды"
   - В "Системные переменные" нажмите "Создать"
   - Имя: `JAVA_HOME`
   - Значение: `C:\Program Files\Java\jdk-21`
   - Нажмите OK

   **Или через PowerShell:**
   ```powershell
   [System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-21", "Machine")
   ```

4. Добавьте Java в PATH (если не добавилось автоматически):
   - В системных переменных найдите `Path`
   - Нажмите "Изменить" → "Создать"
   - Добавьте: `%JAVA_HOME%\bin`

### Вариант 3: Использование Android Studio JDK

Android Studio поставляется со встроенной JDK (JetBrains Runtime):

1. Откройте Android Studio
2. Перейдите в: `File` → `Settings` → `Build, Execution, Deployment` → `Build Tools` → `Gradle`
3. В поле "Gradle JDK" выберите `jbr-21` (или аналогичную встроенную JDK)
4. Нажмите OK

Или настройте JAVA_HOME на встроенную JDK:
```powershell
# Путь может отличаться в зависимости от версии
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Android\Android Studio\jbr", "Machine")
```

---

## Проверка установки

Откройте новый терминал и выполните:

```bash
java -version
```

Должно отобразиться:
```
java version "21.x.x" ...
```

```bash
echo %JAVA_HOME%
```

Должно отобразиться:
```
C:\Program Files\Java\jdk-21
```

---

## Сборка проекта

После установки JDK:

```bash
# Сборка отладочной версии
gradlew assembleDebug

# Установка на устройство (если подключено)
gradlew installDebug

# Очистка и пересборка
gradlew clean assembleDebug
```

---

## Если проблема сохраняется

1. **Перезапустите компьютер** - переменные окружения могут не примениться сразу

2. **Проверьте путь к JDK:**
   ```bash
   where java
   ```

3. **Используйте полный путь к gradlew:**
   ```bash
   D:\SOFT\LEARN\Kotlin\Hotel_App\gradlew assembleDebug
   ```

4. **Попробуйте собрать из Android Studio:**
   - Откройте проект в Android Studio
   - `Build` → `Make Project` (Ctrl+F9)

---

## Быстрая проверка

Выполните эту команду в PowerShell:
```powershell
if ([System.Environment]::GetEnvironmentVariable("JAVA_HOME", "Machine")) { 
    Write-Host "JAVA_HOME установлен: $([System.Environment]::GetEnvironmentVariable("JAVA_HOME", "Machine"))" 
} else { 
    Write-Host "JAVA_HOME НЕ установлен! Установите JDK 21." 
}
```
