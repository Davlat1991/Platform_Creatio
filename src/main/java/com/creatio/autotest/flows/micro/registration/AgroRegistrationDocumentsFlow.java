package com.creatio.autotest.flows.micro.registration;

import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.testdata.models.BorrowerDocumentData;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Флоу «Документы» стадии «Оформление» (анкета заёмщика) для продуктов
 * сегмента «Микро».
 * <p>
 * Самостоятельный класс в ветке {@code flows.micro.registration},
 * изолированный от розничного {@code RegistrationDocumentsFlow}. Логика
 * заполнения на текущем этапе идентична рознице (подтверждено), но данные
 * вынесены в {@link BorrowerDocumentData} — без хардкода. Класс развивается
 * независимо от розницы.
 */
public class AgroRegistrationDocumentsFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroRegistrationDocumentsFlow.class);

    private final LookupComponent lookup   = new LookupComponent();
    private final BasePage        basePage = new BasePage();
    private final ClickService    click    = new ClickService();

    @Step("Заполнение документов заемщика (Агро/Микро)")
    public void fillBorrowerDocuments(BorrowerDocumentData data) {
        log.info("Fill borrower documents (Agro/Micro): country='{}' type='{}'",
                data.getBirthCountry(), data.getDocumentType());

        lookup
                .select("Страна рождения",     data.getBirthCountry())
                .selectByLabel("Дата выдачи",      data.getIssueDate())
                .selectByLabel("Действителен до",  data.getValidUntil());

        click.doubleClickByMarker(data.getDocumentType());

        basePage.checkPage("RegDocumentPageV2Container");

        lookup
                .selectByLabel("Дата выдачи",   data.getIssueDate())
                .selectByLabel("Действует до",  data.getValidUntil());

        click.clickByName("Сохранить");
        basePage.checkPage("EntityLoaded");
    }
}