package com.creatio.autotest.components.collateral;

import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.DomActions;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BaseComponent;
import com.creatio.autotest.core.services.ClickService;
import io.qameta.allure.Step;

public class CollateralValuationComponent extends BaseComponent {

    private final ButtonsComponent buttons  = new ButtonsComponent();
    private final DomActions       dom      = new DomActions();
    private final LookupComponent  lookup   = new LookupComponent();
    private final ClickService     click    = new ClickService();

    // ══════════════════════════════════════
    // VEHICLE / COTTON / REAL ESTATE
    // ══════════════════════════════════════

    @Step("Оценка залога: {assessed} / {market} {currency}")
    public void fillValuation(String assessed, String market, String currency) {
        log.info("Fill valuation: assessed='{}' market='{}'", assessed, market);

        buttons.clickByNameContains("Оценка");
        click.clickById("PledgeRatingDetailAddRecordButtonButton-imageEl");

        dom.clickDivById("PledgeRatingDetailRatingSumFloatEdit-wrap",  assessed);
        dom.clickDivById("PledgeRatingDetailMarketSumFloatEdit-wrap",  market);

        setCurrency("EshCurrency", currency);
        click.clickByMarker("save");
    }

    // ══════════════════════════════════════
    // GOODS / MOVABLE / ACQUIRED / HARVEST
    // ══════════════════════════════════════

    @Step("Объекты обеспечения: {name}")
    public void fillObjects(String name, String sum, String currency,
                            String amount, String description) {
        log.info("Fill collateral objects: name='{}'", name);

        click.clickById(
                "EshPropertyDataObjectsDetailAddRecordButtonButton-imageEl"
        );

        dom.setGridInputValue(
                "EshPropertyDataObjectsDetailNameTextEdit-el", name
        );
        dom.clickDivById(
                "EshPropertyDataObjectsDetailAmountFloatEdit-wrap", sum
        );

        setCurrency("Currency", currency);

        dom.setGridInputValue(
                "EshPropertyDataObjectsDetailCountIntegerEdit-el", amount
        );
        dom.setGridInputValue(
                "EshPropertyDataObjectsDetailDescriptionTextEdit-el", description
        );

        click.clickByMarker("save");
    }

    // ══════════════════════════════════════
    // GOLD
    // ══════════════════════════════════════

    @Step("Золотые изделия: {name}")
    public void fillGold(String name, String purity, String weight,
                         String weightWithoutStones, String amount,
                         String currency) {
        log.info("Fill gold: name='{}'", name);

        click.clickById(
                "EshGoldCollateralValuesDetailAddRecordButtonButton-imageEl"
        );

        dom.setGridInputValue(
                "EshGoldCollateralValuesDetailNameTextEdit-el", name
        );
        dom.clickDivById(
                "EshGoldCollateralValuesDetailAssayIntegerEdit-wrap", purity
        );
        dom.setGridInputValue(
                "EshGoldCollateralValuesDetailWeightFloatEdit-el", weight
        );
        dom.setGridInputValue(
                "EshGoldCollateralValuesDetailWeightWithoutStonesFloatEdit-el",
                weightWithoutStones
        );
        dom.setGridInputValue(
                "EshGoldCollateralValuesDetailAmountFloatEdit-el", amount
        );

        setCurrency("Currency", currency);
        click.clickByMarker("save");
    }

    // ══════════════════════════════════════
    // EQUIPMENT
    // ══════════════════════════════════════

    @Step("Оборудование: {name}")
    public void fillEquipment(String name, String sum, String currency,
                              String country, String year,
                              String equipmentType, String description) {
        log.info("Fill equipment: name='{}'", name);

        click.clickById(
                "EshPropertyDataEquipmentDetailAddRecordButtonButton-wrapperEl"
        );

        dom.setGridInputValue(
                "EshPropertyDataEquipmentDetailNameTextEdit-el", name
        );
        dom.setGridInputValue(
                "EshPropertyDataEquipmentDetailAmountFloatEdit-el", sum
        );

        setCurrencyViaGrid("Currency", currency);

        lookup.selectInGridByMarkerWithSearch("Country", country);
        setLookupByMarker("Type", equipmentType);

        dom.setGridInputValue(
                "EshPropertyDataEquipmentDetailStartYearIntegerEdit-el", year
        );
        dom.setGridInputValue(
                "EshPropertyDataEquipmentDetailDescriptionTextEdit-el", description
        );

        click.clickByMarker("save");
    }

    // ══════════════════════════════════════
    // CASH DEPOSIT
    // ══════════════════════════════════════

    @Step("Оценка депозита: {sum} {currency}")
    public void fillCashDeposit(String sum, String currency) {
        log.info("Fill cash deposit: sum='{}'", sum);

        lookup.selectByLabel("Сумма", sum);
        setCurrency("EshCurrency", currency);

        click.clickByName("Поиск");
        click.clickByName("Выбрать");
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

    private void setCurrency(String marker, String currency) {
        log.debug("setCurrency: marker='{}' currency='{}'", marker, currency);
        lookup.selectByMarker(marker, currency);
        // menu.clickByName убран — selectByMarker уже выбирает значение
    }

    private void setLookupByMarker(String marker, String value) {
        log.debug("setLookupByMarker: marker='{}' value='{}'", marker, value);
        lookup.selectByMarker(marker, value);
        // menu.clickByName убран — selectByMarker уже выбирает значение
    }


    private void setCurrencyViaGrid(String marker, String currency) {
        log.debug("setCurrencyViaGrid: marker='{}' currency='{}'", marker, currency);
        lookup.selectInGridByMarker(marker, currency);
    }
}