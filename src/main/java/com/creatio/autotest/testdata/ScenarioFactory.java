package com.creatio.autotest.testdata;

import com.creatio.autotest.testdata.models.*;
import com.creatio.autotest.testdata.models.documents.DocumentData;
import com.creatio.autotest.testdata.models.documents.DocumentOwner;
import com.creatio.autotest.testdata.models.documents.DocumentOwnerData;
import com.creatio.autotest.testdata.models.registration.*;
import com.creatio.autotest.testdata.models.registration.fields.EmploymentType;

import java.util.Collections;
import java.util.List;

public final class ScenarioFactory {

    private ScenarioFactory() {}

    // ══════════════════════════════════════
    // СЦЕНАРИЙ 1 — Упрощённый маршрут
    // Сумма 500-30000 сомони
    // Без участников, без залогов
    // ══════════════════════════════════════

    public static TestScenario simplePath() {
        return baseBuilder()
                .product(simpleProduct())
                .route(CreditRoute.SIMPLIFIED)
                .expectedScoringDecision(ScoringDecision.APPROVE)
                .participants(Collections.emptyList())
                .collaterals(Collections.emptyList())
                .build();
    }

    // ══════════════════════════════════════
    // СЦЕНАРИЙ 2 — Happy Path (стандартный)
    // Сумма 30001-100000 сомони
    // Поручитель + залогодатель + залоги
    // ══════════════════════════════════════

    public static TestScenario happyPath() {
        return baseBuilder()
                .product(defaultProduct())
                .route(CreditRoute.STANDARD)
                .expectedScoringDecision(ScoringDecision.APPROVE)
                .participants(List.of(
                        ParticipantData.guarantor(false),
                        ParticipantData.pledger(false)
                ))
                .collaterals(List.of(
                        CollateralDataFactory.vehicle(CurrencyType.TJS)

                ))
                .build();
    }

    // ══════════════════════════════════════
    // СЦЕНАРИЙ 3 — Высокий доход
    // ══════════════════════════════════════

    public static TestScenario highIncomePath() {
        return baseBuilder()
                .registration(highIncomeRegistration())
                .product(highAmountProduct())
                .participants(List.of(
                        ParticipantData.guarantor(false),
                        ParticipantData.pledger(false)
                ))
                .collaterals(List.of(
                        CollateralDataFactory.vehicle(CurrencyType.TJS)
                ))
                .build();
    }

    // ══════════════════════════════════════
    // СЦЕНАРИЙ 4 — Только поручитель
    // ══════════════════════════════════════

    public static TestScenario withGuarantorOnly() {
        return baseBuilder()
                .product(defaultProduct())
                .participants(List.of(
                        ParticipantData.guarantor(false)
                ))
                .collaterals(Collections.emptyList())
                .build();
    }

    // ══════════════════════════════════════
    // СЦЕНАРИЙ 5 — Только залогодатель
    // ══════════════════════════════════════

    public static TestScenario withPledgerOnly() {
        return baseBuilder()
                .product(defaultProduct())
                .participants(List.of(
                        ParticipantData.pledger(false)
                ))
                .collaterals(Collections.emptyList())
                .build();
    }

    // ══════════════════════════════════════
    // СЦЕНАРИЙ 6 — Без участников, с залогами
    // ══════════════════════════════════════

    public static TestScenario withCollateralsOnly() {
        return baseBuilder()
                .product(defaultProduct())
                .participants(Collections.emptyList())
                .collaterals(List.of(
                        CollateralDataFactory.vehicle(CurrencyType.TJS),
                        CollateralDataFactory.cashDeposit(CurrencyType.TJS)
                ))
                .build();
    }


    // ══════════════════════════════════════
    // СЦЕНАРИЙ 7 — Все залоги (регрессия)
    // ══════════════════════════════════════

