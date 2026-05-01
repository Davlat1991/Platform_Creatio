package com.creatio.autotest.flows;

import com.codeborne.selenide.Condition;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.pages.ConsultationPanelPage;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class ConsultationStartFlow {

    private static final Logger log =
            LoggerFactory.getLogger(ConsultationStartFlow.class);

    private final BasePage              basePage     = new BasePage();
    private final ConsultationPanelPage consultation = new ConsultationPanelPage();

    @Step("Старт консультации: тема={consultationThemeDim}")
    public void startConsultation(String consultationThemeDim) {
        log.info("Start consultation: dim={}", consultationThemeDim);

        clickStartConsultation();
        openApplicationDetail();
        selectProduct(consultationThemeDim);

        basePage.waitForPage();
        consultation.closeIfOpened();

        log.info("Consultation started successfully");
    }

    // ══════════════════════════════════════
    // PRIVATE
    // ══════════════════════════════════════

    @Step("Нажать 'Начать консультацию'")
    private void clickStartConsultation() {
        log.info("Click 'Начать консультацию'");

        $x("//*[@data-item-marker='Начать консультацию']" +
                " | //span[normalize-space()='Начать консультацию']")
                .shouldBe(Condition.visible, Duration.ofSeconds(20))
                .click();

        basePage.waitForPage();
    }

    @Step("Открыть деталь 'Оформить заявку'")
    private void openApplicationDetail() {
        log.info("Open 'Оформить заявку'");

        $x("//span[contains(@id,'consultation-block-caption')" +
                " and normalize-space()='Оформить заявку']")
                .shouldBe(Condition.visible, Duration.ofSeconds(20))
                .click();

        $x("//div[@data-item-marker='ConsultationThemesContainer']")
                .shouldBe(Condition.exist, Duration.ofSeconds(20));

        log.info("Application detail opened");
    }

    @Step("Выбрать продукт: {dim}")
    private void selectProduct(String dim) {
        log.info("Select product by DIM: {}", dim);

        $x("//div[@data-item-marker='" + dim + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30));

        executeJavaScript(
                "arguments[0].click();",
                $x("//div[@data-item-marker='" + dim + "']")
        );

        // Ждём кнопку "Подобрать" на странице подбора продукта.
        // Из DOM: span.t-btn-wrapper[data-item-marker='SelectionButton'] с текстом 'Подобрать'
        // НЕ используем SearchButton — он принадлежит правой панели поиска клиентов
        $x("//span[@data-item-marker='SelectionButton']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30));

        log.info("Product selection page opened");
    }
}