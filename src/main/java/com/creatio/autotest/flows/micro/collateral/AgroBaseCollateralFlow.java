package com.creatio.autotest.flows.micro.collateral;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.components.collateral.CollateralGridComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.testdata.models.CollateralData;
import org.openqa.selenium.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;

/**
 * Базовый флоу залога (Агро/Микро).
 * <p>
 * Изолированная Микро-копия розничного {@code BaseCollateralFlow}. Общие
 * компоненты ({@link LookupComponent}, {@link BasePage}, {@link ClickService})
 * переиспользуются из Core.
 * <p>
 * Отличие от розничного образца: {@link #setFieldByMarker(String, String)}
 * вводит значение через {@code sendKeys + Keys.TAB} — {@code setValue()}
 * не триггерит onChange/onBlur в Creatio.
 */
public abstract class AgroBaseCollateralFlow {

    protected final Logger          log      = LoggerFactory.getLogger(getClass());
    protected final LookupComponent lookup   = new LookupComponent();
    protected final BasePage        basePage = new BasePage();
    protected final ClickService    click    = new ClickService();
    protected final CollateralGridComponent grid = new CollateralGridComponent();

    /** Якорь открытой формы имущества — поле «Тип» есть только на ней. */
    private static final String PROPERTY_FORM_ANCHOR = "//label[normalize-space(.)='Тип']";

    // ══════════════════════════════════════
    // CONTRACT
    // ══════════════════════════════════════

    public abstract void fill(CollateralData data);

    // ══════════════════════════════════════
    // COMMON METHODS
    // ══════════════════════════════════════

    protected void setMarketValue(String value, String currency) {
        lookup.select("Рыночная стоимость", value);
        lookup.select("Валюта",             currency);
    }

    protected void setLiquidationValue(String value) {
        lookup.select("Ликвидационная стоимость", value);
    }

    protected void saveAndClose() {
        click.clickByName("Сохранить");
        click.clickByName("Закрыть");
    }

    /** Ввод значения через sendKeys + TAB (setValue не триггерит onChange). */
    protected void setFieldByMarker(String marker, String value) {
        log.debug("setFieldByMarker: marker='{}' value='{}'", marker, value);

        SelenideElement input = $x("//div[@data-item-marker='" + marker + "']/input")
                .shouldBe(Condition.visible, Duration.ofSeconds(30));

        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.DELETE);
        input.sendKeys(value);
        input.sendKeys(Keys.TAB);
    }

    /**
     * Клик по лупе выбора имущества с проверкой, что форма реально открылась.
     * <p>
     * Клик может уйти раньше, чем Creatio довесит обработчик на инлайн-редактор
     * строки залога: строка добавлена, лупа видна, но клик не открывает форму,
     * и следующий шаг падает на поиске поля «Тип». Проверяем результат клика,
     * а не сам факт клика.
     * <p>
     * catch ловит и AssertionError: Selenide бросает ElementNotFound, который
     * наследуется от AssertionError, а не от Exception.
     */
    protected void openPropertyLookupStable() {
        for (int attempt = 1; attempt <= 3; attempt++) {
            grid.openPropertyLookup();

            try {
                $x(PROPERTY_FORM_ANCHOR)
                        .shouldBe(Condition.visible, Duration.ofSeconds(15));
                log.info("Форма имущества открылась на попытке {}/3", attempt);
                return;
            } catch (Exception | AssertionError ignored) {
                log.warn("Форма имущества не открылась на попытке {}/3 — повтор клика", attempt);
            }
        }
        throw new AssertionError("Форма имущества не открылась после 3 попыток");
    }
}