package com.creatio.autotest.tests;

import com.creatio.autotest.base.BaseTest;
import com.creatio.autotest.flows.AuthFlow;
import com.creatio.autotest.testdata.Users;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

@Feature("Authentication")
public class LoginTest extends BaseTest {

    // ══════════════════════════════════════
    // TESTS
    // ══════════════════════════════════════

    @Test(groups = "smoke")
    @Story("Manager login")
    @Description("Verify retail manager can login successfully")
    @Severity(SeverityLevel.BLOCKER)
    public void managerLoginTest() {
        new AuthFlow().login(Users.MANAGER());
    }

    @Test(groups = "smoke")
    @Story("Underwriter login")
    @Description("Verify underwriter can login successfully")
    @Severity(SeverityLevel.BLOCKER)
    public void underwriterLoginTest() {
        new AuthFlow().login(Users.UNDERWRITER());
    }

    @Test(groups = "smoke")
    @Story("IKOK login")
    @Description("Verify IKOK can login successfully")
    @Severity(SeverityLevel.BLOCKER)
    public void ikokLoginTest() {
        new AuthFlow().login(Users.IKOK());
    }

    @Test(groups = "smoke")
    @Story("IKOK-GO login")
    @Description("Verify IKOK-GO can login successfully")
    @Severity(SeverityLevel.BLOCKER)
    public void ikokGoLoginTest() {
        new AuthFlow().login(Users.IKOK_GO());
    }

    @Test(groups = "smoke")
    @Story("Cashier login")
    @Description("Verify cashier can login successfully")
    @Severity(SeverityLevel.BLOCKER)
    public void cashierLoginTest() {
        new AuthFlow().login(Users.CASHIER());
    }
}