    public static TestScenario allCollaterals() {
        return baseBuilder()
                .product(defaultProduct())
                .route(CreditRoute.STANDARD)
                .expectedScoringDecision(ScoringDecision.APPROVE)
                .participants(List.of(
                        ParticipantData.guarantor(false),
                        ParticipantData.pledger(false),
                        ParticipantData.pledger1(false)
                ))
                .collaterals(List.of(
                        CollateralDataFactory.vehicle(CurrencyType.TJS),
                        CollateralDataFactory.realEstate(CurrencyType.TJS),
                        CollateralDataFactory.equipment(CurrencyType.TJS),
                        CollateralDataFactory.gold(CurrencyType.TJS),
                        CollateralDataFactory.goods(CurrencyType.TJS),
                        CollateralDataFactory.movableProperty(CurrencyType.TJS),
                        CollateralDataFactory.acquiredProperty(CurrencyType.TJS),
                        CollateralDataFactory.cotton(CurrencyType.TJS),
                        CollateralDataFactory.futureHarvest(CurrencyType.TJS),
                        CollateralDataFactory.cashDeposit(CurrencyType.TJS)
                ))
                .build();
    }

    // ══════════════════════════════════════
    // СЦЕНАРИЙ 8 — Тип клиента: Работает в организации (OTHER_INCOME)
    // Без участников, без залогов — фокус на анкете заёмщика
    // ══════════════════════════════════════

    public static TestScenario otherIncomePath() {
        return baseBuilder()
                .product(defaultProduct())
                .participants(Collections.emptyList())
                .collaterals(Collections.emptyList())
                .build();
    }

    // ══════════════════════════════════════
    // СЦЕНАРИЙ 9 — Тип клиента: Работает в организации (EMPLOYEE)
    // Без участников, без залогов — фокус на анкете заёмщика
    // ══════════════════════════════════════

    public static TestScenario employeePath() {
        return baseBuilder()
                .registration(employeeRegistration())
                .product(defaultProduct())
                .participants(Collections.emptyList())
                .collaterals(Collections.emptyList())
                .build();
    }

    // ══════════════════════════════════════
    // СЦЕНАРИЙ 10 — Тип клиента: Работает в организации (SELF_EMPLOYED)
    // Без участников, без залогов — фокус на анкете заёмщика
    // ══════════════════════════════════════

    public static TestScenario selfEmployedPath() {
        return baseBuilder()
                .registration(selfEmployedRegistration())
                .product(defaultProduct())
                .participants(Collections.emptyList())
                .collaterals(Collections.emptyList())
                .build();
    }

    // ══════════════════════════════════════
    // INTERNAL — BASE BUILDER
    // Общая база для всех сценариев
    // ══════════════════════════════════════

    private static TestScenario.TestScenarioBuilder baseBuilder() {
        return TestScenario.builder()
                .manager(UserPool.manager())
                .underwriter(UserPool.underwriter())
                .ikok(UserPool.ikok())
                .ikokGo(UserPool.ikokGo())
                .cashier(UserPool.cashier())
                .contact(ContactData.defaultContact())
                .registration(defaultRegistration());
    }

    // ══════════════════════════════════════
    // REGISTRATION DATA
    // ══════════════════════════════════════

    private static RegistrationData defaultRegistration() {
        return RegistrationData.builder()
                .region("ш. Хучанд")
                .infoSource("Интернет/Сайт")
                .incomeExpenses(defaultIncomeExpenses())
                .scoringData(BaseScoringScenarios.WITH_DEPENDENTS())
                .clientProfile(otherIncomeProfile())
                .build();
    }

    private static RegistrationData employeeRegistration() {
        return RegistrationData.builder()
                .region("ш. Хучанд")
                .infoSource("Интернет/Сайт")
                .incomeExpenses(defaultIncomeExpenses())
                .scoringData(BaseScoringScenarios.WITH_DEPENDENTS())
                .clientProfile(employeeProfile())
                .build();
    }

