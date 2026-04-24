package com.creatio.autotest.core.services;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.executeJavaScript;

public class ScrollService {

    private static final Logger log = LoggerFactory.getLogger(ScrollService.class);

    // ══════════════════════════════════════
    // SCROLL
    // ══════════════════════════════════════

    @Step("Scroll to element")
    public void scrollTo(SelenideElement element) {
        log.debug("Scroll to element");
        element.scrollIntoView(true);
    }

    @Step("Scroll into center")
    public void scrollToCenter(SelenideElement element) {
        log.debug("Scroll element into center");
        executeJavaScript(
                "arguments[0].scrollIntoView({block:'center'});",
                element
        );
    }

    @Step("Scroll to top")
    public void scrollToTop() {
        log.debug("Scroll to top");
        executeJavaScript("window.scrollTo(0, 0);");
    }

    @Step("Scroll down small")
    public void scrollDownSmall() {
        log.debug("Scroll down small");
        executeJavaScript("window.scrollBy(0, 500);");
    }

    @Step("Scroll to bottom")
    public void scrollToBottom() {
        log.debug("Scroll to bottom");
        executeJavaScript("window.scrollTo(0, document.body.scrollHeight);");
    }
}