package com.creatio.autotest.components;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BaseComponent;
import com.creatio.autotest.testdata.models.ScoringDecision;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

public class GridAssertions extends BaseComponent {

    // ══════════════════════════════════════
    // CREDIT DECISION
    // ══════════════════════════════════════

    @Step("Ожидаем любое решение по кредиту")
    public void waitForAnyCreditDecision() {
        log.info("Waiting for credit decision (Одобрить / Отказать)");

        long endTime = System.currentTimeMillis() +
                Duration.ofSeconds(90).toMillis();

        while (System.currentTimeMillis() < endTime) {
            try {
                openChecksTab();

                SelenideElement container = $x(
                        "//*[@data-item-marker='BnzScoringDetailDetailContainer']"
                ).shouldBe(Condition.visible, Duration.ofSeconds(5));

                SelenideElement decision = container.$x(
                        ".//span[@grid-data-type='text' and " +
                                "(normalize-space()='Одобрить'" +
                                " or normalize-space()='Отказать')]"
                ).shouldBe(Condition.visible, Duration.ofSeconds(5));

                log.info("Credit decision received: {}", decision.getText().trim());
                return;

            } catch (Throwable t) {
                log.debug("Decision not ready yet, retrying...");
            }
        }
        throw new AssertionError(
                "Credit decision did not appear within 90 seconds"
        );
    }

    // ══════════════════════════════════════
    // GRID COLUMN VALUE
    // ══════════════════════════════════════

    @Step("Ожидаем значение '{value}' в колонке '{columnName}'")
    public void waitForValueInGridColumn(String columnName, String value) {
        log.info("Wait for '{}' in column '{}'", value, columnName);

        $$x("//div[contains(@class,'grid-captions')]//label")
                .shouldBe(CollectionCondition.sizeGreaterThan(0),
                        Duration.ofSeconds(10));

        long endTime = System.currentTimeMillis() +
                Duration.ofSeconds(60).toMillis();

        while (System.currentTimeMillis() < endTime) {
            try {
                int columnIndex = findColumnIndex(columnName);
                if (columnIndex == -1) {
                    Thread.sleep(500);
                    continue;
                }

                ElementsCollection rows = $$x(
                        "//div[contains(@class,'grid-listed-row')" +
                                " or contains(@class,'grid-row')]"
                );

                if (rows.isEmpty()) {
                    Thread.sleep(500);
                    continue;
                }

                for (SelenideElement row : rows) {
                    try {
                        row.scrollIntoView(false);
                        SelenideElement cell = row.$x(
                                ".//div[contains(@class,'grid-cols-')]" +
                                        "[" + columnIndex + "]"
                        );
                        if (!cell.exists()) {
                            cell = row.$x(
                                    ".//div[" + columnIndex + "]//span"
                            );
                        }
                        if (cell.exists() && cell.isDisplayed()) {
                            if (cell.getText().trim().equalsIgnoreCase(value)) {
                                log.info("Value '{}' found in column '{}'",
                                        value, columnName);
                                return;
                            }
                        }
                    } catch (org.openqa.selenium.StaleElementReferenceException e) {
                        log.debug("Stale element, retrying row");
                    }
                }

                Thread.sleep(500);

            } catch (Throwable t) {
                log.debug("Error in grid check: {}", t.getMessage());
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            }
        }

        throw new AssertionError(
                "Value '" + value + "' not found in column '" + columnName + "'"
        );
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

    private void openChecksTab() {
        SelenideElement tab = $x(
                "//span[contains(text(),'Проверки')]"
        ).shouldBe(Condition.visible, Condition.enabled);
        tab.hover();
        tab.shouldBe(Condition.interactable);
        tab.click();
    }

    private int findColumnIndex(String columnName) {
        ElementsCollection headers = $$x(
                "//div[contains(@class,'grid-captions')]//label"
        );
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).getText().trim()
                    .equalsIgnoreCase(columnName)) {
                return i + 1;
            }
        }
        return -1;
    }



    @Step("Прочитать решение скоринга")
    public String readScoringDecision() {
        log.info("Read scoring decision");

        long endTime = System.currentTimeMillis() +
                Duration.ofSeconds(90).toMillis();

        while (System.currentTimeMillis() < endTime) {
            try {
                openChecksTab();

                SelenideElement container = $x(
                        "//*[@data-item-marker='BnzScoringDetailDetailContainer']"
                ).shouldBe(Condition.visible, Duration.ofSeconds(5));

                SelenideElement decision = container.$x(
                        ".//span[@grid-data-type='text' and (" +
                                "normalize-space()='Одобрить'" +
                                " or normalize-space()='Отказать'" +
                                " or normalize-space()='Предложить сумму'" +
                                " or normalize-space()='Одобрить с обеспечением')]"
                ).shouldBe(Condition.visible, Duration.ofSeconds(5));

                String text = decision.getText().trim();
                log.info("Scoring decision read: '{}'", text);
                return text;

            } catch (Throwable t) {
                log.debug("Decision not ready yet, retrying...");
            }
        }
        throw new AssertionError(
                "Scoring decision did not appear within 90 seconds"
        );
    }
}