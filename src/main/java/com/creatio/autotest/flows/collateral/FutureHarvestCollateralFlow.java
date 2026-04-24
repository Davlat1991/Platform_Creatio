package com.creatio.autotest.flows.collateral;

import com.creatio.autotest.components.collateral.*;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.testdata.models.CollateralData;
import io.qameta.allure.Step;

public class FutureHarvestCollateralFlow extends BaseCollateralFlow {

    private final CollateralTabsComponent      tabs      = new CollateralTabsComponent();
    private final CollateralGridComponent      grid      = new CollateralGridComponent();
    private final CollateralFormComponent      form      = new CollateralFormComponent();
    private final CollateralAddressComponent   address   = new CollateralAddressComponent();
    private final CollateralValuationComponent valuation = new CollateralValuationComponent();
    private final ClickService click    = new ClickService();

    @Override
    @Step("Залог: Будущий урожай")
    public void fill(CollateralData data) {
        log.info("Fill future harvest collateral");

        tabs.openCollateralTab();
        grid.addCollateral();
        grid.openPropertyLookup();

        form.selectType("Движимое имущество");
        form.selectSubType("Гарави хосили оянда");
        form.setName("Будущий урожай");
        form.selectOwnership("Собственный");

        valuation.fillObjects(
                "Хосили картошка",
                "110000",
                data.getCurrency(),
                "4",
                "Ранний сорт картошки"
        );

        address.fillPropertyAddress();
        form.close();
        click.doubleClickByMarker("Обеспечение.Подтип");
    }
}