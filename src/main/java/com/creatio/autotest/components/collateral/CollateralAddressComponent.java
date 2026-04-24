package com.creatio.autotest.components.collateral;

import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.base.BaseComponent;
import com.creatio.autotest.core.services.ClickService;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class CollateralAddressComponent extends BaseComponent {

    private final LookupComponent lookup   = new LookupComponent();
    private final BasePage        basePage = new BasePage();
    private final ClickService click    = new ClickService();

    @Step("Заполнение адреса имущества")
    public void fillPropertyAddress() {
        log.info("Fill property address");

        click.clickById(
                "EshPropertyDataAddressDetailAddRecordButtonButton-imageEl"
        );

        lookup
                .select("Тип адреса",        "Регистрация")
                .select("Страна",            "Точикистон")
                .selectAddress("Регион",     "Вилояти Сугд")
                .selectAddress("Район",      "Хучанд")
                .selectAddress("Нас. пункт", "ш. Хучанд")
                .select("Тип улицы",         "Проспект")
                .selectByLabel("Улица",      "Исмоили Сомони")
                .selectByLabel("Дом",        "19")
                .selectByLabel("Корпус",     "2/7")
                .selectByLabel("Кв/Офис",    "48")
                .selectByLabel("Дата рег.",  "01.01.2020");

        click.clickByName("Сохранить");
    }
}