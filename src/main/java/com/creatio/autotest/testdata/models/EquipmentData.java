package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

/**
 * Данные объекта обеспечения для EQUIPMENT — под
 * CollateralValuationComponent.fillEquipment(name, sum, currency, country, year, equipmentType, description).
 */
@Getter
@Builder
public class EquipmentData {
    private String name;
    private String sum;
    private String country;
    private String year;
    private String equipmentType;
    private String description;
}