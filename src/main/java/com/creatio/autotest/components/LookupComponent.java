package com.creatio.autotest.components;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BaseComponent;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class LookupComponent extends BaseComponent {

    // ══════════════════════════════════════
    // CORE — основной метод выбора
    // ══════════════════════════════════════

    @Step("Lookup: выбрать '{value}' в поле '{label}'")
    public LookupComponent select(String label, String value) {

        log.info("Lookup select: label='{}' value='{}'", label, value);

        SelenideElement input = getInputByLabel(label);

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                // 1. Фокус + очистка + ввод
                executeJavaScript("arguments[0].focus();", input);
                executeJavaScript("arguments[0].click();", input);
                input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
                input.sendKeys(Keys.BACK_SPACE);
                input.sendKeys(value);

                // 2. Ждём видимый listview
                SelenideElement listView = getVisibleListView();

                // 3. Ищем пункт
                SelenideElement item = listView
                        .$x(".//li[@data-item-marker='" + value +
                                "' or normalize-space(.)='" + value + "']")
                        .shouldBe(visible, Duration.ofSeconds(15));

                // 4. JS клик — стабильнее для Creatio
                executeJavaScript("arguments[0].click();", item);

                // 5. Ждём закрытия списка
                listView.should(disappear, Duration.ofSeconds(5));

                // 6. Проверяем значение
                input.shouldHave(exactValue(value), Duration.ofSeconds(5));

                log.info("Lookup: '{}' выбрано успешно", value);
                return this;

            } catch (Throwable t) {
                log.warn("Lookup попытка {}/3 неудачна: {}", attempt, t.getMessage());
                if (attempt == 3) {
                    throw new AssertionError(
                            "Не удалось выбрать '" + value +
                                    "' в поле '" + label + "'", t);
                }
            }
        }
        return this;
    }


    // ══════════════════════════════════════
    // BY MARKER — поиск по data-item-marker
    // ══════════════════════════════════════

    @Step("Lookup: выбрать '{value}' по marker '{marker}'")
    public LookupComponent selectByMarker(String marker, String value) {

        log.info("Lookup selectByMarker: marker='{}' value='{}'", marker, value);

        SelenideElement input = $x("//*[@data-item-marker='" + marker + "']//input")
                .shouldBe(visible, enabled);

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                executeJavaScript("arguments[0].focus();", input);
                executeJavaScript("arguments[0].click();", input);

                SelenideElement listView = getVisibleListView();

                SelenideElement option = listView
                        .$x(".//li[normalize-space(.)='" + value + "'" +
                                " or @data-item-marker='" + value + "']")
                        .shouldBe(visible, Duration.ofSeconds(5));

                executeJavaScript("arguments[0].click();", option);

                listView.should(disappear, Duration.ofSeconds(5));

                input.shouldHave(Condition.value(value), Duration.ofSeconds(3));

                log.info("Lookup by marker: '{}' выбрано", value);
                return this;

            } catch (Throwable t) {
                log.warn("Попытка {}/3: {}", attempt, t.getMessage());
                if (attempt == 3) {
                    throw new AssertionError(
                            "Не удалось выбрать '" + value +
                                    "' по marker '" + marker + "'", t);
                }
            }
        }
        return this;
    }

    // ══════════════════════════════════════
    // ADDRESS — с TAB для фиксации значения
    // ══════════════════════════════════════

    @Step("Lookup: выбрать адрес '{value}' в поле '{label}'")
    public LookupComponent selectAddress(String label, String value) {

        log.info("Lookup selectAddress: label='{}' value='{}'", label, value);

        SelenideElement input = getInputByLabel(label);

        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.BACK_SPACE);
        input.sendKeys(value);

        SelenideElement listView = getVisibleListView();
        SelenideElement item = listView
                .$x(".//li[normalize-space(.)='" + value + "']")
                .shouldBe(visible);

        item.click();

        // TAB — обязателен для AddressSchema в Creatio
        input.sendKeys(Keys.TAB);
        input.shouldHave(exactValue(value));

        return this;
    }

    // ══════════════════════════════════════
    // SEARCH ICON — лупа для сложных lookup
    // ══════════════════════════════════════

    @Step("Lookup: открыть поиск по иконке '{lookupId}'")
    public LookupComponent clickSearchIcon(String lookupId) {

        log.info("Lookup: клик по иконке поиска '{}'", lookupId);

        SelenideElement wrap = $x("//div[@id='" + lookupId + "-wrap']")
                .shouldBe(visible);
        wrap.hover();

        SelenideElement icon = $x("//div[@id='" + lookupId + "-right-icon']")
                .shouldBe(visible, enabled);

        executeJavaScript("arguments[0].click();", icon);

        return this;
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

    private SelenideElement getInputByLabel(String label) {
        return $x("//label[normalize-space(.)='" + label + "']/../..//input")
                .shouldBe(visible, enabled);
    }

    private SelenideElement getVisibleListView() {
        return $$x("//div[contains(@class,'listview')]")
                .findBy(visible)
                .shouldBe(visible, Duration.ofSeconds(10));
    }

    @Step("Set field by label: {label} = {value}")
    public LookupComponent selectByLabel(String label, String value) {
        log.info("selectByLabel: label='{}' value='{}'", label, value);
        $x("//label[normalize-space()='" + label + "']/../..//input")
                .shouldBe(visible, enabled)
                .setValue(value);
        return this;
    }

    @Step("Set modal search field: {marker} = {value}")
    public LookupComponent setModalSearchField(String marker, String value) {
        log.info("setModalSearchField: marker='{}' value='{}'", marker, value);
        $x("//div[@data-item-marker='" + marker + "']//input")
                .shouldBe(visible)
                .setValue(value);
        return this;
    }
}