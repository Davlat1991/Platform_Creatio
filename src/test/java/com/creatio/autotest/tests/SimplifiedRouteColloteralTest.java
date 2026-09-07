package com.creatio.autotest.tests;

import com.creatio.autotest.base.BaseTest;
import com.creatio.autotest.routes.RetailRoute;
import com.creatio.autotest.testdata.ScenarioFactory;
import com.creatio.autotest.utils.AllureTags;
import io.qameta.allure.*;
import org.testng.annotations.Test;

public class SimplifiedRouteColloteralTest {

    @Epic(AllureTags.Epics.RETAIL)
    @Feature(AllureTags.Features.E2E)
    @Owner("Davlat")
    public class MyRouteTest extends BaseTest {

        @Test(groups = "e2e", enabled = false)
        @Story("Свой маршрут — краткое имя")
        @Description("Что проверяет этот прогон")
        @Severity(SeverityLevel.CRITICAL)
        public void myRoute() {
            new RetailRoute(ScenarioFactory.happyPath()).runStandard();
        }
    }

}
