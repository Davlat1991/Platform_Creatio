package com.creatio.autotest.tests;

import com.creatio.autotest.base.BaseTest;
import com.creatio.autotest.flows.AuthFlow;
import com.creatio.autotest.flows.ConsultationStartFlow;
import com.creatio.autotest.flows.ClientSearchFlow;
import com.creatio.autotest.flows.ProductSelectionFlow;
import com.creatio.autotest.flows.WorkspaceFlow;
import com.creatio.autotest.testdata.ScenarioFactory;
import com.creatio.autotest.testdata.models.TestScenario;
import com.creatio.autotest.testdata.models.Workspace;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

@Feature("Product Selection")
public class ProductSelectionTest extends BaseTest {

    @Test(groups = "regression")
    @Story("Подбор продукта — полный путь")
    @Description("Логин → рабочее место → поиск клиента → консультация → подбор продукта")
    @Severity(SeverityLevel.CRITICAL)
    public void productSelectionTest() {
        TestScenario scenario = ScenarioFactory.happyPath();

        // ── 1. Логин и рабочее место ───────────
        AuthFlow auth = new AuthFlow();
        auth.login(scenario.getManager());
        new WorkspaceFlow().select(Workspace.RETAIL_MANAGER);

        // ── 2. Поиск клиента ───────────────────
        new ClientSearchFlow().searchClient(
                scenario.getContact().getLastName(),
                scenario.getContact().getFirstName(),
                scenario.getContact().getMiddleName()
        );

        // ── 3. Начало консультации ─────────────
        new ConsultationStartFlow().startConsultation(
                config.getConsultationTheme("retail")
        );

        // ── 4. Подбор продукта ─────────────────
        new ProductSelectionFlow().selectProduct(scenario.getProduct());

        auth.logout();
    }
}