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
    private String term;
    private String currency;

    // ── График погашения ──────────────────
    private String repaymentDay;    // Запрашиваемая дата погашения
    private String scheduleNumber;  // Последовательность (порядковый номер строки)
    private String scheduleType;    // Метод расчёта

}