package com.creatio.autotest.testdata;

import com.creatio.autotest.testdata.models.*;

public final class ScenarioFactory {

    private ScenarioFactory() {}

    // ══════════════════════════════════════
    // СЦЕНАРИЙ 1 — Happy Path
    // ══════════════════════════════════════

    public static TestScenario happyPath() {
        return TestScenario.builder()
                // Каждый тест получает своего пользователя из пула
                .manager(UserPool.manager())
                .underwriter(UserPool.underwriter())
                .ikok(UserPool.ikok())
                .ikokGo(UserPool.ikokGo())
                .cashier(UserPool.cashier())
                // Данные регистрации
                .contact(ContactData.defaultContact())
                .registration(defaultRegistration())
                .product(defaultProduct())
                .build();
    }

    // ══════════════════════════════════════
    // СЦЕНАРИЙ 2 — другие данные
    // ══════════════════════════════════════

    public static TestScenario highIncomePath() {
        return TestScenario.builder()
                .manager(UserPool.manager())
                .underwriter(UserPool.underwriter())
                .ikok(UserPool.ikok())
                .ikokGo(UserPool.ikokGo())
                .cashier(UserPool.cashier())
                .contact(ContactData.defaultContact())
                .registration(highIncomeRegistration())
                .product(highAmountProduct())
                .build();
    }

    // ══════════════════════════════════════
    // REGISTRATION DATA
    // ══════════════════════════════════════

    private static RegistrationData defaultRegistration() {
        return RegistrationData.builder()
                .region("ш. Хучанд")
                .infoSource("Интернет/Сайт")
                .incomeExpenses(RegistrationIncomeExpensesData.builder()
                        .income(IncomeData.builder()
                                .type("Заработная плата")
                                .frequency("Ежемесячно")
                                .amount("5000")
                                .build())
                        .expense(ExpenseData.builder()
                                .type("Аренда")
                                .frequency("Ежемесячно")
                                .amount("1000")
                                .build())
                        .build())
                .scoringData(BaseScoringScenarios.WITH_DEPENDENTS())
                .build();
    }

    private static RegistrationData highIncomeRegistration() {
        return RegistrationData.builder()
                .region("ш. Душанбе")
                .infoSource("Рекомендация")
                .incomeExpenses(RegistrationIncomeExpensesData.builder()
                        .income(IncomeData.builder()
                                .type("Заработная плата")
                                .frequency("Ежемесячно")
                                .amount("15000")
                                .build())
                        .expense(ExpenseData.builder()
                                .type("Коммунальные услуги")
                                .frequency("Ежемесячно")
                                .amount("500")
                                .build())
                        .build())
                .scoringData(BaseScoringScenarios.STANDARD_EMPLOYED())
                .build();
    }

    // ══════════════════════════════════════
    // PRODUCT DATA
    // ══════════════════════════════════════

    private static ProductData defaultProduct() {
        return ProductData.builder()
                .productType("Карзхои гуногунмаксад")
                .creditPurpose("Барои эхтиёчоти оилави")
                .amount("50000")
                .term("36")
                .currency("Сомони Чумхурии Точикистон")
                // создание заявки
                .repaymentDay("3")
                .scheduleNumber("2")
                .scheduleType("Аннуитетный")
                .build();
    }

    private static ProductData highAmountProduct() {
        return ProductData.builder()
                .productType("Карзхои гуногунмаксад")
                .creditPurpose("Барои эхтиёчоти оилави")
                .amount("100000")
                .term("48")
                .currency("Сомони Чумхурии Точикистон")
                // создание заявки
                .repaymentDay("3")
                .scheduleNumber("2")
                .scheduleType("Аннуитетный")
                .build();
    }
}