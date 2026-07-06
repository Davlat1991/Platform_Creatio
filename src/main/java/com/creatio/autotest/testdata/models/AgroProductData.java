package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

/**
 * Тест-данные подбора продукта «Агро» (сегмент «Микро»).
 * Отдельная модель от {@link ProductData}, т.к. продукт «Агро» имеет
 * специфичные значения полей подбора (вид продукта, цель кредитования)
 * и требует поля сегмента, отсутствующего в базовом ProductData.
 */
@Getter
@Builder
public class AgroProductData {

    // ── Подбор продукта ───────────────────
    private String productType;     // Вид продукта — "Агро" / "Кишоварзи"
    private String creditPurpose;   // Цель кредитования
    private String amount;          // Сумма
    private String term;            // Срок
    private String currency;        // Валюта
    private String segment;         // Сегмент — "Микро"

    // ── График погашения ──────────────────
    private String repaymentDay;    // Запрашиваемая дата погашения
    private String scheduleNumber;  // Последовательность (порядковый номер строки)
    private String scheduleType;    // Метод расчёта
}