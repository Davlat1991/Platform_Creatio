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
    // Полный путь: логин → создание заявки → регистрация
    // с типом клиента "Имеет другой источник дохода" (OTHER_INCOME)
    // ══════════════════════════════════════

    @Test(groups = "debug", enabled = false)
    @Story("Тип клиента: Иной источник дохода — полный путь")
    @Description("Логин → поиск клиента → консультация → продукт → заявка → регистрация (OTHER_INCOME)")
    @Severity(SeverityLevel.NORMAL)
    public void otherIncomeFullPath() {

        TestScenario scenario = ScenarioFactory.happyPath();
        ProductData product = scenario.getProduct();

        // 1. Авторизация + рабочее место
        AuthFlow auth = new AuthFlow();
        auth.login(scenario.getManager());
        new WorkspaceFlow().select(Workspace.RETAIL_MANAGER);

        // 2. Поиск клиента
        new ClientSearchFlow().searchClient(
                scenario.getContact().getLastName(),
                scenario.getContact().getFirstName(),
                scenario.getContact().getMiddleName()
        );

        // 3. Консультация
        new ConsultationStartFlow()
                .startConsultation(config.getConsultationTheme("retail"));

        // 4. Подбор продукта
        new ProductSelectionFlow().selectProduct(product);

        // 5. Создание заявки
        new ApplicationCreationFlow().createApplication(product);

        // 6. Регистрация с типом клиента OTHER_INCOME
        new RegistrationStageFlow().completeRegistrationStage(
                scenario.getRegistration(),
                ClientFlowFactory.from(scenario.getRegistration().getClientProfile())
        );

        auth.logout();
    }


    @Test(groups = "debug", enabled = false)
    @Story("Тип клиента: Работает в организации — полный путь")
    @Description("Логин → клиент → консультация → продукт → заявка → регистрация (EMPLOYEE)")
    @Severity(SeverityLevel.NORMAL)
    public void employeeFullPath() {

        TestScenario scenario = ScenarioFactory.employeePath();
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

    @Test(groups = "debug", enabled = true)
    @Story("Тип клиента: Самозанятый — полный путь")
    @Description("Логин → клиент → консультация → продукт → заявка → регистрация (SELF_EMPLOYED)")
    @Severity(SeverityLevel.NORMAL)
    public void selfEmployedFullPath() {

        TestScenario scenario = ScenarioFactory.selfEmployedPath();
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
}