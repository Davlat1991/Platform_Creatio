package com.creatio.autotest.pages;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BasePage;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.*;

public class DetailPage extends BasePage {

    // ══════════════════════════════════════
    // OPEN DETAIL
    // ══════════════════════════════════════

    @Step("Open detail: {detailName}")
    public DetailPage openDetail(String detailName) {
        log.info("Open detail: '{}'", detailName);

        SelenideElement container = $x("//span[.='" + detailName + "']/../..");

        if (container.getAttribute("class").contains("collapsed")) {
            $x("//span[.='" + detailName + "']").click();
        }
        return this;
    }

    @Step("Open detail stable: {name}")
    public DetailPage openDetailStable(String name) {
        log.info("Open detail stable: '{}'", name);

        // Берём только видимый span
        SelenideElement detail = $$x("//span[normalize-space()='" + name + "']")
                .filter(Condition.visible)
                .first();

        detail.shouldBe(Condition.visible, Duration.ofSeconds(30))
                .scrollIntoView(true)
                .hover()
                .click();

        $$("input[data-item-marker='AddRecordButton']")
                .shouldBe(CollectionCondition.sizeGreaterThan(0));

        return this;
    }

    // ══════════════════════════════════════
    // ADD RECORD
    // ══════════════════════════════════════

    @Step("Click add record in detail: {detailName}")
    public DetailPage clickAddRecord(String detailName) {
        log.info("Click add record in detail: '{}'", detailName);

        SelenideElement detail = $x(
                "//div[contains(@class,'detail')" +
                        " and @data-item-marker='" + detailName + "']"
        ).shouldBe(Condition.visible).scrollIntoView(true);

        SelenideElement addBtn = detail.$x(
                ".//span[@data-item-marker='AddRecordButton']"
        ).shouldBe(Condition.visible, Condition.enabled);

        executeJavaScript("arguments[0].click();", addBtn);
        return this;
    }

    // ══════════════════════════════════════
    // OPEN DETAIL MENU
    // ══════════════════════════════════════

    @Step("Open detail menu: {detailName}")
    public DetailPage openDetailMenu(String detailName) {
        log.info("Open detail menu: '{}'", detailName);

        SelenideElement detail = $x(
                "//*[@data-item-marker='" + detailName + "']"
        ).shouldBe(Condition.visible);

        SelenideElement menuBtn = detail.$x(
                ".//span[contains(@class,'detail-menu') or @data-item-marker='detail-menu']"
        ).shouldBe(Condition.visible);

        executeJavaScript("arguments[0].click();", menuBtn);
        return this;
    }

    // ══════════════════════════════════════
    // VALIDATE
    // ══════════════════════════════════════

    @Step("Assert detail opened: {detailName}")
    public DetailPage assertDetailOpened(String detailName) {
        $x("//span[normalize-space()='" + detailName + "']/../..")
                .shouldNotHave(Condition.attributeMatching("class", ".*collapsed.*"));
        return this;
    }
}