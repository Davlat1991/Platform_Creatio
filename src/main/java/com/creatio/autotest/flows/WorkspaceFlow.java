package com.creatio.autotest.flows;

import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.pages.WorkspacePage;
import com.creatio.autotest.testdata.models.Workspace;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkspaceFlow {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceFlow.class);

    private final WorkspacePage workspacePage = new WorkspacePage();
    private final BasePage      basePage      = new BasePage();

    // ══════════════════════════════════════
    // SELECT
    // ══════════════════════════════════════

    @Step("Выбрать рабочее место: {workspace}")
    public void select(Workspace workspace) {
        log.info("Selecting workspace: {}", workspace.getUiName());

        workspacePage.selectWorkspace(workspace.getUiName());
        basePage.waitForPage();

        log.info("Workspace ready: {}", workspace.getUiName());
    }
}