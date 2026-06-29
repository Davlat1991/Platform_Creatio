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

@Epic(AllureTags.Epics.RETAIL)
@Feature(AllureTags.Features.E2E)
@Owner("Davlat")
public class CreditApplicationTest extends BaseTest {
// ══════════════════════════════════════
    // DEBUG — сохранение номера заявки в контекст
    // Заявка уже на стадии "Рассмотрение"
    // ══════════════════════════════════════

    @Test(groups = "debug", enabled = true)
    @Description("Debug — открыть заявку на стадии Рассмотрение и сохранить номер в контекст")
    public void debugSaveApplicationNumber() {
        TestScenario scenario = ScenarioFactory.happyPath();

        AuthFlow auth = new AuthFlow();
        auth.login(scenario.getManager());
        new WorkspaceFlow().select(Workspace.RETAIL_MANAGER);

        new NavigationFlow().open(
                config.getBaseUrl() +
                        "/0/Nui/ViewModule.aspx#CardModuleV2/" +
                        "FinApplicationPage/edit/" +
                        "7fbf7e9a-2483-49d3-a3b5-cf21d13a83a3"
        );

        new ReviewStageRetailFlow().completeReview(scenario.getRoute());

        auth.logout();
    }


   /* @Test(groups = "regression")
    @Story("Happy Path: от логина до выдачи кредита")
    @Description("Полный сквозной сценарий розничной кредитной заявки по всем ролям")
    @Severity(SeverityLevel.BLOCKER)
    // @TmsLink("CREATIO-123")   // раскомментируй после заполнения домена Jira в allure.properties
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

        new ReviewStageRetailFlow()
                .completeReview(scenario.getRoute());            // ← пишет applicationNumber в контекст

        auth.logout();

        // ── Верификатор ────────────────────────
        auth.login(scenario.getUnderwriter());
        workspace.select(Workspace.UNDERWRITER);

        new ReviewStageUnderwriterFlow()
                .approveReview("КК4 по заявке");  // ← читает из контекста

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
                .issueLoan();                   // ← читает из контекста

        auth.logout();

        // ── Кассир ─────────────────────────────
        /*auth.login(scenario.getCashier());
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
    }*/
}