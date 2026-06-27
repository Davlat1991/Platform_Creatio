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
 * Вложенные блоки добавляются по мере реализации типов клиента:
 *  - educationCareer — блок "Образование и карьера" (нужен всем типам)
 *  - workAddress / career / activity — добавятся для EMPLOYEE и SELF_EMPLOYED
 */
@Getter
@Builder
public class ClientProfileData {

    /** Тип занятости — определяет стратегию заполнения анкеты. */
    private EmploymentType employmentType;

    /** Блок "Образование и карьера". */
    private EducationCareerData educationCareer;
}