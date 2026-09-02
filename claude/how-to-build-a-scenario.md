# Как собрать сценарий

Практическое руководство: как из готовых блоков собрать тестовый сценарий — от выбора данных до запуска маршрута. Охватывает обе линейки (розница и Агро/Микро). Общая архитектура — на странице «О проекте Platform_Creatio»; карты блоков — `claude/retail-navigation-map.md` и `claude/agro-navigation-map.md`; живые рецепты в коде — `CompositionExamplesTest`.

## 1. Ментальная модель

Любой прогон собирается из трёх вещей, каждая следующая опирается на предыдущую:

1. **Данные** — объект `TestScenario`: кто (учётные записи), кого (контакт клиента), что (продукт), как (анкета, маршрут), с кем (участники), под что (залоги).
2. **Сессия под ролью** — обёртка `as(user, workspace, тело)`: логин → выбор рабочего места → тело → выход. Надевает роль на любой набор шагов.
3. **Блоки и сегменты** — процессные шаги (`createApplication()`, `registration()`, …) и готовые ролевые куски маршрута (`managerUpToReview()`, `runStandard()`, …).

Правило, которое всё упрощает: стадии участников, обеспечения и сбора документов — **scenario-driven**. Пустая секция сценария даёт пустое прохождение: нет участников — стадия участников проходится вхолостую, нет залогов — обеспечение закрывается без залогов. Один и тот же код обслуживает и полный, и сокращённый состав.

## 2. Слой данных — `TestScenario`

### Из чего состоит

`TestScenario` собирается билдером (Lombok). Секции:

| Секция | Что задаёт |
| --- | --- |
| `manager` / `underwriter` / `ikok` / `ikokGo` / `cashier` | учётные записи по ролям (из `UserPool`) |
| `contact` | клиент, на которого создаётся заявка |
| `registration` | анкета: регион, источник, доходы/расходы, скоринг, профиль клиента |
| `product` | параметры продукта: вид, цель, сумма, срок, валюта, график |
| `route` | тип маршрута: `CreditRoute.STANDARD` или `SIMPLIFIED` |
| `participants` | список участников (поручители, залогодатели) |
| `collaterals` | список залогов |

### Готовые сценарии — `ScenarioFactory`

В большинстве случаев собирать с нуля не нужно — берите готовый метод фабрики:

| Метод | Состав |
| --- | --- |
| `authOnly()` | только учётные записи (для блока авторизации) |
| `simplePath()` | упрощённый маршрут, без участников и залогов |
| `happyPath()` | стандартный: поручитель + залогодатель + залог (авто) |
| `highIncomePath()` | стандартный, анкета высокого дохода |
| `withGuarantorOnly()` | только поручитель |
| `withPledgerOnly()` | только залогодатель |
| `withCollateralsOnly()` | без участников, два залога |
| `allCollaterals()` | регрессия: три участника + все десять типов залога |
| `otherIncomePath()` / `employeePath()` / `selfEmployedPath()` | фокус на типе занятости заёмщика |

Агро/Микро — своя фабрика `AgroScenarioFactory`: `agroMicroHappyPath()` (поручитель + залогодатель, хлопок + будущий урожай) и `agroMicroRegression()` (поручитель, один залог).

### Собрать свой сценарий

По правилам проекта данные живут в фабрике, а не в теле теста. Поэтому «свой сценарий с нуля» — это **новый метод в `ScenarioFactory`**, а не сборка внутри тест-класса. Опирайтесь на общую базу `baseBuilder()` (она уже проставляет учётные записи, контакт и анкету по умолчанию) и доопределяйте нужное:

    public static TestScenario myScenario() {
        return baseBuilder()
                .product(defaultProduct())
                .route(CreditRoute.STANDARD)
                .participants(List.of(
                        ParticipantData.guarantor(false)
                ))
                .collaterals(List.of(
                        CollateralDataFactory.realEstate(CurrencyType.TJS)
                ))
                .build();
    }

Что можно менять:

