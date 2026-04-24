package com.creatio.autotest.flows;

import com.creatio.autotest.components.GridComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.components.MenuComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.$x;

public class ApplicationSearchFlow {

    private static final Logger log =
            LoggerFactory.getLogger(ApplicationSearchFlow.class);

    private final BasePage        basePage  = new BasePage();
    private final LookupComponent lookup    = new LookupComponent();
    private final MenuComponent   menu      = new MenuComponent();
    private final GridComponent   grid      = new GridComponent();
    private final ClickService    click     = new ClickService();

    // Сохранённый номер заявки — передаётся снаружи
    private String savedApplicationNumber;

    public ApplicationSearchFlow withApplicationNumber(String number) {
        this.savedApplicationNumber = number;
        return this;
    }

    // ══════════════════════════════════════
    // OPEN BY NUMBER
    // ══════════════════════════════════════

    @Step("Открыть заявку по номеру: {savedApplicationNumber}")
    public void openByNumber() {
        log.info("Open application by number: '{}'",
                savedApplicationNumber);

        click.clickByMarker("Заявки");

        removeFilterIfExists();

        click.clickByName("Фильтры/группы");
        menu.clickByName("Добавить условие");

        lookup.selectByMarker("columnEdit", "Номер");

        applyNumberFilter();

        grid.clickFirstRowInGrid(
                "grid-FinApplicationSectionDataGridGrid-wrap"
        );

        click.clickByMarker("Открыть");
    }

    @Step("Открыть договор по номеру: {savedApplicationNumber}")
    public void openContractByNumber() {
        log.info("Open contract by number: '{}'",
                savedApplicationNumber);

        click.clickByMarker("Договоры");

        removeFilterIfExists();

        click.clickByName("Фильтры/группы");
        menu.clickByName("Добавить условие");

        lookup.selectByMarker("columnEdit", "Номер");

        applyNumberFilter();

        grid.clickFirstRowInGrid(
                "grid-ContractSectionV2DataGridGrid-wrap"
        );

        click.clickByMarker("Открыть");
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

    private void removeFilterIfExists() {
        try {
            $x("//*[contains(@class,'filter-remove')]")
                    .click();
        } catch (Exception e) {
            log.debug("No filter to remove");
        }
    }

    private void applyNumberFilter() {
        $x("//*[@data-item-marker='searchEdit']//input")
                .setValue(savedApplicationNumber);
        click.clickByMarker("applyButton");
    }
}