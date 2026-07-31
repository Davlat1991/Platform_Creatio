package com.creatio.autotest.routes;

import com.creatio.autotest.core.config.Config;
import com.creatio.autotest.flows.*;
import com.creatio.autotest.flows.collateral.CollateralStageFlow;
import com.creatio.autotest.flows.participants.ParticipantsStageFlow;
import com.creatio.autotest.flows.registration.RegistrationStageFlow;
import com.creatio.autotest.testdata.ScenarioFactory;
import com.creatio.autotest.testdata.models.TestScenario;
import com.creatio.autotest.testdata.models.UserCredentials;
import com.creatio.autotest.testdata.models.Workspace;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import com.creatio.autotest.flows.registration.client.ClientFlowFactory;

/**
 * Сборочный слой розничного маршрута (стандартный и упрощённый).
 * <p>
 * Возвращает верхний уровень розницы к LEGO-подходу по образцу
 * {@link AgroMicroRoute}: маршрут раскладывается на три уровня, каждый
 * следующий собирается из предыдущего:
 * <ol>
 *   <li><b>Стадии</b> — процессные шаги без логина, под уже активной сессией;</li>
 *   <li><b>Сессия под ролью</b> — {@link #as(UserCredentials, Workspace, Runnable)}:
 *       логин, рабочее место, тело, гарантированный логаут;</li>
 *   <li><b>Ролевые сегменты</b> — готовые куски маршрута из первых двух уровней.</li>
 * </ol>
 * Стандартный и упрощённый маршруты собираются из одного набора стадий разным
 * составом — как happy-path и regression в Агро.
 * <p>
 * Состояния нет: поле только неизменяемая ссылка на сценарий плюс
 * инфраструктурные флоу без состояния. Объект создаётся локально в тест-методе,
 * поэтому параллельный запуск не ломается (драйвер и {@code ScenarioContext}
 * остаются ThreadLocal).
 */
public class RetailRoute {

    private static final Logger log        = LoggerFactory.getLogger(RetailRoute.class);

    private final Config        config     = Config.getInstance();
    private final AuthFlow      auth       = new AuthFlow();
    private final WorkspaceFlow workspace  = new WorkspaceFlow();

    private final TestScenario scenario;

    public RetailRoute(TestScenario scenario) {
        this.scenario = scenario;
    }

    // ══════════════════════════════════════
    // УРОВЕНЬ 2 — СЕССИЯ ПОД РОЛЬЮ
    // ══════════════════════════════════════

    /**
     * Выполняет тело под указанной учётной записью и рабочим местом.
     * Логаут вызывается всегда. На упавшем сегменте логаут «тихий», чтобы его
     * собственное падение не подменило исходную причину в отчёте.
     */
    @Step("Сессия под ролью {0}, рабочее место {1}")
    public void as(UserCredentials user, Workspace ws, Runnable body) {
        log.info("── Сегмент: {} / рабочее место «{}» ──", user, ws.getUiName());

        auth.login(user);
        workspace.select(ws);
        body.run();

        // Логаут только при штатном завершении сегмента. При падении логаут НЕ
        // делаем: браузер остаётся на экране ошибки для ручного разбора и для
        // скриншота в отчёте. Драйвер закрывается в teardown BaseTest.
        auth.logout();
    }



    // ══════════════════════════════════════
    // УРОВЕНЬ 1 — СТАДИИ (без логина)
    // ══════════════════════════════════════

    /** Поиск клиента, консультация, подбор продукта, создание заявки. */
    @Step("Стадия: поиск клиента → консультация → подбор продукта → создание заявки")
    public void createApplication() {
        log.info("Стадия «Создание заявки»");

        new ClientSearchFlow().searchClient(
                scenario.getContact().getLastName(),
                scenario.getContact().getFirstName(),
                scenario.getContact().getMiddleName()
        );

        new ConsultationStartFlow()
                .startConsultation(config.getConsultationTheme("retail"));

        new ProductSelectionFlow()
                .selectProduct(scenario.getProduct());

        new ApplicationCreationFlow()
                .createApplication(scenario.getProduct());
    }

