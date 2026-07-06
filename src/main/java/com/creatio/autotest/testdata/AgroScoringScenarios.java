package com.creatio.autotest.testdata;

import com.creatio.autotest.testdata.models.AgroScoringData;

/**
 * Типовые наборы {@link AgroScoringData} для сегмента «Микро».
 * Паттерн — по аналогии с {@link BaseScoringScenarios}.
 * <p>
 * TODO: значения предположительные (типовой набор для сельхоз-сегмента),
 * требуют сверки с реальными полями/значениями Creatio на live DOM.
 */
public final class AgroScoringScenarios {

    private AgroScoringScenarios() {}

    public static AgroScoringData MICRO_CROP_FARMING() {
        return AgroScoringData.builder()
                .farmType("Дехканское (фермерское) хозяйство")
                .farmName("ДХ Навобод")
                .landOwnershipForm("Пожизненное наследуемое владение")
                .landAreaValue("1.5")
                .landAreaUnit("га")
                .cropType("Растениеводство")
                .harvestSeasonality("Сезонный")
                .yearsInFarming("6")
                .build();
    }

    public static AgroScoringData MICRO_LIVESTOCK() {
        return AgroScoringData.builder()
                .farmType("Личное подсобное хозяйство")
                .farmName(null)
                .landOwnershipForm("Аренда")
                .landAreaValue("0.5")
                .landAreaUnit("га")
                .cropType("Животноводство")
                .harvestSeasonality("Круглогодичный")
                .yearsInFarming("3")
                .build();
    }
}