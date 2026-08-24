package com.creatio.autotest.flows.retail.collateral;

import com.creatio.autotest.components.collateral.*;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.testdata.models.CollateralData;
import com.creatio.autotest.testdata.models.CollateralType;
import io.qameta.allure.Step;

public class CashDepositCollateralFlow extends BaseCollateralFlow {

    private final CollateralTabsComponent      tabs      = new CollateralTabsComponent();
    private final CollateralGridComponent      grid      = new CollateralGridComponent();
    private final CollateralFormComponent      form      = new CollateralFormComponent();
    private final CollateralValuationComponent valuation = new CollateralValuationComponent();
    private final ClickService                 click     = new ClickService();

    @Override
    @Step("Залог: Денежный вклад")
    public void fill(CollateralData data) {
        log.info("Fill cash deposit collateral");

        CollateralType type = data.getType();

        tabs.openCollateralTab();
        grid.addCollateral();
       // grid.openPropertyLookup();

        form.selectType(type.getCategory());
        form.selectSubType(type.getSubType());
        form.setName(type.getUiName());
        form.selectOwnership(type.getOwnership());

        if (data.getPledger() != null && !data.getPledger().isBlank()) {
            form.selectPledger(data.getPledger());
        }

        valuation.fillCashDeposit(
                data.getMarketValue(),
                data.getCurrency()
        );

        form.save();
        click.doubleClickByMarker("Залоговая стоимость");
        form.close();
        click.doubleClickByMarker("Обеспечение.Подтип");
    }
}