package com.creatio.autotest.flows.retail.registration.client;

import com.creatio.autotest.flows.retail.registration.RegistrationAdditionalInfoFlow;
import com.creatio.autotest.flows.retail.registration.RegistrationAddressFlow;
import com.creatio.autotest.flows.retail.registration.RegistrationParticipantsFlow;
import com.creatio.autotest.flows.retail.registration.RegistrationIncomeExpensesFlow;
import com.creatio.autotest.testdata.models.registration.ClientProfileData;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Тип клиента: "Самозанятый".
 * Полный путь: тип занятости + образование/карьера → адрес работы → деятельность.
 * Деталь "Карьера" подтягивается автоматически (как у EMPLOYEE) — не заполняем.
 * Все значения берутся из ClientProfileData (data-driven).
 */
public class SelfEmployedClientFlow extends BaseClientFlow {

    private static final Logger log =
            LoggerFactory.getLogger(SelfEmployedClientFlow.class);

    private final RegistrationAdditionalInfoFlow additionalInfoFlow =
            new RegistrationAdditionalInfoFlow();
    private final RegistrationAddressFlow addressFlow =
            new RegistrationAddressFlow();
    private final RegistrationParticipantsFlow participantsFlow =
            new RegistrationParticipantsFlow();
    private final RegistrationIncomeExpensesFlow incomeExpensesFlow =
            new RegistrationIncomeExpensesFlow();

    private final ClientProfileData profile;

    public SelfEmployedClientFlow(ClientProfileData profile) {
        this.profile = profile;
    }

    @Override
    @Step("Заполнение анкеты самозанятого клиента")
    public void fill() {
        log.info("Fill SELF_EMPLOYED client");

        additionalInfoFlow.selectEmploymentTypeSelfEmployed(profile);
        addressFlow.fillWorkAddressSelfEmployed(profile.getWorkAddress());
        // Деталь "Карьера" подтягивается автоматически — отдельно не заполняем.
        // participantsFlow.fillCareerDetailsSelfEmployed(profile.getCareer());
        incomeExpensesFlow.fillIndividualActivitySelfEmployed(profile.getActivity());
    }
}