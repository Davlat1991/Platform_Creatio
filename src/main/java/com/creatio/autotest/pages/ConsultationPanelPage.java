package com.creatio.autotest.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BasePage;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class ConsultationPanelPage extends BasePage {

    // ══════════════════════════════════════
    // LOCATORS
    // ══════════════════════════════════════

    private static final String RIGHT_PANEL    = "//div[@id='rightPanel']";
    private static final String TOGGLE_BUTTON  = "//span[@id='view-button-OBSW-imageEl']";
    private static final String MESSAGE_BOX_OK = "//span[@data-item-marker='ОК']";

    // ══════════════════════════════════════
    // PANEL CONTROL
    // ══════════════════════════════════════

    @Step("Ensure consultation panel opened")
    public ConsultationPanelPage ensureOpened() {
        if ($x(RIGHT_PANEL).is(Condition.visible)) {
            log.info("Consultation panel already open");
            return this;
        }
        log.info("Opening consultation panel");
        $x(TOGGLE_BUTTON)
                .shouldBe(Condition.visible, Condition.enabled)
                .click();
        $x(RIGHT_PANEL).shouldBe(Condition.visible, Duration.ofSeconds(5));
        return this;
    }

    @Step("Close consultation panel if opened")
    public ConsultationPanelPage closeIfOpened() {
        if (!$x(RIGHT_PANEL).is(Condition.visible)) {
            log.info("Consultation panel already closed");
            return this;
        }
        log.info("Closing consultation panel");
        $x(TOGGLE_BUTTON)
                .shouldBe(Condition.visible, Condition.enabled)
                .click();
        $x(RIGHT_PANEL).shouldBe(Condition.hidden, Duration.ofSeconds(5));
        return this;
    }

    // ══════════════════════════════════════
    // MESSAGE BOX
    // ══════════════════════════════════════

    @Step("Close message box if exists")
    public ConsultationPanelPage closeMessageBoxIfExists() {
        SelenideElement msg = $x("//div[contains(@class,'messagebox-caption')]");
        if (msg.exists() && msg.isDisplayed()) {
            log.info("Closing message box");
            $x(MESSAGE_BOX_OK)
                    .shouldBe(Condition.visible)
                    .click();
        }
        return this;
    }

    // ══════════════════════════════════════
    // REGISTER PRODUCT
    // ══════════════════════════════════════

    @Step("Register product by DIM: {dim}")
    public ConsultationPanelPage registerProductByDIM(String dim) {
        log.info("Register product by DIM: {}", dim);

        SelenideElement product = $x("//label[@data-item-marker='" + dim + "']")
                .shouldBe(Condition.visible, Condition.enabled);

        executeJavaScript("arguments[0].click();", product);

        // Ждём открытия страницы заявки
        $x("//*[contains(@id,'FinApplicationPage')]")
                .shouldBe(Condition.visible, Duration.ofSeconds(15));

        return this;
    }
}