package com.creatio.autotest.flows.retail.collateral;

import com.creatio.autotest.components.collateral.*;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.testdata.models.CollateralData;
import com.creatio.autotest.testdata.models.CollateralType;
import com.creatio.autotest.testdata.models.RealEstateData;
import io.qameta.allure.Step;

public class RealEstateCollateralFlow extends BaseCollateralFlow {

    private final CollateralTabsComponent            tabs      = new CollateralTabsComponent();
    private final CollateralGridComponent            grid      = new CollateralGridComponent();
    private final CollateralFormComponent            form      = new CollateralFormComponent();
    private final RealEstateCharacteristicsComponent chars     = new RealEstateCharacteristicsComponent();
    private final CollateralAddressComponent         address   = new CollateralAddressComponent();
    private final CollateralValuationComponent       valuation = new CollateralValuationComponent();
    private final ClickService                       click     = new ClickService();

    @Override
    @Step("Залог: Недвижимость")
    public void fill(CollateralData data) {
        log.info("Fill real estate collateral");

        CollateralType type = data.getType();
        RealEstateData realEstate = (RealEstateData) data.getSpecificData();

        tabs.openCollateralTab();
        grid.addCollateral();
     // grid.openPropertyLookup();

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

        chars.fillCharacteristics(
                realEstate.getLivingArea(),
                realEstate.getBuildYear(),
                realEstate.getRooms(),
                realEstate.getDescription(),
                realEstate.getPropertyType(),
                realEstate.getTotalArea()
        );

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