package com.creatio.autotest.flows.registration;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.core.services.ScrollService;
import com.creatio.autotest.testdata.models.ExpenseData;
import com.creatio.autotest.testdata.models.IncomeData;
import com.creatio.autotest.testdata.models.RegistrationIncomeExpensesData;
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

    private final LookupComponent  lookup   = new LookupComponent();
    private final ButtonsComponent buttons  = new ButtonsComponent();
    private final BasePage         basePage = new BasePage();
    private final ClickService     click    = new ClickService();
    private final ScrollService scroll  = new ScrollService();

    @Step("Заполнение доходов и расходов")
    public void fillIncomeAndExpenses(RegistrationIncomeExpensesData data) {
        log.info("Fill income and expenses");

        setRiskAndAdditionalInfo();
        addIncome(data.getIncome());
        addExpense(data.getExpense());
    }

    // ══════════════════════════════════════
    // PRIVATE
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

        $x("//div[@data-item-marker='TsiKind']//input")
                .shouldBe(Condition.visible, Duration.ofSeconds(10));

        openInlineField("TsiKind");
        selectFromListview("TsiKind", income.getType());

        openInlineField("TsiFrequencyType");
        selectFromListview("TsiFrequencyType", income.getFrequency());

        setInlineNumericField("TsiAmountBC", income.getAmount());

        saveInlineRow();
    }

    @Step("Добавить расход")
    private void addExpense(ExpenseData expense) {
        log.info("Add expense: type='{}' frequency='{}' amount='{}'",
                expense.getType(), expense.getFrequency(), expense.getAmount());

        click.clickById("BnzAppExpensesDetailAddRecordButtonButton-imageEl");

        $x("//input[@id='BnzAppExpensesDetailTsiKindComboBoxEdit-el']")
                .shouldBe(Condition.visible, Duration.ofSeconds(10))
                .click();

        selectFromListview("TsiKind", expense.getType());

        $x("//input[@id='BnzAppExpensesDetailTsiFrequencyTypeComboBoxEdit-el']")
                .shouldBe(Condition.visible, Duration.ofSeconds(10))
                .click();

        selectFromListview("TsiFrequencyType", expense.getFrequency());

        setInlineNumericField("TsiAmountBC", expense.getAmount());

        saveInlineRow();
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

    private void openInlineField(String marker) {
        $x("//div[@data-item-marker='" + marker + "']//input")
                .shouldBe(Condition.visible, Duration.ofSeconds(10))
                .click();
    }

    private void selectFromListview(String listviewMarker, String itemMarker) {
        log.debug("selectFromListview: listview='{}' item='{}'", listviewMarker, itemMarker);

        $x("//div[@data-item-marker='" + listviewMarker + "']" +
                "//li[@data-item-marker='" + itemMarker + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(7))
                .click();
    }

    private void setInlineNumericField(String marker, String value) {
        log.debug("setInlineNumericField: marker='{}' value='{}'", marker, value);

        SelenideElement input = $x(
                "//div[@data-item-marker='" + marker + "']//input"
        ).shouldBe(Condition.visible, Duration.ofSeconds(10));

        input.click();
        input.sendKeys(Keys.CONTROL + "a");
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