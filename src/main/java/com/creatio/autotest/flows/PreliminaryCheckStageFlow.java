package com.creatio.autotest.flows;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.components.DashboardComponent;
import com.creatio.autotest.components.MessageBoxComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class PreliminaryCheckStageFlow {

    private static final Logger log =
            LoggerFactory.getLogger(PreliminaryCheckStageFlow.class);

    private final DashboardComponent  dashboard = new DashboardComponent();
    private final MessageBoxComponent msgBox    = new MessageBoxComponent();
    private final BasePage            basePage  = new BasePage();
    private final ClickService        click     = new ClickService();

    // ══════════════════════════════════════
    // MAIN
    // ══════════════════════════════════════

    @Step("Preliminary Check: выполнение всех проверок")
    public void completePreliminaryCheckStage() {
        log.info("Complete preliminary check stage");

        completeParticipantsQuestionnaire();
        completeClientCheck();
        waitForCollateralStage();
    }

    // ══════════════════════════════════════
    // TASK 1: QUESTIONNAIRES
    // ══════════════════════════════════════

    @Step("Заполнение анкет участников")
    private void completeParticipantsQuestionnaire() {
        log.info("Complete participants questionnaire task");

        // Hover на строку активности — кнопка Execute появляется только при наведении
        hoverActivityRow("Добавьте и заполните анкеты участников заявки");

        // Клик по кнопке Execute — открывается попап
        clickExecuteButton("Добавьте и заполните анкеты участников заявки");

        // Выбираем результат в попапе
        selectResult("Result", "Выполнена");

        // Сохраняем попап
        clickSaveEditButton();

        // Подтверждаем диалоги
        confirmNoDebtMessages();

        log.info("Participants questionnaire task completed");
    }

    @Step("Подтверждение сообщений")
    private void confirmNoDebtMessages() {
        log.info("Confirm no debt messages");

        msgBox.shouldSeeModal("Нет задолженности!");
        click.clickByMarker("ОК");
        log.info("Confirmed: Нет задолженности!");

        msgBox.shouldSeeModal("У клиента нет просроченных дней");
        click.clickByMarker("ОК");
        log.info("Confirmed: У клиента нет просроченных дней");
    }

    // ══════════════════════════════════════
    // TASK 2: CLIENT CHECK — после отладки
    // ══════════════════════════════════════

    @Step("Проверка клиента")
    private void completeClientCheck() {
        log.info("Complete client check task");

        // Hover + клик Approve на задаче "Проверка клиента"
        hoverActivityRow("Проверка клиента");
        dashboard.waitAndClick("Проверка клиента", "Approve");
        clickSaveEditButton();

        log.info("Client check task completed");
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

    /**
     * Наводим курсор на строку активности по заголовку.
     * Кнопка Execute видна только при hover.
     */
    private void hoverActivityRow(String activityTitle) {
        log.debug("Hover activity row: '{}'", activityTitle);

        $x("//div[contains(@class,'dashboard-item-content')]" +
                "[.//text()[contains(.,'" + activityTitle + "')]]")
                .shouldBe(Condition.visible, Duration.ofSeconds(100))
                .hover();
    }

    /**
     * Клик по кнопке Execute внутри строки активности.
     */
    private void clickExecuteButton(String activityTitle) {
        log.debug("Click Execute button for: '{}'", activityTitle);

        // Находим строку активности
        SelenideElement activityRow = $x(
                "//div[contains(@class,'dashboard-container-item')]" +
                        "[.//text()[contains(.,'" + activityTitle + "')]]"
        ).shouldBe(Condition.visible, Duration.ofSeconds(100));

        // Hover — делает кнопку видимой
        activityRow.hover();
        log.debug("Hovered on activity row: '{}'", activityTitle);

        // Находим кнопку Execute внутри строки
        SelenideElement executeBtn = activityRow
                .$x(".//span[@data-item-marker='Execute']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30));

        // JS-клик — триггерит событие в Creatio
        executeJavaScript("arguments[0].click();", executeBtn);
        log.debug("Execute button clicked via JS");

        // Ждём появления попапа
        $x("//div[@data-item-marker='ActivityMiniPageContainer']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30));

        log.debug("Execute popup opened for: '{}'", activityTitle);
    }

    /**
     * Выбирает значение из дропдауна результата в попапе.
     * DOM: div[data-item-marker='Result'] → ul → li[data-item-marker='Выполнена']
     */
    private void selectResult(String fieldMarker, String value) {
        log.debug("Select result: field='{}' value='{}'", fieldMarker, value);

        // Кликаем на поле дропдауна
        $x("//div[@data-item-marker='" + fieldMarker + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .click();

        // Выбираем li из списка
        $x("//div[@data-item-marker='" + fieldMarker + "']" +
                "//li[@data-item-marker='" + value + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .click();

        log.debug("Result selected: '{}'", value);
    }

    /**
     * Сохраняет попап кнопкой SaveEditButton.
     */
    private void clickSaveEditButton() {
        log.debug("Click SaveEditButton");

        executeJavaScript(
                "arguments[0].click();",
                $x("//span[@data-item-marker='SaveEditButton']")
                        .shouldBe(Condition.visible, Duration.ofSeconds(30))
        );

        basePage.waitForPage();
        log.debug("SaveEditButton clicked");
    }

    @Step("Ожидание перехода на стадию Обеспечение")
    private void waitForCollateralStage() {
        log.info("Waiting for collateral stage activity...");

        // Якорь — активность появляется только на стадии Обеспечение
        $x("//*[@data-item-marker='Заполните данные обеспечения и поручительства']")
                .shouldBe(Condition.visible, Duration.ofSeconds(60));

        log.info("Collateral stage reached — activity visible");
    }
}