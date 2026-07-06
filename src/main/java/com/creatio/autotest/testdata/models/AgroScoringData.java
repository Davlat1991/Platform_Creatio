package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

/**
 * Агро/Микро-специфичные поля, дополняющие {@link BaseScoringData} и
 * {@link com.creatio.autotest.testdata.models.registration.ActivityData}
 * для сегмента «Микро» продукта «Агро».
 * <p>
 * Сознательно НЕ наследует {@link BaseScoringData} и не встраивается в
 * ActivityData/CareerData: сегмент «Микро» — лишь первая часть будущей
 * линейки Агро-сегментов, и изоляция в отдельную модель убережёт розничные
 * (retail) модели от агро-специфичных полей и наоборот.
 * <p>
 * TODO: значения полей и точный список — предположительный типовой набор
 * для сельхоз-сегмента. Требует сверки с реальными полями Creatio при
 * первом live-запуске AgroRegistrationGeneralInfoFlow / AgroRegistrationAdditionalInfoFlow
 * (Sprint 1, подзадачи 5-6). Поля могут быть переименованы/дополнены по DOM.
 */
@Getter
@Builder
public class AgroScoringData {

    // ── Хозяйство ──────────────────────────
    private String farmType;          // Тип хозяйства (ЛПХ, дехканское/фермерское, кооператив...)
    private String farmName;          // Наименование хозяйства (если применимо)

    // ── Земля ──────────────────────────────
    private String landOwnershipForm; // Форма собственности на землю (частная, аренда, пожизненное наследуемое владение...)
    private String landAreaValue;     // Площадь земли — числовое значение
    private String landAreaUnit;      // Единица измерения площади (га, сотка...)

    // ── Культура / направление ────────────
    private String cropType;          // Основная культура / направление (хлопок, зерновые, животноводство...)
    private String harvestSeasonality; // Сезонность урожая / цикла производства

    // ── Дополнительно ──────────────────────
    private String yearsInFarming;    // Стаж ведения хозяйства (лет)
}