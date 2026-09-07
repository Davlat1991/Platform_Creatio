package com.creatio.autotest.flows.retail.registration;

import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.verify.VerificationCollector;
import com.creatio.autotest.testdata.models.RegistrationData;
import com.creatio.autotest.utils.FieldUtils;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistrationGeneralInfoFlow {

    private static final Logger log =
            LoggerFactory.getLogger(RegistrationGeneralInfoFlow.class);

    private final LookupComponent lookup = new LookupComponent();

    @Step("Заполнение общей информации регистрации")
    public void fillGeneralInfo(RegistrationData data) {
        log.info("Fill general info: region='{}' source='{}'",
                data.getRegion(), data.getInfoSource());

        lookup
                .select("Регион использования кредита", data.getRegion())
                .select("Источник информации",           data.getInfoSource());

        // ROUND-TRIP (V3, в пределах подшага): поля анкеты выгружаются при уходе
        // со вкладки, поэтому сверяем сразу после заполнения, пока вкладка открыта.
        VerificationCollector.check("Регион использования кредита",
                data.getRegion(),
                FieldUtils.getValueByLabelSafe("Регион использования кредита"));
        VerificationCollector.check("Источник информации",
                data.getInfoSource(),
                FieldUtils.getValueByLabelSafe("Источник информации"));
    }
}