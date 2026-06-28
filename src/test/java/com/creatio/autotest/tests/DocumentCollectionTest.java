package com.creatio.autotest.tests;

import com.codeborne.selenide.Selenide;
import com.creatio.autotest.base.BaseTest;
import com.creatio.autotest.flows.AuthFlow;
import com.creatio.autotest.flows.DocumentCollectionStageFlow;
import com.creatio.autotest.flows.NavigationFlow;
import com.creatio.autotest.flows.WorkspaceFlow;
import com.creatio.autotest.testdata.ScenarioFactory;
import com.creatio.autotest.testdata.models.TestScenario;
import com.creatio.autotest.testdata.models.Workspace;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.Test;

@Feature("Document Collection Stage")
public class DocumentCollectionTest extends BaseTest {

    // ══════════════════════════════════════
    // DEBUG — навигация по документам (без загрузки)
    // Заявка уже на стадии "Сбор документов"
    // ══════════════════════════════════════

    @Test(groups = "debug", enabled = false)
    @Story("Debug D4.4+D4.5 — загрузка документов поручителя и залогодателя")
    @Description("Открыть заявку на стадии документов → загрузить заёмщика, поручителя, залогодателя → закрыть активность")
    public void debugAllParticipantsDocuments() {
        TestScenario scenario = ScenarioFactory.happyPath();

        AuthFlow auth = new AuthFlow();
        auth.login(scenario.getManager());
        new WorkspaceFlow().select(Workspace.RETAIL_MANAGER);

        new NavigationFlow().open(
                config.getBaseUrl() +
                        "/0/Nui/ViewModule.aspx#CardModuleV2/" +
                        "FinApplicationPage/edit/" +
                        "90c5e1b2-71b8-436c-bd47-64b188b20c86"
        );

        DocumentCollectionStageFlow docs = new DocumentCollectionStageFlow();

        // Заёмщик (уже работает)
        docs.openDocumentsTab();
        docs.prepareBorrowerDetails();
        docs.uploadBorrowerDocuments();

        // Поручитель + залогодатель (D4.4)
        docs.uploadAllParticipantDocuments(ScenarioFactory.documentOwners());

        // Закрыть активность (D4.5)
        docs.completeDocumentsActivity();

        auth.logout();
    }


    @Test(groups = "debug", enabled = true)
    @Description("Полный путь до стадии Сбор документов (с поручителем и залогодателем)")
    @Story("Debug — довести заявку с участниками до стадии документов")
    public void debugReachDocumentsStageWithParticipants() {
        TestScenario scenario = ScenarioFactory.happyPath();

        AuthFlow auth = new AuthFlow();
        auth.login(scenario.getManager());
        new com.creatio.autotest.flows.WorkspaceFlow().select(Workspace.RETAIL_MANAGER);

        new com.creatio.autotest.flows.ClientSearchFlow().searchClient(
                scenario.getContact().getLastName(),
                scenario.getContact().getFirstName(),
                scenario.getContact().getMiddleName()
        );
        new com.creatio.autotest.flows.ConsultationStartFlow()
                .startConsultation(config.getConsultationTheme("retail"));
        new com.creatio.autotest.flows.ProductSelectionFlow()
                .selectProduct(scenario.getProduct());
        new com.creatio.autotest.flows.ApplicationCreationFlow()
                .createApplication(scenario.getProduct());

        new com.creatio.autotest.flows.registration.RegistrationStageFlow()
                .completeRegistrationStage(scenario.getRegistration());

        new com.creatio.autotest.flows.PreliminaryCheckStageFlow()
                .completePreliminaryCheckStage();

        new com.creatio.autotest.flows.participants.ParticipantsStageFlow()
                .completeParticipantsStage(
                        scenario.getParticipants(),
                        scenario.getRegistration().getIncomeExpenses()
                );

        new com.creatio.autotest.flows.collateral.CollateralStageFlow()
                .completeCollateralStage(scenario.getCollaterals());

        DocumentCollectionStageFlow docs = new DocumentCollectionStageFlow();

        // Заёмщик (уже работает)
        docs.openDocumentsTab();
        docs.prepareBorrowerDetails();
        docs.uploadBorrowerDocuments();

        // Поручитель + залогодатель (D4.4)
        docs.uploadAllParticipantDocuments(ScenarioFactory.documentOwners());

        // Закрыть активность (D4.5)
        docs.completeDocumentsActivity();

        Selenide.sleep(15000);

        auth.logout();
    }

}