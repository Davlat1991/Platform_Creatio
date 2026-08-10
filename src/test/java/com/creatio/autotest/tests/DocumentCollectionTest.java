package com.creatio.autotest.tests;

import com.creatio.autotest.base.BaseTest;
import com.creatio.autotest.flows.common.*;
import com.creatio.autotest.flows.retail.DocumentCollectionStageFlow;
import com.creatio.autotest.flows.common.NavigationFlow;
import com.creatio.autotest.flows.retail.PreliminaryCheckStageFlow;
import com.creatio.autotest.flows.retail.collateral.CollateralStageFlow;
import com.creatio.autotest.flows.retail.participants.ParticipantsStageFlow;
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
@Feature(AllureTags.Features.DOCUMENTS)
@Owner("Davlat")
public class DocumentCollectionTest extends BaseTest {

    // ══════════════════════════════════════
    // DEBUG — навигация по документам (без загрузки)
    // Заявка уже на стадии "Сбор документов"
    // ══════════════════════════════════════

    @Test(groups = "debug", enabled = false)
    @Story("Debug D4.4+D4.5 — загрузка документов поручителя и залогодателя")
    @Description("Открыть заявку на стадии документов → загрузить заёмщика, поручителя, залогодателя → закрыть активность")
    @Severity(SeverityLevel.NORMAL)
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


    @Test(groups = "debug", enabled = false)
    @Description("Полный путь до стадии Сбор документов (с поручителем и залогодателем)")
    @Story("Debug — довести заявку с участниками до стадии документов")
    @Severity(SeverityLevel.NORMAL)
    public void debugReachDocumentsStageWithParticipants() {
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

        new ParticipantsStageFlow()
                .completeParticipantsStage(
                        scenario.getParticipants(),
                        scenario.getRegistration().getIncomeExpenses()
                );

        new CollateralStageFlow()
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

        auth.logout();
    }

}