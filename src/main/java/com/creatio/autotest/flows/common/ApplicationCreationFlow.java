package com.creatio.autotest.flows.common;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.components.MessageBoxComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.testdata.models.ProductData;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.*;

public class ApplicationCreationFlow {

    private static final Logger log =
            LoggerFactory.getLogger(ApplicationCreationFlow.class);

    private final BasePage            basePage = new BasePage();
    private final MessageBoxComponent msgBox   = new MessageBoxComponent();
    private final LookupComponent     lookup   = new LookupComponent();

    // ══════════════════════════════════════
    // MAIN
    // ══════════════════════════════════════

    @Step("Создание кредитной заявки")
    public void createApplication(ProductData product) {
        log.info("Create application: repaymentDay='{}' scheduleNumber='{}' scheduleType='{}' term='{}'",
                product.getRepaymentDay(),
                product.getScheduleNumber(),
                product.getScheduleType(),
                product.getTerm());

        setRepaymentDate(product.getRepaymentDay());
        addScheduleRow();
        fillScheduleDetails(
                product.getScheduleNumber(),
                product.getScheduleType(),
                product.getTerm()
        );
        saveScheduleRow();
        calculateAndCreate();
        confirmNoDebt();
        waitForApplicationCreated();
    }

    // ══════════════════════════════════════
    // STEPS
    // ══════════════════════════════════════

    @Step("Запрашиваемая дата погашения: {day}")
    private void setRepaymentDate(String day) {
        log.info("Set repayment date: {}", day);

        SelenideElement input = $x(
                "//div[@data-item-marker='RequestedPaymentDay']//input"
        ).shouldBe(Condition.visible, Duration.ofSeconds(30));

        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        input.sendKeys(day);
        input.sendKeys(Keys.TAB);

        log.info("Repayment date set: {}", day);
    }

    @Step("Добавить строку в Параметры графика")
    private void addScheduleRow() {
        log.info("Add schedule row");

        $x("//*[@id='KzParameterScheduleDetailAddRecordButtonButton-imageEl']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .click();

        // Ждём появления поля ввода в новой строке
        $x("//div[@data-item-marker='KzNumber']//input")
                .shouldBe(Condition.visible, Duration.ofSeconds(30));

        log.info("Schedule row added");
    }

    @Step("Заполнить строку графика: number={number} type={type} term={term}")
    private void fillScheduleDetails(String number, String type, String term) {
        log.info("Fill schedule: number='{}' type='{}' term='{}'",
                number, type, term);

        // Последовательность — числовое поле
        setNumericFieldByMarker("KzNumber", number);

        // Метод расчёта — справочное поле
        // Используем LookupComponent.selectByMarker() — универсальный метод для всех lookup в проекте
        lookup.selectByMarker("KzTypeScheduler", type);

        // Срок, месяц — совпадает со сроком подбора продукта
        setNumericFieldByMarker("KzTermMonth", term);

        log.info("Schedule details filled");
    }

    @Step("Сохранить строку графика (галочка)")
    private void saveScheduleRow() {
        log.info("Save schedule row");

        SelenideElement saveBtn = $x(
                "//span[@data-item-marker='save' and @data-tag='save']"
        ).shouldBe(Condition.visible, Duration.ofSeconds(30));

        executeJavaScript("arguments[0].click();", saveBtn);

        // Ждём исчезновения галочки — строка сохранена
        $x("//span[@data-item-marker='save' and @data-tag='save']")
                .shouldBe(Condition.hidden, Duration.ofSeconds(30));

        log.info("Schedule row saved");
    }

    @Step("Рассчитать график и создать заявку")
    private void calculateAndCreate() {
        log.info("Click РАССЧИТАТЬ");

        $x("//span[normalize-space()='РАССЧИТАТЬ']" +
                " | //span[normalize-space()='Рассчитать']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .click();

        basePage.waitForPage();

        // Ждём, пока расчёт заполнит деталь "График" строками платежей.
        // Грид результата: #grid-KzProductCalculateScheduleDetailDataGridGrid-wrap,
        // строки — grid-listed-row (подтверждено по DOM). Без этого "Создать заявку"
        // жмётся раньше времени → "Необходимо рассчитать график платежей".
        $$x("//div[@id='grid-KzProductCalculateScheduleDetailDataGridGrid-wrap']" +
                "//div[contains(@class,'grid-listed-row')]")
                .shouldHave(CollectionCondition.sizeGreaterThanOrEqual(1),
                        Duration.ofSeconds(30));

        log.info("Schedule calculated, click СОЗДАТЬ ЗАЯВКУ");

        SelenideElement createBtn = $x(
                "//span[normalize-space()='СОЗДАТЬ ЗАЯВКУ']" +
                        " | //span[normalize-space()='Создать заявку']"
        ).shouldBe(Condition.visible, Duration.ofSeconds(15));

        executeJavaScript("arguments[0].click();", createBtn);
        basePage.waitForPage();

        log.info("Application creation submitted");
    }

    @Step("Подтвердить отсутствие задолженности")
    private void confirmNoDebt() {
        log.info("Confirm no debt dialog");

        msgBox.shouldSeeModal("Нет задолженности!");

        $x("//*[@data-item-marker='ОК']" +
                " | //span[normalize-space()='ОК']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .click();

        basePage.waitForPage();
        log.info("No debt confirmed");
    }

    @Step("Ожидание готовности карточки созданной заявки")
    private void waitForApplicationCreated() {
        log.info("Wait for application card ready");

        // «Создание заявки» — асинхронный бизнес-процесс. waitForPage() отдаёт
        // управление ДО навигации на карточку, из-за чего следующий флоу лезет
        // в поля отсутствующей карточки. Ждём открытие FinApplicationPage
        // и загрузку записи (data-item-marker='EntityLoaded').
        $x("//div[@data-item-marker='FinApplicationPageContainer']")
                .shouldBe(Condition.visible, Duration.ofSeconds(60));

        $x("//div[@data-item-marker='EntityLoaded']")
                .shouldBe(Condition.visible, Duration.ofSeconds(60));

        log.info("Application card ready");
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

    private void setNumericFieldByMarker(String marker, String value) {
        log.debug("setNumericField: marker='{}' value='{}'", marker, value);

        SelenideElement input = $x(
                "//div[@data-item-marker='" + marker + "']//input"
        ).shouldBe(Condition.visible, Duration.ofSeconds(30));

        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        input.sendKeys(value);
        input.sendKeys(Keys.TAB);
    }
}