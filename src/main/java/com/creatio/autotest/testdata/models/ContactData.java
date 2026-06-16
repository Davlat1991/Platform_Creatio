package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContactData {
    private String firstName;
    private String lastName;
    private String middleName;

    public static ContactData defaultContact() {
        return ContactData.builder()
                .firstName("Фарангис")
                .lastName("Ашурова")
                .middleName("Шарифовна")
                .build();
    }
}