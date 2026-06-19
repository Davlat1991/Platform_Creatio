package com.creatio.autotest.tests;

import com.creatio.autotest.base.BaseTest;
import com.creatio.autotest.flows.AuthFlow;
import com.creatio.autotest.flows.WorkspaceFlow;
import com.creatio.autotest.testdata.Users;
import com.creatio.autotest.testdata.models.Workspace;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

@Feature("Authentication")
public class LoginTest extends BaseTest {

    @Test(groups = "smoke")
    @Story("Manager — login + workspace")
    @Description("Менеджер входит, выбирает рабочее место 'Розничный менеджер', выходит")
    @Severity(SeverityLevel.BLOCKER)
    public void managerLoginTest() {
        AuthFlow auth = new AuthFlow();
        auth.login(Users.MANAGER());
        new WorkspaceFlow().select(Workspace.RETAIL_MANAGER);
        auth.logout();
    }

    @Test(groups = "smoke")
    @Story("Underwriter — login + workspace")
    @Description("Верификатор входит, выбирает рабочее место 'Верификатор', выходит")
    @Severity(SeverityLevel.BLOCKER)
    public void underwriterLoginTest() {
        AuthFlow auth = new AuthFlow();
        auth.login(Users.UNDERWRITER());
        new WorkspaceFlow().select(Workspace.UNDERWRITER);
        auth.logout();
    }

    @Test(groups = "smoke")
    @Story("IKOK — login + workspace")
    @Description("ИКОК входит, выбирает рабочее место 'ИКОК', выходит")
    @Severity(SeverityLevel.BLOCKER)
    public void ikokLoginTest() {
        AuthFlow auth = new AuthFlow();
        auth.login(Users.IKOK());
        new WorkspaceFlow().select(Workspace.IKOK);
        auth.logout();
    }

    @Test(groups = "smoke")
    @Story("IKOK-GO — login + workspace")
    @Description("ИКОК-ГО входит, выбирает рабочее место 'ИКОК-ГО', выходит")
    @Severity(SeverityLevel.BLOCKER)
    public void ikokGoLoginTest() {
        AuthFlow auth = new AuthFlow();
        auth.login(Users.IKOK_GO());
        new WorkspaceFlow().select(Workspace.IKOK_GO);
        auth.logout();
    }

    @Test(groups = "smoke")
    @Story("Cashier — login + workspace")
    @Description("Кассир входит, выбирает рабочее место 'Касса', выходит")
    @Severity(SeverityLevel.BLOCKER)
    public void cashierLoginTest() {
        AuthFlow auth = new AuthFlow();
        auth.login(Users.CASHIER());
        new WorkspaceFlow().select(Workspace.CASHIER);
        auth.logout();
    }
}