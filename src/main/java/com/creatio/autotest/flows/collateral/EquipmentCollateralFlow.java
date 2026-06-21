package com.creatio.autotest.flows.collateral;

import com.creatio.autotest.components.collateral.*;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.testdata.models.CollateralData;
import io.qameta.allure.Step;

public class EquipmentCollateralFlow extends BaseCollateralFlow {

    private final CollateralTabsComponent      tabs      = new CollateralTabsComponent();
    private final CollateralGridComponent      grid      = new CollateralGridComponent();
    private final CollateralFormComponent      form      = new CollateralFormComponent();
    private final CollateralAddressComponent   address   = new CollateralAddressComponent();
    private final CollateralValuationComponent valuation = new CollateralValuationComponent();
    private final ClickService click    = new ClickService();

    @Override
    @Step("Залог: Оборудование")
    public void fill(CollateralData data) {
        log.info("Fill equipment collateral");

        tabs.openCollateralTab();
        grid.addCollateral();
        grid.openPropertyLookup();

        form.selectType("Движимое имущество");
        form.selectSubType("Гарави тачхизот");
        form.setName("Залог оборудования");
        form.selectOwnership("Третьего лица");//Собственный
        form.selectPledger("Тестов тест тестович");

        valuation.fillEquipment(
                "Прицеп",
                "80000",
                data.getCurrency(),
                "БЕЛОРУССИЯ",
                "2025",
                "Промышленное оборудование",
                "Германский прицеп"
        );

        address.fillPropertyAddress();
        form.close();
        click.doubleClickByMarker("Обеспечение.Подтип");
    }
}