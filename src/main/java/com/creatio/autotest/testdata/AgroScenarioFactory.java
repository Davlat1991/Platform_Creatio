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
                .productType("Агро кредиты")
                .creditPurpose("Барои кишти пахта ва картошка")
                .amount("70000")
                .term("36")
                .currency("Сомони Чумхурии Точикистон")
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

    // ══════════════════════════════════════
    // INTERNAL — BASE BUILDER
    // ══════════════════════════════════════

    private static TestScenario.TestScenarioBuilder baseBuilder() {
        return TestScenario.builder()
                .manager(UserPool.manager())
                .underwriter(UserPool.underwriter())
                .ikok(UserPool.ikok())
                .ikokGo(UserPool.ikokGo())
                .cashier(UserPool.cashier())
                .contact(ContactData.defaultContact())
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
                        .type("Доход от сельского хозяйства")
                        .frequency("Ежемесячно")
                        .amount("4000")
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
                        .position("Фермер")
                        .organizationName("Дехканское хозяйство")
                        .role("Руководитель")
                        .education("Среднее специальное")
                        .legalForm("Дехканские (фермерские) хозяйства")
                        .businessReputation("Управленческая должность")
                        .build())
                .workAddress(WorkAddressData.builder()
                        .country("Точикистон")
                        .region("Вилояти Сугд")
                        .district("нохияи Б.Гафуров")
                        .city("Б.Гафуров")
                        .streetType("Улица")
                        .street("Дехаи Навобод")
                        .house("5")
                        .index("735700")
                        .regDate("01.01.2019")
                        .build())
                .activity(ActivityData.builder()
                        .sector("Самозанятость")
                        .segment("Агро")
                        .subSegment("Растениеводство")
                        .salesType("Оптовая")
                        .market("Местный рынок")
                        .build())
                .build();
    }
}