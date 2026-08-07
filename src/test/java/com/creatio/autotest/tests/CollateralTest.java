package com.creatio.autotest.tests;

import com.creatio.autotest.base.BaseTest;
import com.creatio.autotest.flows.common.*;
import com.creatio.autotest.flows.common.NavigationFlow;
import com.creatio.autotest.flows.retail.collateral.CollateralStageFlow;
import com.creatio.autotest.flows.retail.participants.ParticipantsStageFlow;
import com.creatio.autotest.flows.retail.PreliminaryCheckStageFlow;
import com.creatio.autotest.flows.retail.registration.RegistrationStageFlow;
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
@Feature(AllureTags.Features.COLLATERAL)
@Owner("Davlat")
public class CollateralTest extends BaseTest {

    // ══════════════════════════════════════
    // DEBUG — только участники
    // Заявка уже прошла регистрацию и предварительную проверку
    // ══════════════════════════════════════

    @Test(groups = "debug", enabled = false)
    @Story("Debug — только участники заявки")
    @Description("Открываем заявку по UUID, добавляем участников")
    @Severity(SeverityLevel.NORMAL)
    public void debugParticipantsOnly() {
        TestScenario scenario = ScenarioFactory.happyPath();

        AuthFlow auth = new AuthFlow();
        auth.login(scenario.getManager());
        new WorkspaceFlow().select(Workspace.RETAIL_MANAGER);

        // Замени UUID на актуальный
        new NavigationFlow().open(
                config.getBaseUrl() +
                        "/0/Nui/ViewModule.aspx#CardModuleV2/" +
                        "FinApplicationPage/edit/" +
                        "cb6594e3-a2c6-4d1e-b83d-19bf6fd53f53"
        );

        new ParticipantsStageFlow().completeParticipantsStage(
                scenario.getParticipants(),
                scenario.getRegistration().getIncomeExpenses()
        );

        auth.logout();
    }

    // ══════════════════════════════════════
    // DEBUG — только залоги
    // Участники уже добавлены
    // ══════════════════════════════════════

    @Test(groups = "debug", enabled = true)
    @Story("Debug — только залоговые обеспечения")
    @Description("Открываем заявку по UUID, добавляем залоги")
    @Severity(SeverityLevel.NORMAL)
    public void debugCollateralsOnly() {
        TestScenario scenario = ScenarioFactory.happyPath();

        AuthFlow auth = new AuthFlow();
        auth.login(scenario.getManager());
        new WorkspaceFlow().select(Workspace.RETAIL_MANAGER);

        // Замени UUID на актуальный
        new NavigationFlow().open(
                config.getBaseUrl() +
                        "/0/Nui/ViewModule.aspx#CardModuleV2/" +
                        "FinApplicationPage/edit/" +
                        "7de58faf-f96c-4d35-8acc-a3190bbe7ad1"
        );

        new CollateralStageFlow().completeCollateralStage(
                scenario.getCollaterals()
        );

        auth.logout();
    }

    // ══════════════════════════════════════
    // DEBUG — участники + залоги вместе
    // ══════════════════════════════════════

    @Test(groups = "debug", enabled = false)
    @Story("Debug — участники и залоги")
    @Description("Открываем заявку по UUID, добавляем участников и залоги")
    @Severity(SeverityLevel.NORMAL)
    public void debugParticipantsAndCollaterals() {
        TestScenario scenario = ScenarioFactory.happyPath();

        AuthFlow auth = new AuthFlow();
        auth.login(scenario.getManager());
        new WorkspaceFlow().select(Workspace.RETAIL_MANAGER);

        // Замени UUID на актуальный
        new NavigationFlow().open(
                config.getBaseUrl() +
                        "/0/Nui/ViewModule.aspx#CardModuleV2/" +
                        "FinApplicationPage/edit/" +
                        "fd01784c-748f-40eb-9351-fff86f0bc480"
        );

        new ParticipantsStageFlow().completeParticipantsStage(
                scenario.getParticipants(),
                scenario.getRegistration().getIncomeExpenses()
        );

        new CollateralStageFlow().completeCollateralStage(
                scenario.getCollaterals()
        );

        auth.logout();
    }

    // ══════════════════════════════════════
    // REGRESSION — полный путь
    // От логина до завершения стадии обеспечения
    // ══════════════════════════════════════

    @Test(groups = "regression", enabled = false)
    @Story("Стадия обеспечения — полный путь")
    @Description("Логин → регистрация → предварительная проверка → участники → залоги")
    @Severity(SeverityLevel.BLOCKER)
    public void collateralStageTest() {
        TestScenario scenario = ScenarioFactory.happyPath();

        AuthFlow auth = new AuthFlow();
        auth.login(scenario.getManager());
        new WorkspaceFlow().select(Workspace.RETAIL_MANAGER);

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

        new ParticipantsStageFlow().completeParticipantsStage(
                scenario.getParticipants(),
                scenario.getRegistration().getIncomeExpenses()
        );

        new CollateralStageFlow().completeCollateralStage(
                scenario.getCollaterals()
        );

        auth.logout();
    }
}