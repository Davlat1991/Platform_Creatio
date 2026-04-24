package com.creatio.autotest.components;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BaseComponent;
import com.creatio.autotest.utils.AllureAttachments;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Duration;
import java.util.Locale;

import static com.codeborne.selenide.Selenide.$x;

public class FieldAssertions extends BaseComponent {

    private static final Logger log =
            LoggerFactory.getLogger(FieldAssertions.class);

    @Step("Проверить поле '{fieldName}' = '{expectedValue}'")
    public void checkFieldValueNormalized(String fieldName,
                                          String expectedValue) {
        log.info("Check field: '{}' = '{}'", fieldName, expectedValue);

        try {
            SelenideElement field = $x(
                    "//label[contains(text(),'" + fieldName + "')]" +
                            "/following::input[1]"
            ).shouldBe(Condition.visible, Duration.ofSeconds(10));

            String actualRaw = field.getValue() != null
                    ? field.getValue()
                    : field.getText();

            String actual   = normalize(actualRaw);
            String expected = normalize(expectedValue);

            BigDecimal actualNum   = tryParseNumber(actual);
            BigDecimal expectedNum = tryParseNumber(expected);

            if (actualNum != null && expectedNum != null) {
                if (actualNum.compareTo(expectedNum) != 0) {
                    fail(fieldName, expectedValue, actualRaw);
                }
            } else {
                if (!expected.equals(actual)) {
                    fail(fieldName, expectedValue, actualRaw);
                }
            }

            log.info("Field '{}' verified: {}", fieldName, expectedValue);
            Allure.step("Поле '" + fieldName + "' проверено: " + expectedValue);

        } catch (Throwable t) {
            AllureAttachments.attachScreenshot("Field check failure");
            AllureAttachments.attachPageSource();
            throw t;
        }
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

    private String normalize(String s) {
        if (s == null) return "";
        return s.replace('\u00A0', ' ')
                .replace('\u202F', ' ')
                .replace('\u2007', ' ')
                .replace('\u2009', ' ')
                .trim()
                .replaceAll("\\s+", " ");
    }

    private BigDecimal tryParseNumber(String s) {
        if (s == null || s.isEmpty()) return null;
        String cleaned = s.replaceAll("[^0-9,\\.\\-]", "");
        if (cleaned.isEmpty()) return null;
        try {
            return new BigDecimal(cleaned.replace(",", "."));
        } catch (NumberFormatException ignored) {}
        try {
            NumberFormat nf = NumberFormat.getInstance(new Locale("ru"));
            return new BigDecimal(nf.parse(s).toString());
        } catch (ParseException | NumberFormatException ignored) {}
        return null;
    }

    private void fail(String fieldName, String expected, String actual) {
        String msg = String.format(
                "Field '%s' mismatch. Expected: '%s', Actual: '%s'",
                fieldName, expected, actual
        );
        AllureAttachments.attachScreenshot("Assertion failure");
        throw new AssertionError(msg);
    }
}