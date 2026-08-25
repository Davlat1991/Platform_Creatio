package com.creatio.autotest.flows.retail;

import com.codeborne.selenide.Condition;
import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.components.MessageBoxComponent;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.core.services.WaitService;
import com.creatio.autotest.flows.common.ApplicationSearchFlow;
import com.creatio.autotest.pages.ConsultationPanelPage;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;

public class ReviewStageUnderwriterFlow {

    private static final Logger log =
            LoggerFactory.getLogger(ReviewStageUnderwriterFlow.class);

    private final ButtonsComponent      buttons      = new ButtonsComponent();
    private final LookupComponent       lookup       = new LookupComponent();
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

        // Якорь готовности — ApproveButton: появляется только после взятия в
        // работу, не зависит от пути входа. Прежний якорь (Condition.editable по
        // маркеру 'Вид кредита для Проекта решения') не срабатывал: реальный
        // маркер в DOM составной ('BnzTypeCreditProjectSolution Вид кредита...'),
        // точного совпадения нет, цикл из 5 попыток прогорал как слепая задержка ~4с.
        // Паттерн из AgroReviewStageUnderwriterFlow.takeToWork().
        $x("//*[@data-item-marker='ApproveButton']")
                .shouldBe(Condition.visible, Duration.ofSeconds(60));

        buttons.clickTabByName("Решение");
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