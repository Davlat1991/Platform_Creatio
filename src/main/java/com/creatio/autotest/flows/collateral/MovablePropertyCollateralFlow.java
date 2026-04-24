package com.creatio.autotest.flows.collateral;

import com.creatio.autotest.components.collateral.*;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.testdata.models.CollateralData;
import io.qameta.allure.Step;

public class MovablePropertyCollateralFlow extends BaseCollateralFlow {

    private final CollateralTabsComponent      tabs      = new CollateralTabsComponent();
    private final CollateralGridComponent      grid      = new CollateralGridComponent();
    private final CollateralFormComponent      form      = new CollateralFormComponent();
    private final CollateralAddressComponent   address   = new CollateralAddressComponent();
    private final CollateralValuationComponent valuation = new CollateralValuationComponent();
    private final ClickService click    = new ClickService();

    @Override
    @Step("Залог: Движимое имущество")
    public void fill(CollateralData data) {
        log.info("Fill movable property collateral");

        tabs.openCollateralTab();
        grid.addCollateral();
        grid.openPropertyLookup();

        form.selectType("Движимое имущество");
        form.selectSubType("Гарави амволи манкул");
        form.setName("Залог движимого имущества");
        form.selectOwnership("Собственный");

        valuation.fillObjects(
                "Производственное оборудование",
                "70000",
                data.getCurrency(),
                "2",
                "Новое оборудование"
        );

        address.fillPropertyAddress();
        form.close();
        click.doubleClickByMarker("Обеспечение.Подтип");
    }
}