package com.creatio.autotest.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BasePage;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class FieldPage extends BasePage {

    // ══════════════════════════════════════
    // SET FIELD
    // ══════════════════════════════════════

    @Step("Ввести '{value}' в поле '{label}'")
    public FieldPage setField(String label, String value) {
        log.info("setField: label='{}' value='{}'", label, value);

        SelenideElement field = findByLabel(label)
                .shouldBe(visible)
                .scrollIntoView(true);

        try {
            field.clear();
            field.setValue(value);
        } catch (Throwable t) {
            executeJavaScript(
                    "arguments[0].value = arguments[1];" +
                            "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));" +
                            "arguments[0].dispatchEvent(new Event('change',{bubbles:true}));",
                    field, value
            );
        }
        return this;
    }

    @Step("Ввести '{value}' в поле по placeholder '{placeholder}'")
    public FieldPage setFieldByPlaceholder(String placeholder, String value) {
        log.info("setFieldByPlaceholder: '{}' = '{}'", placeholder, value);
        $x("//input[@placeholder='" + placeholder + "']")
                .shouldBe(visible)
                .setValue(value);
        return this;
    }

    // ══════════════════════════════════════
    // GET FIELD VALUE
    // ══════════════════════════════════════

    @Step("Получить значение поля '{label}'")
    public String getFieldValue(String label) {
        return findByLabel(label)
                .shouldBe(visible)
                .getValue();
    }

    // ══════════════════════════════════════
    // VALIDATE
    // ══════════════════════════════════════

    @Step("Проверить значение поля '{label}' = '{expected}'")
    public FieldPage shouldHaveValue(String label, String expected) {
        findByLabel(label).shouldHave(exactValue(expected));
        return this;
    }

    // ══════════════════════════════════════
    // FIND HELPERS
    // ══════════════════════════════════════

    public SelenideElement findByLabel(String label) {

        // 1️⃣ По label → input
        String byLabel =
                "//label[normalize-space()='" + label + "']" +
                        "/following::*[self::input or self::textarea][1]";
        if ($x(byLabel).exists()) return $x(byLabel);

        // 2️⃣ По label → container → input
        String byLabelContainer =
                "//label[normalize-space()='" + label + "']/../..//input";
        if ($x(byLabelContainer).exists()) return $x(byLabelContainer);

        // 3️⃣ По placeholder
        String byPlaceholder =
                "//input[@placeholder='" + label + "'] | " +
                        "//textarea[@placeholder='" + label + "']";
        if ($x(byPlaceholder).exists()) return $x(byPlaceholder);

        throw new AssertionError("Field not found: '" + label + "'");
    }
}