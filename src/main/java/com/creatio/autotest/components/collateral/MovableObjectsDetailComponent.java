package com.creatio.autotest.components.collateral;

import com.creatio.autotest.components.DomActions;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.base.BaseComponent;
import com.creatio.autotest.core.services.ClickService;
import io.qameta.allure.Step;

public class MovableObjectsDetailComponent extends BaseComponent {

    private final DomActions      dom      = new DomActions();
    private final LookupComponent lookup   = new LookupComponent();
    private final BasePage        basePage = new BasePage();
    private final ClickService click    = new ClickService();

    @Step("Добавление объекта обеспечения: {name}")
    public void addObject(String name, String amount, String currency,
                          String quantity, String description) {
        log.info("Add movable object: name='{}'", name);

        click.clickById(
                "EshPropertyDataObjectsDetailAddRecordButtonButton-imageEl"
        );

        dom.setGridInputValue(
                "EshPropertyDataObjectsDetailNameTextEdit-el", name
        );
        dom.setGridInputValue(
                "EshPropertyDataObjectsDetailAmountFloatEdit-el", amount
        );

        lookup.select("Валюта", currency);

        dom.setGridInputValue(
                "EshPropertyDataObjectsDetailCountIntegerEdit-el", quantity
        );
        dom.setGridInputValue(
                "EshPropertyDataObjectsDetailDescriptionTextEdit-el", description
        );

        click.clickByMarker("save");
    }
}