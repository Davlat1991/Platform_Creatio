package com.creatio.autotest.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.WaitService;
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
    private static final String COMPLETE_PANEL_BUTTON = "//*[@data-item-marker='CompleteConsultationButton']";
    private static final String COMPLETE_MODAL_BUTTON = "//*[@data-item-marker='CompleteButton']";

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

    @Step("Завершить консультацию: 'Завершить' в панели + подтверждение в модалке")
    public void completeConsultation() {
        log.info("Complete consultation via panel");

        // 1. Кнопка 'Завершить' в консультационной панели.
        // JS-клик — в старом проекте нативный клик по этой кнопке не срабатывал.
        SelenideElement panelBtn = $x(COMPLETE_PANEL_BUTTON)
                .shouldBe(Condition.visible, Duration.ofSeconds(20));
        executeJavaScript("arguments[0].click();", panelBtn);
        log.info("Clicked 'Завершить' in consultation panel");

        // 2. Кнопка 'Завершить' в модальном окне подтверждения.
        SelenideElement modalBtn = $x(COMPLETE_MODAL_BUTTON)
                .shouldBe(Condition.visible, Duration.ofSeconds(20));
        executeJavaScript("arguments[0].click();", modalBtn);
        log.info("Clicked 'Завершить' in modal — consultation completed");
    }


    // ══════════════════════════════════════
    // REGISTER PRODUCT
    // ══════════════════════════════════════

    @Step("Close consultation panel after page reload")
    public ConsultationPanelPage closeAfterReload() {
        new WaitService().waitForLoader();
        try {
            $x(RIGHT_PANEL).shouldBe(Condition.visible, Duration.ofSeconds(20));
        } catch (Exception e) {
            log.info("Panel did not reappear after reload — nothing to close");
            return this;
        }
        return closeIfOpened();
    }


}