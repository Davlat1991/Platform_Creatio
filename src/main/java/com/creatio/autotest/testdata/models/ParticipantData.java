package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParticipantData {

    private ParticipantRole role;

    private String          lastName;
    private String          firstName;
    private String          middleName;
    private boolean         pzlRelation;

    // ══════════════════════════════════════
    // FACTORIES
    // ══════════════════════════════════════

    public static ParticipantData guarantor(boolean pzl) {
        return ParticipantData.builder()
                .role(ParticipantRole.GUARANTOR)
                .lastName("Ашурова")
                .firstName("Шукрона")
                .middleName("Махсудовна")
                .pzlRelation(pzl)
                .build();
    }

    public static ParticipantData pledger(boolean pzl) {
        return ParticipantData.builder()
                .role(ParticipantRole.PLEDGER)
                .lastName("Садуллоев")
                .firstName("Сафарали")
                .middleName("Нусратович")
                .pzlRelation(pzl)
                .build();
    }

    public static ParticipantData pledger1(boolean pzl) {
        return ParticipantData.builder()
                .role(ParticipantRole.PLEDGER)
                .lastName("Хамидов")
                .firstName("Азизходжа")
                .middleName("Азамходжаевич")
                .pzlRelation(pzl)
                .build();
    }
}