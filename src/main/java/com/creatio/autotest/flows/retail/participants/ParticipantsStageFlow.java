package com.creatio.autotest.flows.retail.participants;

import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.core.verify.VerificationCollector;
import com.creatio.autotest.testdata.models.ParticipantData;
import com.creatio.autotest.testdata.models.ParticipantRole;
import com.creatio.autotest.testdata.models.RegistrationIncomeExpensesData;
import com.creatio.autotest.utils.FieldUtils;
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

        verifyParticipantsRoundTrip(participants);

        log.info("Participants stage completed");
    }

    /**
     * ROUND-TRIP участников (V4). Грид участников:
     * grid-BnzVwFinApplicationAllParticipantDetailDataGridGrid-wrap (live-подтверждён).
     * Число строк = добавленные участники + 1 заёмщик (заёмщик сидит в гриде
     * с этапа оформления — ROW 0). Структурированных ячеек (column-name) в строке
     * нет — роль и ФИО сверяем по тексту строки (gridHasRowContainingSafe).
     * flush здесь: стадия «Участники» — самодостаточный сегмент, флоу общий для
     * обеих линеек.
     */
    @Step("Round-trip участников заявки")
    private void verifyParticipantsRoundTrip(List<ParticipantData> participants) {
        final String gridWrap =
                "grid-BnzVwFinApplicationAllParticipantDetailDataGridGrid-wrap";

        int expectedRows = participants.size() + 1; // +1 заёмщик
        VerificationCollector.check("Участники — число строк (+заёмщик)",
                String.valueOf(expectedRows),
                String.valueOf(FieldUtils.getGridRowCount(gridWrap)));

        for (ParticipantData p : participants) {
            String fio = (p.getLastName() + " " + p.getFirstName() + " " + p.getMiddleName()).trim();
            String role = p.getRole().getUiValue();
            VerificationCollector.checkTrue(
                    "Участник в гриде: " + role + " — " + fio,
                    FieldUtils.gridHasRowContainingSafe(gridWrap, fio, role),
                    "строка с ФИО и ролью в гриде участников");
        }

        VerificationCollector.flush("Участники");
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