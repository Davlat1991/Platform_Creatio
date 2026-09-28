package com.creatio.autotest.flows.micro;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.components.*;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.flows.common.ApplicationSearchFlow;
import com.creatio.autotest.flows.common.CollateralContractsFlow;
import com.creatio.autotest.pages.ConsultationPanelPage;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import com.creatio.autotest.testdata.models.ActivityResult;
import com.creatio.autotest.utils.FieldUtils;
import com.creatio.autotest.core.verify.VerificationCollector;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.*;

/**
 * Стадия «Подписание» (Агро/Микро), роль ИКОК-ГО.
 * <p>
 * Состав стадии:
 * <ol>
 *   <li>проверка решения КК;</li>
 *   <li>открытие временного кредитного договора через мини-панель «Связи»;</li>
 *   <li>создание договора в АБС;</li>
 *   <li>депозитный счёт, график платежей, привязка счёта к договору;</li>
 *   <li>регистрация договоров обеспечения и печатные формы
 *       ({@link CollateralContractsFlow} — общий флоу для розницы, Микро и юрлиц);</li>
 *   <li>завершение активности создания договора в АБС.</li>
 * </ol>
 * Изолированная Микро-копия соответствующей части розничного
 * {@code SigningStageFlow}; сущность «кредитный договор» одна и та же, поэтому
 * entity-маркеры переиспользуются — изолируется флоу, а не разметка.
 * <p>
 * Результат активности ставится без {@code setValue} — явный клик по полю
 * (дропдаун результата не существует в DOM до клика) + выбор {@code li[marker]},
 * по паттерну {@code AgroPreliminaryCheckFlow}.
 * <p>
 * Осталось реализовать: подписание договора, создание ордеров, печатная форма
 * «Оферта», проверка ордеров, кассовые операции.
 */
