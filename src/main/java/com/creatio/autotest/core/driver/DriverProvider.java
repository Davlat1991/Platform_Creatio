package com.creatio.autotest.core.driver;

import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DriverProvider {

    private static final Logger log = LoggerFactory.getLogger(DriverProvider.class);

    // ThreadLocal — каждый поток имеет свой драйвер
    private static final ThreadLocal<WebDriver> driverHolder = new ThreadLocal<>();

    private DriverProvider() {}

    // ══════════════════════════════════════
    // GET / SET / REMOVE
    // ══════════════════════════════════════

    public static WebDriver get() {
        WebDriver driver = driverHolder.get();
        if (driver == null) {
            throw new IllegalStateException(
                    "WebDriver not initialized. Call DriverSetup.init() first."
            );
        }
        return driver;
    }

    public static void set(WebDriver driver) {
        driverHolder.set(driver);
        WebDriverRunner.setWebDriver(driver);
        log.info("WebDriver set for thread: {}", Thread.currentThread().getName());
    }

    public static void remove() {
        WebDriver driver = driverHolder.get();
        if (driver != null) {
            try {
                driver.quit();
                log.info("WebDriver quit for thread: {}", Thread.currentThread().getName());
            } catch (Exception e) {
                log.warn("Error quitting WebDriver: {}", e.getMessage());
            } finally {
                driverHolder.remove();
            }
        }
    }

    public static boolean isInitialized() {
        return driverHolder.get() != null;
    }
}