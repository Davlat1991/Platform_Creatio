package com.creatio.autotest.flows.collateral;

import com.creatio.autotest.components.collateral.*;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.testdata.models.CollateralData;
import com.creatio.autotest.testdata.models.CollateralType;
import com.creatio.autotest.testdata.models.GoldData;
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

        CollateralType type = data.getType();
        GoldData gold = (GoldData) data.getSpecificData();

        tabs.openCollateralTab();
        grid.addCollateral();
        grid.openPropertyLookup();

        form.selectType(type.getCategory());
        form.selectSubType(type.getSubType());
        form.setName(type.getUiName());
        form.selectOwnership(type.getOwnership());

        valuation.fillGold(
                gold.getName(),
                gold.getPurity(),
                gold.getWeight(),
                gold.getWeightWithoutStones(),
                gold.getAmount(),
                data.getCurrency()
        );

        form.close();
        click.doubleClickByMarker("Обеспечение.Подтип");
    }
}