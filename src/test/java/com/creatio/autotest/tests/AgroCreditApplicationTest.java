package com.creatio.autotest.tests;

import com.creatio.autotest.base.BaseTest;
import com.creatio.autotest.routes.MicroSegmentRoute;
import com.creatio.autotest.testdata.AgroScenarioFactory;
import com.creatio.autotest.testdata.models.TestScenario;
import com.creatio.autotest.testdata.models.Workspace;
import com.creatio.autotest.utils.AllureTags;
import io.qameta.allure.*;
import org.testng.annotations.Test;

/**
 * E2E-маршруты продукта «Агро» (сегмент «Микро»).
 * <p>
 * Тест — сборка, а не последовательность. Вся оркестрация вынесена в
 * {@link MicroSegmentRoute}: полный маршрут собирается из семи ролевых сегментов,
 * сокращённый — из тех же сегментов с другим сценарием, debug-входы — из
 * отдельных сегментов с засеянным контекстом. Дублирования между сценариями
 * нет: различается только состав вызовов.
 * <p>
 * Роли по стадиям: кредитный эксперт (заявка → рассмотрение, информирование
 * клиента, завершение), верификатор (утверждение проекта решения),
 * мидл-контролер (документы заёмщика, прикрепление кассового ордера),
 * ИКОК-ГО (подписание, подтверждение получения документа), кассир Микро
 * (подтверждение кассовых ордеров).
 * <p>
 * Известные ограничения на стороне настроек системы, не автотеста: модальное
 * окно выбора должностного лица при печати договоров обеспечения не
 * открывается; кассовые ордера не отображаются под ролью ИКОК-ГО.
 * Соответствующие шаги реализованы и включатся без изменения кода.
 */
@Epic(AllureTags.Epics.AGRO)
@Feature(AllureTags.Features.E2E)
@Owner("Davlat")
public class AgroCreditApplicationTest extends BaseTest {

    // Номер заявки для debug-прогонов. Переопределяется через -Dapp=NNNNN.
    private static final String RESUME_APPLICATION_NUMBER = "392008";
    // Номер кредитного договора для debug-прогонов. Переопределяется через -Dcontract=...
    private static final String RESUME_CONTRACT_NUMBER = "02-008-033680";

    // ══════════════════════════════════════
    // ОСНОВНОЙ E2E-ПРОГОН
    // ══════════════════════════════════════

    @Test(groups = "e2e", enabled = true)
    @Story("Агро/Микро — полный маршрут: от подбора продукта до завершения заявки")
    @Description("Сквозной прогон продукта Агро сегмента «Микро»: подбор продукта, создание " +
            "заявки, регистрация, предварительная проверка, участники, обеспечение, сбор " +
            "документов, рассмотрение и утверждение верификатором, информирование клиента, " +
            "вложение документов мидл-контролером, стадия «Подписание» под ИКОК-ГО " +
            "(решение КК, кредитный договор, депозитный счёт, график, привязка счёта, " +
            "договоры обеспечения с печатными формами, подписание, выдача наличными, оферта), " +
            "кассовые операции под кассиром Микро и завершение заявки.")
    @Severity(SeverityLevel.BLOCKER)
    public void agroMicroFullRoute() {
        route(AgroScenarioFactory.agroMicroHappyPath()).runFull();
    }

    // ══════════════════════════════════════
    // СОКРАЩЁННЫЙ РЕГРЕССИОННЫЙ ПРОГОН
    // ══════════════════════════════════════

    /**
     * Тот же маршрут на минимальном составе: заёмщик, поручитель, один залог
     * «Будущий урожай». Проверяет, что флоу участников и обеспечения корректно
     * отрабатывают списки из одного элемента.
     * <p>
     * Отличие от полного прогона — одна строка: другой сценарий.
     * <p>
     * Включается отдельным прогоном (шаг 3 плана), чтобы не удваивать время
     * подтверждающего запуска полного маршрута.
     */
    @Test(groups = "regression", enabled = false)
    @Story("Агро/Микро — сокращённый маршрут: минимальный состав участников и залогов")
    @Description("Сквозной прогон продукта Агро сегмента «Микро» на сокращённом составе: " +
            "заёмщик и один поручитель, один залог «Будущий урожай». Маршрут полностью " +
            "совпадает с основным сценарием, различается только состав тест-данных.")
    @Severity(SeverityLevel.CRITICAL)
    public void agroMicroRegressionRoute() {

        MicroSegmentRoute route = route(AgroScenarioFactory.agroMicroRegression());

        route.creditExpertUpToReview();
        route.underwriterApprove();
        route.clientNotification();
        route.middleControllerDocuments();
        route.ikokGoSigning();
        route.cashierOperations();
        route.finishApplication();
    }

