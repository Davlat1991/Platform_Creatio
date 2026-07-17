package com.creatio.autotest.flows.micro.collateral;

import com.creatio.autotest.components.collateral.CollateralAddressComponent;
import com.creatio.autotest.components.collateral.CollateralFormComponent;
import com.creatio.autotest.components.collateral.CollateralGridComponent;
import com.creatio.autotest.components.collateral.CollateralTabsComponent;
import com.creatio.autotest.components.collateral.CollateralValuationComponent;
import com.creatio.autotest.testdata.models.CollateralData;
import com.creatio.autotest.testdata.models.CollateralType;
import com.creatio.autotest.testdata.models.SimpleObjectData;
import io.qameta.allure.Step;

/**
 * Залог «Будущий урожай» (Агро/Микро).
 * <p>
 * Изолированная Микро-копия розничного {@code FutureHarvestCollateralFlow}:
 * шаги идентичны, значения объекта залога читаются из
 * {@code data.getSpecificData()} (как и в рознице — хардкода там не было).
 */
public class AgroFutureHarvestCollateralFlow extends AgroBaseCollateralFlow {

    private final CollateralTabsComponent      tabs      = new CollateralTabsComponent();
    private final CollateralFormComponent      form      = new CollateralFormComponent();
    private final CollateralAddressComponent   address   = new CollateralAddressComponent();
    private final CollateralValuationComponent valuation = new CollateralValuationComponent();

    @Override
    @Step("Агро: залог «Будущий урожай»")
    public void fill(CollateralData data) {
        log.info("Agro: fill future harvest collateral");

        CollateralType   type   = data.getType();
        SimpleObjectData object = (SimpleObjectData) data.getSpecificData();

        tabs.openCollateralTab();
        grid.addCollateral();
        openPropertyLookupStable();


        form.selectType(type.getCategory());
        form.selectSubType(type.getSubType());
        form.setName(type.getUiName());
        form.selectOwnership(type.getOwnership());

        valuation.fillObjects(
                object.getName(),
                object.getSum(),
                data.getCurrency(),
                object.getAmount(),
                object.getDescription()
        );

        address.fillPropertyAddress();
        form.close();

        // После close() система авто-переключает на «Выбранный продукт» —
        // возвращаемся на «Обеспечение», иначе маркер подтипа не найти.
        tabs.openCollateralTab();

        click.doubleClickByMarker("Обеспечение.Подтип");

        log.info("Agro: future harvest collateral filled");
    }
}