package com.creatio.autotest.flows;

import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.GridAssertions;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import com.creatio.autotest.utils.FieldUtils;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.refresh;

public class ReviewStageRetailFlow {

    private static final Logger log =
            LoggerFactory.getLogger(ReviewStageRetailFlow.class);

    private final ButtonsComponent buttons        = new ButtonsComponent();
    private final GridAssertions   gridAssertions = new GridAssertions();
    private final BasePage         basePage       = new BasePage();
    private final ClickService     click          = new ClickService();

    // ══════════════════════════════════════
    // MAIN
    // ══════════════════════════════════════

    @Step("Review: Retail Manager завершает документы и ожидает решение")
    public void completeReview() {
        log.info("Complete retail manager review");

        openChecksTab();
        waitForCreditDecision();
        refresh();
        openDecisionTab();
        verifyCommittee();
        saveApplicationNumber();

        log.info("Review completed. Application number: {}",
                ScenarioContextProvider.get().getApplicationNumber());
    }

    // ══════════════════════════════════════
    // STEPS
    // ══════════════════════════════════════

    @Step("Открыть вкладку 'Проверки'")
    private void openChecksTab() {
        buttons.clickTabByName("Проверки");
    }

    @Step("Ожидать решение по кредиту")
    private void waitForCreditDecision() {
        gridAssertions.waitForAnyCreditDecision();
    }

    @Step("Открыть вкладку 'Решение по заявке'")
    private void openDecisionTab() {
        scrollTabsRight();
        buttons.clickTabByName("Решение по заявке");
    }

    @Step("Проверить комитет КК4")
    private void verifyCommittee() {
        gridAssertions.waitForValueInGridColumn("Комитет", "КК4");
    }

    @Step("Сохранить номер заявки в контекст")
    private void saveApplicationNumber() {
        String number = FieldUtils.getValueByMarker("Number");
        ScenarioContextProvider.get().setApplicationNumber(number);
        log.info("Application number saved to context: {}", number);
    }

    private void scrollTabsRight() {
        try {
            click.clickByMarker("FinApplicationPageTabsTabPanel-scroll-right");
        } catch (Exception e) {
            log.debug("Scroll right not needed");
        }
    }
}