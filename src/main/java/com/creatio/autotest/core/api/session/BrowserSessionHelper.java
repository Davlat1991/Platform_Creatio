package com.creatio.autotest.core.api.session;

import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.Cookie;

import java.util.stream.Collectors;

/**
 * Извлекает данные авторизованной сессии браузера для переиспользования в API-запросах.
 * Selenide открывает сессию через UI — RestAssured переиспользует её cookie.
 */
public class BrowserSessionHelper {

    public static String getCookieString() {
        return WebDriverRunner.getWebDriver()
                .manage()
                .getCookies()
                .stream()
                .map(cookie -> cookie.getName() + "=" + cookie.getValue())
                .collect(Collectors.joining("; "));
    }

    public static String getBpmcsrf() {
        Cookie cookie = WebDriverRunner.getWebDriver()
                .manage()
                .getCookieNamed("BPMCSRF");

        if (cookie == null) {
            throw new RuntimeException("BPMCSRF cookie не найден");
        }

        return cookie.getValue();
    }
}