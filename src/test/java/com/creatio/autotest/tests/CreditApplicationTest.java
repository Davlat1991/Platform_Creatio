package com.creatio.autotest.tests;

import com.creatio.autotest.base.BaseTest;
import com.creatio.autotest.flows.*;
import com.creatio.autotest.flows.collateral.CollateralStageFlow;
import com.creatio.autotest.flows.participants.ParticipantsStageFlow;
import com.creatio.autotest.flows.registration.RegistrationStageFlow;
import com.creatio.autotest.testdata.BaseScoringScenarios;
import com.creatio.autotest.testdata.CollateralDataFactory;
import com.creatio.autotest.testdata.ScenarioFactory;
import com.creatio.autotest.testdata.models.*;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import java.util.List;

@Feature("Credit Application")
public class CreditApplicationTest extends BaseTest {

    @Test(groups = "regression")
    @Description("Happy Path — полный сценарий кредитной заявки")
    @Severity(SeverityLevel.BLOCKER)
    public void creditApplicationHappyPath() {

        // ══════════════════════════════════════
        // 1. ДАННЫЕ СЦЕНАРИЯ
        // ══════════════════════════════════════

        TestScenario scenario = ScenarioFactory.happyPath();

        RegistrationData registration = scenario.getRegistration();

        List<ParticipantData> participants = List.of(
                ParticipantData.guarantor(false),
                ParticipantData.pledger(false)
        );

        List<CollateralData> collaterals = List.of(
                CollateralDataFactory.cashDeposit(CurrencyType.TJS),
                CollateralDataFactory.realEstate(CurrencyType.TJS),
                CollateralDataFactory.vehicle(CurrencyType.TJS),
                CollateralDataFactory.equipment(CurrencyType.TJS),
                CollateralDataFactory.futureHarvest(CurrencyType.TJS),
                CollateralDataFactory.cotton(CurrencyType.TJS),
                CollateralDataFactory.acquiredProperty(CurrencyType.TJS),
                CollateralDataFactory.movableProperty(CurrencyType.TJS),
                CollateralDataFactory.gold(CurrencyType.TJS),
                CollateralDataFactory.goods(CurrencyType.TJS)
        );

        // ══════════════════════════════════════
        // 2. ФЛОУ
        // ══════════════════════════════════════

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

        new ConsultationStartFlow().startConsultation(
                config.getConsultationTheme("retail"));

        new ProductSelectionFlow().selectProduct(scenario.getProduct());

        new ApplicationCreationFlow().createApplication(scenario.getProduct() );

        // 2. Открываем заявку напрямую — пропускаем всё до этого места
        new NavigationFlow().open(
                config.getBaseUrl() +
                        "/0/Nui/ViewModule.aspx#CardModuleV2/FinApplicationPage/edit/" +
                        "5fae52d3-a5fe-464a-87c0-aa12e9c389c7"  );

        new RegistrationStageFlow()
                .completeRegistrationStage(registration);

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

        // ── Андеррайтер ────────────────────────
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
                .withApplicationNumber(applicationNumber).issueLoan();

        auth.logout();

        // ── Кассир ─────────────────────────────
        auth.login(scenario.getCashier());

        workspace.select(Workspace.CASHIER);

        new SigningStageFlow()
                .withContractNumber(applicationNumber).completeSigningStage();

        auth.logout();

        // ── ИКОК-ГО (завершение) ───────────────
        auth.login(scenario.getIkokGo());

        workspace.select(Workspace.IKOK_GO);

        new ApplicationFinishFlow().completeApplicationFinish();

        auth.logout();
    }


}


