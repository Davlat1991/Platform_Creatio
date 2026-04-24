package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BaseScoringData {
    private String maritalStatus;
    private String dependentsCount;
    private String childrensCount;
    private String familyCount;
    private String totalExperience;
    private String totalExperienceYears;
    private String totalExperienceMonths;
    private String lastJobExperienceYears;
    private String lastJobExperienceMonths;
}