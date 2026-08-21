package com.creatio.autotest.flows.retail;

import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.GridAssertions;
import com.creatio.autotest.pages.ConsultationPanelPage;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import com.creatio.autotest.testdata.models.CreditRoute;
import com.creatio.autotest.testdata.models.ScoringDecision;
import com.creatio.autotest.utils.FieldUtils;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$x;
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

    @Step("Review: Retail Manager — стадия рассмотрения")
    public void completeReview(CreditRoute route, ScoringDecision expectedDecision) {
        log.info("Complete retail manager review. Route: {}, expected decision: {}",
                route, expectedDecision);

        consultation.closeIfOpened();

        verifyRoute(route);

        switch (route) {
            case STANDARD   -> completeStandardRoute();
            case SIMPLIFIED -> completeSimplifiedRoute();
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
        // КК4 в "Проекты решений" формируется бэкендом с задержкой (до ~60с)
        // и подтягивается в грид только после рефреша. Делаем несколько
        // попыток: рефреш → длинное ожидание строки с КК4.
        int maxAttempts = 10;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            refresh();
            consultation.closeAfterReload();
            openDecisionTab();

            if (gridAssertions.waitForCommitteeDecision(
                    "КК4", java.time.Duration.ofSeconds(10))) {
                log.info("КК4 найдено на попытке {}/{}", attempt, maxAttempts);
                return;
            }
            log.warn("КК4 не найдено на попытке {}/{} — повтор рефреша",
                    attempt, maxAttempts);
        }

        throw new AssertionError(
                "КК4 не появилось в 'Проект решений' после " +
                        maxAttempts + " рефрешей");
    }

    @Step("Упрощённый маршрут: ожидание перехода на 'Информирование клиента'")
    private void completeSimplifiedRoute() {
        // Финал скоринга на упрощённом = стадия ушла на "Информирование клиента".
        // Появление этой активности в дашборде — надёжный событийный якорь.
        // Переход происходит ТОЛЬКО при одобрении, поэтому сам факт появления
        // активности доказывает, что скоринг финализировался как "Одобрить".
        // Промежуточное "Отказать" на этот якорь не влияет (перехода ещё нет).
        $x("//label[@data-item-marker='Информирование клиента']")
                .shouldBe(visible, Duration.ofSeconds(90));

        log.info("Стадия перешла на 'Информирование клиента' — скоринг одобрил");
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


    @Step("Сохранить номер заявки в контекст")
    private void saveApplicationNumber() {
        String number = FieldUtils.getValueByMarker("Number");
        ScenarioContextProvider.get().setApplicationNumber(number);
        log.info("Application number saved to context: {}", number);
    }
}