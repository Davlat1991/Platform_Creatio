package com.creatio.autotest.components;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BaseComponent;
import io.qameta.allure.Step;
import org.testng.Assert;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;

public class CreditApplicationAssertions extends BaseComponent {

    @Step("Проверить состояние ордера: {expected}")
    public void assertOrderState(String expected) {
        log.info("Assert order state: '{}'", expected);

        String actual = getOrderState();

        Assert.assertEquals(actual, expected,
                "Order state mismatch"
        );
    }

    private String getOrderState() {
        return $x(
                "//label[contains(text(),'Состояние ордера')]" +
                        "/ancestor::div[contains(@class,'label-wrap')]" +
                        "/following-sibling::div[contains(@class,'control-wrap')]" +
                        "//input"
        ).shouldBe(Condition.visible, Duration.ofSeconds(10))
                .getValue()
                .trim();
    }
}