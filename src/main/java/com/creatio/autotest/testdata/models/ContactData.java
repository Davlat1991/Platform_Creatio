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

    public static ContactData selfemployeeContact() {
        return ContactData.builder()
                .firstName("Сафарали")
                .lastName("Садуллоев")
                .middleName("Нусратович")
                .build();
    }

    public static ContactData selfemployeeContact1() {
        return ContactData.builder()
                .firstName("Фарангис")
                .lastName("Ашурова")
                .middleName("Шарифовна")
                .build();
    }
}