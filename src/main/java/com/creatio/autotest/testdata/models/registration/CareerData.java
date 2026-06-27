package com.creatio.autotest.testdata.models.registration;

import lombok.Builder;
import lombok.Getter;

/**
 * Данные блока "Карьера".
 * Заполняется для EMPLOYEE и SELF_EMPLOYED (наборы полей различаются).
 */
@Getter
@Builder
public class CareerData {

    private String employmentType;     // Тип занятости
    private String organizationName;   // Наименование организации
    private String position;           // Должность
    private String startDate;          // Дата начала
    private String employer;           // Работодатель
    private String positionFullName;   // Полное название должности
    private String dueDate;            // Дата завершения
}