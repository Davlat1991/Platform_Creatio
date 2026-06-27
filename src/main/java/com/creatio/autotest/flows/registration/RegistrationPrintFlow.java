package com.creatio.autotest.flows.registration;

import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.MenuComponent;
import com.creatio.autotest.components.MessageBoxComponent;
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

    @Step("Сохранение и печать анкеты-заявки")
    public void printApplication() {
        log.info("Print application");

        click.clickByName("Сохранить");
        click.clickByName("Закрыть");

        // диалог смены типа занятости (может появиться)
        msgBox.confirmIfPresent("Да","При изменении типа занятости");
        buttons.clickTabByName("Участники заявки");
        click.clickByName("Заемщик");

        detailPage.openDetailMenu("Участники заявки");
        menu.clickByName("Печать Анкеты-заявки");
        // Ждём завершения скачивания — файл не должен иметь расширение .crdownload
        waitForDownload();
    }


    private void waitForDownload() {
        log.info("Waiting for download to complete...");

        String downloadPath = System.getProperty("user.dir") + "\\downloads";
        java.io.File downloadDir = new java.io.File(downloadPath);

        // Ждём до 30 секунд пока .crdownload файл исчезнет
        for (int i = 0; i < 30; i++) {
            java.io.File[] files = downloadDir.listFiles(
                    f -> f.getName().endsWith(".crdownload")
            );

            if (files == null || files.length == 0) {
                log.info("Download completed");
                return;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        log.warn("Download may not have completed within 30 seconds");
    }
}