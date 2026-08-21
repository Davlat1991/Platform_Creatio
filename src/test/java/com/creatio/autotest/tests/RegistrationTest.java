package com.creatio.autotest.tests;

import com.creatio.autotest.base.BaseTest;
import com.creatio.autotest.flows.common.*;
import com.creatio.autotest.flows.retail.registration.RegistrationStageFlow;
import com.creatio.autotest.testdata.ScenarioFactory;
import com.creatio.autotest.testdata.models.TestScenario;
import com.creatio.autotest.testdata.models.Workspace;
import com.creatio.autotest.utils.AllureTags;
import io.qameta.allure.*;
import org.testng.annotations.Test;

@Epic(AllureTags.Epics.RETAIL)
@Feature(AllureTags.Features.REGISTRATION)
@Owner("Davlat")
public class RegistrationTest extends BaseTest {

    // ══════════════════════════════════════
    // DEBUG — прямой переход по UUID заявки
    // Заменить UUID на актуальный из адресной строки
    // ══════════════════════════════════════

    @Test(groups = "debug", enabled = false)
    @Story("Debug — этап регистрации напрямую")
    @Description("Открываем заявку по UUID и выполняем этап регистрации")
    @Severity(SeverityLevel.NORMAL)
    public void debugRegistrationStage() {
        AuthFlow auth = new AuthFlow();
        auth.login(ScenarioFactory.happyPath().getManager());
        new WorkspaceFlow().select(Workspace.RETAIL_MANAGER);

        // Замени UUID на актуальный из адресной строки браузера
        new NavigationFlow().open(
                config.getBaseUrl() +
                        "/0/Nui/ViewModule.aspx#CardModuleV2/" +
                        "FinApplicationPage/edit/" +
                        "29b666c2-2ee7-4ea6-957f-60f9046f5f90"
        );

        TestScenario scenario = ScenarioFactory.happyPath();
        new RegistrationStageFlow().completeRegistrationStage(scenario.getRegistration());

        auth.logout();
    }

    // ══════════════════════════════════════
    // REGRESSION — полный путь
    // ══════════════════════════════════════

    @Test(groups = "regression")
    @Story("Этап регистрации — полный путь")
    @Description("Полный путь от логина до завершения этапа регистрации")
    @Severity(SeverityLevel.BLOCKER)
    public void registrationStageTest() {
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

        auth.logout();
    }
}