    // ══════════════════════════════════════
    // DEBUG / RESUME (временные входы)
    // ══════════════════════════════════════

    /**
     * DEBUG: вложение документов мидл-контролером по номеру заявки.
     * Экономит полный прогон регистрации на каждой итерации.
     */
    @Test(groups = "resume", enabled = false)
    @Story("Агро/Микро — DEBUG: вложение документов мидл-контролером")
    @Description("Логин мидл-контролера (рабочее место ИКОК), открытие существующей заявки " +
            "по номеру и вложение единственного документа заёмщика.")
    @Severity(SeverityLevel.NORMAL)
    public void agroMicroResumeMiddleControllerDocuments() {
        route(AgroScenarioFactory.agroMicroHappyPath())
                .resumeAt(applicationNumber())
                .middleControllerDocuments();
    }

    /**
     * DEBUG: стадия «Подписание» по номеру заявки, доведённой до этой точки.
     * Номер: -Dapp=NNNNN или правка RESUME_APPLICATION_NUMBER.
     */
    @Test(groups = "resume", enabled = false)
    @Story("Агро/Микро — DEBUG: стадия «Подписание» по номеру заявки")
    @Description("Логин ИКОК-ГО, открытие заявки по номеру, стадия «Подписание»: решение КК → " +
            "кредитный договор → депозитный счёт → график → привязка счёта → договоры " +
            "обеспечения → закрытие активности → подписание → выдача наличными → оферта.")
    @Severity(SeverityLevel.CRITICAL)
    public void agroMicroResumeSigning() {
        route(AgroScenarioFactory.agroMicroHappyPath())
                .resumeAt(applicationNumber())
                .ikokGoSigning();
    }

    /**
     * DEBUG: кассовые операции по номеру кредитного договора.
     */
    @Test(groups = "resume", enabled = false)
    @Story("Агро/Микро — DEBUG: кассовые операции")
    @Description("Логин кассира Микро, открытие кредитного договора по номеру, подтверждение " +
            "приходного и расходного кассовых ордеров с печатью форм.")
    @Severity(SeverityLevel.CRITICAL)
    public void agroMicroResumeCashier() {
        route(AgroScenarioFactory.agroMicroHappyPath())
                .resumeWithContract(contractNumber())
                .cashierOperations();
    }

    /**
     * DEBUG: три финальных шага завершения заявки по номеру кредитного договора.
     */
    @Test(groups = "resume", enabled = false)
    @Story("Агро/Микро — DEBUG: завершение заявки")
    @Description("Прикрепление расходного кассового ордера и закрытие активности, " +
            "подтверждение получения документа под ИКОК-ГО, завершение заявки " +
            "через консультационную панель.")
    @Severity(SeverityLevel.CRITICAL)
    public void agroMicroResumeFinish() {
        route(AgroScenarioFactory.agroMicroHappyPath())
                .resumeWithContract(contractNumber())
                .finishApplication();
    }

    /**
     * DEBUG: только регистрация — стоп сразу после стадии «Оформление».
     * <p>
     * Демонстрация собираемости маршрута: сегмент собирается из сессии под
     * ролью и двух стадий, новых методов в {@link MicroSegmentRoute} не требуется.
     */
    @Test(groups = "resume", enabled = false)
    @Story("Агро/Микро — DEBUG: только создание заявки и регистрация")
    @Description("Логин кредитного эксперта, поиск клиента, консультация, подбор продукта, " +
            "создание заявки и стадия «Оформление». Дальше маршрут не идёт.")
    @Severity(SeverityLevel.NORMAL)
    public void agroMicroRegistrationOnly() {
        TestScenario scenario = AgroScenarioFactory.agroMicroHappyPath();
        MicroSegmentRoute route = route(scenario);

        route.as(scenario.getCreditExpert(), Workspace.CREDIT_EXPERT, () -> {
            route.createApplication();
            route.registration();
        });
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

    /**
     * Маршрут создаётся локально в каждом тест-методе — полей с состоянием
     * в тест-классе нет, параллельный запуск не ломается.
     */
    private MicroSegmentRoute route(TestScenario scenario) {
        return new MicroSegmentRoute(scenario);
    }

    private String applicationNumber() {
        return System.getProperty("app", RESUME_APPLICATION_NUMBER);
    }

    private String contractNumber() {
        return System.getProperty("contract", RESUME_CONTRACT_NUMBER);
    }

}