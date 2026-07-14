package com.creatio.autotest.flows.micro.registration;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.components.MessageBoxComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.pages.DetailPage;
import com.creatio.autotest.testdata.models.AddressBaseData;
import com.creatio.autotest.testdata.models.registration.WorkAddressData;
import com.creatio.autotest.testdata.models.registration.fields.AddressField;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

/**
 * Флоу «Адреса» стадии «Оформление» (анкета заёмщика) для продуктов сегмента
 * «Микро».
 * <p>
 * Самостоятельный класс в ветке {@code flows.micro.registration}, изолированный
 * от розничного {@code RegistrationAddressFlow}. Логика идентична рознице
 * (подтверждено), но: данные вынесены в модели (без хардкода), а ввод дат
 * использует {@code sendKeys + Keys.TAB} вместо {@code setValue()} — по
 * правилу проекта (setValue не триггерит onChange/onBlur в Creatio).
 */
public class AgroRegistrationAddressFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroRegistrationAddressFlow.class);

    private final ButtonsComponent    buttons  = new ButtonsComponent();
    private final LookupComponent     lookup   = new LookupComponent();
    private final BasePage            basePage = new BasePage();
    private final ClickService        click    = new ClickService();
    private final DetailPage          detailPage = new DetailPage();
    private final MessageBoxComponent msgBox   = new MessageBoxComponent();

    @Step("Заполнение адресов (Агро/Микро)")
    public void fillAddresses(AddressBaseData data) {
        log.info("Fill addresses (Agro/Micro)");

        // Регистрация
        buttons.doubleClickByName("Регистрация");
        setDate(data.getRegistrationDate());
        click.clickByName("Сохранить");
        basePage.waitForPage();

        // Средства связи
        lookup.selectByMarker("BnzAffiliation", data.getCommunicationMeans());

        // Фактический
      /*  buttons.doubleClickByName("Фактический");
        setDate(data.getFactualDate());
        click.clickByName("Сохранить");
        basePage.waitForPage(); */

        // Тип клиента
        lookup.select("Тип клиента", data.getClientType());
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

    @Step("Заполнение адреса места работы (Самозанятый) — Агро/Микро")
    public void fillWorkAddressSelfEmployed(WorkAddressData addr) {
        log.info("Fill work address (SelfEmployed) Agro/Micro");

        detailPage.clickAddRecord("Адрес места работы");

        // диалог смены типа занятости (может появиться)
        msgBox.confirmIfPresent("Да", "При изменении типа занятости");

        lookup.selectCascadeByMarker(AddressField.COUNTRY.marker,     addr.getCountry());
        lookup.selectCascadeByMarker(AddressField.REGION.marker,      addr.getRegion());
        lookup.selectCascadeByMarker(AddressField.DISTRICT.marker,    addr.getDistrict());
        lookup.selectCascadeByMarker(AddressField.CITY.marker,        addr.getCity());
        lookup.selectByMarkerWithSoftValidation(
                AddressField.STREET_TYPE.marker, addr.getStreetType());

        // Текстовые поля адреса — пропускаем пустые (корпус/квартира и др.
        // необязательны; ввод null/blank ломал бы setTextByMarker).
        setTextIfPresent(AddressField.STREET.marker,    addr.getStreet());
        setTextIfPresent(AddressField.HOUSE.marker,     addr.getHouse());
        setTextIfPresent(AddressField.BUILDING.marker,  addr.getBuilding());
        setTextIfPresent(AddressField.APARTMENT.marker, addr.getApartment());
        setTextIfPresent(AddressField.INDEX.marker,     addr.getIndex());
        setTextIfPresent(AddressField.REG_DATE.marker,  addr.getRegDate());

        click.clickByName("Сохранить");
        basePage.waitForPage();
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

    /**
     * Ввод текста в поле по marker только если значение непустое.
     * Необязательные поля адреса (корпус, квартира) часто отсутствуют —
     * ввод null/blank ломал бы setTextByMarker.
     */
    private void setTextIfPresent(String marker, String value) {
        if (value == null || value.isBlank()) {
            log.debug("Skip empty address field: marker='{}'", marker);
            return;
        }
        lookup.setTextByMarker(marker, value);
    }

    /**
     * Ввод даты в поле BnzRegistrationDate через sendKeys + TAB
     * (setValue не триггерит onChange/onBlur в Creatio).
     */
    private void setDate(String value) {
        SelenideElement input = $x("//*[@data-item-marker='BnzRegistrationDate']//input")
                .shouldBe(Condition.visible);

        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        input.sendKeys(value);
        input.sendKeys(Keys.TAB);
    }
}