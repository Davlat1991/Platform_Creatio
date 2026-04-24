package com.creatio.autotest.components;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BaseComponent;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$x;

public class MessageBoxComponent extends BaseComponent {

    // ══════════════════════════════════════
    // GET MESSAGE BOX
    // ══════════════════════════════════════

    private SelenideElement getMessageBox() {
        return $x(
                "//div[contains(@class,'messagebox')" +
                        " or contains(@class,'msg-box')]"
        ).shouldBe(visible, Duration.ofSeconds(15));
    }

    // ══════════════════════════════════════
    // WAIT
    // ══════════════════════════════════════

    @Step("Wait message box appear")
    public MessageBoxComponent waitAppear() {
        log.info("Wait message box appear");
        getMessageBox();
        return this;
    }

    @Step("Wait message box close")
    public MessageBoxComponent waitClose() {
        log.info("Wait message box close");
        $x("//div[contains(@class,'messagebox') or contains(@class,'msg-box')]")
                .should(disappear, Duration.ofSeconds(15));
        return this;
    }

    // ══════════════════════════════════════
    // VALIDATE
    // ══════════════════════════════════════

    @Step("Validate message box text: {text}")
    public MessageBoxComponent validateText(String text) {
        log.info("Validate message box text: '{}'", text);
        getMessageBox()
                .$x(".//*[contains(text(),'" + text + "')]")
                .shouldBe(visible);
        return this;
    }

    @Step("Should see modal with text: {text}")
    public MessageBoxComponent shouldSeeModal(String text) {
        log.info("Should see modal: '{}'", text);
        $x("//div[contains(@class,'modal')" +
                " or contains(@class,'dialog')" +
                " or contains(@class,'message')]" +
                "[contains(.,'" + text + "')]")
                .shouldBe(visible, Duration.ofSeconds(30));
        return this;
    }

    // ══════════════════════════════════════
    // CLICK
    // ══════════════════════════════════════

    @Step("Click OK in message box")
    public MessageBoxComponent clickOk() {
        log.info("Click OK in message box");
        getMessageBox()
                .$x(".//span[normalize-space(.)='OK'" +
                        " or normalize-space(.)='ОК']")
                .shouldBe(visible, enabled)
                .click();
        return this;
    }

    @Step("Click button in message box: {name}")
    public MessageBoxComponent clickButton(String name) {
        log.info("Click message box button: '{}'", name);
        getMessageBox()
                .$x(".//span[normalize-space(.)='" + name + "']")
                .shouldBe(visible, enabled)
                .click();
        return this;
    }

    // ══════════════════════════════════════
    // CONFIRM
    // ══════════════════════════════════════

    @Step("Confirm OK: {text}")
    public MessageBoxComponent confirmOk(String text) {
        waitAppear();
        validateText(text);
        clickOk();
        waitClose();
        return this;
    }
}