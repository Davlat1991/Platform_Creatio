package com.creatio.autotest.flows.micro.registration.client;

import com.creatio.autotest.flows.micro.registration.AgroRegistrationAddressFlow;
import com.creatio.autotest.flows.micro.registration.AgroRegistrationAdditionalInfoFlow;
import com.creatio.autotest.flows.micro.registration.AgroRegistrationIncomeExpensesFlow;
import com.creatio.autotest.testdata.models.registration.ClientProfileData;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Тип клиента сегмента «Микро»: «Самозанятый» (актуально для фермера Агро).
 * <p>
 * Параллель розничному {@code SelfEmployedClientFlow}, но вызывает
 * Микро-под-флоу регистрации, а не розничные:
 *  1) выбор типа занятости (Дополнительная информация),
 *  2) адрес места работы,
 *  3) деятельность физ.лица.
 * <p>
 * Данные — исключительно из {@link ClientProfileData} (data-driven).
 */
public class MicroSelfEmployedClientFlow extends MicroBaseClientFlow {

    private static final Logger log =
            LoggerFactory.getLogger(MicroSelfEmployedClientFlow.class);

    private final AgroRegistrationAdditionalInfoFlow additionalInfoFlow =
            new AgroRegistrationAdditionalInfoFlow();
    private final AgroRegistrationAddressFlow addressFlow =
            new AgroRegistrationAddressFlow();
    private final AgroRegistrationIncomeExpensesFlow incomeExpensesFlow =
            new AgroRegistrationIncomeExpensesFlow();

    private final ClientProfileData profile;

    public MicroSelfEmployedClientFlow(ClientProfileData profile) {
        this.profile = profile;
    }

    @Override
    @Step("Заполнение анкеты самозанятого клиента (Микро)")
    public void fill() {
        log.info("Fill MICRO SELF_EMPLOYED client");

        additionalInfoFlow.selectEmploymentTypeSelfEmployed(profile);
        addressFlow.fillWorkAddressSelfEmployed(profile.getWorkAddress());
        incomeExpensesFlow.fillIndividualActivitySelfEmployed(profile.getActivity());
    }
}