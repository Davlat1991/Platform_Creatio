package com.creatio.autotest.flows.retail.collateral;

import com.creatio.autotest.components.collateral.*;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.testdata.models.CollateralData;
import com.creatio.autotest.testdata.models.CollateralType;
import io.qameta.allure.Step;

public class CottonCollateralFlow extends BaseCollateralFlow {

    private final CollateralTabsComponent      tabs      = new CollateralTabsComponent();
    private final CollateralGridComponent      grid      = new CollateralGridComponent();
    private final CollateralFormComponent      form      = new CollateralFormComponent();
    private final CottonCharacteristicsComponent cotton  = new CottonCharacteristicsComponent();
    private final CollateralAddressComponent   address   = new CollateralAddressComponent();
    private final CollateralValuationComponent valuation = new CollateralValuationComponent();
    private final ClickService click    = new ClickService();

    @Override
    @Step("Залог: Хлопок")
    public void fill(CollateralData data) {
        log.info("Fill cotton collateral");

        CollateralType type = data.getType();

        tabs.openCollateralTab();
        grid.addCollateral();
        grid.openPropertyLookup();

        form.selectType(type.getCategory());
        form.selectSubType(type.getSubType());
        form.setName(type.getUiName());
        form.selectOwnership(type.getOwnership());

        cotton.fillTechnicalData();
        address.fillPropertyAddress();

        valuation.fillValuation(
                data.getLiquidationValue(),
                data.getMarketValue(),
                data.getCurrency()
        );

        form.save();
        form.close();
        click.doubleClickByMarker("Обеспечение.Подтип");
    }
}