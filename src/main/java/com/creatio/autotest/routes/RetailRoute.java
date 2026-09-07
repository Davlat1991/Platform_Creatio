package com.creatio.autotest.routes;


import com.creatio.autotest.flows.retail.*;
import com.creatio.autotest.flows.retail.collateral.CollateralStageFlow;
import com.creatio.autotest.flows.retail.participants.ParticipantsStageFlow;
import com.creatio.autotest.flows.retail.registration.RegistrationStageFlow;
import com.creatio.autotest.flows.retail.registration.client.ClientFlowFactory;
import com.creatio.autotest.core.verify.VerificationCollector;
import com.creatio.autotest.utils.FieldUtils;
import com.creatio.autotest.testdata.ParticipantDocuments;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import com.creatio.autotest.testdata.models.TestScenario;
import com.creatio.autotest.testdata.models.UserCredentials;
import com.creatio.autotest.testdata.models.Workspace;
import io.qameta.allure.Step;

/**
 * Сборочный слой розничного маршрута (стандартный и упрощённый).
 * <p>
 * Возвращает верхний уровень розницы к LEGO-подходу по образцу
 * {@link MicroSegmentRoute}: маршрут раскладывается на три уровня, каждый
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
public class RetailRoute extends CreditApplicationRoute<RetailRoute> {

    public RetailRoute(TestScenario scenario) {
        super(scenario);
    }

    @Override
    protected RetailRoute self() {
        return this;
    }


    // ══════════════════════════════════════
    // УРОВЕНЬ 1 — СТАДИИ (без логина)
    // ══════════════════════════════════════

    /**
     * Стадия «Оформление». Идёт через двухаргументную перегрузку с клиентским
     * флоу — он заполняет обязательное «Тип занятости» на вкладке «Дополнительная
     * информация». Тип клиента резолвится по employmentType профиля
     * (OTHER_INCOME / EMPLOYEE / SELF_EMPLOYED).
     */
    public void registration() {
        log.info("Стадия «Оформление»");
        verifyProductRoundTrip();
        new RegistrationStageFlow().completeRegistrationStage(
                scenario.getRegistration(),
                ClientFlowFactory.from(scenario.getRegistration().getClientProfile())
        );
    }

    /**
     * ROUND-TRIP продукта (V2): выбранный на подборе продукт должен сохраниться
     * на карточке заявки. Читаем 4 поля из живого DOM карточки и сверяем с
     * ProductData сценария. Локаторы подтверждены по live DOM (page source):
     * «Продукт» — маркер FinAppProductName; Сумма/Срок мес./Валюта — по лейблу.
     * «Цель кредита» (BnzCreditSubPurpose) на карточке под другим лейблом и,
     * вероятно, под-цель — в round-trip пока не включена (требует отдельной сверки).
     */
    private void verifyProductRoundTrip() {
        var product = scenario.getProduct();
        VerificationCollector.check("Продукт (вид)",
                product.getProductKind(),
                FieldUtils.getValueByMarkerSafe("FinAppProductName"));
        VerificationCollector.checkNumeric("Сумма",
                product.getAmount(),
                FieldUtils.getValueByLabelSafe("Сумма"));
        VerificationCollector.check("Срок, мес.",
                product.getTerm(),
                FieldUtils.getValueByLabelSafe("Срок, мес."));
        VerificationCollector.check("Валюта",
                product.getCurrency(),
                FieldUtils.getValueByLabelSafe("Валюта"));
        VerificationCollector.flush("Оформление — продукт");
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

        documents.uploadAllParticipantDocuments(
                ParticipantDocuments.forParticipants(scenario.getParticipants()));

        documents.completeDocumentsActivity();
    }

    /** Стадия «Рассмотрение», сторона кредитного менеджера (STANDARD: КК4). */
    @Step("Стадия «Рассмотрение» (менеджер)")
    public void review() {
        log.info("Стадия «Рассмотрение»");
        new ReviewStageRetailFlow().completeReview(scenario.getRoute());
        verifyReviewReached();
    }

    /**
     * STATE-верификация (V1, первое демо): после «Рассмотрения» заявка имеет номер.
     * Свежее чтение поля «Number» из живого DOM (safe-ридер) сверяется с номером,
     * который стадия рассмотрения положила в ScenarioContext, и проверяется на непустоту.
     * Флоу не трогаем — проверка живёт на маршрутном шве.
     */
    private void verifyReviewReached() {
        String expected = ScenarioContextProvider.get().getApplicationNumber();
        String actual   = FieldUtils.getValueByMarkerSafe("Number");
        VerificationCollector.check("Номер заявки (контекст vs DOM)", expected, actual);
        VerificationCollector.checkNonEmpty("Номер заявки не пустой", actual);
        VerificationCollector.flush("Рассмотрение");
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
                .completeCashOperations();

        VerificationCollector.flush("Касса");
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
        // Стандартный финал не содержит проверок состояния ордера: ордера ИКОК-ГО
        // в системе не настроены (checkConfirmed*Order закомментированы в
        // ApplicationFinishFlow — R1 blocked). Верификация терминала — задача V8.
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
     * одна сессия (упрощённый маршрут). Стадии участников/обеспечения/документов
     * scenario-driven — теми же блоками, что стандарт: пустой сценарий даёт пустое
     * прохождение (участники пропускаются, обеспечение закрывается пустым, документы —
     * только заёмщик). Рассмотрение авто-переходит на информирование; верификатор
     * не участвует.
     */
    @Step("Сегмент: розничный менеджер — от создания заявки до информирования (упрощённый)")
    public void managerSimplifiedUpToNotification() {
        as(scenario.getManager(), Workspace.RETAIL_MANAGER, () -> {
            createApplication();
            registration();
            preliminaryCheck();
            participantsStage();
            collateralStage();
            documentCollection();
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
        // Упрощённый финал (ИКОК) проверяет состояние приходного и расходного
        // ордеров (Подтвержден) — эти проверки записываются в completeSimplifiedFinish.
        VerificationCollector.flush("Завершение");
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


}