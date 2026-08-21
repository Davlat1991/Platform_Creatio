package com.creatio.autotest.components;

import com.codeborne.selenide.Condition;
import com.creatio.autotest.core.base.BaseComponent;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;

public class GridAssertions extends BaseComponent {

    // ══════════════════════════════════════
    // CREDIT DECISION
    // ══════════════════════════════════════


    // HELPERS
    // ══════════════════════════════════════


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