public class AgroSigningStageFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroSigningStageFlow.class);

    private static final String KK_DECISION_ACTIVITY     = "Проверка решения КК";
    private static final String ACTIVITY_RESULT          = ActivityResult.COMPLETED.getUiText();
    private static final String CONTRACT_ACTIVITY_RESULT = ActivityResult.COMPLETED.getUiText();

    /** Активность, открывающая мини-панель «Связи» со ссылкой на временный договор. */
    private static final String CONTRACT_OPEN_ACTIVITY = "Создание договора в АБС (печать договоров для встречи)";
    private static final String CONTRACT_PAGE_MARKER = "BnzContractCreditPageContainer";

    /** Внутренний рейтинг заёмщика — окно рейтинга при создании договора (Агро/Микро). */
    private static final String BORROWER_RATING = "A";

    /** Уровень ЭиС риска — то же окно. */
    private static final String EIS_RISK_LEVEL = "Низкий";

    /** Вид планирования — значение продуктовой линейки Агро/Микро. */
    private static final String PLANNING_TYPE       = "Кредиты_агро_008_аннуитет";
    private static final String CREDIT_RECEIPT_TYPE = "Получает сегодня";


    private static final String DEPOSIT_CREATE_BUTTON_ID =
            "BnzContractCreditPageBnzCreateSavingAcountContractButtonButton-imageEl";
    private static final String CREDIT_ACCOUNT_LOOKUP_ID =
            "BnzContractCreditPageBnzCreditBankAccountLookupEdit";

    // Точное имя из меню «Печать» (live DOM 06.08.2026): одинарный пробел «СИ без».
    // normalize-space() схлопывает пробелы только у элемента — литерал должен быть уже нормализован.
    // Альтернативная форма: «Шартномаи кредити АГРО - СИ бо генерали».
    private static final String OFFER_PRINT_FORM = "Шартномаи кредити АГРО - СИ без ген- комиссия";

    private static final String ORDER_PAGE_MARKER = "BnzOrderPageContainer";

    private static final String SCHEDULE_GRID_WRAP =
            "grid-BnzPaymentScheduleDetailDataGridGrid-wrap";

    private final BasePage                basePage            = new BasePage();
    private final LookupComponent         lookup              = new LookupComponent();
    private final MenuComponent           menu                = new MenuComponent();
    private final MessageBoxComponent     msgBox              = new MessageBoxComponent();
    private final ClickService            click               = new ClickService();
    private final ConsultationPanelPage   consultation        = new ConsultationPanelPage();
    private final ApplicationSearchFlow   searchFlow          = new ApplicationSearchFlow();
    private final CollateralContractsFlow collateralContracts = new CollateralContractsFlow();
    private final ButtonsComponent        buttons             = new ButtonsComponent();
    private final PrintComponent          print               = new PrintComponent();
    private final GridAssertions          gridAssertions      = new GridAssertions();

    // ══════════════════════════════════════
    // MAIN — стадия целиком
    // ══════════════════════════════════════

    /**
     * Стадия «Подписание» целиком. Точка входа для E2E-прогона.
     * <p>
     * После {@link #completeSigningStageUpToBinding()} мы уже находимся на
     * странице кредитного договора, поэтому договоры обеспечения обрабатываются
     * без повторного поиска, а активность закрывается там же, где висит —
     * в «Следующих шагах» кредитного договора.
     */
    @Step("Агро: стадия «Подписание»")
    public void completeSigningStage() {
        completeSigningStageUpToBinding();
        collateralContracts.processAllCollateralContracts();
        completeContractCreationActivity();

        log.info("Agro signing stage completed");
    }

    // ══════════════════════════════════════
    // MAIN — проход А (создание договора и счетов)
    // ══════════════════════════════════════

    @Step("Агро: стадия «Подписание» — создание договора и привязка счёта")
    public void completeSigningStageUpToBinding() {
        String applicationNumber =
                ScenarioContextProvider.get().getApplicationNumber();
        log.info("Agro signing (pass A): number='{}'", applicationNumber);

        if (applicationNumber == null || applicationNumber.isBlank()) {
            throw new IllegalStateException("Номер заявки отсутствует в контексте");
        }

        consultation.closeIfOpened();
        searchFlow.withApplicationNumber(applicationNumber).openByNumber();

        completeKkDecisionCheck();
        openContract();
        createAbsContract();
        bindAccountsAndSchedule();

        log.info("Agro signing (pass A) completed");
    }


    // ══════════════════════════════════════
    // STEPS
    // ══════════════════════════════════════

    @Step("Проверка решения КК")
    private void completeKkDecisionCheck() {
        log.info("Agro: KK decision check");
        hoverActivityRow(KK_DECISION_ACTIVITY);
        clickExecuteButton(KK_DECISION_ACTIVITY);
        selectResult("Result", ACTIVITY_RESULT);
        clickSaveEditButton();
        waitActivityProcessed();
        log.info("Agro: KK decision check completed");
    }

    @Step("Открыть временный договор")
    private void openContract() {
        log.info("Agro: open temporary contract");

        // Активность появляется в основном дашборде после перехода стадии.
        // Исключаем скрытую копию из правого сайдбара (RightSideBarModule),
        // куда упирался прежний //div[.='…'], и ждём видимую до 60с.
        $x("//div[.='" + CONTRACT_OPEN_ACTIVITY + "'" +
                " and not(ancestor::*[contains(@id,'RightSideBarModule')])]")
                .shouldBe(Condition.visible, Duration.ofSeconds(60))
                .click();

        click.jsClickWithRetry(() -> $x(
                        "//a[@data-item-marker='Contract'" +
                                " and contains(@href,'BnzContractCreditPage/edit')]"),
                "ссылка на договор");

        $x("//*[@data-item-marker='" + CONTRACT_PAGE_MARKER + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30));
    }

    @Step("Создание договора в АБС")
    private void createAbsContract() {
        log.info("Agro: create ABS contract");
        click.clickByName("Действия");
        menu.clickByName("Создание договора");

        // Окно «Выбрать рейтинг кредитоспособности и уровень ЭиС риска»
        // (появилось после настройки справочников): два обязательных lookup-поля,
        // затем «Утвердить». До него «Вид планирования» ещё не отрисован.
        lookup.selectByMarker("Внутренний рейтинг заёмщика", BORROWER_RATING);
        lookup.selectByMarker("Уровень ЭиС риска", EIS_RISK_LEVEL);
        click.clickByMarker("Утвердить");

        lookup.select("Вид планирования", PLANNING_TYPE);
        lookup.selectComboboxByMarker("LoadCreditType", CREDIT_RECEIPT_TYPE);

        click.clickByName("Выбрать");

        msgBox.shouldSeeModal("Нет задолженности!");
        click.clickByMarker("ОК");

        msgBox.shouldSeeModal("Договор успешно создан");
        click.clickByMarker("ОК");
    }

    @Step("Создание депозитного счёта, график и привязка счёта")
    private void bindAccountsAndSchedule() {
        log.info("Agro: deposit account, schedule and account binding");

        // Депозитный счёт
        click.clickById(DEPOSIT_CREATE_BUTTON_ID);
        msgBox.shouldSeeModal("Депозитный договор успешно создан в АБС");
        click.clickByMarker("ОК");

        // Номер счёта — только в ссылке поля (input пустой/readonly)
        SelenideElement depositLink = $x(
                "//*[@data-item-marker='BnzDepositBankAccount']" +
                        "//a[contains(@class,'base-edit-link')]"
        ).shouldBe(Condition.visible, Duration.ofSeconds(30))
                .shouldHave(Condition.matchText("\\d+"), Duration.ofSeconds(30));

        String depositAccount = depositLink.getText().trim();
        ScenarioContextProvider.get().setDepositAccountNumber(depositAccount);
        log.info("Депозитный счёт '{}' сохранён в контекст", depositAccount);

        click.clickByName("Закрыть");

        refresh();

        waitContractPageReady();

        // Привязка счёта: лупа → поиск → выбор
        lookup.selectInLookupByIdWithSearch(CREDIT_ACCOUNT_LOOKUP_ID, depositAccount);
        click.clickByName("Сохранить");

        // График платежей
        click.clickByName("Действия");
        menu.clickByName("Получение графика платежей");
        msgBox.shouldSeeModal("График платежей успешно получен");
        click.clickByMarker("ОК");

        // Привязка счёта к договору
        click.clickByName("Действия");
        menu.clickByName("Привязка счета к договору");
        msgBox.shouldSeeModal("Счет успешно привязан к кредитному договору");
        click.clickByMarker("ОК");

        // Creatio после сохранения сам возвращает активную вкладку на
        // «Основную информацию». Дожидаемся окончания обработки здесь, чтобы
        // переброс случился до перехода на «Параметры договора», а не после.
        basePage.waitForPage();
    }

    /**
     * Активность закрывается последней — после регистрации всех договоров
     * обеспечения и скачивания печатных форм. Шаг необратим: повторно
     * выполнить активность нельзя.
     */
    @Step("Завершение активности создания договора в АБС")
    public void completeContractCreationActivity() {
        log.info("Agro: complete contract creation activity");

        hoverActivityRow(CONTRACT_OPEN_ACTIVITY);
        clickExecuteButton(CONTRACT_OPEN_ACTIVITY);
        selectResult("Result", CONTRACT_ACTIVITY_RESULT);
        clickSaveEditButton();
        waitActivityProcessed();

        log.info("Agro: contract creation activity completed");
    }

    // ══════════════════════════════════════
    // HELPERS (паттерн активности — из AgroPreliminaryCheckFlow)
    // ══════════════════════════════════════

    private void hoverActivityRow(String activityTitle) {
        $x("//div[contains(@class,'dashboard-item-content')]" +
                "[.//text()[contains(.,'" + activityTitle + "')]]")
                .shouldBe(Condition.visible, Duration.ofSeconds(60))
                .hover();
    }

    private void clickExecuteButton(String activityTitle) {
        $x("//div[contains(@class,'dashboard-container-item')]" +
                "[.//text()[contains(.,'" + activityTitle + "')]]")
                .shouldBe(Condition.visible, Duration.ofSeconds(60))
                .hover();

        click.jsClickWithRetry(() -> $x(
                "//div[contains(@class,'dashboard-container-item')]" +
                        "[.//text()[contains(.,'" + activityTitle + "')]]" +
                        "//span[@data-item-marker='Execute']"), "Execute");

        $x("//div[@data-item-marker='ActivityMiniPageContainer']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30));
    }

    private void selectResult(String fieldMarker, String value) {
        $x("//div[@data-item-marker='" + fieldMarker + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .click();
        $x("//div[@data-item-marker='" + fieldMarker + "']" +
                "//li[@data-item-marker='" + value + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .click();
    }

    private void clickSaveEditButton() {
        click.jsClickWithRetry(() -> $x("//span[@data-item-marker='SaveEditButton']"),
                "SaveEditButton");
        basePage.waitForPage();
    }

    /**
     * Ждём завершения серверной обработки после сохранения активности:
     * попап активности и оверлей держатся дольше 10с (переход стадии /
     * создание следующей активности), только потом появляется следующий шаг.
     */
    private void waitActivityProcessed() {
        $x("//div[@data-item-marker='ActivityMiniPageContainer']")
                .should(Condition.disappear, Duration.ofSeconds(60));
        $("div.AlignableMiniPageContainer-overlay")
                .should(Condition.disappear, Duration.ofSeconds(30));
        basePage.waitForPage();
    }


    /**
     * Готовность страницы кредитного договора после refresh.
     * <p>
     * Каркас страницы появляется раньше данных карточки, поэтому клик по лупе
     * сразу после refresh давал пустой результат поиска («Нет данных»).
     * Якорь — активность стадии в дашборде: он догружается после данных
     * сущности, поэтому её появление надёжнее свидетельствует о готовности
     * карточки, чем отрисованное (и пока пустое) поле привязки счёта.
     * Скрытую копию активности из правого сайдбара исключаем — она есть в DOM
     * всегда и с displayed:false.
     */
    private void waitContractPageReady() {
        $x("//*[@data-item-marker='" + CONTRACT_PAGE_MARKER + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(60));

        $x("//div[.='" + CONTRACT_OPEN_ACTIVITY + "'" +
                " and not(ancestor::*[contains(@id,'RightSideBarModule')])]")
                .shouldBe(Condition.visible, Duration.ofSeconds(60));

        basePage.waitForPage();
        log.info("Страница кредитного договора готова к привязке счёта");
    }



    // ══════════════════════════════════════
    // MAIN — часть 2 (подписание, ордера, оферта)
    // ══════════════════════════════════════

    /**
     * Часть 2 стадии «Подписание»: подписание договора, выдача наличными,
     * печать оферты и проверка созданных ордеров.
     * <p>
     * Активность «Создание договора в АБС» закрыта в части 1, поэтому
     * (в отличие от розничного {@code signAndCreateOrders}) начинаем сразу
     * с действия «Подписание договора». Выполняется на открытой странице
     * кредитного договора.
     */
    @Step("Агро: подписание договора и выдача кредита")
    public void signAndIssueCredit() {
        signContract();
        issueCashCredit();

        // V6: график проверяем ПОСЛЕ создания ордеров (при выдаче пересчитывается).
        verifyPaymentSchedule();

        verifyOrdersAndPrint();

        log.info("Agro: sign and issue completed");
    }

    @Step("Подписание договора")
    private void signContract() {
        log.info("Agro: sign contract");

        click.clickByName("Действия");
        menu.clickByName("Подписание договора");

        msgBox.shouldSeeModal("Резервирование счетов выполнено успешно");
        click.clickByMarker("ОК");
        basePage.waitForPage();
    }

    @Step("Выдача кредита наличными")
    private void issueCashCredit() {
        log.info("Agro: issue cash credit");

        click.clickByName("Действия");
        menu.hoverAndClickSubmenu("Выдача кредита", "Наличными");

        // Модальная страница «Выдача кредита»: сумма readonly, зависит от кредита.
        String issueAmount = FieldUtils.getValueByMarker("Amount");
        log.info("Сумма к выдаче: '{}'", issueAmount);

        // V8.1a: выдача == одобренной сумме (одобрено-центричный инвариант).
        VerificationCollector.checkNumeric(
                "Выдача == одобренной сумме",
                ScenarioContextProvider.get().getApprovedAmount(),
                issueAmount);
        VerificationCollector.flush("Выдача (Агро)");

        click.clickByName("Подтвердить");

        // Модальная страница «Комиссия за выдачу кредита» (ts-modalbox, не messagebox).
        // Якорь появления — само чтение полей: getValueByMarker ждёт поле и падает,
        // если значение пустое.
        String commissionAmount = FieldUtils.getValueByMarker("CommissionAmount");
        log.info("Комиссия: выдача='{}', процент='{}', сумма='{}'",
                FieldUtils.getValueByMarker("IssuanceAmount"),
                FieldUtils.getValueByMarker("CommissionPercentage"),
                commissionAmount);

        // V8.1c: стэш комиссии для сверки приходного ордера в кассе.
        ScenarioContextProvider.get().setIssuedCommission(commissionAmount);
        click.clickByName("Подтвердить");

        msgBox.shouldSeeModal(
                "Действие успешно выполнено. Необходимо оплатить комиссию за кредит");
        click.clickByMarker("ОК");

        msgBox.shouldSeeModal("Кассовый ордер успешно сформирован");
        click.clickByMarker("ОК");

        msgBox.shouldSeeModal("График платежей успешно получен");
        click.clickByMarker("ОК");
    }

    @Step("Печать оферты и проверка ордеров")
    private void verifyOrdersAndPrint() {
        log.info("Agro: verify orders and print offer");

        // Номер финального кредитного договора — только в заголовке страницы
        // (input поля пустой). Нужен кассиру для поиска договора в части 3.
        String contractNumber = $x("//label[@id='MainHeaderSchemaPageHeaderCaptionLabel']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .shouldHave(Condition.matchText("\\d"), Duration.ofSeconds(30))
                .getText().trim();

        ScenarioContextProvider.get().setCreditContractNumber(contractNumber);
        log.info("Номер кредитного договора '{}' сохранён в контекст", contractNumber);

        printOffer();

        // Ордера под ИКОК-ГО не отображаются — настройка на стороне системы
        // не выполнена. Метод реализован и готов к включению; проверка ордеров
        // выполняется кассиром на следующей стадии.
        log.info("Проверка ордеров пропущена: отображение для ИКОК-ГО не настроено");
    }

    /** Печать оферты с проверкой факта скачивания (паттерн из CollateralContractsFlow). */
    private void printOffer() {
        log.info("Печатная форма: '{}'", OFFER_PRINT_FORM);
        print.printFormAndDownload(OFFER_PRINT_FORM);
    }

    private void openAndCloseOrder(String orderType) {
        log.info("Проверка ордера: '{}'", orderType);

        buttons.doubleClickByName(orderType);
        basePage.checkPage(ORDER_PAGE_MARKER);
        click.clickByMarker("CloseButton");
        basePage.waitForPage();
    }

    /**
     * НЕ ВЫЗЫВАЕТСЯ: отображение ордеров под ролью ИКОК-ГО не настроено
     * в системе. Включить после исправления настроек — вызов из
     * {@link #verifyOrdersAndPrint()} после {@code printOffer()}.
     */
    @Step("Проверка созданных ордеров")
    private void verifyOrders() {
        buttons.clickTabByName("Операции по договору");
        openAndCloseOrder("Приходный");
        openAndCloseOrder("Расходный");
    }

    @Step("Агро V6: верификация графика платежей")
    private void verifyPaymentSchedule() {
        buttons.clickTabByName("Операции по договору");

        gridAssertions.verifyPaymentSchedule(
                SCHEDULE_GRID_WRAP,
                ScenarioContextProvider.get().getApprovedTermMonths(),
                ScenarioContextProvider.get().getApprovedAmount());

        VerificationCollector.flush("График (Агро)");
    }



}