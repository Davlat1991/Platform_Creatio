package com.creatio.autotest.components;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BaseComponent;
import io.qameta.allure.Step;
import org.openqa.selenium.interactions.Actions;

import java.time.Duration;
import java.util.List;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class GridComponent extends BaseComponent {

    // ══════════════════════════════════════
    // GET GRID
    // ══════════════════════════════════════

    public SelenideElement getGrid(String marker) {
        return $x("//*[@data-item-marker='" + marker + "']")
                .shouldBe(visible);
    }

    // ══════════════════════════════════════
    // SELECT ROW
    // ══════════════════════════════════════

    @Step("Grid: выбрать строку '{text}' в '{marker}'")
    public GridComponent selectRow(String marker, String text) {

        log.info("Grid selectRow: marker='{}' text='{}'", marker, text);

        SelenideElement row = getGrid(marker)
                .$$x(".//div[contains(@class,'grid-row')]")
                .findBy(text(text))
                .shouldBe(visible)
                .scrollIntoView(true);

        executeJavaScript("arguments[0].click();", row);
        row.shouldHave(cssClass("grid-row-selected"));

        return this;
    }

    @Step("Grid: выбрать первую строку в '{marker}'")
    public GridComponent selectFirstRow(String marker) {

        log.info("Grid selectFirstRow: marker='{}'", marker);

        SelenideElement row = getGrid(marker)
                .$$x(".//div[contains(@class,'grid-row')" +
                        " and not(contains(@class,'grid-header'))]")
                .filter(visible)
                .first()
                .shouldBe(visible);

        executeJavaScript("arguments[0].click();", row);
        row.shouldHave(cssClass("grid-row-selected"));

        return this;
    }

    @Step("Grid: выбрать строку с индексом {index} в '{marker}'")
    public GridComponent selectRowByIndex(String marker, int index) {

        log.info("Grid selectRowByIndex: marker='{}' index={}", marker, index);

        SelenideElement row = getGrid(marker)
                .$$x(".//div[contains(@class,'grid-row')]")
                .get(index)
                .shouldBe(visible, enabled);

        executeJavaScript("arguments[0].click();", row);

        return this;
    }

    // ══════════════════════════════════════
    // DOUBLE CLICK
    // ══════════════════════════════════════

    @Step("Grid: двойной клик по строке '{text}'")
    public GridComponent doubleClickRow(String text) {

        log.info("Grid doubleClickRow: text='{}'", text);

        SelenideElement row = $x(
                "//div[contains(@class,'grid-row')]" +
                        "[.//*[contains(normalize-space(.),'" + text + "')]]"
        ).shouldBe(visible).scrollIntoView(true);

        try {
            new Actions(com.codeborne.selenide.WebDriverRunner.getWebDriver())
                    .doubleClick(row).perform();
        } catch (Throwable e) {
            log.warn("doubleClick failed, using JS fallback");
            executeJavaScript(
                    "arguments[0].dispatchEvent(" +
                            "new MouseEvent('dblclick',{bubbles:true,cancelable:true}))",
                    row
            );
        }

        return this;
    }

    @Step("Grid: двойной клик по первой строке в '{marker}'")
    public GridComponent doubleClickFirstRow(String marker) {

        log.info("Grid doubleClickFirstRow: marker='{}'", marker);

        SelenideElement row = getGrid(marker)
                .$$x(".//div[contains(@class,'grid-row')" +
                        " and not(contains(@class,'grid-header'))]")
                .filter(visible)
                .first()
                .shouldBe(visible)
                .scrollIntoView(true);

        try {
            new Actions(com.codeborne.selenide.WebDriverRunner.getWebDriver())
                    .doubleClick(row).perform();
        } catch (Throwable e) {
            executeJavaScript(
                    "arguments[0].dispatchEvent(" +
                            "new MouseEvent('dblclick',{bubbles:true,cancelable:true}))",
                    row
            );
        }

        return this;
    }

    // ══════════════════════════════════════
    // WAIT / VALIDATE
    // ══════════════════════════════════════

    @Step("Grid: ожидать минимум {count} строк в '{marker}'")
    public GridComponent waitForRows(String marker, int count) {

        log.info("Grid waitForRows: marker='{}' count={}", marker, count);

        getGrid(marker)
                .$$x(".//div[contains(@class,'grid-row')]")
                .shouldHave(CollectionCondition.sizeGreaterThanOrEqual(count),
                        Duration.ofSeconds(15));

        return this;
    }

    @Step("Grid: получить все тексты строк")
    public List<String> getAllRowTexts() {
        return $$("[class*='grid-row']")
                .filter(Condition.visible)
                .texts();
    }

    @Step("Grid: получить все строки")
    public ElementsCollection getAllRows(String marker) {
        return getGrid(marker)
                .$$x(".//div[contains(@class,'grid-row')" +
                        " and not(contains(@class,'grid-header'))]")
                .filter(visible);
    }

    // Добавить в GridComponent.java:

    @Step("Click first row in grid: {gridId}")
    public void clickFirstRowInGrid(String gridId) {
        log.info("Click first row in grid: '{}'", gridId);

        SelenideElement row = $x(
                "//div[@id='" + gridId + "']" +
                        "//div[contains(@class,'grid-row')" +
                        " and not(contains(@class,'grid-header'))]"
        ).shouldBe(Condition.visible).scrollIntoView(true);

        executeJavaScript("arguments[0].click();", row);
    }
}