package com.creatio.autotest.tests;

import com.creatio.autotest.base.BaseTest;
import com.creatio.autotest.flows.common.*;
import com.creatio.autotest.testdata.ScenarioFactory;
import com.creatio.autotest.testdata.models.TestScenario;
import com.creatio.autotest.testdata.models.Workspace;
import com.creatio.autotest.utils.AllureTags;
import io.qameta.allure.*;
import org.testng.annotations.Test;

@Epic(AllureTags.Epics.RETAIL)
@Feature(AllureTags.Features.APP_CREATION)
@Owner("Davlat")
public class ApplicationCreationTest extends BaseTest {

    @Test(groups = "regression")
    @Story("Создание кредитной заявки — полный путь")
    @Description("Логин → рабочее место → поиск клиента → консультация → подбор продукта → создание заявки")
    @Severity(SeverityLevel.BLOCKER)
    public void applicationCreationTest() {
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

        // ── 5. Создание заявки ─────────────────
        new ApplicationCreationFlow().createApplication(scenario.getProduct());

        auth.logout();
    }
}