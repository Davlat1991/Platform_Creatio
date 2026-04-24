package com.creatio.autotest.flows.registration;

import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.testdata.models.BaseScoringData;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistrationAdditionalInfoFlow {

    private static final Logger log =
            LoggerFactory.getLogger(RegistrationAdditionalInfoFlow.class);

    private final LookupComponent lookup = new LookupComponent();

    @Step("Заполнение базовых скоринговых данных")
    public void fillBaseScoringData(BaseScoringData data) {
        log.info("Fill base scoring data");

        lookup.select("Семейное положение", data.getMaritalStatus());

        setField("Количество иждивенцев (строка)", data.getDependentsCount());
        setField("Количество детей",               data.getChildrensCount());
        setField("Количество членов семьи",         data.getFamilyCount());
        setField("Общий стаж",                     data.getTotalExperience());
        setField("Общий стаж, лет",                data.getTotalExperienceYears());
        setField("Общий стаж, мес",                data.getTotalExperienceMonths());
        setField("Стаж на последнем месте, лет",   data.getLastJobExperienceYears());
        setField("Стаж на последнем месте, мес",   data.getLastJobExperienceMonths());
    }

    private void setField(String label, String value) {
        if (value == null || value.isEmpty()) return;
        lookup.selectByLabel(label, value);
    }
}