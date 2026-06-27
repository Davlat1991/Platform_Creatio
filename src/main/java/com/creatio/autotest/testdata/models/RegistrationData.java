package com.creatio.autotest.testdata.models;

import com.creatio.autotest.testdata.models.registration.ClientProfileData;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegistrationData {

    private String region;
    private String infoSource;
    private RegistrationIncomeExpensesData incomeExpenses;
    private BaseScoringData scoringData;
    private ClientProfileData clientProfile;
}