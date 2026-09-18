package com.creatio.autotest.flows.retail.collateral;

import com.creatio.autotest.components.collateral.*;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.testdata.models.CollateralData;
import com.creatio.autotest.testdata.models.CollateralType;
import com.creatio.autotest.testdata.models.VehicleData;
import io.qameta.allure.Step;

public class VehicleCollateralFlow extends BaseCollateralFlow {

    private final CollateralTabsComponent         tabs      = new CollateralTabsComponent();
    private final CollateralGridComponent         grid      = new CollateralGridComponent();
    private final CollateralFormComponent         form      = new CollateralFormComponent();
    private final VehicleCharacteristicsComponent vehicle   = new VehicleCharacteristicsComponent();
    private final CollateralAddressComponent      address   = new CollateralAddressComponent();
    private final CollateralValuationComponent    valuation = new CollateralValuationComponent();
    private final ClickService                    click     = new ClickService();

    @Override
    @Step("Залог: Транспортное средство")
    public void fill(CollateralData data) {
        log.info("Fill vehicle collateral");

        CollateralType type = data.getType();

        tabs.openCollateralTab();

        grid.addCollateral();

        form.selectType(type.getCategory());
        form.selectSubType(type.getSubType());
        form.setName(type.getUiName());
        form.selectCondition(type.getCondition());
        form.selectOwnership(type.getOwnership());

        grid.addCollateralValue();
        grid.waitForPageLoaded();

        form.setName(type.getUiName());

        if (data.getPledger() != null && !data.getPledger().isBlank()) {
            form.selectPledger(data.getPledger());
        }

        vehicle.fillTechnicalData((VehicleData) data.getSpecificData());

        // DEF-COLLATERAL-NAME: характеристики (большой посимвольный ввод) асинхронно
        // перерисовывают карточку и сбрасывают «Название». Закрепляем его СРАЗУ перед
        // адресом — иначе клик «+» (EshPropertyDataAddressDetailAddRecordButtonButton)
        // валидирует карточку: «Поле "Название": Необходимо указать значение» → адрес
        // не открывается, «Тип адреса» не находится.
        form.setName(type.getUiName());

        address.fillPropertyAddress();

        valuation.fillValuation(
                data.getLiquidationValue(),
                data.getMarketValue(),
                data.getCurrency()
        );

        form.close();
        form.setName(type.getUiName());
        form.save();
        form.close();

        click.doubleClickByMarker("Обеспечение.Подтип");
    }
}