package com.creatio.autotest.tests;

import com.creatio.autotest.base.BaseTest;
import com.creatio.autotest.flows.*;
import com.creatio.autotest.flows.micro.AgroPreliminaryCheckFlow;
import com.creatio.autotest.flows.micro.AgroProductSelectionFlow;
import com.creatio.autotest.flows.micro.registration.*;
import com.creatio.autotest.flows.micro.registration.client.MicroClientFlowFactory;
import com.creatio.autotest.testdata.AgroScenarioFactory;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import com.creatio.autotest.testdata.models.AgroProductData;
import com.creatio.autotest.testdata.models.TestScenario;
import com.creatio.autotest.testdata.models.Workspace;
import com.creatio.autotest.utils.AllureTags;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

/**
 * E2E-маршрут продукта «Агро» (сегмент «Микро»).
 * <p>
 * Sprint 1 / подзадача 7: сборка реализованных на данный момент Агро-флоу
 * (подзадачи 1–6) в единый тест. Частичный прогон — тест проходит начало
 * маршрута и доходит до предела реализованных Агро-флоу регистрации
 * (общая информация + дополнительная информация). Стадия регистрации
 * целиком НЕ завершается: Агро-версии остальных под-флоу регистрации
 * (адрес, доходы/расходы, документы, скоринг-подтверждение, печать) ещё
 * не реализованы (Sprint 2).
 * <p>
 * Инфраструктурные флоу (Auth, Workspace, ClientSearch, ConsultationStart,
 * ApplicationCreation) переиспользуются из общей части — они не зависят от
 * продуктовой линейки. Отличие Микро: вход через роль «Кредитный эксперт»
 * (рабочее место {@code Workspace.CREDIT_EXPERT}), а не розничного менеджера.
 * Стыковка типов: {@link AgroProductData#toProductData()} для
 * {@code ApplicationCreationFlow}, типизированного под розничный ProductData.
 */
@Epic(AllureTags.Epics.AGRO)
@Feature(AllureTags.Features.E2E)
@Owner("Davlat")
public class AgroCreditApplicationTest extends BaseTest {

    // Номер заявки для debug-прогона. Переопределяется через -Dapp=NNNNN.
    private static final String RESUME_APPLICATION_NUMBER = "394969"; // ← подставь свой номер

    /**
     * DEBUG / RESUME — продолжение отладки с середины заявки.
     * <p>
     * Не гоняет регистрацию заново: логин Кредитным экспертом → открытие уже
     * созданной заявки по номеру → запуск только отлаживаемой стадии. Экономит
     * ~5 мин полного прогона на каждой итерации.
     * <p>
     * Заявка должна быть заранее доведена до нужной точки. Для стадии
     * «Предварительная проверка» — «Оформление» с заполненной анкетой заёмщика
     * и активностью «Добавьте и заполните анкеты участников заявки».
     * <p>
     * Номер: -Dapp=NNNNN или правка RESUME_APPLICATION_NUMBER. Запуск только этого метода:
     *   mvn test -Dtest=AgroCreditApplicationTest#agroMicroResumeFromApplication -Dapp=12345
     */
    @Test(groups = "resume", enabled = false)
    @Story("Агро/Микро — DEBUG: продолжение с середины заявки по номеру")
    @Description("Отладочный вход: логин кредитного эксперта, открытие существующей заявки " +
            "по номеру и запуск отлаживаемой стадии без повторного прогона регистрации.")
    @Severity(SeverityLevel.NORMAL)
    public void agroMicroResumeFromApplication() {

        String appNumber = System.getProperty("app", RESUME_APPLICATION_NUMBER);

        TestScenario scenario = AgroScenarioFactory.agroMicroHappyPath();

        AuthFlow auth = new AuthFlow();
        WorkspaceFlow workspace = new WorkspaceFlow();

        // ── Логин + рабочее место ──
        auth.login(scenario.getCreditExpert());
        workspace.select(Workspace.CREDIT_EXPERT);

        // ── Открыть существующую заявку по номеру ──
        new ApplicationSearchFlow()
                .withApplicationNumber(appNumber)
                .openByNumber();

        // ── RESUME POINT — отлаживаемая стадия. Меняй по мере продвижения Sprint 2. ──
        new AgroPreliminaryCheckFlow()
                .completePreliminaryCheckStage();

        auth.logout();
    }


