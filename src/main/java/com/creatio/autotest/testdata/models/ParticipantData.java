package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class ParticipantData {

    private ParticipantRole role;

    private String  lastName;
    private String  firstName;
    private String  middleName;
    private boolean pzlRelation;

    /**
     * Грузить ли документы этого участника на стадии «Сбор документов».
     * Розничный дефолт — true (фабрики). Агро/Микро документы участников не грузит,
     * поэтому флаг там игнорируется. Явное отключение — {@link #withoutDocuments()}.
     */
    private boolean attachDocuments;

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
                .attachDocuments(true)
                .build();
    }

    public static ParticipantData pledger(boolean pzl) {
        return ParticipantData.builder()
                .role(ParticipantRole.PLEDGER)
                .lastName("Садуллоев")
                .firstName("Сафарали")
                .middleName("Нусратович")
                .pzlRelation(pzl)
                .attachDocuments(true)
                .build();
    }

    public static ParticipantData pledger1(boolean pzl) {
        return ParticipantData.builder()
                .role(ParticipantRole.PLEDGER)
                .lastName("Хамидов")
                .firstName("Азизходжа")
                .middleName("Азамходжаевич")
                .pzlRelation(pzl)
                .attachDocuments(true)
                .build();
    }

    // ══════════════════════════════════════
    // TOGGLES
    // ══════════════════════════════════════

    /** Копия участника с отключённой загрузкой его документов на «Сборе документов». */
    public ParticipantData withoutDocuments() {
        return this.toBuilder().attachDocuments(false).build();
    }
}