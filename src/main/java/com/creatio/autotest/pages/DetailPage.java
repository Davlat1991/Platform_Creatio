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

        // Находим заголовок детали по тексту
        // затем поднимаемся к контейнеру и ищем кнопку ToolsButton
        SelenideElement menuBtn = $x(
                "//span[normalize-space()='" + detailName + "']" +
                        "/ancestor::div[contains(@class,'ts-controlgroup')]" +
                        "//span[@data-item-marker='ToolsButton']"
        ).shouldBe(Condition.visible, Duration.ofSeconds(30));

        executeJavaScript("arguments[0].click();", menuBtn);

        log.info("Detail menu opened: '{}'", detailName);
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

    @Step("Detail has record: {detailName}")
    public boolean detailHasRecord(String detailName) {
        log.info("Check detail has record: '{}'", detailName);

        // Контейнер детали должен быть видим (значит мы на нужной вкладке)
        $x("//div[contains(@class,'detail') and @data-item-marker='" + detailName + "']")
                .shouldBe(Condition.visible, java.time.Duration.ofSeconds(15));

        // Есть запись = есть хотя бы одна видимая строка грида (grid-listed-row).
        // Пустая grid-detail строк не содержит (показывает «Нет данных»).
        boolean exists = $$x(
                "//div[contains(@class,'detail') and @data-item-marker='" + detailName + "']" +
                        "//div[contains(@class,'grid-listed-row')]"
        ).filter(Condition.visible).size() > 0;

        log.info("Detail '{}' has record: {}", detailName, exists);
        return exists;
    }
}