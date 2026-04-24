package com.creatio.autotest.testdata.models;

public enum CurrencyType {

    TJS ("Сомони Чумхурии Точикистон"),
    USD ("Доллар США"),
    EUR ("Евро");

    private final String uiValue;

    CurrencyType(String uiValue) {
        this.uiValue = uiValue;
    }

    public String getUiValue() {
        return uiValue;
    }
}