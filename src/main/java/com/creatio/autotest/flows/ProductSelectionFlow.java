package com.creatio.autotest.flows;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.testdata.models.ProductData;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class ProductSelectionFlow {

    private static final Logger log =
            LoggerFactory.getLogger(ProductSelectionFlow.class);

    private final BasePage basePage = new BasePage();

    @Step("Выбор продукта")
    public void selectProduct(ProductData product) {
        log.info("Select product: type='{}'", product.getProductType());

        fillCriteria(product);
        findProducts();
        chooseFirstProduct();

        log.info("Product selected successfully");
    }

    // ══════════════════════════════════════
    // FILL CRITERIA
    // ══════════════════════════════════════

    @Step("Заполнить критерии подбора")
    private void fillCriteria(ProductData product) {
        log.info("Fill product criteria");

        // Вид продукта — разблокирует "Цель кредитования"
        setHandbookField("Вид продукта", "ProductKind", product.getProductType());

        // Цель кредитования — активна только после выбора "Вид продукта"
        setHandbookField("Цель кредитования", "CreditPurpose", product.getCreditPurpose());

        // Сумма — числовое поле (FloatValue)
        setNumericField("Сумма", product.getAmount());

        // Срок, мес. — числовое поле (IntegerValue)
        setNumericField("Срок, мес.", product.getTerm());

        // Валюта — справочник
        setHandbookField("Валюта", "SpecificationListItem", product.getCurrency());
    }

    // ══════════════════════════════════════
    // FIND PRODUCTS
    // ══════════════════════════════════════

    @Step("Нажать 'Подобрать'")
    private void findProducts() {
        log.info("Click 'Подобрать'");

        // Из DOM: span.t-btn-wrapper[data-item-marker='SelectionButton'] текст 'Подобрать'
        $x("//span[@data-item-marker='SelectionButton']")
                .shouldBe(Condition.visible, Duration.ofSeconds(10))
                .click();

        basePage.waitForPage();
    }

    // ══════════════════════════════════════
    // CHOOSE FIRST PRODUCT
    // ══════════════════════════════════════

    @Step("Выбрать первый продукт из грида")
    private void chooseFirstProduct() {
        log.info("Choose first product from grid");

        SelenideElement gridWrap = $x(
                "//div[@id='grid-TsiOpportunityConditionSelectionDetailDataGridGrid-wrap']"
        ).shouldBe(Condition.visible, Duration.ofSeconds(15));

        gridWrap.scrollIntoView(true);

        // Строки с классом grid-active-selectable — кликаем, ждём кнопку "Выбрать"
        for (int attempt = 1; attempt <= 5; attempt++) {
            try {
                SelenideElement row = gridWrap.$x(
                        ".//div[contains(@class,'grid-row')" +
                                " and contains(@class,'grid-active-selectable')]"
                ).shouldBe(Condition.visible, Duration.ofSeconds(5));

                row.click();

                $x("//span[normalize-space()='Выбрать']")
                        .shouldBe(Condition.visible, Duration.ofSeconds(5))
                        .shouldBe(Condition.enabled);

                break;

            } catch (Exception e) {
                if (attempt == 5) {
                    throw new AssertionError(
                            "После клика по строке грида кнопка 'Выбрать' не появилась", e
                    );
                }
            }
        }

        $x("//span[@data-item-marker='Выбрать']" +
                " | //span[normalize-space()='Выбрать']")
                .shouldBe(Condition.visible, Duration.ofSeconds(10))
                .shouldBe(Condition.enabled)
                .click();

        basePage.waitForPage();
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

    /**
     * Заполняет справочное ComboBox-поле по тексту лейбла.
     *
     * id полей содержит динамический GUID → поиск по id ненадёжен.
     * Используем label → ancestor control-width → input.
     *
     * Дропдаун рендерится в body с data-item-marker (ProductKind / CreditPurpose / SpecificationListItem).
     */
    private void setHandbookField(String labelText, String dropdownMarker, String value) {
        log.debug("setHandbookField: label='{}' marker='{}' value='{}'",
                labelText, dropdownMarker, value);

        SelenideElement input = inputByLabel(labelText)
                .shouldBe(Condition.visible, Duration.ofSeconds(10))
                .shouldNotBe(Condition.disabled);

        // Клик на wrapper — снимает focus-trap, открывает поле
        input.closest("div").click();

        input.sendKeys(Keys.CONTROL + "a");
        input.sendKeys(Keys.DELETE);
        input.sendKeys(value);

        // Дропдаун в body по data-item-marker
        $x("//div[@data-item-marker='" + dropdownMarker + "']" +
                "//li[normalize-space(.)='" + value + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(7))
                .click();

        // Проверяем фиксацию значения
        input.shouldHave(Condition.exactValue(value));
    }

    /**
     * Заполняет числовое поле по тексту лейбла.
     * Tab после ввода фиксирует значение в Creatio.
     */
    private void setNumericField(String labelText, String value) {
        log.debug("setNumericField: label='{}' value='{}'", labelText, value);

        SelenideElement input = inputByLabel(labelText)
                .shouldBe(Condition.visible, Duration.ofSeconds(10));

        input.click();
        input.sendKeys(Keys.CONTROL + "a");
        input.sendKeys(value);
        input.sendKeys(Keys.TAB);
    }

    /**
     * Ищет input по тексту лейбла через структуру Creatio control-width.
     *
     * Структура DOM:
     *   label[text=labelText]
     *     └── ancestor div.control-width
     *           └── ... input
     *
     * id полей НЕ используем — они содержат динамический GUID.
     */
    private SelenideElement inputByLabel(String labelText) {
        return $x(
                "//label[normalize-space(.)='" + labelText + "']" +
                        "/ancestor::div[contains(@class,'control-width')]" +
                        "//input"
        );
    }
}