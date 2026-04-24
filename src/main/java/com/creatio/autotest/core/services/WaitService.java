package com.creatio.autotest.core.services;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class WaitService {

    private static final Logger log = LoggerFactory.getLogger(WaitService.class);

    // ══════════════════════════════════════
    // VISIBLE / HIDDEN
    // ══════════════════════════════════════

    @Step("Wait visible: {element}")
    public void waitVisible(SelenideElement element) {
        element.shouldBe(visible);
    }

    @Step("Wait visible: {element} timeout={seconds}s")
    public void waitVisible(SelenideElement element, int seconds) {
        element.shouldBe(visible, Duration.ofSeconds(seconds));
    }

    @Step("Wait hidden: {element}")
    public void waitHidden(SelenideElement element) {
        element.shouldBe(hidden);
    }

    // ══════════════════════════════════════
    // LOADER / OVERLAY
    // ══════════════════════════════════════

    @Step("Wait for loader hidden")
    public void waitForLoader() {
        try {
            $x("//div[contains(@class,'ts-loader-mask')]")
                    .should(disappear, Duration.ofSeconds(15));
        } catch (Exception e) {
            log.debug("Loader not found or already hidden");
        }
    }

    @Step("Wait until not busy")
    public void waitUntilNotBusy() {
        for (int i = 0; i < 40; i++) {
            boolean busy = false;
            for (SelenideElement e : $$(".ts-loader,.ui-loader,.process-indicator,.loading-indicator,.mask")) {
                if (e.exists() && e.is(Condition.visible)) {
                    busy = true;
                    break;
                }
            }
            if (!busy) return;
            Selenide.sleep(250);
        }
        throw new RuntimeException("Loader did not disappear in time");
    }

    // ══════════════════════════════════════
    // PAGE READY
    // ══════════════════════════════════════

    @Step("Wait for page ready state")
    public void waitForPageReady() {
        Selenide.Wait().until(driver ->
                ((org.openqa.selenium.JavascriptExecutor) driver)
                        .executeScript("return document.readyState")
                        .equals("complete")
        );
    }
}