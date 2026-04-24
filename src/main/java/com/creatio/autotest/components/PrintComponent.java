package com.creatio.autotest.components;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BaseComponent;
import com.creatio.autotest.utils.AllureAttachments;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class PrintComponent extends BaseComponent {

    @Step("Выбор пункта печати: {name}")
    public void selectPrintOption(String name) {
        log.info("Select print option: '{}'", name);

        try {
            SelenideElement printBtn = $x(
                    "//*[@data-item-marker='PrintButton']"
            ).shouldBe(Condition.visible, Duration.ofSeconds(10));

            executeJavaScript("arguments[0].click();", printBtn);

            SelenideElement item = $x(
                    "//*[contains(@data-item-marker,'" + name + "')" +
                            " or contains(text(),'" + name + "')]"
            ).shouldBe(Condition.visible, Duration.ofSeconds(10));

            executeJavaScript("arguments[0].click();", item);

            log.info("Print option selected: '{}'", name);

        } catch (Throwable t) {
            AllureAttachments.attachScreenshot("Print failure");
            AllureAttachments.attachPageSource();
            throw t;
        }
    }
}