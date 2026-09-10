package com.creatio.autotest.flows.micro.participants;

import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.core.verify.VerificationCollector;
import com.creatio.autotest.testdata.AgroScenarioFactory;
import com.creatio.autotest.testdata.models.ParticipantData;
import com.creatio.autotest.utils.FieldUtils;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Стадия «Участники заявки» (Агро/Микро) — оркестратор.
 * <p>
 * Для каждого участника: переход на вкладку «Участники заявки» → добавление
 * участника с нужной ролью → заполнение анкеты по роли (поручитель —
 * полная, залогодатель — короткая).
 * <p>
 * Изолированная Микро-копия розничного {@code ParticipantsStageFlow}.
 * Залоговое обеспечение сюда НЕ входит — это отдельная стадия.
 */
public class AgroParticipantsStageFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroParticipantsStageFlow.class);

    private static final String PARTICIPANTS_TAB = "Участники заявки";

    private final ButtonsComponent        buttons   = new ButtonsComponent();
    private final AgroParticipantsAddFlow addFlow   = new AgroParticipantsAddFlow();
    private final AgroGuarantorFlow       guarantor = new AgroGuarantorFlow();
    private final AgroPledgerFlow         pledger   = new AgroPledgerFlow();

    // ══════════════════════════════════════
    // PUBLIC API
    // ══════════════════════════════════════

    @Step("Агро: стадия «Участники заявки»")
    public void completeParticipantsStage(List<ParticipantData> participants) {
        log.info("Agro: complete participants stage, count={}", participants.size());

        if (participants == null || participants.isEmpty()) {
            log.info("Agro: no participants in scenario — skip stage");
            return;
        }

        for (ParticipantData participant : participants) {
            addAndFill(participant);
        }

        verifyParticipantsRoundTrip(participants);

        log.info("Agro: participants stage completed");
    }

    /**
     * ROUND-TRIP участников (V4, Агро). Грид участников — общесистемная деталь
     * Creatio (grid-BnzVwFinApplicationAllParticipantDetailDataGridGrid-wrap),
     * от линейки не зависит; подтверждается живым прогоном Агро-регресса. Число
     * строк = добавленные участники + 1 заёмщик (сидит в гриде с оформления).
     * Роль/ФИО — по тексту строки (структурированных column-name в гриде нет).
     */
    @Step("Агро: round-trip участников заявки")
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

        VerificationCollector.flush("Участники (Агро)");
    }

    /** Добавление одного участника и заполнение его анкеты по роли. */
    @Step("Агро: участник {participant.role}")
    public void addAndFill(ParticipantData participant) {
        log.info("Agro: add and fill participant role='{}'",
                participant.getRole().getUiValue());

        buttons.clickTabByName(PARTICIPANTS_TAB);
        addFlow.addParticipant(participant);

        switch (participant.getRole()) {
            case GUARANTOR -> guarantor.fill(AgroScenarioFactory.agroGuarantorData());
            case PLEDGER   -> pledger.fill(AgroScenarioFactory.agroPledgerData());
            default -> throw new IllegalArgumentException(
                    "Unsupported participant role for Agro: " + participant.getRole());
        }

        log.info("Agro: participant '{}' done", participant.getRole().getUiValue());
    }
}