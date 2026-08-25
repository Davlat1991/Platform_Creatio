package com.creatio.autotest.components.collateral;

import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BaseComponent;
import com.creatio.autotest.testdata.models.VehicleData;
import io.qameta.allure.Step;

public class VehicleCharacteristicsComponent extends BaseComponent {

    private final LookupComponent lookup = new LookupComponent();

    @Step("Заполнение характеристик транспортного средства")
    public void fillTechnicalData(VehicleData data) {
        log.info("Fill vehicle technical data");

        lookup
                .selectByLabel("Техпаспорт",                    data.getTechPassport())
                .selectByLabel("Орган выдавший техпаспорт",     data.getTechPassportIssuer())
                .selectByLabel("Дата регистрации",              data.getRegistrationDate())
                .selectByLabel("Регистрационный номер",         data.getRegistrationNumber())
                .selectByLabel("VIN номер",                     data.getVin())
                .select("Марка ТС",                             data.getBrand())
                .selectByLabel("Модель",                        data.getModel())
                .select("Класс транспортного средства",         data.getVehicleClass())
                .selectByLabel("Год изготовления",              data.getManufactureYear())
                .selectByLabel("Номер кузова",                  data.getBodyNumber())
                .selectByLabel("Номер шасси",                   data.getChassisNumber())
                .selectByLabel("Цвет",                          data.getColor())
                .select("Тип кузова",                           data.getBodyType())
                .select("Тип двигателя",                        data.getEngineType())
                .selectByLabel("Объем двигателя, куб. см.",     data.getEngineVolume())
                .selectByLabel("Мощность двигателя, кВт/л.с.",  data.getEnginePower())
                .selectByLabel("Грузоподъёмность, кг",          data.getLoadCapacity())
                .selectByLabel("Вес, кг",                       data.getWeight())
                .selectByLabel("Описание",                      data.getDescription());
    }
}