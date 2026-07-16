package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

/**
 * Данные вкладки «Решение» проекта решения (стадия «Рассмотрение», верификатор)
 * для продуктов сегмента «Микро».
 * <p>
 * В рознице «Вид кредита для Проекта решения» захардкожен в теле
 * {@code ReviewStageUnderwriterFlow}, а «Кредитный лимит» не заполняется вовсе.
 * Для Микро заполняются оба поля, значения подаются из
 * {@code AgroScenarioFactory.agroDecisionData()}.
 */
@Getter
@Builder
public class AgroDecisionData {

    /** Поле «Вид кредита для Проекта решения» (обязательное). */
    private final String creditType;

    /** Поле «Кредитный лимит» — заполняется только в сегменте «Микро». */
    private final String creditLimit;
}