package com.creatio.autotest.components;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BaseComponent;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class FiltersComponent extends BaseComponent {

    @Step("Удалить фильтр если есть")
    public boolean removeFilterIfExists() {
        log.info("Remove filter if exists");

        try {
            SelenideElement container = $x(
                    "//*[contains(@id,'QuickFilterModuleV2')]"
            ).shouldBe(Condition.visible, Duration.ofSeconds(10));

            ElementsCollection xBtns = container.$$x(
                    ".//*[contains(@class,'filter-remove-button')]"
            );

            if (xBtns.isEmpty()) {
                log.info("No filter to remove");
                return false;
            }

            SelenideElement xBtn = xBtns.first()
                    .shouldBe(Condition.visible, Condition.enabled);

            try {
                xBtn.click();
            } catch (Exception e) {
                executeJavaScript("arguments[0].click();", xBtn);
            }

            xBtn.should(Condition.disappear, Duration.ofSeconds(5));
            log.info("Filter removed");
            return true;

        } catch (Exception e) {
            log.debug("Filter not found: {}", e.getMessage());
            return false;
        }
    }

    @Step("Применить значение '{value}' в фильтр '{fieldMarker}'")
    public FiltersComponent applyFilter(String fieldMarker,
                                        String applyMarker,
                                        String value) {
        log.info("Apply filter: marker='{}' value='{}'", fieldMarker, value);

        SelenideElement input = $x(
                "//*[@data-item-marker='" + fieldMarker + "']//input"
        ).shouldBe(Condition.visible, Condition.enabled);

        input.clear();
        input.setValue(value);
        input.shouldHave(Condition.value(value));

        $x("//*[@data-item-marker='" + applyMarker + "']")
                .shouldBe(Condition.visible, Condition.enabled)
                .click();

        return this;
    }
}