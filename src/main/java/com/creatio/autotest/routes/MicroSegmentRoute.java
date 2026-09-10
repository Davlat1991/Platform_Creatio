package com.creatio.autotest.routes;


import com.creatio.autotest.flows.micro.*;
import com.creatio.autotest.flows.micro.collateral.AgroCollateralStageFlow;
import com.creatio.autotest.flows.micro.participants.AgroParticipantsStageFlow;
import com.creatio.autotest.flows.micro.registration.AgroRegistrationStageFlow;
import com.creatio.autotest.core.verify.VerificationCollector;
import com.creatio.autotest.testdata.AgroScenarioFactory;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import com.creatio.autotest.testdata.models.TestScenario;
import com.creatio.autotest.utils.FieldUtils;
import com.creatio.autotest.testdata.models.UserCredentials;
import com.creatio.autotest.testdata.models.Workspace;
import io.qameta.allure.Step;


/**
 * Сборочный слой маршрута «Агро» (сегмент «Микро»).
 * <p>
 * Назначение — вернуть верхний уровень маршрута к LEGO-подходу. Раньше полная
 * последовательность жила внутри тест-метода и не переиспользовалась: собрать
 * из неё сокращённый прогон, отказную ветку или сценарий без кассира было
 * нельзя. Здесь маршрут разложен на три уровня, каждый следующий собирается
 * из предыдущего:
 * <ol>
 *   <li><b>Стадии</b> — {@link #createApplication()}, {@link #registration()},
 *       {@link #review()} и т.д. Логина не содержат, выполняются под уже
 *       активной сессией.</li>
 *   <li><b>Сессия под ролью</b> — {@link #as(UserCredentials, Workspace, Runnable)}:
 *       логин, выбор рабочего места, тело, гарантированный логаут.</li>
 *   <li><b>Ролевые сегменты</b> — {@link #creditExpertUpToReview()} и другие:
 *       готовые куски маршрута, собранные из первых двух уровней.</li>
 * </ol>
 * Полный маршрут = семь вызовов ролевых сегментов. Произвольный сценарий
 * («только регистрация», «до рассмотрения и стоп») собирается из {@code as(...)}
 * и нужных стадий без единой новой строки в этом классе.
 * <p>
 * <b>Роль не зашита в бизнес-логику.</b> Флоу ничего не знают об учётках и
 * рабочих местах — вся привязка ролей живёт здесь. Практическая проверка уже
 * была: перенос прикрепления кассового ордера с кредитного эксперта на
 * мидл-контролера стоил одной строки (см. {@link #finishApplication()}).
 * <p>
 * <b>Состояния нет.</b> Поля — только неизменяемые ссылки на сценарий и продукт
 * плюс инфраструктурные флоу без состояния. Объект создаётся локально в
 * тест-методе, поэтому параллельный запуск не ломается: драйвер и
 * {@code ScenarioContext} остаются ThreadLocal.
 * <p>
 * Передача данных между ролями идёт через {@code ScenarioContextProvider}:
 * номер заявки пишет {@code AgroReviewStageFlow}, номер кредитного договора и
 * депозитного счёта — {@code AgroSigningStageFlow}. Границы сегментов
 * намеренно совпадают с этими точками передачи — поэтому в любой сегмент можно
 * войти отдельно, предварительно засеяв контекст через {@link #resumeAt(String)}
 * или {@link #resumeWithContract(String)}.
 */
public class MicroSegmentRoute extends CreditApplicationRoute<MicroSegmentRoute> {

    public MicroSegmentRoute(TestScenario scenario) {
        super(scenario);
    }

    @Override
    protected MicroSegmentRoute self() {
        return this;
    }


    // ══════════════════════════════════════
    // УРОВЕНЬ 3 — РОЛЕВЫЕ СЕГМЕНТЫ
    // ══════════════════════════════════════

    /**
     * Кредитный эксперт: от подбора продукта до рассмотрения.
     * Продукты сегмента «Микро» ведёт кредитный эксперт, не розничный менеджер.
     * По завершении номер заявки лежит в {@code ScenarioContext}.
     */
    @Step("Сегмент: кредитный эксперт — от подбора продукта до рассмотрения")
    public void creditExpertUpToReview() {
        as(scenario.getCreditExpert(), Workspace.CREDIT_EXPERT, () -> {
            createApplication();
            registration();
            preliminaryCheck();
            participantsStage();
            collateralStage();
            documentCollection();
            review();
        });
    }

