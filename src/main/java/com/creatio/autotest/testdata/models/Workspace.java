package com.creatio.autotest.testdata.models;

public enum Workspace {

    RETAIL_MANAGER ("Розничный менеджер", true),
    UNDERWRITER    ("Верификатор",        false),
    IKOK           ("ИКОК",               false),
    IKOK_GO        ("ИКОК-ГО",            false),
    CASHIER        ("Касса",              false);

    private final String  uiName;
    private final boolean hasConsultationPanel;

    Workspace(String uiName, boolean hasConsultationPanel) {
        this.uiName               = uiName;
        this.hasConsultationPanel = hasConsultationPanel;
    }

    public String getUiName() {
        return uiName;
    }

    public boolean hasConsultationPanel() {
        return hasConsultationPanel;
    }
}