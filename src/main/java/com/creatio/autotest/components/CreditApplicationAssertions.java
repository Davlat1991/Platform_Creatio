package com.creatio.autotest.components;

import com.codeborne.selenide.Condition;
import com.creatio.autotest.core.base.BaseComponent;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;

public class CreditApplicationAssertions extends BaseComponent {

    @Step("Проверить состояние ордера: {expected}")
    public void assertOrderState(String expected) {
        log.info("Assert order state: '{}'", expected);

        String actual = getOrderState();

        if (!actual.equals(expected)) {
            throw new AssertionError(
                    "Order state mismatch." +
                            " Expected: '" + expected + "'" +
                            " Actual: '" + actual + "'"
            );
        }

        log.info("Order state verified: '{}'", expected);
    }

    private String getOrderState() {
        return $x(
                "//label[contains(text(),'Состояние ордера')]" +
                        "/ancestor::div[contains(@class,'label-wrap')]" +
                        "/following-sibling::div[contains(@class,'control-wrap')]" +
                        "//input"
        ).shouldBe(Condition.visible, Duration.ofSeconds(30))
                .getValue()
                .trim();
    }
}