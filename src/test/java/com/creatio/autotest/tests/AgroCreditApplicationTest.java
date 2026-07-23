package com.creatio.autotest.tests;

import com.creatio.autotest.base.BaseTest;
import com.creatio.autotest.flows.*;
import com.creatio.autotest.flows.micro.*;
import com.creatio.autotest.flows.micro.collateral.AgroCollateralStageFlow;
import com.creatio.autotest.flows.micro.participants.AgroParticipantsStageFlow;
import com.creatio.autotest.flows.micro.registration.*;
import com.creatio.autotest.flows.micro.registration.client.MicroClientFlowFactory;
import com.creatio.autotest.testdata.AgroScenarioFactory;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import com.creatio.autotest.testdata.models.AgroProductData;
import com.creatio.autotest.testdata.models.TestScenario;
import com.creatio.autotest.testdata.models.Workspace;
import com.creatio.autotest.utils.AllureTags;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

/**
 * E2E-маршрут продукта «Агро» (сегмент «Микро») — полный цикл от подбора
 * продукта до завершения заявки.
 * <p>
 * Роли по стадиям: Кредитный эксперт (регистрация → рассмотрение →
 * информирование клиента → завершение заявки), Верификатор (утверждение
 * проекта решения), Мидл-контролер (вложение документов заёмщика, прикрепление
 * кассового ордера), ИКОК-ГО (стадия «Подписание», подтверждение получения
 * документа), Кассир Микро (подтверждение кассовых ордеров).
 * <p>
 * Инфраструктурные флоу (Auth, Workspace, ClientSearch, ConsultationStart,
 * ApplicationCreation) переиспользуются из общей части — они не зависят от
 * продуктовой линейки. Отличие Микро: вход через роль «Кредитный эксперт»
 * (рабочее место {@code Workspace.CREDIT_EXPERT}), а не розничного менеджера.
 * Стыковка типов: {@link AgroProductData#toProductData()} для
 * {@code ApplicationCreationFlow}, типизированного под розничный ProductData.
 * <p>
 * Передача данных между ролями идёт через {@code ScenarioContextProvider}:
 * номер заявки пишет {@code AgroReviewStageFlow}, номер депозитного счёта и
 * номер кредитного договора — {@code AgroSigningStageFlow}.
 * <p>
 * Известные ограничения (на стороне настроек системы, не автотеста):
 * модальное окно выбора должностного лица при печати договоров обеспечения
 * не открывается; отображение кассовых ордеров под ролью ИКОК-ГО не настроено.
 * Соответствующие шаги реализованы и включатся без изменения кода.
 */
@Epic(AllureTags.Epics.AGRO)
@Feature(AllureTags.Features.E2E)
@Owner("Davlat")
public class AgroCreditApplicationTest extends BaseTest {

    // Номер заявки для debug-прогонов. Переопределяется через -Dapp=NNNNN.
    private static final String RESUME_APPLICATION_NUMBER = "392008";
    // Номер кредитного договора для debug-прогонов. Переопределяется через -Dcontract=...
    private static final String RESUME_CONTRACT_NUMBER = "02-008-033523";

    // ══════════════════════════════════════
    // ОСНОВНОЙ E2E-ПРОГОН
    // ══════════════════════════════════════

