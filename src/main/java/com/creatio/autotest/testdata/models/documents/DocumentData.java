package com.creatio.autotest.testdata.models.documents;

import lombok.Builder;
import lombok.Getter;

/**
 * Один документ для загрузки.
 *
 *  - type        — логический тип (Income / Passport / INN) — ключ для маппинга
 *  - fileName    — имя файла в src/test/resources/files/
 *  - detail      — деталь Creatio, в которую грузится документ
 *                  ("Финансовое досье" / "Досье клиента")
 */
@Getter
@Builder
public class DocumentData {

    private String type;
    private String fileName;
    private String detail;
}