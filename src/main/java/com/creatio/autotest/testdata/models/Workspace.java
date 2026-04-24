package com.creatio.autotest.testdata.models;

public enum Workspace {

    RETAIL_MANAGER ("Розничный менеджер"),
    UNDERWRITER    ("Верификатор"),
    IKOK           ("ИКОК"),
    IKOK_GO        ("ИКОК-ГО"),
    CASHIER        ("Касса");

    private final String uiName;

    Workspace(String uiName) {
        this.uiName = uiName;
    }

    public String getUiName() {
        return uiName;
    }
}