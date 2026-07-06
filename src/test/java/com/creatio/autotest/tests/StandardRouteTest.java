package com.creatio.autotest.tests;

import com.creatio.autotest.base.BaseTest;
import com.creatio.autotest.flows.*;
import com.creatio.autotest.flows.collateral.CollateralStageFlow;
import com.creatio.autotest.flows.participants.ParticipantsStageFlow;
import com.creatio.autotest.flows.registration.RegistrationStageFlow;
import com.creatio.autotest.testdata.ScenarioFactory;
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
 * Стандартный маршрут (30001–100000 сомони).
 * Тип клиента: "Имеет другой источник дохода" (OTHER_INCOME, дефолт фабрики).
 * Поручитель + залогодатель + залоги.
 * Скоринг даёт любое из 4 решений; заявка ВСЕГДА идёт через
 * верификатора и кредитный комитет (КК4).
 */
@Epic(AllureTags.Epics.RETAIL)
@Feature(AllureTags.Features.E2E)
@Owner("Davlat")
public class StandardRouteTest extends BaseTest {


    @Test(groups = "debug", enabled = false)
    @Story("Debug — от стадии Рассмотрение до конца")
    @Description("Старт на заявке в стадии Рассмотрение по UUID, дальше весь процесс")
    @Severity(SeverityLevel.NORMAL)
    public void debugFromReviewStage() {
        TestScenario scenario = ScenarioFactory.happyPath();

        AuthFlow auth = new AuthFlow();
        WorkspaceFlow workspace = new WorkspaceFlow();

        // ── Розничный менеджер: заходим на заявку в стадии "Рассмотрение" ──
       auth.login(scenario.getManager());
        workspace.select(Workspace.RETAIL_MANAGER);

        new NavigationFlow().open(
                config.getBaseUrl() +
                        "/0/Nui/ViewModule.aspx#CardModuleV2/" +
                        "FinApplicationPage/edit/" +
                        "99a45586-1ed5-4e23-acb8-70f833202c5c"
        );

        // completeReview сам сохранит applicationNumber в контекст
        new ReviewStageRetailFlow().completeReview(
                scenario.getRoute(),
                scenario.getExpectedScoringDecision()
        );

        auth.logout();

         // ── Верификатор ────────────────────────
        auth.login(scenario.getUnderwriter());
        workspace.select(Workspace.UNDERWRITER);

        new ReviewStageUnderwriterFlow()
                .approveReview("КК4 по заявке");

        auth.logout();

        // ── Розничный менеджер (уведомление) ───
        auth.login(scenario.getManager());
        workspace.select(Workspace.RETAIL_MANAGER);

        new ClientNotificationStageFlow()
                .completeClientNotificationWithSearch(scenario.getIkokGo().getDisplayName());

        auth.logout();

        // ── ИКОК-ГО (выдача) ───────────────────
        auth.login(scenario.getIkokGo());
        workspace.select(Workspace.IKOK_GO);

        new LoanIssuanceFlow()
                .issueLoan();

        auth.logout();
    }



    @Test(groups = "regression")
    @Story("Стандартный маршрут — полный путь с верификатором и комитетом")
    @Description("Стандартный маршрут — полный happy path с верификатором и комитетом")
    @Severity(SeverityLevel.BLOCKER)
    public void standardRouteHappyPath() {

        TestScenario scenario = ScenarioFactory.happyPath();

        AuthFlow auth = new AuthFlow();
        WorkspaceFlow workspace = new WorkspaceFlow();

        // ── Розничный менеджер ─────────────────
        auth.login(scenario.getManager());
        workspace.select(Workspace.RETAIL_MANAGER);

        new ClientSearchFlow().searchClient(
                scenario.getContact().getLastName(),
                scenario.getContact().getFirstName(),
                scenario.getContact().getMiddleName()
        );

        new ConsultationStartFlow()
                .startConsultation(config.getConsultationTheme("retail"));

        new ProductSelectionFlow()
                .selectProduct(scenario.getProduct());

        new ApplicationCreationFlow()
                .createApplication(scenario.getProduct());

        new RegistrationStageFlow()
                .completeRegistrationStage(scenario.getRegistration());

        new PreliminaryCheckStageFlow()
                .completePreliminaryCheckStage();

        new ParticipantsStageFlow()
                .completeParticipantsStage(
                        scenario.getParticipants(),
                        scenario.getRegistration().getIncomeExpenses()
                );

        new CollateralStageFlow()
                .completeCollateralStage(scenario.getCollaterals());

        // ── Сбор документов (заёмщик + поручитель + залогодатель) ──
        // ── Сбор документов (заёмщик + поручитель + залогодатель) ──
        DocumentCollectionStageFlow documents = new DocumentCollectionStageFlow();
        documents.openDocumentsTab();

        // Заёмщик — ДВА шага: сначала подготовка (наполняет карту), потом загрузка
        documents.prepareBorrowerDetails();
        documents.uploadBorrowerDocuments();

        // Поручитель + залогодатель
        documents.uploadAllParticipantDocuments(ScenarioFactory.documentOwners());

        documents.completeDocumentsActivity();

        // ── Рассмотрение (STANDARD: КК4) ───────
        new ReviewStageRetailFlow().completeReview(
                scenario.getRoute(),
                scenario.getExpectedScoringDecision()
        );

        auth.logout();

        // ── Верификатор ────────────────────────
        auth.login(scenario.getUnderwriter());
        workspace.select(Workspace.UNDERWRITER);

        new ReviewStageUnderwriterFlow()
        .approveReview("КК4 по заявке");

        auth.logout();

        // ── Розничный менеджер (уведомление) ───
        auth.login(scenario.getManager());
        workspace.select(Workspace.RETAIL_MANAGER);

        new ClientNotificationStageFlow()
                .completeClientNotificationWithSearch(scenario.getIkokGo().getDisplayName());

        auth.logout();

        // ── ИКОК-ГО ────────────────────────────
        auth.login(scenario.getIkokGo());
        workspace.select(Workspace.IKOK_GO);

        new LoanIssuanceFlow()
                .issueLoan();

        auth.logout();

        // ── Кассир ─────────────────────────────
        // Кассир ищет 10-значный номер договора (раздел "Договоры").
        auth.login(scenario.getCashier());
        workspace.select(Workspace.CASHIER);

        new SigningStageFlow().completeSigningStage();

        auth.logout();

        // ── ИКОК-ГО (завершение) ───────────────
        auth.login(scenario.getIkokGo());
        workspace.select(Workspace.IKOK_GO);

        new ApplicationFinishFlow()
                .completeApplicationFinish();

        auth.logout();
    }
}