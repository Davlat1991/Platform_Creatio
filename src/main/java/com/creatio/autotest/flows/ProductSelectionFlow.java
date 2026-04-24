package com.creatio.autotest.flows;

import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.core.services.WaitService;
import com.creatio.autotest.pages.FieldPage;
import com.creatio.autotest.testdata.models.ProductData;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import com.codeborne.selenide.Condition;

import java.time.Duration;

public class ProductSelectionFlow {

    private static final Logger log = LoggerFactory.getLogger(ProductSelectionFlow.class);

    private final LookupComponent lookup   = new LookupComponent();
    private final FieldPage       field    = new FieldPage();
    private final BasePage        basePage = new BasePage();
    private final FieldPage fieldPage = new FieldPage();
    private final ClickService click = new ClickService();
    private final WaitService wait  = new WaitService();

    // ══════════════════════════════════════
    // SELECT PRODUCT
    // ══════════════════════════════════════


    @Step("Выбор продукта")
    public void selectProduct(ProductData product) {
        log.info("Select product: type='{}'", product.getProductType());

        fillCriteria(
                product.getProductType(),
                product.getCreditPurpose(),
                product.getAmount(),
                product.getTerm(),
                product.getCurrency()
        );
        findProducts();
        chooseFirstProduct();
        log.info("Product selected successfully");
    }

    // ══════════════════════════════════════
    // PRIVATE STEPS
    // ══════════════════════════════════════

    @Step("Заполнить критерии подбора продукта")
    private void fillCriteria(String productType,
                              String creditPurpose,
                              String amount,
                              String term,
                              String currency) {

        lookup.select("Вид продукта",      productType);
        lookup.select("Цель кредитования", creditPurpose);
        field.setField("Сумма",            amount);
        field.setField("Срок, мес.",       term);
        lookup.select("Валюта",            currency);
    }

    @Step("Подобрать продукты")
    private void findProducts() {
        log.info("Click 'Подобрать'");
        click.clickByName("Подобрать");
        basePage.waitForPage();
    }

    @Step("Выбрать первый продукт из списка")
    private void chooseFirstProduct() {
        log.info("Choose first product from grid");

        // Кликаем по первой строке грида
        $x("//div[@id='grid-TsiOpportunityConditionSelectionDetailDataGridGrid-wrap']" +
                "//div[contains(@class,'grid-row') and not(contains(@class,'grid-header'))]")
                .shouldBe(Condition.visible, Duration.ofSeconds(10))
                .scrollIntoView(true);

        executeJavaScript(
                "arguments[0].click();",
                $x("//div[@id='grid-TsiOpportunityConditionSelectionDetailDataGridGrid-wrap']" +
                        "//div[contains(@class,'grid-row') and not(contains(@class,'grid-header'))]")
        );

        // Ждём кнопку "Выбрать"
        $x("//span[normalize-space()='Выбрать']")
                .shouldBe(Condition.visible, Duration.ofSeconds(10));

        // Кликаем "Выбрать"
        click.clickByMarker("Выбрать");

        basePage.waitForPage();
    }
}