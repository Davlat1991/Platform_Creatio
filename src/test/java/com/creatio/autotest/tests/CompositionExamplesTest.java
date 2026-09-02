package com.creatio.autotest.tests;

import com.creatio.autotest.base.BaseTest;
import com.creatio.autotest.routes.RetailRoute;
import com.creatio.autotest.testdata.ScenarioFactory;
import com.creatio.autotest.testdata.models.TestScenario;
import com.creatio.autotest.testdata.models.Workspace;
import com.creatio.autotest.utils.AllureTags;
import io.qameta.allure.*;
import org.testng.annotations.Test;

/**
 * Каталог рецептов сборки сценариев из блоков (LEGO).
 * <p>
 * ВСЕ методы disabled — это живые примеры «что куда класть и как жонглировать»,
 * не CI-тесты. Модель сборки:
 * <ol>
 *   <li>данные — TestScenario (ScenarioFactory.*): users, contact, product,
 *       participants, collaterals;</li>
 *   <li>сессия под ролью — route.as(user, ws, тело): login → рабочее место →
 *       тело → logout;</li>
 *   <li>блоки/сегменты — createApplication(), registration(), review(), …,
 *       либо готовые сегменты (managerUpToReview() и т.д.).</li>
 * </ol>
 * Полная карта блоков: claude/retail-navigation-map.md.
 * <p>
 * ВНИМАНИЕ: свободное «жонглирование» участниками/документами в любом маршруте
 * (упрощённый с залогодателем, стандарт без участников) откроется после Шага 7
 * (документы от сценария) и Шага 14 (scenario-driven упрощённого). Ниже — только
 * то, что собирается уже сейчас.
 */
@Epic(AllureTags.Epics.RETAIL)
@Feature(AllureTags.Features.E2E)
@Owner("Davlat")
public class CompositionExamplesTest extends BaseTest {

    /** Рецепт: только авторизация (один кубик). */
    @Test(groups = "examples", enabled = false)
    @Story("Рецепт — только авторизация")
    @Description("Собрать сценарий из одного блока authenticate()")
    @Severity(SeverityLevel.NORMAL)
    public void recipe_authOnly() {
        TestScenario scenario = ScenarioFactory.authOnly();
        new RetailRoute(scenario)
                .authenticate(scenario.getManager(), Workspace.RETAIL_MANAGER);
    }

    /** Рецепт: авторизация + создание заявки, стоп. Блок под сессией через as(). */
    @Test(groups = "examples", enabled = false)
    @Story("Рецепт — авторизация + создание заявки")
    @Description("Логин розничного менеджера и создание заявки; дальше не идём. "
            + "Создаёт реальную заявку в системе — гонять точечно")
    @Severity(SeverityLevel.NORMAL)
    public void recipe_authPlusCreateApplication() {
        TestScenario scenario = ScenarioFactory.happyPath();
        RetailRoute route = new RetailRoute(scenario);
        route.as(scenario.getManager(), Workspace.RETAIL_MANAGER,
                route::createApplication);
    }

    /** Рецепт: полный стандартный маршрут — один вызов. */
    @Test(groups = "examples", enabled = false)
    @Story("Рецепт — полный стандартный маршрут")
    @Description("Стандартный E2E: happyPath → runStandard()")
    @Severity(SeverityLevel.NORMAL)
    public void recipe_standardFull() {
        new RetailRoute(ScenarioFactory.happyPath()).runStandard();
    }

    /** Рецепт: полный упрощённый маршрут — один вызов. */
    @Test(groups = "examples", enabled = false)
    @Story("Рецепт — полный упрощённый маршрут")
    @Description("Упрощённый E2E: simplePath → runSimplified()")
    @Severity(SeverityLevel.NORMAL)
    public void recipe_simplifiedFull() {
        new RetailRoute(ScenarioFactory.simplePath()).runSimplified();
    }

    /**
     * Рецепт: продолжить с середины по номеру заявки — засев контекста +
     * запуск нужного сегмента без прохождения предыдущих.
     */
    @Test(groups = "examples", enabled = false)
    @Story("Рецепт — вход в середину маршрута (resumeAt)")
    @Description("resumeAt(номер) засевает заявку в контекст; дальше — любой сегмент")
    @Severity(SeverityLevel.NORMAL)
    public void recipe_resumeMidRoute() {
        TestScenario scenario = ScenarioFactory.happyPath();
        RetailRoute route = new RetailRoute(scenario);
        route.resumeAt("XXXXXX")      // ← номер существующей заявки
                .underwriterApprove();   // ← нужный сегмент
    }
}