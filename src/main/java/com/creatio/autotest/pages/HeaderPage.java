package com.creatio.autotest.pages;

import com.codeborne.selenide.Condition;
import com.creatio.autotest.core.base.BasePage;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;

public class HeaderPage extends BasePage {

    // ══════════════════════════════════════
    // LOGOUT
    // ══════════════════════════════════════

    @Step("Выйти из системы")
    public void logout() {
        log.info("Logging out...");

        $x("//*[@data-item-marker='userProfileButton']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .click();

        $x("//*[@data-item-marker='Выход']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .click();

        // Ждём страницу логина
        $x("//*[@id='loginContainer']")
                .shouldBe(Condition.visible, Duration.ofSeconds(15));

        log.info("Logout successful");
    }
}