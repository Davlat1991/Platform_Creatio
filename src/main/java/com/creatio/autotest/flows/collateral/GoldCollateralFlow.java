package com.creatio.autotest.flows.collateral;

import com.creatio.autotest.components.collateral.*;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.testdata.models.CollateralData;
import io.qameta.allure.Step;

public class GoldCollateralFlow extends BaseCollateralFlow {

    private final CollateralTabsComponent      tabs      = new CollateralTabsComponent();
    private final CollateralGridComponent      grid      = new CollateralGridComponent();
    private final CollateralFormComponent      form      = new CollateralFormComponent();
    private final CollateralValuationComponent valuation = new CollateralValuationComponent();
    private final ClickService click    = new ClickService();

    @Override
    @Step("Залог: Золотые изделия")
    public void fill(CollateralData data) {
        log.info("Fill gold collateral");

        tabs.openCollateralTab();
        grid.addCollateral();
        grid.openPropertyLookup();

        form.selectType("Движимое имущество");
        form.selectSubType("Гарави дорои");
        form.setName("Золотые изделия");
        form.selectOwnership("Собственный");

        valuation.fillGold(
                "Золотое украшение",
                "999",
                "50",
                "40",
                "50000",
                data.getCurrency()
        );

        form.close();
        click.doubleClickByMarker("Обеспечение.Подтип");
    }
}