    /**
     * Стадия «Оформление». Идёт через двухаргументную перегрузку с клиентским
     * флоу — он заполняет обязательное «Тип занятости» на вкладке «Дополнительная
     * информация». Тип клиента резолвится по employmentType профиля
     * (OTHER_INCOME / EMPLOYEE / SELF_EMPLOYED).
     */
    @Step("Стадия «Оформление»")
    public void registration() {
        log.info("Стадия «Оформление»");
        new RegistrationStageFlow().completeRegistrationStage(
                scenario.getRegistration(),
                ClientFlowFactory.from(scenario.getRegistration().getClientProfile())
        );
    }

    /** Стадия «Предварительная проверка». */
    @Step("Стадия «Предварительная проверка»")
    public void preliminaryCheck() {
        log.info("Стадия «Предварительная проверка»");
        new PreliminaryCheckStageFlow()
                .completePreliminaryCheckStage();
    }

    /** Стадия «Участники»: состав и доходы/расходы берутся из сценария. */
    @Step("Стадия «Участники»")
    public void participantsStage() {
        log.info("Стадия «Участники»");
        new ParticipantsStageFlow()
                .completeParticipantsStage(
                        scenario.getParticipants(),
                        scenario.getRegistration().getIncomeExpenses()
                );
    }

    /** Стадия «Обеспечение»: состав залогов берётся из сценария. */
    @Step("Стадия «Обеспечение»")
    public void collateralStage() {
        log.info("Стадия «Обеспечение»");
        new CollateralStageFlow()
                .completeCollateralStage(scenario.getCollaterals());
    }

    /**
     * Стадия «Сбор документов»: заёмщик (подготовка + загрузка), затем документы
     * поручителя и залогодателя.
     */
    @Step("Стадия «Сбор документов»")
    public void documentCollection() {
        log.info("Стадия «Сбор документов»");

        DocumentCollectionStageFlow documents = new DocumentCollectionStageFlow();
        documents.openDocumentsTab();

        documents.prepareBorrowerDetails();
        documents.uploadBorrowerDocuments();

        documents.uploadAllParticipantDocuments(ScenarioFactory.documentOwners());

        documents.completeDocumentsActivity();
    }

    /** Стадия «Рассмотрение», сторона кредитного менеджера (STANDARD: КК4). */
    @Step("Стадия «Рассмотрение» (менеджер)")
    public void review() {
        log.info("Стадия «Рассмотрение»");
        new ReviewStageRetailFlow().completeReview(
                scenario.getRoute(),
                scenario.getExpectedScoringDecision()
        );
    }

    /**
     * Стадия «Обеспечение» для упрощённого маршрута: залогов нет, но стадию
     * обязательно закрыть, иначе процесс не переходит к сбору документов.
     * No-arg перегрузка CollateralStageFlow — только закрытие стадии.
     */
    @Step("Стадия «Обеспечение» (упрощённый — без залогов)")
    public void collateralStageClose() {
        log.info("Стадия «Обеспечение» (упрощённый, без залогов)");
        new CollateralStageFlow().completeCollateralStage();
    }

    /**
     * Стадия «Сбор документов» для упрощённого маршрута: только документы
     * заёмщика (участников нет).
     */
    @Step("Стадия «Сбор документов» (упрощённый — только заёмщик)")
    public void documentCollectionBorrowerOnly() {
        log.info("Стадия «Сбор документов» (упрощённый, только заёмщик)");
        DocumentCollectionStageFlow documents = new DocumentCollectionStageFlow();
        documents.openDocumentsTab();
        documents.prepareBorrowerDetails();
        documents.uploadBorrowerDocuments();
        documents.completeDocumentsActivity();
    }

    /**
     * Стадия «Информирование клиента» для упрощённого маршрута: без поиска,
     * цель — ИКОК (в стандартном — с поиском, цель ИКОК-ГО).
     */
    @Step("Стадия «Информирование клиента» (упрощённый)")
    public void notifyClientSimplified() {
        log.info("Стадия «Информирование клиента» (упрощённый)");
        new ClientNotificationStageFlow()
                .completeClientNotification(scenario.getIkok().getDisplayName());
    }


    /** Утверждение проекта решения верификатором (STANDARD: КК4). */
    @Step("Утверждение проекта решения")
    public void approveDecision() {
        log.info("Стадия «Утверждение решения» (верификатор)");
        new ReviewStageUnderwriterFlow()
                .approveReview("КК4 по заявке");
    }

