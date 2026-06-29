package com.creatio.autotest.testdata.models;

/**
 * Решение скоринга на стадии рассмотрения.
 * Ось независима от маршрута: ветку флоу НЕ определяет, проверяется отдельным assert.
 */
public enum ScoringDecision {

    APPROVE("Одобрить"),
    REJECT("Отказать"),
    OFFER_AMOUNT("Предложить сумму"),
    APPROVE_WITH_COLLATERAL("Одобрить с обеспечением");

    private final String uiText;

    ScoringDecision(String uiText) {
        this.uiText = uiText;
    }

    /** Текст решения как он отображается в UI. */
    public String getUiText() {
        return uiText;
    }
}