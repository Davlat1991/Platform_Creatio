package com.creatio.autotest.components;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BaseComponent;
import com.creatio.autotest.core.base.BasePage;
import io.qameta.allure.Step;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

/**
 * Единый компонент закрытия активности Creatio: hover строки → Execute → выбор
 * результата → Save. Заменяет дублировавшиеся приватные реализации во флоу
 * (Ш15.4). Общее ядro вынесено в {@link #openAndSubmit}; публичные методы
 * различаются только ведущим hover, таймаутом Save и хвостом-ожиданием (Ш15.5).
 */
public class ActivityComponent extends BaseComponent {

    private final BasePage basePage = new BasePage();

    // ══════════════════════════════════════
    // ЯДРО (общее для всех вариантов)
    // ══════════════════════════════════════

    /** hover строки активности → Execute → мини-страница → Result → Save. */
    private void openAndSubmit(String title, String result, Duration saveTimeout) {
        SelenideElement row = $x("//div[contains(@class,'dashboard-container-item')]" +
                "[.//text()[contains(.,'" + title + "')]]")
                .shouldBe(Condition.visible, Duration.ofSeconds(60));
        row.hover();

        SelenideElement executeBtn = row.$x(".//span[@data-item-marker='Execute']")
                .shouldBe(Condition.visible, Duration.ofSeconds(60));
        executeJavaScript("arguments[0].click();", executeBtn);

        $x("//div[@data-item-marker='ActivityMiniPageContainer']")
                .shouldBe(Condition.visible, Duration.ofSeconds(60));

        $x("//div[@data-item-marker='Result']")
                .shouldBe(Condition.visible, Duration.ofSeconds(60)).click();
        $x("//div[@data-item-marker='Result']//li[@data-item-marker='" + result + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30)).click();

        executeJavaScript("arguments[0].click();",
                $x("//span[@data-item-marker='SaveEditButton']")
                        .shouldBe(Condition.visible, saveTimeout));
    }

    /** Ведущий hover по строке уровня dashboard-item-content (нужен части флоу). */
    private void hoverItemContent(String title) {
        $x("//div[contains(@class,'dashboard-item-content')]" +
                "[.//text()[contains(.,'" + title + "')]]")
                .shouldBe(Condition.visible, Duration.ofSeconds(60))
                .hover();
    }

    // ══════════════════════════════════════
    // ВАРИАНТЫ (ядро + свой хвост)
    // ══════════════════════════════════════

    /** Закрыть активность + дождаться пустоты набора оверлеев (ApplicationFinishFlow). */
    @Step("Закрыть активность '{title}' (Result='{result}')")
    public void completeActivityWaitingOverlay(String title, String result) {
        log.info("Close activity '{}' with result '{}'", title, result);
        openAndSubmit(title, result, Duration.ofSeconds(60));

        $$("#AlignableMiniPageContainer-overlay, .alignable-container-overlay, .ts-loader, .mask")
                .filter(Condition.visible)
                .shouldHave(CollectionCondition.size(0), Duration.ofSeconds(30));

        basePage.waitForPage();
        log.info("Activity closed: '{}'", title);
    }

    /** Ведущий hover + закрыть активность, без ожидания оверлея (DocumentCollectionStageFlow). */
    @Step("Закрыть активность '{title}' (Result='{result}', без оверлея)")
    public void completeActivity(String title, String result) {
        log.info("Close activity '{}' with result '{}'", title, result);
        hoverItemContent(title);
        openAndSubmit(title, result, Duration.ofSeconds(60));
        basePage.waitForPage();
        log.info("Activity closed: '{}'", title);
    }

    /** Закрыть активность + дождаться закрытия мини-страницы и оверлея (AgroApplicationFinishFlow). */
    @Step("Закрыть активность '{title}' (Result='{result}', ожидание закрытия мини-страницы)")
    public void completeActivityAwaitingMiniPageClose(String title, String result) {
        log.info("Close activity '{}' with result '{}'", title, result);
        openAndSubmit(title, result, Duration.ofSeconds(30));

        $x("//div[@data-item-marker='ActivityMiniPageContainer']")
                .should(Condition.disappear, Duration.ofSeconds(60));
        $("div.AlignableMiniPageContainer-overlay")
                .should(Condition.disappear, Duration.ofSeconds(30));

        basePage.waitForPage();
        log.info("Activity closed: '{}'", title);
    }

    /** Ведущий hover + закрыть активность + дождаться снятия оверлея по id (AgroDocumentCollectionStageFlow). */
    @Step("Закрыть активность '{title}' (Result='{result}', ожидание оверлея по id)")
    public void completeActivityWaitingOverlayById(String title, String result) {
        log.info("Close activity '{}' with result '{}'", title, result);
        hoverItemContent(title);
        openAndSubmit(title, result, Duration.ofSeconds(60));
        basePage.waitForPage();

        $x("//div[@id='AlignableMiniPageContainer-overlay']")
                .should(Condition.disappear, Duration.ofSeconds(80));

        log.info("Activity closed: '{}'", title);
    }
}