package com.creatio.autotest.components.collateral;

import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BaseComponent;
import io.qameta.allure.Step;

public class CottonCharacteristicsComponent extends BaseComponent {

    private final LookupComponent lookup = new LookupComponent();

    @Step("Характеристики хлопка")
    public void fillTechnicalData() {
        log.info("Fill cotton characteristics");

        lookup
                .selectByLabel("Описание",   "Хороший урожай")
                .selectByLabel("Наименование", "Урожай 2026")
                .selectByLabel("Количество", "500");
    }
}