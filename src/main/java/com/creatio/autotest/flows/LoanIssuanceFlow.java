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
import com.creatio.autotest.utils.FieldUtils;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
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

        //completeKkDecisionCheck();
        openContract();
       // createAbsContract();
       // bindAccountsAndSchedule();
       // signAndCreateOrders();
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
        /*dashboard.clickByName(
                "Создание договора в АБС (печать договоров для встречи)"*/

        dashboard.clickByName(
                        "Сформируйте РКО"

        );

        // В детали "Связи" кликаем по номеру временного договора.
        // Скоуп marker='Contract' + href на страницу договора отсекает значок
        // (marker='ContractImage') и соседние ссылки (FinApplication, CreatedBy).
        SelenideElement contractLink = $x(
                "//a[@data-item-marker='Contract'" +
                        " and contains(@href,'BnzContractCreditPage/edit')]"
        ).shouldBe(visible);

        // JS-клик: нативный клик по ссылке в мини-панели перехватывается значком документа.
        click.jsClick(contractLink);

        // Только теперь открывается страница временного договора.
        $x("//*[@data-item-marker='" + CONTRACT_PAGE_MARKER + "']")
                .shouldBe(visible);
    }

    @Step("Создание договора в АБС")
    private void createAbsContract() {
        log.info("Create ABS contract");

        click.clickByName("Действия");
        menu.clickByName("Создание договора");

        lookup.select("Вид планирования", "Аннуитетный_005");

    // Тип получения кредита — combobox с ul-листвью, marker=LoadCreditType
        lookup.selectComboboxByMarker("LoadCreditType", "Получает сегодня");

        click.clickByName("Выбрать");

        msgBox.shouldSeeModal("Нет задолженности!");
        click.clickByName("ОК");

        msgBox.shouldSeeModal("Договор успешно создан");
        click.clickByName("ОК");


    }

    @Step("Создание и привязка счётов и графика платежей")
    private void bindAccountsAndSchedule() {
        log.info("Bind accounts and schedule");

        click.clickById(
                "BnzContractCreditPageBnzCreateSavingAcountContractButtonButton-imageEl"
        );

        msgBox.shouldSeeModal("Депозитный договор успешно создан в АБС");
        click.clickByName("ОК");

        // Сохраняем номер депозитного счёта из ссылки поля (input пустой/readonly)
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
        lookup.selectInLookupByIdWithSearch(
                "BnzContractCreditPageBnzCreditBankAccountLookupEdit",
                depositAccount
        );

        click.clickByName("Сохранить");

        // Получение графика платежей
        click.clickByName("Действия");
        menu.clickByName("Получение графика платежей");

        msgBox.shouldSeeModal("График платежей успешно получен");
        click.clickByName("ОК");


        // Привязка счёта к договору
        click.clickByName("Действия");
        menu.clickByName("Привязка счета к договору");
        msgBox.shouldSeeModal("Счет успешно привязан к кредитному договору");
        click.clickByName("ОК");

    }


    @Step("Подписание и выдача кредита")
    private void signAndCreateOrders() {
        log.info("Sign and create orders");

        dashboard.clickAndWaitMiniPage("Создание договора в АБС (печать договоров для встречи)","Execute");

        setFieldByMarker("Result", "Выполнена");
        menu.clickByName("Выполнена");
        click.clickByMarker("SaveEditButton");

        click.clickByName("Действия");
        menu.clickByName("Подписание договора");
        msgBox.shouldSeeModal("Резервирование счетов выполнено успешно");
        click.clickByName("ОК");

        issueCashCredit();
    }

    @Step("Выдача кредита наличными")
    private void issueCashCredit() {
        log.info("Issue cash credit");

        // Действия → Выдача кредита → Наличными
        click.clickByName("Действия");
        menu.hoverAndClickSubmenu("Выдача кредита", "Наличными");

        // ── Модалка "Выдача кредита" ──
        // Сумма к выдаче (marker=Amount) должна быть заполнена.
        String issueAmount = FieldUtils.getValueByMarker("Amount");
        log.info("Сумма к выдаче: '{}'", issueAmount);

        click.clickByName("Подтвердить");

        // ── Модалка "Комиссия за выдачу кредита" ──
        // Это модальная СТРАНИЦА (ts-modalbox), не messagebox — якорем появления
        // служит само чтение полей ниже (getValueByMarker ждёт поле до 30с).
        // Проверяем что все три поля комиссии заполнены (readonly, значения от суммы).
        // Не завязываемся на конкретные числа — они зависят от суммы кредита.
        String commissionBase    = FieldUtils.getValueByMarker("IssuanceAmount");
        String commissionPercent = FieldUtils.getValueByMarker("CommissionPercentage");
        String commissionAmount  = FieldUtils.getValueByMarker("CommissionAmount");
        log.info("Комиссия: сумма выдачи='{}', процент='{}', сумма комиссии='{}'",
                commissionBase, commissionPercent, commissionAmount);

        click.clickByName("Подтвердить");

        // ── Три подтверждающих сообщения ──
        msgBox.shouldSeeModal(
                "Действие успешно выполнено. Необходимо оплатить комиссию за кредит");
        click.clickByName("ОК");

        msgBox.shouldSeeModal("Кассовый ордер успешно сформирован");
        click.clickByName("ОК");

        msgBox.shouldSeeModal("График платежей успешно получен");
        click.clickByName("ОК");
    }

    @Step("Проверка ордеров и печать")
    private void verifyOrdersAndPrint() {
        log.info("Verify orders and print");

        // Сохраняем номер финального кредитного договора из заголовка страницы.
        // input поля пустой — номер только в label заголовка (data-item-marker='Кредитный').
        // Нужен кассиру для поиска договора в разделе "Договоры".
        String contractNumber = $x(
                "//label[@id='MainHeaderSchemaPageHeaderCaptionLabel']"
        ).shouldBe(Condition.visible, Duration.ofSeconds(15))
                .shouldHave(Condition.matchText("\\d"), Duration.ofSeconds(10))
                .getText().trim();

        ScenarioContextProvider.get().setCreditContractNumber(contractNumber);
        log.info("Номер кредитного договора '{}' сохранён в контекст", contractNumber);

        print.selectPrintOption("Чек лист маълумотнома(оферта)");

        openDecisionTab();

        buttons.doubleClickByName("Приходный");
        basePage.checkPage("BnzOrderPageContainer");
        click.clickByName("Закрыть");

        buttons.doubleClickByName("Расходный");
        basePage.checkPage("BnzOrderPageContainer");
        click.clickByName("Закрыть");
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
        ).shouldBe(visible);
        actions().doubleClick(row).perform();
    }

    private void createCollateralContract() {
        click.clickByName("Действия");
        menu.clickByName("Создание договора обеспечения");
        msgBox.shouldSeeModal("Договор успешно создан");
        click.clickByName("ОК");
    }

    private void printCollateralContract(String formName) {
        click.clickByName("Печать");
        $x("//ul[@data-item-marker='PrintButton']" +
                "//li[normalize-space()='" + formName + "']")
                .shouldBe(visible)
                .click();
    }

    private void waitForConfidantGrid() {
        $("[data-item-marker='Должностные лица']")
                .shouldBe(visible);
    }

    private void selectConfidantByPosition(String position) {
        $x("//span[@grid-data-type='text'" +
                " and normalize-space()='" + position + "']")
                .shouldBe(visible)
                .click();
        $x("//span[text()='Выбрать']")
                .shouldBe(visible)
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

    @Step("Открыть вкладку 'Решение по заявке'")
    private void openDecisionTab() {
        buttons.clickTabByName("Операции по договору");
    }

}