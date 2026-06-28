package com.creatio.autotest.base;

import com.codeborne.selenide.logevents.SelenideLogger;
import com.creatio.autotest.core.config.Config;
import com.creatio.autotest.core.driver.DriverProvider;
import com.creatio.autotest.core.driver.DriverSetup;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import io.qameta.allure.selenide.AllureSelenide;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Listeners(TestListener.class)
public abstract class BaseTest {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected final Config config = Config.getInstance();

    // ══════════════════════════════════════
    // SUITE SETUP — Allure environment widget
    // ══════════════════════════════════════

    @BeforeSuite(alwaysRun = true)
    public void writeAllureEnvironment() {
        try {
            // surefire передаёт путь с переносами строк → trim()
            String dir = System.getProperty(
                    "allure.results.directory", "target/allure-results").trim();
            Path resultsDir = Path.of(dir);
            Files.createDirectories(resultsDir);

            String content = String.join("\n",
                    "Environment=" + config.getEnvironment().name(),
                    "Stand=" + config.getBaseUrl(),
                    "Browser=" + config.getBrowser(),
                    "Headless=" + config.isHeadless(),
                    "Timeout.ms=" + config.getTimeout(),
                    "PageLoad.ms=" + config.getPageLoadTimeout(),
                    "Creatio=7.18",
                    "Java=Corretto 17",
                    "Selenide=7.2.1",
                    "TestNG=7.11.0"
            );

            Files.writeString(
                    resultsDir.resolve("environment.properties"),
                    content,
                    StandardCharsets.UTF_8);

            log.info("Allure environment.properties записан → {}", resultsDir);

        } catch (Exception e) {
            // не валим сьют из-за виджета
            log.warn("Не удалось записать environment.properties: {}", e.getMessage());
        }
    }

    // ══════════════════════════════════════
    // SETUP / TEARDOWN
    // ══════════════════════════════════════

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        log.info("════════════════════════════════════");
        log.info("Thread: {}", Thread.currentThread().getName());
        log.info("Setting up WebDriver...");

        // Allure ↔ Selenide: каждое действие = шаг, скриншот + page source на падении шага
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide()
                .screenshots(true)
                .savePageSource(true));

        DriverSetup.init();
        ScenarioContextProvider.init();

        log.info("WebDriver ready");
        log.info("════════════════════════════════════");
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        log.info("Tearing down WebDriver...");
        ScenarioContextProvider.remove();
        DriverProvider.remove();

        // Снимаем листенер в том же потоке, где добавили (важно для parallel)
        SelenideLogger.removeListener("AllureSelenide");

        log.info("WebDriver removed");
    }
}