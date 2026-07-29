package com.creatio.autotest.tests;

import com.creatio.autotest.base.BaseTest;
import com.creatio.autotest.routes.RetailRoute;
import com.creatio.autotest.testdata.ScenarioFactory;
import com.creatio.autotest.testdata.models.TestScenario;
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

    @Test(groups = "regression")
    @Story("Стандартный маршрут — полный путь с верификатором и комитетом")
    @Description("Стандартный маршрут — полный happy path с верификатором и комитетом (КК4)")
    @Severity(SeverityLevel.BLOCKER)
    public void standardRouteHappyPath() {
        TestScenario scenario = ScenarioFactory.happyPath();
        new RetailRoute(scenario).runStandard();
    }

    /**
     * Ручной вход в середину маршрута: засеваем контекст номером уже созданной
     * заявки (или договора) и запускаем нужный сегмент, минуя предыдущие.
     * Отключён — включать точечно при отладке конкретной стадии.
     */
    @Test(groups = "debug", enabled = false)
    @Story("Debug — вход в середину маршрута по номеру заявки/договора")
    @Description("Засев контекста и запуск сегмента без прохождения всего маршрута")
    @Severity(SeverityLevel.NORMAL)
    public void debugResumeMidRoute() {
        TestScenario scenario = ScenarioFactory.happyPath();
        RetailRoute route = new RetailRoute(scenario);

        // Заявка уже создана — старт с верификатора:
        route.resumeAt("391996").underwriterApprove();

        // Договор уже создан — вместо строки выше:
        // route.resumeWithContract("02-005-033413").cashierOperations();
    }
}