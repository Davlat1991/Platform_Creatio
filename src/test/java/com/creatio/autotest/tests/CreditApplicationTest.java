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

        AuthFlow      auth      = new AuthFlow();
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
                "consultation-theme-7a0f11cc-756d-474a-885f-1dd64eeca5b3"
        );

        new ProductSelectionFlow().selectProduct(scenario.getProduct());

        new ApplicationCreationFlow().createApplication(
                "3", "2", "Аннуитетный", "36"
        );

        new RegistrationStageFlow()
                .completeRegistrationStage(registration);

        new PreliminaryCheckStageFlow()
                .completePreliminaryCheckStage();

        new ParticipantsStageFlow()
                .completeParticipantsStage(
                        participants,
                        registration.getIncomeExpenses()
                );

        new CollateralStageFlow()
                .completeCollateralStage(collaterals);

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
                .completeClientNotification("Назарова Азиза Акбаровна");

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