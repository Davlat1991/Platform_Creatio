package com.creatio.autotest.utils;

import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public final class AllureAttachments {

    private static final Logger log = LoggerFactory.getLogger(AllureAttachments.class);

    private AllureAttachments() {}

    // ══════════════════════════════════════
    // SCREENSHOT
    // ══════════════════════════════════════

    public static void attachScreenshot(String name) {
        try {
            if (!WebDriverRunner.hasWebDriverStarted()) return;

            byte[] screenshot = ((TakesScreenshot) WebDriverRunner.getWebDriver())
                    .getScreenshotAs(OutputType.BYTES);

            Allure.addAttachment(
                    name,
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    "png"
            );

            log.info("Screenshot attached: {}", name);

        } catch (Exception e) {
            log.warn("Failed to attach screenshot: {}", e.getMessage());
        }
    }

    public static void attachScreenshot() {
        attachScreenshot("Screenshot");
    }

    // ══════════════════════════════════════
    // PAGE SOURCE
    // ══════════════════════════════════════

    public static void attachPageSource() {
        try {
            if (!WebDriverRunner.hasWebDriverStarted()) return;

            String source = WebDriverRunner.getWebDriver().getPageSource();

            Allure.addAttachment(
                    "Page Source",
                    "text/html",
                    new ByteArrayInputStream(
                            source.getBytes(StandardCharsets.UTF_8)
                    ),
                    "html"
            );

            log.info("Page source attached");

        } catch (Exception e) {
            log.warn("Failed to attach page source: {}", e.getMessage());
        }
    }

    // ══════════════════════════════════════
    // TEXT
    // ══════════════════════════════════════

    public static void attachText(String name, String content) {
        try {
            Allure.addAttachment(
                    name,
                    "text/plain",
                    new ByteArrayInputStream(
                            content.getBytes(StandardCharsets.UTF_8)
                    ),
                    "txt"
            );

            log.info("Text attached: {}", name);

        } catch (Exception e) {
            log.warn("Failed to attach text: {}", e.getMessage());
        }
    }

    // ══════════════════════════════════════
    // BROWSER LOGS
    // ══════════════════════════════════════

    public static void attachBrowserLogs() {
        try {
            if (!WebDriverRunner.hasWebDriverStarted()) return;

            StringBuilder logs = new StringBuilder();

            WebDriverRunner.getWebDriver()
                    .manage()
                    .logs()
                    .get("browser")
                    .forEach(entry -> logs
                            .append(entry.getLevel())
                            .append(": ")
                            .append(entry.getMessage())
                            .append("\n"));

            if (logs.length() > 0) {
                attachText("Browser Logs", logs.toString());
                log.info("Browser logs attached");
            }

        } catch (Exception e) {
            log.warn("Failed to attach browser logs: {}", e.getMessage());
        }
    }

    // ══════════════════════════════════════
    // URL
    // ══════════════════════════════════════

    public static void attachCurrentUrl() {
        try {
            if (!WebDriverRunner.hasWebDriverStarted()) return;

            String url = WebDriverRunner.getWebDriver().getCurrentUrl();
            attachText("Current URL", url);

        } catch (Exception e) {
            log.warn("Failed to attach URL: {}", e.getMessage());
        }
    }
}