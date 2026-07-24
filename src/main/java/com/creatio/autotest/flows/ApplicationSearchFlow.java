package com.creatio.autotest.flows;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.components.GridComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.components.MenuComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class ApplicationSearchFlow {

    private static final Logger log =
            LoggerFactory.getLogger(ApplicationSearchFlow.class);

    private final BasePage        basePage  = new BasePage();
    private final LookupComponent lookup    = new LookupComponent();
    private final MenuComponent   menu      = new MenuComponent();
    private final GridComponent   grid      = new GridComponent();
    private final ClickService    click     = new ClickService();

    // Сохранённый номер заявки — передаётся снаружи
    private String savedApplicationNumber;

    public ApplicationSearchFlow withApplicationNumber(String number) {
        this.savedApplicationNumber = number;
        return this;
    }

    // ══════════════════════════════════════
    // OPEN BY NUMBER
    // ══════════════════════════════════════

    @Step("Открыть заявку по номеру: {savedApplicationNumber}")
    public void openByNumber() {
        log.info("Open application by number: '{}'",
                savedApplicationNumber);

        click.clickByMarker("Заявки");

        removeFilterIfExists();

        click.clickByName("Фильтры/группы");
        menu.clickByName("Добавить условие");

        lookup.selectByMarker("columnEdit", "Номер");

        applyNumberFilter();

        grid.clickFirstRowContaining(
                "grid-FinApplicationSectionDataGridGrid-wrap",
                savedApplicationNumber
        );

        click.clickByMarker("Открыть");
    }


    // ══════════════════════════════════════
    // OPEN BY CONTRACT NUMBER
    // ══════════════════════════════════════


    @Step("Открыть договор по номеру: {savedApplicationNumber}")
    public void openContractByNumber() {
        log.info("Open contract by number: '{}'",
                savedApplicationNumber);

        click.clickByMarker("Договоры");

        removeFilterIfExists();

        click.clickByName("Фильтры/группы");
        menu.clickByName("Добавить условие");

        lookup.selectByMarker("columnEdit", "Номер");

        applyNumberFilter();

        grid.clickFirstRowContaining(
                "grid-ContractSectionV2DataGridGrid-wrap",
                savedApplicationNumber
        );

        click.clickByMarker("Открыть");
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

   /* private void removeFilterIfExists() {
        // Ждём загрузки раздела: кнопка "Фильтры/группы" (filterButton) есть всегда.
        $x("//*[@data-item-marker='filterButton']")
                .shouldBe(Condition.visible, java.time.Duration.ofSeconds(30));

        // Проверяем активный фильтр БЕЗ выброса исключения.
        // ВАЖНО: Selenide ElementNotFound наследует AssertionError (Error),
        // поэтому старый catch(Exception) его НЕ ловил — отсюда падение.
        SelenideElement removeBtn = $x("//*[contains(@class,'filter-remove')]");
        if (removeBtn.is(Condition.exist)) {
            log.info("Активный фильтр найден — удаляем");
            removeBtn.click();
        } else {
            log.debug("Активного фильтра нет — пропускаем");
        }
    }

    */

    private void removeFilterIfExists() {
        $x("//*[@data-item-marker='filterButton']")
                .shouldBe(Condition.visible, java.time.Duration.ofSeconds(30));
        basePage.waitForPage();

        SelenideElement removeBtn = $x(
                "//*[contains(@id,'QuickFilterModuleV2')]" +
                        "//*[contains(@class,'filter-remove-button')]");
        SelenideElement textBtn = $x("//span[normalize-space(.)='Фильтры/группы']");

        // Раздел определяется с состоянием асинхронно: либо появляется чип
        // фильтра с крестиком, либо текстовая кнопка «Фильтры/группы».
        // Фиксированное ожидание (5 с) не заставало чип на загруженной среде,
        // и фильтр оставался. Ждём то, что наступит раньше.
        Boolean filterPresent;
        try {
            filterPresent = new org.openqa.selenium.support.ui.FluentWait<>(
                    com.codeborne.selenide.WebDriverRunner.getWebDriver())
                    .withTimeout(java.time.Duration.ofSeconds(30))
                    .pollingEvery(java.time.Duration.ofMillis(500))
                    .until(d -> {
                        if (removeBtn.is(Condition.exist))   return Boolean.TRUE;
                        if (textBtn.is(Condition.visible))   return Boolean.FALSE;
                        return null;   // состояние ещё не определилось
                    });
        } catch (org.openqa.selenium.TimeoutException e) {
            throw new AssertionError(
                    "Раздел не определился с состоянием фильтра за 30 с", e);
        }

        if (!filterPresent) {
            log.debug("Активного фильтра нет — пропускаем");
            return;
        }

        log.info("Активный фильтр найден — удаляем");
        executeJavaScript("arguments[0].click();", removeBtn);
        removeBtn.should(Condition.disappear, java.time.Duration.ofSeconds(10));
        basePage.waitForPage();

        // Кнопка меняет вид по состоянию: иконочная при активном фильтре,
        // текстовая в пустом. Дожидаемся перерисовки.
        textBtn.shouldBe(Condition.visible, java.time.Duration.ofSeconds(30));
        log.info("Фильтр удалён");
    }

    /*private void applyNumberFilter() {
        $x("//*[@data-item-marker='searchEdit']//input")
                .setValue(savedApplicationNumber);
        click.clickByMarker("applyButton");
    }*/

    private void applyNumberFilter() {
        SelenideElement searchInput = $x(
                "//*[@data-item-marker='searchEdit']//input"
        ).shouldBe(Condition.visible, java.time.Duration.ofSeconds(15));

        // Creatio НЕ слышит setValue — вводим через sendKeys + TAB,
        // чтобы сработали onChange/onBlur и фильтр "увидел" значение.
        searchInput.click();
        searchInput.sendKeys(savedApplicationNumber);
        searchInput.sendKeys(Keys.TAB);

        // Кнопка "Поиск" (applyButton) — ждём кликабельности и жмём.
        SelenideElement applyBtn = $x("//*[@data-item-marker='applyButton']")
                .shouldBe(Condition.visible, java.time.Duration.ofSeconds(15));
        applyBtn.click();
    }
}