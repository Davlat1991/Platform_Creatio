package com.creatio.autotest.flows;

import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.core.services.WaitService;
import com.creatio.autotest.pages.FieldPage;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientSearchFlow {

    private static final Logger log = LoggerFactory.getLogger(ClientSearchFlow.class);

    private final FieldPage fieldPage = new FieldPage();
    private final BasePage  basePage  = new BasePage();
    private final ClickService click = new ClickService();
    private final WaitService wait  = new WaitService();
    private final FieldPage    field = new FieldPage();

    // ══════════════════════════════════════
    // SEARCH
    // ══════════════════════════════════════

    @Step("Поиск клиента: {lastName} {firstName} {middleName}")
    public void searchClient(String lastName,
                             String firstName,
                             String middleName) {
        log.info("Search client: {} {} {}", lastName, firstName, middleName);

        fieldPage
                .setFieldByPlaceholder("Фамилия", lastName)
                .setFieldByPlaceholder("Имя", firstName)
                .setFieldByPlaceholder("Отчество", middleName);

        click.clickByName("Поиск");

        log.info("Client search submitted");
    }
}