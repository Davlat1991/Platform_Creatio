package com.creatio.autotest.flows;

import com.creatio.autotest.pages.ConsultationPanelPage;
import com.creatio.autotest.pages.WorkspacePage;
import com.creatio.autotest.testdata.models.Workspace;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkspaceFlow {

    private static final Logger log =
            LoggerFactory.getLogger(WorkspaceFlow.class);

    private final WorkspacePage         workspacePage = new WorkspacePage();
    private final ConsultationPanelPage consultation  = new ConsultationPanelPage();

    // ══════════════════════════════════════
    // PUBLIC API
    // ══════════════════════════════════════

    /**
     * Всегда кликает через UI и выбирает рабочее место.
     * Использовать при первом входе: LoginTest, начало CreditApplicationTest.
     */
    @Step("Выбрать рабочее место: {workspace}")
    public void select(Workspace workspace) {
        log.info("Select workspace: {}", workspace.getUiName());

        workspacePage.selectWorkspace(workspace.getUiName());
        openPanelIfNeeded(workspace);

        log.info("Workspace ready: {}", workspace.getUiName());
    }

    /**
     * Пропускает выбор если рабочее место уже активно.
     * Использовать при смене ролей внутри теста (relogin между этапами).
     */
    @Step("Переключить рабочее место (если нужно): {workspace}")
    public void switchIfNeeded(Workspace workspace) {
        log.info("Switch workspace if needed: {}", workspace.getUiName());

        workspacePage.switchWorkspaceIfNeeded(workspace.getUiName());
        openPanelIfNeeded(workspace);

        log.info("Workspace ready: {}", workspace.getUiName());
    }

    // ══════════════════════════════════════
    // PRIVATE
    // ══════════════════════════════════════

    private void openPanelIfNeeded(Workspace workspace) {
        if (workspace.hasConsultationPanel()) {
            log.info("Opening consultation panel for: {}", workspace.getUiName());
            consultation.ensureOpened();
        }
    }
}