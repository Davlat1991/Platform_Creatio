package com.creatio.autotest.components;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BaseComponent;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class MessageBoxComponent extends BaseComponent {

    // ══════════════════════════════════════
    // ЛОКАТОР МОДАЛЬНОГО ОКНА
    // Creatio 7.18 использует класс "ts-messagebox-box".
    // Старые варианты оставлены для совместимости.
    // ══════════════════════════════════════

    private static final String MSGBOX_XPATH =
            "//div[contains(@class,'ts-messagebox-box')" +
                    " or contains(@class,'messagebox')" +
                    " or contains(@class,'msg-box')]";

    private SelenideElement getMessageBox() {
        return $x(MSGBOX_XPATH).shouldBe(visible, Duration.ofSeconds(15));
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
        $x(MSGBOX_XPATH).should(disappear, Duration.ofSeconds(15));
        return this;
    }

    // ══════════════════════════════════════
    // VALIDATE
    // ══════════════════════════════════════

    @Step("Validate message box text: {text}")
    public MessageBoxComponent validateText(String text) {
        log.info("Validate message box text: '{}'", text);
        // shouldHave(text) проверяет текст рекурсивно по всему контейнеру —
        // устойчивее чем .//*[contains(text(),...)] который требует точного
        // видимого дочернего узла.
        getMessageBox()
                .shouldHave(Condition.text(text), Duration.ofSeconds(20));
        return this;
    }

    @Step("Should see modal with text: {text}")
    public MessageBoxComponent shouldSeeModal(String text) {
        log.info("Should see modal: '{}'", text);
        $x(MSGBOX_XPATH + "[contains(.,'" + text + "')]")
                .shouldBe(visible, Duration.ofSeconds(60));
        return this;
    }

    // ══════════════════════════════════════
    // CLICK
    // ══════════════════════════════════════

    @Step("Click OK in message box")
    public MessageBoxComponent clickOk() {
        log.info("Click OK in message box");
        SelenideElement box = getMessageBox();
        // Creatio 7.18: кнопка ОК имеет data-item-marker="OK" (подтверждено DOM).
        // Fallback — по тексту кнопки.
        SelenideElement btn = box.find("[data-item-marker='OK']");
        if (!btn.exists()) {
            btn = box.$x(".//span[normalize-space(.)='OK'" +
                    " or normalize-space(.)='ОК']");
        }
        btn.shouldBe(visible, enabled).click();
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

    // ══════════════════════════════════════
    // OPTIONAL CONFIRM — нажать кнопку, ЕСЛИ диалог появился.
    // Если диалога нет — тихо продолжаем (без ожидания/падения).
    // ══════════════════════════════════════

    @Step("Подтвердить диалог '{expectedTextPart}' кнопкой '{buttonMarker}', если он появился")
    public MessageBoxComponent confirmIfPresent(String buttonMarker, String expectedTextPart) {
        log.info("confirmIfPresent: button='{}' expectedText~'{}'", buttonMarker, expectedTextPart);

        SelenideElement box = $x(MSGBOX_XPATH);

        if (box.is(visible, Duration.ofSeconds(3))) {
            String actualText = box.getText();

            if (expectedTextPart != null && !actualText.contains(expectedTextPart)) {
                log.warn("Появился НЕ ожидаемый диалог. Текст: '{}'. Пропуск подтверждения.",
                        actualText);
                return this;
            }

            SelenideElement btn = $x("//*[@data-item-marker='" + buttonMarker + "']")
                    .shouldBe(visible, Duration.ofSeconds(5));
            executeJavaScript("arguments[0].click();", btn);
            box.should(disappear, Duration.ofSeconds(20));
            log.info("Диалог подтверждён кнопкой '{}'", buttonMarker);
        } else {
            log.debug("Диалог не появился — пропуск");
        }
        return this;
    }
}