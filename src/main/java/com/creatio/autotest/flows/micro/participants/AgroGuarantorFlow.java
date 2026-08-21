package com.creatio.autotest.flows.micro.participants;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.CheckboxComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.core.services.ScrollService;
import com.creatio.autotest.testdata.models.AgroGuarantorData;
import com.creatio.autotest.testdata.models.ExpenseData;
import com.creatio.autotest.testdata.models.IncomeData;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

/**
 * Анкета поручителя (Агро/Микро) — та же карточка с вкладками, что у заёмщика:
 * Документы → Адреса → Доход/расход + доп.инфо → Скоринг → Оценка информации →
 * Сохранить/Закрыть.
 * <p>
 * Изолированная Микро-копия розничного {@code GuarantorFlow}: шаги идентичны, но
 * значения приходят из {@link AgroGuarantorData} (без хардкода в теле), а даты
 * вводятся через {@code sendKeys + Keys.TAB} (setValue не триггерит onChange).
 * Общие компоненты — из Core.
 */
public class AgroGuarantorFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroGuarantorFlow.class);

    // Контейнеры деталей доход/расход — те же маркеры, что у заёмщика
    private static final String INCOME_CONTAINER  = "BnzAppIncomesDetailDetailContainer";
    private static final String EXPENSE_CONTAINER = "BnzAppExpensesDetailDetailContainer";

    private final LookupComponent   lookup   = new LookupComponent();
    private final ButtonsComponent  buttons  = new ButtonsComponent();
    private final CheckboxComponent checkbox = new CheckboxComponent();
    private final BasePage          basePage = new BasePage();
    private final ClickService      click    = new ClickService();
    private final ScrollService     scroll   = new ScrollService();

    // ══════════════════════════════════════
    // PUBLIC API
    // ══════════════════════════════════════

    @Step("Агро: заполнение анкеты поручителя")
    public void fill(AgroGuarantorData data) {
        log.info("Agro: fill guarantor questionnaire");

        waitForQuestionnaireCard();
        fillDocuments(data);
        fillAddresses(data);
        fillIncomeAndExpenses(data);
        fillScoringData(data);
        fillOtherIncomeData(data);
        confirmScoring(data);

        log.info("Agro: guarantor questionnaire filled");
    }

    // ══════════════════════════════════════
    // PRIVATE — STEPS
    // ══════════════════════════════════════

    @Step("Документы поручителя")
    private void fillDocuments(AgroGuarantorData data) {
        log.info("Fill guarantor documents");

        lookup.selectByMarker("BnzAffiliation", data.getCommunicationMeans());
        lookup.select("Страна рождения",         data.getBirthCountry());
        lookup.selectByLabel("Дата выдачи",      data.getDocIssueDate());
        lookup.selectByLabel("Действителен до",  data.getDocValidUntil());

        click.doubleClickByMarker("Шиносномаи ЧТ");
        basePage.checkPage("RegDocumentPageV2Container");

        lookup.selectByLabel("Дата выдачи",  data.getPassportIssueDate());
        lookup.selectByLabel("Действует до", data.getPassportValidUntil());

        click.clickByName("Сохранить");
        basePage.checkPage("EntityLoaded");

        log.info("Guarantor documents filled");
    }

    @Step("Адреса поручителя")
    private void fillAddresses(AgroGuarantorData data) {
        log.info("Fill guarantor addresses");

        buttons.doubleClickByName("Регистрация");
        setDateField("BnzRegistrationDate", data.getRegistrationDate());
        click.clickByName("Сохранить");
        basePage.waitForPage();

        lookup.select("Тип клиента", data.getClientType());
        lookup.select("Вид связи",   data.getRelationType());

        log.info("Guarantor addresses filled");
    }

    @Step("Доходы и расходы поручителя")
    private void fillIncomeAndExpenses(AgroGuarantorData data) {
        log.info("Fill guarantor income and expenses");

        lookup.select("Уровень риска", data.getRiskLevel());
        buttons.clickTabByName("ДОПОЛНИТЕЛЬНАЯ ИНФОРМАЦИЯ");
        lookup
                .select("Клиент предоставил подтверждение дохода",
                        data.getIncomeConfirmationProvided())
                .select("Тип документа подтверждения дохода",
                        data.getIncomeConfirmationDocType());

        addIncome(data.getIncome());
        addExpense(data.getExpense());

        log.info("Guarantor income and expenses filled");
    }

    @Step("Скоринговые данные поручителя")
    private void fillScoringData(AgroGuarantorData data) {
        log.info("Fill guarantor scoring data");

        lookup.select("Семейное положение",                     data.getMaritalStatus());
        lookup.selectByLabel("Количество иждивенцев (строка)",  data.getDependents());
        lookup.selectByLabel("Количество детей",                data.getChildren());
        lookup.selectByLabel("Количество членов семьи",         data.getFamilyMembers());
        lookup.selectByLabel("Общий стаж",                      data.getTotalExperience());
        lookup.selectByLabel("Общий стаж, лет",                 data.getExperienceYears());
        lookup.selectByLabel("Общий стаж, мес",                 data.getExperienceMonths());
        lookup.selectByLabel("Стаж на последнем месте, лет",    data.getLastPlaceYears());
        lookup.selectByLabel("Стаж на последнем месте, мес",    data.getLastPlaceMonths());

        log.info("Guarantor scoring data filled");
    }

    @Step("Причина отсутствия работы")
    private void fillOtherIncomeData(AgroGuarantorData data) {
        log.info("Fill other income data");

        lookup.select("Причина отсутствия работы", data.getReasonForNotWorking());

        log.info("Other income data filled");
    }

    @Step("Подтверждение скоринга поручителя")
    private void confirmScoring(AgroGuarantorData data) {
        log.info("Confirm guarantor scoring");

        scroll.scrollToTop();
        buttons.clickTabByName("ОЦЕНКА ИНФОРМАЦИИ");

        lookup
                .select("Тип собственности на недвижимость", data.getPropertyOwnershipType())
                .select("Тип владения автомобилем",           data.getVehicleOwnershipType());

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
                .shouldBe(Condition.visible, Duration.ofSeconds(10));

        selectInContainer(INCOME_CONTAINER,     "TsiKind",          income.getType());
        selectInContainer(INCOME_CONTAINER,     "TsiFrequencyType", income.getFrequency());
        setNumericInContainer(INCOME_CONTAINER, "TsiAmountBC",      income.getAmount());

        saveInlineRow();
    }

    private void addExpense(ExpenseData expense) {
        log.info("Add guarantor expense: type='{}' frequency='{}' amount='{}'",
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

    /** Скоуп через контейнер детали — снимает дубли маркеров в Доходах/Расходах. */
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
        ).shouldBe(Condition.visible, Duration.ofSeconds(10));

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

    /** Дата через sendKeys + TAB (setValue не триггерит onChange/onBlur в Creatio). */
    private void setDateField(String marker, String value) {
        log.debug("setDateField: marker='{}' value='{}'", marker, value);

        SelenideElement input = $x("//*[@data-item-marker='" + marker + "']//input")
                .shouldBe(Condition.visible, Duration.ofSeconds(10));

        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        input.sendKeys(value);
        input.sendKeys(Keys.TAB);
    }

    /**
     * Анкета участника открывается после «Выбрать» асинхронно: розничный
     * lookup.selectByMarker имеет собственный таймаут 10 с и падает раньше,
     * чем карточка успевает отрисоваться. Ждём первое поле анкеты.
     */
    @Step("Ожидание готовности анкеты поручителя")
    private void waitForQuestionnaireCard() {
        log.info("Wait for guarantor questionnaire card");

        $x("//*[@data-item-marker='BnzAffiliation']//input")
                .shouldBe(Condition.visible, Duration.ofSeconds(60));

        log.info("Guarantor questionnaire card ready");
    }
}