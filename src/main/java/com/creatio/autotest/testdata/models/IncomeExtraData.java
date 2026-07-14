package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

/**
 * Дополнительные данные вкладки «Доходы и расходы» (уровень риска,
 * подтверждение дохода) для сегмента «Микро».
 * Вынесены из флоу, чтобы {@code AgroRegistrationIncomeExpensesFlow} не
 * содержал хардкода.
 */
@Getter
@Builder
public class IncomeExtraData {

    private String riskLevel;              // Уровень риска
    private String incomeConfirmation;     // Клиент предоставил подтверждение дохода
    private String confirmationDocType;    // Тип документа подтверждения дохода
}