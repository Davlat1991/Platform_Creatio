package com.creatio.autotest.core.services;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class TypeService {

    private static final Logger log = LoggerFactory.getLogger(TypeService.class);

    // ══════════════════════════════════════
    // TYPE
    // ══════════════════════════════════════

    @Step("Type '{value}'")
    public void type(SelenideElement element, String value) {
        log.debug("Type '{}' into element", value);
        element.shouldBe(visible).shouldBe(enabled).clear();
        element.sendKeys(value);
    }

    @Step("Safe type '{value}'")
    public void safeType(SelenideElement element, String value) {
        log.debug("Safe type '{}'", value);
        element.shouldBe(visible, enabled).scrollIntoView(true);
        try {
            element.clear();
            element.setValue(value);
        } catch (Throwable t) {
            executeJavaScript(
                    "arguments[0].value = arguments[1];" +
                            "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));",
                    element, value
            );
        }
    }

    @Step("Type '{value}' and press Tab")
    public void typeAndTab(SelenideElement element, String value) {
        type(element, value);
        element.sendKeys(Keys.TAB);
    }

    @Step("Type '{value}' and press Enter")
    public void typeAndEnter(SelenideElement element, String value) {
        type(element, value);
        element.sendKeys(Keys.ENTER);
    }

    @Step("Clear element")
    public void clear(SelenideElement element) {
        element.shouldBe(visible).shouldBe(enabled).clear();
    }
}