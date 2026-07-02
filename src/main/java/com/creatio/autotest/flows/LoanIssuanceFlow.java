package com.creatio.autotest.flows;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.components.*;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.pages.ConsultationPanelPage;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import com.creatio.autotest.testdata.models.CollateralType;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.*;

public class LoanIssuanceFlow {

    private static final Logger log =
            LoggerFactory.getLogger(LoanIssuanceFlow.class);

    private static final String CONTRACT_PAGE_MARKER =
            "BnzContractCreditPageContainer";

    private final BasePage              basePage     = new BasePage();
    private final ButtonsComponent      buttons      = new ButtonsComponent();
    private final DashboardComponent    dashboard    = new DashboardComponent();
    private final MenuComponent         menu         = new MenuComponent();
    private final LookupComponent       lookup       = new LookupComponent();
    private final MessageBoxComponent   msgBox       = new MessageBoxComponent();
    private final PrintComponent        print        = new PrintComponent();
    private final ClickService          click        = new ClickService();
    private final ConsultationPanelPage consultation = new ConsultationPanelPage();
    private final ApplicationSearchFlow searchFlow   = new ApplicationSearchFlow();



    // ══════════════════════════════════════
    // MAIN
    // ══════════════════════════════════════

    @Step("Loan Issuance: полный сценарий выдачи кредита")
    public void issueLoan() {
        String applicationNumber =
                ScenarioContextProvider.get().getApplicationNumber();
        log.info("Issue loan: number='{}'", applicationNumber);

        consultation.closeIfOpened();

        searchFlow.withApplicationNumber(applicationNumber).openByNumber();

      //  completeKkDecisionCheck();
        openContract();
        createAbsContract();
        bindAccountsAndSchedule();
        signAndIssueCredit();
        verifyOrdersAndPrint();
    }

    // ══════════════════════════════════════
    // STEPS
    // ══════════════════════════════════════

    @Step("Проверка решения КК")
    private void completeKkDecisionCheck() {
        log.info("Complete KK decision check");

        dashboard.clickAndWaitMiniPage("Проверка решения КК", "Execute");

        setFieldByMarker("Result", "Выполнена");
        menu.clickByName("Выполнена");
        click.clickByMarker("SaveEditButton");
    }

    @Step("Открыть договор")
    private void openContract() {
        // Клик по названию активности открывает мини-панель "Связи", а не страницу договора.
        dashboard.clickByName(
                "Создание договора в АБС (печать договоров для встречи)"
        );

        // В детали "Связи" кликаем по номеру временного договора.
        // Скоуп marker='Contract' + href на страницу договора отсекает значок
        // (marker='ContractImage') и соседние ссылки (FinApplication, CreatedBy).
        SelenideElement contractLink = $x(
                "//a[@data-item-marker='Contract'" +
                        " and contains(@href,'BnzContractCreditPage/edit')]"
        ).shouldBe(Condition.visible);

        // JS-клик: нативный клик по ссылке в мини-панели перехватывается значком документа.
        click.jsClick(contractLink);

        // Только теперь открывается страница временного договора.
        $x("//*[@data-item-marker='" + CONTRACT_PAGE_MARKER + "']")
                .shouldBe(Condition.visible);
    }

    @Step("Создание договора в АБС")
    private void createAbsContract() {
        log.info("Create ABS contract");

       /* click.clickByName("Действия");
        menu.clickByName("Создание договора");

        lookup.select("Вид планирования", "Аннуитетный_005");

    // Тип получения кредита — combobox с ul-листвью, marker=LoadCreditType
        lookup.selectComboboxByMarker("LoadCreditType", "Получает сегодня");

        click.clickByName("Выбрать");

        msgBox.shouldSeeModal("Нет задолженности!");
        click.clickByName("ОК");

        msgBox.shouldSeeModal("Договор успешно создан");
        click.clickByName("ОК"); */

        click.clickById(
                "BnzContractCreditPageBnzCreateSavingAcountContractButtonButton-imageEl"
        );

        msgBox.shouldSeeModal("Депозитный договор успешно создан в АБС");
        click.clickByName("ОК");

        click.clickByName("Закрыть");
        refresh();
    }

