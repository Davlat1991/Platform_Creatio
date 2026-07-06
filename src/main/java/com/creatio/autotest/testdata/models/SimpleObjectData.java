package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

/**
 * Данные объекта обеспечения для типов залога, использующих
 * CollateralValuationComponent.fillObjects(name, sum, currency, amount, description):
 * GOODS, MOVABLE_PROPERTY, ACQUIRED_PROPERTY, FUTURE_HARVEST.
 */
@Getter
@Builder
public class SimpleObjectData {
    private String name;
    private String sum;
    private String amount;
    private String description;
}