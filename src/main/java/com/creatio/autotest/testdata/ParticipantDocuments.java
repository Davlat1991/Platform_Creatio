package com.creatio.autotest.testdata;

import com.creatio.autotest.testdata.models.ParticipantData;
import com.creatio.autotest.testdata.models.ParticipantRole;
import com.creatio.autotest.testdata.models.documents.DocumentData;
import com.creatio.autotest.testdata.models.documents.DocumentOwnerData;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Резолвер «участники → документы на стадии Сбор документов».
 * <p>
 * Документы — производное свойство участника, а не отдельная структура: набор
 * берётся по роли (единый источник правды — список участников сценария), грузятся
 * только участники с attachDocuments=true. Дедуп по роли (несколько залогодателей →
 * один PLEDGER-таб), порядок появления сохраняется.
 * <p>
 * Заменяет статический ScenarioFactory.documentOwners() — состав документов теперь
 * управляется сценарием.
 */
public final class ParticipantDocuments {

    private ParticipantDocuments() {}

    public static List<DocumentOwnerData> forParticipants(List<ParticipantData> participants) {
        Set<ParticipantRole> roles = new LinkedHashSet<>();
        if (participants != null) {
            for (ParticipantData p : participants) {
                if (p.isAttachDocuments()) {
                    roles.add(p.getRole());
                }
            }
        }

        List<DocumentOwnerData> result = new ArrayList<>();
        for (ParticipantRole role : roles) {
            result.add(DocumentOwnerData.builder()
                    .owner(role)
                    .documents(defaultDocuments(role))
                    .build());
        }
        return result;
    }

    private static List<DocumentData> defaultDocuments(ParticipantRole role) {
        return switch (role) {
            case GUARANTOR -> List.of(
                    DocumentData.builder().type("Income").fileName("Income.pdf").build(),
                    DocumentData.builder().type("INN").fileName("INN.pdf").build(),
                    DocumentData.builder().type("Passport").fileName("Passport.pdf").build());
            case PLEDGER -> List.of(
                    DocumentData.builder().type("FATCA").fileName("FATCA.pdf").build());
            case BORROWER -> List.of();
        };
    }
}