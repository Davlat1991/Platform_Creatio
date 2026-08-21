package com.creatio.autotest.flows.retail.participants;

import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.testdata.models.ParticipantData;
import com.creatio.autotest.testdata.models.RegistrationIncomeExpensesData;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.$x;

public class PledgerFlow {

    private static final Logger log = LoggerFactory.getLogger(PledgerFlow.class);

    private final LookupComponent   lookup   = new LookupComponent();
    private final ButtonsComponent  buttons  = new ButtonsComponent();
    private final BasePage          basePage = new BasePage();
    private final ClickService      click    = new ClickService();

    @Step("Заполнение анкеты залогодателя")
    public void fill(ParticipantData participant,
                     RegistrationIncomeExpensesData incomeData) {
        log.info("Fill pledger questionnaire");

        // Средства связи
        lookup.selectByMarker("BnzAffiliation", "Мобильный");

        // Регистрация
        buttons.doubleClickByName("Регистрация");
        setDateField("BnzRegistrationDate", "01.01.2020");
        click.clickByName("Сохранить");
        basePage.waitForPage();

        // Вид связи
        lookup.select("Вид связи", "Партнер");

        // Национальность
        lookup.select("Национальность", "Таджик / таджичка");

        // Дополнительная информация
        buttons.clickTabByName("ДОПОЛНИТЕЛЬНАЯ ИНФОРМАЦИЯ");

        lookup.select("Семейное положение", "Оиладор (зан)");
        lookup.select("Уровень риска",      "Низкий");

        click.clickByName("Сохранить");
        click.clickByName("Закрыть");
    }

    private void setDateField(String marker, String value) {
        $x("//*[@data-item-marker='" + marker + "']//input")
                .setValue(value);
    }
}