    /** Стадия «Информирование клиента» (STANDARD: с поиском, цель — ИКОК-ГО). */
    @Step("Стадия «Информирование клиента»")
    public void notifyClient() {
        log.info("Стадия «Информирование клиента»");
        new ClientNotificationStageFlow()
                .completeClientNotificationWithSearch(
                        scenario.getIkokGo().getDisplayName());
    }

    /**
     * Стадия «Подписание» и выдача кредита. Если в заявке есть поручитель/
     * залогодатель или хотя бы один залог — на открытом договоре регистрируются
     * договоры обеспечения. Пишет номер договора и депозитного счёта в контекст.
     */
    @Step("Стадия «Подписание» и выдача")
    public void issueLoan() {
        boolean hasSecuredItems =
                (scenario.getParticipants() != null && !scenario.getParticipants().isEmpty())
                        || (scenario.getCollaterals() != null && !scenario.getCollaterals().isEmpty());
        log.info("Стадия «Подписание» и выдача (договоры обеспечения={})", hasSecuredItems);
        new SigningStageFlow().issueLoan(hasSecuredItems, false);
    }


    /**
     * Кассовые операции: подтверждение приходного и расходного ордеров.
     * Номер договора берётся из {@code ScenarioContext} (его пишет стадия
     * подписания) — раньше монолит искал договор по неустановленному полю.
     */
    @Step("Кассовые операции")
    public void cashierOrders() {
        String contractNumber =
                ScenarioContextProvider.get().getCreditContractNumber();
        log.info("Стадия «Касса»: договор='{}'", contractNumber);

        new CashierFlow()
                .withContractNumber(contractNumber)
                .completeCashOperations();
    }

    /** Завершение (ИКОК-ГО): прикрепление документа розничным менеджером. */
    @Step("Завершение: прикрепление документа (менеджер)")
    public void managerAttach() {
        log.info("Завершение: прикрепление документа менеджером");
        new ApplicationFinishFlow().completeManagerAttach();
    }

    /** Завершение (ИКОК-ГО): подтверждение получения документа. */
    @Step("Завершение: подтверждение получения документа (ИКОК-ГО)")
    public void ikokGoConfirm() {
        log.info("Завершение: подтверждение получения документа (ИКОК-ГО)");
        new ApplicationFinishFlow().completeIkokGoFinish();
    }

    /** Завершение заявки: розничный менеджер закрывает заявку через консультацию. */
    @Step("Завершение через консультационную панель (менеджер)")
    public void finishViaConsultation() {
        log.info("Завершение заявки менеджером через консультацию");
        new ApplicationFinishFlow().completeIkokGoManagerFinish();
    }


    // ══════════════════════════════════════
    // УРОВЕНЬ 3 — РОЛЕВЫЕ СЕГМЕНТЫ (стандартный маршрут)
    // ══════════════════════════════════════

    /**
     * Розничный менеджер: от создания заявки до рассмотрения.
     * По завершении номер заявки лежит в {@code ScenarioContext}
     * (его пишет {@code ReviewStageRetailFlow}).
     */
    @Step("Сегмент: розничный менеджер — от создания заявки до рассмотрения")
    public void managerUpToReview() {
        as(scenario.getManager(), Workspace.RETAIL_MANAGER, () -> {
            createApplication();
            registration();
            preliminaryCheck();
            participantsStage();
            collateralStage();
            documentCollection();
            review();
        });
    }

    /** Верификатор: утверждение проекта решения (КК4). */
    @Step("Сегмент: верификатор — утверждение проекта решения")
    public void underwriterApprove() {
        as(scenario.getUnderwriter(), Workspace.UNDERWRITER, this::approveDecision);
    }

    /** Розничный менеджер: информирование клиента. */
    @Step("Сегмент: розничный менеджер — информирование клиента")
    public void managerNotification() {
        as(scenario.getManager(), Workspace.RETAIL_MANAGER, this::notifyClient);
    }

    /** ИКОК-ГО: стадия «Подписание» и выдача кредита. */
    @Step("Сегмент: ИКОК-ГО — подписание и выдача")
    public void ikokGoIssuance() {
        as(scenario.getIkokGo(), Workspace.IKOK_GO, this::issueLoan);
    }

