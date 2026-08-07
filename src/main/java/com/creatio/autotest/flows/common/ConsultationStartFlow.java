package com.creatio.autotest.flows.common;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
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
    // ШАГИ (public — для пошагового вызова)
    // ──────────────────────────────────────
    // Методы публичны, чтобы вызывающий код мог вставить действия между
    // шагами консультации (например, проверку сегмента клиента на анкете,
    // открывающейся сразу после «Начать консультацию» — см. Микро-маршрут).
    // Розничный startConsultation() вызывает их последовательно как есть.
    // ══════════════════════════════════════

    @Step("Нажать 'Начать консультацию'")
    public void clickStartConsultation() {
        log.info("Click 'Начать консультацию'");

        // Берём кнопку в НЕ скрытой строке результатов (grid-row-hidden —
        // это чужие строки при нескольких результатах поиска). Для единственного
        // результата (розница) фильтр ничего не меняет.
        SelenideElement startBtn = $x(
                "//div[contains(@class,'grid-row-actions')" +
                        " and not(contains(@class,'grid-row-hidden'))]" +
                        "//span[@data-item-marker='Начать консультацию']" +
                        " | //*[@data-item-marker='Начать консультацию']" +
                        "[not(ancestor::div[contains(@class,'grid-row-hidden')])]"
        ).shouldBe(Condition.visible, Duration.ofSeconds(60));

        // JS-клик вместо нативного: нативный физически перемещает курсор на
        // кнопку в строке грида и ОСТАВЛЯЕТ его там. После перерисовки в панель
        // консультации под курсором оказывается имя клиента → всплывает
        // hover-мини-карточка контакта → она перекрывает «Оформить заявку»
        // в следующем шаге (click intercepted). JS-клик курсор не двигает.
        executeJavaScript("arguments[0].click();", startBtn);

        basePage.waitForPage();
    }


    @Step("Открыть деталь 'Оформить заявку'")
    public void openApplicationDetail() {
        log.info("Open 'Оформить заявку'");

        // JS-клик вместо обычного: обычный клик Selenide наводит курсор на
        // элемент, а над панелью «Продолжить консультацию» (список отложенных
        // заявок) наведение на любое имя открывает hover-мини-карточку контакта,
        // перекрывающую «Оформить заявку» (click intercepted). JS-клик не двигает
        // курсор (не триггерит hover) и вызывается напрямую на элементе, минуя
        // overlay.
        SelenideElement caption = $x(
                "//span[contains(@id,'consultation-block-caption')" +
                        " and normalize-space()='Оформить заявку']"
        ).shouldBe(Condition.visible, Duration.ofSeconds(20));

        executeJavaScript("arguments[0].click();", caption);

        $x("//div[@data-item-marker='ConsultationThemesContainer']")
                .shouldBe(Condition.exist, Duration.ofSeconds(20));

        log.info("Application detail opened");
    }

    @Step("Выбрать продукт: {dim}")
    public void selectProduct(String dim) {
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