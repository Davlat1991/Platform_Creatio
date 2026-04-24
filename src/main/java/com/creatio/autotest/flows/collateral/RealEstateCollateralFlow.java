package com.creatio.autotest.flows.collateral;

import com.creatio.autotest.components.collateral.*;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.testdata.models.CollateralData;
import com.creatio.autotest.testdata.models.RealEstateData;
import io.qameta.allure.Step;

public class RealEstateCollateralFlow extends BaseCollateralFlow {

    private final CollateralTabsComponent         tabs      = new CollateralTabsComponent();
    private final CollateralGridComponent         grid      = new CollateralGridComponent();
    private final CollateralFormComponent         form      = new CollateralFormComponent();
    private final RealEstateCharacteristicsComponent chars  = new RealEstateCharacteristicsComponent();
    private final CollateralAddressComponent      address   = new CollateralAddressComponent();
    private final CollateralValuationComponent    valuation = new CollateralValuationComponent();
    private final ClickService click    = new ClickService();

    @Override
    @Step("Залог: Недвижимость")
    public void fill(CollateralData data) {
        log.info("Fill real estate collateral");

        RealEstateData realEstate = (RealEstateData) data.getSpecificData();

        tabs.openCollateralTab();
        grid.addCollateral();
        grid.openPropertyLookup();

        form.selectType("Недвижимое имущество");
        form.selectSubType("Гарави амволи гайриманкул");
        form.setName("Залог недвижимого имущества");
        form.selectCondition("Новое");
        form.selectOwnership("Собственный");

        grid.addCollateralValue();
        grid.waitForPageLoaded();

        form.setName("Залог недвижимого имущества");
        form.selectPledger("Чаборова Дилафруз Кобилчоновна");

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
                data.getMarketValue(),
                data.getLiquidationValue(),
                data.getCurrency()
        );

        form.close();
        form.setName("Залог недвижимого имущества");
        form.save();
        form.close();

        click.doubleClickByMarker("Обеспечение.Подтип");
    }
}