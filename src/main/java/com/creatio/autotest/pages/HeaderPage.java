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

        // DEF-LOGOUT: клик по профилю иногда не раскрывает меню → «Выход» не появляется
        // за таймаут (раньше спасал только перезапуск). Ретрай (бюджет 3): меню профиля
        // открываем ТОЛЬКО если «Выход» ещё не виден (защита от toggle-схлопывания), затем клик.
        boolean clicked = false;
        for (int attempt = 1; attempt <= 3 && !clicked; attempt++) {
            if (!$x("//*[@data-item-marker='Выход']").is(Condition.visible)) {
                $x("//*[@data-item-marker='userProfileButton']")
                        .shouldBe(Condition.visible, Duration.ofSeconds(30))
                        .click();
            }
            try {
                $x("//*[@data-item-marker='Выход']")
                        .shouldBe(Condition.visible, Duration.ofSeconds(15))
                        .click();
                clicked = true;
            } catch (Exception | AssertionError notYet) {
                log.warn("DEF-LOGOUT: «Выход» не появился (попытка {}/3) — повтор раскрытия меню профиля", attempt);
            }
        }
        if (!clicked) {
            throw new AssertionError(
                    "DEF-LOGOUT: кнопка «Выход» не найдена после 3 попыток раскрыть меню профиля");
        }

        // Ждём страницу логина
        $x("//*[@id='loginContainer']")
                .shouldBe(Condition.visible, Duration.ofSeconds(60));

        log.info("Logout successful");
    }
}