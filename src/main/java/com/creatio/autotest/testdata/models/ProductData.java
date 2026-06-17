package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductData {

    // ── Подбор продукта ───────────────────
    private String productType;
    private String creditPurpose;
    private String amount;
    private String term;        // используется и здесь...
    private String currency;

    // ── Создание заявки ───────────────────
    private String repaymentDay;    // день погашения
    private String scheduleNumber;  // номер графика
    private String scheduleType;    // тип графика ("Аннуитетный")

    // term используется и здесь ↑
}