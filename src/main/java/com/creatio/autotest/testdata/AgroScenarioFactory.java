package com.creatio.autotest.testdata;

import com.creatio.autotest.testdata.models.*;
import com.creatio.autotest.testdata.models.registration.ActivityData;
import com.creatio.autotest.testdata.models.registration.ClientProfileData;
import com.creatio.autotest.testdata.models.registration.EducationCareerData;
import com.creatio.autotest.testdata.models.registration.WorkAddressData;
import com.creatio.autotest.testdata.models.registration.fields.EmploymentType;

import java.util.Collections;

/**
 * Фабрика тест-данных для сценария «Агро» (сегмент «Микро»).
 * Отдельная от {@link ScenarioFactory}, т.к. продукт и профиль клиента
 * имеют специфичные для Агро значения (вид деятельности, продукт, залоги).
 * <p>
 * Sprint 1 / Подзадача 1: только тест-данные подбора продукта и базовый
 * happy-path сценарий без участников и залогов (они добавятся в Sprint 2
 * подзадачах 2-3 и последующих флоу).
 */
public final class AgroScenarioFactory {

    private AgroScenarioFactory() {}

    // ══════════════════════════════════════
    // СЦЕНАРИЙ — Агро / Микро, Happy Path
    // ──────────────────────────────────────
    // Примечание: AgroProductData НЕ хранится в TestScenario.product
    // (тот типизирован под базовый ProductData). Для флоу подбора продукта
    // Агро (AgroProductSelectionFlow, Sprint 1 / подзадача 4) AgroProductData
    // передаётся отдельным параметром, полученным через agroMicroProduct().
    // ══════════════════════════════════════

    public static TestScenario agroMicroHappyPath() {
        return baseBuilder()
                .route(CreditRoute.SIMPLIFIED)
                .expectedScoringDecision(ScoringDecision.APPROVE)
                .participants(Collections.emptyList())
                .collaterals(Collections.emptyList())
                .build();
    }

    // ══════════════════════════════════════
    // AGRO PRODUCT DATA
    // ══════════════════════════════════════

    public static AgroProductData agroMicroProduct() {
        return AgroProductData.builder()
                .productType("Агро кредиты")   // Тип продукта
                .productKind("Агро Животноводство")   // Вид продукта
                .creditPurpose("Харидани чорво")
                .amount("70000")
                .term("36")
                .currency("Сомони Чумхурии Точикистон")
                // Имя строки результата подбора (частичное совпадение).
                // "нав сегменти микро" = продукт для нового клиента сегмента Микро.
                .productName("барои мизочони нав сегменти микро")
                .repaymentDay("3")
                .scheduleNumber("1")
                .scheduleType("Аннуитетный")
                .build();
    }

    // ══════════════════════════════════════
    // AGRO SCORING DATA
    // ──────────────────────────────────────
    // Примечание: как и AgroProductData, НЕ хранится в RegistrationData
    // (тот типизирован под BaseScoringData). Передаётся отдельным параметром
    // во флоу AgroRegistrationGeneralInfoFlow / AgroRegistrationAdditionalInfoFlow
    // (Sprint 1, подзадачи 5-6).
    // ══════════════════════════════════════

    public static AgroScoringData agroMicroScoring() {
        return AgroScoringScenarios.MICRO_CROP_FARMING();
    }

    /**
     * Значение обязательного поля «Сектор экономики» в общей информации Агро.
     * Передаётся отдельно (не через RegistrationData), чтобы не тащить
     * Агро-специфику в розничную модель.
     */
    public static String agroEconomicSector() {
        return "Животноводство";
    }

    /**
     * Данные документов заёмщика для Агро/Микро (значения совпадают с
     * розничными, но заданы явно в тест-данных, а не в теле флоу).
     */
    public static BorrowerDocumentData agroBorrowerDocuments() {
        return BorrowerDocumentData.builder()
                .birthCountry("Точикистон")
                .issueDate("01.01.2020")
                .validUntil("01.01.2030")
                .documentType("Шиносномаи ЧТ")
                .build();
    }

    /**
     * Базовые данные вкладки «Адреса» (значения совпадают с розничными,
     * заданы явно в тест-данных).
     */
    public static AddressBaseData agroAddressBase() {
        return AddressBaseData.builder()
                .registrationDate("01.01.2020")
                .communicationMeans("Мобильный")
                .factualDate("01.01.2020")
                .clientType("Нав")
                .build();
    }

    /**
     * Доп.данные вкладки «Доходы и расходы» (уровень риска, подтверждение).
     */
    public static IncomeExtraData agroIncomeExtra() {
        return IncomeExtraData.builder()
                .riskLevel("Низкий")
                .incomeConfirmation("Да")
                .confirmationDocType("Выписка с банковского счета")
                .build();
    }

    // ══════════════════════════════════════
    // INTERNAL — BASE BUILDER
    // ══════════════════════════════════════

    private static TestScenario.TestScenarioBuilder baseBuilder() {
        return TestScenario.builder()
                .manager(UserPool.manager())
                .creditExpert(UserPool.creditExpert())
                .underwriter(UserPool.underwriter())
                .ikok(UserPool.ikok())
                .ikokGo(UserPool.ikokGo())
                .cashier(UserPool.cashier())
                .contact(ContactData.microContact())
                .registration(agroMicroRegistration());
    }

    // ══════════════════════════════════════
    // REGISTRATION DATA — Агро / Микро
    // ══════════════════════════════════════

    private static RegistrationData agroMicroRegistration() {
        return RegistrationData.builder()
                .region("ш. Хучанд")
                .infoSource("Интернет/Сайт")
                .incomeExpenses(agroIncomeExpenses())
                .scoringData(BaseScoringScenarios.WITH_DEPENDENTS())
                .clientProfile(agroMicroProfile())
                .build();
    }

    private static RegistrationIncomeExpensesData agroIncomeExpenses() {
        return RegistrationIncomeExpensesData.builder()
                .income(IncomeData.builder()
                        .type("Предпринимательство")
                        .frequency("Ежемесячно")
                        .amount("20000")
                        .build())
                .expense(ExpenseData.builder()
                        .type("Коммунальные платежи")
                        .frequency("Ежемесячно")
                        .amount("800")
                        .build())
                .build();
    }

    // ══════════════════════════════════════
    // ПРОФИЛЬ КЛИЕНТА — Агро / Микро (SELF_EMPLOYED)
    // ══════════════════════════════════════

    private static ClientProfileData agroMicroProfile() {
        return ClientProfileData.builder()
                .employmentType(EmploymentType.SELF_EMPLOYED)
                .educationCareer(EducationCareerData.builder()
                        .position("Агроном")
                        .organizationName("Дехканское хозяйство")
                        .role("Руководитель")
                        .education("Высшее")
                        .legalForm("Дехканские (фермерские) хозяйства")
                        .businessReputation("Управленческая должность")
                        .build())
                .workAddress(WorkAddressData.builder()
                        .country("Точикистон")
                        .region("Вилояти Сугд")
                        .district("Хучанд")
                        .city("ш. Хучанд")
                        .streetType("Улица")
                        .street("Чашма, куаи Пушкин")
                        .house("106")
                        .index("735700")
                        .regDate("01.01.2019")
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
}