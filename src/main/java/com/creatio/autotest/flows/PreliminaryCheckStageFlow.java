package com.creatio.autotest.flows;

import com.creatio.autotest.components.DashboardComponent;
import com.creatio.autotest.components.MenuComponent;
import com.creatio.autotest.components.MessageBoxComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.$x;

public class PreliminaryCheckStageFlow {

    private static final Logger log =
            LoggerFactory.getLogger(PreliminaryCheckStageFlow.class);

    private final DashboardComponent   dashboard = new DashboardComponent();
    private final MessageBoxComponent  msgBox    = new MessageBoxComponent();
    private final MenuComponent        menu      = new MenuComponent();
    private final BasePage             basePage  = new BasePage();
    private final ClickService         click     = new ClickService();

    // ══════════════════════════════════════
    // MAIN
    // ══════════════════════════════════════

    @Step("Preliminary Check: выполнение всех проверок")
    public void completePreliminaryCheckStage() {
        log.info("Complete preliminary check stage");

        completeParticipantsQuestionnaire();
        completeClientCheck();
    }

    // ══════════════════════════════════════
    // TASK 1: QUESTIONNAIRES
    // ══════════════════════════════════════

    @Step("Заполнение анкет участников")
    private void completeParticipantsQuestionnaire() {
        log.info("Complete participants questionnaire");

        dashboard.clickAndWaitMiniPage(
                "Добавьте и заполните анкеты участников заявки",
                "Execute"
        );

        setFieldByMarker("Result", "Выполнена");
        menu.clickByName("Выполнена");
        click.clickByMarker("SaveEditButton");

        confirmNoDebtMessages();
    }

    @Step("Подтверждение сообщений об отсутствии задолженности")
    private void confirmNoDebtMessages() {
        msgBox.shouldSeeModal("Нет задолженности!");
        click.clickByMarker("ОК");

        msgBox.shouldSeeModal("У клиента нет просроченных дней");
        click.clickByMarker("ОК");
    }

    // ══════════════════════════════════════
    // TASK 2: CLIENT CHECK
    // ══════════════════════════════════════

    @Step("Проверка клиента")
    private void completeClientCheck() {
        log.info("Complete client check");

        openClientCheckTask();
        click.clickByMarker("SaveEditButton");

        completeCollateralTask();
    }

    private void openClientCheckTask() {
        dashboard.waitAndClick("Проверка клиента", "Approve");
    }

    @Step("Завершение задачи по залогу и поручительству")
    private void completeCollateralTask() {
        dashboard.clickAndWaitMiniPage(
                "Заполните данные обеспечения и поручительства",
                "Execute"
        );

        setFieldByMarker("ProcessResult", "Выполнена");
        menu.clickByName("Выполнена");
        click.clickByMarker("SaveEditButton");
    }

    // ══════════════════════════════════════
    // HELPER
    // ══════════════════════════════════════

    private void setFieldByMarker(String marker, String value) {
        $x("//div[@data-item-marker='" + marker + "']/input")
                .setValue(value);
    }
}