- **Продукт** — `simpleProduct()` / `defaultProduct()` / `highAmountProduct()` (сумма/срок), либо свой `ProductData.builder()`.
- **Маршрут** — `CreditRoute.STANDARD` или `SIMPLIFIED`.
- **Тип клиента** — через профиль занятости в анкете (`EmploymentType.OTHER_INCOME` / `EMPLOYEE` / `SELF_EMPLOYED`); в фабрике это отдельные регистрации (`employeeRegistration()` и т.п.). Нужную реализацию клиентского флоу подберёт `ClientFlowFactory` по данным — ветвлений в тесте не пишем.

### Участники

    ParticipantData.guarantor(false)   // поручитель
    ParticipantData.pledger(false)     // залогодатель
    ParticipantData.pledger1(false)    // второй залогодатель

Документы участника прикрепляются по умолчанию, состав определяется его ролью (`ParticipantRole` → `ParticipantDocuments`). Чтобы не прикреплять документы конкретному участнику:

    ParticipantData.guarantor(false).withoutDocuments()

### Залоги

`CollateralDataFactory`, все методы принимают валюту (`CurrencyType.TJS`):

    vehicle · realEstate · equipment · cashDeposit · gold · goods ·
    movableProperty · acquiredProperty · cotton · futureHarvest   // розница
    agroCotton · agroFutureHarvest                                 // Агро

Пример:

    .collaterals(List.of(
            CollateralDataFactory.vehicle(CurrencyType.TJS),
            CollateralDataFactory.gold(CurrencyType.TJS)
    ))

## 3. Слой маршрута

### Три уровня

- **Стадии** (без логина, под активной сессией): `createApplication()`, `registration()`, `preliminaryCheck()`, `participantsStage()`, `collateralStage()`, `documentCollection()`, `review()`, `issueLoan()`, `cashierOrders()` и т.д.
- **Сессия под ролью**: `as(user, workspace, тело)`.
- **Ролевые сегменты**: готовые куски маршрута.

### Розница — `RetailRoute`

Создание: `new RetailRoute(scenario)`.

Полный маршрут одной строкой:

    new RetailRoute(ScenarioFactory.happyPath()).runStandard();
    new RetailRoute(ScenarioFactory.simplePath()).runSimplified();

Сегменты стандартного: `managerUpToReview` → `underwriterApprove` → `managerNotification` → `ikokGoIssuance` → `cashierOperations` → `finishApplication`.
Сегменты упрощённого: `managerSimplifiedUpToNotification` → `ikokIssuance` → `cashierOperations` → `simplifiedFinish`.

### Агро/Микро — `MicroSegmentRoute`

    new MicroSegmentRoute(AgroScenarioFactory.agroMicroHappyPath()).runFull();

Сегменты `runFull()`: `creditExpertUpToReview` → `underwriterApprove` → `clientNotification` → `middleControllerDocuments` → `ikokGoSigning` → `cashierOperations` → `finishApplication`.

### Роли и рабочие места

Учётные записи берутся из сценария (`scenario.getManager()` и т.д.), рабочие места — из `Workspace`: `RETAIL_MANAGER` («Розничный менеджер»), `CREDIT_EXPERT` («Кредитный эксперт»), `UNDERWRITER` («Верификатор»), `IKOK` («ИКОК»), `IKOK_GO` («ИКОК-ГО»), `CASHIER` («Касса»).

## 4. Рецепты сборки

Все рецепты живут в `CompositionExamplesTest` (все `enabled = false` — это примеры, не CI-тесты).

**Только авторизация:**

    TestScenario scenario = ScenarioFactory.authOnly();
    new RetailRoute(scenario)
            .authenticate(scenario.getManager(), Workspace.RETAIL_MANAGER);

**Авторизация + создание заявки, стоп** (блок под сессией через `as()`):

    TestScenario scenario = ScenarioFactory.happyPath();
    RetailRoute route = new RetailRoute(scenario);
    route.as(scenario.getManager(), Workspace.RETAIL_MANAGER,
            route::createApplication);

