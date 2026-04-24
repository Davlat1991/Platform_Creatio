package com.creatio.autotest.flows;

import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.core.services.WaitService;
import com.creatio.autotest.pages.ConsultationPanelPage;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReviewStageUnderwriterFlow {

    private static final Logger log =
            LoggerFactory.getLogger(ReviewStageUnderwriterFlow.class);

    private final ButtonsComponent      buttons      = new ButtonsComponent();
    private final LookupComponent       lookup       = new LookupComponent();
    private final BasePage              basePage     = new BasePage();
    private final ClickService          click        = new ClickService();
    private final ConsultationPanelPage consultation = new ConsultationPanelPage();
    private final ApplicationSearchFlow searchFlow   = new ApplicationSearchFlow();
    private final WaitService wait = new WaitService();

    // ══════════════════════════════════════
    // MAIN
    // ══════════════════════════════════════

    @Step("Review: Underwriter утверждает решение")
    public void approveReview(String applicationNumber,
                              String decisionProjectName) {
        log.info("Underwriter approve review: number='{}'",
                applicationNumber);

        consultation.closeIfOpened();

        // Открываем заявку по номеру
        searchFlow
                .withApplicationNumber(applicationNumber)
                .openByNumber();

        openDecisionTab();
        openDecisionProject(decisionProjectName);
        takeToWork();
        approveDecision();

        log.info("Underwriter review approved");
    }

    // ══════════════════════════════════════
    // STEPS
    // ══════════════════════════════════════

    @Step("Открыть вкладку 'Решение по заявке'")
    private void openDecisionTab() {
        try {
            click.clickByMarker(
                    "FinApplicationPageTabsTabPanel-scroll-right"
            );
        } catch (Exception e) {
            log.debug("Scroll right not needed");
        }
        buttons.clickTabByName("Решение по заявке");
    }

    @Step("Открыть проект решения: {name}")
    private void openDecisionProject(String name) {
        log.info("Open decision project: '{}'", name);
        // Двойной клик по строке в гриде
        click.doubleClickByMarker(name);
    }

    @Step("Взять в работу")
    private void takeToWork() {
        click.waitAndClick("TakeToWorkButton");
        buttons.clickTabByName("Решение");
        lookup.select(
                "Вид кредита для Проекта решения", "Кредит"
        );
    }

    @Step("Утвердить решение")
    private void approveDecision() {
        click.waitAndClick("ApproveButton");
    }
}