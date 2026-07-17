package com.creatio.autotest.flows.micro;

import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.GridAssertions;
import com.creatio.autotest.pages.ConsultationPanelPage;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import com.creatio.autotest.testdata.models.CreditRoute;
import com.creatio.autotest.utils.FieldUtils;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.refresh;

/**
 * Стадия «Рассмотрение» (Агро/Микро) — сторона кредитного эксперта.
 * <p>
 * Изолированная Микро-копия розничного {@code ReviewStageRetailFlow}.
 * <p>
 * Ключевое отличие от розницы: продукты сегмента «Микро» идут по
 * <b>стандартному маршруту с самого начала заявки</b>, и при любом результате
 * скоринга заявка всё равно попадает в проекты решений. Поэтому ветвления по
 * {@link CreditRoute} здесь нет, а ожидаемое решение скоринга не является
 * параметром: в розничном образце {@code expectedDecision} фактически не
 * использовался ни в одной ветке — только логировался.
 * <p>
 * Решение скоринга намеренно не читается напрямую: оно может кратковременно
 * меняться («Отказать» → «Одобрить») и давать ложные падения. Надёжный
 * якорь финализации — появление решения комитета в гриде «Проекты решений».
 */
public class AgroReviewStageFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroReviewStageFlow.class);

    /** Маршрут рассмотрения для продуктов «Микро» — всегда стандартный. */
    private static final CreditRoute ROUTE = CreditRoute.STANDARD;

    /** Комитет, ожидаемый в гриде «Проекты решений». */
    private static final String COMMITTEE = "КК4";

    /** Сколько раз рефрешим страницу в ожидании решения комитета. */
    private static final int MAX_ATTEMPTS = 10;

    private final ButtonsComponent      buttons        = new ButtonsComponent();
    private final GridAssertions        gridAssertions = new GridAssertions();
    private final ConsultationPanelPage consultation   = new ConsultationPanelPage();

    // ══════════════════════════════════════
    // MAIN
    // ══════════════════════════════════════

    @Step("Агро: стадия «Рассмотрение» (кредитный эксперт)")
    public void completeReview() {
        log.info("Agro: complete review stage. Route: {}", ROUTE);

        consultation.closeIfOpened();

        verifyRoute(ROUTE);
        waitForCommitteeDecision();
        saveApplicationNumber();

        log.info("Agro: review completed. Application number: {}",
                ScenarioContextProvider.get().getApplicationNumber());
    }

    // ══════════════════════════════════════
    // STEPS
    // ══════════════════════════════════════

    /**
     * Проверка маршрута по детали «Утверждённые параметры».
     * <p>
     * Для Микро это заодно живая проверка бизнес-правила: маршрут обязан быть
     * стандартным независимо от суммы и результата скоринга.
     */
    @Step("Агро: проверка маршрута рассмотрения")
    private void verifyRoute(CreditRoute intended) {
        String actual = FieldUtils.getDetailInputValueScoped(
                "TsiApprovedFinApplicationSpecDetailContainer",
                "Маршрут рассмотрения"
        );
        log.info("Agro: route — intended: '{}', actual UI: '{}'",
                intended.getUiText(), actual);

        if (!actual.equalsIgnoreCase(intended.getUiText())) {
            throw new AssertionError(
                    "Маршрут не совпал. Ожидался '" + intended.getUiText() +
                            "', фактически '" + actual + "'"
            );
        }
    }

    /**
     * Ожидание решения комитета в гриде «Проекты решений».
     * <p>
     * Решение формируется бэкендом с задержкой (до ~60 с) и подтягивается
     * в грид только после рефреша страницы. Поэтому: рефреш → закрыть панель
     * консультации → открыть вкладку решения → длинное ожидание строки.
     */
    @Step("Агро: ожидание решения комитета ({COMMITTEE}) в «Проектах решений»")
    private void waitForCommitteeDecision() {
        log.info("Agro: waiting for committee decision '{}'", COMMITTEE);

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            refresh();
            consultation.closeAfterReload();
            openDecisionTab();

            if (gridAssertions.waitForCommitteeDecision(
                    COMMITTEE, Duration.ofSeconds(10))) {
                log.info("Agro: '{}' найдено на попытке {}/{}",
                        COMMITTEE, attempt, MAX_ATTEMPTS);
                return;
            }
            log.warn("Agro: '{}' не найдено на попытке {}/{} — повтор рефреша",
                    COMMITTEE, attempt, MAX_ATTEMPTS);
        }

        throw new AssertionError(
                "'" + COMMITTEE + "' не появилось в «Проектах решений» после " +
                        MAX_ATTEMPTS + " рефрешей");
    }

    @Step("Агро: открыть вкладку «Решение по заявке»")
    private void openDecisionTab() {
        buttons.clickTabByName("Решение по заявке");
    }

    /**
     * Сохранение 6-значного номера заявки в контекст сценария.
     * Номер нужен последующим стадиям и ролям для поиска заявки.
     */
    @Step("Агро: сохранение номера заявки в контекст")
    private void saveApplicationNumber() {
        String number = FieldUtils.getValueByMarker("Number");
        ScenarioContextProvider.get().setApplicationNumber(number);
        log.info("Agro: application number saved to context: {}", number);
    }
}