    private static RegistrationData selfEmployedRegistration() {
        return RegistrationData.builder()
                .region("ш. Хучанд")
                .infoSource("Интернет/Сайт")
                .incomeExpenses(defaultIncomeExpenses())
                .scoringData(BaseScoringScenarios.WITH_DEPENDENTS())
                .clientProfile(selfEmployedProfile())
                .build();
    }

    private static RegistrationData highIncomeRegistration() {
        return RegistrationData.builder()
                .region("ш. Душанбе")
                .infoSource("Рекомендация")
                .incomeExpenses(RegistrationIncomeExpensesData.builder()
                        .income(IncomeData.builder()
                                .type("Зарплата по доп. месту работы")
                                .frequency("Ежемесячно")
                                .amount("15000")
                                .build())
                        .expense(ExpenseData.builder()
                                .type("Коммунальные платежи")
                                .frequency("Ежемесячно")
                                .amount("500")
                                .build())
                        .build())
                .scoringData(BaseScoringScenarios.STANDARD_EMPLOYED())
                .build();
    }

    // Общий блок доходов/расходов (переиспользуется default и employee)
    private static RegistrationIncomeExpensesData defaultIncomeExpenses() {
        return RegistrationIncomeExpensesData.builder()
                .income(IncomeData.builder()
                        .type("Зарплата по доп. месту работы")
                        .frequency("Ежемесячно")
                        .amount("5000")
                        .build())
                .expense(ExpenseData.builder()
                        .type("Коммунальные платежи")
                        .frequency("Ежемесячно")
                        .amount("1000")
                        .build())
                .build();
    }

    // ══════════════════════════════════════
    // ПРОФИЛИ КЛИЕНТА (тип занятости)
    // ══════════════════════════════════════

    private static ClientProfileData otherIncomeProfile() {
        return ClientProfileData.builder()
                .employmentType(EmploymentType.OTHER_INCOME)
                .educationCareer(EducationCareerData.builder()
                        .reasonForNotWorking("Получатель Д/П")
                        .build())
                .build();
    }

    private static ClientProfileData employeeProfile() {
        return ClientProfileData.builder()
                .employmentType(EmploymentType.EMPLOYEE)
                .educationCareer(EducationCareerData.builder()
                        .position("Ведущий специалист")
                        .organizationName("МТГ \"Мактаби П.Пулотов\"")
                        .role("Исполнитель")
                        .socialStatus("Гослужитель с доп.доходом")
                        .education("Высшее")
                        .positionFullName("Омузгори забони англиси")
                        .businessReputation("Простой рабочий")
                        .build())
                .workAddress(WorkAddressData.builder()
                        .country("Точикистон")
                        .region("Вилояти Сугд")
                        .district("нохияи Б.Гафуров")
                        .city("Б.Гафуров")
                        .streetType("Улица")
                        .street("МТМУ №18")
                        .house("19")
                        .building("2/7")
                        .apartment("48")
                        .index("735700")
                        .regDate("01.01.2020")
                        .build())
                .career(CareerData.builder()
                        .employmentType("Другой учитель")
                        .organizationName("МТГ \"Мактаби П.Пулотов\"")
                        .position("Ведущий специалист")
                        .startDate("15.01.2024")
                        //.employer("МТГ \"Мактаби П.Пулотов\"")
                        .positionFullName("Омузгори забони англиси")
                        .dueDate("15.01.2034")
                        .build())
                .activity(ActivityData.builder()
                        .sector("Работник организации")
                        .segment("Работник в сфере образования")
                        .subSegment("Учитель ср. школы")
                        .salesType("Не торгует")
                        .build())
                .build();
    }

