package com.creatio.autotest.base;

import com.codeborne.selenide.Selenide;
import com.creatio.autotest.utils.AllureAttachments;
import io.qameta.allure.Allure;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.codeborne.selenide.WebDriverRunner;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class TestListener implements ITestListener {

    private static final Logger log = LoggerFactory.getLogger(TestListener.class);

    // ══════════════════════════════════════
    // TEST EVENTS
    // ══════════════════════════════════════

    @Override
    public void onTestStart(ITestResult result) {
        log.info("▶ START: {}.{}",
                result.getTestClass().getName(),
                result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("✅ PASS: {}", result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        log.error("❌ FAIL: {} — {}",
                result.getMethod().getMethodName(),
                result.getThrowable().getMessage());

        // ✅ Используем утилиту
        AllureAttachments.attachScreenshot("Failure: " + result.getMethod().getMethodName());
        AllureAttachments.attachPageSource();
        AllureAttachments.attachCurrentUrl();
        AllureAttachments.attachBrowserLogs();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("⏭ SKIP: {}", result.getMethod().getMethodName());
    }

    // ══════════════════════════════════════
    // ATTACHMENTS
    // ══════════════════════════════════════

    private void takeScreenshot(String testName) {
        try {
            if (WebDriverRunner.hasWebDriverStarted()) {
                byte[] screenshot = ((TakesScreenshot) WebDriverRunner.getWebDriver())
                        .getScreenshotAs(OutputType.BYTES);

                Allure.addAttachment(
                        "Screenshot: " + testName,
                        "image/png",
                        new ByteArrayInputStream(screenshot),
                        "png"
                );

                log.info("Screenshot attached for: {}", testName);
            }
        } catch (Exception e) {
            log.warn("Failed to take screenshot: {}", e.getMessage());
        }
    }

    private void attachPageSource() {
        try {
            if (WebDriverRunner.hasWebDriverStarted()) {
                String pageSource = WebDriverRunner.getWebDriver().getPageSource();
                Allure.addAttachment(
                        "Page Source",
                        "text/html",
                        new ByteArrayInputStream(
                                pageSource.getBytes(StandardCharsets.UTF_8)
                        ),
                        "html"
                );
            }
        } catch (Exception e) {
            log.warn("Failed to attach page source: {}", e.getMessage());
        }
    }

    @Attachment(value = "Browser Logs", type = "text/plain")
    private String attachBrowserLogs() {
        try {
            if (WebDriverRunner.hasWebDriverStarted()) {
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
                return logs.toString();
            }
        } catch (Exception e) {
            log.warn("Failed to attach browser logs: {}", e.getMessage());
        }
        return "No logs available";
    }
}