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
 * E2E-маршрут продукта «Агро» (сегмент «Микро»).
 * <p>
 * Sprint 2: маршрут собран от подбора продукта до стадии «Подписание»
 * включительно — регистрация, предварительная проверка, участники,
 * обеспечение, сбор документов, рассмотрение (кредитный эксперт +
 * верификатор), информирование клиента, вложение документов мидл-контролером,
 * подписание (решение КК → кредитный договор → депозитный счёт → график →
 * привязка счёта → договоры обеспечения с печатными формами → закрытие
 * активности).
 * <p>
 * Не реализовано: подписание договора, создание ордеров, печатная форма
 * «Оферта», проверка ордеров, кассовые операции.
 * <p>
 * Инфраструктурные флоу (Auth, Workspace, ClientSearch, ConsultationStart,
 * ApplicationCreation) переиспользуются из общей части — они не зависят от
 * продуктовой линейки. Отличие Микро: вход через роль «Кредитный эксперт»
 * (рабочее место {@code Workspace.CREDIT_EXPERT}), а не розничного менеджера.
 * Стыковка типов: {@link AgroProductData#toProductData()} для
 * {@code ApplicationCreationFlow}, типизированного под розничный ProductData.
 */
@Epic(AllureTags.Epics.AGRO)
@Feature(AllureTags.Features.E2E)
@Owner("Davlat")
public class AgroCreditApplicationTest extends BaseTest {

    // Номер заявки для debug-прогонов. Переопределяется через -Dapp=NNNNN.
    private static final String RESUME_APPLICATION_NUMBER = "";

    // ══════════════════════════════════════
    // ОСНОВНОЙ E2E-ПРОГОН
    // ══════════════════════════════════════

    @Test(groups = "e2e", enabled = true)
    @Story("Агро/Микро — маршрут от подбора продукта до стадии «Подписание»")
    @Description("Полный прогон реализованной части маршрута Агро/Микро: логин кредитного " +
            "эксперта, поиск клиента, старт консультации, подбор продукта, создание заявки, " +
            "регистрация, предварительная проверка, участники, обеспечение, сбор документов, " +
            "рассмотрение и утверждение верификатором, информирование клиента, вложение " +
            "документов мидл-контролером, стадия «Подписание» под ИКОК-ГО.")
    @Severity(SeverityLevel.BLOCKER)
    public void agroMicroPartialRoute() {

        TestScenario scenario = AgroScenarioFactory.agroMicroHappyPath();
        AgroProductData product = AgroScenarioFactory.agroMicroProduct();

        AuthFlow auth = new AuthFlow();
        WorkspaceFlow workspace = new WorkspaceFlow();

        // ── Шаг 1. Кредитный эксперт: логин + рабочее место ──
        // Продукты сегмента «Микро» ведёт Кредитный эксперт (не розничный менеджер).
        auth.login(scenario.getCreditExpert());
        workspace.select(Workspace.CREDIT_EXPERT);

        // ── Шаг 1. Поиск клиента по ФИО ──
        new ClientSearchFlow().searchClient(
                scenario.getContact().getLastName(),
                scenario.getContact().getFirstName(),
                scenario.getContact().getMiddleName()
        );

        // ── Шаг 2. Консультация ──
        // ВРЕМЕННО: проверка сегмента клиента отключена (неактуальна сейчас).
        // Поэтому консультация выполняется монолитно — как в рознице.
        // У Агро нет отдельной плитки продукта — marker темы тот же, что у розницы.
        new ConsultationStartFlow()
                .startConsultation(config.getConsultationTheme("retail"));

        // ── Шаг 2. Подбор продукта Агро ──
        new AgroProductSelectionFlow()
                .selectProduct(product);

        // ── Шаг 3. Создание заявки (через конвертацию в ProductData) ──
        new ApplicationCreationFlow()
                .createApplication(product.toProductData());

        // ── Шаг 4. Регистрация ──
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

        new AgroDocumentCollectionStageFlow()
                .completeDocumentCollectionStage();

        new AgroReviewStageFlow()
                .completeReview();

        auth.logout();

        // ── Верификатор: утверждение проекта решения ──
        auth.login(scenario.getUnderwriter());
        workspace.select(Workspace.UNDERWRITER);

        new AgroReviewStageUnderwriterFlow()
                .approveReview(AgroScenarioFactory.agroDecisionData());

        auth.logout();

        // ── Кредитный эксперт: информирование клиента ──
        auth.login(scenario.getCreditExpert());
        workspace.select(Workspace.CREDIT_EXPERT);

        new AgroClientNotificationStageFlow()
                .completeClientNotification(
                        AgroScenarioFactory.agroClientNotificationData(
                                scenario.getMiddleController(),
                                scenario.getIkokGo()));

        auth.logout();

        // ── Мидл-контролер: вложение документов заёмщика ──
        // На «Сборе документов» ничего не прикрепляли (сценарий Микро);
        // единственный документ заёмщика (Шиносномаи ЧТ) вкладывается здесь,
        // под ролью «Мидл контролер» — вход через рабочее место ИКОК.
        auth.login(scenario.getMiddleController());
        workspace.select(Workspace.IKOK);

        new AgroMiddleControllerDocumentsFlow()
                .attachBorrowerDocuments(
                        AgroScenarioFactory.agroMiddleControllerDocuments());

        auth.logout();

        // ── ИКОК-ГО: стадия «Подписание» ──
        // Решение КК → кредитный договор → депозитный счёт → график →
        // привязка счёта → договоры обеспечения с печатными формами →
        // закрытие активности создания договора.
        auth.login(scenario.getIkokGo());
        workspace.select(Workspace.IKOK_GO);

        new AgroSigningStageFlow().completeSigningStage();

        auth.logout();
    }

    // ══════════════════════════════════════
    // DEBUG / RESUME (временные входы)
    // ══════════════════════════════════════

    /**
     * ВРЕМЕННЫЙ вход: печать форм по уже зарегистрированным договорам
     * обеспечения. Номер кредитного договора задаётся в теле метода.
     */
    @Test(groups = "resume", enabled = false)
    @Story("Агро/Микро — DEBUG: печать форм по договорам обеспечения")
    @Description("Логин ИКОК-ГО, открытие кредитного договора по номеру, обход детали " +
            "«Договоры обеспечения» с печатью форм без повторного создания в АБС.")
    @Severity(SeverityLevel.CRITICAL)
    public void agroMicroResumeCollateralContracts() {
        TestScenario scenario = AgroScenarioFactory.agroMicroHappyPath();

        AuthFlow auth = new AuthFlow();
        WorkspaceFlow workspace = new WorkspaceFlow();

        auth.login(scenario.getIkokGo());
        workspace.select(Workspace.IKOK_GO);

        new AgroSigningStageFlow()
                .debugPrintCollateralContracts("02-008-033521");

        auth.logout();
    }

    /**
     * DEBUG: стадия «Подписание» по номеру заявки, доведённой до этой точки.
     * Номер: -Dapp=NNNNN или правка RESUME_APPLICATION_NUMBER.
     */
    @Test(groups = "resume", enabled = false)
    @Story("Агро/Микро — DEBUG: стадия «Подписание» по номеру заявки")
    @Description("Логин ИКОК-ГО, открытие заявки по номеру, стадия «Подписание» целиком: " +
            "решение КК → кредитный договор → депозитный счёт → график → привязка счёта → " +
            "договоры обеспечения → закрытие активности.")
    @Severity(SeverityLevel.CRITICAL)
    public void agroMicroResumeSigning() {
        String appNumber = System.getProperty("app", RESUME_APPLICATION_NUMBER);
        TestScenario scenario = AgroScenarioFactory.agroMicroHappyPath();

        AuthFlow auth = new AuthFlow();
        WorkspaceFlow workspace = new WorkspaceFlow();

        auth.login(scenario.getIkokGo());
        workspace.select(Workspace.IKOK_GO);

        ScenarioContextProvider.get().setApplicationNumber(appNumber);

        new AgroSigningStageFlow().completeSigningStage();

        auth.logout();
    }

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
}