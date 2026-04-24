package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParticipantData {

    private ParticipantRole role;
    private String          firstName;
    private String          lastName;
    private String          middleName;
    private boolean         pzlRelation;

    // ══════════════════════════════════════
    // FACTORIES
    // ══════════════════════════════════════

    public static ParticipantData guarantor(boolean pzl) {
        return ParticipantData.builder()
                .role(ParticipantRole.GUARANTOR)
                .firstName("Фарида")
                .lastName("Тест")
                .middleName("Тестовна")
                .pzlRelation(pzl)
                .build();
    }

    public static ParticipantData pledger(boolean pzl) {
        return ParticipantData.builder()
                .role(ParticipantRole.PLEDGER)
                .firstName("Фарида")
                .lastName("Тест")
                .middleName("Тестовна")
                .pzlRelation(pzl)
                .build();
    }
}