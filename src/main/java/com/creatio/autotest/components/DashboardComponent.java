package com.creatio.autotest.components;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BaseComponent;
import com.creatio.autotest.core.services.WaitService;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class DashboardComponent extends BaseComponent {

    // ══════════════════════════════════════
    // GET DASHBOARD
    // ══════════════════════════════════════

    private SelenideElement getDashboard() {
        // Дашборд появляется после догрузки страницы (серый оверлей "Загрузка").
        // Сначала ждём исчезновения маски Creatio, затем сам дашборд.
        new WaitService().waitForLoader();
        return $x("//*[@data-item-marker='Dashboard']")
                .shouldBe(visible, Duration.ofSeconds(60));
    }

    // ══════════════════════════════════════
    // WAIT
    // ══════════════════════════════════════

    @Step("Wait process block: {blockName}")
    public DashboardComponent waitProcessBlock(String blockName) {
        log.info("Wait dashboard block: '{}'", blockName);
        getDashboard()
                .$x(".//*[contains(text(),'" + blockName + "')]")
                .shouldBe(visible, Duration.ofSeconds(60));
        return this;
    }

    // ══════════════════════════════════════
    // CLICK ACTION
    // ══════════════════════════════════════

    @Step("Click action '{action}' in block '{blockName}'")
    public DashboardComponent clickAction(String blockName, String action) {
        log.info("Dashboard click: block='{}' action='{}'", blockName, action);

        SelenideElement block = getDashboard().$x(
                ".//*[contains(text(),'" + blockName + "')]" +
                        "/ancestor::*[contains(@class,'controlgroup')]"
        ).shouldBe(visible);

        SelenideElement button = block.$x(
                ".//span[contains(text(),'" + action + "')]"
        ).shouldBe(visible, enabled);

        executeJavaScript("arguments[0].click();", button);
        return this;
    }

    @Step("Click dashboard '{name}' marker '{marker}' wait minipage")
    public DashboardComponent clickAndWaitMiniPage(String name, String marker) {
        log.info("Dashboard click: name='{}' marker='{}'", name, marker);

        for (int attempt = 1; attempt <= 4; attempt++) {
            try {
                $x("//div[.='" + name + "']" +
                        "/..//span[@data-item-marker='" + marker + "']")
                        .scrollIntoView(false)
                        .hover()
                        .click();

                $x("//*[@data-item-marker='MiniPage']")
                        .shouldBe(visible, Duration.ofSeconds(10));

                log.info("MiniPage opened on attempt {}", attempt);
                return this;

            } catch (AssertionError e) {
                log.warn("Attempt {}/4 failed", attempt);
                if (attempt == 4) throw e;
            }
        }
        return this;
    }

    @Step("Wait and click dashboard '{name}' marker '{marker}'")
    public DashboardComponent waitAndClick(String name, String marker) {
        log.info("Wait dashboard: name='{}' marker='{}'", name, marker);

        $x("//div[.='" + name + "']")
                .shouldBe(visible, Duration.ofSeconds(60));

        $x("//div[.='" + name + "']" +
                "/..//span[@data-item-marker='" + marker + "']")
                .scrollIntoView(false)
                .hover()
                .click();

        return this;
    }

    @Step("Click dashboard element: {name}")
    public DashboardComponent clickByName(String name) {
        log.info("Dashboard click by name: '{}'", name);
        $x("//div[.='" + name + "']")
                .shouldBe(visible, enabled)
                .click();
        return this;
    }
}