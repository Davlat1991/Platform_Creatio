package com.creatio.autotest.testdata.models.registration.fields;

/**
 * Поля AddressPageV2.
 * Используются ТОЛЬКО data-item-marker (английские, не локализованные).
 */
public enum AddressField {

    ADDRESS_TYPE("AddressType"),     // Тип адреса
    COUNTRY("Country"),              // Страна
    REGION("Region"),                // Регион
    DISTRICT("City"),                // Район
    CITY("BnzSettlement"),           // Населённый пункт
    STREET_TYPE("TsiStreetType"),    // Тип улицы
    STREET("Street"),                // Улица
    HOUSE("Building1"),              // Дом
    BUILDING("BnzHousing"),          // Корпус
    APARTMENT("AptOffice"),          // Квартира/Офис
    ADDRESS("Address"),              // Адрес
    INDEX("Zip"),                    // Индекс
    REG_DATE("BnzRegistrationDate"); // Дата регистрации

    public final String marker;

    AddressField(String marker) {
        this.marker = marker;
    }
}