    /** Кассир: подтверждение приходного и расходного ордеров. */
    @Step("Сегмент: кассир — кассовые операции")
    public void cashierOperations() {
        as(scenario.getCashier(), Workspace.CASHIER, this::cashierOrders);
    }

    /**
     * Завершение заявки — три сессии под тремя ролями подряд.
     * По отдельности шаги смысла не имеют, поэтому собраны в один сегмент,
     * но внутри остаются тремя независимыми сессиями.
     */
    @Step("Сегмент: завершение заявки")
    public void finishApplication() {
        as(scenario.getManager(), Workspace.RETAIL_MANAGER, this::managerAttach);
        as(scenario.getIkokGo(),  Workspace.IKOK_GO,        this::ikokGoConfirm);
        as(scenario.getManager(), Workspace.RETAIL_MANAGER, this::finishViaConsultation);
    }

    // ══════════════════════════════════════
    // СБОРКА — СТАНДАРТНЫЙ МАРШРУТ
    // ══════════════════════════════════════

    /** Полный стандартный маршрут: шесть ролевых сегментов. */
    @Step("Стандартный маршрут — полный E2E")
    public void runStandard() {
        managerUpToReview();
        underwriterApprove();
        managerNotification();
        ikokGoIssuance();
        cashierOperations();
        finishApplication();
    }


    // ══════════════════════════════════════
    // УРОВЕНЬ 3 — РОЛЕВЫЕ СЕГМЕНТЫ (упрощённый маршрут)
    // ══════════════════════════════════════

    /**
     * Розничный менеджер: от создания заявки до информирования включительно —
     * одна сессия. Без участников и верификатора; обеспечение закрывается
     * пустым; документы — только заёмщик; рассмотрение авто-переходит на
     * информирование.
     */
    @Step("Сегмент: розничный менеджер — от создания заявки до информирования (упрощённый)")
    public void managerSimplifiedUpToNotification() {
        as(scenario.getManager(), Workspace.RETAIL_MANAGER, () -> {
            createApplication();
            registration();
            preliminaryCheck();
            collateralStageClose();
            documentCollectionBorrowerOnly();
            review();
            notifyClientSimplified();
        });
    }

    /** ИКОК: подписание и выдача. Без договоров обеспечения, с проверкой ордеров. */
    @Step("Сегмент: ИКОК — подписание и выдача (упрощённый)")
    public void ikokIssuance() {
        as(scenario.getIkok(), Workspace.IKOK,
                () -> new SigningStageFlow().issueLoan(false, true));
    }

    /** ИКОК: завершение заявки (упрощённый маршрут — одна сессия). */
    @Step("Сегмент: ИКОК — завершение заявки (упрощённый)")
    public void simplifiedFinish() {
        as(scenario.getIkok(), Workspace.IKOK,
                () -> new ApplicationFinishFlow().completeSimplifiedFinish());
    }

    // ══════════════════════════════════════
    // СБОРКА — УПРОЩЁННЫЙ МАРШРУТ
    // ══════════════════════════════════════

    /** Полный упрощённый маршрут: четыре сегмента. */
    @Step("Упрощённый маршрут — полный E2E")
    public void runSimplified() {
        managerSimplifiedUpToNotification();
        ikokIssuance();
        cashierOperations();
        simplifiedFinish();
    }

    // ══════════════════════════════════════
    // ТОЧКИ ВХОДА В СЕРЕДИНУ МАРШРУТА
    // ══════════════════════════════════════

    /**
     * Засев номера заявки в контекст — вход в сегменты, которым нужна уже
     * созданная заявка (информирование, подписание).
     */
    @Step("Контекст: номер заявки = {0}")
    public RetailRoute resumeAt(String applicationNumber) {
        ScenarioContextProvider.get().setApplicationNumber(applicationNumber);
        log.info("Контекст засеян номером заявки: {}", applicationNumber);
        return this;
    }

    /**
     * Засев номера кредитного договора — вход в сегменты, которым нужен уже
     * созданный договор (касса, завершение заявки).
     */
    @Step("Контекст: номер кредитного договора = {0}")
    public RetailRoute resumeWithContract(String contractNumber) {
        ScenarioContextProvider.get().setCreditContractNumber(contractNumber);
        log.info("Контекст засеян номером договора: {}", contractNumber);
        return this;
    }
}