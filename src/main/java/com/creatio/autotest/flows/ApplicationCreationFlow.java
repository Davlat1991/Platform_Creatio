package com.creatio.autotest.flows;

import com.creatio.autotest.components.MessageBoxComponent;
import com.creatio.autotest.components.MenuComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.$x;

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
        log.info("Create application: day='{}' number='{}'" +
                        " type='{}' term='{}'",
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
        $x("//label[normalize-space()=" +
                "'Запрашиваемая дата погашения']/../..//input")
                .setValue(day);
    }

    @Step("Добавить строку графика")
    private void addScheduleRow() {
        click.clickById(
                "KzParameterScheduleDetailAddRecordButtonButton-imageEl"
        );
    }

    @Step("Заполнить детали графика")
    private void fillScheduleDetails(String number,
                                     String type,
                                     String term) {
        setFieldByMarker("KzNumber", number);

        // Тип графика через lookup
        setFieldByMarker("KzTypeScheduler", type);
        menu.clickByName(type);

        setFieldByMarker("KzTermMonth", term);
        click.clickByMarker("save");
    }

    @Step("Рассчитать и создать заявку")
    private void calculateAndCreate() {
        click.clickByName("Рассчитать");
        click.clickByName("Создать заявку");
    }

    @Step("Подтвердить отсутствие задолженности")
    private void confirmNoDebt() {
        msgBox.shouldSeeModal("Нет задолженности!");
        click.clickByMarker("ОК");
    }

    // ══════════════════════════════════════
    // HELPER
    // ══════════════════════════════════════

    private void setFieldByMarker(String marker, String value) {
        $x("//div[@data-item-marker='" + marker + "']/input")
                .setValue(value);
    }
}