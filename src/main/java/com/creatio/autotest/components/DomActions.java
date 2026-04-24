package com.creatio.autotest.components;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BaseComponent;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class DomActions extends BaseComponent {

    @Step("Click div by id and set value: {id}")
    public DomActions clickDivById(String id, String value) {
        log.info("clickDivById: id='{}' value='{}'", id, value);

        SelenideElement wrap = $("[data-item-marker='" + id + "'], #" + id)
                .shouldBe(Condition.visible);

        executeJavaScript("arguments[0].click();", wrap);

        SelenideElement input = wrap.$("input, textarea")
                .shouldBe(Condition.visible);

        input.clear();
        input.setValue(value);

        return this;
    }

    @Step("Set grid input value: {id}")
    public DomActions setGridInputValue(String id, String value) {
        log.info("setGridInputValue: id='{}' value='{}'", id, value);

        SelenideElement input = $("[id='" + id + "']")
                .shouldBe(Condition.visible);

        input.clear();
        input.setValue(value);

        return this;
    }
}