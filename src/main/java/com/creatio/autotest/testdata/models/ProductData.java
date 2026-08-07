package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

/**
 * Единая модель данных подбора продукта — для всех продуктовых линий
 * (розница, Агро/Микро и будущие типы). Заменяет раздельные розничную и
 * агро-модели: продуктовые линии различаются не типом модели, а значениями
 * полей и двумя nullable-переключателями ниже.
 * <p>
 * Соответствие полей странице «Подбор продукта» (Creatio 7.18):
 * <ul>
 *   <li>«Категория продукта» — readonly, автозаполняется, тестом не задаётся;</li>
 *   <li>«Тип продукта» → {@link #productType} (маркер дропдауна {@code ProductType});</li>
 *   <li>«Вид продукта» → {@link #productKind} (маркер {@code ProductKind});</li>
 *   <li>«Цель кредитования» → {@link #creditPurpose} (маркер {@code CreditPurpose});</li>
 *   <li>«Сумма» → {@link #amount}; «Срок, мес.» → {@link #term};</li>
 *   <li>«Валюта» → {@link #currency} (маркер {@code SpecificationListItem}).</li>
 * </ul>
 *
 * <b>Два nullable-переключателя, задающие поведение флоу подбора:</b>
 * <ul>
 *   <li>{@link #productType} — {@code null}/пусто ⇒ флоу НЕ трогает «Тип продукта»
 *       и полагается на значение по умолчанию «Потребительское кредитование»
 *       (розница). Заполнен ⇒ флоу выставляет «Тип продукта» перед «Видом»
 *       (Агро и прочие небазовые типы).</li>
 *   <li>{@link #productName} — {@code null}/пусто ⇒ из «Результатов подбора»
 *       берётся первая строка грида (розница). Заполнен ⇒ строка выбирается по
 *       частичному совпадению имени (Агро/Микро: несколько результатов
 *       различаются диапазоном суммы и типом клиента — новый/повторный).</li>
 * </ul>
 *
 * Поля графика погашения ({@link #repaymentDay}, {@link #scheduleNumber},
 * {@link #scheduleType}) потребляются {@code ApplicationCreationFlow} и к
 * подбору продукта отношения не имеют.
 */
@Getter
@Builder
public class ProductData {

    // ── Подбор продукта ───────────────────

    /**
     * «Тип продукта» (маркер {@code ProductType}).
     * {@code null}/пусто ⇒ шаг пропускается, действует дефолт
     * «Потребительское кредитование» (розница). Пример небазового значения:
     * «Агро кредиты».
     */
    private String productType;

    /**
     * «Вид продукта» (маркер {@code ProductKind}). Разблокирует «Цель
     * кредитования». Пример: «Карзхои гуногунмаксад» (розница),
     * «Агро кредиты» (Агро).
     */
    private String productKind;

    /** «Цель кредитования» (маркер {@code CreditPurpose}). */
    private String creditPurpose;

    /** «Сумма» — числовое поле (FloatValue). */
    private String amount;

    /** «Срок, мес.» — числовое поле (IntegerValue). Совпадает со сроком графика. */
    private String term;

    /** «Валюта» — справочник (маркер {@code SpecificationListItem}). */
    private String currency;

    /**
     * Имя строки в «Результаты подбора» для выбора (частичное совпадение по
     * {@code data-item-marker} строки грида). {@code null}/пусто ⇒ берётся
     * первая строка грида.
     */
    private String productName;

    // ── График погашения ──────────────────

    /** «Запрашиваемая дата погашения». */
    private String repaymentDay;

    /** «Последовательность» (порядковый номер строки графика). */
    private String scheduleNumber;

    /** «Метод расчёта». */
    private String scheduleType;
}