package com.creatio.autotest.flows.retail.registration;

import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.flows.retail.registration.client.BaseClientFlow;
import com.creatio.autotest.testdata.models.RegistrationData;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistrationStageFlow {

    private static final Logger log =
            LoggerFactory.getLogger(RegistrationStageFlow.class);

    private final RegistrationGeneralInfoFlow         generalInfoFlow;
    private final RegistrationParticipantsFlow        participantsFlow;
    private final RegistrationDocumentsFlow           documentsFlow;
    private final RegistrationAddressFlow             addressFlow;
    private final RegistrationIncomeExpensesFlow      incomeExpensesFlow;
    private final RegistrationAdditionalInfoFlow      additionalInfoFlow;
    private final RegistrationScoringConfirmationFlow scoringFlow;
    private final RegistrationPrintFlow               printFlow;
    private final BasePage basePage = new BasePage();

    public RegistrationStageFlow() {
        this.generalInfoFlow    = new RegistrationGeneralInfoFlow();
        this.participantsFlow   = new RegistrationParticipantsFlow();
        this.documentsFlow      = new RegistrationDocumentsFlow();
        this.addressFlow        = new RegistrationAddressFlow();
        this.incomeExpensesFlow = new RegistrationIncomeExpensesFlow();
        this.additionalInfoFlow = new RegistrationAdditionalInfoFlow();
        this.scoringFlow        = new RegistrationScoringConfirmationFlow();
        this.printFlow          = new RegistrationPrintFlow();
    }

    @Step("Этап регистрации заявки")
    public void completeRegistrationStage(RegistrationData data) {
        log.info("Complete registration stage");

        // 1. Общая информация
        generalInfoFlow.fillGeneralInfo(data);

        // 2. Участники
        participantsFlow.addBorrower();

        // 3. Документы
        documentsFlow.fillBorrowerDocuments();

        // 4. Адреса
        addressFlow.fillAddresses();
        addressFlow.setPzlRelation(false);

        // 5. Доходы и расходы
        incomeExpensesFlow.fillIncomeAndExpenses(data.getIncomeExpenses());

        // 6. Скоринговые данные
        additionalInfoFlow.fillBaseScoringData(data.getScoringData());

        // 7. Подтверждение скоринга
        scoringFlow.confirmScoring();

        // 8. Печать
        printFlow.printApplication();

       // Selenide.refresh();
        basePage.waitForPage();

        log.info("Registration stage completed");


    }

    @Step("Этап регистрации заявки (с типом клиента)")
    public void completeRegistrationStage(RegistrationData data, BaseClientFlow clientFlow) {
        log.info("Complete registration stage with client type");

        generalInfoFlow.fillGeneralInfo(data);
        participantsFlow.addBorrower();
        documentsFlow.fillBorrowerDocuments();
        addressFlow.fillAddresses();
        addressFlow.setPzlRelation(false);
        incomeExpensesFlow.fillIncomeAndExpenses(data.getIncomeExpenses());
        additionalInfoFlow.fillBaseScoringData(data.getScoringData());

        // специфика типа клиента
        clientFlow.fill();

        scoringFlow.confirmScoring();
        printFlow.printApplication();

     //   Selenide.refresh();
        basePage.waitForPage();

        log.info("Registration stage with client type completed");
    }
}