package com.creatio.autotest.components;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BaseComponent;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class ButtonsComponent extends BaseComponent {

    @Step("Click button by name: {name}")
    public ButtonsComponent clickByName(String name) {
        log.info("Click button: '{}'", name);
        SelenideElement btn = $x("//span[normalize-space(.)='" + name + "']")
                .shouldBe(visible, enabled);
        executeJavaScript("arguments[0].click();", btn);
        return this;
    }

    @Step("Click button contains name: {name}")
    public ButtonsComponent clickByNameContains(String name) {
        log.info("Click button contains: '{}'", name);
        SelenideElement btn = $x("//span[contains(text(),'" + name + "')]")
                .shouldBe(visible, enabled);
        executeJavaScript("arguments[0].click();", btn);
        return this;
    }

    @Step("Click button by marker: {marker}")
    public ButtonsComponent clickByMarker(String marker) {
        log.info("Click button marker: '{}'", marker);
        SelenideElement btn = $x("//span[@data-item-marker='" + marker + "']")
                .shouldBe(visible, enabled);
        executeJavaScript("arguments[0].click();", btn);
        return this;
    }

    @Step("Click button by id: {id}")
    public ButtonsComponent clickById(String id) {
        log.info("Click button id: '{}'", id);
        SelenideElement btn = $x("//span[@id='" + id + "']")
                .shouldBe(visible, enabled);
        executeJavaScript("arguments[0].click();", btn);
        return this;
    }

    @Step("Double click button by name: {name}")
    public ButtonsComponent doubleClickByName(String name) {
        log.info("Double click button: '{}'", name);
        $x("//span[normalize-space(.)='" + name + "']")
                .shouldBe(visible, enabled)
                .doubleClick();
        return this;
    }


    @Step("Click tab by name: {value}")
    public ButtonsComponent clickTabByName(String value) {
        log.info("Click tab: '{}'", value);

        String liXpath =
                "//ul[contains(@class,'ts-tabpanel-items')]" +
                        "/li[.//span[contains(normalize-space(),'" + value + "')]]";

        for (int i = 0; i < 4; i++) {
            try {
                // Именно ВИДИМАЯ вкладка (в DOM может быть несколько таб-баров)
                SelenideElement li = $$x(liXpath)
                        .filter(visible)
                        .first()
                        .shouldBe(visible, Duration.ofSeconds(15));

                // Горизонтальное центрирование, без вертикального прыжка
                executeJavaScript(
                        "arguments[0].scrollIntoView({block:'nearest', inline:'center'});", li);

                // ВСЕГДА кликаем (клик по активной вкладке идемпотентен).
                // НЕ выходим по 'уже активна': после сохранения деталей система
                // авто-переключает на «Основную информацию», и устаревший класс
                // active на этой вкладке ложно срабатывал бы как short-circuit,
                // оставляя нас на чужой вкладке.
                if (i < 2) {
                    executeJavaScript("arguments[0].click();", li);
                } else {
                    li.click();
                }

                li.shouldHave(Condition.cssClass("ts-tabpanel-active-item"),
                        Duration.ofSeconds(5));
                log.info("Tab '{}' activated", value);
                return this;
            } catch (Exception e) {
                log.warn("Tab click attempt {}/4 failed for '{}'", i + 1, value);
            }
        }
        throw new RuntimeException("Failed to click tab: " + value);
    }


    public ButtonsComponent clickButtonByContainName(String Value) {
        SelenideElement element = $x("//span[contains(text(), '" + Value + "')]")
                .shouldBe(Condition.visible)
                .shouldBe(Condition.enabled)
                .shouldHave(Condition.text(Value));

        element.hover();                    // помогает "активировать" элемент
        element.shouldBe(Condition.interactable); // теперь interactable безопасен

        element.click();

        return this;
    }


    @Step("Поиск участника по ФИО: {fullName}")
    public void searchParticipantByName(String fullName) {
        log.info("Search participant: '{}'", fullName);

        // Ждём модал
        $x("//div[contains(@class,'bnz-search-questionnaire-container')]")
                .shouldBe(visible, Duration.ofSeconds(15));

        // Берём SearchButton ТОЛЬКО внутри модала участника
        SelenideElement searchBtn = $x(
                "//div[@id='BnzSearchContactModalPageCardContentContainerContainer']" +
                        "//span[@data-item-marker='SearchButton']"
        ).shouldBe(visible, Duration.ofSeconds(15))
                .shouldBe(enabled, Duration.ofSeconds(5));

        executeJavaScript("arguments[0].click();", searchBtn);

        try {
            $x("//span[contains(@class,'grid-label') and normalize-space(.)='ФИО']")
                    .shouldBe(visible, Duration.ofSeconds(15));
        } catch (Throwable e) {
            log.warn("Search result not appeared, retrying click...");
            executeJavaScript("arguments[0].click();", searchBtn);
            $x("//span[contains(@class,'grid-label') and normalize-space(.)='ФИО']")
                    .shouldBe(visible, Duration.ofSeconds(15));
        }

        log.info("Search results appeared");
    }
}