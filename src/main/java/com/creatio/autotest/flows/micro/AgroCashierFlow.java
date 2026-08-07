package com.creatio.autotest.flows.micro;

import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.CreditApplicationAssertions;
import com.creatio.autotest.components.MessageBoxComponent;
import com.creatio.autotest.components.PrintComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.flows.common.ApplicationSearchFlow;
import com.creatio.autotest.pages.ConsultationPanelPage;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import com.codeborne.selenide.Condition;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Set;

import static com.codeborne.selenide.Selenide.$x;

/**
 * Кассовые операции по кредитному договору (Агро/Микро), роль «Кассир».
 * <p>
 * Подтверждение приходного ордера (комиссия за выдачу) и расходного
 * (выдача кредита наличными), с печатью кассовых форм. Номер договора
 * берётся из {@link ScenarioContextProvider} — его сохраняет
 * {@code AgroSigningStageFlow.verifyOrdersAndPrint()}.
 * <p>
 * Отличия от розничного {@code CashierFlow}: суммы документов не
 * проверяются на конкретные значения (зависят от суммы кредита и продукта —
 * хардкод в теле флоу запрещён), модалки обрабатываются через
 * {@code shouldSeeModal} + клик по «ОК», скачивание печатных форм
 * проверяется фактически.
 */
public class AgroCashierFlow {

    private static final Logger log = LoggerFactory.getLogger(AgroCashierFlow.class);

    private static final String ORDER_PAGE_MARKER = "BnzOrderPageContainer";
    private static final String STATE_NEW         = "Новый";
    private static final String STATE_CONFIRMED   = "Подтвержден";

    /** ← ПРОВЕРЬ названия форм в меню «Печать» на карточке ордера. */
    private static final String INCOMING_PRINT_FORM = "Приходный кассовый ордер по комиссии";
    private static final String OUTGOING_PRINT_FORM = "Расходный кассовый ордер";

    private final BasePage                    basePage     = new BasePage();
    private final ButtonsComponent            buttons      = new ButtonsComponent();
    private final MessageBoxComponent         msgBox       = new MessageBoxComponent();
    private final PrintComponent              print        = new PrintComponent();
    private final CreditApplicationAssertions assertions   = new CreditApplicationAssertions();
    private final ClickService                click        = new ClickService();
    private final ConsultationPanelPage       consultation = new ConsultationPanelPage();
    private final ApplicationSearchFlow       searchFlow   = new ApplicationSearchFlow();

    // ══════════════════════════════════════
    // MAIN
    // ══════════════════════════════════════

    @Step("Агро: кассовые операции по договору")
    public void completeCashOperations() {
        String contractNumber =
                ScenarioContextProvider.get().getCreditContractNumber();
        log.info("Agro cashier: contract='{}'", contractNumber);

        consultation.closeIfOpened();
        searchFlow.withApplicationNumber(contractNumber).openContractByNumber();

        buttons.clickTabByName("Операции по договору");

        processIncomingOrder();
        processOutgoingOrder();

        log.info("Agro cashier: cash operations completed");
    }

    // ══════════════════════════════════════
    // STEPS
    // ══════════════════════════════════════

    @Step("Приходный кассовый ордер (комиссия)")
    private void processIncomingOrder() {
        log.info("Agro cashier: incoming order");

        openOrder("Приходный");
        assertions.assertOrderState(STATE_NEW);

        click.clickByName("Подтвердить");
        msgBox.shouldSeeModal("Кассовый ордер успешно подтвержден");
        click.clickByMarker("ОК");

        assertions.assertOrderState(STATE_CONFIRMED);
        printOrderForm(INCOMING_PRINT_FORM);

        click.clickByName("Закрыть");
        basePage.waitForPage();
    }

    @Step("Расходный кассовый ордер (выдача кредита)")
    private void processOutgoingOrder() {
        log.info("Agro cashier: outgoing order");

        openOrder("Расходный");
        assertions.assertOrderState(STATE_NEW);

        click.clickByName("Подтвердить");
        msgBox.shouldSeeModal("Кассовый ордер успешно подтвержден");
        click.clickByMarker("ОК");

        msgBox.shouldSeeModal(
                "Получение фактических операций клиента по договору успешно выполнено!");
        click.clickByMarker("ОК");

        assertions.assertOrderState(STATE_CONFIRMED);
        printOrderForm(OUTGOING_PRINT_FORM);

        click.clickByName("Закрыть");
        basePage.waitForPage();
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

    private void openOrder(String orderType) {
        buttons.doubleClickByName(orderType);
        basePage.checkPage(ORDER_PAGE_MARKER);
    }

    /** Печать с проверкой факта скачивания. Маркер меню печати различается
     *  между страницами (PrintButton / CombinedModePrintButton) — ищем через contains. */
    private void printOrderForm(String formName) {
        log.info("Печатная форма: '{}'", formName);

        Set<String> before = print.snapshotDownloads();

        click.clickByName("Печать");
        $x("//ul[contains(@data-item-marker,'PrintButton')]" +
                "//li[normalize-space()='" + formName + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .click();

        print.waitForNewDownload(before, formName);
    }
}