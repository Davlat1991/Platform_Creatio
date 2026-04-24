package com.creatio.autotest.flows.participants;

import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.testdata.models.ParticipantData;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParticipantsAddFlow {

    private static final Logger log =
            LoggerFactory.getLogger(ParticipantsAddFlow.class);

    private final LookupComponent lookup   = new LookupComponent();
    private final BasePage        basePage = new BasePage();
    private final ClickService    click    = new ClickService();

    @Step("Добавление участника: {participant.role}")
    public void addParticipant(ParticipantData participant) {
        log.info("Add participant: role='{}'",
                participant.getRole().getUiValue());

        // 1. Кнопка добавления
        click.clickById(
                "BnzVwFinApplicationAllParticipantDetailAddTypedRecordButtonButton-imageEl"
        );

        // 2. Тип участника
        click.clickByName("Физ. лицо");
        click.clickByName(participant.getRole().getUiValue());

        // 3. Поиск по ФИО
        lookup.setModalSearchField("Surname",    participant.getLastName());
        lookup.setModalSearchField("GivenName",  participant.getFirstName());
        lookup.setModalSearchField("MiddleName", participant.getMiddleName());

        click.clickByName("Поиск");
        click.clickByName("Выбрать");
        basePage.waitForPage();

        log.info("Participant added: '{}'",
                participant.getRole().getUiValue());
    }
}