package com.creatio.autotest.testdata;

import com.creatio.autotest.testdata.models.CollateralData;
import com.creatio.autotest.testdata.models.CollateralType;
import com.creatio.autotest.testdata.models.CurrencyType;
import com.creatio.autotest.testdata.models.RealEstateData;

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
                .build();
    }

    // ══════════════════════════════════════
    // GOODS
    // ══════════════════════════════════════

    public static CollateralData goods(CurrencyType currency) {
        return CollateralData.builder()
                .type(CollateralType.GOODS)
                .currency(currency.getUiValue())
                .build();
    }

    // ══════════════════════════════════════
    // MOVABLE PROPERTY
    // ══════════════════════════════════════

    public static CollateralData movableProperty(CurrencyType currency) {
        return CollateralData.builder()
                .type(CollateralType.MOVABLE_PROPERTY)
                .currency(currency.getUiValue())
                .build();
    }

    // ══════════════════════════════════════
    // ACQUIRED PROPERTY
    // ══════════════════════════════════════

    public static CollateralData acquiredProperty(CurrencyType currency) {
        return CollateralData.builder()
                .type(CollateralType.ACQUIRED_PROPERTY)
                .currency(currency.getUiValue())
                .build();
    }

    // ══════════════════════════════════════
    // COTTON
    // ══════════════════════════════════════

    public static CollateralData cotton(CurrencyType currency) {
        return CollateralData.builder()
                .type(CollateralType.COTTON)
                .liquidationValue("150000")
                .marketValue("200000")
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
                .build();
    }
}