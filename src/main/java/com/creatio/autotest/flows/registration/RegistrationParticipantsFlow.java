package com.creatio.autotest.flows.registration;

import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.MenuComponent;
import com.creatio.autotest.core.services.ClickService;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.$x;

public class RegistrationParticipantsFlow {

    private static final Logger log =
            LoggerFactory.getLogger(RegistrationParticipantsFlow.class);

    private final ButtonsComponent buttons = new ButtonsComponent();
    private final MenuComponent    menu    = new MenuComponent();
    private final ClickService     click   = new ClickService();

    @Step("Добавление заемщика")
    public void addBorrower() {
        log.info("Add borrower");

        buttons.clickTabByName("Участники заявки");
        //click.clickById("BnzVwFinApplicationAllParticipantDetailAddTypedRecordButtonButton-imageEl");
        //menu.clickByName("Заемщик");
        menu.doubleclickButtonByName("Заемщик"); //Метод для продолжения заявки
    }


}