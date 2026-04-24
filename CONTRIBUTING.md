# 📚 Методичка разработчика — Creatio Autotest Framework

## 🗺️ Содержание
- [Структура проекта](#структура-проекта)
- [Как добавить новый тест](#как-добавить-новый-тест)
- [Как добавить новый флоу](#как-добавить-новый-флоу)
- [Как добавить новую страницу](#как-добавить-новую-страницу)
- [Как добавить новый компонент](#как-добавить-новый-компонент)
- [Как добавить нового пользователя](#как-добавить-нового-пользователя)
- [Как добавить новый тип залога](#как-добавить-новый-тип-залога)
- [Как добавить тестовые данные](#как-добавить-тестовые-данные)
- [Правила именования](#правила-именования)
- [Anti-flaky правила](#anti-flaky-правила)
- [Запуск тестов](#запуск-тестов)
- [Параллельный запуск](#параллельный-запуск)

---

## 🏗️ Структура проекта

| Слой | Пакет | Назначение |
|---|---|---|
| Тесты | `tests/` | Только сценарии, никакой логики |
| Флоу | `flows/` | Бизнес-логика, оркестрация |
| Страницы | `pages/` | PageObject, локаторы |
| Компоненты | `components/` | Переиспользуемые UI-блоки |
| Core | `core/` | Драйвер, конфиг, сервисы |
| Тестданные | `testdata/` | Модели, фабрики, сценарии |
| Утилиты | `utils/` | Allure, JS, вспомогательные |

---

## ✅ Как добавить новый тест

### Шаг 1 — Создать класс теста в `tests/`

```java
@Feature("Название фичи")
public class MyNewTest extends BaseTest {

    @Test(groups = "regression")
    @Description("Описание теста")
    @Severity(SeverityLevel.BLOCKER)
    public void myTest() {

        // 1. Получить данные
        TestScenario scenario = ScenarioFactory.happyPath();

        // 2. Войти
        new AuthFlow().login(scenario.getManager());
        new WorkspaceFlow().select(Workspace.RETAIL_MANAGER);

        // 3. Выполнить флоу
        new MyFlow().doSomething(scenario.getData());

        // 4. Выйти
        new AuthFlow().logout();
    }
}
```

### Шаг 2 — Добавить тест в `single.xml` для отладки

```xml
<class name="com.creatio.autotest.tests.MyNewTest">
    <methods>
        <include name="myTest"/>
    </methods>
</class>
```

### Шаг 3 — Запустить

```bash
mvn test -P single
```

---

## ✅ Как добавить новый флоу

### Правила:
- Флоу **не наследует** `BasePage`
- Флоу **не знает** о других флоу (кроме инфраструктурных)
- Флоу **получает данные** через параметры
- Все UI-действия — через сервисы или страницы

```java
package com.creatio.autotest.flows;

import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyNewFlow {

    private static final Logger log =
            LoggerFactory.getLogger(MyNewFlow.class);

    // ✅ Зависимости через поля, не через конструктор
    private final LookupComponent lookup   = new LookupComponent();
    private final BasePage        basePage = new BasePage();
    private final ClickService    click    = new ClickService();

    @Step("Мой новый флоу")
    public void doSomething(MyData data) {
        log.info("Do something: data='{}'", data);

        lookup.select("Поле", data.getValue());
        basePage.clickByName("Кнопка");
    }
}
```

---

## ✅ Как добавить новую страницу

```java
package com.creatio.autotest.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BasePage;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$x;

public class MyNewPage extends BasePage {

    // ══════════════════════════════════
    // LOCATORS — методы, не поля!
    // ══════════════════════════════════

    private SelenideElement myField() {
        return $x("//input[@id='my-field']");
    }

    private SelenideElement myButton() {
        return $x("//span[@data-item-marker='MyButton']");
    }

    // ══════════════════════════════════
    // ACTIONS
    // ══════════════════════════════════

    @Step("Заполнить поле: {value}")
    public MyNewPage fillField(String value) {
        log.info("Fill field: '{}'", value);
        myField().shouldBe(Condition.visible).setValue(value);
        return this;
    }

    @Step("Нажать кнопку")
    public MyNewPage clickButton() {
        log.info("Click button");
        click.safeClick(myButton());
        return this;
    }
}
```

**Важные правила:**
- Локаторы — **методы**, не поля (защита от StaleElement)
- Методы возвращают `this` для цепочки вызовов
- Всегда `@Step` на публичных методах
- Всегда `log.info` в начале метода

---

## ✅ Как добавить новый компонент

Компонент — переиспользуемый UI-блок (не зависит от конкретной страницы).

```java
package com.creatio.autotest.components;

import com.creatio.autotest.core.base.BaseComponent;
import io.qameta.allure.Step;

public class MyComponent extends BaseComponent {

    @Step("Действие компонента: {value}")
    public MyComponent doAction(String value) {
        log.info("Component action: '{}'", value);
        // используем сервисы из BaseComponent
        click.clickByName(value);
        return this;
    }
}
```

---

## ✅ Как добавить нового пользователя

### Шаг 1 — `config.properties`

```properties
user.manager.3.login=RetailManager3
user.manager.3.password=Pass123
```

### Шаг 2 — Если новая роль, добавить в `UserRole.java`

```java
public enum UserRole {
    MANAGER     ("user.manager"),
    NEW_ROLE    ("user.new_role");  // ← добавить
}
```

### Шаг 3 — Добавить метод в `UserPool.java`

```java
public static UserCredentials newRole() {
    return acquire(UserRole.NEW_ROLE);
}
```

### Шаг 4 — Добавить в `Workspace.java` если нужно

```java
NEW_WORKSPACE ("Название в UI");
```

---

## ✅ Как добавить новый тип залога

### Шаг 1 — Добавить в `CollateralType.java`

```java
MY_COLLATERAL ("UI название", "Печатная форма", true);
```

### Шаг 2 — Создать флоу в `flows/collateral/`

```java
public class MyCollateralFlow extends BaseCollateralFlow {

    private final CollateralTabsComponent tabs = new CollateralTabsComponent();
    // ... другие компоненты

    @Override
    @Step("Залог: Мой тип")
    public void fill(CollateralData data) {
        tabs.openCollateralTab();
        // ... заполнение
    }
}
```

### Шаг 3 — Добавить в `resolveFlow()` в `CollateralStageFlow.java`

```java
case MY_COLLATERAL: return new MyCollateralFlow();
```

### Шаг 4 — Добавить фабричный метод в `CollateralDataFactory.java`

```java
public static CollateralData myCollateral(CurrencyType currency) {
    return CollateralData.builder()
            .type(CollateralType.MY_COLLATERAL)
            .currency(currency.getUiValue())
            .build();
}
```

---

## ✅ Как добавить тестовые данные

### Шаг 1 — Создать модель

```java
@Getter
@Builder
public class MyData {
    private String field1;
    private String field2;
}
```

### Шаг 2 — Добавить в `TestScenario.java` если нужно

```java
private MyData myData;
```

### Шаг 3 — Добавить в `ScenarioFactory.java`

```java
public static TestScenario happyPath() {
    return TestScenario.builder()
            // ...
            .myData(MyData.builder()
                    .field1("value1")
                    .field2("value2")
                    .build())
            .build();
}
```

---

## 📋 Правила именования

| Что | Правило | Пример |
|---|---|---|
| Тест | `*Test.java` | `CreditApplicationTest` |
| Флоу | `*Flow.java` | `RegistrationStageFlow` |
| Страница | `*Page.java` | `LoginPage` |
| Компонент | `*Component.java` | `LookupComponent` |
| Модель | существительное | `CollateralData` |
| Фабрика | `*Factory.java` | `CollateralDataFactory` |
| Метод теста | camelCase | `creditApplicationHappyPath` |
| @Step | глагол + описание | `"Заполнить поле: {value}"` |

---

## 🛡️ Anti-flaky правила

### ❌ ЗАПРЕЩЕНО

```java
// Никогда не использовать Thread.sleep или Selenide.sleep
Selenide.sleep(3000); // ❌

// Никогда не использовать индексы для поиска
$x("//input[3]"); // ❌

// Никогда не хранить состояние в static полях
private static String savedValue; // ❌
```

### ✅ ОБЯЗАТЕЛЬНО

```java
// Всегда shouldBe/shouldHave
element.shouldBe(visible, Duration.ofSeconds(10)); // ✅

// Retry для нестабильных операций
for (int i = 1; i <= 3; i++) { ... } // ✅

// JS click для Creatio
executeJavaScript("arguments[0].click();", element); // ✅

// Ждать исчезновения loader
basePage.waitForPage(); // ✅
```

---

## 🚀 Запуск тестов

```bash
# Один тест (разработка)
mvn test -P single

# Smoke тесты
mvn test -P smoke

# Regression все тесты
mvn test -P regression

# Параллельно 4 потока
mvn test -P parallel

# CI/CD headless
mvn test -P ci -Dheadless=true

# Переопределить URL
mvn test -Dbase.url=http://staging.creatio.com

# Переопределить браузер
mvn test -Dbrowser=firefox
```

---

## ⚡ Параллельный запуск

### Как работает

### Настройка `parallel.xml`

```xml
<suite parallel="methods" thread-count="4">
```

### Правила для параллельности

### Добавить пользователей для параллельности

```properties
# Чем больше пользователей — тем больше потоков
user.manager.1.login=Manager1
user.manager.2.login=Manager2
user.manager.3.login=Manager3
user.manager.4.login=Manager4
```

---

## 🔍 Отладка упавшего теста

---

*Версия: 1.0 | Апрель 2026*