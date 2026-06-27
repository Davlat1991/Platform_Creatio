package com.creatio.autotest.testdata.models.registration;

import lombok.Builder;
import lombok.Getter;

/**
 * Данные блока "Деятельность физ. лица".
 * Поля каскадные: sector -> segment -> subSegment.
 * market необязателен (для некоторых типов продаж отсутствует).
 */
@Getter
@Builder
public class ActivityData {

    private String sector;       // Сектор
    private String segment;      // Сегмент
    private String subSegment;   // Подсегмент
    private String salesType;    // Тип продаж
    private String market;       // Рынок (опционально)
}