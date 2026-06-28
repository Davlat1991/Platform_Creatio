package com.creatio.autotest.components.collateral;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.core.base.BaseComponent;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import static com.codeborne.selenide.Selenide.refresh;

public class CollateralTabsComponent extends BaseComponent {

    private final ButtonsComponent buttons = new ButtonsComponent();

    @Step("Открыть вкладку 'Обеспечение'")
    public void openCollateralTab() {
        log.info("Open collateral tab");

        $x("//div[@id='FinApplicationPageTabsContainerContainer']")
                .shouldBe(Condition.visible);

        // Ждём стабильности контейнера вместо рефреша
        $x("//div[@id='FinApplicationPageTabsContainerContainer']")
                .shouldBe(Condition.visible, Duration.ofSeconds(60));

        for (int i = 0; i < 3; i++) {
            try {
                SelenideElement tab = $x(
                        "//li//span[normalize-space()='Обеспечение']"
                ).shouldBe(Condition.visible);

                executeJavaScript("arguments[0].click();", tab);

                $x("//span[contains(text(),'Залоги')]")
                        .shouldBe(Condition.visible, Duration.ofSeconds(30));

                log.info("Collateral tab opened");
                return;

            } catch (Exception e) {
                log.warn("Retry open collateral tab: {}/3", i + 1);
            }
        }
        throw new RuntimeException("Failed to open collateral tab");
    }
}