package com.creatio.autotest.components;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BaseComponent;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.*;

public class GridComponent extends BaseComponent {

    // ══════════════════════════════════════
    // GET GRID
    // ══════════════════════════════════════


    @Step("Click row containing '{expectedText}' in grid: {gridId}")
    public void clickFirstRowContaining(String gridId, String expectedText) {
        log.info("Click row containing '{}' in grid '{}'", expectedText, gridId);

        String rowXpath =
                "//div[@id='" + gridId + "']" +
                        "//div[contains(@class,'grid-row')" +
                        " and not(contains(@class,'grid-header'))" +
                        " and contains(normalize-space(.), '" + expectedText + "')]";

        // 1. Дождаться результата фильтрации: искомая строка видима.
        $x(rowXpath).shouldBe(Condition.visible, Duration.ofSeconds(30));

        // 2. Дождаться, пока грид перестанет перерисовываться (лоадеры ушли).
        //    Именно перерисовка грида после фильтра рвала ссылку → StaleElement.
        waitGridSettled();

        // 3. Клик с ретраями: строку перечитываем на КАЖДОЙ попытке.
        //    Никакого кэша WebElement между resolve и кликом — это убивает stale.
        for (int attempt = 1; attempt <= 4; attempt++) {
            try {
                SelenideElement row = $x(rowXpath)
                        .shouldBe(Condition.visible, Duration.ofSeconds(20))
                        .scrollIntoView(true);
                executeJavaScript("arguments[0].click();", row);
                log.info("Row clicked on attempt {}/4", attempt);
                return;
            } catch (Throwable e) {
                log.warn("Attempt {}/4 failed ({}) — грид перерисовывается, повтор",
                        attempt, e.getClass().getSimpleName());
                if (attempt == 4) {
                    throw new AssertionError(
                            "Не удалось кликнуть строку '" + expectedText +
                                    "' в гриде '" + gridId + "' после 4 попыток", e);
                }
                waitGridSettled();
            }
        }
    }

    /** Ждём исчезновения лоадеров/масок Creatio — тот же приём, что в clickTabByName. */
    private void waitGridSettled() {
        $$(".ts-loader,.mask,.loading-indicator")
                .filter(Condition.visible)
                .shouldHave(CollectionCondition.size(0), Duration.ofSeconds(15));
    }
}