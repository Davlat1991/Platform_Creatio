package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

/**
 * Базовые данные вкладки «Адреса» анкеты заёмщика (регистрация, средства
 * связи, фактический адрес, тип клиента) для сегмента «Микро».
 * Вынесены из флоу, чтобы {@code AgroRegistrationAddressFlow} не содержал
 * хардкода.
 */
@Getter
@Builder
public class AddressBaseData {

    private String registrationDate;   // Дата регистрации
    private String communicationMeans; // Средства связи (BnzAffiliation)
    private String factualDate;        // Дата фактического адреса
    private String clientType;         // Тип клиента (Такрори/Нав)
}