package com.creatio.autotest.flows.retail.participants;

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

        log.info("Complete participants stage: count={}", participants.size());

        if (participants.isEmpty()) {
            log.info("No participants — skip");
            return;
        }

        for (ParticipantData participant : participants) {
            processParticipant(participant, incomeData);
        }

        log.info("Participants stage completed");
    }

    // ══════════════════════════════════════
    // PRIVATE
    // ══════════════════════════════════════

    private void processParticipant(ParticipantData participant,
                                    RegistrationIncomeExpensesData incomeData) {
        log.info("Process participant: role='{}'",
                participant.getRole().getUiValue());

        // Каждый раз перед добавлением открываем вкладку
        // После возврата из анкеты страница может быть на другой вкладке
        openParticipantsTab();

        addFlow.addParticipant(participant);

        if (participant.getRole() == ParticipantRole.PLEDGER) {
            new PledgerFlow().fill(participant, incomeData);
        } else if (participant.getRole() == ParticipantRole.GUARANTOR) {
            new GuarantorFlow().fill(participant, incomeData);
        }

        log.info("Participant processed: role='{}'",
                participant.getRole().getUiValue());
    }

    private void openParticipantsTab() {
        log.info("Open participants tab");
        buttons.clickTabByName("Участники заявки");
    }
}