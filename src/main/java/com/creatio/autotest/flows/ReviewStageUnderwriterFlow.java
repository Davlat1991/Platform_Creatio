package com.creatio.autotest.flows;

import com.codeborne.selenide.Condition;
import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.components.MessageBoxComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.core.services.WaitService;
import com.creatio.autotest.pages.ConsultationPanelPage;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.$x;

public class ReviewStageUnderwriterFlow {

    private static final Logger log =
            LoggerFactory.getLogger(ReviewStageUnderwriterFlow.class);

    private final ButtonsComponent      buttons      = new ButtonsComponent();
    private final LookupComponent       lookup       = new LookupComponent();
    private final BasePage              basePage     = new BasePage();
    private final ClickService          click        = new ClickService();
    private final MessageBoxComponent   msgBox       = new MessageBoxComponent();
    private final ConsultationPanelPage consultation = new ConsultationPanelPage();
    private final ApplicationSearchFlow searchFlow   = new ApplicationSearchFlow();
    private final WaitService wait = new WaitService();

    // ══════════════════════════════════════
    // MAIN
    // ══════════════════════════════════════

    @Step("Review: Underwriter утверждает решение")
    public void approveReview(String decisionProjectPrefix) {
        String applicationNumber =
                ScenarioContextProvider.get().getApplicationNumber();
        log.info("Underwriter approve review: number='{}'",
                applicationNumber);

        consultation.closeIfOpened();

        searchFlow
                .withApplicationNumber(applicationNumber)
                .openByNumber();

        openDecisionTab();
        openDecisionProject(decisionProjectPrefix, applicationNumber);
        takeToWork();
        approveDecision();

        log.info("Underwriter review approved");
    }

    // ══════════════════════════════════════
    // STEPS
    // ══════════════════════════════════════

    @Step("Открыть вкладку 'Решение по заявке'")
    private void openDecisionTab() {
        buttons.clickTabByName("Решение по заявке");
    }

    @Step("Открыть проект решения: {prefix} {applicationNumber}")
    private void openDecisionProject(String prefix, String applicationNumber) {
        // Реальный маркер строки содержит номер: "КК4 по заявке 391973".
        // Собираем полный маркер из префикса + номер из контекста.
        String fullMarker = prefix + " " + applicationNumber;
        log.info("Open decision project: '{}'", fullMarker);
        click.doubleClickByMarker(fullMarker);
    }

    @Step("Взять в работу и открыть вкладку 'Решение'")
    private void takeToWork() {
        click.waitAndClickWithRefresh("TakeToWorkButton");

        // После "Взять в работу" форма перезагружается. Маску ждать ненадёжно
        // (появляется с задержкой). Ждём РЕЗУЛЬТАТ: вкладка "Решение" открыта,
        // когда на ней виден инпут поля "Вид кредита для Проекта решения".
        // Retry: клик вкладки может потеряться, пока страница догружается.
        String creditTypeInput =
                "//*[@data-item-marker='Вид кредита для Проекта решения']//input";

        for (int attempt = 1; attempt <= 5; attempt++) {
            buttons.clickTabByName("Решение");

            // editable = enabled И без readonly. Прямой признак, что поле готово к вводу,
            // в отличие от visible (инпут виден и заблокированным до взятия в работу).
            if ($x(creditTypeInput).is(Condition.editable)) {
                log.info("Поле 'Вид кредита' редактируемо на попытке {}/5", attempt);
                break;
            }
            log.warn("Поле ещё заблокировано на попытке {}/5 — повтор", attempt);
            wait.waitForLoader();
        }
        lookup.select("Вид кредита для Проекта решения", "Кредит");
    }


    @Step("Утвердить решение")
    private void approveDecision() {
        click.waitAndClick("ApproveButton");

        // После утверждения появляется модалка завершения процесса — ждём и
        // подтверждаем. Раньше ожидания не было (наблюдение с прогона).
        // Паттерн из AgroReviewStageUnderwriterFlow.approveDecision().
        msgBox.shouldSeeModal("Процесс успешно завершен");
        click.clickByName("ОК");
    }
}