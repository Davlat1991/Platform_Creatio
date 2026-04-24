package com.creatio.autotest.components.collateral;

import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.base.BaseComponent;
import com.creatio.autotest.core.services.ClickService;
import io.qameta.allure.Step;

public class CollateralFormComponent extends BaseComponent {

    private final LookupComponent lookup   = new LookupComponent();
    private final BasePage        basePage = new BasePage();
    private final ClickService click    = new ClickService();

    @Step("Тип залога: {value}")
    public void selectType(String value) {
        lookup.select("Тип", value);
    }

    @Step("Подтип залога: {value}")
    public void selectSubType(String value) {
        lookup.select("Подтип", value);
    }

    @Step("Состояние: {value}")
    public void selectCondition(String value) {
        lookup.select("Состояние", value);
    }

    @Step("Право владения: {value}")
    public void selectOwnership(String value) {
        lookup.select("Право владения", value);
    }

    @Step("Залогодатель: {value}")
    public void selectPledger(String value) {
        lookup.select("Залогодатель", value);
    }

    @Step("Название: {value}")
    public void setName(String value) {
        lookup.selectByLabel("Название", value);
    }

    @Step("Сохранить")
    public void save() {
        click.clickByName("Сохранить");
    }

    @Step("Закрыть")
    public void close() {
        click.clickByName("Закрыть");
    }
}