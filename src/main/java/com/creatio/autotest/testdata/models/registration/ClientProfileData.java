package com.creatio.autotest.testdata.models.registration;

import com.creatio.autotest.testdata.models.registration.fields.EmploymentType;
import lombok.Builder;
import lombok.Getter;

/**
 * Корневая модель данных профиля клиента (заёмщика) для этапа регистрации.
 *
 * Определяет тип занятости и содержит вложенные данные по блокам анкеты.
 * Используется стратегиями BaseClientFlow для data-driven заполнения —
 * никаких захардкоженных значений в логике флоу.
 *
 * Какие блоки заполняются для каждого типа:
 *  - OTHER_INCOME  → educationCareer (только reasonForNotWorking)
 *  - EMPLOYEE      → educationCareer + workAddress + career + activity
 *  - SELF_EMPLOYED → educationCareer + workAddress + career + activity
 *
 * Неиспользуемые типом блоки остаются null.
 */
@Getter
@Builder
public class ClientProfileData {

    /** Тип занятости — определяет стратегию заполнения анкеты. */
    private EmploymentType employmentType;

    /** Блок "Образование и карьера" (нужен всем типам). */
    private EducationCareerData educationCareer;

    /** Блок "Адрес места работы" (EMPLOYEE / SELF_EMPLOYED). */
    private WorkAddressData workAddress;

    /** Блок "Карьера" (EMPLOYEE / SELF_EMPLOYED). */
    private CareerData career;

    /** Блок "Деятельность физ. лица" (EMPLOYEE / SELF_EMPLOYED). */
    private ActivityData activity;
}