    private static ClientProfileData selfEmployedProfile() {
        return ClientProfileData.builder()
                .employmentType(EmploymentType.SELF_EMPLOYED)
                .educationCareer(EducationCareerData.builder()
                        .position("Агроном")
                        .organizationName("Агропром")
                        .role("Руководитель")
                        .education("2 и более высших")
                        .legalForm("Дехканские (фермерские) хозяйства")
                        .businessReputation("Управленческая должность")
                        .build())
                .workAddress(WorkAddressData.builder()
                        .country("Точикистон")
                        .region("Вилояти Сугд")
                        .district("Хучанд")
                        .city("ш. Хучанд")
                        .streetType("Проспект")
                        .street("Исмоили Сомони")
                        .house("19")
                        .building("2/7")
                        .apartment("48")
                        .index("735700")
                        .regDate("01.01.2020")
                        .build())
                .activity(ActivityData.builder()
                        .sector("Самозанятость")
                        .segment("Агро")
                        .subSegment("Животновод")
                        .salesType("Оптовая")
                        .market("Авангард")
                        .build())
                .build();
    }

    // ══════════════════════════════════════
    // PRODUCT DATA
    // ══════════════════════════════════════

    private static ProductData simpleProduct() {
        return ProductData.builder()
                .productKind("Карзхои гуногунмаксад")
                .creditPurpose("Барои эхтиёчоти оилави") // Барои эхтиёчоти шахси
                .amount("5000")
                .term("12")
                .currency("Сомони Чумхурии Точикистон")
                .repaymentDay("3")
                .scheduleNumber("1")
                .scheduleType("Аннуитетный")
                .build();
    }

    private static ProductData defaultProduct() {
        return ProductData.builder()
                .productKind("Карзхои гуногунмаксад")
                .creditPurpose("Барои эхтиёчоти оилави") // Барои эхтиёчоти шахси
                .amount("50000")
                .term("36")
                .currency("Сомони Чумхурии Точикистон")
                .repaymentDay("3")
                .scheduleNumber("1")
                .scheduleType("Аннуитетный")
                .build();
    }

    private static ProductData highAmountProduct() {
        return ProductData.builder()
                .productKind("Карзхои гуногунмаксад")
                .creditPurpose("Барои эхтиёчоти оилави")
                .amount("100000")
                .term("48")
                .currency("Сомони Чумхурии Точикистон")
                .repaymentDay("3")
                .scheduleNumber("1")
                .scheduleType("Аннуитетный")
                .build();
    }


    // ══════════════════════════════════════
    // DOCUMENTS
    // ══════════════════════════════════════

    // В ScenarioFactory — новый статический метод
    public static List<DocumentOwnerData> documentOwners() {
        return List.of(
                // Поручитель: те же 3 документа, что у заёмщика
                DocumentOwnerData.builder()
                        .owner(DocumentOwner.GUARANTOR)
                        .documents(List.of(
                                DocumentData.builder().type("Income").fileName("Income.pdf").build(),
                                DocumentData.builder().type("INN").fileName("INN.pdf").build(),
                                DocumentData.builder().type("Passport").fileName("Passport.pdf").build()
                        ))
                        .build(),
                // Залогодатель: только FATCA
                DocumentOwnerData.builder()
                        .owner(DocumentOwner.PLEDGER)
                        .documents(List.of(
                                DocumentData.builder().type("FATCA").fileName("FATCA.pdf").build()
                        ))
                        .build()
        );
    }

    // Только поручитель
    public static List<DocumentOwnerData> guarantorDocuments() {
        return List.of(
                DocumentOwnerData.builder()
                        .owner(DocumentOwner.GUARANTOR)
                        .documents(List.of(
                                DocumentData.builder().type("Income").fileName("Income.pdf").build(),
                                DocumentData.builder().type("INN").fileName("INN.pdf").build(),
                                DocumentData.builder().type("Passport").fileName("Passport.pdf").build()
                        ))
                        .build()
        );
    }

    // Только залогодатель
    public static List<DocumentOwnerData> pledgerDocuments() {
        return List.of(
                DocumentOwnerData.builder()
                        .owner(DocumentOwner.PLEDGER)
                        .documents(List.of(
                                DocumentData.builder().type("FATCA").fileName("FATCA.pdf").build()
                        ))
                        .build()
        );
    }




}