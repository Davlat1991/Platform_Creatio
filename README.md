# 🤖 Creatio Autotest Framework

> Фреймворк для автоматизированного тестирования CRM-системы Creatio (7.18 → 8.3)

---

## 📋 Содержание

- [Общие сведения](#общие-сведения)
- [Технологический стек](#технологический-стек)
- [Структура проекта](#структура-проекта)
- [Архитектурные слои](#архитектурные-слои)
- [Ключевые компоненты](#ключевые-компоненты)
- [Конфигурация](#конфигурация)
- [Запуск тестов](#запуск-тестов)
- [Логирование](#логирование)
- [Параллельный запуск](#параллельный-запуск)
- [Статус реализации](#статус-реализации)
- [План развития](#план-развития)

---

## 📌 Общие сведения

| Параметр | Значение |
|---|---|
| Название | Creatio Autotest Framework |
| Версия | 1.0.0 |
| Язык | Java 17 |
| Репозиторий | GitHub (Platform_Creatio) |
| Дата начала | Апрель 2026 |
| Статус | В разработке |

**Ключевые принципы:**
- 🧩 **LEGO-подход** — тесты собираются из переиспользуемых блоков
- 🔒 **Thread-safe** — поддержка параллельного выполнения
- ⚙️ **CI/CD-ready** — оптимизирован для Jenkins / GitLab CI
- 🛡️ **Anti-flaky** — стабильные ожидания без `Thread.sleep`

---

## 🛠️ Технологический стек

| Компонент | Технология | Версия |
|---|---|---|
| Язык | Java | 17 |
| UI автоматизация | Selenide | 7.2.1 |
| Test Framework | TestNG | 7.11.0 |
| Отчётность | Allure | 2.27.0 |
| API тестирование | RestAssured | 5.4.0 |
| Логирование | SLF4J + Logback | 2.0.13 / 1.5.6 |
| Сборка | Maven | 3.x |
| AOP | AspectJ | 1.9.21 |
| Утилиты | Lombok | 1.18.32 |
| Сериализация | Gson | 2.10.1 |

---

## 📁 Структура проекта

creatio-autotest/
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/creatio/autotest/
│   │   │       ├── core/
│   │   │       │   ├── config/
│   │   │       │   │   └── Config.java
│   │   │       │   ├── driver/
│   │   │       │   │   ├── DriverProvider.java
│   │   │       │   │   └── DriverSetup.java
│   │   │       │   ├── services/
│   │   │       │   │   ├── ClickService.java
│   │   │       │   │   ├── WaitService.java
│   │   │       │   │   ├── TypeService.java
│   │   │       │   │   └── ScrollService.java
│   │   │       │   └── base/
│   │   │       │       ├── BaseComponent.java
│   │   │       │       └── BasePage.java
│   │   │       ├── components/
│   │   │       ├── pages/
│   │   │       │   └── LoginPage.java
│   │   │       ├── flows/
│   │   │       │   └── LoginFlow.java
│   │   │       ├── testdata/
│   │   │       └── utils/
│   │   │           ├── AllureAttachments.java
│   │   │           └── JavaScriptUtils.java
│   │   └── resources/
│   │       ├── config.properties
│   │       └── logback.xml
│   └── test/
│       ├── java/
│       │   └── com/creatio/autotest/
│       │       ├── base/
│       │       │   ├── BaseTest.java
│       │       │   └── TestListener.java
│       │       └── tests/
│       │           └── LoginTest.java
│       └── resources/
│           ├── config.properties
│           ├── logback.xml
│           └── suites/
│               ├── regression.xml
│               ├── smoke.xml
│               ├── parallel.xml
│               └── single.xml
---

## 🏗️ Архитектурные слои

┌─────────────────────────────────┐
│           TESTS                 │  ← сценарии
├─────────────────────────────────┤
│           FLOWS                 │  ← бизнес-логика
├─────────────────────────────────┤
│           PAGES                 │  ← PageObject
├─────────────────────────────────┤
│         COMPONENTS              │  ← UI блоки
├─────────────────────────────────┤
│      CORE (base/services)       │  ← фундамент
└─────────────────────────────────┘


| Слой | Назначение | Пакет |
|---|---|---|
| core | Конфигурация, драйвер, сервисы | `core.*` |
| components | Переиспользуемые UI-блоки | `components.*` |
| pages | PageObject — элементы страниц | `pages.*` |
| flows | Бизнес-сценарии (композиция) | `flows.*` |
| testdata | Модели и фабрики данных | `testdata.*` |
| utils | Вспомогательные утилиты | `utils.*` |
| tests | Тестовые сценарии | `tests.*` |
| base | BaseTest, TestListener | `base.*` |

---

## 🔧 Ключевые компоненты

### Config.java
Централизованное управление конфигурацией.
- Singleton паттерн
- Читает `config.properties` из classpath
- Поддерживает переопределение через `-D` параметры

### DriverProvider.java
Управление WebDriver через `ThreadLocal`.

Thread 1 → DriverProvider → WebDriver_1
Thread 2 → DriverProvider → WebDriver_2
Thread 3 → DriverProvider → WebDriver_3

### Services

| Сервис | Назначение | Ключевые методы |
|---|---|---|
| `ClickService` | Клики | `click()`, `jsClick()`, `safeClick()` |
| `WaitService` | Ожидания | `waitVisible()`, `waitHidden()`, `waitForLoader()` |
| `TypeService` | Ввод текста | `type()`, `typeAndTab()`, `typeAndEnter()` |
| `ScrollService` | Прокрутка | `scrollTo()`, `scrollToCenter()` |

### LoginPage — локаторы

| Элемент | Селектор |
|---|---|
| Поле логина | `#loginEdit-el` |
| Поле пароля | `#passwordEdit-el` |
| Кнопка входа | `[data-item-marker='btnLogin']` |
| Заголовок | `#left-header-container` |
| Ошибка | `.base-edit-validation` |

### Utils

**AllureAttachments:**

| Метод | Назначение |
|---|---|
| `attachScreenshot(name)` | Скриншот в Allure |
| `attachPageSource()` | HTML страницы |
| `attachText(name, content)` | Текст |
| `attachBrowserLogs()` | Логи браузера |
| `attachCurrentUrl()` | Текущий URL |

**JavaScriptUtils:**

| Метод | Назначение |
|---|---|
| `click(element)` | JS клик |
| `scrollIntoView(element)` | Прокрутка |
| `setValue(element, value)` | Установка значения |
| `fireChangeEvent(element)` | Событие change |
| `fireBlurEvent(element)` | Событие blur |
| `fixFileApiService()` | Фикс TsiFileApiService |

---

## ⚙️ Конфигурация

Файл: `src/main/resources/config.properties`

```properties
base.url=http://10.10.202.254
browser=chrome
headless=false
timeout=10
page.load.timeout=30
admin.login=Supervisor
admin.password=Supervisor
```

| Параметр | Описание | Пример |
|---|---|---|
| `base.url` | URL приложения | `http://10.10.202.254` |
| `browser` | Браузер | `chrome` / `firefox` |
| `headless` | Headless режим | `true` / `false` |
| `timeout` | Таймаут элементов (сек) | `10` |
| `page.load.timeout` | Таймаут загрузки (сек) | `30` |
| `admin.login` | Логин администратора | `Supervisor` |
| `admin.password` | Пароль администратора | `Supervisor` |

---

## 🚀 Запуск тестов

### Основные команды

```bash
# Regression (по умолчанию)
mvn test

# Smoke
mvn test -P smoke

# Parallel
mvn test -P parallel

# Single тест
mvn test -P single

# CI/CD (headless)
mvn test -P ci
```

### Переопределение параметров

```bash
# Другой URL
mvn test -Dbase.url=http://staging.creatio.com

# Headless режим
mvn test -Dheadless=true

# Другой браузер
mvn test -Dbrowser=firefox

# Комбинация
mvn test -P ci -Dbase.url=http://staging.creatio.com -Dheadless=true
```

### Allure отчёт

```bash
# Генерация отчёта
mvn allure:report

# Открыть отчёт
mvn allure:serve
```

---

## 📊 Логирование

Конфигурация: `src/main/resources/logback.xml`

| Appender | Назначение | Путь |
|---|---|---|
| CONSOLE | Вывод в консоль | — |
| FILE | Все логи | `target/logs/autotest.log` |
| ERROR_FILE | Только ошибки | `target/logs/errors.log` |

| Пакет | Уровень |
|---|---|
| `com.creatio.autotest` | DEBUG |
| `com.codeborne.selenide` | INFO |
| `org.openqa.selenium` | WARN |
| `io.restassured` | INFO |

---

## ⚡ Параллельный запуск

Настройка в `parallel.xml`:
```xml
<suite parallel="methods" thread-count="4">
```

**Гарантии изоляции:**
- `ThreadLocal<WebDriver>` — каждый поток имеет свой браузер
- `@BeforeMethod` / `@AfterMethod` — инициализация на уровне метода
- Отсутствие `static` полей с состоянием в Page Object-ах

---

## ✅ Статус реализации

| Этап | Компонент | Статус |
|---|---|---|
| 1 | pom.xml + структура проекта | ✅ Завершён |
| 2 | Config.java | ✅ Завершён |
| 3 | DriverProvider + DriverSetup | ✅ Завершён |
| 4 | Services (Click/Wait/Type/Scroll) | ✅ Завершён |
| 5 | BaseComponent + BasePage | ✅ Завершён |
| 6 | BaseTest + TestListener | ✅ Завершён |
| 7 | LoginPage + LoginFlow | ✅ Завершён |
| 8 | LoginTest | ✅ Завершён |
| 9 | logback.xml | ✅ Завершён |
| 10 | AllureAttachments + JavaScriptUtils | ✅ Завершён |
| 11 | Components (Lookup, Grid, Modal) | 🔄 В плане |
| 12 | Бизнес-страницы | 🔄 В плане |
| 13 | TestData (модели + билдеры) | 🔄 В плане |
| 14 | Бизнес-тесты | 🔄 В плане |
| 15 | CI/CD (Jenkins/GitLab) | 🔄 В плане |
| 16 | Docker поддержка | 🔄 В плане |

---

## 🗺️ План развития

Этап 11 → Components (LookupComponent, GridComponent, ModalComponent)
Этап 12 → Бизнес-страницы (главная, контакты и др.)
Этап 13 → TestData (модели + Builder pattern)
Этап 14 → Первые бизнес-тесты
Этап 15 → CI/CD конфигурация (Jenkins/GitLab)
Этап 16 → Docker поддержка
---

*Документ актуален на: Апрель 2026*
*Версия документа: 1.0*