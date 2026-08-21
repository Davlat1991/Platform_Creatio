package com.creatio.autotest.flows.micro;

import com.codeborne.selenide.Condition;
import com.creatio.autotest.components.DashboardComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.components.MenuComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.flows.common.ApplicationSearchFlow;
import com.creatio.autotest.pages.ConsultationPanelPage;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import com.creatio.autotest.testdata.models.AgroClientNotificationData;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

/**
 * Стадия «Информирование клиента» (Агро/Микро) — кредитный эксперт.
 * <p>
 * Не копия розничного {@code ClientNotificationStageFlow}, а его расширение:
 * розница знает одну мастер-страницу («Ответственный за подписание»), в Микро
 * их две.
 * <p>
 * Последовательность:
 * <ol>
 *   <li>«Завершить» на активности → результат «Клиент согласен» → сохранить;</li>
 *   <li>мастер-страница «Укажите дату и время идентификации клиента» —
 *       поле «Идентифицировал клиента» (мидл-контролер) → «Подтвердить»;</li>
 *   <li>мастер-страница «Укажите дату и время подписания договора» —
 *       поле «Ответственный за подписание» (ИКОК-ГО) → «Подтвердить»;</li>
 *   <li>возврат на карточку заявки, проверка поля «Мидл контроллер».</li>
 * </ol>
 * <p>
 * Даты и время на обеих мастер-страницах предзаполнены системой и намеренно
 * не изменяются.
 * <p>
 * «Мидл контролер» — функциональная роль внутри пользователя, а не отдельное
 * рабочее место: заводить {@code UserRole}/{@code Workspace} не требуется,
 * достаточно ФИО в тест-данных.
 */