**Полный маршрут — один вызов:**

    new RetailRoute(ScenarioFactory.happyPath()).runStandard();
    new RetailRoute(ScenarioFactory.simplePath()).runSimplified();
    new MicroSegmentRoute(AgroScenarioFactory.agroMicroHappyPath()).runFull();

**Вход в середину маршрута** (без прохождения предыдущих стадий):

    RetailRoute route = new RetailRoute(ScenarioFactory.happyPath());
    route.resumeAt("392126").underwriterApprove();       // по номеру заявки
    route.resumeWithContract("02-005-033413").cashierOperations();  // по номеру договора

**Свой сегмент под своей ролью** (собрать из сессии и стадий, без новых методов маршрута):

    RetailRoute route = new RetailRoute(scenario);
    route.as(scenario.getIkokGo(), Workspace.IKOK_GO,
            () -> new SigningStageFlow().issueLoan(false, true));

**Свой состав участников/залогов** — задаётся в сценарии (см. раздел 2), маршрут при этом не меняется: `participantsStage()`, `collateralStage()` и `documentCollection()` пройдут по данным.

## 5. Пошагово «с нуля»

1. **Данные.** Есть ли готовый метод в `ScenarioFactory` / `AgroScenarioFactory`? Если да — берите его. Если нет — добавьте новый метод фабрики на базе `baseBuilder()`, доопределив продукт, маршрут, участников, залоги.
2. **Тест-класс.** Тест наследует `BaseTest`. На классе — Allure-метки `@Epic` / `@Feature` / `@Owner`.
3. **Сборка.** В тест-методе создайте маршрут (`new RetailRoute(scenario)` или `new MicroSegmentRoute(scenario)`) и либо вызовите `runStandard()` / `runSimplified()` / `runFull()`, либо соберите нужные сегменты, либо войдите с середины через `resumeAt` / `resumeWithContract`.
4. **Метки метода.** `@Test` → `@Story` → `@Description` → `@Severity` (именно в этом порядке).
5. **Запуск.** Точечно гоняйте руками — маршруты создают реальные заявки в системе.

Скелет тест-класса:

    @Epic(AllureTags.Epics.RETAIL)
    @Feature(AllureTags.Features.E2E)
    @Owner("Davlat")
    public class MyRouteTest extends BaseTest {

        @Test(groups = "e2e", enabled = false)
        @Story("Свой маршрут — краткое имя")
        @Description("Что проверяет этот прогон")
        @Severity(SeverityLevel.CRITICAL)
        public void myRoute() {
            new RetailRoute(ScenarioFactory.happyPath()).runStandard();
        }
    }

## 6. Правила и подводные камни

- **`UserPool` — ровно один раз за сценарий.** Счётчик выдачи round-robin ведётся отдельно по каждой роли и сдвигается на каждом вызове. Учётные записи берутся один раз — в фабрике (`baseBuilder()`). Повторный вызов `UserPool.manager()` выдаст другую запись и сломает логику.
- **Нет состояния в static-полях.** Маршрут держит только ссылку на сценарий; передача данных между ролями — через контекст сценария, привязанный к потоку. Это условие параллельного запуска.
- **Данные — только через фабрику.** Не собирайте `TestScenario` инлайн в тесте: новый состав = новый метод фабрики.
- **Пустые секции — это нормально.** Пустой список участников или залогов не ломает маршрут, а даёт пустое прохождение стадии.
- **Allure-метки обязательны** для каждого тест-класса; строки — только через константы `AllureTags`.

## 7. Куда смотреть дальше

- **`CompositionExamplesTest`** — исполняемые рецепты сборки.
- **Карты блоков** — `claude/retail-navigation-map.md`, `claude/agro-navigation-map.md`.
- **Страницы продуктов** — «Розничный кредит» (раздел «Карта структуры и как собрать маршрут») и «Агро/Микро» (раздел «Сборка маршрута»).
- **«О проекте Platform_Creatio»** — общая архитектура и правила кода.
