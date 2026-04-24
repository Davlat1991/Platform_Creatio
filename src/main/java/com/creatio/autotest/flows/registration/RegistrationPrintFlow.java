package com.creatio.autotest.flows.registration;

import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.MenuComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.pages.DetailPage;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistrationPrintFlow {

    private static final Logger log =
            LoggerFactory.getLogger(RegistrationPrintFlow.class);

    private final ButtonsComponent buttons    = new ButtonsComponent();
    private final MenuComponent    menu       = new MenuComponent();
    private final BasePage         basePage   = new BasePage();
    private final DetailPage       detailPage = new DetailPage();
    private final ClickService click = new ClickService();

    @Step("Сохранение и печать анкеты-заявки")
    public void printApplication() {
        log.info("Print application");

        click.clickByName("Сохранить");
        click.clickByName("Закрыть");

        buttons.clickTabByName("Участники заявки");
        click.clickByName("Заемщик");

        detailPage.openDetailMenu("Участники заявки");
        menu.clickByName("Печать Анкеты-заявки");
    }
}