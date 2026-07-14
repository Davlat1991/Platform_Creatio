package com.creatio.autotest.flows.micro.registration;

import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.CheckboxComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.services.ScrollService;
import com.creatio.autotest.testdata.models.AgroScoringConfirmationData;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Стадия «Оформление» (Агро/Микро) — подтверждение этапа оценки информации
 * заёмщика: вкладка «ОЦЕНКА ИНФОРМАЦИИ».
 * <p>
 * Изолированная Микро-копия розничного {@code RegistrationScoringConfirmationFlow}.
 * Структурно повторяет розницу, но, в отличие от неё, НЕ хардкодит значения
 * дропдаунов в теле флоу — они приходят из {@link AgroScoringConfirmationData}
 * (фабрика {@code AgroScenarioFactory.agroScoringConfirmation()}).
 * <p>
 * Общие компоненты ({@link LookupComponent}, {@link CheckboxComponent},
 * {@link ButtonsComponent}, {@link ScrollService}) переиспользуются из Core —
 * они продукт-агностик.
 */
public class AgroRegistrationScoringConfirmationFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroRegistrationScoringConfirmationFlow.class);

    private final ButtonsComponent  buttons  = new ButtonsComponent();
    private final LookupComponent   lookup   = new LookupComponent();
    private final CheckboxComponent checkbox = new CheckboxComponent();
    private final ScrollService     scroll   = new ScrollService();

    @Step("Агро: подтверждение этапа оценки информации заёмщика")
    public void confirmScoring(AgroScoringConfirmationData data) {
        log.info("Agro: confirm scoring (property='{}', vehicle='{}')",
                data.getPropertyOwnershipType(), data.getVehicleOwnershipType());

        scroll.scrollToTop();

        buttons.clickTabByName("ОЦЕНКА ИНФОРМАЦИИ");

        lookup
                .select("Тип собственности на недвижимость", data.getPropertyOwnershipType())
                .select("Тип владения автомобилем",           data.getVehicleOwnershipType());

        checkbox.ensureCheckedBKI("IsConsentBKIProcessing");

        log.info("Agro: scoring confirmed");
    }
}