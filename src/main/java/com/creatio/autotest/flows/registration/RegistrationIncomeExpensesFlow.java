package com.creatio.autotest.flows.registration;

import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.CheckboxComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.components.MenuComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.testdata.models.ExpenseData;
import com.creatio.autotest.testdata.models.IncomeData;
import com.creatio.autotest.testdata.models.RegistrationIncomeExpensesData;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class RegistrationIncomeExpensesFlow {

    private static final Logger log =
            LoggerFactory.getLogger(RegistrationIncomeExpensesFlow.class);

    private final LookupComponent   lookup   = new LookupComponent();
    private final ButtonsComponent  buttons  = new ButtonsComponent();
    private final MenuComponent     menu     = new MenuComponent();
    private final CheckboxComponent checkbox = new CheckboxComponent();
    private final BasePage          basePage = new BasePage();
    private final ClickService      click    = new ClickService();

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
                .select("Клиент предоставил подтверждение дохода",
                        "Да")
                .select("Тип документа подтверждения дохода",
                        "Выписка с банковского счета");
    }

    @Step("Добавить доход")
    private void addIncome(IncomeData income) {
        log.info("Add income: type='{}'", income.getType());

        click.clickById(
                "BnzAppIncomesDetailAddRecordButtonButton-imageEl"
        );

        setFieldByMarker("TsiKind", income.getType());
        menu.clickByName(income.getType());

        setFieldByMarker("TsiFrequencyType", income.getFrequency());
        menu.clickByName(income.getFrequency());

        setFieldByMarker("TsiAmountBC", income.getAmount());

        click.clickByMarker("save");
    }

    @Step("Добавить расход")
    private void addExpense(ExpenseData expense) {
        log.info("Add expense: type='{}'", expense.getType());

        click.clickById(
                "BnzAppExpensesDetailAddRecordButtonButton-imageEl"
        );

        checkbox.clickByInputId(
                "BnzAppExpensesDetailTsiKindComboBoxEdit-el"
        );
        menu.clickByName(expense.getType());

        checkbox.clickByInputId(
                "BnzAppExpensesDetailTsiFrequencyTypeComboBoxEdit-el"
        );
        menu.clickByName(expense.getFrequency());

        setFieldByMarker("TsiAmountBC", expense.getAmount());

        click.clickByMarker("save");
    }

    private void setFieldByMarker(String marker, String value) {
        $x("//div[@data-item-marker='" + marker + "']/input")
                .setValue(value);
    }
}