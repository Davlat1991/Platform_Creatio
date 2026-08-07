package com.creatio.autotest.flows.retail.registration.client;

import com.creatio.autotest.testdata.models.registration.ClientProfileData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Фабрика стратегий заполнения анкеты клиента.
 * По типу занятости из ClientProfileData создаёт нужную реализацию BaseClientFlow.
 *
 * Тип клиента определяется ДАННЫМИ сценария, а не кодом теста — тест остаётся
 * декларативным и не знает о конкретных классах стратегий.
 */
public final class ClientFlowFactory {

    private static final Logger log =
            LoggerFactory.getLogger(ClientFlowFactory.class);

    private ClientFlowFactory() {
    }

    public static BaseClientFlow from(ClientProfileData profile) {
        if (profile == null || profile.getEmploymentType() == null) {
            throw new IllegalArgumentException(
                    "ClientProfileData / employmentType не заданы в сценарии");
        }

        log.info("ClientFlowFactory: создаём флоу для типа {}",
                profile.getEmploymentType());

        switch (profile.getEmploymentType()) {
            case OTHER_INCOME:
                return new OtherIncomeClientFlow(profile);
            case EMPLOYEE:
                return new EmployeeClientFlow(profile);
            case SELF_EMPLOYED:
                return new SelfEmployedClientFlow(profile);
            default:
                throw new UnsupportedOperationException(
                        "Тип клиента пока не реализован: "
                                + profile.getEmploymentType());
        }
    }
}