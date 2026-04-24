package com.creatio.autotest.components.collateral;

import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BaseComponent;
import io.qameta.allure.Step;

public class GoldCharacteristicsComponent extends BaseComponent {

    private final LookupComponent lookup = new LookupComponent();

    @Step("Заполнение характеристик золота: вес={weight} проба={purity}")
    public void fill(String weight, String purity, String description) {
        log.info("Fill gold characteristics");

        lookup
                .selectByLabel("Вес, грамм", weight)
                .selectByLabel("Проба",      purity)
                .selectByLabel("Описание",   description);
    }
}