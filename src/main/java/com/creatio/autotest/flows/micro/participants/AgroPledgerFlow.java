package com.creatio.autotest.flows.micro.participants;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.testdata.models.AgroPledgerData;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;

/**
 * Анкета залогодателя (Агро/Микро) — короткая: средство связи → регистрация →
 * вид связи → национальность → доп. информация (семейное положение, уровень
 * риска) → Сохранить/Закрыть.
 * <p>
 * Изолированная Микро-копия розничного {@code PledgerFlow}: шаги идентичны, но
 * значения приходят из {@link AgroPledgerData} (без хардкода в теле), дата
 * вводится через {@code sendKeys + Keys.TAB} (setValue не триггерит onChange).
 */
public class AgroPledgerFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroPledgerFlow.class);

    private final LookupComponent  lookup   = new LookupComponent();
    private final ButtonsComponent buttons  = new ButtonsComponent();
    private final BasePage         basePage = new BasePage();
    private final ClickService     click    = new ClickService();

    // ══════════════════════════════════════
    // PUBLIC API
    // ══════════════════════════════════════

    @Step("Агро: заполнение анкеты залогодателя")
    public void fill(AgroPledgerData data) {
        log.info("Agro: fill pledger questionnaire");

        waitForQuestionnaireCard();

        // Средство связи
        lookup.selectByMarker("BnzAffiliation", data.getCommunicationMeans());

        // Регистрация
        buttons.doubleClickByName("Регистрация");
        setDateField("BnzRegistrationDate", data.getRegistrationDate());
        click.clickByName("Сохранить");
        basePage.waitForPage();

        // Вид связи / национальность
        lookup.select("Вид связи",     data.getRelationType());
        lookup.select("Национальность", data.getNationality());

        // Дополнительная информация
        buttons.clickTabByName("ДОПОЛНИТЕЛЬНАЯ ИНФОРМАЦИЯ");
        lookup.select("Семейное положение", data.getMaritalStatus());
        lookup.select("Уровень риска",      data.getRiskLevel());

        click.clickByName("Сохранить");
        click.clickByName("Закрыть");

        log.info("Agro: pledger questionnaire filled");
    }

    // ══════════════════════════════════════
    // PRIVATE
    // ══════════════════════════════════════

    /**
     * Анкета участника открывается после «Выбрать» асинхронно: lookup имеет
     * собственный таймаут 10 с и падает раньше, чем карточка отрисуется.
     * Ждём первое поле анкеты.
     */
    @Step("Ожидание готовности анкеты залогодателя")
    private void waitForQuestionnaireCard() {
        log.info("Wait for pledger questionnaire card");

        $x("//*[@data-item-marker='BnzAffiliation']//input")
                .shouldBe(Condition.visible, Duration.ofSeconds(60));

        log.info("Pledger questionnaire card ready");
    }

    /** Дата через sendKeys + TAB (setValue не триггерит onChange/onBlur в Creatio). */
    private void setDateField(String marker, String value) {
        log.debug("setDateField: marker='{}' value='{}'", marker, value);

        SelenideElement input = $x("//*[@data-item-marker='" + marker + "']//input")
                .shouldBe(Condition.visible, Duration.ofSeconds(10));

        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        input.sendKeys(value);
        input.sendKeys(Keys.TAB);
    }
}