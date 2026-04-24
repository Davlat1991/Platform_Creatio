package com.creatio.autotest.flows.participants;

import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.CheckboxComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.components.MenuComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.core.services.ScrollService;
import com.creatio.autotest.testdata.models.ParticipantData;
import com.creatio.autotest.testdata.models.RegistrationIncomeExpensesData;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.$x;

public class GuarantorFlow {

    private static final Logger log = LoggerFactory.getLogger(GuarantorFlow.class);

    private final LookupComponent   lookup   = new LookupComponent();
    private final ButtonsComponent  buttons  = new ButtonsComponent();
    private final CheckboxComponent checkbox = new CheckboxComponent();
    private final MenuComponent     menu     = new MenuComponent();
    private final BasePage          basePage = new BasePage();
    private final ClickService      click    = new ClickService();
    private final ScrollService scroll  = new ScrollService();

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
    // PRIVATE
    // ══════════════════════════════════════

    @Step("Документы поручителя")
    private void fillDocuments() {
        lookup.selectByMarker("BnzAffiliation", "Мобильный");
        lookup.select("Страна рождения",  "Точикистон");
        lookup.selectByLabel("Дата выдачи",     "01.01.2020");
        lookup.selectByLabel("Действителен до", "01.01.2030");

        click.doubleClickByMarker("Шиносномаи ЧТ");
        basePage.checkPage("RegDocumentPageV2Container");

        lookup.selectByLabel("Дата выдачи",  "01.01.2020");
        lookup.selectByLabel("Действует до", "01.01.2030");

        click.clickByName("Сохранить");
        basePage.checkPage("EntityLoaded");
    }

    @Step("Адреса поручителя")
    private void fillAddresses() {
        buttons.doubleClickByName("Регистрация");
        setDateField("BnzRegistrationDate", "01.01.2020");
        click.clickByName("Сохранить");
        basePage.waitForPage();

        lookup.select("Тип клиента", "Такрори");

        buttons.doubleClickByName("Фактический");
        setDateField("BnzRegistrationDate", "01.01.2020");
        click.clickByName("Сохранить");
        basePage.waitForPage();

        lookup.select("Вид связи", "Партнер");
    }

    @Step("Доходы и расходы поручителя")
    private void fillIncomeAndExpenses(RegistrationIncomeExpensesData data) {
        lookup.select("Уровень риска", "Низкий");
        buttons.clickTabByName("ДОПОЛНИТЕЛЬНАЯ ИНФОРМАЦИЯ");

        lookup
                .select("Клиент предоставил подтверждение дохода",
                        "Да")
                .select("Тип документа подтверждения дохода",
                        "Выписка с банковского счета");

        // Доход
        click.clickById(
                "BnzAppIncomesDetailAddRecordButtonButton-imageEl"
        );
        setFieldByMarker("TsiKind", data.getIncome().getType());
        menu.clickByName(data.getIncome().getType());
        setFieldByMarker("TsiFrequencyType", data.getIncome().getFrequency());
        menu.clickByName(data.getIncome().getFrequency());
        setFieldByMarker("TsiAmountBC", data.getIncome().getAmount());
        click.clickByMarker("save");

        // Расход
        click.clickById(
                "BnzAppExpensesDetailAddRecordButtonButton-imageEl"
        );
        checkbox.clickByInputId(
                "BnzAppExpensesDetailTsiKindComboBoxEdit-el"
        );
        menu.clickByName(data.getExpense().getType());
        checkbox.clickByInputId(
                "BnzAppExpensesDetailTsiFrequencyTypeComboBoxEdit-el"
        );
        menu.clickByName(data.getExpense().getFrequency());
        setFieldByMarker("TsiAmountBC", data.getExpense().getAmount());
        click.clickByMarker("save");
    }

    @Step("Скоринговые данные поручителя")
    private void fillScoringData() {
        lookup.select("Семейное положение", "Оиладор (зан)");
        lookup.selectByLabel("Количество иждивенцев (строка)", "3");
        lookup.selectByLabel("Количество детей",               "3");
        lookup.selectByLabel("Количество членов семьи",         "5");
        lookup.selectByLabel("Общий стаж",                     "36");
        lookup.selectByLabel("Общий стаж, лет",                "3");
        lookup.selectByLabel("Общий стаж, мес",                "36");
        lookup.selectByLabel("Стаж на последнем месте, лет",   "2");
        lookup.selectByLabel("Стаж на последнем месте, мес",   "24");
    }

    @Step("Причина отсутствия работы")
    private void fillOtherIncomeData() {
        lookup.select("Причина отсутствия работы", "Получатель Д/П");
    }

    @Step("Подтверждение скоринга поручителя")
    private void confirmScoring() {
        scroll.scrollToTop();
        buttons.clickTabByName("ОЦЕНКА ИНФОРМАЦИИ");

        lookup
                .select("Тип собственности на недвижимость", "Более 2-ух квартир")
                .select("Тип владения автомобилем",           "Есть автомобиль");

        checkbox.ensureCheckedBKI("IsConsentBKIProcessing");

        click.clickByName("Сохранить");
        click.clickByName("Закрыть");
    }

    private void setFieldByMarker(String marker, String value) {
        $x("//div[@data-item-marker='" + marker + "']/input")
                .setValue(value);
    }

    private void setDateField(String marker, String value) {
        $x("//*[@data-item-marker='" + marker + "']//input")
                .setValue(value);
    }
}