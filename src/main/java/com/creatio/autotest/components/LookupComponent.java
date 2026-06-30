package com.creatio.autotest.components;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BaseComponent;
import com.creatio.autotest.testdata.models.registration.fields.EducationCareerField;
import com.creatio.autotest.testdata.models.registration.fields.EmploymentType;
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
                // На повторных попытках гасим «висящий» список перед переоткрытием.
                if (attempt > 1) {
                    try { input.sendKeys(Keys.ESCAPE); } catch (Throwable ignored) {}
                }

                // 1. Фокус + НАДЁЖНАЯ очистка + проверка пустоты + ввод
                executeJavaScript("arguments[0].focus();", input);
                executeJavaScript("arguments[0].click();", input);
                clearInput(input);
                input.shouldHave(exactValue(""), Duration.ofSeconds(5));
                input.sendKeys(value);

                // 2. Ждём видимый listview
                SelenideElement listView = getVisibleListView();

                // 3. Ищем пункт
                SelenideElement item = listView
                        .$x(".//li[@data-item-marker='" + value +
                                "' or normalize-space(.)='" + value + "']")
                        .shouldBe(visible, Duration.ofSeconds(30));

                // 4. JS клик — стабильнее для Creatio
                executeJavaScript("arguments[0].click();", item);

                // 5. Ждём закрытия списка
                listView.should(disappear, Duration.ofSeconds(30));

                // 6. Проверяем значение
                input.shouldHave(exactValue(value), Duration.ofSeconds(30));

                log.info("Lookup: '{}' выбрано успешно", value);
                return this;

            } catch (Throwable t) {
                log.warn("Lookup попытка {}/3 неудачна: {}", attempt, t.getMessage());
                try { clearInput(input); } catch (Throwable ignored) {}
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
                // На повторных попытках закрываем «висящий» список перед переоткрытием.
                // Первая попытка идентична проверенному поведению.
                if (attempt > 1) {
                    try { input.sendKeys(Keys.ESCAPE); } catch (Throwable ignored) {}
                }

                executeJavaScript("arguments[0].focus();", input);
                executeJavaScript("arguments[0].click();", input);

                // Короткое ожидание: если список не открылся за 5 сек — быстро падаем
                // и кликаем заново на след. попытке, а не ждём общий 60-сек таймаут.
                SelenideElement listView = getVisibleListView(Duration.ofSeconds(5));

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
                log.warn("selectByMarker попытка {}/3: {}", attempt, t.getMessage());
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

    // ══════════════════════════════════════
    // TEXT INPUT — простое текстовое поле (без выпадающего списка)
    // ══════════════════════════════════════

    @Step("Text: ввести '{value}' в поле '{label}'")
    public LookupComponent setText(String label, String value) {

        log.info("setText: label='{}' value='{}'", label, value);

        SelenideElement input = getInputByLabel(label);

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                executeJavaScript("arguments[0].focus();", input);
                executeJavaScript("arguments[0].click();", input);

                clearInput(input);

                input.sendKeys(value);
                input.sendKeys(Keys.TAB);

                input.shouldHave(exactValue(value), Duration.ofSeconds(10));

                log.info("setText: '{}' введено в '{}'", value, label);
                return this;

            } catch (Throwable t) {
                log.warn("setText попытка {}/3 неудачна: {}", attempt, t.getMessage());
                try { clearInput(input); } catch (Throwable ignored) {}
                if (attempt == 3) {
                    throw new AssertionError(
                            "Не удалось ввести '" + value +
                                    "' в поле '" + label + "'", t);
                }
            }
        }
        return this;
    }

    // ══════════════════════════════════════
    // CLEAR — гарантированная очистка поля
    // ══════════════════════════════════════

    private void clearInput(SelenideElement input) {
        executeJavaScript(
                "var el = arguments[0];" +
                        "var setter = Object.getOwnPropertyDescriptor(" +
                        "  window.HTMLInputElement.prototype, 'value').set;" +
                        "setter.call(el, '');" +
                        "el.dispatchEvent(new Event('input', { bubbles: true }));",
                input);

        for (int i = 0; i < 5 && !input.getValue().isEmpty(); i++) {
            input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        }
    }

    private SelenideElement getInputByLabel(String label) {
        return $x("//label[normalize-space(.)='" + label + "']/../..//input")
                .shouldBe(visible, enabled);
    }

    private SelenideElement getVisibleListView() {
        return getVisibleListView(Duration.ofSeconds(60));
    }

    private SelenideElement getVisibleListView(Duration timeout) {
        return $$x("//div[contains(@class,'listview')]")
                .findBy(visible)
                .shouldBe(visible, timeout);
    }

    @Step("Set field by label: {label} = {value}")
    public LookupComponent selectByLabel(String label, String value) {
        log.info("selectByLabel: label='{}' value='{}'", label, value);
        $x("//label[normalize-space()='" + label + "']/../..//input")
                .shouldBe(visible, enabled)
                .setValue(value);
        return this;
    }

    //Новый метод 17.06.2026

    @Step("Set modal search field: {marker} = {value}")
    public LookupComponent setModalSearchField(String marker, String value) {
        log.info("setModalSearchField: marker='{}' value='{}'", marker, value);

        SelenideElement input = $x("//div[@data-item-marker='" + marker + "']//input")
                .shouldBe(visible, enabled);

        // Устанавливаем значение через JS + триггерим события которые Creatio слушает
        executeJavaScript(
                "var el = arguments[0];" +
                        "var val = arguments[1];" +
                        "var nativeInputValueSetter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;" +
                        "nativeInputValueSetter.call(el, val);" +
                        "el.dispatchEvent(new Event('input', { bubbles: true }));" +
                        "el.dispatchEvent(new Event('change', { bubbles: true }));" +
                        "el.dispatchEvent(new Event('blur', { bubbles: true }));",
                input, value
        );

        input.shouldHave(value(value), Duration.ofSeconds(15));

        return this;
    }

    // ══════════════════════════════════════
    // GRID LOOKUP — выбор через модалку по лупе (data-item-marker LookupGrid)
    // ══════════════════════════════════════

    @Step("Grid lookup: выбрать '{value}' по marker '{marker}'")
    public LookupComponent selectInGridByMarker(String marker, String value) {

        log.info("selectInGridByMarker: marker='{}' value='{}'", marker, value);

        // 1. Фокус на ячейку (лупа появляется только при hover/фокусе)
        SelenideElement cell = $x("//*[@data-item-marker='" + marker + "']")
                .shouldBe(visible);
        cell.hover();
        SelenideElement cellInput = cell.$x(".//input");
        executeJavaScript("arguments[0].focus();", cellInput);

        // Кликаем лупу через JS (она может быть скрыта до hover — JS-клик не требует visible)
        SelenideElement icon = cell
                .$x(".//div[contains(@class,'lookup-edit-right-icon')]");
        executeJavaScript("arguments[0].click();", icon);

        // 2. Ждём grid-модалку
        SelenideElement grid = $x("//div[@data-item-marker='LookupGrid']")
                .shouldBe(visible, Duration.ofSeconds(30));

        // 3. Ищем строку по точному значению
        SelenideElement row = grid
                .$x(".//div[@data-item-marker='" + value + "']")
                .shouldBe(visible, Duration.ofSeconds(30));
        executeJavaScript("arguments[0].click();", row);

        // 4. Жмём ВЫБРАТЬ
        SelenideElement selectBtn = $x("//span[@data-tag='SelectButton']")
                .shouldBe(visible, enabled);
        executeJavaScript("arguments[0].click();", selectBtn);

        // 5. Ждём закрытия модалки
        grid.should(disappear, Duration.ofSeconds(30));

        log.info("selectInGridByMarker: '{}' выбрано", value);
        return this;
    }


    // ══════════════════════════════════════
    // GRID LOOKUP + SEARCH — модалка с поиском (длинные справочники: Страны)
    // ══════════════════════════════════════

    @Step("Grid lookup+search: выбрать '{value}' по marker '{marker}'")
    public LookupComponent selectInGridByMarkerWithSearch(String marker, String value) {

        log.info("selectInGridByMarkerWithSearch: marker='{}' value='{}'", marker, value);

        // 1. Фокус на ячейку + клик по лупе (она скрыта до hover — JS-клик)
        SelenideElement cell = $x("//*[@data-item-marker='" + marker + "']")
                .shouldBe(visible);
        cell.hover();
        SelenideElement cellInput = cell.$x(".//input");
        executeJavaScript("arguments[0].focus();", cellInput);
        SelenideElement icon = cell
                .$x(".//div[contains(@class,'lookup-edit-right-icon')]");
        executeJavaScript("arguments[0].click();", icon);

        // 2. Ждём grid-модалку
        SelenideElement grid = $x("//div[@data-item-marker='LookupGrid']")
                .shouldBe(visible, Duration.ofSeconds(30));

        // 3. Вводим значение в поле поиска и подтверждаем через Enter
        SelenideElement searchInput = $("#searchEdit-el")
                .shouldBe(visible, Duration.ofSeconds(10));
        searchInput.click();
        searchInput.sendKeys(value);
        searchInput.sendKeys(Keys.ENTER);   // ← запускает фильтрацию надёжнее, чем клик по кнопке



        // 5. Кликаем найденную строку
        SelenideElement row = grid
                .$x(".//div[@data-item-marker='" + value + "']")
                .shouldBe(visible, Duration.ofSeconds(30));
        executeJavaScript("arguments[0].click();", row);

        // 6. Жмём ВЫБРАТЬ
        SelenideElement selectBtn = $x("//span[@data-tag='SelectButton']")
                .shouldBe(visible, enabled);
        executeJavaScript("arguments[0].click();", selectBtn);

        // 7. Ждём закрытия модалки
        grid.should(disappear, Duration.ofSeconds(30));

        log.info("selectInGridByMarkerWithSearch: '{}' выбрано", value);
        return this;
    }



    // ══════════════════════════════════════
    // LOOKUP + SOFT-VALIDATION — выбор из списка с обработкой confirm-подтверждения
    // (поля Образование/Карьера/Адрес: после выбора Creatio показывает кнопку подтверждения)
    // ══════════════════════════════════════

    @Step("Lookup+confirm: выбрать '{value}' по marker '{marker}'")
    public LookupComponent selectByMarkerWithSoftValidation(String marker, String value) {

        log.info("selectByMarkerWithSoftValidation: marker='{}' value='{}'", marker, value);

        SelenideElement input = $x("//*[@data-item-marker='" + marker + "']//input")
                .shouldBe(visible, enabled);

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                executeJavaScript("arguments[0].focus();", input);
                executeJavaScript("arguments[0].click();", input);

                clearInput(input);
                input.sendKeys(value);

                // выбор пункта из выпадающего списка
                SelenideElement listView = getVisibleListView();
                SelenideElement item = listView
                        .$x(".//li[normalize-space(.)='" + value + "']")
                        .shouldBe(visible, Duration.ofSeconds(30));
                executeJavaScript("arguments[0].click();", item);

                // soft-validation: если Creatio показал кнопку подтверждения — жмём её
                SelenideElement confirm = input.closest(".base-edit")
                        .$x(".//div[contains(@id,'soft-validation-confirm')]");
                if (confirm.exists() && confirm.isDisplayed()) {
                    log.debug("soft-validation confirm найден — подтверждаем");
                    executeJavaScript("arguments[0].click();", confirm);
                }

                input.shouldHave(exactValue(value), Duration.ofSeconds(15));

                log.info("selectByMarkerWithSoftValidation: '{}' выбрано", value);
                return this;

            } catch (Throwable t) {
                log.warn("Попытка {}/3: {}", attempt, t.getMessage());
                try { clearInput(input); } catch (Throwable ignored) {}
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
    // CASCADE LOOKUP — каскадные зависимые поля (Деятельность физ. лица:
    // SECTOR -> SEGMENT -> SUB_SEGMENT, следующее активируется после выбора предыдущего)
    // ══════════════════════════════════════

    @Step("Cascade lookup: выбрать '{value}' по marker '{marker}'")
    public LookupComponent selectCascadeByMarker(String marker, String value) {

        log.info("selectCascadeByMarker: marker='{}' value='{}'", marker, value);

        SelenideElement input = $x("//*[@data-item-marker='" + marker + "']//input")
                .shouldBe(visible);

        // ждём, что поле активно (не readonly) — следующее в каскаде включается не сразу
        input.shouldBe(not(attribute("readonly")), Duration.ofSeconds(30));

        executeJavaScript("arguments[0].focus();", input);
        executeJavaScript("arguments[0].click();", input);

        clearInput(input);
        input.sendKeys(value);

        SelenideElement listView = getVisibleListView();
        SelenideElement item = listView
                .$x(".//li[normalize-space(.)='" + value + "']")
                .shouldBe(visible, Duration.ofSeconds(30));
        executeJavaScript("arguments[0].click();", item);

        // soft-validation confirm (если есть)
        SelenideElement confirm = input.closest(".base-edit")
                .$x(".//div[contains(@id,'soft-validation-confirm')]");
        if (confirm.exists() && confirm.isDisplayed()) {
            log.debug("soft-validation confirm найден — подтверждаем");
            executeJavaScript("arguments[0].click();", confirm);
        }

        input.shouldHave(exactValue(value), Duration.ofSeconds(15));

        log.info("selectCascadeByMarker: '{}' выбрано", value);
        return this;
    }


    @Step("Text: ввести '{value}' по marker '{marker}'")
    public LookupComponent setTextByMarker(String marker, String value) {

        log.info("setTextByMarker: marker='{}' value='{}'", marker, value);

        SelenideElement input = $x("//*[@data-item-marker='" + marker + "']//input")
                .shouldBe(visible, enabled);

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                executeJavaScript("arguments[0].focus();", input);
                executeJavaScript("arguments[0].click();", input);

                clearInput(input);
                input.sendKeys(value);
                input.sendKeys(Keys.TAB);

                input.shouldHave(exactValue(value), Duration.ofSeconds(10));

                log.info("setTextByMarker: '{}' введено", value);
                return this;

            } catch (Throwable t) {
                log.warn("setTextByMarker попытка {}/3: {}", attempt, t.getMessage());
                try { clearInput(input); } catch (Throwable ignored) {}
                if (attempt == 3) {
                    throw new AssertionError(
                            "Не удалось ввести '" + value +
                                    "' по marker '" + marker + "'", t);
                }
            }
        }
        return this;
    }


    // ══════════════════════════════════════
    // CLEAR LOOKUP — очистить lookup-поле, если в нём есть значение
    // (крестик base-edit-clear-icon появляется только при непустом значении)
    // ══════════════════════════════════════

    @Step("Lookup: очистить поле по marker '{marker}' (если заполнено)")
    public LookupComponent clearByMarkerIfPresent(String marker) {
        log.info("clearByMarkerIfPresent: marker='{}'", marker);

        SelenideElement input = $x("//*[@data-item-marker='" + marker + "']//input");
        if (!input.exists()) {
            log.debug("Поле '{}' не найдено — пропуск", marker);
            return this;
        }

        String current = input.getValue();
        if (current == null || current.isBlank()) {
            log.debug("Поле '{}' уже пустое — пропуск", marker);
            return this;
        }

        // крестик очистки внутри того же base-edit
        SelenideElement clearIcon = input.closest(".base-edit")
                .$x(".//div[contains(@id,'clear-icon') " +
                        "and contains(@class,'base-edit-clear-icon')]");

        if (clearIcon.exists()) {
            executeJavaScript("arguments[0].click();", clearIcon);
            input.shouldHave(exactValue(""), Duration.ofSeconds(5));
            log.info("Поле '{}' очищено", marker);
        } else {
            // запасной путь — очистка через клавиатуру
            clearInput(input);
            log.info("Поле '{}' очищено (fallback)", marker);
        }
        return this;
    }


}