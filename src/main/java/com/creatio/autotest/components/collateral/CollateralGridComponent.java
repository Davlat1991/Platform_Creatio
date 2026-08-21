package com.creatio.autotest.components.collateral;

import com.codeborne.selenide.Condition;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.base.BaseComponent;
import com.creatio.autotest.core.services.ClickService;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$;

public class CollateralGridComponent extends BaseComponent {

    private final BasePage basePage = new BasePage();
    private final ClickService click    = new ClickService();

    @Step("Добавить запись залога")
    public void addCollateral() {
        log.info("Add collateral record");
        click.clickById(
                "FinAppPledgeDetailAddRecordButtonButton-imageEl"
        );
        // Новый поток: после «+» сразу открывается «Новая запись», промежуточный
        // выбор PropertyData больше не нужен. Строка сохранена на случай отката.
        // click.clickByTagAndMarker("div", "PropertyData");

        // Форма грузится асинхронно — ждём флаг готовности записи (EntityLoaded)
        // перед любым заполнением полей, иначе клики уходят в неготовую форму (гонка).
        waitForPageLoaded();
    }

    @Step("Открыть лупу выбора имущества")
    public void openPropertyLookup() {
        log.info("Open property lookup");
        basePage.clickLookupIcon(
                "FinAppPledgeDetailPropertyDataLookupEdit-el"
        );
    }

    @Step("Добавить залоговую ценность")
    public void addCollateralValue() {
        log.info("Add collateral value");
        click.clickById(
                "EshCollateralValuesDetailAddRecordButtonButton-imageEl"
        );
    }

    @Step("Wait for page loaded")
    public void waitForPageLoaded() {
        $("[data-item-marker='EntityLoaded']")
                .shouldBe(Condition.visible, java.time.Duration.ofSeconds(30));
    }
}