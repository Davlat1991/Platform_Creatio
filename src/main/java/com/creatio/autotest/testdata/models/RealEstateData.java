package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RealEstateData {
    private String livingArea;
    private String buildYear;
    private String rooms;
    private String description;
    private String propertyType;
    private String totalArea;
}