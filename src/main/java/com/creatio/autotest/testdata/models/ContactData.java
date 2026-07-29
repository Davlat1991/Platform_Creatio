package com.creatio.autotest.testdata.models;

import com.creatio.autotest.testdata.models.registration.fields.ClientSegment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContactData {
    private String firstName;
    private String lastName;
    private String middleName;

    /**
     * Сегмент клиента в банке — предустановлен в карточке физ.лица.
     * Определяет, кто ведёт заявку и под какие продукты она доступна.
     * Не заполняется тестом, используется для fail-fast проверки.
     */
    private ClientSegment segment;

    public static ContactData defaultContact() {
        return ContactData.builder()
                .firstName("Фарангис")
                .lastName("Ашурова")
                .middleName("Шарифовна")
                .segment(ClientSegment.RETAIL)
                .build();
    }

    public static ContactData defaultContact2() {
        return ContactData.builder()
                .firstName("Шукрона")
                .lastName("Ашурова")
                .middleName("Махсудовна")
                .segment(ClientSegment.RETAIL)
                .build();
    }

    public static ContactData selfemployeeContact() {
        return ContactData.builder()
                .firstName("Сафарали")
                .lastName("Садуллоев")
                .middleName("Нусратович")
                .segment(ClientSegment.RETAIL)
                .build();
    }

    public static ContactData selfemployeeContact1() {
        return ContactData.builder()
                .firstName("Давлатахмад")
                .lastName("Одинаев")
                .middleName("Сангахмадович")
                .segment(ClientSegment.RETAIL)
                .build();
    }

    public static ContactData selfemployeeContact2() {
        return ContactData.builder()
                .firstName("Фарангис")
                .lastName("Ашурова")
                .middleName("Шарифовна")
                .segment(ClientSegment.RETAIL)
                .build();
    }


    public static ContactData selfemployeeContact3() {
        return ContactData.builder()
                .firstName("Фарангис")
                .lastName("Ашурова")
                .middleName("Шарифовна")
                .segment(ClientSegment.RETAIL)
                .build();
    }

    /**
     * Клиент сегмента «Микро» — физ.лицо, под которое кредитный эксперт
     * может создавать заявки на продукты линейки Микро (в т.ч. Агро).
     * из тестовой системы.
     */
    public static ContactData microContact() {
        return ContactData.builder()
                .firstName("Собирчон")
                .lastName("Абдуллоев")
                .middleName("Абдучабборович")
                .segment(ClientSegment.MICRO)
                .build();
    }
}