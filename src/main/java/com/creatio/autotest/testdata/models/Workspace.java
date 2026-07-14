package com.creatio.autotest.testdata.models;

import com.creatio.autotest.testdata.models.registration.ActivityData;
import com.creatio.autotest.testdata.models.registration.ClientProfileData;
import com.creatio.autotest.testdata.models.registration.EducationCareerData;
import com.creatio.autotest.testdata.models.registration.WorkAddressData;
import com.creatio.autotest.testdata.models.registration.fields.EmploymentType;

public enum Workspace {

    RETAIL_MANAGER ("Розничный менеджер", true),
    CREDIT_EXPERT  ("Кредитный эксперт",  true),
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