    @Test(groups = "e2e", enabled = true)
    @Story("Агро/Микро — полный маршрут: от подбора продукта до завершения заявки")
    @Description("Сквозной прогон продукта Агро сегмента «Микро»: подбор продукта, создание " +
            "заявки, регистрация, предварительная проверка, участники, обеспечение, сбор " +
            "документов, рассмотрение и утверждение верификатором, информирование клиента, " +
            "вложение документов мидл-контролером, стадия «Подписание» под ИКОК-ГО " +
            "(решение КК, кредитный договор, депозитный счёт, график, привязка счёта, " +
            "договоры обеспечения с печатными формами, подписание, выдача наличными, оферта), " +
            "кассовые операции под кассиром Микро и завершение заявки.")
    @Severity(SeverityLevel.BLOCKER)
    public void agroMicroFullRoute() {

        //TestScenario scenario = AgroScenarioFactory.agroMicroHappyPath();
        AgroProductData product = AgroScenarioFactory.agroMicroProduct();
        TestScenario scenario = AgroScenarioFactory.agroMicroRegression();


        AuthFlow auth = new AuthFlow();
        WorkspaceFlow workspace = new WorkspaceFlow();

        // ══════════════════════════════════════
        // КРЕДИТНЫЙ ЭКСПЕРТ: заявка → рассмотрение
        // ══════════════════════════════════════
        // Продукты сегмента «Микро» ведёт Кредитный эксперт (не розничный менеджер).
        auth.login(scenario.getCreditExpert());
        workspace.select(Workspace.CREDIT_EXPERT);

        new ClientSearchFlow().searchClient(
                scenario.getContact().getLastName(),
                scenario.getContact().getFirstName(),
                scenario.getContact().getMiddleName()
        );

        // ВРЕМЕННО: проверка сегмента клиента отключена (неактуальна сейчас).
        // Поэтому консультация выполняется монолитно — как в рознице.
        // У Агро нет отдельной плитки продукта — marker темы тот же, что у розницы.
        new ConsultationStartFlow()
                .startConsultation(config.getConsultationTheme("retail"));

        new AgroProductSelectionFlow()
                .selectProduct(product);

        new ApplicationCreationFlow()
                .createApplication(product.toProductData());

        // ── Регистрация ──
        // Общая информация (регион, источник, сектор экономики)
        new AgroRegistrationGeneralInfoFlow()
                .fillGeneralInfo(
                        scenario.getRegistration(),
                        AgroScenarioFactory.agroEconomicSector());

        // Участники заявки: добавить заёмщика → открывается анкета заёмщика
        new AgroRegistrationParticipantsFlow()
                .addBorrower();

        // Обязательные поля первой вкладки анкеты заёмщика — Микро-версии
        // (изолированы от розницы). Порядок: документы → адрес → доходы/расходы.
        new AgroRegistrationDocumentsFlow()
                .fillBorrowerDocuments(AgroScenarioFactory.agroBorrowerDocuments());

        AgroRegistrationAddressFlow addressFlow = new AgroRegistrationAddressFlow();
        addressFlow.fillAddresses(AgroScenarioFactory.agroAddressBase());
        addressFlow.setPzlRelation(false);

        new AgroRegistrationIncomeExpensesFlow()
                .fillIncomeAndExpenses(
                        scenario.getRegistration().getIncomeExpenses(),
                        AgroScenarioFactory.agroIncomeExtra());

        // Базовый скоринг (семейное положение, стаж) — вкладка «Дополнительная
        // информация» анкеты заёмщика.
        new AgroRegistrationAdditionalInfoFlow()
                .fillBaseScoringData(scenario.getRegistration().getScoringData());

        // Анкета клиента Микро — стратегия по типу занятости (data-driven).
        // Для фермера Агро → MicroSelfEmployedClientFlow.
        MicroClientFlowFactory
                .from(scenario.getRegistration().getClientProfile())
                .fill();

        new AgroRegistrationScoringConfirmationFlow()
                .confirmScoring(AgroScenarioFactory.agroScoringConfirmation());

        new AgroRegistrationPrintFlow()
                .printApplication();

        new AgroPreliminaryCheckFlow()
                .completePreliminaryCheckStage();

        new AgroParticipantsStageFlow()
                .completeParticipantsStage(scenario.getParticipants());

        new AgroCollateralStageFlow()
                .completeCollateralStage(scenario.getCollaterals());

        // По сценарию Микро документы на этой стадии не прикрепляются —
        // активность просто закрывается. Документ заёмщика вкладывается позже,
        // мидл-контролером.
        new AgroDocumentCollectionStageFlow()
                .completeDocumentCollectionStage();

        // Номер заявки кладётся в ScenarioContext — нужен последующим ролям.
        new AgroReviewStageFlow()
                .completeReview();

        auth.logout();

        // ══════════════════════════════════════
        // ВЕРИФИКАТОР: утверждение проекта решения
        // ══════════════════════════════════════
        auth.login(scenario.getUnderwriter());
        workspace.select(Workspace.UNDERWRITER);

        new AgroReviewStageUnderwriterFlow()
                .approveReview(AgroScenarioFactory.agroDecisionData());

        auth.logout();

        // ══════════════════════════════════════
        // КРЕДИТНЫЙ ЭКСПЕРТ: информирование клиента
        // ══════════════════════════════════════
        auth.login(scenario.getCreditExpert());
        workspace.select(Workspace.CREDIT_EXPERT);

        new AgroClientNotificationStageFlow()
                .completeClientNotification(
                        AgroScenarioFactory.agroClientNotificationData(
                                scenario.getMiddleController(),
                                scenario.getIkokGo()));

        auth.logout();

        // ══════════════════════════════════════
        // МИДЛ-КОНТРОЛЕР: вложение документов заёмщика
        // ══════════════════════════════════════
        // Единственный документ заёмщика (Шиносномаи ЧТ) вкладывается здесь —
        // на «Сборе документов» по сценарию Микро ничего не прикрепляли.
        // Мидл-контролер — функциональная роль, вход через рабочее место ИКОК.
        auth.login(scenario.getMiddleController());
        workspace.select(Workspace.IKOK);

        new AgroMiddleControllerDocumentsFlow()
                .attachBorrowerDocuments(
                        AgroScenarioFactory.agroMiddleControllerDocuments());

        auth.logout();

        // ══════════════════════════════════════
        // ИКОК-ГО: стадия «Подписание»
        // ══════════════════════════════════════
        // Часть 1: решение КК → кредитный договор → депозитный счёт → график →
        //          привязка счёта → договоры обеспечения → закрытие активности.
        // Часть 2: подписание договора → выдача наличными → оферта.
        // Обе части выполняются на одной открытой странице кредитного договора;
        // номер договора кладётся в ScenarioContext — нужен кассиру и финишу.
        auth.login(scenario.getIkokGo());
        workspace.select(Workspace.IKOK_GO);

        AgroSigningStageFlow signingFlow = new AgroSigningStageFlow();
        signingFlow.completeSigningStage();
        signingFlow.signAndIssueCredit();

        auth.logout();

        // ══════════════════════════════════════
        // КАССИР МИКРО: подтверждение ордеров
        // ══════════════════════════════════════
        // Отдельная учётка кассира для Микро — функционально роль та же
        // (кассир подразделения видит все ордера), разделение нужно для
        // прозрачности прогонов и логов.
        auth.login(scenario.getMicroCashier());
        workspace.select(Workspace.CASHIER);

        new AgroCashierFlow().completeCashOperations();

        auth.logout();

        // ══════════════════════════════════════
        // ЗАВЕРШЕНИЕ ЗАЯВКИ
        // ══════════════════════════════════════
        AgroApplicationFinishFlow finishFlow = new AgroApplicationFinishFlow();

        // Прикрепление расходного кассового ордера. По текущей доработке
        // процесса Микро ответственный — мидл-контролер; когда доработку
        // завершат, здесь вернётся кредитный эксперт (правка только теста).
        auth.login(scenario.getMiddleController());
        workspace.select(Workspace.IKOK);

        finishFlow.completeAttachOrder();

        auth.logout();

        // ИКОК-ГО: скачивание ордера и подтверждение получения документа
        auth.login(scenario.getIkokGo());
        workspace.select(Workspace.IKOK_GO);

        finishFlow.completeIkokGoConfirm();

        auth.logout();

        // Кредитный эксперт: завершение заявки через консультационную панель
        auth.login(scenario.getCreditExpert());
        workspace.select(Workspace.CREDIT_EXPERT);

        finishFlow.completeApplicationViaConsultation();

        auth.logout();
    }

