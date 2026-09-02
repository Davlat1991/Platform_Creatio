package com.creatio.autotest.flows.micro.registration;

import com.creatio.autotest.flows.micro.registration.client.MicroClientFlowFactory;
import com.creatio.autotest.testdata.AgroScenarioFactory;
import com.creatio.autotest.testdata.models.RegistrationData;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Стадия «Оформление» (Агро/Микро).
 * <p>
 * Инкапсулирует ранее инлайн-последовательность из {@code MicroSegmentRoute.registration()}
 * (Ш15.2): порядок и вызовы 8 подфлоу идентичны прежним, маршрут теперь делегирует сюда
 * одной строкой — по образцу розничного {@code RegistrationStageFlow}.
 * <p>
 * Агро-специфичные данные берутся из {@link AgroScenarioFactory} внутри стадии
 * (принятая связка флоу→фабрика для Агро-специфичного этапа).
 */
public class AgroRegistrationStageFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroRegistrationStageFlow.class);

    private final AgroRegistrationGeneralInfoFlow         generalInfoFlow    = new AgroRegistrationGeneralInfoFlow();
    private final AgroRegistrationParticipantsFlow        participantsFlow   = new AgroRegistrationParticipantsFlow();
    private final AgroRegistrationDocumentsFlow           documentsFlow      = new AgroRegistrationDocumentsFlow();
    private final AgroRegistrationAddressFlow             addressFlow        = new AgroRegistrationAddressFlow();
    private final AgroRegistrationIncomeExpensesFlow      incomeExpensesFlow = new AgroRegistrationIncomeExpensesFlow();
    private final AgroRegistrationAdditionalInfoFlow      additionalInfoFlow = new AgroRegistrationAdditionalInfoFlow();
    private final AgroRegistrationScoringConfirmationFlow scoringFlow        = new AgroRegistrationScoringConfirmationFlow();
    private final AgroRegistrationPrintFlow               printFlow          = new AgroRegistrationPrintFlow();

    @Step("Агро: стадия «Оформление»")
    public void completeRegistrationStage(RegistrationData data) {
        log.info("Agro: complete registration stage");

        // 1. Общая информация (+ сектор экономики / ОКОНХ)
        generalInfoFlow.fillGeneralInfo(
                data,
                AgroScenarioFactory.agroEconomicSector(),
                AgroScenarioFactory.OKONH());

        // 2. Добавить заёмщика (открывает анкету)
        participantsFlow.addBorrower();

        // 3. Документы заёмщика
        documentsFlow.fillBorrowerDocuments(AgroScenarioFactory.agroBorrowerDocuments());

        // 4. Адреса
        addressFlow.fillAddresses(AgroScenarioFactory.agroAddressBase());
        addressFlow.setPzlRelation(false);

        // 5. Доходы и расходы
        incomeExpensesFlow.fillIncomeAndExpenses(
                data.getIncomeExpenses(),
                AgroScenarioFactory.agroIncomeExtra());

        // 6. Скоринговые данные
        additionalInfoFlow.fillBaseScoringData(data.getScoringData());

        // 7. Анкета клиента (стратегия по типу занятости)
        MicroClientFlowFactory.from(data.getClientProfile()).fill();

        // 8. Подтверждение скоринга
        scoringFlow.confirmScoring(AgroScenarioFactory.agroScoringConfirmation());

        // 9. Печать
        printFlow.printApplication();

        log.info("Agro: registration stage completed");
    }
}