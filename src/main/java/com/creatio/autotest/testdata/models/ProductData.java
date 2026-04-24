package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductData {
    private String productType;
    private String creditPurpose;
    private String amount;
    private String term;
    private String currency;
}