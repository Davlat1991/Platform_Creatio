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


    // ── ЛОКАТОРЫ (строки-константы, не SelenideElement-поля) ──
    private static final String DASHBOARD_CONTAINER =
            "//div[@data-item-marker='SectionActionsDashboardContainer']";
    private static final String TASK_TAB =
            "//li[@data-item-marker='task-message-tab']";

    /** Дефолтный таймаут ожидания панели. Дашборд Creatio грузится долго. */
    private static final Duration ACTIONS_DASHBOARD_DEFAULT = Duration.ofSeconds(60);

// ══════════════════════════════════════
// WAIT FOR ACTIONS DASHBOARD ("Следующие шаги")
// ══════════════════════════════════════

    /** Ожидание с дефолтным таймаутом (60 сек). */
    @Step("Ожидание загрузки панели 'Следующие шаги' (Actions Dashboard)")
    public DashboardComponent waitForActionsDashboardLoaded() {
        return waitForActionsDashboardLoaded(ACTIONS_DASHBOARD_DEFAULT);
    }

    /** Ожидание с управляемым таймаутом — для медленных стендов/CI. */
    @Step("Ожидание загрузки панели 'Следующие шаги' (таймаут {timeout})")
    public DashboardComponent waitForActionsDashboardLoaded(Duration timeout) {
        log.info("Wait for ActionsDashboard loaded, timeout={}", timeout);

        // 1) Корневой контейнер виджета отрисован и видим
        $x(DASHBOARD_CONTAINER)
                .shouldBe(visible, timeout);

        // 2) Табы канала достроены — самый надёжный признак готовности.
        //    Вкладка "Задача" присутствует всегда, вне зависимости от наличия задач.
        $x(TASK_TAB)
                .shouldBe(visible, timeout);

        // 3) Оверлеи/лоадеры ушли — иначе следующий клик перехватит маска
        wait.waitForLoader();

        log.info("ActionsDashboard is ready");
        return this;
    }
}