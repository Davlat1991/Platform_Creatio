package com.creatio.autotest.core.base;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class BasePage extends BaseComponent {

    // ══════════════════════════════════════
    // NAVIGATION
    // ══════════════════════════════════════

    @Step("Open URL: {url}")
    public void open(String url) {
        log.info("Opening URL: {}", url);
        Selenide.open(url);
        waitForPage();
    }

    @Step("Open base URL")
    public void openBaseUrl() {
        log.info("Opening base URL: {}", config.getBaseUrl());
        Selenide.open(config.getBaseUrl());
        waitForPage();
    }

    @Step("Refresh page")
    public void refresh() {
        log.info("Refreshing page");
        Selenide.refresh();
        waitForPage();
    }

    // ══════════════════════════════════════
    // WAIT FOR PAGE
    // ══════════════════════════════════════

    @Step("Wait for page ready")
    public void waitForPage() {
        for (int i = 0; i < 40; i++) {
            boolean ready = Boolean.TRUE.equals(
                    executeJavaScript("return document.readyState === 'complete';")
            );

            boolean busy = false;
            for (SelenideElement e : $$(".ts-loader,.ui-loader,.process-indicator,.loading-indicator,.mask")) {
                if (e.exists() && e.is(Condition.visible)) {
                    busy = true;
                    break;
                }
            }

            if (ready && !busy) return;

            Selenide.sleep(250);
        }
        throw new RuntimeException("Page did not load in time");
    }

    // ══════════════════════════════════════
    // LOOKUP ICON (Creatio specific)
    // ══════════════════════════════════════

    @Step("Click lookup icon: {inputId}")
    public BasePage clickLookupIcon(String inputId) {
        log.info("Click lookup icon: {}", inputId);

        SelenideElement input = $x("//input[@id='" + inputId + "']")
                .shouldBe(visible)
                .scrollIntoView(true);

        SelenideElement wrapper = input.closest("div");
        wrapper.hover();

        SelenideElement icon = wrapper
                .$x(".//div[contains(@class,'right-icon')]")
                .shouldBe(visible);

        executeJavaScript("arguments[0].click();", icon);
        return this;
    }

    // ══════════════════════════════════════
    // VALIDATION
    // ══════════════════════════════════════

    @Step("Check page by marker: {marker}")
    public BasePage checkPage(String marker) {
        log.info("Check page marker: {}", marker);
        $x("//*[@data-item-marker='" + marker + "']")
                .should(appear, Duration.ofSeconds(15));
        return this;
    }

    public boolean isVisible(String xpath) {
        try {
            return $x(xpath).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // ══════════════════════════════════════
    // RETRY
    // ══════════════════════════════════════

    protected <T> T retry(java.util.concurrent.Callable<T> callable,
                          int attempts, long sleepMs) {
        Throwable last = null;
        for (int i = 1; i <= attempts; i++) {
            try {
                return callable.call();
            } catch (Throwable e) {
                last = e;
                if (i < attempts) Selenide.sleep(sleepMs);
            }
        }
        throw new RuntimeException(
                "Retry failed after " + attempts + " attempts", last
        );
    }
}