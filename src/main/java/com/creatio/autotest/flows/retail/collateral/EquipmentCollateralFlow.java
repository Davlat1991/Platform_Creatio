package com.creatio.autotest.flows.retail.collateral;

import com.creatio.autotest.components.collateral.*;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.testdata.models.CollateralData;
import com.creatio.autotest.testdata.models.CollateralType;
import com.creatio.autotest.testdata.models.EquipmentData;
import io.qameta.allure.Step;

public class EquipmentCollateralFlow extends BaseCollateralFlow {

    private final CollateralTabsComponent      tabs      = new CollateralTabsComponent();
    private final CollateralGridComponent      grid      = new CollateralGridComponent();
    private final CollateralFormComponent      form      = new CollateralFormComponent();
    private final CollateralAddressComponent   address   = new CollateralAddressComponent();
    private final CollateralValuationComponent valuation = new CollateralValuationComponent();
    private final ClickService                 click     = new ClickService();

    @Override
    @Step("Залог: Оборудование")
    public void fill(CollateralData data) {
        log.info("Fill equipment collateral");

        CollateralType type = data.getType();
        EquipmentData equipment = (EquipmentData) data.getSpecificData();

        tabs.openCollateralTab();
        grid.addCollateral();
      //  grid.openPropertyLookup();

        form.selectType(type.getCategory());
        form.selectSubType(type.getSubType());
        form.setName(type.getUiName());
        form.selectOwnership(type.getOwnership());

        valuation.fillEquipment(
                equipment.getName(),
                equipment.getSum(),
                data.getCurrency(),
                equipment.getCountry(),
                equipment.getYear(),
                equipment.getEquipmentType(),
                equipment.getDescription()
        );

        address.fillPropertyAddress();
        form.close();
        click.doubleClickByMarker("Обеспечение.Подтип");
    }
}