    // ══════════════════════════════════════
    // DEBUG / RESUME (временные входы)
    // ══════════════════════════════════════

    /**
     * DEBUG: вложение документов мидл-контролером по номеру заявки.
     * Экономит полный прогон регистрации на каждой итерации.
     */
    @Test(groups = "resume", enabled = false)
    @Story("Агро/Микро — DEBUG: вложение документов мидл-контролером")
    @Description("Логин мидл-контролера (рабочее место ИКОК), открытие существующей заявки " +
            "по номеру и вложение единственного документа заёмщика.")
    @Severity(SeverityLevel.NORMAL)
    public void agroMicroResumeMiddleControllerDocuments() {
        String appNumber = System.getProperty("app", RESUME_APPLICATION_NUMBER);
        TestScenario scenario = AgroScenarioFactory.agroMicroHappyPath();

        AuthFlow auth = new AuthFlow();
        WorkspaceFlow workspace = new WorkspaceFlow();

        auth.login(scenario.getMiddleController());
        workspace.select(Workspace.IKOK);

        ScenarioContextProvider.get().setApplicationNumber(appNumber);

        new AgroMiddleControllerDocumentsFlow()
                .attachBorrowerDocuments(
                        AgroScenarioFactory.agroMiddleControllerDocuments());

        auth.logout();
    }

