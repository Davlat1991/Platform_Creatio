package com.creatio.autotest.components;

import com.creatio.autotest.core.base.BaseComponent;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$x;

public class DashboardComponent extends BaseComponent {


    // ══════════════════════════════════════
    // CLICK ACTION
    // ══════════════════════════════════════

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