package com.creatio.autotest.components.collateral;

import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BaseComponent;
import io.qameta.allure.Step;

public class VehicleCharacteristicsComponent extends BaseComponent {

    private final LookupComponent lookup = new LookupComponent();

    @Step("Заполнение характеристик транспортного средства")
    public void fillTechnicalData() {
        log.info("Fill vehicle technical data");

        lookup
                .selectByLabel("Техпаспорт",                       "123456789")
                .selectByLabel("Орган выдавший техпаспорт",        "ШВКД дар ш.Хучанд")
                .selectByLabel("Дата регистрации",                 "01.01.2020")
                .selectByLabel("Регистрационный номер",            "777")
                .selectByLabel("VIN номер",                        "123")
                .select("Марка ТС",                                "BMW")
                .selectByLabel("Модель",                           "М5")
                .select("Класс транспортного средства",            "Сабукрав")
                .selectByLabel("Год изготовления",                 "01.01.2020")
                .selectByLabel("Номер кузова",                     "777")
                .selectByLabel("Номер шасси",                      "555")
                .selectByLabel("Цвет",                             "Белый")
                .select("Тип кузова",                              "Седан")
                .select("Тип двигателя",                           "Бензин+Газ")
                .selectByLabel("Объем двигателя, куб. см.",        "300")
                .selectByLabel("Мощность двигателя, кВт/л.с.",     "1000")
                .selectByLabel("Грузоподъёмность, кг",             "500")
                .selectByLabel("Вес, кг",                          "1500")
                .selectByLabel("Описание",                         "Классная машина");
    }
}