package com.creatio.autotest.flows;

import com.codeborne.selenide.Condition;
import com.creatio.autotest.core.base.BasePage;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;

public class ClientSearchFlow {

    private static final Logger log =
            LoggerFactory.getLogger(ClientSearchFlow.class);

    private final BasePage basePage = new BasePage();

    @Step("Поиск клиента: {lastName} {firstName} {middleName}")
    public void searchClient(String lastName,
                             String firstName,
                             String middleName) {
        log.info("Search client: {} {} {}", lastName, firstName, middleName);

        waitForSearchPanel();

        setField("Фамилия",  lastName);
        setField("Имя",      firstName);
        setField("Отчество", middleName);

        clickSearchButton();

        // Поиск в ЦФТ занимает 5-10 сек — ждём появления результата
        waitForSearchResult();

        // Выбрать конкретного клиента по ФИО среди результатов.
        // Поиск может вернуть несколько строк (однофамильцы); без явного
        // выбора старт консультации кликнул бы по первой строке в DOM —
        // не обязательно нашему клиенту (это открывало мини-карточку чужого
        // контакта и перекрывало «Оформить заявку»).
        selectClientRow(lastName, firstName, middleName);

        log.info("Client search completed");
    }

    // ══════════════════════════════════════
    // PRIVATE
    // ══════════════════════════════════════

    private void waitForSearchPanel() {
        $x("//input[@placeholder='Фамилия']")
                .shouldBe(Condition.visible, Duration.ofSeconds(15));
    }

    private void setField(String placeholder, String value) {
        if (value == null || value.trim().isEmpty()) return;
        log.debug("Set search field: '{}' = '{}'", placeholder, value);
        $x("//input[@placeholder='" + placeholder + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(20))
                .setValue(value);
    }

    private void clickSearchButton() {
        log.info("Click search button");

        // Скоупим внутри SearchButtonContainer — исключаем дубликаты SearchButton на странице
        $x("//div[@data-item-marker='SearchButtonContainer']" +
                "//span[@data-item-marker='SearchButton']")
                .shouldBe(Condition.visible, Duration.ofSeconds(20))
                .click();
    }

    private void waitForSearchResult() {
        log.info("Waiting for search result from CFT...");

        // Ждём появления кнопки "Начать консультацию" — это сигнал что клиент найден
        $x("//*[@data-item-marker='Начать консультацию']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30));

        log.info("Search result received");
    }

    /**
     * Выбирает строку клиента по ФИО среди результатов поиска.
     * Строка результата имеет data-item-marker = «Фамилия Имя Отчество».
     * Клик по строке делает её активной (grid-row-selected), после чего
     * старт консультации гарантированно относится к этому клиенту.
     */
    private void selectClientRow(String lastName, String firstName, String middleName) {
        String fullName = (lastName + " " + firstName + " " + middleName).trim();
        log.info("Select client row by full name: '{}'", fullName);

        $x("//div[contains(@class,'grid-row')" +
                " and @data-item-marker='" + fullName + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(15))
                .click();

        log.info("Client row selected: '{}'", fullName);
    }
}