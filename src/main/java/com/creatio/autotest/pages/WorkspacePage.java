package com.creatio.autotest.pages;

import com.codeborne.selenide.Condition;
import com.creatio.autotest.core.base.BasePage;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$x;

public class WorkspacePage extends BasePage {

    // ══════════════════════════════════════
    // LOCATORS
    // ══════════════════════════════════════

    private static final String WORKPLACE_LIST =
            "//span[@id='menu-workplace-button-menuWrapEl']";

    private static final String WORKPLACE_ITEM =
            "//ul[@data-item-marker='TopWorkplaceMenu']//li[.='%s']";

    // ══════════════════════════════════════
    // ACTIONS
    // ══════════════════════════════════════

    @Step("Выбрать рабочее место: {workspaceName}")
    public WorkspacePage selectWorkspace(String workspaceName) {
        log.info("Selecting workspace: {}", workspaceName);

        $x(WORKPLACE_LIST)
                .shouldBe(Condition.visible)
                .click();

        $x(String.format(WORKPLACE_ITEM, workspaceName))
                .shouldBe(Condition.visible)
                .click();

        log.info("Workspace selected: {}", workspaceName);
        return this;
    }
}