   @Test(groups = "debug", enabled = true)
    @Story("Агро/Микро — начало маршрута (подбор продукта → создание заявки → частичная регистрация)")
    @Description("Частичный прогон реализованных Агро-флоу Sprint 1: логин кредитного эксперта, " +
            "поиск клиента, старт консультации, проверка сегмента клиента (Микро), подбор продукта " +
            "Агро, создание заявки, заполнение общей и дополнительной информации регистрации. " +
            "Останавливается на пределе реализованных Агро-флоу — стадия регистрации не завершается.")
    @Severity(SeverityLevel.BLOCKER)
    public void agroMicroPartialRoute() {

        TestScenario scenario = AgroScenarioFactory.agroMicroHappyPath();
        AgroProductData product = AgroScenarioFactory.agroMicroProduct();

        AuthFlow auth = new AuthFlow();
        WorkspaceFlow workspace = new WorkspaceFlow();

        // ── Шаг 1. Кредитный эксперт: логин + рабочее место ──
        // Продукты сегмента «Микро» ведёт Кредитный эксперт (не розничный менеджер).
        auth.login(scenario.getCreditExpert());
        workspace.select(Workspace.CREDIT_EXPERT);

        // ── Шаг 1. Поиск клиента по ФИО ──
        new ClientSearchFlow().searchClient(
                scenario.getContact().getLastName(),
                scenario.getContact().getFirstName(),
                scenario.getContact().getMiddleName()
        );

        // ── Шаг 2. Консультация ──
        // ВРЕМЕННО: проверка сегмента клиента отключена (неактуальна сейчас).
        // Поэтому консультация выполняется монолитно — как в рознице.
        // У Агро нет отдельной плитки продукта — marker темы тот же, что у розницы.
        new ConsultationStartFlow()
                .startConsultation(config.getConsultationTheme("retail"));

        // ── Шаг 2. Подбор продукта Агро ──
        new AgroProductSelectionFlow()
                .selectProduct(product);

        // ── Шаг 3. Создание заявки (через конвертацию в ProductData) ──
        new ApplicationCreationFlow()
                .createApplication(product.toProductData());

        // ── Шаг 4. Регистрация — реализованная часть ──
        // Общая информация (регион, источник, сектор экономики)
        new AgroRegistrationGeneralInfoFlow()
                .fillGeneralInfo(
                        scenario.getRegistration(),
                        AgroScenarioFactory.agroEconomicSector());

        // Участники заявки: добавить заёмщика → открывается анкета заёмщика
        new AgroRegistrationParticipantsFlow()
                .addBorrower();

        // Обязательные поля первой вкладки анкеты заёмщика — Микро-версии
        // (изолированы от розницы). Порядок: документы → адрес → доходы/расходы.
        new AgroRegistrationDocumentsFlow()
                .fillBorrowerDocuments(AgroScenarioFactory.agroBorrowerDocuments());

        AgroRegistrationAddressFlow addressFlow = new AgroRegistrationAddressFlow();
        addressFlow.fillAddresses(AgroScenarioFactory.agroAddressBase());
        addressFlow.setPzlRelation(false);

        new AgroRegistrationIncomeExpensesFlow()
                .fillIncomeAndExpenses(
                        scenario.getRegistration().getIncomeExpenses(),
                        AgroScenarioFactory.agroIncomeExtra());

        // Базовый скоринг (семейное положение, стаж) — вкладка «Дополнительная
        // информация» анкеты заёмщика.
        new AgroRegistrationAdditionalInfoFlow()
                .fillBaseScoringData(scenario.getRegistration().getScoringData());

        // Анкета клиента Микро — стратегия по типу занятости (data-driven).
        // Для фермера Агро → MicroSelfEmployedClientFlow.
        MicroClientFlowFactory
                .from(scenario.getRegistration().getClientProfile())
                .fill();

        new AgroRegistrationScoringConfirmationFlow()
                .confirmScoring(AgroScenarioFactory.agroScoringConfirmation());

        new AgroRegistrationPrintFlow()
                .printApplication();

        new AgroPreliminaryCheckFlow()
                .completePreliminaryCheckStage();


        auth.logout();
    }
}