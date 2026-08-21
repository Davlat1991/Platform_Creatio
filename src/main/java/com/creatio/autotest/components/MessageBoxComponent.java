package com.creatio.autotest.components;

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


    // ══════════════════════════════════════
    // VALIDATE
    // ══════════════════════════════════════

    @Step("Should see modal with text: {text}")
    public MessageBoxComponent shouldSeeModal(String text) {
        log.info("Should see modal: '{}'", text);
        $x(MSGBOX_XPATH + "[contains(.,'" + text + "')]")
                .shouldBe(visible, Duration.ofSeconds(100));
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