package com.creatio.autotest.flows.micro.collateral;

import com.creatio.autotest.components.collateral.CollateralAddressComponent;
import com.creatio.autotest.components.collateral.CollateralFormComponent;
import com.creatio.autotest.components.collateral.CollateralGridComponent;
import com.creatio.autotest.components.collateral.CollateralTabsComponent;
import com.creatio.autotest.components.collateral.CollateralValuationComponent;
import com.creatio.autotest.components.collateral.CottonCharacteristicsComponent;
import com.creatio.autotest.testdata.models.CollateralData;
import com.creatio.autotest.testdata.models.CollateralType;
import com.creatio.autotest.testdata.models.SimpleObjectData;
import io.qameta.allure.Step;

/**
 * Залог «Хлопок» (Агро/Микро).
 * <p>
 * Изолированная Микро-копия розничного {@code CottonCollateralFlow}: шаги
 * идентичны. Отличие — характеристики хлопка заполняются из
 * {@code data.getSpecificData()} через параметризованную перегрузку
 * {@code CottonCharacteristicsComponent.fillTechnicalData(SimpleObjectData)},
 * а не захардкоженными значениями (розничный no-arg метод не тронут).
 */
public class AgroCottonCollateralFlow extends AgroBaseCollateralFlow {

    private final CollateralTabsComponent        tabs      = new CollateralTabsComponent();
    private final CollateralGridComponent        grid      = new CollateralGridComponent();
    private final CollateralFormComponent        form      = new CollateralFormComponent();
    private final CottonCharacteristicsComponent cotton    = new CottonCharacteristicsComponent();
    private final CollateralAddressComponent     address   = new CollateralAddressComponent();
    private final CollateralValuationComponent   valuation = new CollateralValuationComponent();

    @Override
    @Step("Агро: залог «Хлопок»")
    public void fill(CollateralData data) {
        log.info("Agro: fill cotton collateral");

        CollateralType   type   = data.getType();
        SimpleObjectData object = (SimpleObjectData) data.getSpecificData();

        tabs.openCollateralTab();
        grid.addCollateral();
        grid.openPropertyLookup();

        form.selectType(type.getCategory());
        form.selectSubType(type.getSubType());
        form.setName(type.getUiName());
        form.selectOwnership(type.getOwnership());

        cotton.fillTechnicalData(object);
        address.fillPropertyAddress();

        valuation.fillValuation(
                data.getLiquidationValue(),
                data.getMarketValue(),
                data.getCurrency()
        );

        form.save();
        form.close();

        // После close() система авто-переключает на «Выбранный продукт» —
        // возвращаемся на «Обеспечение», иначе маркер подтипа не найти.
        tabs.openCollateralTab();

        click.doubleClickByMarker("Обеспечение.Подтип");

        log.info("Agro: cotton collateral filled");
    }
}