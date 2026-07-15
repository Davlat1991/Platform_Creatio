package com.creatio.autotest.flows.micro.participants;

import com.codeborne.selenide.Condition;
import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.testdata.models.ParticipantData;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

/**
 * Добавление участника заявки (Агро/Микро): «+» → «Физ. лицо» → роль
 * (Поручитель/Залогодатель) → поиск контакта по ФИО → «Выбрать».
 * <p>
 * Изолированная Микро-копия розничного {@code ParticipantsAddFlow}: механика
 * идентична. Модель {@link ParticipantData} нейтральна (роль + ФИО) и
 * переиспользуется. Общие компоненты — из Core.
 */
public class AgroParticipantsAddFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroParticipantsAddFlow.class);

    private final LookupComponent  lookup   = new LookupComponent();
    private final BasePage         basePage = new BasePage();
    private final ClickService     click    = new ClickService();
    private final ButtonsComponent buttons  = new ButtonsComponent();

    @Step("Агро: добавление участника — {participant.role}")
    public void addParticipant(ParticipantData participant) {
        log.info("Agro: add participant role='{}'", participant.getRole().getUiValue());

        clickAddButton();
        selectPhysicalPerson();
        selectParticipantRole(participant.getRole().getUiValue());

        lookup.setModalSearchField("Surname",    participant.getLastName());
        lookup.setModalSearchField("GivenName",  participant.getFirstName());
        lookup.setModalSearchField("MiddleName", participant.getMiddleName());
        buttons.searchParticipantByName("SearchButton");
        click.clickByName("Выбрать");
        basePage.waitForPage();

        log.info("Agro: participant added '{}'", participant.getRole().getUiValue());
    }

    // ══════════════════════════════════════
    // PRIVATE
    // ══════════════════════════════════════

    private void clickAddButton() {
        log.debug("Click add participant button");

        executeJavaScript(
                "arguments[0].click();",
                $x("//*[@id='BnzVwFinApplicationAllParticipantDetail" +
                        "AddTypedRecordButtonButton-imageEl']")
                        .shouldBe(Condition.visible, Duration.ofSeconds(60))
        );

        $x("//ul[@data-item-marker='AddTypedRecordButton']")
                .shouldBe(Condition.visible, Duration.ofSeconds(60));

        log.debug("Add button clicked, first dropdown visible");
    }

    private void selectPhysicalPerson() {
        log.debug("Hover 'Физ. лицо' to open submenu");

        $x("//ul[@data-item-marker='AddTypedRecordButton']" +
                "//li[@data-tag='addContactRecord']")
                .shouldBe(Condition.visible, Duration.ofSeconds(60))
                .hover();

        $x("//li[normalize-space()='Поручитель']")
                .shouldBe(Condition.visible, Duration.ofSeconds(60));

        log.debug("Submenu with roles visible");
    }

    private void selectParticipantRole(String roleName) {
        log.debug("Select participant role: '{}'", roleName);

        $x("//li[normalize-space()='" + roleName + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(60))
                .click();

        log.debug("Role selected: '{}'", roleName);
    }
}