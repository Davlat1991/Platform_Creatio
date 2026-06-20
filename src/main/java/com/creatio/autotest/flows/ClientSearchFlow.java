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
                .shouldBe(Condition.visible, Duration.ofSeconds(10))
                .setValue(value);
    }

    private void clickSearchButton() {
        log.info("Click search button");

        // Скоупим внутри SearchButtonContainer — исключаем дубликаты SearchButton на странице
        $x("//div[@data-item-marker='SearchButtonContainer']" +
                "//span[@data-item-marker='SearchButton']")
                .shouldBe(Condition.visible, Duration.ofSeconds(10))
                .click();
    }

    private void waitForSearchResult() {
        log.info("Waiting for search result from CFT...");

        // Ждём появления кнопки "Начать консультацию" — это сигнал что клиент найден
        $x("//*[@data-item-marker='Начать консультацию']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30));

        log.info("Search result received");
    }
}