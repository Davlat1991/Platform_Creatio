package com.creatio.autotest.flows.retail.registration;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.CheckboxComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.pages.DetailPage;
import com.creatio.autotest.testdata.models.ExpenseData;
import com.creatio.autotest.testdata.models.IncomeData;
import com.creatio.autotest.testdata.models.RegistrationIncomeExpensesData;
import com.creatio.autotest.testdata.models.registration.ActivityData;
import com.creatio.autotest.testdata.models.registration.fields.ActivityField;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class RegistrationIncomeExpensesFlow {

    private static final Logger log =
            LoggerFactory.getLogger(RegistrationIncomeExpensesFlow.class);

    private final DetailPage        detailPage = new DetailPage();
    private final CheckboxComponent checkbox   = new CheckboxComponent();

    // ── Уникальные контейнеры деталей ─────
    // Оба имеют одинаковые column-name внутри (TsiKind, TsiFrequencyType, TsiAmountBC)
    // Скоупируем через родительский контейнер чтобы не конфликтовали
    private static final String INCOME_CONTAINER  = "BnzAppIncomesDetailDetailContainer";
    private static final String EXPENSE_CONTAINER = "BnzAppExpensesDetailDetailContainer";

    private final LookupComponent  lookup   = new LookupComponent();
    private final ButtonsComponent buttons  = new ButtonsComponent();
    private final BasePage         basePage = new BasePage();
    private final ClickService     click    = new ClickService();

    // ══════════════════════════════════════
    // PUBLIC API
    // ══════════════════════════════════════

    @Step("Заполнение доходов и расходов")
    public void fillIncomeAndExpenses(RegistrationIncomeExpensesData data) {
        log.info("Fill income and expenses");

        setRiskAndAdditionalInfo();
        addIncome(data.getIncome());
        addExpense(data.getExpense());
    }

    // ══════════════════════════════════════
    // PRIVATE — STEPS
    // ══════════════════════════════════════

    @Step("Уровень риска и дополнительная информация")
    private void setRiskAndAdditionalInfo() {
        lookup.select("Уровень риска", "Низкий");

        buttons.clickTabByName("ДОПОЛНИТЕЛЬНАЯ ИНФОРМАЦИЯ");

        lookup
                .select("Клиент предоставил подтверждение дохода", "Да")
                .select("Тип документа подтверждения дохода",
                        "Выписка с банковского счета");
    }

    @Step("Добавить доход")
    private void addIncome(IncomeData income) {
        log.info("Add income: type='{}' frequency='{}' amount='{}'",
                income.getType(), income.getFrequency(), income.getAmount());

        click.clickById("BnzAppIncomesDetailAddRecordButtonButton-imageEl");

        // Ждём появления строки ввода
        $x("//div[@data-item-marker='" + INCOME_CONTAINER + "']" +
                "//div[@data-item-marker='TsiKind']//input")
                .shouldBe(Condition.visible, Duration.ofSeconds(30));

        // selectInContainer сам кликает по полю и выбирает значение
        selectInContainer(INCOME_CONTAINER, "TsiKind",          income.getType());
        selectInContainer(INCOME_CONTAINER, "TsiFrequencyType", income.getFrequency());
        setNumericInContainer(INCOME_CONTAINER, "TsiAmountBC",  income.getAmount());

        saveInlineRow();
    }

    @Step("Добавить расход")
    private void addExpense(ExpenseData expense) {
        log.info("Add expense: type='{}' frequency='{}' amount='{}'",
                expense.getType(), expense.getFrequency(), expense.getAmount());

        click.clickById("BnzAppExpensesDetailAddRecordButtonButton-imageEl");

        // Ждём появления строки ввода
        $x("//div[@data-item-marker='" + EXPENSE_CONTAINER + "']" +
                "//div[@data-item-marker='TsiKind']//input")
                .shouldBe(Condition.visible, Duration.ofSeconds(30));

        selectInContainer(EXPENSE_CONTAINER, "TsiKind",          expense.getType());
        selectInContainer(EXPENSE_CONTAINER, "TsiFrequencyType", expense.getFrequency());
        setNumericInContainer(EXPENSE_CONTAINER, "TsiAmountBC",  expense.getAmount());

        saveInlineRow();
    }

    // ══════════════════════════════════════
    // PRIVATE — HELPERS
    // ══════════════════════════════════════

    /**
     * Кликает поле внутри конкретного контейнера детали,
     * затем выбирает значение из ul-дропдауна (рендерится в конце body — паттерн Creatio).
     * Решает проблему дублирующихся data-item-marker в деталях Доходы/Расходы.
     */
    private void selectInContainer(String container, String fieldMarker, String value) {
        log.debug("selectInContainer: container='{}' field='{}' value='{}'",
                container, fieldMarker, value);

        // Кликаем поле внутри нужного контейнера
        $x("//div[@data-item-marker='" + container + "']" +
                "//div[@data-item-marker='" + fieldMarker + "']//input")
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .click();

        // div.listview имеет data-item-marker — НЕ ul
        $x("//div[contains(@class,'listview') and @data-item-marker='" + fieldMarker + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30));

        // li внутри этого div
        $x("//div[contains(@class,'listview') and @data-item-marker='" + fieldMarker + "']" +
                "//li[@data-item-marker='" + value + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .click();

        log.debug("Selected: '{}'", value);
    }

    /**
     * Заполняет числовое поле внутри конкретного контейнера детали.
     */
    private void setNumericInContainer(String container, String fieldMarker, String value) {
        log.debug("setNumericInContainer: container='{}' field='{}' value='{}'",
                container, fieldMarker, value);

        SelenideElement input = $x(
                "//div[@data-item-marker='" + container + "']" +
                        "//div[@data-item-marker='" + fieldMarker + "']//input"
        ).shouldBe(Condition.visible, Duration.ofSeconds(15));

        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        input.sendKeys(value);
        input.sendKeys(Keys.TAB);
    }

    private void saveInlineRow() {
        log.info("Save inline row");

        SelenideElement saveBtn = $x("//*[@data-item-marker='save']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30));

        executeJavaScript("arguments[0].click();", saveBtn);
        basePage.waitForPage();
    }


    @Step("Заполнение деятельности физ. лица (Работает в организации)")
    public void fillIndividualActivityEmployed(ActivityData activity) {
        log.info("Fill individual activity (Employee)");

        buttons.clickTabByName("ДОПОЛНИТЕЛЬНАЯ ИНФОРМАЦИЯ");
        detailPage.clickAddRecord("Деятельность физ.лица");

        lookup.selectCascadeByMarker(ActivityField.SECTOR.marker,      activity.getSector());
        lookup.selectCascadeByMarker(ActivityField.SEGMENT.marker,     activity.getSegment());
        lookup.selectCascadeByMarker(ActivityField.SUB_SEGMENT.marker, activity.getSubSegment());
        lookup.selectCascadeByMarker(ActivityField.SALES_TYPE.marker,  activity.getSalesType());

        // market опционален — для Employee ("Не торгует") его нет
        if (activity.getMarket() != null && !activity.getMarket().isBlank()) {
            lookup.selectCascadeByMarker(ActivityField.MARKET.marker, activity.getMarket());
        }

        checkbox.ensureChecked("BnzIsPrimary");

        click.clickByName("Сохранить");
    }

    @Step("Заполнение деятельности физ. лица (Самозанятый)")
    public void fillIndividualActivitySelfEmployed(ActivityData activity) {
        log.info("Fill individual activity (SelfEmployed)");

        buttons.clickTabByName("ДОПОЛНИТЕЛЬНАЯ ИНФОРМАЦИЯ");
        detailPage.clickAddRecord("Деятельность физ.лица");

        lookup.selectCascadeByMarker(ActivityField.SECTOR.marker,      activity.getSector());
        lookup.selectCascadeByMarker(ActivityField.SEGMENT.marker,     activity.getSegment());
        lookup.selectCascadeByMarker(ActivityField.SUB_SEGMENT.marker, activity.getSubSegment());
        lookup.selectCascadeByMarker(ActivityField.SALES_TYPE.marker,  activity.getSalesType());

        if (activity.getMarket() != null && !activity.getMarket().isBlank()) {
            lookup.selectCascadeByMarker(ActivityField.MARKET.marker, activity.getMarket());
        }

        checkbox.ensureChecked("BnzIsPrimary");

        click.clickByName("Сохранить");


    }
}