package com.creatio.autotest.flows.retail.registration;

import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.MenuComponent;
import com.creatio.autotest.components.MessageBoxComponent;
import com.creatio.autotest.components.PrintComponent;
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
    private final MessageBoxComponent msgBox = new MessageBoxComponent();
    private final PrintComponent   print      = new PrintComponent();

    @Step("Сохранение и печать анкеты-заявки")
    public void printApplication() {
        log.info("Print application");

        click.clickByName("Сохранить");
        // диалог смены типа занятости (может появиться)
        msgBox.confirmIfPresent("Да", "При изменении типа занятости");
        click.clickByName("Закрыть");

        // диалог смены типа занятости (может появиться)
        msgBox.confirmIfPresent("Да","При изменении типа занятости");
        buttons.clickTabByName("Участники заявки");
        click.clickByName("Заемщик");

        detailPage.openDetailMenu("Участники заявки");
        menu.clickByName("Печать Анкеты-заявки");

        // Ждём завершения скачивания (общий метод PrintComponent)
        print.waitForDownload();
    }


}