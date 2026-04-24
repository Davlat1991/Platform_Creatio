package com.creatio.autotest.components;

import com.codeborne.selenide.Condition;
import com.creatio.autotest.core.base.BaseComponent;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$x;

public class MenuComponent extends BaseComponent {

    @Step("Click menu item: {value}")
    public MenuComponent clickByName(String value) {
        log.info("Click menu item: '{}'", value);
        $x("//li[contains(text(),'" + value + "')]")
                .shouldBe(Condition.visible)
                .click();
        return this;
    }

    @Step("Click menu item by marker: {marker}")
    public MenuComponent clickByMarker(String marker) {
        log.info("Click menu item marker: '{}'", marker);
        $x("//li[@data-item-marker='" + marker + "']")
                .shouldBe(Condition.visible)
                .click();
        return this;
    }
}