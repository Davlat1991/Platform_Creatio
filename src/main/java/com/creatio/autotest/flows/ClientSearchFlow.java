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

        // Ждём загрузки правой панели
        waitForSearchPanel();

        // Заполняем поля
        setField("Фамилия",  lastName);
        setField("Имя",      firstName);
        setField("Отчество", middleName);

        // Нажимаем ПОИСК
        clickSearchButton();

        basePage.waitForPage();
        log.info("Client search submitted");
    }

    // ══════════════════════════════════════
    // PRIVATE
    // ══════════════════════════════════════

    private void waitForSearchPanel() {
        $x("//input[@placeholder='Фамилия']")
                .shouldBe(Condition.exist, Duration.ofSeconds(15));
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

        // Из скриншота — кнопка ПОИСК зелёная
        $x("//*[@data-item-marker='SearchButton']" +
                " | //a[normalize-space()='ПОИСК']" +
                " | //span[normalize-space()='ПОИСК']")
                .shouldBe(Condition.visible, Duration.ofSeconds(10))
                .click();
    }
}