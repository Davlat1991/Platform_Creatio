package com.creatio.autotest.pages;

import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BasePage;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class LoginPage extends BasePage {

    // ══════════════════════════════════════
    // LOCATORS
    // ══════════════════════════════════════

    private SelenideElement usernameInput() {
        return $("#loginEdit-el");
    }

    private SelenideElement passwordInput() {
        return $("#passwordEdit-el");
    }

    private SelenideElement loginButton() {
        return $("[data-item-marker='btnLogin']");
    }

    private SelenideElement header() {
        return $("#left-header-container");
    }

    private SelenideElement loginError() {
        return $(".base-edit-validation");
    }

    // ══════════════════════════════════════
    // ACTIONS
    // ══════════════════════════════════════

    @Step("Open login page")
    public LoginPage openLoginPage() {
        log.info("Opening login page: {}", config.getBaseUrl());
        open(config.getBaseUrl());
        usernameInput().shouldBe(visible, Duration.ofSeconds(15));
        return this;
    }

    @Step("Enter username: {username}")
    public LoginPage enterUsername(String username) {
        log.info("Entering username: {}", username);
        usernameInput().shouldBe(visible).setValue(username);
        return this;
    }

    @Step("Enter password")
    public LoginPage enterPassword(String password) {
        log.info("Entering password");
        passwordInput().shouldBe(visible).setValue(password);
        return this;
    }

    @Step("Click login button")
    public LoginPage clickLogin() {
        log.info("Clicking login button");
        loginButton().shouldBe(visible).click();
        return this;
    }

    // ══════════════════════════════════════
    // VALIDATION
    // ══════════════════════════════════════

    @Step("Verify login success")
    public LoginPage verifyLoginSuccess() {
        log.info("Verifying login success");
        header().shouldBe(visible, Duration.ofSeconds(30));
        log.info("Login successful — dashboard loaded");
        return this;
    }

    @Step("Check error visible")
    public boolean isErrorVisible() {
        return loginError().isDisplayed();
    }

    @Step("Get error text")
    public String getErrorText() {
        return loginError().getText();
    }
}