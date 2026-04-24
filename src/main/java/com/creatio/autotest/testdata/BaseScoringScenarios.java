package com.creatio.autotest.testdata;

import com.creatio.autotest.testdata.models.BaseScoringData;

public final class BaseScoringScenarios {

    private BaseScoringScenarios() {}

    public static BaseScoringData WITH_DEPENDENTS() {
        return BaseScoringData.builder()
                .maritalStatus("Оиладор (зан)")
                .dependentsCount("2")
                .childrensCount("1")
                .familyCount("3")
                .totalExperience("60")
                .totalExperienceYears("5")
                .totalExperienceMonths("60")
                .lastJobExperienceYears("3")
                .lastJobExperienceMonths("36")
                .build();
    }

    public static BaseScoringData STANDARD_EMPLOYED() {
        return BaseScoringData.builder()
                .maritalStatus("Мучаррад (мард)")
                .dependentsCount("0")
                .totalExperience("36")
                .totalExperienceYears("3")
                .totalExperienceMonths("36")
                .lastJobExperienceYears("2")
                .lastJobExperienceMonths("24")
                .build();
    }
}