package com.creatio.autotest.testdata.models.registration.fields;

/**
 * Поля детали "Образование и карьера".
 * marker = data-item-marker в DOM Creatio.
 */
public enum EducationCareerField {

    EMPLOYMENT_TYPE("BnzEmploymentStatus"),              // Тип занятости
    REASON_FOR_NOT_WORKING("BnzReasonForNotWorking"),    // Причина отсутствия работы
    POSITION("TsiJob"),                                  // Должность
    ORGANIZATION_NAME("BnzOrganizationName"),            // Наименование организации
    ROLE("BnzRole"),                                     // Роль
    SOCIAL_STATUS("TsiSocialStatus"),                    // Социальный статус
    EDUCATION("TsiEducation"),                           // Образование
    POSITION_FULL_NAME("BnzJobTitle"),                   // Полное название должности
    EMPLOYER("BnzEmployer"),                             // Работодатель
    LEGAL_FORM("BnzAccountOwnership"),                   // Организационно-правовая форма
    BUSINESS_REPUTATION("BnzBusinessReputation");        // Деловая репутация

    public final String marker;

    EducationCareerField(String marker) {
        this.marker = marker;
    }
}