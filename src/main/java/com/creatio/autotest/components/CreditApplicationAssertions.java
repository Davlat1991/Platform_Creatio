package com.creatio.autotest.components;

import com.codeborne.selenide.Condition;
import com.creatio.autotest.core.base.BaseComponent;
import com.creatio.autotest.core.verify.VerificationCollector;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;

public class CreditApplicationAssertions extends BaseComponent {

    @Step("Проверить состояние ордера: {expected}")
    public void assertOrderState(String expected) {
        log.info("Assert order state: '{}'", expected);

        String actual = getOrderState();

        // Пишем результат в отчёт верификации (V1): проверка становится видимой
        // в per-stage отчёте и Allure. Throw-поведение ниже сохранено 1:1.
        VerificationCollector.check("Состояние ордера", expected, actual);

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
        ).shouldBe(Condition.visible, Duration.ofSeconds(10))
                .getValue()
                .trim();
    }
}