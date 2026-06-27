package com.creatio.autotest.testdata.models.registration;

import lombok.Builder;
import lombok.Getter;

/**
 * Данные блока "Образование и карьера" анкеты заёмщика.
 *
 * Заполняется по-разному в зависимости от типа занятости:
 *  - OTHER_INCOME  → только reasonForNotWorking
 *  - EMPLOYEE      → position, organizationName, role, socialStatus, education,
 *                    positionFullName, businessReputation
 *  - SELF_EMPLOYED → position, organizationName, role, education, legalForm,
 *                    businessReputation
 *
 * Все поля необязательные: конкретный тип клиента заполняет только свои.
 */
@Getter
@Builder
public class EducationCareerData {

    private String reasonForNotWorking;   // Причина отсутствия работы
    private String position;              // Должность
    private String organizationName;      // Наименование организации
    private String role;                  // Роль
    private String socialStatus;          // Социальный статус
    private String education;             // Образование
    private String positionFullName;      // Полное название должности
    private String employer;              // Работодатель
    private String legalForm;             // Организационно-правовая форма
    private String businessReputation;    // Деловая репутация
}