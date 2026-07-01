package com.creatio.autotest.tests;

import com.creatio.autotest.base.BaseTest;
import com.creatio.autotest.flows.*;
import com.creatio.autotest.flows.collateral.CollateralStageFlow;
import com.creatio.autotest.flows.registration.RegistrationStageFlow;
import com.creatio.autotest.flows.registration.client.ClientFlowFactory;
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

/**
 * Упрощённый маршрут (500–30000 сомони).
 * Тип клиента: "Имеет другой источник дохода" (OTHER_INCOME, дефолт фабрики).
 * БЕЗ участников (поручитель/залогодатель) и БЕЗ залогов.
 * На стадии "Сбор документов" — только документы заёмщика.
 * Скоринг обязан дать "Одобрить"; заявка авто-уходит на "Информирование клиента".
 * Верификатор и кредитный комитет НЕ участвуют.
 */
@Epic(AllureTags.Epics.RETAIL)
@Feature(AllureTags.Features.E2E)
@Owner("Davlat")
public class SimplifiedRouteTest extends BaseTest {

    @Test(groups = "regression")
    @Story("Упрощённый маршрут — без участников, залогов и верификатора")
    @Description("Упрощённый маршрут — без участников, без залогов, без верификатора")
    @Severity(SeverityLevel.BLOCKER)
    public void simplifiedRouteHappyPath() {

        TestScenario scenario = ScenarioFactory.simplePath();

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

        new ConsultationStartFlow()
                .startConsultation(config.getConsultationTheme("retail"));

        new ProductSelectionFlow()
                .selectProduct(scenario.getProduct());

        new ApplicationCreationFlow()
                .createApplication(scenario.getProduct());

        new RegistrationStageFlow()
                .completeRegistrationStage(
                        scenario.getRegistration(),
                        ClientFlowFactory.from(
                                scenario.getRegistration().getClientProfile())
                );

        new PreliminaryCheckStageFlow()
                .completePreliminaryCheckStage();

        // Без участников: стадия "Участники заявки" не добавляет никого.
        // Но стадию "Обеспечение" ОБЯЗАТЕЛЬНО завершить (иначе процесс не идёт
        // к "Сбор документов"), хотя залогов нет.
        new CollateralStageFlow()
                .completeCollateralStage();   // ← перегрузка без аргументов: только закрыть стадию

        // ── Сбор документов (только заёмщик) ───
        DocumentCollectionStageFlow documents = new DocumentCollectionStageFlow();
        documents.openDocumentsTab();
        documents.prepareBorrowerDetails();
        documents.uploadBorrowerDocuments();
        documents.completeDocumentsActivity();

        // ── Рассмотрение (SIMPLIFIED: авто-переход на "Информирование клиента") ──
        new ReviewStageRetailFlow().completeReview(
                scenario.getRoute(),
                scenario.getExpectedScoringDecision()
        );

        // Верификатор и комитет НЕ участвуют — блок отсутствует намеренно.

        // ── Розничный менеджер (уведомление) ───
        new ClientNotificationStageFlow()
                .completeClientNotification(scenario.getIkok().getDisplayName());

        auth.logout();

        // ── ИКОК ────────────────────────────
        auth.login(scenario.getIkok());
        workspace.select(Workspace.IKOK);

        new LoanIssuanceFlow()
                .issueLoan();

        auth.logout();

        // ── Кассир ─────────────────────────────
        // TODO: активировать после миграции creditContractNumber на ScenarioContext.
        // auth.login(scenario.getCashier());
        // workspace.select(Workspace.CASHIER);
        // new SigningStageFlow().completeSigningStage();
        // auth.logout();

        // ── ИКОК (завершение) ───────────────
        auth.login(scenario.getIkok());
        workspace.select(Workspace.IKOK);

        new ApplicationFinishFlow()
                .completeApplicationFinish();

        auth.logout();
    }
}