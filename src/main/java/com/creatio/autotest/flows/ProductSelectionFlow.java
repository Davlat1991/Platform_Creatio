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

/**
 * Единый флоу подбора продукта — для всех продуктовых линий (розница,
 * Агро/Микро и будущие типы). Заменяет раздельные розничный и агро-флоу.
 * <p>
 * Поведение задаётся двумя nullable-полями {@link ProductData}, а не типом
 * флоу:
 * <ul>
 *   <li>{@code productType} задан ⇒ выставляется «Тип продукта» перед «Видом»
 *       (Агро и прочие небазовые типы); {@code null}/пусто ⇒ шаг пропускается,
 *       действует дефолт «Потребительское кредитование» (розница);</li>
 *   <li>{@code productName} задан ⇒ строка результата выбирается по частичному
 *       совпадению имени (Агро/Микро); {@code null}/пусто ⇒ берётся первая
 *       строка грида (розница).</li>
 * </ul>
 * Значения полей подтверждены по live DOM обеих продуктовых линий; локаторы
 * перенесены из прежних флоу без изменений.
 */
public class ProductSelectionFlow {

    private static final Logger log =
            LoggerFactory.getLogger(ProductSelectionFlow.class);

    private final BasePage basePage = new BasePage();

    @Step("Выбор продукта")
    public void selectProduct(ProductData product) {
        log.info("Select product: type='{}' kind='{}' name='{}'",
                product.getProductType(), product.getProductKind(), product.getProductName());

        fillCriteria(product);
        findProducts();
        chooseProduct(product.getProductName());

        log.info("Product selected successfully");
    }

    // ══════════════════════════════════════
    // FILL CRITERIA
    // ══════════════════════════════════════

    @Step("Заполнить критерии подбора")
    private void fillCriteria(ProductData product) {
        log.info("Fill product criteria");

        // «Тип продукта» — только для небазовых типов (Агро и др.). Розница
        // оставляет поле пустым в данных → шаг пропускается, действует дефолт
        // «Потребительское кредитование».
        if (product.getProductType() != null && !product.getProductType().isBlank()) {
            setHandbookField("Тип продукта", "ProductType", product.getProductType());
        }

        // «Вид продукта» — разблокирует «Цель кредитования».
        setHandbookField("Вид продукта", "ProductKind", product.getProductKind());

        // «Цель кредитования» — активна только после выбора «Вид продукта».
        setHandbookField("Цель кредитования", "CreditPurpose", product.getCreditPurpose());

        // «Сумма» — числовое поле (FloatValue).
        setNumericField("Сумма", product.getAmount());

        // «Срок, мес.» — числовое поле (IntegerValue).
        setNumericField("Срок, мес.", product.getTerm());

        // «Валюта» — справочник.
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
                .shouldBe(Condition.visible, Duration.ofSeconds(60))
                .click();

        basePage.waitForPage();
    }

    // ══════════════════════════════════════
    // CHOOSE PRODUCT — стратегия по productName
    // ══════════════════════════════════════

    /**
     * Выбор строки в «Результаты подбора».
     * {@code productName} задан ⇒ по имени (частичное совпадение); пусто ⇒
     * первая строка грида.
     */
    @Step("Выбрать продукт из результатов подбора")
    private void chooseProduct(String productName) {
        if (productName != null && !productName.isBlank()) {
            chooseProductByName(productName);
        } else {
            chooseFirstProduct();
        }
    }

    @Step("Выбрать первый продукт из грида")
    private void chooseFirstProduct() {
        log.info("Choose first product from grid");

        SelenideElement gridWrap = $x(
                "//div[@id='grid-TsiOpportunityConditionSelectionDetailDataGridGrid-wrap']"
        ).shouldBe(Condition.visible, Duration.ofSeconds(20));

        gridWrap.scrollIntoView(true);

        // Строки с классом grid-active-selectable — кликаем, ждём кнопку "Выбрать"
        for (int attempt = 1; attempt <= 5; attempt++) {
            try {
                SelenideElement row = gridWrap.$x(
                        ".//div[contains(@class,'grid-row')" +
                                " and contains(@class,'grid-active-selectable')]"
                ).shouldBe(Condition.visible, Duration.ofSeconds(10));

                row.click();

                $x("//span[normalize-space()='Выбрать']")
                        .shouldBe(Condition.visible, Duration.ofSeconds(10))
                        .shouldBe(Condition.enabled);

                break;

            } catch (Exception e) {
                if (attempt == 5) {
                    throw new AssertionError(
                            "Послеклика по строке грида кнопка 'Выбрать' не появилась", e
                    );
                }
            }
        }

        $x("//span[@data-item-marker='Выбрать']" +
                " | //span[normalize-space()='Выбрать']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .shouldBe(Condition.enabled)
                .click();

        basePage.waitForPage();
    }

    /**
     * Выбирает продукт из «Результаты подбора» по имени (частичное совпадение).
     * <p>
     * Из DOM: строка грида — div.grid-row с data-item-marker = названием
     * продукта. Кнопка «Выбрать» лежит в отдельном соседнем блоке
     * div.grid-row-actions, связанном со строкой общим суффиксом
     * id (…-item-&lt;rowGuid&gt;). Клик по строке делает её активной и
     * раскрывает её actions, после чего кликаем «Выбрать» именно этой строки.
     */
    @Step("Выбрать продукт по имени: {productName}")
    private void chooseProductByName(String productName) {
        log.info("Choose product by name: '{}'", productName);

        SelenideElement row = $x(
                "//div[contains(@class,'grid-row')" +
                        " and contains(@data-item-marker, \"" + productName + "\")]"
        ).shouldBe(Condition.visible, Duration.ofSeconds(15));

        row.scrollIntoView(true);
        row.click();  // активируем строку → раскрываются её actions

        // Извлекаем суффикс item-<rowGuid> из id строки, чтобы найти
        // именно её блок actions (а не соседних строк).
        String rowId = row.getAttribute("id");
        String selectSpan;
        if (rowId != null && rowId.contains("item-")) {
            String rowGuid = rowId.substring(rowId.lastIndexOf("item-") + "item-".length());
            selectSpan = "//div[contains(@id,'actions-item-" + rowGuid + "')]" +
                    "//span[@data-item-marker='Выбрать']";
        } else {
            // Фолбэк: видимая (не скрытая) кнопка «Выбрать» активной строки.
            selectSpan = "//div[contains(@class,'grid-row-actions')" +
                    " and not(contains(@class,'grid-row-hidden'))]" +
                    "//span[@data-item-marker='Выбрать']";
        }

        $x(selectSpan)
                .shouldBe(Condition.visible, Duration.ofSeconds(20))
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
     * Дропдаун рендерится в body с data-item-marker (ProductType / ProductKind /
     * CreditPurpose / SpecificationListItem).
     */
    private void setHandbookField(String labelText, String dropdownMarker, String value) {
        log.debug("setHandbookField: label='{}' marker='{}' value='{}'",
                labelText, dropdownMarker, value);

        SelenideElement input = inputByLabel(labelText)
                .shouldBe(Condition.visible, Duration.ofSeconds(60))
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
                .shouldBe(Condition.visible, Duration.ofSeconds(30));

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