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

    private final WorkspacePage       workspacePage = new WorkspacePage();
    private final ConsultationPanelPage consultation = new ConsultationPanelPage();

    @Step("Выбрать рабочее место: {workspace}")
    public void select(Workspace workspace) {
        log.info("Select workspace: {}", workspace.getUiName());

        // 1. Выбрать рабочее место
        workspacePage.selectWorkspace(workspace.getUiName());

        // 2. Открыть консультационную панель
        consultation.ensureOpened();

        log.info("Workspace ready: {}", workspace.getUiName());
    }
}