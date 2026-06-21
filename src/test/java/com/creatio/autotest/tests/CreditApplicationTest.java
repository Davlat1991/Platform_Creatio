package com.creatio.autotest.tests;

import com.creatio.autotest.base.BaseTest;
import com.creatio.autotest.flows.*;
import com.creatio.autotest.flows.collateral.CollateralStageFlow;
import com.creatio.autotest.flows.participants.ParticipantsStageFlow;
import com.creatio.autotest.flows.registration.RegistrationStageFlow;
import com.creatio.autotest.testdata.ScenarioFactory;
import com.creatio.autotest.testdata.models.TestScenario;
import com.creatio.autotest.testdata.models.Workspace;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

@Feature("Credit Application")
public class CreditApplicationTest extends BaseTest {

    @Test(groups = "regression")
    @Description("Happy Path — полный сценарий кредитной заявки")
    @Severity(SeverityLevel.BLOCKER)
    public void creditApplicationHappyPath() {

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

        // API загрузка документов
        // new DocumentsApiFlow().uploadAllDocumentsViaApi();

        String applicationNumber = new ReviewStageRetailFlow()
                .completeReview();

        auth.logout();

        // ── Верификатор ────────────────────────
        auth.login(scenario.getUnderwriter());
        workspace.select(Workspace.UNDERWRITER);

        new ReviewStageUnderwriterFlow()
                .approveReview(applicationNumber, "КК4 по заявке");

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
                .withApplicationNumber(applicationNumber)
                .issueLoan();

        auth.logout();

        // ── Кассир ─────────────────────────────
        auth.login(scenario.getCashier());
        workspace.select(Workspace.CASHIER);

        new SigningStageFlow()
                .withContractNumber(applicationNumber)
                .completeSigningStage();

        auth.logout();

        // ── ИКОК-ГО (завершение) ───────────────
        auth.login(scenario.getIkokGo());
        workspace.select(Workspace.IKOK_GO);

        new ApplicationFinishFlow()
                .completeApplicationFinish();

        auth.logout();
    }
}