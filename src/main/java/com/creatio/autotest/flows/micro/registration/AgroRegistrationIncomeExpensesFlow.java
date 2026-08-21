package com.creatio.autotest.flows.micro.registration;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.CheckboxComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.components.MessageBoxComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.pages.DetailPage;
import com.creatio.autotest.testdata.models.ExpenseData;
import com.creatio.autotest.testdata.models.IncomeData;
import com.creatio.autotest.testdata.models.IncomeExtraData;
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

/**
 * Флоу «Доходы и расходы» стадии «Оформление» (анкета заёмщика) для продуктов
 * сегмента «Микро».
 * <p>
 * Самостоятельный класс в ветке {@code flows.micro.registration}, изолированный
 * от розничного {@code RegistrationIncomeExpensesFlow}. Логика идентична
 * рознице (подтверждено), но данные (уровень риска, подтверждение дохода)
 * вынесены в {@link IncomeExtraData} — без хардкода.
 */
public class AgroRegistrationIncomeExpensesFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroRegistrationIncomeExpensesFlow.class);

    private static final String INCOME_CONTAINER  = "BnzAppIncomesDetailDetailContainer";
    private static final String EXPENSE_CONTAINER = "BnzAppExpensesDetailDetailContainer";

    private final DetailPage           detailPage = new DetailPage();
    private final CheckboxComponent    checkbox   = new CheckboxComponent();
    private final LookupComponent      lookup     = new LookupComponent();
    private final ButtonsComponent     buttons    = new ButtonsComponent();
    private final BasePage             basePage   = new BasePage();
    private final ClickService         click      = new ClickService();
    private final MessageBoxComponent  msgBox     = new MessageBoxComponent();

    // ══════════════════════════════════════
    // PUBLIC API
    // ══════════════════════════════════════

    @Step("Заполнение доходов и расходов (Агро/Микро)")
    public void fillIncomeAndExpenses(RegistrationIncomeExpensesData data, IncomeExtraData extra) {
        log.info("Fill income and expenses (Agro/Micro)");

        setRiskAndAdditionalInfo(extra);
        addIncome(data.getIncome());
        addExpense(data.getExpense());
    }

    @Step("Заполнение деятельности физ. лица (Самозанятый) — Агро/Микро")
    public void fillIndividualActivitySelfEmployed(ActivityData activity) {
        log.info("Fill individual activity (SelfEmployed) Agro/Micro");

        // Всегда возвращаемся на «Дополнительную информацию»: после добавления
        // адреса система авто-переключает на «Основную», а при skip адреса мы уже
        // здесь (клик идемпотентен — вкладка остаётся активной).
        buttons.clickTabByName("ДОПОЛНИТЕЛЬНАЯ ИНФОРМАЦИЯ");

        detailPage.clickAddRecord("Деятельность физ.лица");

        // диалог смены типа занятости (может появиться)
        msgBox.confirmIfPresent("Да", "При изменении типа занятости");

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

    // ══════════════════════════════════════
    // PRIVATE — STEPS
    // ══════════════════════════════════════

    @Step("Уровень риска и дополнительная информация")
    private void setRiskAndAdditionalInfo(IncomeExtraData extra) {
        lookup.select("Уровень риска", extra.getRiskLevel());

        buttons.clickTabByName("ДОПОЛНИТЕЛЬНАЯ ИНФОРМАЦИЯ");

        lookup
                .select("Клиент предоставил подтверждение дохода", extra.getIncomeConfirmation())
                .select("Тип документа подтверждения дохода",      extra.getConfirmationDocType());
    }

    @Step("Добавить доход")
    private void addIncome(IncomeData income) {
        log.info("Add income: type='{}' frequency='{}' amount='{}'",
                income.getType(), income.getFrequency(), income.getAmount());

        click.clickById("BnzAppIncomesDetailAddRecordButtonButton-imageEl");

        $x("//div[@data-item-marker='" + INCOME_CONTAINER + "']" +
                "//div[@data-item-marker='TsiKind']//input")
                .shouldBe(Condition.visible, Duration.ofSeconds(10));

        selectInContainer(INCOME_CONTAINER,     "TsiKind",          income.getType());
        selectInContainer(INCOME_CONTAINER,     "TsiFrequencyType", income.getFrequency());
        setNumericInContainer(INCOME_CONTAINER, "TsiAmountBC",      income.getAmount());

        saveInlineRow();
    }

    @Step("Добавить расход")
    private void addExpense(ExpenseData expense) {
        log.info("Add expense: type='{}' frequency='{}' amount='{}'",
                expense.getType(), expense.getFrequency(), expense.getAmount());

        click.clickById("BnzAppExpensesDetailAddRecordButtonButton-imageEl");

        $x("//div[@data-item-marker='" + EXPENSE_CONTAINER + "']" +
                "//div[@data-item-marker='TsiKind']//input")
                .shouldBe(Condition.visible, Duration.ofSeconds(10));

        selectInContainer(EXPENSE_CONTAINER,     "TsiKind",          expense.getType());
        selectInContainer(EXPENSE_CONTAINER,     "TsiFrequencyType", expense.getFrequency());
        setNumericInContainer(EXPENSE_CONTAINER, "TsiAmountBC",      expense.getAmount());

        saveInlineRow();
    }

    // ══════════════════════════════════════
    // PRIVATE — HELPERS
    // ══════════════════════════════════════

    private void selectInContainer(String container, String fieldMarker, String value) {
        log.debug("selectInContainer: container='{}' field='{}' value='{}'",
                container, fieldMarker, value);

        $x("//div[@data-item-marker='" + container + "']" +
                "//div[@data-item-marker='" + fieldMarker + "']//input")
                .shouldBe(Condition.visible, Duration.ofSeconds(10))
                .click();

        $x("//div[contains(@class,'listview') and @data-item-marker='" + fieldMarker + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(10));

        $x("//div[contains(@class,'listview') and @data-item-marker='" + fieldMarker + "']" +
                "//li[@data-item-marker='" + value + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(10))
                .click();

        log.debug("Selected: '{}'", value);
    }

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
                .shouldBe(Condition.visible, Duration.ofSeconds(10));

        executeJavaScript("arguments[0].click();", saveBtn);
        basePage.waitForPage();
    }
}