package com.creatio.autotest.testdata.models;

import com.creatio.autotest.testdata.models.registration.fields.ClientSegment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContactData {
    private String lastName;
    private String firstName;
    private String middleName;

    /**
     * Сегмент клиента в банке — предустановлен в карточке физ.лица.
     * Определяет, кто ведёт заявку и под какие продукты она доступна.
     * Не заполняется тестом, используется для fail-fast проверки.
     */
    private ClientSegment segment;

    public static ContactData defaultContact() {
        return ContactData.builder()
                .lastName("Ашурова")
                .firstName("Фарангис")
                .middleName("Шарифовна")
                .segment(ClientSegment.RETAIL)
                .build();
    }

    public static ContactData defaultContact2() {
        return ContactData.builder()
                .lastName("Ашурова")
                .firstName("Шукрона")
                .middleName("Махсудовна")
                .segment(ClientSegment.RETAIL)
                .build();
    }

    public static ContactData selfemployeeContact() {
        return ContactData.builder()
                .lastName("Садуллоев")
                .firstName("Сафарали")
                .middleName("Нусратович")
                .segment(ClientSegment.RETAIL)
                .build();
    }

    public static ContactData selfemployeeContact1() {
        return ContactData.builder()
                .lastName("Одинаев")
                .firstName("Давлатахмад")
                .middleName("Сангахмадович")
                .segment(ClientSegment.RETAIL)
                .build();
    }

    public static ContactData selfemployeeContact2() {
        return ContactData.builder()
                .lastName("Ашурова")
                .firstName("Фарангис")
                .middleName("Шарифовна")
                .segment(ClientSegment.RETAIL)
                .build();
    }


    public static ContactData selfemployeeContact3() {
        return ContactData.builder()
                .lastName("Ашурова")
                .firstName("Фарангис")
                .middleName("Шарифовна")
                .segment(ClientSegment.RETAIL)
                .build();
    }

    /**
     * Клиент сегмента «Микро» — физ.лицо, под которое кредитный эксперт
     * может создавать заявки на продукты линейки Микро (в т.ч. Агро).
     * из тестовой системы.
     */
    public static ContactData microContact1() {
        return ContactData.builder()
                .lastName("Абдуллоев")
                .firstName("Собирчон")
                .middleName("Абдучабборович")
                .segment(ClientSegment.MICRO)
                .build();
    }

    /**
     * Клиент сегмента «Микро» — физ.лицо, под которое кредитный эксперт
     * может создавать заявки на продукты линейки Микро (в т.ч. Агро).
     * из тестовой системы.
     */
    public static ContactData microContact() {
        return ContactData.builder()
                .lastName("Абдурахмонов")
                .firstName("Исроилхуча")
                .middleName("Исмоилхучаевич")
                .segment(ClientSegment.MICRO)
                .build();
    }
}