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
 * Маршрут «Только авторизация».
 * Демонстрирует композиционный блок authenticate() — сборка сценария из одного
 * кубика: логин под ролью + выбор рабочего места + выход.
 */
@Epic(AllureTags.Epics.PLATFORM)
@Feature(AllureTags.Features.AUTH)
@Owner("Davlat")
public class AuthRouteTest extends BaseTest {

    @Test(groups = "smoke", enabled = true)
    @Story("Только авторизация — Розничный менеджер")
    @Description("Сборка блока authenticate(): вход под розничным менеджером, "
            + "выбор рабочего места «Розничный менеджер» и выход")
    @Severity(SeverityLevel.BLOCKER)
    public void authOnlyManager() {
        TestScenario scenario = ScenarioFactory.authOnly();
        new RetailRoute(scenario).authenticate(scenario.getManager(), Workspace.RETAIL_MANAGER);
    }
}