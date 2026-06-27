package com.creatio.autotest.testdata.models.registration;

import lombok.Builder;
import lombok.Getter;

/**
 * Данные блока "Адрес места работы".
 * Заполняется для EMPLOYEE и SELF_EMPLOYED.
 */
@Getter
@Builder
public class WorkAddressData {

    private String country;       // Страна
    private String region;        // Регион
    private String district;      // Район
    private String city;          // Населённый пункт
    private String streetType;    // Тип улицы
    private String street;        // Улица
    private String house;         // Дом
    private String building;      // Корпус
    private String apartment;     // Квартира/Офис
    private String index;         // Индекс
    private String regDate;       // Дата регистрации
}