package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExpenseData {
    private String type;
    private String frequency;
    private String amount;
}