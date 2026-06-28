package com.creatio.autotest.tests;

import com.creatio.autotest.base.BaseTest;
import com.creatio.autotest.flows.AuthFlow;
import com.creatio.autotest.flows.ClientSearchFlow;
import com.creatio.autotest.flows.ConsultationStartFlow;
import com.creatio.autotest.flows.ProductSelectionFlow;
import com.creatio.autotest.flows.ApplicationCreationFlow;
import com.creatio.autotest.flows.WorkspaceFlow;
import com.creatio.autotest.flows.registration.RegistrationStageFlow;
import com.creatio.autotest.flows.registration.client.ClientFlowFactory;
import com.creatio.autotest.testdata.ScenarioFactory;
import com.creatio.autotest.testdata.models.ProductData;
import com.creatio.autotest.testdata.models.TestScenario;
import com.creatio.autotest.testdata.models.Workspace;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

@Feature("Client Type")
public class ClientTypeTest extends BaseTest {

    // ══════════════════════════════════════
    // Общий прогон одного типа клиента
    // ══════════════════════════════════════
    private void runClientType(TestScenario scenario) {
        ProductData product = scenario.getProduct();

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

        new ProductSelectionFlow().selectProduct(product);
        new ApplicationCreationFlow().createApplication(product);

        new RegistrationStageFlow().completeRegistrationStage(
                scenario.getRegistration(),
                ClientFlowFactory.from(scenario.getRegistration().getClientProfile())
        );

        auth.logout();
    }

    // ══════════════════════════════════════
    // DEBUG — по одному типу за раз
    // (один менеджер + одно физ.лицо: гонять отдельно,
    //  между прогонами — новая заявка/контакт)
    // ══════════════════════════════════════

    @Test(groups = "debug", enabled = false)
    @Story("Тип клиента: Иной источник дохода")
    @Severity(SeverityLevel.NORMAL)
    public void otherIncomeFullPath() {
        runClientType(ScenarioFactory.otherIncomePath());
    }

    @Test(groups = "debug", enabled = false)
    @Story("Тип клиента: Работает в организации")
    @Severity(SeverityLevel.NORMAL)
    public void employeeFullPath() {
        runClientType(ScenarioFactory.employeePath());
    }

    @Test(groups = "debug", enabled = true)
    @Story("Тип клиента: Самозанятый")
    @Severity(SeverityLevel.NORMAL)
    public void selfEmployedFullPath() {
        runClientType(ScenarioFactory.selfEmployedPath());
    }

    // ══════════════════════════════════════
    // РЕГРЕССИЯ (Путь B) — закомментировано до этапа регрессии.
    // Требует 3 разных менеджера + 3 разных физ.лица, иначе
    // 2-й и 3-й прогон упадут на активной заявке первого.
    // Вернуть, когда будет реализован полный флоу + multi-contact.
    // ══════════════════════════════════════

    /*
    @DataProvider(name = "clientTypes")
    public Object[][] clientTypes() {
        return new Object[][] {
                { "OTHER_INCOME",  ScenarioFactory.otherIncomePath()  },
                { "EMPLOYEE",      ScenarioFactory.employeePath()     },
                { "SELF_EMPLOYED", ScenarioFactory.selfEmployedPath() },
        };
    }

    @Test(dataProvider = "clientTypes", groups = "regression")
    @Story("Регистрация заявки по типу клиента")
    @Description("Параметризовано по типу занятости")
    public void clientTypeRegistration(String typeName, TestScenario scenario) {
        runClientType(scenario);
    }
    */
}