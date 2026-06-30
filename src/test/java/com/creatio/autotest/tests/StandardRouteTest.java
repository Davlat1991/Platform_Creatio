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
        // TODO: развернуть рабочий блок D4.1–D4.5 после живой проверки.
        new DocumentCollectionStageFlow().uploadBorrowerDocuments();
        new DocumentCollectionStageFlow().uploadAllParticipantDocuments(
                 ScenarioFactory.documentOwners());
        new DocumentCollectionStageFlow().completeDocumentsActivity();

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
                .completeClientNotification(scenario.getIkokGo().getDisplayName());

        auth.logout();

        // ── ИКОК-ГО ────────────────────────────
        auth.login(scenario.getIkokGo());
        workspace.select(Workspace.IKOK_GO);

        new LoanIssuanceFlow()
                .issueLoan();

        auth.logout();

        // ── Кассир ─────────────────────────────
        // TODO: активировать после миграции creditContractNumber на ScenarioContext.
        // Кассир ищет 10-значный номер договора (раздел "Договоры").
        // auth.login(scenario.getCashier());
        // workspace.select(Workspace.CASHIER);
        // new SigningStageFlow().completeSigningStage();
        // auth.logout();

        // ── ИКОК-ГО (завершение) ───────────────
        auth.login(scenario.getIkokGo());
        workspace.select(Workspace.IKOK_GO);

        new ApplicationFinishFlow()
                .completeApplicationFinish();

        auth.logout();
    }
}