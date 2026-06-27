package com.creatio.autotest.testdata.models.registration.fields;

/**
 * Тип занятости заёмщика.
 * Используется в процессе регистрации кредитной заявки для выбора стратегии
 * заполнения анкеты (см. BaseClientFlow и его реализации).
 */
public enum EmploymentType {

    SELF_EMPLOYED("Самозанятый"),
    EMPLOYEE("Работает в организации"),
    OTHER_INCOME("Имеет другой источник дохода");

    private final String uiName;

    EmploymentType(String uiName) {
        this.uiName = uiName;
    }

    /**
     * UI-значение для выбора в lookup-поле Creatio.
     */
    public String getUiName() {
        return uiName;
    }
}