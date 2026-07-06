package com.creatio.autotest.flows.collateral;

import com.creatio.autotest.components.collateral.*;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.testdata.models.CollateralData;
import com.creatio.autotest.testdata.models.CollateralType;
import io.qameta.allure.Step;

public class VehicleCollateralFlow extends BaseCollateralFlow {

    private final CollateralTabsComponent      tabs      = new CollateralTabsComponent();
    private final CollateralGridComponent      grid      = new CollateralGridComponent();
    private final CollateralFormComponent      form      = new CollateralFormComponent();
    private final VehicleCharacteristicsComponent vehicle = new VehicleCharacteristicsComponent();
    private final CollateralAddressComponent   address   = new CollateralAddressComponent();
    private final CollateralValuationComponent valuation = new CollateralValuationComponent();
    private final ClickService click    = new ClickService();

    @Override
    @Step("Залог: Транспортное средство")
    public void fill(CollateralData data) {
        log.info("Fill vehicle collateral");

        CollateralType type = data.getType();

        tabs.openCollateralTab();

        grid.addCollateral();
        grid.openPropertyLookup();

        form.selectType(type.getCategory());
        form.selectSubType(type.getSubType());
        form.setName(type.getUiName());
        form.selectCondition(type.getCondition());
        form.selectOwnership(type.getOwnership());

        grid.addCollateralValue();
        grid.waitForPageLoaded();

        form.setName(type.getUiName());
        // TODO: залогодатель сейчас захардкожен, параметризация отложена
        form.selectPledger("Садуллоев Сафарали Нусратович");

        vehicle.fillTechnicalData();
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