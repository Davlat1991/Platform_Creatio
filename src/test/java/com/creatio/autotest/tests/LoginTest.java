package com.creatio.autotest.tests;

import com.creatio.autotest.base.BaseTest;
import com.creatio.autotest.flows.AuthFlow;
import com.creatio.autotest.testdata.Users;
import com.creatio.autotest.utils.AllureTags;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

@Epic(AllureTags.Epics.PLATFORM)
@Feature(AllureTags.Features.AUTH)
@Owner("Davlat")
public class LoginTest extends BaseTest {

    // ══════════════════════════════════════
    // TESTS
    // ══════════════════════════════════════

    @Test(groups = "smoke")
    @Story("Вход: Розничный менеджер")
    @Description("Розничный менеджер успешно входит в систему")
    @Severity(SeverityLevel.BLOCKER)
    public void managerLoginTest() {
        new AuthFlow().login(Users.MANAGER());
    }

    @Test(groups = "smoke")
    @Story("Вход: Верификатор")
    @Description("Верификатор успешно входит в систему")
    @Severity(SeverityLevel.BLOCKER)
    public void underwriterLoginTest() {
        new AuthFlow().login(Users.UNDERWRITER());
    }

    @Test(groups = "smoke")
    @Story("Вход: ИКОК")
    @Description("ИКОК успешно входит в систему")
    @Severity(SeverityLevel.BLOCKER)
    public void ikokLoginTest() {
        new AuthFlow().login(Users.IKOK());
    }

    @Test(groups = "smoke")
    @Story("Вход: ИКОК-ГО")
    @Description("ИКОК-ГО успешно входит в систему")
    @Severity(SeverityLevel.BLOCKER)
    public void ikokGoLoginTest() {
        new AuthFlow().login(Users.IKOK_GO());
    }

    @Test(groups = "smoke")
    @Story("Вход: Касса")
    @Description("Кассир успешно входит в систему")
    @Severity(SeverityLevel.BLOCKER)
    public void cashierLoginTest() {
        new AuthFlow().login(Users.CASHIER());
    }
}