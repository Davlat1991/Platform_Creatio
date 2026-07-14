package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

/**
 * Данные документов заёмщика (стадия «Оформление», вкладка «Документы»)
 * для продуктов сегмента «Микро».
 * Вынесены из флоу, чтобы {@code AgroRegistrationDocumentsFlow} не содержал
 * хардкода (правило проекта «no hardcoded values»).
 */
@Getter
@Builder
public class BorrowerDocumentData {

    private String birthCountry;    // Страна рождения
    private String issueDate;       // Дата выдачи
    private String validUntil;      // Действителен до / Действует до
    private String documentType;    // Тип документа (data-item-marker)
}