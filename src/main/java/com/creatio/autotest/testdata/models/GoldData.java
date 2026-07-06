package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

/**
 * Данные объекта обеспечения для GOLD — под
 * CollateralValuationComponent.fillGold(name, purity, weight, weightWithoutStones, amount, currency).
 */
@Getter
@Builder
public class GoldData {
    private String name;
    private String purity;
    private String weight;
    private String weightWithoutStones;
    private String amount;
}