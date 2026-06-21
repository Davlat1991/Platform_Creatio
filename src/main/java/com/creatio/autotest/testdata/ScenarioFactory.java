package com.creatio.autotest.testdata;

import com.creatio.autotest.testdata.models.*;

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
                .participants(List.of(
                        ParticipantData.guarantor(false),
                        ParticipantData.pledger(false)
                ))
                .collaterals(List.of(
                        CollateralDataFactory.vehicle(CurrencyType.TJS),
                        CollateralDataFactory.realEstate(CurrencyType.TJS)
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
                        CollateralDataFactory.realEstate(CurrencyType.TJS)
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
                .participants(List.of(
                        ParticipantData.guarantor(false),
                        ParticipantData.pledger(false)
                ))
                .collaterals(List.of(
                        CollateralDataFactory.vehicle(CurrencyType.TJS),
                        CollateralDataFactory.realEstate(CurrencyType.TJS),
                        CollateralDataFactory.cashDeposit(CurrencyType.TJS),
                        CollateralDataFactory.equipment(CurrencyType.TJS),
                        CollateralDataFactory.gold(CurrencyType.TJS),
                        CollateralDataFactory.goods(CurrencyType.TJS),
                        CollateralDataFactory.movableProperty(CurrencyType.TJS),
                        CollateralDataFactory.acquiredProperty(CurrencyType.TJS),
                        CollateralDataFactory.cotton(CurrencyType.TJS),
                        CollateralDataFactory.futureHarvest(CurrencyType.TJS)
                ))
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
                .incomeExpenses(RegistrationIncomeExpensesData.builder()
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

    // ══════════════════════════════════════
    // PRODUCT DATA
    // ══════════════════════════════════════

    private static ProductData simpleProduct() {
        return ProductData.builder()
                .productType("Карзхои гуногунмаксад")
                .creditPurpose("Барои эхтиёчоти оилави")
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
                .productType("Карзхои гуногунмаксад")
                .creditPurpose("Барои эхтиёчоти оилави")
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
                .productType("Карзхои гуногунмаксад")
                .creditPurpose("Барои эхтиёчоти оилави")
                .amount("100000")
                .term("48")
                .currency("Сомони Чумхурии Точикистон")
                .repaymentDay("3")
                .scheduleNumber("1")
                .scheduleType("Аннуитетный")
                .build();
    }
}