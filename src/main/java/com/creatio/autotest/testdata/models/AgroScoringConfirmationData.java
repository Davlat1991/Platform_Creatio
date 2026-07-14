package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

/**
 * Данные вкладки «ОЦЕНКА ИНФОРМАЦИИ» (стадия «Оформление», Агро/Микро).
 * <p>
 * В рознице эти значения захардкожены прямо в теле
 * {@code RegistrationScoringConfirmationFlow} ("Более 2-ух квартир" /
 * "Есть автомобиль"), что нарушает правило «данные только через фабрику».
 * Для Агро/Микро значения вынесены в тест-данные и подаются из
 * {@code AgroScenarioFactory.agroScoringConfirmation()}.
 */
@Getter
@Builder
public class AgroScoringConfirmationData {

    /** Поле «Тип собственности на недвижимость» (обязательное). */
    private final String propertyOwnershipType;

    /** Поле «Тип владения автомобилем». */
    private final String vehicleOwnershipType;
}