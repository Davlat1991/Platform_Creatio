package com.creatio.autotest.core.services;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class ClickService {

    private static final Logger log = LoggerFactory.getLogger(ClickService.class);

    // ══════════════════════════════════════
    // BASIC CLICK
    // ══════════════════════════════════════

    @Step("Click: {element}")
    public void click(SelenideElement element) {
        log.debug("Click on element");
        element.shouldBe(visible).shouldBe(enabled).click();
    }

    @Step("JS Click: {element}")
    public void jsClick(SelenideElement element) {
        log.debug("JS click on element");
        executeJavaScript(
                "arguments[0].scrollIntoView(true); arguments[0].click();",
                element
        );
    }

    @Step("Safe click: {element}")
    public void safeClick(SelenideElement element) {
        for (int i = 1; i <= 3; i++) {
            try {
                element.shouldBe(visible, enabled)
                        .click();
                return;
            } catch (Throwable t) {
                log.warn("safeClick attempt {}/3: {}", i, t.getMessage());
                if (i == 3) {
                    jsClick(element);
                }
            }
        }
    }

    // ══════════════════════════════════════
    // CLICK BY LOCATOR
    // ══════════════════════════════════════

    @Step("Click by marker: {marker}")
    public void clickByMarker(String marker) {
        log.info("Click by marker: {}", marker);
        SelenideElement el = $x("//*[@data-item-marker='" + marker + "']")
                .shouldBe(visible)
                .shouldBe(enabled);
        executeJavaScript("arguments[0].click();", el);
    }

    @Step("Click by name: {name}")
    public void clickByName(String name) {
        log.info("Click by name: {}", name);
        $x("//span[normalize-space(.)='" + name + "']")
                .shouldBe(visible)
                .shouldBe(enabled)
                .click();
    }

    @Step("Click by id: {id}")
    public void clickById(String id) {
        log.info("Click by id: {}", id);
        SelenideElement el = $x("//span[@id='" + id + "']")
                .shouldBe(visible)
                .shouldBe(enabled);
        try {
            el.click();
        } catch (Throwable e) {
            executeJavaScript("arguments[0].click();", el);
        }
    }

    @Step("Click by tag+marker: {tag}[{marker}]")
    public void clickByTagAndMarker(String tag, String marker) {
        log.info("Click tag={} marker={}", tag, marker);
        SelenideElement el = $x("//" + tag + "[@data-item-marker='" + marker + "']")
                .shouldBe(visible);
        executeJavaScript("arguments[0].click();", el);
    }

    @Step("Click if visible: {marker}")
    public void clickByMarkerIfVisible(String marker) {
        SelenideElement el = $x("//*[@data-item-marker='" + marker + "']")
                .should(exist, Duration.ofSeconds(5));
        if (el.is(visible)) {
            executeJavaScript("arguments[0].click();", el);
        }
    }

    // ══════════════════════════════════════
    // WAIT AND CLICK
    // ══════════════════════════════════════

    @Step("Wait and click by marker: {marker}")
    public void waitAndClick(String marker) {
        log.info("waitAndClick: {}", marker);
        String xpath = "//*[@data-item-marker='" + marker + "']";

        for (int i = 1; i <= 20; i++) {
            try {
                SelenideElement btn = $x(xpath);
                if (btn.exists() && btn.isDisplayed()) {
                    btn.shouldBe(visible, Duration.ofSeconds(5))
                            .shouldBe(enabled, Duration.ofSeconds(5))
                            .click();
                    return;
                }
            } catch (Exception ignored) {}

            // ✅ Вместо sleep — ждём изменения DOM
            $x(xpath).shouldBe(visible, Duration.ofSeconds(3));
        }
        throw new AssertionError(
                "Button marker='" + marker + "' did not appear in time"
        );
    }

    @Step("Wait and click by marker (with refresh): {marker}")
    public void waitAndClickWithRefresh(String marker) {
        log.info("waitAndClickWithRefresh: {}", marker);
        String xpath = "//*[@data-item-marker='" + marker + "']";

        for (int i = 1; i <= 30; i++) {
            try {
                SelenideElement btn = $x(xpath);
                if (btn.exists() && btn.isDisplayed()) {
                    btn.shouldBe(visible, Duration.ofSeconds(5))
                            .shouldBe(enabled, Duration.ofSeconds(5))
                            .click();
                    return;
                }
            } catch (Exception ignored) {}

            com.codeborne.selenide.Selenide.refresh();

            // ✅ Вместо sleep(5000) — ждём появления элемента
            try {
                $x(xpath).shouldBe(visible, Duration.ofSeconds(5));
            } catch (Exception ignored) {}
        }
        throw new AssertionError(
                "Button marker='" + marker + "' did not appear after refresh"
        );
    }

    // ══════════════════════════════════════
    // DOUBLE CLICK
    // ══════════════════════════════════════

    @Step("Double click by marker: {marker}")
    public void doubleClickByMarker(String marker) {
        log.info("doubleClick by marker: {}", marker);
        SelenideElement el = $x("//*[@data-item-marker='" + marker + "']")
                .shouldBe(visible, enabled);
        try {
            el.doubleClick();
        } catch (Throwable e) {
            executeJavaScript(
                    "arguments[0].dispatchEvent(" +
                            "new MouseEvent('dblclick',{bubbles:true,cancelable:true}))",
                    el
            );
        }
    }

    @Step("Double click by text: {text}")
    public void doubleClickByText(String text) {
        log.info("doubleClick by text: {}", text);
        SelenideElement el = $x(
                "//div[contains(@class,'grid-row')]" +
                        "[.//*[contains(normalize-space(.),'" + text + "')]]"
        ).shouldBe(visible).scrollIntoView(true);

        try {
            new Actions(com.codeborne.selenide.WebDriverRunner.getWebDriver())
                    .doubleClick(el).perform();
        } catch (Throwable e) {
            executeJavaScript(
                    "arguments[0].dispatchEvent(" +
                            "new MouseEvent('dblclick',{bubbles:true,cancelable:true}))",
                    el
            );
        }
    }

}