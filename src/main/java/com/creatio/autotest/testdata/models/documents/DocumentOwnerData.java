package com.creatio.autotest.testdata.models.documents;

import com.creatio.autotest.testdata.models.ParticipantRole;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Документы одного владельца (заёмщик / поручитель / залогодатель).
 *
 * Заменяет хрупкий offset-маппинг: вместо "заёмщик=0, поручитель=3" —
 * явный список документов на каждого владельца. Стадия итерирует по владельцам.
 */
@Getter
@Builder
public class DocumentOwnerData {

    private ParticipantRole owner;
    private List<DocumentData> documents;
}