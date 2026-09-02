package com.creatio.autotest.flows.micro;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.components.DashboardComponent;
import com.creatio.autotest.components.MessageBoxComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.creatio.autotest.testdata.models.ActivityResult;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

/**
 * Стадия «Предварительная проверка» (Агро/Микро) — цельный процесс.
 * <p>
 * Изолированная Микро-копия розничного {@code PreliminaryCheckStageFlow}
 * (процесс Агро до «Информирования клиента» идентичен рознице). Общие
 * компоненты ({@link DashboardComponent}, {@link MessageBoxComponent},
 * {@link ClickService}, {@link BasePage}) — из Core, продукт-агностик.
 * <p>
 * {@link #completePreliminaryCheckStage()} выполняет стадию целиком:
 * <ol>
 *   <li>завершение активности «Оформления» → переход на «Предварительную проверку»;</li>
 *   <li>задача «Проверка клиента» (Approve);</li>
 *   <li>ожидание перехода на стадию «Обеспечение».</li>
 * </ol>
 */
public class AgroPreliminaryCheckFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroPreliminaryCheckFlow.class);

    /** Активность стадии «Оформление», завершение которой открывает «Предварительную проверку». */
    private static final String REGISTRATION_ACTIVITY = "Добавьте и заполните анкеты участников заявки";

    /** Результат выполнения активности (happy-path). */
    private static final String ACTIVITY_RESULT = ActivityResult.COMPLETED.getUiText();

    /** Задача стадии «Предварительная проверка». */
    private static final String CLIENT_CHECK_ACTIVITY = "Проверка клиента";

    /** Маркер кнопки одобрения на задаче проверки клиента. */
    private static final String CLIENT_CHECK_APPROVE  = "Approve";

    /** Якорь — активность, появляющаяся только на стадии «Обеспечение». */
    private static final String COLLATERAL_STAGE_ANCHOR = "Заполните данные обеспечения и поручительства";

    private final DashboardComponent  dashboard = new DashboardComponent();
    private final MessageBoxComponent msgBox    = new MessageBoxComponent();
    private final BasePage            basePage  = new BasePage();
    private final ClickService        click     = new ClickService();

    // ══════════════════════════════════════
    // MAIN
    // ══════════════════════════════════════

    @Step("Агро: стадия «Предварительная проверка»")
    public void completePreliminaryCheckStage() {
        log.info("Agro: complete preliminary check stage");

        completeRegistrationActivity();
        completeClientCheck();
        waitForCollateralStage();

        log.info("Agro: preliminary check stage completed");
    }

    // ══════════════════════════════════════
    // TASK 1: завершение активности «Оформление» → «Предварительная проверка»
    // ══════════════════════════════════════

    @Step("Агро: завершение активности стадии «Оформление»")
    private void completeRegistrationActivity() {
        log.info("Agro: complete registration-stage activity");

        hoverActivityRow(REGISTRATION_ACTIVITY);
        clickExecuteButton(REGISTRATION_ACTIVITY);
        selectResult("Result", ACTIVITY_RESULT);
        clickSaveEditButton();
        confirmNoDebtMessages();

        log.info("Agro: registration-stage activity completed");
    }

    // ══════════════════════════════════════
    // TASK 2: проверка клиента
    // ══════════════════════════════════════

    @Step("Агро: проверка клиента")
    private void completeClientCheck() {
        log.info("Agro: complete client check task");

        hoverActivityRow(CLIENT_CHECK_ACTIVITY);
        dashboard.waitAndClick(CLIENT_CHECK_ACTIVITY, CLIENT_CHECK_APPROVE);
        clickSaveEditButton();

        log.info("Agro: client check task completed");
    }

    // ══════════════════════════════════════
    // TASK 3: ожидание перехода на стадию «Обеспечение»
    // ══════════════════════════════════════

    @Step("Агро: ожидание перехода на стадию «Обеспечение»")
    private void waitForCollateralStage() {
        log.info("Agro: waiting for collateral stage activity...");

        $x("//*[@data-item-marker='" + COLLATERAL_STAGE_ANCHOR + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(60));

        log.info("Agro: collateral stage reached — activity visible");
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

    /** Наводим курсор на строку активности — кнопка Execute видна только при hover. */
    private void hoverActivityRow(String activityTitle) {
        log.debug("Hover activity row: '{}'", activityTitle);

        $x("//div[contains(@class,'dashboard-item-content')]" +
                "[.//text()[contains(.,'" + activityTitle + "')]]")
                .shouldBe(Condition.visible, Duration.ofSeconds(80))
                .hover();
    }

    /** Клик по кнопке Execute внутри строки активности → открывается попап. */
    private void clickExecuteButton(String activityTitle) {
        log.debug("Click Execute button for: '{}'", activityTitle);

        SelenideElement activityRow = $x(
                "//div[contains(@class,'dashboard-container-item')]" +
                        "[.//text()[contains(.,'" + activityTitle + "')]]"
        ).shouldBe(Condition.visible, Duration.ofSeconds(60));

        activityRow.hover();

        SelenideElement executeBtn = activityRow
                .$x(".//span[@data-item-marker='Execute']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30));

        executeJavaScript("arguments[0].click();", executeBtn);

        $x("//div[@data-item-marker='ActivityMiniPageContainer']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30));

        log.debug("Execute popup opened for: '{}'", activityTitle);
    }

    /** Выбор значения результата в попапе: div[marker] → li[marker=value]. */
    private void selectResult(String fieldMarker, String value) {
        log.debug("Select result: field='{}' value='{}'", fieldMarker, value);

        $x("//div[@data-item-marker='" + fieldMarker + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .click();

        $x("//div[@data-item-marker='" + fieldMarker + "']" +
                "//li[@data-item-marker='" + value + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .click();

        log.debug("Result selected: '{}'", value);
    }

    /** Сохранение попапа кнопкой SaveEditButton. */
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



    /** Подтверждение модальных сообщений после завершения активности. */
    private void confirmNoDebtMessages() {
        log.info("Confirm no debt messages");

        msgBox.shouldSeeModal("Нет задолженности!");
        click.clickByMarker("ОК");
        log.info("Confirmed: Нет задолженности!");

        msgBox.shouldSeeModal("У клиента нет просроченных дней");
        click.clickByMarker("ОК");
        log.info("Confirmed: У клиента нет просроченных дней");
    }
}