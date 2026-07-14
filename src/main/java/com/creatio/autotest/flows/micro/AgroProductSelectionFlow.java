package com.creatio.autotest.flows.micro;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.testdata.models.AgroProductData;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;

/**
 * Флоу подбора продукта «Агро» (сегмент «Микро»).
 * <p>
 * Live-подтверждение (скрины страницы «Подбор продукта» для Агро):
 * заполняются «Тип продукта» (по умолчанию «Потребительское кредитование»
 * → «Агро кредиты»), «Вид продукта», «Цель кредитования», «Сумма»,
 * «Срок, мес.», «Валюта». «Категория продукта» — readonly, не заполняется.
 * После «Подобрать» из детали «Результаты подбора» выбирается нужный
 * продукт ПО ИМЕНИ (несколько результатов различаются диапазоном суммы и
 * типом клиента — новый/повторный).
 */
public class AgroProductSelectionFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroProductSelectionFlow.class);

    private final BasePage basePage = new BasePage();

    @Step("Выбор продукта Агро")
    public void selectProduct(AgroProductData product) {
        log.info("Select Agro product: type='{}' kind='{}' name='{}'",
                product.getProductType(), product.getProductKind(), product.getProductName());

        fillCriteria(product);
        findProducts();
        chooseProductByName(product.getProductName());

        log.info("Agro product selected successfully");
    }

    // ══════════════════════════════════════
    // FILL CRITERIA
    // ══════════════════════════════════════

    @Step("Заполнить критерии подбора Агро")
    private void fillCriteria(AgroProductData product) {
        log.info("Fill Agro product criteria");

        // Тип продукта — по умолчанию "Потребительское кредитование",
        // меняем на "Агро кредиты" (разблокирует Вид продукта для Агро).
        setHandbookField("Тип продукта", "ProductType", product.getProductType());

        // Вид продукта — разблокирует "Цель кредитования"
        setHandbookField("Вид продукта", "ProductKind", product.getProductKind());

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

        $x("//span[@data-item-marker='SelectionButton']")
                .shouldBe(Condition.visible, Duration.ofSeconds(20))
                .click();

        basePage.waitForPage();
    }

    // ══════════════════════════════════════
    // CHOOSE PRODUCT BY NAME
    // ══════════════════════════════════════

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
        log.info("Choose Agro product by name: '{}'", productName);

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
    // HELPERS (идентичны ProductSelectionFlow)
    // ══════════════════════════════════════

    private void setHandbookField(String labelText, String dropdownMarker, String value) {
        log.debug("setHandbookField: label='{}' marker='{}' value='{}'",
                labelText, dropdownMarker, value);

        SelenideElement input = inputByLabel(labelText)
                .shouldBe(Condition.visible, Duration.ofSeconds(10))
                .shouldNotBe(Condition.disabled);

        input.closest("div").click();

        input.sendKeys(Keys.CONTROL + "a");
        input.sendKeys(Keys.DELETE);
        input.sendKeys(value);

        $x("//div[@data-item-marker='" + dropdownMarker + "']" +
                "//li[normalize-space(.)='" + value + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(7))
                .click();

        input.shouldHave(Condition.exactValue(value));
    }

    private void setNumericField(String labelText, String value) {
        log.debug("setNumericField: label='{}' value='{}'", labelText, value);

        SelenideElement input = inputByLabel(labelText)
                .shouldBe(Condition.visible, Duration.ofSeconds(10));

        input.click();
        input.sendKeys(Keys.CONTROL + "a");
        input.sendKeys(value);
        input.sendKeys(Keys.TAB);
    }

    private SelenideElement inputByLabel(String labelText) {
        return $x(
                "//label[normalize-space(.)='" + labelText + "']" +
                        "/ancestor::div[contains(@class,'control-width')]" +
                        "//input"
        );
    }
}