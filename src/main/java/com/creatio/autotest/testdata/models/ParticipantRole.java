package com.creatio.autotest.testdata.models;

public enum ParticipantRole {

    BORROWER  ("Заемщик"),
    GUARANTOR ("Поручитель"),
    PLEDGER   ("Залогодатель");

    private final String uiValue;

    ParticipantRole(String uiValue) {
        this.uiValue = uiValue;
    }

    public String getUiValue() {
        return uiValue;
    }
}