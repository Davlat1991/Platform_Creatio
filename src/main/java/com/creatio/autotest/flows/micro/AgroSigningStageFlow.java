package com.creatio.autotest.flows.micro;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.components.MenuComponent;
import com.creatio.autotest.components.MessageBoxComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.flows.ApplicationSearchFlow;
import com.creatio.autotest.flows.CollateralContractsFlow;
import com.creatio.autotest.pages.ConsultationPanelPage;
import com.creatio.autotest.testdata.ScenarioContextProvider;
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
 * {@code LoanIssuanceFlow}; сущность «кредитный договор» одна и та же, поэтому
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
    private static final String ACTIVITY_RESULT          = "Выполнена";
    private static final String CONTRACT_ACTIVITY_RESULT = "Выполнена";

    /** Активность, открывающая мини-панель «Связи» со ссылкой на временный договор. */
    private static final String CONTRACT_OPEN_ACTIVITY =
            "Создание договора в АБС (печать договоров для встречи)";
    private static final String CONTRACT_PAGE_MARKER = "BnzContractCreditPageContainer";

    /** Вид планирования — значение продуктовой линейки Агро/Микро. */
    private static final String PLANNING_TYPE       = "Кредиты_агро_008_аннуитет";
    private static final String CREDIT_RECEIPT_TYPE = "Получает сегодня";

    private static final String DEPOSIT_CREATE_BUTTON_ID =
            "BnzContractCreditPageBnzCreateSavingAcountContractButtonButton-imageEl";
    private static final String CREDIT_ACCOUNT_LOOKUP_ID =
            "BnzContractCreditPageBnzCreditBankAccountLookupEdit";

    private final BasePage                basePage            = new BasePage();
    private final LookupComponent         lookup              = new LookupComponent();
    private final MenuComponent           menu                = new MenuComponent();
    private final MessageBoxComponent     msgBox              = new MessageBoxComponent();
    private final ClickService            click               = new ClickService();
    private final ConsultationPanelPage   consultation        = new ConsultationPanelPage();
    private final ApplicationSearchFlow   searchFlow          = new ApplicationSearchFlow();
    private final CollateralContractsFlow collateralContracts = new CollateralContractsFlow();

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
    // RESUME — договоры обеспечения отдельным входом
    // ══════════════════════════════════════

    /**
     * Отдельный вход в регистрацию договоров обеспечения по номеру заявки:
     * используется, когда проход А уже выполнен в предыдущем прогоне.
     * В сквозном сценарии вызывается {@link #completeSigningStage()}.
     */
    @Step("Агро: регистрация договоров обеспечения")
    public void processCollateralContracts() {
        String applicationNumber =
                ScenarioContextProvider.get().getApplicationNumber();

        consultation.closeIfOpened();
        searchFlow.withApplicationNumber(applicationNumber).openByNumber();
        openContract();

        collateralContracts.processAllCollateralContracts();
        completeContractCreationActivity();
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

        // В мини-панели «Связи» кликаем по ссылке договора (JS: нативный клик
        // перехватывается значком документа). Скоуп marker='Contract' + href.
        SelenideElement contractLink = $x(
                "//a[@data-item-marker='Contract'" +
                        " and contains(@href,'BnzContractCreditPage/edit')]"
        ).shouldBe(Condition.visible, Duration.ofSeconds(30));
        click.jsClick(contractLink);

        $x("//*[@data-item-marker='" + CONTRACT_PAGE_MARKER + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30));
    }

    @Step("Создание договора в АБС")
    private void createAbsContract() {
        log.info("Agro: create ABS contract");
        click.clickByName("Действия");
        menu.clickByName("Создание договора");

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
        SelenideElement activityRow = $x(
                "//div[contains(@class,'dashboard-container-item')]" +
                        "[.//text()[contains(.,'" + activityTitle + "')]]"
        ).shouldBe(Condition.visible, Duration.ofSeconds(60));
        activityRow.hover();

        SelenideElement executeBtn = activityRow
                .$x(".//span[@data-item-marker='Execute']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30));
        executeJavaScript("arguments[0].click();", executeBtn);

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
        executeJavaScript("arguments[0].click();",
                $x("//span[@data-item-marker='SaveEditButton']")
                        .shouldBe(Condition.visible, Duration.ofSeconds(30)));
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

    // ══════════════════════════════════════
    // DEBUG (временное — удалить после закрытия подзадачи 6)
    // ══════════════════════════════════════

    /**
     * ВРЕМЕННЫЙ отладочный вход: заход сразу по номеру кредитного договора,
     * печать форм по уже зарегистрированным договорам обеспечения без
     * повторного создания их в АБС.
     */
    @Step("DEBUG: печать форм по договору {contractNumber}")
    public void debugPrintCollateralContracts(String contractNumber) {
        consultation.closeIfOpened();
        searchFlow.withApplicationNumber(contractNumber).openContractByNumber();

        $x("//*[@data-item-marker='" + CONTRACT_PAGE_MARKER + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30));

        collateralContracts.debugPrintExistingContracts();

        completeContractCreationActivity();
    }
}