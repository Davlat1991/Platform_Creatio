package com.creatio.autotest.flows.common;

import com.creatio.autotest.pages.HeaderPage;
import com.creatio.autotest.pages.LoginPage;
import com.creatio.autotest.testdata.models.UserCredentials;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthFlow {

    private static final Logger log = LoggerFactory.getLogger(AuthFlow.class);

    private final LoginPage  loginPage  = new LoginPage();
    private final HeaderPage headerPage = new HeaderPage();

    // ══════════════════════════════════════
    // LOGIN / LOGOUT
    // ══════════════════════════════════════

    @Step("Войти как: {user}")
    public void login(UserCredentials user) {
        log.info("Login as: {}", user);

        loginPage
                .openLoginPage()
                .enterUsername(user.getLogin())
                .enterPassword(user.getPassword())
                .clickLogin()
                .verifyLoginSuccess();
    }

    @Step("Выйти из системы")
    public void logout() {
        log.info("Logout");
        headerPage.logout();
    }

    @Step("Перелогиниться как: {user}")
    public void relogin(UserCredentials user) {
        logout();
        login(user);
    }
}