package com.creatio.autotest.components.collateral;

import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BaseComponent;
import io.qameta.allure.Step;

public class RealEstateCharacteristicsComponent extends BaseComponent {

    private final LookupComponent lookup = new LookupComponent();

    @Step("Заполнение характеристик недвижимости")
    public void fillCharacteristics(
            String livingArea,
            String buildYear,
            String rooms,
            String description,
            String propertyType,
            String totalArea) {

        log.info("Fill real estate characteristics");

        lookup
                .selectByLabel("Жилая площадь, кв. метров", livingArea)
                .selectByLabel("Год строительства",          buildYear)
                .selectByLabel("Количество комнат",          rooms)
                .selectByLabel("Описание",                   description)
                .select("Тип недвижимости",                  propertyType)
                .selectByLabel("Общая площадь, кв. метров",  totalArea);
    }
}