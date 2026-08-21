package com.creatio.autotest.flows.retail;

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
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public class SigningStageFlow {

    private static final Logger log =
            LoggerFactory.getLogger(SigningStageFlow.class);

    private static final String CONTRACT_PAGE_MARKER =
            "BnzContractCreditPageContainer";

    private static final String CONTRACT_OPEN_ACTIVITY =
            "Создание договора в АБС (печать договоров для встречи)";

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
    private final CollateralContractsFlow
                                 collateralContracts = new CollateralContractsFlow();



    // ══════════════════════════════════════
    // MAIN
    // ══════════════════════════════════════

    /**
     * @param registerCollateralContracts регистрировать ли договоры обеспечения
     *        на открытом кредитном договоре. true — заявки с поручителем и/или
     *        залогом (стандартный маршрут); false — упрощённый маршрут, где
     *        деталь «Договоры обеспечения» пуста.
     * @param verifyOrders проверять ли приходный/расходный ордер после печати
     *        оферты. true — упрощённый маршрут (настроено и работает); false —
     *        стандартный маршрут (в текущей конфигурации системы не настроено).
     */
    @Step("Стадия «Подписание»: выдача (обеспечение={0}, проверка ордеров={1})")
    public void issueLoan(boolean registerCollateralContracts, boolean verifyOrders) {
        String applicationNumber =
                ScenarioContextProvider.get().getApplicationNumber();
        log.info("Issue loan: number='{}', обеспечение={}, проверка ордеров={}",
                applicationNumber, registerCollateralContracts, verifyOrders);

        consultation.closeIfOpened();

        searchFlow.withApplicationNumber(applicationNumber).openByNumber();

        completeKkDecisionCheck();
        openContract();
        createAbsContract();
        bindAccountsAndSchedule();

        // Договоры обеспечения регистрируются на открытом кредитном договоре,
        // ДО закрытия активности «Создание договора в АБС» (её закрывает
        // signAndCreateOrders). Точка вставки и порядок — как в AgroSigningStageFlow.
        if (registerCollateralContracts) {
            collateralContracts.processAllCollateralContracts();
        }

        signAndCreateOrders();
        verifyOrdersAndPrint(verifyOrders);
    }

    // ══════════════════════════════════════
    // STEPS
    // ══════════════════════════════════════

    @Step("Проверка решения КК")
    private void completeKkDecisionCheck() {
        log.info("Complete KK decision check");

        dashboard.clickAndWaitMiniPage("Проверка решения КК", "Execute");

        menu.openResultDropdown();

        menu.clickByName(ActivityResult.COMPLETED.getUiText());
        click.clickByMarker("SaveEditButton");
    }

    @Step("Открыть договор")
    private void openContract() {
        // Клик по названию активности открывает мини-панель "Связи", а не страницу договора.
        dashboard.clickByName(
                "Создание договора в АБС (печать договоров для встречи)" );

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

        // После закрытия страницы сберегательного счёта refresh() возвращается
        // до загрузки данных договора — ждём готовности перед привязкой счёта.
        // Паттерн из AgroSigningStageFlow.waitContractPageReady().
        waitContractPageReady();

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

    /**
     * Ждёт готовности страницы кредитного договора: маркер страницы + активность
     * договора в дашборде + waitForPage. Нужно после закрытия страницы
     * сберегательного счёта — refresh() возвращается до загрузки данных.
     * Паттерн из AgroSigningStageFlow.waitContractPageReady().
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


    @Step("Подписание и выдача кредита")
    private void signAndCreateOrders() {
        log.info("Sign and create orders");

        dashboard.clickAndWaitMiniPage("Создание договора в АБС (печать договоров для встречи)","Execute");

        menu.openResultDropdown();

        menu.clickByName(ActivityResult.COMPLETED.getUiText());
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
    private void verifyOrdersAndPrint(boolean verifyOrders) {
        log.info("Verify orders and print (verifyOrders={})", verifyOrders);

        // Сохраняем номер финального кредитного договора из заголовка страницы.
        // input поля пустой — номер только в label заголовка (data-item-marker='Кредитный').
        // Нужен кассиру для поиска договора в разделе "Договоры".
        String contractNumber = $x(
                "//label[@id='MainHeaderSchemaPageHeaderCaptionLabel']"
        ).shouldBe(Condition.visible, Duration.ofSeconds(15))
                .shouldHave(Condition.matchText("\\d"), Duration.ofSeconds(20))
                .getText().trim();

        ScenarioContextProvider.get().setCreditContractNumber(contractNumber);
        log.info("Номер кредитного договора '{}' сохранён в контекст", contractNumber);

        print.selectPrintOption("Чек лист маълумотнома(оферта)");

        // Проверка приходного/расходного ордера на стороне ИКОК-ГО. Нужна
        // упрощённому маршруту; для стандартного в текущей конфигурации системы
        // не настроена — выполняется только при verifyOrders=true.
        if (verifyOrders) {
            openDecisionTab();

            buttons.doubleClickByName("Приходный");
            basePage.checkPage("BnzOrderPageContainer");
            click.clickByName("Закрыть");

            buttons.doubleClickByName("Расходный");
            basePage.checkPage("BnzOrderPageContainer");
            click.clickByName("Закрыть");
        }
    }


    @Step("Открыть вкладку 'Решение по заявке'")
    private void openDecisionTab() {
        buttons.clickTabByName("Операции по договору");
    }


    // ══════════════════════════════════════
    // DEBUG — дорегистрация на существующей заявке
    // ══════════════════════════════════════

    /**
     * DEBUG: дорегистрация договоров обеспечения на УЖЕ существующей заявке —
     * без прохождения маршрута заново.
     * <p>
     * Открывает кредитный договор существующей заявки и запускает только
     * {@link CollateralContractsFlow#processAllCollateralContracts()}: проверка
     * КК, создание договора в АБС и привязка счетов пропускаются (уже сделаны).
     * Регистрирует оставшиеся временные договоры (370…); зарегистрированные
     * (Z-001-…) флоу пропускает сам.
     * <p>
     * Предусловие: стадия подписания НЕ завершена, поэтому активность
     * «Создание договора в АБС …» ещё открыта — по ней {@code openContract()}
     * находит договор. Номер заявки берётся из контекста ({@code resumeAt}).
     */
    @Step("DEBUG: дорегистрация договоров обеспечения на существующей заявке")
    public void debugResumeCollateralContracts() {
        String applicationNumber =
                ScenarioContextProvider.get().getApplicationNumber();
        log.info("DEBUG resume collateral contracts: application='{}'", applicationNumber);

        consultation.closeIfOpened();
        searchFlow.withApplicationNumber(applicationNumber).openByNumber();
        openContract();

        collateralContracts.processAllCollateralContracts();

        log.info("DEBUG: дорегистрация договоров обеспечения завершена");
    }

}