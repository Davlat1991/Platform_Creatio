package com.creatio.autotest.flows.retail.participants;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.CheckboxComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.core.services.ScrollService;
import com.creatio.autotest.testdata.models.ExpenseData;
import com.creatio.autotest.testdata.models.IncomeData;
import com.creatio.autotest.testdata.models.ParticipantData;
import com.creatio.autotest.testdata.models.RegistrationIncomeExpensesData;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class GuarantorFlow {

    private static final Logger log =
            LoggerFactory.getLogger(GuarantorFlow.class);

    // ── Контейнеры деталей — такие же как у заёмщика ──
    // Поручитель открывается в отдельной карточке,
    // поэтому маркеры контейнеров те же самые
    private static final String INCOME_CONTAINER  =
            "BnzAppIncomesDetailDetailContainer";
    private static final String EXPENSE_CONTAINER =
            "BnzAppExpensesDetailDetailContainer";

    private final LookupComponent   lookup   = new LookupComponent();
    private final ButtonsComponent  buttons  = new ButtonsComponent();
    private final CheckboxComponent checkbox = new CheckboxComponent();
    private final BasePage          basePage = new BasePage();
    private final ClickService      click    = new ClickService();
    private final ScrollService     scroll   = new ScrollService();

    // ══════════════════════════════════════
    // PUBLIC API
    // ══════════════════════════════════════

    @Step("Заполнение анкеты поручителя")
    public void fill(ParticipantData participant,
                     RegistrationIncomeExpensesData incomeData) {
        log.info("Fill guarantor questionnaire");

        fillDocuments();
        fillAddresses();
        fillIncomeAndExpenses(incomeData);
        fillScoringData();
        fillOtherIncomeData();
        confirmScoring();
    }

    // ══════════════════════════════════════
    // PRIVATE — STEPS
    // ══════════════════════════════════════

    @Step("Документы поручителя")
    private void fillDocuments() {
        log.info("Fill guarantor documents");

        lookup.selectByMarker("BnzAffiliation",    "Мобильный");
        lookup.select("Страна рождения",            "Точикистон");
        lookup.selectByLabel("Дата выдачи",         "01.01.2020");
        lookup.selectByLabel("Действителен до",     "01.01.2030");

        click.doubleClickByMarker("Шиносномаи ЧТ");
        basePage.checkPage("RegDocumentPageV2Container");

        lookup.selectByLabel("Дата выдачи",  "01.01.2020");
        lookup.selectByLabel("Действует до", "01.01.2030");

        click.clickByName("Сохранить");
        basePage.checkPage("EntityLoaded");

        log.info("Guarantor documents filled");
    }

    @Step("Адреса поручителя")
    private void fillAddresses() {
        log.info("Fill guarantor addresses");

        buttons.doubleClickByName("Регистрация");
        setDateField("BnzRegistrationDate", "01.01.2020");
        click.clickByName("Сохранить");
        basePage.waitForPage();

        lookup.select("Тип клиента", "Такрори");

       /* buttons.doubleClickByName("Фактический");
        setDateField("BnzRegistrationDate", "01.01.2020");
        click.clickByName("Сохранить");
        basePage.waitForPage();*/

        lookup.select("Вид связи", "Партнер");

        log.info("Guarantor addresses filled");
    }

    @Step("Доходы и расходы поручителя")
    private void fillIncomeAndExpenses(RegistrationIncomeExpensesData data) {
        log.info("Fill guarantor income and expenses");

        // Уровень риска и доп. информация — идентично заёмщику
        lookup.select("Уровень риска", "Низкий");
        buttons.clickTabByName("ДОПОЛНИТЕЛЬНАЯ ИНФОРМАЦИЯ");
        lookup
                .select("Клиент предоставил подтверждение дохода", "Да")
                .select("Тип документа подтверждения дохода",
                        "Выписка с банковского счета");

        // Доход — через selectInContainer как у заёмщика
        addIncome(data.getIncome());

        // Расход — через selectInContainer как у заёмщика
        addExpense(data.getExpense());

        log.info("Guarantor income and expenses filled");
    }

    @Step("Скоринговые данные поручителя")
    private void fillScoringData() {
        log.info("Fill guarantor scoring data");

        lookup.select("Семейное положение",                   "Оиладор (зан)");
        lookup.selectByLabel("Количество иждивенцев (строка)", "3");
        lookup.selectByLabel("Количество детей",               "3");
        lookup.selectByLabel("Количество членов семьи",        "5");
        lookup.selectByLabel("Общий стаж",                     "36");
        lookup.selectByLabel("Общий стаж, лет",                "3");
        lookup.selectByLabel("Общий стаж, мес",                "36");
        lookup.selectByLabel("Стаж на последнем месте, лет",   "2");
        lookup.selectByLabel("Стаж на последнем месте, мес",   "24");

        log.info("Guarantor scoring data filled");
    }

    @Step("Причина отсутствия работы")
    private void fillOtherIncomeData() {
        log.info("Fill other income data");

        lookup.select("Причина отсутствия работы", "Получатель Д/П");

        log.info("Other income data filled");
    }

    @Step("Подтверждение скоринга поручителя")
    private void confirmScoring() {
        log.info("Confirm guarantor scoring");

        scroll.scrollToTop();
        buttons.clickTabByName("ОЦЕНКА ИНФОРМАЦИИ");

        lookup
                .select("Тип собственности на недвижимость", "Более 2-ух квартир")
                .select("Тип владения автомобилем",           "Есть автомобиль");

        checkbox.ensureCheckedBKI("IsConsentBKIProcessing");

        click.clickByName("Сохранить");
        click.clickByName("Закрыть");

        log.info("Guarantor scoring confirmed");
    }

    // ══════════════════════════════════════
    // PRIVATE — INCOME / EXPENSE
    // ══════════════════════════════════════

    private void addIncome(IncomeData income) {
        log.info("Add guarantor income: type='{}' frequency='{}' amount='{}'",
                income.getType(), income.getFrequency(), income.getAmount());

        click.clickById("BnzAppIncomesDetailAddRecordButtonButton-imageEl");

        $x("//div[@data-item-marker='" + INCOME_CONTAINER + "']" +
                "//div[@data-item-marker='TsiKind']//input")
                .shouldBe(Condition.visible, Duration.ofSeconds(30));

        selectInContainer(INCOME_CONTAINER, "TsiKind",          income.getType());
        selectInContainer(INCOME_CONTAINER, "TsiFrequencyType", income.getFrequency());
        setNumericInContainer(INCOME_CONTAINER, "TsiAmountBC",  income.getAmount());

        saveInlineRow();
    }

    private void addExpense(ExpenseData expense) {
        log.info("Add guarantor expense: type='{}' frequency='{}' amount='{}'",
                expense.getType(), expense.getFrequency(), expense.getAmount());

        click.clickById("BnzAppExpensesDetailAddRecordButtonButton-imageEl");

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
     * Скоуп через контейнер детали — решает проблему
     * дублирующихся маркеров TsiKind/TsiFrequencyType/TsiAmountBC
     * в деталях Доходы и Расходы.
     */
    private void selectInContainer(String container,
                                   String fieldMarker,
                                   String value) {
        log.debug("selectInContainer: container='{}' field='{}' value='{}'",
                container, fieldMarker, value);

        $x("//div[@data-item-marker='" + container + "']" +
                "//div[@data-item-marker='" + fieldMarker + "']//input")
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .click();

        $x("//div[contains(@class,'listview') and @data-item-marker='" + fieldMarker + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30));

        $x("//div[contains(@class,'listview') and @data-item-marker='" + fieldMarker + "']" +
                "//li[@data-item-marker='" + value + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .click();

        log.debug("Selected: '{}'", value);
    }

    private void setNumericInContainer(String container,
                                       String fieldMarker,
                                       String value) {
        log.debug("setNumericInContainer: container='{}' field='{}' value='{}'",
                container, fieldMarker, value);

        SelenideElement input = $x(
                "//div[@data-item-marker='" + container + "']" +
                        "//div[@data-item-marker='" + fieldMarker + "']//input"
        ).shouldBe(Condition.visible, Duration.ofSeconds(30));

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

    private void setDateField(String marker, String value) {
        log.debug("setDateField: marker='{}' value='{}'", marker, value);

        $x("//*[@data-item-marker='" + marker + "']//input")
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .setValue(value);
    }
}