package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

/**
 * Тест-данные подбора продукта «Агро» (сегмент «Микро»).
 * Отдельная модель от {@link ProductData}, т.к. продукт «Агро» имеет
 * специфичные значения полей подбора и дополнительное поле «Тип продукта».
 * <p>
 * Live-подтверждение (скрин страницы «Подбор продукта»):
 *  - «Категория продукта» — readonly, автозаполняется («Финансирование»);
 *  - «Тип продукта» (marker ProductType) — по умолчанию «Потребительское
 *    кредитование», требует смены на «Агро кредиты»;
 *  - «Вид продукта» (marker ProductKind) — «Агро кредиты»;
 *  - отдельного поля «Сегмент» на UI нет.
 */
@Getter
@Builder
public class AgroProductData {

    // ── Подбор продукта ───────────────────
    private String productType;     // Тип продукта (ProductType) — "Агро кредиты"
    private String productKind;     // Вид продукта (ProductKind) — "Агро кредиты"
    private String creditPurpose;   // Цель кредитования
    private String amount;          // Сумма
    private String term;            // Срок, мес.
    private String currency;        // Валюта

    // ── Результат подбора ─────────────────
    // Имя строки в «Результаты подбора» для выбора (частичное совпадение по
    // data-item-marker строки грида). Позволяет выбрать нужный продукт среди
    // нескольких результатов (напр., «нав сегменти микро» — новый клиент,
    // «такрори сегменти микро» — повторный).
    private String productName;

    // ── График погашения ──────────────────
    private String repaymentDay;    // Запрашиваемая дата погашения
    private String scheduleNumber;  // Последовательность (порядковый номер строки)
    private String scheduleType;    // Метод расчёта

    /**
     * Конвертация в розничный {@link ProductData} для переиспользования
     * {@code ApplicationCreationFlow} (тот использует только поля графика
     * погашения). Изоляция сохраняется: Агро-модель знает про розничную.
     */
    public ProductData toProductData() {
        return ProductData.builder()
                .productType(productKind)
                .creditPurpose(creditPurpose)
                .amount(amount)
                .term(term)
                .currency(currency)
                .repaymentDay(repaymentDay)
                .scheduleNumber(scheduleNumber)
                .scheduleType(scheduleType)
                .build();
    }
}