    /**
     * DEBUG: стадия «Подписание» по номеру заявки, доведённой до этой точки.
     * Номер: -Dapp=NNNNN или правка RESUME_APPLICATION_NUMBER.
     */
    @Test(groups = "resume", enabled = false)
    @Story("Агро/Микро — DEBUG: стадия «Подписание» по номеру заявки")
    @Description("Логин ИКОК-ГО, открытие заявки по номеру, стадия «Подписание»: решение КК → " +
            "кредитный договор → депозитный счёт → график → привязка счёта → договоры " +
            "обеспечения → закрытие активности → подписание → выдача наличными → оферта.")
    @Severity(SeverityLevel.CRITICAL)
    public void agroMicroResumeSigning() {
        String appNumber = System.getProperty("app", RESUME_APPLICATION_NUMBER);
        TestScenario scenario = AgroScenarioFactory.agroMicroHappyPath();

        AuthFlow auth = new AuthFlow();
        WorkspaceFlow workspace = new WorkspaceFlow();

        auth.login(scenario.getIkokGo());
        workspace.select(Workspace.IKOK_GO);

        ScenarioContextProvider.get().setApplicationNumber(appNumber);

        AgroSigningStageFlow signingFlow = new AgroSigningStageFlow();
        signingFlow.completeSigningStage();
        signingFlow.signAndIssueCredit();

        auth.logout();
    }

    /**
     * DEBUG: кассовые операции по номеру кредитного договора.
     */
    @Test(groups = "resume", enabled = false)
    @Story("Агро/Микро — DEBUG: кассовые операции")
    @Description("Логин кассира Микро, открытие кредитного договора по номеру, подтверждение " +
            "приходного и расходного кассовых ордеров с печатью форм.")
    @Severity(SeverityLevel.CRITICAL)
    public void agroMicroResumeCashier() {
        String contractNumber = System.getProperty("contract", RESUME_CONTRACT_NUMBER);
        TestScenario scenario = AgroScenarioFactory.agroMicroHappyPath();

        AuthFlow auth = new AuthFlow();
        WorkspaceFlow workspace = new WorkspaceFlow();

        auth.login(scenario.getMicroCashier());
        workspace.select(Workspace.CASHIER);

        ScenarioContextProvider.get().setCreditContractNumber(contractNumber);

        new AgroCashierFlow().completeCashOperations();

        auth.logout();
    }

    /**
     * DEBUG: три финальных шага завершения заявки по номеру кредитного договора.
     */
    @Test(groups = "resume", enabled = false)
    @Story("Агро/Микро — DEBUG: завершение заявки")
    @Description("Прикрепление расходного кассового ордера и закрытие активности, " +
            "подтверждение получения документа под ИКОК-ГО, завершение заявки " +
            "через консультационную панель.")
    @Severity(SeverityLevel.CRITICAL)
    public void agroMicroResumeFinish() {
        String contractNumber = System.getProperty("contract", RESUME_CONTRACT_NUMBER);
        TestScenario scenario = AgroScenarioFactory.agroMicroHappyPath();

        AuthFlow auth = new AuthFlow();
        WorkspaceFlow workspace = new WorkspaceFlow();
        AgroApplicationFinishFlow finishFlow = new AgroApplicationFinishFlow();

        ScenarioContextProvider.get().setCreditContractNumber(contractNumber);

        auth.login(scenario.getMiddleController());
        workspace.select(Workspace.IKOK);
        finishFlow.completeAttachOrder();
        auth.logout();

        auth.login(scenario.getIkokGo());
        workspace.select(Workspace.IKOK_GO);
        finishFlow.completeIkokGoConfirm();
        auth.logout();

        auth.login(scenario.getCreditExpert());
        workspace.select(Workspace.CREDIT_EXPERT);
        finishFlow.completeApplicationViaConsultation();
        auth.logout();
    }
}