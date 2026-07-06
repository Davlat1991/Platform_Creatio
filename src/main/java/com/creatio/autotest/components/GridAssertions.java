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
import static java.lang.Thread.sleep;

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
                        Duration.ofSeconds(30));

        long endTime = System.currentTimeMillis() +
                Duration.ofSeconds(60).toMillis();

        while (System.currentTimeMillis() < endTime) {
            try {
                int columnIndex = findColumnIndex(columnName);
                if (columnIndex == -1) {
                    sleep(500);
                    continue;
                }

                ElementsCollection rows = $$x(
                        "//div[contains(@class,'grid-listed-row')" +
                                " or contains(@class,'grid-row')]"
                );

                if (rows.isEmpty()) {
                    sleep(500);
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

                sleep(500);

            } catch (Throwable t) {
                log.debug("Error in grid check: {}", t.getMessage());
                try { sleep(500); } catch (InterruptedException ignored) {}
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


    @Step("Проверить значение '{value}' в колонке '{columnName}' (без ожидания)")
    public boolean isValueInGridColumn(String columnName, String value) {
        log.info("Check '{}' in column '{}' (no-throw)", value, columnName);

        $$x("//div[contains(@class,'grid-captions')]//label")
                .shouldBe(CollectionCondition.sizeGreaterThan(0),
                        Duration.ofSeconds(10));

        // Одна попытка с коротким окном — для retry-циклов снаружи.
        long endTime = System.currentTimeMillis() +
                Duration.ofSeconds(10).toMillis();

        while (System.currentTimeMillis() < endTime) {
            try {
                int columnIndex = findColumnIndex(columnName);
                if (columnIndex == -1) {
                    sleep(500);
                    continue;
                }

                ElementsCollection rows = $$x(
                        "//div[contains(@class,'grid-listed-row')" +
                                " or contains(@class,'grid-row')]"
                );

                if (rows.isEmpty()) {
                    sleep(500);
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
                        if (cell.exists() && cell.isDisplayed()
                                && cell.getText().trim().equalsIgnoreCase(value)) {
                            log.info("Value '{}' found in column '{}'",
                                    value, columnName);
                            return true;
                        }
                    } catch (org.openqa.selenium.StaleElementReferenceException e) {
                        log.debug("Stale element, retrying row");
                    }
                }

                sleep(500);

            } catch (Throwable t) {
                log.debug("Error in grid check: {}", t.getMessage());
                try { sleep(500); } catch (InterruptedException ignored) {}
            }
        }

        log.info("Value '{}' NOT found in column '{}' (this attempt)",
                value, columnName);
        return false;
    }


    @Step("Ожидать КК4 в детали 'Проекты решений'")
    public boolean waitForCommitteeDecision(String committee, Duration timeout) {
        log.info("Wait for '{}' in 'Проекты решений' (timeout={}s)",
                committee, timeout.toSeconds());

        // Строка данных в детали несёт committee прямо в data-item-marker
        // (например "КК4 по заявке 391972"). Ищем строку со скоупом к гриду,
        // маркер которой содержит нужный комитет.
        try {
            $x("//div[@id='grid-TsiProjectSolutionDetailDataGridGrid-wrap']" +
                    "//div[contains(@class,'grid-listed-row')" +
                    " and contains(@data-item-marker,'" + committee + "')]"
            ).shouldBe(Condition.visible, timeout);
            log.info("Committee '{}' found in 'Проекты решений'", committee);
            return true;
        } catch (Throwable t) {
            log.warn("Committee '{}' NOT found within {}s",
                    committee, timeout.toSeconds());
            return false;
        }
    }
}