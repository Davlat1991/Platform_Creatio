package com.creatio.autotest.flows.participants;

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

public class ParticipantsAddFlow {

    private static final Logger log =
            LoggerFactory.getLogger(ParticipantsAddFlow.class);

    private final LookupComponent lookup   = new LookupComponent();
    private final BasePage        basePage = new BasePage();
    private final ClickService    click    = new ClickService();
    private final ButtonsComponent buttons      = new ButtonsComponent();

    @Step("Добавление участника: {participant.role}")
    public void addParticipant(ParticipantData participant) {
        log.info("Add participant: role='{}'",
                participant.getRole().getUiValue());

        // 1. Клик по кнопке "+" — открывает первый дропдаун
        clickAddButton();

        // 2. Выбор "Физ. лицо" из первого дропдауна
        selectPhysicalPerson();

        // 3. Выбор роли из второго дропдауна (Поручитель / Залогодатель)
        selectParticipantRole(participant.getRole().getUiValue());

        // 4. Поиск по ФИО в модальном окне
        lookup.setModalSearchField("Surname",    participant.getLastName());
        lookup.setModalSearchField("GivenName",  participant.getFirstName());
        lookup.setModalSearchField("MiddleName", participant.getMiddleName());
        buttons.searchParticipantByName("SearchButton");
        click.clickByName("Выбрать");
        basePage.waitForPage();

        log.info("Participant added: '{}'", participant.getRole().getUiValue());
    }

    // ══════════════════════════════════════
    // PRIVATE
    // ══════════════════════════════════════

    private void clickAddButton() {
        log.debug("Click add participant button");

        // imageEl — левая часть кнопки, открывает первый дропдаун
        executeJavaScript(
                "arguments[0].click();",
                $x("//*[@id='BnzVwFinApplicationAllParticipantDetail" +
                        "AddTypedRecordButtonButton-imageEl']")
                        .shouldBe(Condition.visible, Duration.ofSeconds(60))
        );

        // Ждём появления первого дропдауна
        $x("//ul[@data-item-marker='AddTypedRecordButton']")
                .shouldBe(Condition.visible, Duration.ofSeconds(60));

        log.debug("Add button clicked, first dropdown visible");
    }

    private void selectPhysicalPerson() {
        log.debug("Hover 'Физ. лицо' to open submenu");

        // Hover на "Физ. лицо" — открывает подменю с ролями
        $x("//ul[@data-item-marker='AddTypedRecordButton']" +
                "//li[@data-tag='addContactRecord']")
                .shouldBe(Condition.visible, Duration.ofSeconds(60))
                .hover();

        // Ждём появления подменю с ролями
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