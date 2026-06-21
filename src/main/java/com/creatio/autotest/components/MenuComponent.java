package com.creatio.autotest.components;

import com.codeborne.selenide.Condition;
import com.creatio.autotest.core.base.BaseComponent;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;

public class MenuComponent extends BaseComponent {

    /**
     * Клик по пункту меню/листвью.
     * Приоритет: data-item-marker (стабильный) → normalize-space текст (fallback).
     * Используется для Creatio listview, ComboBox дропдаунов и контекстных меню.
     */
    @Step("Click menu item: {value}")
    public MenuComponent clickByName(String value) {
        log.info("Click menu item: '{}'", value);

        $x("//li[@data-item-marker='" + value + "']" +
                " | //li[normalize-space(.)='" + value + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(15))
                .click();

        return this;
    }

    public MenuComponent doubleclickButtonByName(String nameButton){
        $x("//span[.='" + nameButton + "']").doubleClick();

        return this;
    }

    @Step("Click menu item by marker: {marker}")
    public MenuComponent clickByMarker(String marker) {
        log.info("Click menu item marker: '{}'", marker);

        $x("//li[@data-item-marker='" + marker + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(7))
                .click();

        return this;
    }
}