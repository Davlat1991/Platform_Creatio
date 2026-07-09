package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

/**
 * Тест-данные подбора продукта «Агро» (сегмент «Микро»).
 * Отдельная модель от {@link ProductData}, т.к. продукт «Агро» имеет
 * специфичные значения полей подбора (вид продукта, цель кредитования).
 * <p>
 * Live-подтверждение (скрин страницы «Подбор продукта»): отдельного поля
 * «Сегмент» на UI нет — сегмент выражается через значение «Вид продукта»
 * / «Цель кредитования», отдельное поле для него в модели не заводим.
 */
@Getter
@Builder
public class AgroProductData {

    // ── Подбор продукта ───────────────────
    private String productType;     // Вид продукта — "Агро кредиты"
    private String creditPurpose;   // Цель кредитования
    private String amount;          // Сумма
    private String term;            // Срок, мес.
    private String currency;        // Валюта

    // ── График погашения ──────────────────
    private String repaymentDay;    // Запрашиваемая дата погашения
    private String scheduleNumber;  // Последовательность (порядковый номер строки)
    private String scheduleType;    // Метод расчёта
}