    /** Верификатор: утверждение проекта решения. */
    @Step("Сегмент: верификатор — утверждение проекта решения")
    public void underwriterApprove() {
        as(scenario.getUnderwriter(), Workspace.UNDERWRITER, this::approveDecision);
    }

    /** Кредитный эксперт: информирование клиента. */
    @Step("Сегмент: кредитный эксперт — информирование клиента")
    public void clientNotification() {
        as(scenario.getCreditExpert(), Workspace.CREDIT_EXPERT, this::notifyClient);
    }

    /**
     * Мидл-контролер: вложение документов заёмщика.
     * Мидл-контролер — функциональная роль, вход через рабочее место ИКОК.
     */
    @Step("Сегмент: мидл-контролер — вложение документов заёмщика")
    public void middleControllerDocuments() {
        as(scenario.getMiddleController(), Workspace.IKOK, this::attachBorrowerDocuments);
    }

    /** ИКОК-ГО: стадия «Подписание» целиком, включая выдачу и оферту. */
    @Step("Сегмент: ИКОК-ГО — стадия «Подписание»")
    public void ikokGoSigning() {
        as(scenario.getIkokGo(), Workspace.IKOK_GO, this::signing);
    }

    /**
     * Кассир Микро: подтверждение приходного и расходного ордеров.
     * Отдельная учётка кассира для Микро нужна для прозрачности прогонов и
     * логов — функционально роль та же, кассир подразделения видит все ордера.
     */
    @Step("Сегмент: кассир Микро — подтверждение кассовых ордеров")
    public void cashierOperations() {
        as(scenario.getMicroCashier(), Workspace.CASHIER, this::confirmCashOrders);
    }

    /**
     * Завершение заявки — три шага под тремя ролями подряд.
     * <p>
     * По отдельности эти шаги не имеют смысла, поэтому они собраны в один
     * сегмент, но внутри остаются тремя независимыми сессиями.
     * <p>
     * Прикрепление расходного ордера сейчас выполняет мидл-контролер: так
     * устроена текущая доработка процесса Микро. Когда доработку завершат,
     * здесь вернётся {@code scenario.getCreditExpert()} и
     * {@code Workspace.CREDIT_EXPERT} — правка одной строки, флоу не меняется.
     */

    @Step("Сегмент: завершение заявки")
    public void finishApplication() {
        as(scenario.getMiddleController(), Workspace.IKOK, this::attachOrder);
        as(scenario.getIkokGo(), Workspace.IKOK_GO, this::ikokGoConfirm);
        as(scenario.getCreditExpert(), Workspace.CREDIT_EXPERT, this::finishViaConsultation);
        // Проверок состояния ордера нет: отображение ордеров под ИКОК-ГО не настроено
        // (AgroApplicationFinishFlow.completeIkokGoConfirm, стр. 95 — R1 blocked).
        // Верификация терминала — задача V8.
    }

    // ══════════════════════════════════════
    // УРОВЕНЬ 1 — СТАДИИ (без логина)
    // ══════════════════════════════════════

    /**
     * Стадия «Оформление» целиком: общая информация, анкета заёмщика,
     * профиль клиента, подтверждение скоринга, печать.
     */
    @Step("Стадия «Оформление»")
    public void registration() {
        new AgroRegistrationStageFlow()
                .completeRegistrationStage(scenario.getRegistration());
        VerificationCollector.flush("Оформление (Агро)");
    }

    /** Стадия «Предварительная проверка». */
    @Step("Стадия «Предварительная проверка»")
    public void preliminaryCheck() {
        new AgroPreliminaryCheckFlow()
                .completePreliminaryCheckStage();
    }

    /** Стадия «Участники»: состав берётся из сценария. */
    @Step("Стадия «Участники»")
    public void participantsStage() {
        new AgroParticipantsStageFlow()
                .completeParticipantsStage(scenario.getParticipants());
    }

    /** Стадия «Обеспечение»: состав залогов берётся из сценария. */
    @Step("Стадия «Обеспечение»")
    public void collateralStage() {
        new AgroCollateralStageFlow()
                .completeCollateralStage(scenario.getCollaterals());
    }

    /**
     * Стадия «Сбор документов».
     * По сценарию Микро документы здесь не прикрепляются — активность просто
     * закрывается. Документ заёмщика вкладывается позже, мидл-контролером.
     */
    @Step("Стадия «Сбор документов»")
    public void documentCollection() {
        new AgroDocumentCollectionStageFlow()
                .completeDocumentCollectionStage();
    }


