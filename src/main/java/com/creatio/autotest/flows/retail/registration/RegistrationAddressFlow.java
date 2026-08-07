package com.creatio.autotest.flows.retail.registration;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.components.MessageBoxComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.pages.DetailPage;
import com.creatio.autotest.testdata.models.registration.WorkAddressData;
import com.creatio.autotest.testdata.models.registration.fields.AddressField;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

public class RegistrationAddressFlow {

    private static final Logger log =
            LoggerFactory.getLogger(RegistrationAddressFlow.class);

    private final ButtonsComponent buttons  = new ButtonsComponent();
    private final LookupComponent  lookup   = new LookupComponent();
    private final BasePage         basePage = new BasePage();
    private final ClickService click = new ClickService();
    private final DetailPage detailPage = new DetailPage();
    private final MessageBoxComponent msgBox = new MessageBoxComponent();

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

        // Средства связи
        lookup.selectByMarker("BnzAffiliation", "Мобильный");

        // Фактический
        buttons.doubleClickByName("Фактический");

        $x("//*[@data-item-marker='BnzRegistrationDate']//input")
                .shouldBe(Condition.visible)
                .setValue("01.01.2020");

        click.clickByName("Сохранить");
        basePage.waitForPage();

        // Тип клиента
        lookup.select("Тип клиента", "Такрори");

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


    @Step("Заполнение адреса места работы (Работает в организации)")
    public void fillWorkAddressEmployee(WorkAddressData addr) {
        log.info("Fill work address (Employee)");

        // Подтверждение при смене типа занятости (если всплывёт)
        // обрабатывается отдельно при необходимости

        detailPage.clickAddRecord("Адрес места работы");

        // диалог смены типа занятости (может появиться)
        msgBox.confirmIfPresent("Да", "При изменении типа занятости");

        lookup.selectCascadeByMarker(AddressField.COUNTRY.marker,     addr.getCountry());
        lookup.selectCascadeByMarker(AddressField.REGION.marker,      addr.getRegion());
        lookup.selectCascadeByMarker(AddressField.DISTRICT.marker,    addr.getDistrict());
        lookup.selectCascadeByMarker(AddressField.CITY.marker,        addr.getCity());
        lookup.selectByMarkerWithSoftValidation(
                AddressField.STREET_TYPE.marker, addr.getStreetType());
        lookup.setTextByMarker(AddressField.STREET.marker,    addr.getStreet());
        lookup.setTextByMarker(AddressField.HOUSE.marker,     addr.getHouse());
        lookup.setTextByMarker(AddressField.BUILDING.marker,  addr.getBuilding());
        lookup.setTextByMarker(AddressField.APARTMENT.marker, addr.getApartment());
        lookup.setTextByMarker(AddressField.INDEX.marker,     addr.getIndex());
        lookup.setTextByMarker(AddressField.REG_DATE.marker,  addr.getRegDate());

        click.clickByName("Сохранить");
        basePage.waitForPage();
    }

    @Step("Заполнение адреса места работы (Самозанятый)")
    public void fillWorkAddressSelfEmployed(WorkAddressData addr) {
        log.info("Fill work address (SelfEmployed)");

        detailPage.clickAddRecord("Адрес места работы");

        // диалог смены типа занятости (может появиться)
        msgBox.confirmIfPresent("Да", "При изменении типа занятости");

        lookup.selectCascadeByMarker(AddressField.COUNTRY.marker,     addr.getCountry());
        lookup.selectCascadeByMarker(AddressField.REGION.marker,      addr.getRegion());
        lookup.selectCascadeByMarker(AddressField.DISTRICT.marker,    addr.getDistrict());
        lookup.selectCascadeByMarker(AddressField.CITY.marker,        addr.getCity());
        lookup.selectByMarkerWithSoftValidation(
                AddressField.STREET_TYPE.marker, addr.getStreetType());
        lookup.setTextByMarker(AddressField.STREET.marker,    addr.getStreet());
        lookup.setTextByMarker(AddressField.HOUSE.marker,     addr.getHouse());
        lookup.setTextByMarker(AddressField.BUILDING.marker,  addr.getBuilding());
        lookup.setTextByMarker(AddressField.APARTMENT.marker, addr.getApartment());
        lookup.setTextByMarker(AddressField.INDEX.marker,     addr.getIndex());
        lookup.setTextByMarker(AddressField.REG_DATE.marker,  addr.getRegDate());

        click.clickByName("Сохранить");
        basePage.waitForPage();
    }
}