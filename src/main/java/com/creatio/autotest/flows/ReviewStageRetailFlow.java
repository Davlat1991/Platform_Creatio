package com.creatio.autotest.flows;

import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.GridAssertions;
import com.creatio.autotest.pages.ConsultationPanelPage;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import com.creatio.autotest.testdata.models.CreditRoute;
import com.creatio.autotest.utils.FieldUtils;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.refresh;

public class ReviewStageRetailFlow {

    private static final Logger log =
            LoggerFactory.getLogger(ReviewStageRetailFlow.class);

    private final ButtonsComponent      buttons        = new ButtonsComponent();
    private final GridAssertions        gridAssertions = new GridAssertions();
    private final ConsultationPanelPage consultation   = new ConsultationPanelPage();

    // ══════════════════════════════════════
    // MAIN
    // ══════════════════════════════════════

    @Step("Review: Retail Manager завершает документы и ожидает решение")
    public void completeReview(CreditRoute route) {
        log.info("Complete retail manager review. Route: {}", route);

        consultation.closeIfOpened();

        verifyRoute(route);

        openChecksTab();
        waitForCreditDecision();
        refresh();
        consultation.closeAfterReload();
        openDecisionTab();
        verifyCommittee();
        saveApplicationNumber();

        log.info("Review completed. Application number: {}",
                ScenarioContextProvider.get().getApplicationNumber());
    }

    // ══════════════════════════════════════
    // STEPS
    // ══════════════════════════════════════

    @Step("Проверить маршрут рассмотрения (Утверждённые параметры)")
    private void verifyRoute(CreditRoute intended) {
        String actual = FieldUtils.getDetailInputValueScoped(
                "TsiApprovedFinApplicationSpecDetailContainer",
                "Маршрут рассмотрения"
        );
        log.info("Route — intended: '{}', actual UI: '{}'",
                intended.getUiText(), actual);

        if (!actual.equalsIgnoreCase(intended.getUiText())) {
            throw new AssertionError(
                    "Маршрут не совпал. Ожидался '" + intended.getUiText() +
                            "', фактически '" + actual + "'"
            );
        }
    }

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
}