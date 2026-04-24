package com.creatio.autotest.flows;

import com.creatio.autotest.core.base.BasePage;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NavigationFlow {

    private static final Logger log = LoggerFactory.getLogger(NavigationFlow.class);

    private final BasePage basePage = new BasePage();

    // ══════════════════════════════════════
    // NAVIGATE
    // ══════════════════════════════════════

    @Step("Открыть URL: {url}")
    public void open(String url) {
        log.info("Opening URL: {}", url);
        basePage.open(url);
        basePage.waitForPage();
    }
}