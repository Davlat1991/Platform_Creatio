package com.creatio.autotest.flows.registration;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class RegistrationAddressFlow {

    private static final Logger log =
            LoggerFactory.getLogger(RegistrationAddressFlow.class);

    private final ButtonsComponent buttons  = new ButtonsComponent();
    private final LookupComponent  lookup   = new LookupComponent();
    private final BasePage         basePage = new BasePage();
    private final ClickService click = new ClickService();

    @Step("Заполнение адресов")
    public void fillAddresses() {
        log.info("Fill addresses");

        // Регистрация
        buttons.doubleClickByName("Регистрация");

        $x("//*[@data-item-marker='BnzRegistrationDate']//input")
                .shouldBe(Condition.visible)
                .setValue("01.01.2020");

        click.clickByName("Сохранить");
        basePage.waitForPage();

        // Тип клиента
        lookup.select("Тип клиента", "Такрори");

        // Средства связи
        lookup.selectByMarker("BnzAffiliation", "Мобильный");
    }

    @Step("Установка значения 'Связь с ПЗЛ': {expectedValue}")
    public void setPzlRelation(boolean expectedValue) {
        log.info("Set PZL relation: {}", expectedValue);

        SelenideElement pzlBlock = $("[data-item-marker='ПЗЛ']")
                .shouldBe(Condition.visible);

        SelenideElement yesRadio = pzlBlock
                .find("[data-item-marker='BnzPZLTrue']")
                .closest(".radio-button-container")
                .find(".t-radio-wrap");

        SelenideElement noRadio = pzlBlock
                .find("[data-item-marker='BnzPZLFalse']")
                .closest(".radio-button-container")
                .find(".t-radio-wrap");

        boolean isYesSelected = yesRadio.has(Condition.cssClass("t-radio-checked"));

        if (expectedValue && !isYesSelected) {
            yesRadio.click();
            yesRadio.shouldHave(Condition.cssClass("t-radio-checked"));
        } else if (!expectedValue && isYesSelected) {
            noRadio.click();
            noRadio.shouldHave(Condition.cssClass("t-radio-checked"));
        }

        log.info("PZL relation set to: {}", expectedValue ? "Да" : "Нет");
    }
}