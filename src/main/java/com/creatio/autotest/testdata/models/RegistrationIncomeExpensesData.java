package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegistrationIncomeExpensesData {
    private IncomeData  income;
    private ExpenseData expense;
}