package com.creatio.autotest.base;

import com.creatio.autotest.utils.AllureAttachments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestListener;
import org.testng.ITestResult;

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

}