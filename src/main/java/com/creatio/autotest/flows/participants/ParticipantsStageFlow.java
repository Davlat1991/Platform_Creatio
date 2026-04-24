package com.creatio.autotest.flows.participants;

import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.testdata.models.ParticipantData;
import com.creatio.autotest.testdata.models.ParticipantRole;
import com.creatio.autotest.testdata.models.RegistrationIncomeExpensesData;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ParticipantsStageFlow {

    private static final Logger log =
            LoggerFactory.getLogger(ParticipantsStageFlow.class);

    private final ParticipantsAddFlow      addFlow      = new ParticipantsAddFlow();
    private final ButtonsComponent         buttons      = new ButtonsComponent();

    @Step("Этап: Участники заявки")
    public void completeParticipantsStage(
            List<ParticipantData> participants,
            RegistrationIncomeExpensesData incomeData) {

        log.info("Complete participants stage: count={}",
                participants.size());

        openParticipantsTab();

        for (ParticipantData participant : participants) {
            processParticipant(participant, incomeData);
        }
    }

    // ══════════════════════════════════════
    // PRIVATE
    // ══════════════════════════════════════

    private void openParticipantsTab() {
        buttons.clickTabByName("Участники заявки");
    }

    private void processParticipant(ParticipantData participant,
                                    RegistrationIncomeExpensesData incomeData) {
        log.info("Process participant: role='{}'",
                participant.getRole().getUiValue());

        addFlow.addParticipant(participant);

        // Специфичная логика для каждой роли
        if (participant.getRole() == ParticipantRole.PLEDGER) {
            new PledgerFlow().fill(participant, incomeData);
        } else if (participant.getRole() == ParticipantRole.GUARANTOR) {
            new GuarantorFlow().fill(participant, incomeData);
        }
    }
}