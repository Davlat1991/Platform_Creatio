package com.creatio.autotest.components;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BaseComponent;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class CheckboxComponent extends BaseComponent {

    // ══════════════════════════════════════
    // BY MARKER
    // ══════════════════════════════════════

    @Step("Ensure checkbox checked: {marker}")
    public CheckboxComponent ensureChecked(String marker) {
        log.info("Ensure checkbox checked: '{}'", marker);

        SelenideElement wrap = $x(
                "//span[contains(@class,'t-checkboxedit-wrap')" +
                        " and @data-item-marker='" + marker + "']"
        ).shouldBe(visible);

        Boolean checked = Selenide.executeJavaScript(
                "return arguments[0].classList.contains('t-checkboxedit-checked');",
                wrap
        );

        if (!Boolean.TRUE.equals(checked)) {
            wrap.scrollIntoView(true);
            executeJavaScript("arguments[0].click();", wrap);
            log.info("Checkbox '{}' checked", marker);
        } else {
            log.info("Checkbox '{}' already checked", marker);
        }

        return this;
    }

    @Step("Ensure checkbox BKI checked: {marker}")
    public CheckboxComponent ensureCheckedBKI(String marker) {
        log.info("Ensure BKI checkbox checked: '{}'", marker);

        SelenideElement wrap = $x(
                "//*[@data-item-marker='" + marker + "']" +
                        "[contains(@class,'t-checkboxedit-wrap')]"
        ).shouldBe(visible);

        boolean checked = wrap.has(Condition.cssClass("t-checkboxedit-checked"));

        if (!checked) {
            wrap.scrollIntoView(true).click();
            wrap.shouldHave(Condition.cssClass("t-checkboxedit-checked"));
            log.info("BKI Checkbox '{}' checked", marker);
        } else {
            log.info("BKI Checkbox '{}' already checked", marker);
        }

        return this;
    }



}