package com.creatio.autotest.utils;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.ex.ElementNotFound;
import com.codeborne.selenide.ex.ElementShould;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$x;

public final class FieldUtils {

    private static final Logger log =
            LoggerFactory.getLogger(FieldUtils.class);

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private FieldUtils() {}

    // ══════════════════════════════════════
    // GET VALUE BY MARKER
    // ══════════════════════════════════════

    public static String getValueByMarker(String marker) {
        log.info("Get value by marker: '{}'", marker);

        SelenideElement input = $x(
                "//*[@data-item-marker='" + marker + "']//input"
        ).shouldBe(exist, TIMEOUT).shouldBe(visible, TIMEOUT);

        String value = input.getValue();

        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException(
                    "Field marker='" + marker + "' found but value is empty"
            );
        }
        return value;
    }

    // ══════════════════════════════════════
    // GET DETAIL INPUT VALUE (scoped, readonly input)
    // ══════════════════════════════════════

    /**
     * Читает value у readonly-input поля внутри детали со скоупом к контейнеру.
     * Маркер дублируется (label + control + input) — целимся строго в input.
     * Служит якорем "скоринг завершён": ждёт непустого value
     * (до скоринга поле пустое, placeholder="Не заполнено").
     */
    public static String getDetailInputValueScoped(String scopeId, String marker) {
        log.info("Get detail input value scoped: scope='{}', marker='{}'",
                scopeId, marker);

        SelenideElement input = $x(
                "//*[@id='" + scopeId + "']" +
                        "//*[@data-item-marker='" + marker + "']//input"
        ).shouldBe(exist, TIMEOUT)
                .shouldHave(Condition.attributeMatching("value", ".+"), TIMEOUT);

        String value = input.getValue();
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException(
                    "Detail input marker='" + marker +
                            "' is empty in scope '" + scopeId + "'"
            );
        }
        return value.trim();
    }

    // ══════════════════════════════════════
    // GET VALUE BY LABEL
    // ══════════════════════════════════════

    public static String getValueByLabel(String label) {
        log.info("Get value by label: '{}'", label);

        // По label → input
        try {
            SelenideElement input = $x(
                    "//label[normalize-space()='" + label + "']" +
                            "/following::input[1]"
            ).shouldBe(exist, TIMEOUT);

            String value = input.getValue();
            if (value != null && !value.trim().isEmpty()) return value;
        } catch (ElementNotFound | ElementShould e) {
            log.debug("Value by label '{}': input branch miss ({})",
                    label, e.getMessage());
        }

        // По label → span/div
        try {
            SelenideElement el = $x(
                    "//label[normalize-space()='" + label + "']" +
                            "/following::*[self::span or self::div][1]"
            ).shouldBe(exist, TIMEOUT);

            String text = el.getText();
            if (text != null && !text.isBlank()) return text.trim();
        } catch (ElementNotFound | ElementShould e) {
            log.debug("Value by label '{}': span/div branch miss ({})",
                    label, e.getMessage());
        }

        throw new RuntimeException(
                "Cannot get value for label='" + label + "'"
        );
    }
}