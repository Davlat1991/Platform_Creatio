package com.creatio.autotest.tests;

import com.creatio.autotest.base.BaseTest;
import com.creatio.autotest.flows.retail.SigningStageFlow;
import com.creatio.autotest.routes.RetailRoute;
import com.creatio.autotest.testdata.ScenarioFactory;
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
 * Стандартный маршрут (30001–100000 сомони).
 * Тип клиента: «Имеет другой источник дохода» (OTHER_INCOME, дефолт фабрики).
 * Поручитель + залогодатель + залоги.
 * Скоринг даёт любое из решений; заявка ВСЕГДА идёт через верификатора
 * и кредитный комитет (КК4).
 * <p>
 * Маршрут собран из ролевых сегментов в {@link RetailRoute} — тело теста
 * содержит только выбор сценария и запуск сборки.
 */
@Epic(AllureTags.Epics.RETAIL)
@Feature(AllureTags.Features.E2E)
@Owner("Davlat")
public class StandardRouteTest extends BaseTest {

    @Test(groups = "regression", enabled = true)
    @Story("Стандартный маршрут — полный путь с верификатором и комитетом")
    @Description("Стандартный маршрут — полный happy path с верификатором и комитетом (КК4)")
    @Severity(SeverityLevel.BLOCKER)
    public void standardRouteHappyPath() {
        TestScenario scenario = ScenarioFactory.happyPath();
        new RetailRoute(scenario).runStandard();
    }

    /**
     * Стандартный маршрут на всех 10 типах залога в одном прогоне — широта
     * договоров обеспечения (Задача 2). Сценарий allCollaterals: все типы
     * обеспечения + поручитель и 2 залогодателя. Цель прогона — регистрация
     * договоров обеспечения и печатные формы через CollateralContractsFlow.
     */
    @Test(groups = "regression", enabled = false)
    @Story("Стандартный маршрут — все 10 типов залога в одном прогоне")
    @Description("Стандартный маршрут на сценарии allCollaterals: 10 типов обеспечения "
            + "+ поручитель и 2 залогодателя; проверка регистрации договоров "
            + "обеспечения и печатных форм через CollateralContractsFlow (КК4)")
    @Severity(SeverityLevel.BLOCKER)
    public void standardRouteAllCollaterals() {
        TestScenario scenario = ScenarioFactory.allCollaterals();
        new RetailRoute(scenario).runStandard();
    }

    /**
     * DEBUG: дорегистрация договоров обеспечения на УЖЕ существующей заявке.
     * Открывает кредитный договор (номер — в resumeAt) под ИКОК-ГО и
     * дорегистрирует оставшиеся временные договоры, не проходя маршрут заново.
     * Предусловие: подписание не завершено (активность «Создание договора в
     * АБС …» ещё открыта). Отключён — включать точечно.
     */
    @Test(groups = "debug", enabled = false)
    @Story("Debug — дорегистрация договоров обеспечения на существующей заявке")
    @Description("Открыть кредитный договор существующей заявки и дорегистрировать "
            + "оставшиеся договоры обеспечения без прохождения маршрута заново")
    @Severity(SeverityLevel.NORMAL)
    public void debugResumeCollateralContracts() {
        TestScenario scenario = ScenarioFactory.allCollaterals();
        RetailRoute route = new RetailRoute(scenario);

        // Номер существующей заявки из прошлого прогона:
        route.resumeAt("392056");

        // Сессия ИКОК-ГО → открыть договор → дорегистрировать обеспечение:
        route.as(scenario.getIkokGo(), Workspace.IKOK_GO,
                () -> new SigningStageFlow().debugResumeCollateralContracts());
    }
}