package com.creatio.autotest.components;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BaseComponent;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class ModalComponent extends BaseComponent {

    // ══════════════════════════════════════
    // GET MODAL
    // ══════════════════════════════════════

    private SelenideElement getMiniPage() {
        return $x("//*[@data-item-marker='MiniPage']")
                .shouldBe(visible, Duration.ofSeconds(15));
    }

    private SelenideElement getModal(String marker) {
        return $x("//*[@data-item-marker='" + marker + "']")
                .shouldBe(visible, Duration.ofSeconds(15));
    }

    // ══════════════════════════════════════
    // WAIT
    // ══════════════════════════════════════

    @Step("Modal: ожидать открытия MiniPage")
    public ModalComponent waitOpen() {
        log.info("Waiting for MiniPage to open");
        getMiniPage();
        return this;
    }

    @Step("Modal: ожидать открытия '{marker}'")
    public ModalComponent waitOpen(String marker) {
        log.info("Waiting for modal: '{}'", marker);
        getModal(marker);
        return this;
    }

    @Step("Modal: ожидать закрытия MiniPage")
    public ModalComponent waitClose() {
        log.info("Waiting for MiniPage to close");
        $x("//*[@data-item-marker='MiniPage']")
                .should(disappear, Duration.ofSeconds(15));
        return this;
    }

    // ══════════════════════════════════════
    // CLICK BUTTON
    // ══════════════════════════════════════

    @Step("Modal: нажать кнопку '{text}'")
    public ModalComponent clickButton(String text) {
        log.info("Modal clickButton: '{}'", text);

        SelenideElement button = getMiniPage()
                .$x(".//span[contains(text(),'" + text + "')]")
                .shouldBe(visible, enabled);

        executeJavaScript("arguments[0].click();", button);
        return this;
    }

    @Step("Modal: нажать кнопку по marker '{marker}'")
    public ModalComponent clickButtonByMarker(String marker) {
        log.info("Modal clickButtonByMarker: '{}'", marker);

        SelenideElement button = getMiniPage()
                .$x(".//*[@data-item-marker='" + marker + "']")
                .shouldBe(visible, enabled);

        executeJavaScript("arguments[0].click();", button);
        return this;
    }

    // ══════════════════════════════════════
    // SELECT LIST ITEM
    // ══════════════════════════════════════

    @Step("Modal: выбрать '{value}' из списка")
    public ModalComponent selectListItem(String value) {
        log.info("Modal selectListItem: '{}'", value);

        SelenideElement item = getMiniPage()
                .$x(".//li[normalize-space(text())='" + value + "']")
                .shouldBe(visible);

        executeJavaScript("arguments[0].click();", item);
        return this;
    }

    @Step("Modal: выбрать элемент по marker '{marker}'")
    public ModalComponent selectListItemByMarker(String marker) {
        log.info("Modal selectListItemByMarker: '{}'", marker);

        SelenideElement item = getMiniPage()
                .$x(".//*[@data-item-marker='" + marker + "']")
                .shouldBe(visible);

        executeJavaScript("arguments[0].click();", item);
        return this;
    }

    // ══════════════════════════════════════
    // VALIDATE
    // ══════════════════════════════════════

    @Step("Modal: проверить текст '{text}'")
    public ModalComponent validateText(String text) {
        log.info("Modal validateText: '{}'", text);
        getMiniPage().shouldHave(Condition.text(text));
        return this;
    }
}