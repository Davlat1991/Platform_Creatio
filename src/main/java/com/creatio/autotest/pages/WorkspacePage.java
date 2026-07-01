package com.creatio.autotest.pages;

import com.codeborne.selenide.Condition;
import com.creatio.autotest.core.base.BasePage;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class WorkspacePage extends BasePage {

    // ══════════════════════════════════════
    // LOCATORS
    // ══════════════════════════════════════

    // Кнопка открытия дропдауна
    private static final String WORKPLACE_BUTTON =
            "//span[@id='menu-workplace-button-wrapperEl']";

    // Текст текущего рабочего места
    private static final String WORKPLACE_TEXT =
            "//span[@id='menu-workplace-button-textEl']";

    // Контейнер дропдауна
    private static final String WORKPLACE_DROPDOWN =
            "//ul[@data-item-marker='TopWorkplaceMenu']";

    // Пункт меню по data-item-marker (совпадает с текстом — подтверждено DOM)
    private static final String WORKPLACE_ITEM =
            "//ul[@data-item-marker='TopWorkplaceMenu']//li[@data-item-marker='%s']";

    // ══════════════════════════════════════
    // PUBLIC API
    // ══════════════════════════════════════

    /**
     * Всегда кликает через UI и выбирает рабочее место.
     * Использовать при первом входе (LoginTest).
     */
    @Step("Выбрать рабочее место: {workspaceName}")
    public WorkspacePage selectWorkspace(String workspaceName) {
        log.info("Selecting workspace: {}", workspaceName);

        waitForPage();
        openDropdown();
        clickItem(workspaceName);
        waitForPage();
        verifySelected(workspaceName);

        log.info("Workspace selected: {}", workspaceName);
        return this;
    }

    /**
     * Пропускает выбор если рабочее место уже активно.
     * Использовать при смене ролей внутри теста (CreditApplicationTest).
     */
    @Step("Переключить рабочее место (если нужно): {workspaceName}")
    public WorkspacePage switchWorkspaceIfNeeded(String workspaceName) {
        log.info("Check workspace: {}", workspaceName);

        waitForPage();

        if (isAlreadySelected(workspaceName)) {
            log.info("Workspace '{}' already active — skip", workspaceName);
            return this;
        }

        openDropdown();
        clickItem(workspaceName);
        waitForPage();
        verifySelected(workspaceName);

        log.info("Workspace switched to: {}", workspaceName);
        return this;
    }

    // ══════════════════════════════════════
    // PRIVATE
    // ══════════════════════════════════════

    private void openDropdown() {
        $x(WORKPLACE_BUTTON)
                .shouldBe(Condition.visible, Duration.ofSeconds(30));

        executeJavaScript("arguments[0].click();", $x(WORKPLACE_BUTTON));

        // Ждём появления дропдауна после клика
        $x(WORKPLACE_DROPDOWN)
                .shouldBe(Condition.visible, Duration.ofSeconds(30));

        log.info("Workspace dropdown opened");
    }

    private void clickItem(String workspaceName) {
        $x(String.format(WORKPLACE_ITEM, workspaceName))
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .click();

        log.info("Clicked workspace item: {}", workspaceName);
    }

    private void verifySelected(String workspaceName) {
        String current = $x(WORKPLACE_TEXT)
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .getText()
                .trim();

        if (!current.contains(workspaceName)) {
            throw new AssertionError(
                    "Workspace selection failed. Expected: '" + workspaceName +
                            "', Actual: '" + current + "'"
            );
        }

        log.info("Workspace verified: '{}'", current);
    }

    private boolean isAlreadySelected(String workspaceName) {
        try {
            String current = $x(WORKPLACE_TEXT)
                    .shouldBe(Condition.visible, Duration.ofSeconds(30))
                    .getText()
                    .trim();

            log.info("Current workspace: '{}'", current);
            return current.contains(workspaceName);
        } catch (Exception e) {
            log.debug("Cannot read current workspace: {}", e.getMessage());
            return false;
        }
    }
}