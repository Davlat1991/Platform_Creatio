package com.creatio.autotest.flows.collateral;

import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.testdata.models.CollateralData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.$x;

public abstract class BaseCollateralFlow {

    protected final Logger        log      = LoggerFactory.getLogger(getClass());
    protected final LookupComponent lookup  = new LookupComponent();
    protected final BasePage      basePage  = new BasePage();
    protected final ClickService  click     = new ClickService();

    // ══════════════════════════════════════
    // CONTRACT
    // ══════════════════════════════════════

    public abstract void fill(CollateralData data);

    // ══════════════════════════════════════
    // COMMON METHODS
    // ══════════════════════════════════════

    protected void setMarketValue(String value, String currency) {
        lookup.select("Рыночная стоимость", value);
        lookup.select("Валюта",             currency);
    }

    protected void setLiquidationValue(String value) {
        lookup.select("Ликвидационная стоимость", value);
    }

    protected void saveAndClose() {
        click.clickByName("Сохранить");
        click.clickByName("Закрыть");
    }

    protected void setFieldByMarker(String marker, String value) {
        $x("//div[@data-item-marker='" + marker + "']/input")
                .setValue(value);
    }
}