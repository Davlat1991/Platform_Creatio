package com.creatio.autotest.pages;

import com.codeborne.selenide.Condition;
import com.creatio.autotest.core.base.BasePage;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class WorkspacePage extends BasePage {

    // ══════════════════════════════════════
    // LOCATORS — правильные из DOM
    // ══════════════════════════════════════

    // Кнопка открытия меню рабочих мест
    private static final String WORKPLACE_BUTTON =
            "//span[@id='menu-workplace-button-wrapperEl']";

    // Текущее выбранное рабочее место
    private static final String WORKPLACE_TEXT =
            "//span[@id='menu-workplace-button-textEl']";

    // Пункт меню рабочего места
    private static final String WORKPLACE_ITEM =
            "//ul[@data-item-marker='TopWorkplaceMenu']//li[.='%s']";

    // ══════════════════════════════════════
    // ACTIONS
    // ══════════════════════════════════════

    @Step("Выбрать рабочее место: {workspaceName}")
    public WorkspacePage selectWorkspace(String workspaceName) {
        log.info("Selecting workspace: {}", workspaceName);

        // Ждём загрузки страницы
        waitForPage();

        // Проверяем текущее рабочее место
        if (isAlreadySelected(workspaceName)) {
            log.info("Workspace '{}' already active — skip", workspaceName);
            return this;
        }

        // Кликаем по кнопке выбора
        $x(WORKPLACE_BUTTON)
                .shouldBe(Condition.visible, Duration.ofSeconds(15));

        executeJavaScript(
                "arguments[0].click();",
                $x(WORKPLACE_BUTTON)
        );

        // Выбираем нужное рабочее место
        $x(String.format(WORKPLACE_ITEM, workspaceName))
                .shouldBe(Condition.visible, Duration.ofSeconds(10))
                .click();

        waitForPage();

        log.info("Workspace selected: {}", workspaceName);
        return this;
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

    private boolean isAlreadySelected(String workspaceName) {
        try {
            String current = $x(WORKPLACE_TEXT)
                    .shouldBe(Condition.visible, Duration.ofSeconds(10))
                    .getText()
                    .trim();

            log.info("Current workspace: '{}'", current);
            return current.contains(workspaceName) ||
                    workspaceName.contains(current);
        } catch (Exception e) {
            log.debug("Cannot read current workspace: {}", e.getMessage());
            return false;
        }
    }
}