    @Step("Привязка счётов и графика платежей")
    private void bindAccountsAndSchedule() {
        log.info("Bind accounts and schedule");

        // Привязка созданного депозитного счёта: лупа поля "Счёт" →
        // поиск по сохранённому номеру → выбор.
        String depositAccount =
                ScenarioContextProvider.get().getDepositAccountNumber();
        lookup.selectInLookupByIdWithSearch(
                "BnzContractCreditPageBnzCreditBankAccountLookupEdit",
                depositAccount
        );

        click.clickByName("Сохранить");
        click.clickByName("Действия");

        msgBox.confirmOk("График платежей успешно получен");

        click.clickByName("Действия");

        msgBox.confirmOk("Счет успешно привязан к кредитному договору");
    }

    @Step("Подписание и выдача кредита")
    private void signAndIssueCredit() {
        log.info("Sign and issue credit");

        dashboard.clickAndWaitMiniPage(
                "Создание договора в АБС (печать договоров для встречи)",
                "Execute"
        );

        setFieldByMarker("Result", "Выполнена");
        menu.clickByName("Выполнена");
        click.clickByMarker("SaveEditButton");

        click.clickByName("Действия");

        msgBox.confirmOk("Резервирование счетов выполнено успешно");

        click.clickByName("Действия");
    }

    @Step("Проверка ордеров и печать")
    private void verifyOrdersAndPrint() {
        log.info("Verify orders and print");

        buttons.doubleClickByName("Приходный");
        basePage.checkPage("BnzOrderPageContainer");
        click.clickByName("Закрыть");

        buttons.doubleClickByName("Расходный");
        basePage.checkPage("BnzOrderPageContainer");
        click.clickByName("Закрыть");

        print.selectPrintOption("Чек лист маълумотнома(оферта)");
    }

    // ══════════════════════════════════════
    // COLLATERAL
    // ══════════════════════════════════════

    @Step("Обработка залога: {type}")
    public void processCollateral(CollateralType type) {
        openCollateralByName(type.getUiName());
        createCollateralContract();

        if (type.hasPrintForm()) {
            printCollateralContract(type.getPrintForm());
            waitForConfidantGrid();
            selectConfidantByPosition("Сардор дар Идораи амалиёт");
        }

        saveAndCloseCollateral();
    }

    private void openCollateralByName(String name) {
        SelenideElement row = $x(
                "//span[@grid-data-type='text'" +
                        " and normalize-space()='" + name + "']"
        ).shouldBe(Condition.visible);
        actions().doubleClick(row).perform();
    }

    private void createCollateralContract() {
        click.clickByName("Действия");
        menu.clickByName("Создание договора обеспечения");
        msgBox.confirmOk("Договор успешно создан");
    }

    private void printCollateralContract(String formName) {
        click.clickByName("Печать");
        $x("//ul[@data-item-marker='PrintButton']" +
                "//li[normalize-space()='" + formName + "']")
                .shouldBe(Condition.visible)
                .click();
    }

    private void waitForConfidantGrid() {
        $("[data-item-marker='Должностные лица']")
                .shouldBe(Condition.visible);
    }

    private void selectConfidantByPosition(String position) {
        $x("//span[@grid-data-type='text'" +
                " and normalize-space()='" + position + "']")
                .shouldBe(Condition.visible)
                .click();
        $x("//span[text()='Выбрать']")
                .shouldBe(Condition.visible)
                .click();
    }

    private void saveAndCloseCollateral() {
        click.clickByMarkerIfVisible("SaveButton");
        click.clickByMarkerIfVisible("SaveButton");
    }

    private void setFieldByMarker(String marker, String value) {
        $x("//div[@data-item-marker='" + marker + "']/input")
                .setValue(value);
    }
}