package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

/**
 * Данные анкеты поручителя (Агро/Микро).
 * <p>
 * В рознице все эти значения захардкожены в теле {@code GuarantorFlow};
 * для Агро вынесены в модель и подаются из
 * {@code AgroScenarioFactory.agroGuarantorData()} (правило «данные только
 * через фабрику»). Доход/расход переиспользуют нейтральные {@link IncomeData}
 * / {@link ExpenseData}.
 * <p>
 * ВНИМАНИЕ: значения посеяны розничными и подлежат проверке по live DOM
 * (справочники Агро могут отличаться — как было с «Дом»/«Предпринимательство»).
 */
@Getter
@Builder
public class AgroGuarantorData {

    // ── Документы ──
    private final String communicationMeans;   // Средство связи (BnzAffiliation)
    private final String birthCountry;         // Страна рождения
    private final String docIssueDate;         // Дата выдачи (первый документ)
    private final String docValidUntil;        // Действителен до (первый документ)
    private final String passportIssueDate;    // Дата выдачи (паспорт «Шиносномаи ЧТ»)
    private final String passportValidUntil;   // Действует до (паспорт)

    // ── Адреса ──
    private final String registrationDate;     // Дата регистрации (BnzRegistrationDate)
    private final String clientType;           // Тип клиента
    private final String relationType;         // Вид связи

    // ── Доход/расход + доп. информация ──
    private final String riskLevel;                    // Уровень риска
    private final String incomeConfirmationProvided;   // Клиент предоставил подтверждение дохода
    private final String incomeConfirmationDocType;    // Тип документа подтверждения дохода
    private final IncomeData  income;
    private final ExpenseData expense;

    // ── Скоринговые данные ──
    private final String maritalStatus;        // Семейное положение
    private final String dependents;           // Количество иждивенцев
    private final String children;             // Количество детей
    private final String familyMembers;        // Количество членов семьи
    private final String totalExperience;      // Общий стаж
    private final String experienceYears;      // Общий стаж, лет
    private final String experienceMonths;     // Общий стаж, мес
    private final String lastPlaceYears;       // Стаж на последнем месте, лет
    private final String lastPlaceMonths;      // Стаж на последнем месте, мес

    // ── Причина отсутствия работы ──
    private final String reasonForNotWorking;

    // ── Оценка информации ──
    private final String propertyOwnershipType;  // Тип собственности на недвижимость
    private final String vehicleOwnershipType;   // Тип владения автомобилем
}