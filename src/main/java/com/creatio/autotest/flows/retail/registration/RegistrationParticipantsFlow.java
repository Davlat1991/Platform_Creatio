package com.creatio.autotest.flows.retail.registration;

import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.components.MenuComponent;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.pages.DetailPage;
import com.creatio.autotest.testdata.models.registration.CareerData;
import com.creatio.autotest.testdata.models.registration.fields.CareerField;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistrationParticipantsFlow {

    private static final Logger log =
            LoggerFactory.getLogger(RegistrationParticipantsFlow.class);

    private final ButtonsComponent buttons = new ButtonsComponent();
    private final MenuComponent    menu    = new MenuComponent();
    private final ClickService     click   = new ClickService();

    private final LookupComponent lookup     = new LookupComponent();
    private final DetailPage      detailPage = new DetailPage();



    @Step("Добавление заемщика")
    public void addBorrower() {
        log.info("Add borrower");

        buttons.clickTabByName("Участники заявки");
        click.clickById("BnzVwFinApplicationAllParticipantDetailAddTypedRecordButtonButton-imageEl");
        menu.clickByName("Заемщик");
        //menu.doubleclickButtonByName("Заемщик"); //Метод для продолжения заявки
    }


    @Step("Заполнение карьеры заемщика (Работает в организации)")
    public void fillCareerDetailsEmployee(CareerData career) {
        log.info("Fill career details (Employee)");

        buttons.clickByNameContains("ДОПОЛНИТЕЛЬНАЯ ИНФОРМАЦИЯ");
        detailPage.clickAddRecord("Карьера");

        lookup.selectByMarkerWithSoftValidation(
                CareerField.EMPLOYMENT_TYPE.marker, career.getEmploymentType());
        lookup.setTextByMarker(
                CareerField.ORGANIZATION_NAME.marker, career.getOrganizationName());
        lookup.selectByMarkerWithSoftValidation(
                CareerField.POSITION.marker, career.getPosition());
        lookup.setTextByMarker(
                CareerField.START_DATE.marker, career.getStartDate());
        lookup.selectByMarkerWithSoftValidation(
                CareerField.EMPLOYER.marker, career.getEmployer());
        lookup.setTextByMarker(
                CareerField.POSITION_FULL_NAME.marker, career.getPositionFullName());
        lookup.setTextByMarker(
                CareerField.DUE_DATE.marker, career.getDueDate());

        click.clickByName("Сохранить");
    }


}