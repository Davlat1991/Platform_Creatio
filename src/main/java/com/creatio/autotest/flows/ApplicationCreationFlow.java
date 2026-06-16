package com.creatio.autotest.flows;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.components.MessageBoxComponent;
import com.creatio.autotest.components.MenuComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class ApplicationCreationFlow {

    private static final Logger log =
            LoggerFactory.getLogger(ApplicationCreationFlow.class);

    private final BasePage            basePage = new BasePage();
    private final MessageBoxComponent msgBox   = new MessageBoxComponent();
    private final MenuComponent       menu     = new MenuComponent();
    private final ClickService        click    = new ClickService();

    // ══════════════════════════════════════
    // MAIN
    // ══════════════════════════════════════

    @Step("Создание кредитной заявки")
    public void createApplication(String repaymentDay,
                                  String scheduleNumber,
                                  String scheduleType,
                                  String termMonths) {
        log.info("Create application: day='{}' number='{}' type='{}' term='{}'",
                repaymentDay, scheduleNumber, scheduleType, termMonths);

        setRepaymentDate(repaymentDay);
        addScheduleRow();
        fillScheduleDetails(scheduleNumber, scheduleType, termMonths);
        calculateAndCreate();
        confirmNoDebt();
    }

    // ══════════════════════════════════════
    // STEPS
    // ══════════════════════════════════════

    @Step("Запрашиваемая дата погашения: {day}")
    private void setRepaymentDate(String day) {
        log.info("Set repayment date: {}", day);

        SelenideElement input = $x(
                "//label[normalize-space()='Запрашиваемая дата погашения']" +
                        "/../..//input"
        ).shouldBe(Condition.visible, Duration.ofSeconds(10));

        input.click();
        input.sendKeys(Keys.CONTROL + "a");
        input.sendKeys(day);
        input.sendKeys(Keys.TAB);
    }

    @Step("Добавить строку графика")
    private void addScheduleRow() {
        log.info("Add schedule row");

        click.clickById("KzParameterScheduleDetailAddRecordButtonButton-imageEl");

        // Ждём появления строки для ввода
        $x("//div[@data-item-marker='KzNumber']//input")
                .shouldBe(Condition.visible, Duration.ofSeconds(10));
    }

    @Step("Заполнить детали графика: number={number} type={type} term={term}")
    private void fillScheduleDetails(String number, String type, String term) {
        log.info("Fill schedule: number='{}' type='{}' term='{}'",
                number, type, term);

        // Номер — текстовое поле
        setNumericFieldByMarker("KzNumber", number);

        // Тип графика — справочник
        setHandbookFieldByMarker("KzTypeScheduler", type);

        // Срок, месяц — числовое поле.
        // setValue НЕ триггерит onChange в Creatio → используем sendKeys + Tab
        setNumericFieldByMarker("KzTermMonth", term);

        // Сохраняем строку грида
        saveGridRow();
    }

    @Step("Рассчитать и создать заявку")
    private void calculateAndCreate() {
        log.info("Calculate and create application");

        click.clickByName("Рассчитать");
        basePage.waitForPage();

        // JS-клик обходит перекрытие другими элементами (overlay, spinner)
        SelenideElement createBtn = $x(
                "//span[normalize-space()='Создать заявку']"
        ).shouldBe(Condition.visible, Duration.ofSeconds(15));

        executeJavaScript("arguments[0].click();", createBtn);
        basePage.waitForPage();
    }

    @Step("Подтвердить отсутствие задолженности")
    private void confirmNoDebt() {
        log.info("Confirm no debt");

        msgBox.shouldSeeModal("Нет задолженности!");
        click.clickByMarker("ОК");
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

    /**
     * Числовое/текстовое поле по data-item-marker.
     * sendKeys + Tab фиксирует значение в Creatio (setValue не триггерит onChange).
     */
    private void setNumericFieldByMarker(String marker, String value) {
        log.debug("setNumericFieldByMarker: marker='{}' value='{}'", marker, value);

        SelenideElement input = $x(
                "//div[@data-item-marker='" + marker + "']//input"
        ).shouldBe(Condition.visible, Duration.ofSeconds(10));

        input.click();
        input.sendKeys(Keys.CONTROL + "a");
        input.sendKeys(Keys.DELETE);
        input.sendKeys(value);
        input.sendKeys(Keys.TAB);
    }

    /**
     * Справочное поле по data-item-marker → выбор из меню.
     */
    private void setHandbookFieldByMarker(String marker, String value) {
        log.debug("setHandbookFieldByMarker: marker='{}' value='{}'", marker, value);

        SelenideElement input = $x(
                "//div[@data-item-marker='" + marker + "']//input"
        ).shouldBe(Condition.visible, Duration.ofSeconds(10));

        input.click();
        input.sendKeys(Keys.CONTROL + "a");
        input.sendKeys(Keys.DELETE);
        input.sendKeys(value);

        menu.clickByName(value);
    }

    /**
     * Сохранение строки inline-грида.
     * Пробуем data-item-marker='save', затем JS-клик на кнопку сохранения.
     */
    private void saveGridRow() {
        log.info("Save grid row");

        SelenideElement saveBtn = $x(
                "//*[@data-item-marker='save']" +
                        " | //span[normalize-space()='Сохранить']/ancestor::button" +
                        " | //span[normalize-space()='Сохранить']/ancestor::a"
        ).shouldBe(Condition.visible, Duration.ofSeconds(10));

        executeJavaScript("arguments[0].click();", saveBtn);
    }
}