    /** Стадия «Рассмотрение», сторона кредитного эксперта. Пишет номер заявки в контекст. */
    @Step("Стадия «Рассмотрение» (кредитный эксперт)")
    public void review() {
        new AgroReviewStageFlow()
                .completeReview();
        verifyReviewReached();
    }

    /**
     * STATE-верификация (V1): после «Рассмотрения» проект решения КК сформирован
     * (пишется в AgroReviewStageFlow) и заявка имеет номер. Свежее чтение поля
     * «Number» из живого DOM сверяется с номером в ScenarioContext и проверяется
     * на непустоту. Флоу не трогаем — проверка живёт на маршрутном шве.
     */
    private void verifyReviewReached() {
        String expected = ScenarioContextProvider.get().getApplicationNumber();
        String actual   = FieldUtils.getValueByMarkerSafe("Number");
        VerificationCollector.check("Номер заявки (контекст vs DOM)", expected, actual);
        VerificationCollector.checkNonEmpty("Номер заявки не пустой", actual);
        VerificationCollector.flush("Рассмотрение");
    }


    /** Утверждение проекта решения верификатором. */
    @Step("Утверждение проекта решения")
    public void approveDecision() {
        new AgroReviewStageUnderwriterFlow()
                .approveReview(AgroScenarioFactory.agroDecisionData());
    }

    /** Стадия «Информирование клиента». */
    @Step("Стадия «Информирование клиента»")
    public void notifyClient() {
        new AgroClientNotificationStageFlow()
                .completeClientNotification(
                        AgroScenarioFactory.agroClientNotificationData(
                                scenario.getMiddleController(),
                                scenario.getIkokGo()));
    }

    /**
     * Вложение единственного документа заёмщика (Шиносномаи ЧТ).
     * На «Сборе документов» по сценарию Микро ничего не прикрепляли.
     */
    @Step("Вложение документов заёмщика")
    public void attachBorrowerDocuments() {
        new AgroMiddleControllerDocumentsFlow()
                .attachBorrowerDocuments(
                        AgroScenarioFactory.agroMiddleControllerDocuments());
    }

    /**
     * Стадия «Подписание» целиком.
     * <p>
     * Часть 1: решение КК → кредитный договор → депозитный счёт → график →
     * привязка счёта → договоры обеспечения → закрытие активности.
     * Часть 2: подписание → выдача наличными → оферта.
     * Обе части выполняются на одной открытой странице кредитного договора,
     * поэтому делить их на отдельные стадии нельзя. Номер договора уходит
     * в {@code ScenarioContext} — нужен кассиру и завершению заявки.
     */
    @Step("Стадия «Подписание» и выдача")
    public void signing() {
        AgroSigningStageFlow signingFlow = new AgroSigningStageFlow();
        signingFlow.completeSigningStage();
        signingFlow.signAndIssueCredit();
    }

    /** Подтверждение приходного и расходного кассовых ордеров. */
    @Step("Кассовые операции")
    public void confirmCashOrders() {
        new AgroCashierFlow()
                .completeCashOperations();
        VerificationCollector.flush("Касса");
    }


    /** Прикрепление расходного кассового ордера и закрытие активности. */
    @Step("Прикрепление расходного кассового ордера")
    public void attachOrder() {
        new AgroApplicationFinishFlow()
                .completeAttachOrder();
    }

    /** Скачивание ордера и подтверждение получения документа под ИКОК-ГО. */
    @Step("Подтверждение получения документа (ИКОК-ГО)")
    public void ikokGoConfirm() {
        new AgroApplicationFinishFlow()
                .completeIkokGoConfirm();
    }

    /** Завершение заявки через консультационную панель. */
    @Step("Завершение заявки через консультационную панель")
    public void finishViaConsultation() {
        new AgroApplicationFinishFlow()
                .completeApplicationViaConsultation();
    }

    // ══════════════════════════════════════
    // СБОРКА — ПОЛНЫЙ МАРШРУТ
    // ══════════════════════════════════════

    /**
     * Полный маршрут Агро/Микро — семь ролевых сегментов подряд.
     * Паритет с {@code RetailRoute.runStandard()}: собирает маршрут одной строкой,
     * тест выбирает только сценарий. Порядок совпадает с E2E в AgroCreditApplicationTest.
     */
    @Step("Агро/Микро маршрут — полный E2E")
    public void runFull() {
        creditExpertUpToReview();
        underwriterApprove();
        clientNotification();
        middleControllerDocuments();
        ikokGoSigning();
        cashierOperations();
        finishApplication();
    }



}