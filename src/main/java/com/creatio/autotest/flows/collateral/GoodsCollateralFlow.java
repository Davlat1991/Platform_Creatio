package com.creatio.autotest.flows.collateral;

import com.creatio.autotest.components.collateral.*;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.testdata.models.CollateralData;
import io.qameta.allure.Step;

public class GoodsCollateralFlow extends BaseCollateralFlow {

    private final CollateralTabsComponent      tabs      = new CollateralTabsComponent();
    private final CollateralGridComponent      grid      = new CollateralGridComponent();
    private final CollateralFormComponent      form      = new CollateralFormComponent();
    private final CollateralAddressComponent   address   = new CollateralAddressComponent();
    private final CollateralValuationComponent valuation = new CollateralValuationComponent();
    private final ClickService click    = new ClickService();

    @Override
    @Step("Залог: Товары в обороте")
    public void fill(CollateralData data) {
        log.info("Fill goods collateral");

        tabs.openCollateralTab();
        grid.addCollateral();
        grid.openPropertyLookup();

        form.selectType("Движимое имущество");
        form.selectSubType("Гарави амволи дар муомилотбуда");
        form.setName("Товары в обороте и переработке");
        form.selectOwnership("Собственный");

        valuation.fillObjects(
                "Кухонный набор",
                "400000",
                data.getCurrency(),
                "4",
                "Импортный набор"
        );

        address.fillPropertyAddress();
        form.close();
        click.doubleClickByMarker("Обеспечение.Подтип");
    }
}