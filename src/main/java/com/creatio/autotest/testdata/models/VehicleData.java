package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

/**
 * Данные объекта обеспечения для VEHICLE — под
 * VehicleCharacteristicsComponent.fillTechnicalData(VehicleData).
 */
@Getter
@Builder
public class VehicleData {
    private String techPassport;
    private String techPassportIssuer;
    private String registrationDate;
    private String registrationNumber;
    private String vin;
    private String brand;
    private String model;
    private String vehicleClass;
    private String manufactureYear;
    private String bodyNumber;
    private String chassisNumber;
    private String color;
    private String bodyType;
    private String engineType;
    private String engineVolume;
    private String enginePower;
    private String loadCapacity;
    private String weight;
    private String description;
}