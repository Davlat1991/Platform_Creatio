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

    @Step("Click tab button contains: {name}")
    public ButtonsComponent clickTabByName(String name) {
        log.info("Click tab: '{}'", name);

        for (int i = 0; i < 3; i++) {
            try {
                SelenideElement btn = $x("//span[contains(text(),'" + name + "')]")
                        .shouldBe(visible)
                        .scrollIntoView(true);

                // Ждём исчезновения лоадеров
                $$(".ts-loader,.mask,.loading-indicator")
                        .filter(visible)
                        .shouldHave(CollectionCondition.size(0));

                executeJavaScript("arguments[0].click();", btn);

                btn.closest("li")
                        .shouldHave(Condition.cssClass("ts-tabpanel-active-item"),
                                Duration.ofSeconds(5));
                return this;
            } catch (Exception e) {
                log.warn("Tab click attempt {}/3 failed", i + 1);
            }
        }
        throw new RuntimeException("Failed to click tab: " + name);
    }
}