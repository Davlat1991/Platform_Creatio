package com.creatio.autotest.testdata;

import com.creatio.autotest.testdata.models.CollateralData;
import com.creatio.autotest.testdata.models.CollateralType;
import com.creatio.autotest.testdata.models.CurrencyType;
import com.creatio.autotest.testdata.models.EquipmentData;
import com.creatio.autotest.testdata.models.GoldData;
import com.creatio.autotest.testdata.models.RealEstateData;
import com.creatio.autotest.testdata.models.SimpleObjectData;

public final class CollateralDataFactory {

    private CollateralDataFactory() {}

    // ══════════════════════════════════════
    // VEHICLE
    // ══════════════════════════════════════

    public static CollateralData vehicle(CurrencyType currency) {
        return CollateralData.builder()
                .type(CollateralType.VEHICLE)
                .liquidationValue("250000")
                .marketValue("300000")
                .currency(currency.getUiValue())
                .build();
    }

    // ══════════════════════════════════════
    // REAL ESTATE
    // ══════════════════════════════════════

    public static CollateralData realEstate(CurrencyType currency) {
        return CollateralData.builder()
                .type(CollateralType.REAL_ESTATE)
                .liquidationValue("100000")
                .marketValue("1100000")
                .currency(currency.getUiValue())
                .specificData(RealEstateData.builder()
                        .livingArea("80")
                        .buildYear("2015")
                        .rooms("3")
                        .description("Квартира в центре")
                        .propertyType("Квартира")
                        .totalArea("100")
                        .build())
                .build();
    }

    // ══════════════════════════════════════
    // EQUIPMENT
    // ══════════════════════════════════════

    public static CollateralData equipment(CurrencyType currency) {
        return CollateralData.builder()
                .type(CollateralType.EQUIPMENT)
                .liquidationValue("450000")
                .marketValue("500000")
                .currency(currency.getUiValue())
                .specificData(EquipmentData.builder()
                        .name("Прицеп")
                        .sum("80000")
                        .country("БЕЛОРУССИЯ")
                        .year("2025")
                        .equipmentType("Промышленное оборудование")
                        .description("Германский прицеп")
                        .build())
                .build();
    }

    // ══════════════════════════════════════
    // CASH DEPOSIT
    // ══════════════════════════════════════

    public static CollateralData cashDeposit(CurrencyType currency) {
        return CollateralData.builder()
                .type(CollateralType.CASH_DEPOSIT)
                .marketValue("1000")
                .currency(currency.getUiValue())
                .build();
    }

    // ══════════════════════════════════════
    // GOLD
    // ══════════════════════════════════════

    public static CollateralData gold(CurrencyType currency) {
        return CollateralData.builder()
                .type(CollateralType.GOLD)
                .currency(currency.getUiValue())
                .specificData(GoldData.builder()
                        .name("Золотое украшение")
                        .purity("999")
                        .weight("50")
                        .weightWithoutStones("40")
                        .amount("50000")
                        .build())
                .build();
    }

    // ══════════════════════════════════════
    // GOODS
    // ══════════════════════════════════════

    public static CollateralData goods(CurrencyType currency) {
        return CollateralData.builder()
                .type(CollateralType.GOODS)
                .currency(currency.getUiValue())
                .specificData(SimpleObjectData.builder()
                        .name("Кухонный набор")
                        .sum("400000")
                        .amount("4")
                        .description("Импортный набор")
                        .build())
                .build();
    }

    // ══════════════════════════════════════
    // MOVABLE PROPERTY
    // ══════════════════════════════════════

    public static CollateralData movableProperty(CurrencyType currency) {
        return CollateralData.builder()
                .type(CollateralType.MOVABLE_PROPERTY)
                .currency(currency.getUiValue())
                .specificData(SimpleObjectData.builder()
                        .name("Производственное оборудование")
                        .sum("70000")
                        .amount("2")
                        .description("Новое оборудование")
                        .build())
                .build();
    }

    // ══════════════════════════════════════
    // ACQUIRED PROPERTY
    // ══════════════════════════════════════

    public static CollateralData acquiredProperty(CurrencyType currency) {
        return CollateralData.builder()
                .type(CollateralType.ACQUIRED_PROPERTY)
                .currency(currency.getUiValue())
                .specificData(SimpleObjectData.builder()
                        .name("Новое оборудование")
                        .sum("200000")
                        .amount("6")
                        .description("Оборудование для тканей")
                        .build())
                .build();
    }

    // ══════════════════════════════════════
    // COTTON
    // ══════════════════════════════════════

    public static CollateralData cotton(CurrencyType currency) {
        return CollateralData.builder()
                .type(CollateralType.COTTON)
                .liquidationValue("555000")
                .marketValue("777000")
                .currency(currency.getUiValue())
                .build();
    }

    // ══════════════════════════════════════
    // FUTURE HARVEST
    // ══════════════════════════════════════

    public static CollateralData futureHarvest(CurrencyType currency) {
        return CollateralData.builder()
                .type(CollateralType.FUTURE_HARVEST)
                .currency(currency.getUiValue())
                .specificData(SimpleObjectData.builder()
                        .name("Хосили картошка")
                        .sum("110000")
                        .amount("4")
                        .description("Ранний сорт картошки")
                        .build())
                .build();
    }

    // ══════════════════════════════════════
    // AGRO — COTTON (сегмент «Микро»)
    // ──────────────────────────────────────
    // Примечание: CottonCharacteristicsComponent.fillTechnicalData() сейчас
    // не принимает параметров и не читает specificData (см. аудит) —
    // это известный техдолг вне рамок подзадачи 3, фиксируется только на
    // память. specificData здесь заложен заранее (SimpleObjectData), чтобы
    // конструктор CollateralData был готов к моменту, когда компонент будет
    // параметризован в Sprint 2.
    // ══════════════════════════════════════

    public static CollateralData agroCotton(CurrencyType currency) {
        return CollateralData.builder()
                .type(CollateralType.COTTON)
                .liquidationValue("450000")
                .marketValue("620000")
                .currency(currency.getUiValue())
                .specificData(SimpleObjectData.builder()
                        .name("Урожай хлопка 2026")
                        .sum("620000")
                        .amount("2500")
                        .description("Хлопок-сырец, поле Б.Гафуров")
                        .build())
                .build();
    }

    // ══════════════════════════════════════
    // AGRO — FUTURE HARVEST (сегмент «Микро»)
    // ══════════════════════════════════════

    public static CollateralData agroFutureHarvest(CurrencyType currency) {
        return CollateralData.builder()
                .type(CollateralType.FUTURE_HARVEST)
                .liquidationValue("90000")
                .marketValue("130000")
                .currency(currency.getUiValue())
                .specificData(SimpleObjectData.builder()
                        .name("Будущий урожай пшеницы")
                        .sum("130000")
                        .amount("3")
                        .description("Озимая пшеница, площадь 1.5 га")
                        .build())
                .build();
    }
}