package com.creatio.autotest.flows;

import com.creatio.autotest.components.*;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.pages.ConsultationPanelPage;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CashierFlow {

    private static final Logger log =
            LoggerFactory.getLogger(CashierFlow.class);

    private final BasePage                 basePage       = new BasePage();
    private final ButtonsComponent         buttons        = new ButtonsComponent();
    private final MessageBoxComponent      msgBox         = new MessageBoxComponent();
    private final PrintComponent           print          = new PrintComponent();
    private final CreditApplicationAssertions assertions  = new CreditApplicationAssertions();
    private final FieldAssertions          fieldAssert    = new FieldAssertions();
    private final ConsultationPanelPage    consultation   = new ConsultationPanelPage();
    private final ApplicationSearchFlow    searchFlow     = new ApplicationSearchFlow();
    private final ClickService click    = new ClickService();

    private String contractNumber;

    public CashierFlow withContractNumber(String number) {
        this.contractNumber = number;
        return this;
    }

    // ══════════════════════════════════════
    // MAIN
    // ══════════════════════════════════════

    @Step("Кассовые операции: подтверждение приходного и расходного ордеров")
    public void completeCashOperations() {
        log.info("Complete signing stage");

        String contractNumber =
                ScenarioContextProvider.get().getCreditContractNumber();

        consultation.closeIfOpened();

        searchFlow.withApplicationNumber(contractNumber).openContractByNumber();

        buttons.clickTabByName("Операции по договору");

        processIncomingOrder();
        processOutgoingOrder();
    }

    // ══════════════════════════════════════
    // STEPS
    // ══════════════════════════════════════

    @Step("Приходный кассовый ордер (комиссия)")
    private void processIncomingOrder() {
        log.info("Process incoming order");

        buttons.doubleClickByName("Приходный");
        basePage.checkPage("BnzOrderPageContainer");

        assertions.assertOrderState("Новый");

        click.clickByName("Подтвердить");

        msgBox.shouldSeeModal("Кассовый ордер успешно подтвержден");
        click.clickByName("ОК");



        assertions.assertOrderState("Подтвержден");
        print.selectPrintOption("Приходный кассовый ордер по комиссии");

        click.clickByName("Закрыть");
    }

    @Step("Расходный кассовый ордер (выдача кредита)")
    private void processOutgoingOrder() {
        log.info("Process outgoing order");

        buttons.doubleClickByName("Расходный");
        basePage.checkPage("BnzOrderPageContainer");

        assertions.assertOrderState("Новый");

        click.clickByName("Подтвердить");

        msgBox.shouldSeeModal("Кассовый ордер успешно подтвержден");
        click.clickByName("ОК");

        msgBox.shouldSeeModal("Получение фактических операций клиента по договору успешно выполнено!");
        click.clickByName("ОК");


        assertions.assertOrderState("Подтвержден");
        print.selectPrintOption("Расходный кассовый ордер");

        click.clickByName("Закрыть");
    }
}