package com.creatio.autotest.flows.registration;

import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.CheckboxComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ScrollService;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistrationScoringConfirmationFlow {

    private static final Logger log =
            LoggerFactory.getLogger(RegistrationScoringConfirmationFlow.class);

    private final ButtonsComponent  buttons  = new ButtonsComponent();
    private final LookupComponent   lookup   = new LookupComponent();
    private final CheckboxComponent checkbox = new CheckboxComponent();
    private final BasePage          basePage = new BasePage();
    private final ScrollService scroll = new ScrollService();

    @Step("Подтверждение этапа оценки информации заемщика")
    public void confirmScoring() {
        log.info("Confirm scoring");

        scroll.scrollToTop();

        buttons.clickTabByName("ОЦЕНКА ИНФОРМАЦИИ");

        lookup
                .select("Тип собственности на недвижимость", "Более 2-ух квартир")
                .select("Тип владения автомобилем",           "Есть автомобиль");

        checkbox.ensureCheckedBKI("IsConsentBKIProcessing");
    }
}