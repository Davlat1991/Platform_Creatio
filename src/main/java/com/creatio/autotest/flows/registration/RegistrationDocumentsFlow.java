package com.creatio.autotest.flows.registration;

import com.creatio.autotest.components.GridComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.pages.FieldPage;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistrationDocumentsFlow {

    private static final Logger log =
            LoggerFactory.getLogger(RegistrationDocumentsFlow.class);

    private final LookupComponent lookup   = new LookupComponent();
    private final GridComponent   grid     = new GridComponent();
    private final BasePage        basePage = new BasePage();
    private final ClickService click = new ClickService();
    private final FieldPage fieldPage = new FieldPage();



    @Step("Заполнение документов заемщика")
    public void fillBorrowerDocuments() {
        log.info("Fill borrower documents");

        lookup
                .select("Страна рождения", "Точикистон")
                .selectByLabel("Дата выдачи",      "01.01.2020")
                .selectByLabel("Действителен до",  "01.01.2030");

        click.doubleClickByMarker("Шиносномаи ЧТ");

        basePage.checkPage("RegDocumentPageV2Container");

        lookup
                .selectByLabel("Дата выдачи",   "01.01.2020")
                .selectByLabel("Действует до",  "01.01.2030");

        click.clickByName("Сохранить");
        basePage.checkPage("EntityLoaded");
    }
}