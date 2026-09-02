# 🤖 Creatio Autotest Framework

> Фреймворк автоматизированного тестирования CRM-системы Creatio 7.18 (банковская конфигурация).
> Сквозные маршруты кредитных продуктов полного цикла — от логина до выдачи кредита и завершения заявки.

---

## 📋 Содержание

- [Общие сведения](#-общие-сведения)
- [Технологический стек](#️-технологический-стек)
- [Структура проекта](#-структура-проекта)
- [Архитектурные слои](#️-архитектурные-слои)
- [Ключевые сервисы](#-ключевые-сервисы)
- [Конфигурация](#️-конфигурация)
- [Запуск тестов](#-запуск-тестов)
- [Логирование](#-логирование)
- [Параллельный запуск](#-параллельный-запуск)
- [Как собрать сценарий](#-как-собрать-сценарий)
- [Документация](#-документация)
- [Статус](#-статус)

---

## 📌 Общие сведения

| Параметр | Значение |
|---|---|
| Название | Creatio Autotest Framework (Platform_Creatio) |
| Координаты | `com.creatio:creatio-autotest:1.0.0` |
| Язык | Java 17 (Amazon Corretto) |
| Репозиторий | GitHub — Platform_Creatio |
| Среды | test / pre-prod (внутренняя сеть банка) |
| Статус | Полный цикл по обеим линейкам |

**Ключевые принципы:**
- 🧩 **LEGO-подход** — тесты собираются из переиспользуемых блоков (стадии → сессия под ролью → сегменты → маршрут)
- 🎯 **Scenario-driven** — состав участников/залогов/документов задаётся данными; пустая секция = пустое прохождение
- 🔒 **Thread-safe** — драйвер и контекст сценария в `ThreadLocal`, состояние не хранится в static-полях
- 🛡️ **Anti-flaky** — ожидания по условию с таймаутом, без `Thread.sleep`

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

    src/main/java/com/creatio/autotest/
    ├── core/          config, driver, services (Click/Wait/Type/Scroll),
    │                  api (files/session), base (BaseComponent/BasePage)
    ├── components/    UI-блоки Creatio (гриды, лукапы, модалки, печать, фильтры,
    │                  активность) + collateral/ (характеристики залогов)
    ├── pages/         LoginPage, HeaderPage, WorkspacePage, DetailPage, ConsultationPanelPage
    ├── routes/        CreditApplicationRoute (база), RetailRoute, MicroSegmentRoute
    ├── flows/         common/ (общие) + retail/ + micro/ (по линейкам) +
    │                  общие блоки registration/ и participants/
    ├── testdata/      ScenarioFactory, AgroScenarioFactory, CollateralDataFactory,
    │                  UserPool, ScenarioContext + models/
    └── utils/         AllureAttachments, AllureTags, FieldUtils

    src/test/java/com/creatio/autotest/
    ├── base/          BaseTest, TestListener
    └── tests/         LoginTest, AuthRouteTest, StandardRouteTest, SimplifiedRouteTest,
                       CreditApplicationTest, AgroCreditApplicationTest,
                       CompositionExamplesTest, CollateralTest, DocumentCollectionTest, …

    src/test/resources/suites/   regression.xml, smoke.xml, parallel.xml, single.xml

Полная схема пакетов — на странице Confluence «О проекте Platform_Creatio».

---

## 🏗️ Архитектурные слои

    ┌─────────────────────────────────┐
    │            TESTS                │  ← выбор сценария и запуск
    ├─────────────────────────────────┤
    │            ROUTES               │  ← сессии, стадии, ролевые сегменты
    ├─────────────────────────────────┤
    │            FLOWS                │  ← законченные действия (common/retail/micro)
    ├─────────────────────────────────┤
    │            PAGES                │  ← PageObject
    ├─────────────────────────────────┤
    │          COMPONENTS             │  ← UI-блоки
    ├─────────────────────────────────┤
    │       CORE (base/services)      │  ← фундамент
    └─────────────────────────────────┘

| Слой | Назначение | Пакет |
|---|---|---|
| core | Конфигурация, драйвер, сервисы, API-хелперы | `core.*` |
| components | Переиспользуемые UI-блоки | `components.*` |
| pages | PageObject — страницы и панели | `pages.*` |
| routes | Сборка маршрута из стадий и сегментов | `routes.*` |
| flows | Законченные действия (общие / розница / Микро) | `flows.*` |
| testdata | Модели, фабрики, пул пользователей | `testdata.*` |
| utils | Вспомогательные утилиты | `utils.*` |
| tests | Тестовые сценарии | `tests.*` |

---

## 🔧 Ключевые сервисы

| Сервис | Назначение | Ключевые методы |
|---|---|---|
| `ClickService` | Клики | `clickByMarker()`, `clickByName()`, `jsClick()`, `safeClick()`, `waitAndClick()`, `jsClickWithRetry()` |
| `WaitService` | Ожидания | `waitForLoader()` |
| `TypeService` | Ввод текста | `type()`, `safeType()`, `typeAndTab()`, `typeAndEnter()`, `clear()` |
| `ScrollService` | Прокрутка | `scrollTo()`, `scrollToCenter()`, `scrollToTop()`, `scrollToBottom()` |

Ввод в поля Creatio — только посимвольный с переходом по табуляции (`typeAndTab()`); прямой `setValue()` не используется, так как не триггерит `onChange`.

---

## ⚙️ Конфигурация

Файл `config.properties` создаётся на основе `config.properties.example` (файл с реальными учётными данными в git не коммитится).

| Параметр | Описание | Пример |
|---|---|---|
| `base.url` | URL среды | `http://10.10.202.254` (test) / `http://10.10.202.245` (pre-prod) |
| `browser` | Браузер | `chrome` |
| `headless` | Headless-режим | `true` / `false` |
| `timeout` | Таймаут элементов (сек) | `10` |
| `page.load.timeout` | Таймаут загрузки (сек) | `30` |
| `user.<role>.<n>.login` / `.password` | Пулы учётных записей по ролям | `user.manager.1.login=…` |

Учётные записи задаются пулами по ролям (менеджер, верификатор, ИКОК, ИКОК-ГО, кассир и т.д.); выдача — round-robin, счётчик отдельный на каждую роль.

---

## 🚀 Запуск тестов

### Профили Maven

Каждый профиль указывает свой TestNG-сьют; по умолчанию — `regression`.

```bash
mvn clean test                # regression (по умолчанию)
mvn clean test -P smoke       # дымовой набор
mvn clean test -P parallel    # параллельный прогон (4 потока)
mvn clean test -P single      # одиночный сьют
mvn clean test -P ci          # регрессия в headless для CI
```

### Переопределение параметров

```bash
mvn clean test -Dheadless=true
mvn clean test -Dbrowser=chrome
mvn test -Dtest=StandardRouteTest      # один класс
```

### Allure-отчёт

```bash
mvn allure:report    # генерация
mvn allure:serve     # открыть
```

---

## 📊 Логирование

Конфигурация: `logback.xml`.

| Appender | Назначение | Путь |
|---|---|---|
| CONSOLE | Вывод в консоль | — |
| FILE | Все логи (rolling) | `target/logs/autotest.log` |
| ERROR_FILE | Только ошибки (rolling) | `target/logs/errors.log` |

| Логгер | Уровень |
|---|---|
| `com.creatio.autotest` | DEBUG |
| `com.codeborne.selenide` | INFO |
| `io.restassured` | INFO |
| `org.testng` | INFO |
| `org.openqa.selenium` | WARN |
| root | WARN |

Примечание: в Windows-консоли кириллица может отображаться как `?????` — для диагностики используйте файлы логов, а не вывод консоли.

---

## ⚡ Параллельный запуск

Настройка в `parallel.xml`:

```xml
<suite parallel="methods" thread-count="4">
```

**Гарантии изоляции:**
- `ThreadLocal<WebDriver>` — у каждого потока свой браузер
- контекст сценария (`ScenarioContext`) привязан к потоку
- папки загрузки изолируются по потокам
- отсутствие `static`-полей с состоянием
- пул пользователей round-robin со счётчиком отдельно по каждой роли

---

## 🧩 Как собрать сценарий

Маршрут собирается из данных сценария и блоков:

```java
// Полный стандартный розничный маршрут — один вызов
new RetailRoute(ScenarioFactory.happyPath()).runStandard();

// Полный упрощённый
new RetailRoute(ScenarioFactory.simplePath()).runSimplified();

// Полный Агро/Микро
new MicroSegmentRoute(AgroScenarioFactory.agroMicroHappyPath()).runFull();
```

Данные берутся из фабрик (`ScenarioFactory` / `AgroScenarioFactory`), учётные записи — из `UserPool`. Пошаговое руководство по сборке своего сценария с нуля — `claude/how-to-build-a-scenario.md` и живые рецепты в `CompositionExamplesTest`.

---

## 📚 Документация

- `claude/how-to-build-a-scenario.md` — как собрать сценарий с нуля
- `claude/retail-navigation-map.md`, `claude/agro-navigation-map.md` — карты блоков
- `claude/composability-roadmap.md`, `claude/composability-journal.md` — инициатива Composability
- `CONTRIBUTING.md` — правила разработки
- Confluence: «О проекте Platform_Creatio», «Розничный кредит», «Агро/Микро», «Как собрать сценарий», «База знаний Creatio 7.18», «Запуск и среды», «Отчётность»

---

## ✅ Статус

| Линейка | Состояние |
|---|---|
| Розничный кредит | стандартный и упрощённый маршруты — сквозной E2E зелёный |
| Агро/Микро | полный цикл, эпик закрыт |
| Инициатива Composability | закрыта; обе линейки на единой композиционной архитектуре |
| CI/CD | инфраструктура готова; запуск заблокирован доступом к GitLab и связностью с тестовыми средами |

---

*Актуально на: сентябрь 2026*
