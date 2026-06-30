package com.creatio.autotest.flows;

import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.DashboardComponent;
import com.creatio.autotest.components.GridAssertions;
import com.creatio.autotest.pages.ConsultationPanelPage;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import com.creatio.autotest.testdata.models.CreditRoute;
import com.creatio.autotest.testdata.models.ScoringDecision;
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
    private final DashboardComponent    dashboard      = new DashboardComponent();
    private final ConsultationPanelPage consultation   = new ConsultationPanelPage();

    // ══════════════════════════════════════
    // MAIN
    // ══════════════════════════════════════

    @Step("Review: Retail Manager — стадия рассмотрения")
    public void completeReview(CreditRoute route, ScoringDecision expectedDecision) {
        log.info("Complete retail manager review. Route: {}, expected decision: {}",
                route, expectedDecision);

        consultation.closeIfOpened();

        verifyRoute(route);

        switch (route) {
            case STANDARD   -> completeStandardRoute();
            case SIMPLIFIED -> completeSimplifiedRoute(expectedDecision);
        }

        saveApplicationNumber();

        log.info("Review completed. Application number: {}",
                ScenarioContextProvider.get().getApplicationNumber());
    }

    // ══════════════════════════════════════
    // ROUTES
    // ══════════════════════════════════════

    @Step("Стандартный маршрут: ожидание решения комитета (КК4)")
    private void completeStandardRoute() {
        // Финал скоринга на стандартном = появление КК4 в "Проекты решений".
        // Конкретное решение скоринга не проверяем (может быть любым из 4).
        refresh();
        consultation.closeAfterReload();
        openDecisionTab();
        verifyCommittee();
    }

    @Step("Упрощённый маршрут: переход на 'Информирование клиента' + проверка решения")
    private void completeSimplifiedRoute(ScoringDecision expectedDecision) {
        // Финал скоринга на упрощённом = стадия ушла на "Информирование клиента".
        // Это надёжный якорь: промежуточное "Отказать" ещё не финал.
        dashboard.waitProcessBlock("Информирование клиента");

        // После финализации вкладка "Проверки" доступна — читаем итоговое решение.
        String actual = gridAssertions.readScoringDecision();
        log.info("Scoring decision — expected: '{}', actual UI: '{}'",
                expectedDecision.getUiText(), actual);

        if (!actual.equalsIgnoreCase(expectedDecision.getUiText())) {
            throw new AssertionError(
                    "Решение скоринга не совпало. Ожидалось '" +
                            expectedDecision.getUiText() +
                            "', фактически '" + actual + "'"
            );
        }
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