package com.creatio.autotest.flows.registration.client;

import com.creatio.autotest.flows.registration.RegistrationAdditionalInfoFlow;
import com.creatio.autotest.testdata.models.registration.ClientProfileData;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Тип клиента: "Имеет другой источник дохода".
 * Самый короткий путь — только выбор типа занятости + причина отсутствия работы.
 * Все значения берутся из ClientProfileData (data-driven, без хардкода).
 */
public class OtherIncomeClientFlow extends BaseClientFlow {

    private static final Logger log =
            LoggerFactory.getLogger(OtherIncomeClientFlow.class);

    private final RegistrationAdditionalInfoFlow additionalInfoFlow =
            new RegistrationAdditionalInfoFlow();

    private final ClientProfileData profile;

    public OtherIncomeClientFlow(ClientProfileData profile) {
        this.profile = profile;
    }

    @Override
    @Step("Заполнение анкеты клиента с иным источником дохода")
    public void fill() {
        log.info("Fill OTHER_INCOME client");
        additionalInfoFlow.selectEmploymentTypeOtherIncome(profile);
    }
}