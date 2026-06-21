package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CollateralData {

    private CollateralType type;
    private String         liquidationValue;
    private String         marketValue;
    private String         currency;
    private Object         specificData;
}