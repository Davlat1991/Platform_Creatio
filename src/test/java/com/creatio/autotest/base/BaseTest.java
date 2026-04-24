package com.creatio.autotest.base;

import com.creatio.autotest.core.config.Config;
import com.creatio.autotest.core.driver.DriverProvider;
import com.creatio.autotest.core.driver.DriverSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

@Listeners(TestListener.class)
public abstract class BaseTest {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final Config config = Config.getInstance();

    // ══════════════════════════════════════
    // SETUP / TEARDOWN
    // ══════════════════════════════════════

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        log.info("════════════════════════════════════");
        log.info("Thread: {}", Thread.currentThread().getName());
        log.info("Setting up WebDriver...");

        DriverSetup.init();

        log.info("WebDriver ready");
        log.info("════════════════════════════════════");
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        log.info("Tearing down WebDriver...");
        DriverProvider.remove();
        log.info("WebDriver removed");
    }
}