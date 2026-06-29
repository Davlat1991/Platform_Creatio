package com.creatio.autotest.core.driver;

import com.codeborne.selenide.Configuration;
import com.creatio.autotest.core.config.Config;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.codeborne.selenide.Configuration.baseUrl;

public final class DriverSetup {

    private static final Logger log = LoggerFactory.getLogger(DriverSetup.class);
    private static final Config config = Config.getInstance();

    private DriverSetup() {}

    // ══════════════════════════════════════
    // INIT
    // ══════════════════════════════════════

    public static void init() {
        String browser = config.getBrowser().toLowerCase().trim();
        boolean headless = config.isHeadless();

        log.info("Initializing WebDriver: browser={} headless={}", browser, headless);

        // Selenide configuration
        baseUrl      = config.getBaseUrl();
        Configuration.timeout      = config.getTimeout() * 1000;
        Configuration.pageLoadTimeout = config.getPageLoadTimeout() * 1000;
        Configuration.screenshots  = true; //после прогона сделать false
        Configuration.savePageSource = false;
        Configuration.reportsFolder   = "target/selenide"; // ← скриншоты Selenide теперь под target/
        Configuration.browser      = browser;
        Configuration.headless     = headless;

        // Создаём драйвер
        WebDriver driver = createDriver(browser, headless);
        driver.manage().window().maximize();

        // Регистрируем в ThreadLocal
        DriverProvider.set(driver);

        log.info("WebDriver initialized successfully");
    }

    // ══════════════════════════════════════
    // FACTORY
    // ══════════════════════════════════════

    private static WebDriver createDriver(String browser, boolean headless) {
        switch (browser) {
            case "chrome":
                return createChrome(headless);
            case "firefox":
                return createFirefox(headless);
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
    }

    private static WebDriver createChrome(boolean headless) {
        ChromeOptions options = new ChromeOptions();

        if (headless) {
            options.addArguments("--headless=new");
        }

        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--window-size=1920,1080",
                "--disable-extensions",
                "--remote-allow-origins=*",
                "--safebrowsing-disable-download-protection",
                "--disable-features=InsecureDownloadWarnings,SafeBrowsing",
                "--allow-running-insecure-content",
                "--unsafely-treat-insecure-origin-as-secure=" + baseUrl  // ← динамически
        );

        String downloadPath = System.getProperty("user.dir") + "\\downloads";

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory",               downloadPath);
        prefs.put("download.prompt_for_download",             false);
        prefs.put("download.directory_upgrade",               true);
        prefs.put("safebrowsing.enabled",                     false);
        prefs.put("safebrowsing.disable_download_protection", true);
        prefs.put("plugins.always_open_pdf_externally",       true);
        prefs.put("profile.default_content_setting_values.notifications", 2); // блокировать уведомления

        options.setExperimentalOption("prefs", prefs);

        log.info("Download path: {}", downloadPath);

        return new ChromeDriver(options);
    }


    private static WebDriver createFirefox(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();

        if (headless) {
            options.addArguments("--headless");
        }

        return new FirefoxDriver(options);
    }
}