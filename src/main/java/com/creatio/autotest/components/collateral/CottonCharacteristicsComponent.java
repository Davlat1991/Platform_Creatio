package com.creatio.autotest.components.collateral;

import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BaseComponent;
import com.creatio.autotest.testdata.models.SimpleObjectData;   // ← новый импорт
import io.qameta.allure.Step;

public class CottonCharacteristicsComponent extends BaseComponent {

    private final LookupComponent lookup = new LookupComponent();

    // ── Розничная версия — БЕЗ ИЗМЕНЕНИЙ, её зовёт CottonCollateralFlow ──
    @Step("Характеристики хлопка")
    public void fillTechnicalData() {
        log.info("Fill cotton characteristics");

        lookup
                .selectByLabel("Описание",     "Хороший урожай")
                .selectByLabel("Наименование", "Урожай 2026")
                .selectByLabel("Количество",   "500");
    }

    // ── НОВОЕ: параметризованная версия для Агро/Микро ──
    /**
     * Параметризованная версия для Агро/Микро: читает значения из тест-данных.
     * No-arg перегрузка выше оставлена для розницы без изменений.
     */
    @Step("Характеристики хлопка")
    public void fillTechnicalData(SimpleObjectData data) {
        log.info("Fill cotton characteristics: name='{}' amount='{}'",
                data.getName(), data.getAmount());

        lookup
                .selectByLabel("Описание",     data.getDescription())
                .selectByLabel("Наименование", data.getName())
                .selectByLabel("Количество",   data.getAmount());
    }
}