public class AgroClientNotificationStageFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroClientNotificationStageFlow.class);

    /** Активность стадии. */
    private static final String NOTIFICATION_ACTIVITY = "Информирование клиента";

    /** Заголовки мастер-страниц — якоря готовности. */
    private static final String IDENTIFICATION_PAGE = "Укажите дату и время идентификации клиента";

    private static final String SIGNING_PAGE = "Укажите дату и время подписания договора";

    /** Инпут поля «Мидл контроллер» на карточке заявки (маркер EshIdentifiedClient). */
    private static final String MIDDLE_CONTROLLER_INPUT =
            "//input[@id='FinApplicationPageEshIdentifiedClientLookupEdit-el']";

    private final DashboardComponent    dashboard    = new DashboardComponent();
    private final LookupComponent       lookup       = new LookupComponent();
    private final MenuComponent         menu         = new MenuComponent();
    private final BasePage              basePage     = new BasePage();
    private final ClickService          click        = new ClickService();
    private final ConsultationPanelPage consultation = new ConsultationPanelPage();
    private final ApplicationSearchFlow searchFlow   = new ApplicationSearchFlow();

    // ══════════════════════════════════════
    // MAIN
    // ══════════════════════════════════════

    @Step("Агро: стадия «Информирование клиента»")
    public void completeClientNotification(AgroClientNotificationData data) {
        String applicationNumber =
                ScenarioContextProvider.get().getApplicationNumber();
        log.info("Agro: complete client notification, number='{}'", applicationNumber);

        if (applicationNumber == null || applicationNumber.isBlank()) {
            throw new IllegalStateException(
                    "Номер заявки отсутствует в контексте — сначала должен " +
                            "отработать AgroReviewStageFlow.completeReview()");
        }

        consultation.closeIfOpened();

        // Кредитный эксперт зашёл заново после верификатора — открываем заявку по номеру
        searchFlow.withApplicationNumber(applicationNumber).openByNumber();

        setClientAgreement(data.getClientAgreement());
        fillIdentificationPage(data.getMiddleControllerName());
        fillSigningPage(data.getSigningResponsible());
        verifyMiddleController(data.getMiddleControllerName());

        log.info("Agro: client notification completed");
    }

    // ══════════════════════════════════════
    // STEPS
    // ══════════════════════════════════════

    /**
     * «Завершить» на активности информирования → результат «Клиент согласен».
     * <p>
     * Розничный образец делает {@code setValue()} по полю Result и следом
     * {@code menu.clickByName()}. Фактически setValue там служит открывашкой
     * дропдауна (список не существует в DOM, пока не обратились к полю).
     * Здесь дропдаун открывается явным кликом — setValue не триггерит
     * onChange/onBlur и запрещён правилами проекта.
     */
    @Step("Агро: согласие клиента — «{agreement}»")
    private void setClientAgreement(String agreement) {
        log.info("Agro: set client agreement '{}'", agreement);

        consultation.closeIfOpened();
        dashboard.clickAndWaitMiniPage(NOTIFICATION_ACTIVITY, "Execute");

        openResultDropdown();
        menu.clickByName(agreement);
        click.clickByMarker("SaveEditButton");

        log.info("Agro: client agreement set");
    }

    /**
     * Мастер-страница «Укажите дату и время идентификации клиента».
     * Дата и время предзаполнены — не трогаем.
     */
    @Step("Агро: идентификация клиента — мидл-контролер «{middleController}»")
    private void fillIdentificationPage(String middleController) {
        log.info("Agro: identification page, middle controller='{}'", middleController);

        waitForMasterPage(IDENTIFICATION_PAGE);

        lookup.select("Идентифицировал клиента", middleController);
        click.clickByName("Подтвердить");

        log.info("Agro: identification confirmed");
    }

    /**
     * Мастер-страница «Укажите дату и время подписания договора».
     * Ответственным за подписание обязательно указывается ИКОК-ГО.
     */
    @Step("Агро: подписание договора — ответственный «{responsible}»")
    private void fillSigningPage(String responsible) {
        log.info("Agro: signing page, responsible='{}'", responsible);

        waitForMasterPage(SIGNING_PAGE);

        lookup.select("Ответственный за подписание", responsible);
        click.clickByName("Подтвердить");

        log.info("Agro: signing responsible confirmed");
    }

    /**
     * Возврат на карточку заявки и проверка, что поле «Мидл контроллер»
     * заполнено выбранным пользователем.
     */
    @Step("Агро: проверка поля «Мидл контроллер»")
    private void verifyMiddleController(String expected) {
        log.info("Agro: verify middle controller field, expected='{}'", expected);

        // Мастер-страница закрылась — ждём карточку заявки
        $x("//div[@data-item-marker='FinApplicationPageContainer']")
                .shouldBe(Condition.visible, Duration.ofSeconds(60));
        basePage.waitForPage();

        String actual = $x(MIDDLE_CONTROLLER_INPUT)
                .shouldBe(Condition.exist, Duration.ofSeconds(30))
                .getAttribute("value");

        log.info("Agro: middle controller field — expected: '{}', actual: '{}'",
                expected, actual);

        if (actual == null || !actual.trim().equalsIgnoreCase(expected.trim())) {
            throw new AssertionError(
                    "Поле «Мидл контроллер» не совпало. Ожидалось '" + expected +
                            "', фактически '" + actual + "'");
        }

        log.info("Agro: middle controller verified");
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

    /** Ожидание готовности мастер-страницы по её заголовку. */
    private void waitForMasterPage(String title) {
        log.debug("Wait for master page: '{}'", title);

        $x("//*[contains(normalize-space(text()),'" + title + "')]")
                .shouldBe(Condition.visible, Duration.ofSeconds(60));

        log.debug("Master page ready: '{}'", title);
    }

    /**
     * Открывает дропдаун результата: список не существует в DOM,
     * пока не обратились к полю.
     */
    private void openResultDropdown() {
        log.debug("Open activity result dropdown");

        $$x("//div[@data-item-marker='Result' or @data-item-marker='ProcessResult']//input")
                .filter(Condition.visible)
                .first()
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .click();
    }
}