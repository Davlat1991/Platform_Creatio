package com.creatio.autotest.flows.micro;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.flows.ProductSelectionFlow;
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
 * Live-подтверждение (скрин страницы «Подбор продукта» для Агро-клиента):
 * структура страницы идентична розничному {@link ProductSelectionFlow} —
 * заполняются программно только «Вид продукта» и «Цель кредитования»,
 * плюс «Сумма», «Срок, мес.» и «Валюта». Отличаются только конкретные
 * значения (продукт «Агро кредиты», иная цель, сумма и срок).
 * <p>
 * «Категория продукта» и «Тип продукта» — readonly-поля (серый текст на
 * скрине), проставляются Creatio автоматически при выборе «Вид продукта»,
 * программно не заполняются.
 * <p>
 * Поле «Сегмент» из {@code AgroProductData} на UI страницы подбора
 * продукта отсутствует — не используется в этом флоу.
 */
public class AgroProductSelectionFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroProductSelectionFlow.class);

    private final BasePage basePage = new BasePage();

    @Step("Выбор продукта Агро")
    public void selectProduct(AgroProductData product) {
        log.info("Select Agro product: type='{}'", product.getProductType());

        fillCriteria(product);
        findProducts();
        chooseFirstProduct();

        log.info("Agro product selected successfully");
    }

    // ══════════════════════════════════════
    // FILL CRITERIA
    // ══════════════════════════════════════

    @Step("Заполнить критерии подбора Агро")
    private void fillCriteria(AgroProductData product) {
        log.info("Fill Agro product criteria");

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
        log.info("Choose first Agro product from grid");

        SelenideElement gridWrap = $x(
                "//div[@id='grid-TsiOpportunityConditionSelectionDetailDataGridGrid-wrap']"
        ).shouldBe(Condition.visible, Duration.ofSeconds(15));

        gridWrap.scrollIntoView(true);

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