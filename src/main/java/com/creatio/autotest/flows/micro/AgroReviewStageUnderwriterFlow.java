package com.creatio.autotest.flows.micro;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.components.MessageBoxComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.flows.common.ApplicationSearchFlow;
import com.creatio.autotest.pages.ConsultationPanelPage;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import com.creatio.autotest.testdata.models.AgroDecisionData;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;

/**
 * Стадия «Рассмотрение» (Агро/Микро) — сторона верификатора: утверждение
 * проекта решения комитета.
 * <p>
 * Продукты «Микро» идут по стандартному маршруту с начала заявки, поэтому
 * верификатор участвует всегда, независимо от результата скоринга.
 * <p>
 * Отличия от розничного образца:
 * <ul>
 *   <li>на вкладке «Решение» заполняется не только «Вид кредита для Проекта
 *       решения», но и «Кредитный лимит» — специфика сегмента «Микро»;</li>
 *   <li>значения приходят из {@link AgroDecisionData}, а не хардкодом;</li>
 *   <li>готовность формы после «Взять в работу» определяется появлением
 *       {@code ApproveButton} вместо недостоверной проверки поля.</li>
 * </ul>
 * <p>
 * Флоу предполагает, что номер заявки уже сохранён в контексте сценария
 * ({@code AgroReviewStageFlow.completeReview()}), и сам находит заявку по
 * номеру — вход выполняется под другим пользователем.
 */
public class AgroReviewStageUnderwriterFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroReviewStageUnderwriterFlow.class);

    /** Префикс маркера строки проекта решения; полный маркер — «КК4 по заявке 394980». */
    private static final String DECISION_PROJECT_PREFIX = "КК4 по заявке";

    /** Контейнер поля «Кредитный лимит» — скоуп для поиска инпута. */
    private static final String CREDIT_LIMIT_CONTAINER =
            "TsiProjectSolutionPageBnzCreditLimitContainer";

    private final ButtonsComponent      buttons      = new ButtonsComponent();
    private final LookupComponent       lookup       = new LookupComponent();
    private final ClickService          click        = new ClickService();
    private final ConsultationPanelPage consultation = new ConsultationPanelPage();
    private final ApplicationSearchFlow searchFlow   = new ApplicationSearchFlow();
    private final MessageBoxComponent   msgBox       = new MessageBoxComponent();
    private final BasePage              basePage     = new BasePage();

    // ══════════════════════════════════════
    // MAIN
    // ══════════════════════════════════════

    @Step("Агро: верификатор утверждает решение")
    public void approveReview(AgroDecisionData data) {
        String applicationNumber =
                ScenarioContextProvider.get().getApplicationNumber();
        log.info("Agro: underwriter approve review, number='{}'", applicationNumber);

        if (applicationNumber == null || applicationNumber.isBlank()) {
            throw new IllegalStateException(
                    "Номер заявки отсутствует в контексте — сначала должен " +
                            "отработать AgroReviewStageFlow.completeReview()");
        }

        consultation.closeIfOpened();

        searchFlow
                .withApplicationNumber(applicationNumber)
                .openByNumber();

        openDecisionTab();
        openDecisionProject(applicationNumber);
        takeToWork();
        fillDecision(data);
        approveDecision();

        log.info("Agro: underwriter review approved");
    }

    // ══════════════════════════════════════
    // STEPS
    // ══════════════════════════════════════

    @Step("Агро: открыть вкладку «Решение по заявке»")
    private void openDecisionTab() {
        buttons.clickTabByName("Решение по заявке");
    }

    /**
     * Открытие проекта решения. Маркер строки содержит номер заявки:
     * «КК4 по заявке 394980» — собираем из префикса и номера из контекста.
     */
    @Step("Агро: открыть проект решения по заявке {applicationNumber}")
    private void openDecisionProject(String applicationNumber) {
        String fullMarker = DECISION_PROJECT_PREFIX + " " + applicationNumber;
        log.info("Agro: open decision project: '{}'", fullMarker);
        click.doubleClickByMarker(fullMarker);
    }

    /**
     * «Взять в работу» — якорь готовности: появление кнопки утверждения.
     * <p>
     * {@code ApproveButton} появляется только после взятия записи в работу,
     * поэтому её видимость доказывает смену состояния независимо от пути входа
     * на страницу проекта решения.
     * <p>
     * Блокировку самого поля («t-label-disabled» на лейбле) как якорь
     * использовать нельзя: она зависит от пути входа. При входе через раздел
     * «Проект решения» поле доступно к заполнению и без взятия в работу, и
     * ожидание снятия класса прошло бы вхолостую.
     * <p>
     * Розничный образец ждал {@code Condition.editable} по локатору
     * {@code //*[@data-item-marker='Вид кредита для Проекта решения']//input}.
     * В DOM маркер составной («BnzTypeCreditProjectSolution Вид кредита для
     * Проекта решения»), точное совпадение не матчится, элемент не находится —
     * все 5 попыток всегда прогорали, а цикл работал как задержка ~4 с.
     */
    @Step("Агро: взять проект решения в работу")
    private void takeToWork() {
        click.waitAndClickWithRefresh("TakeToWorkButton");

        $x("//*[@data-item-marker='ApproveButton']")
                .shouldBe(Condition.visible, Duration.ofSeconds(60));

        log.info("Agro: проект решения взят в работу");
    }

    /**
     * Заполнение вкладки «Решение».
     * <p>
     * «Кредитный лимит» — специфика Микро, в рознице не заполняется. Инпут
     * ищется в скоупе контейнера поля: маркер лейбла составной
     * («BnzCreditLimit Кредитный лимит»), точное совпадение по нему не работает.
     */
    @Step("Агро: заполнение вкладки «Решение»")
    private void fillDecision(AgroDecisionData data) {
        log.info("Agro: fill decision tab (creditType='{}', creditLimit='{}')",
                data.getCreditType(), data.getCreditLimit());

        buttons.clickTabByName("Решение");

        lookup.select("Вид кредита для Проекта решения", data.getCreditType());
        setCreditLimit(data.getCreditLimit());

        log.info("Agro: decision tab filled");
    }

    /**
     * Утверждение решения.
     * <p>
     * После клика приходит модалка «Процесс успешно завершен» — её подтверждение
     * и есть признак, что действие обработано бэкендом. Кнопка «Утвердить» при
     * этом не исчезает, поэтому ожидание её скрытия недопустимо.
     */
    @Step("Агро: утвердить решение")
    private void approveDecision() {
        click.waitAndClick("ApproveButton");

        msgBox.shouldSeeModal("Процесс успешно завершен");
        click.clickByName("ОК");
        basePage.waitForPage();

        log.info("Agro: решение утверждено, процесс завершён");
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

    /** Числовое поле: sendKeys + TAB (setValue не триггерит onChange/onBlur). */
    private void setCreditLimit(String value) {
        log.debug("Set credit limit: '{}'", value);

        SelenideElement input = $x(
                "//div[@id='" + CREDIT_LIMIT_CONTAINER + "']//input"
        ).shouldBe(Condition.visible, Duration.ofSeconds(30));

        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        input.sendKeys(value);
        input.sendKeys(Keys.TAB);
    }
}