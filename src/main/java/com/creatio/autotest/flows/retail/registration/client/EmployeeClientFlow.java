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
 * Тип клиента: "Работает в организации".
 * Полный путь: тип занятости + образование/карьера → адрес работы → карьера → деятельность.
 * Все значения берутся из ClientProfileData (data-driven).
 */
public class EmployeeClientFlow extends BaseClientFlow {

    private static final Logger log =
            LoggerFactory.getLogger(EmployeeClientFlow.class);

    private final RegistrationAdditionalInfoFlow additionalInfoFlow =
            new RegistrationAdditionalInfoFlow();
    private final RegistrationAddressFlow addressFlow =
            new RegistrationAddressFlow();
    private final RegistrationParticipantsFlow participantsFlow =
            new RegistrationParticipantsFlow();
    private final RegistrationIncomeExpensesFlow incomeExpensesFlow =
            new RegistrationIncomeExpensesFlow();

    private final ClientProfileData profile;

    public EmployeeClientFlow(ClientProfileData profile) {
        this.profile = profile;
    }

    @Override
    @Step("Заполнение анкеты клиента, работающего в организации")
    public void fill() {
        log.info("Fill EMPLOYEE client");

        additionalInfoFlow.selectEmploymentTypeEmployee(profile);
        addressFlow.fillWorkAddressEmployee(profile.getWorkAddress());
        // Деталь "Карьера" подтягивается автоматически из "Образование и карьера" —
        // отдельное заполнение создаёт дубль. Оставлено для страховки.
        // participantsFlow.fillCareerDetailsEmployee(profile.getCareer());
        incomeExpensesFlow.fillIndividualActivityEmployed(profile.getActivity());
    }
}