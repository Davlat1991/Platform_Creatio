package com.creatio.autotest.flows.retail;

import com.creatio.autotest.core.api.files.DocumentUploadMap;
import com.creatio.autotest.core.api.files.FileUploadApiHelper;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.pages.DetailPage;
import com.creatio.autotest.testdata.models.documents.DocumentData;
import com.creatio.autotest.testdata.models.documents.DocumentOwner;
import com.creatio.autotest.testdata.models.documents.DocumentOwnerData;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

import static com.codeborne.selenide.Condition.cssClass;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

/**
 * Стадия "Сбор документов".
 * D4.1-D4.3: документы заёмщика (проверенный путь).
 * D4.4: универсальный uploadDocuments(ownerData) — единый движок для всех владельцев.
 * D4.5: закрытие активности "Вложить документы и отправить на рассмотрение".
 */
public class DocumentCollectionStageFlow {

    private static final Logger log =
            LoggerFactory.getLogger(DocumentCollectionStageFlow.class);

    private final BasePage   basePage   = new BasePage();
    private final DetailPage detailPage = new DetailPage();

    // ══════════════════════════════════════
    // НАВИГАЦИЯ
    // ══════════════════════════════════════

    @Step("Открыть вкладку 'Документы'")
    public void openDocumentsTab() {
        log.info("Open Documents tab");
        openTopTab("Документы");
        basePage.waitForPage();
    }

    @Step("Открыть вложенную вкладку '{tabName}'")
    public void openSubTab(String tabName) {
        log.info("Open sub-tab: {}", tabName);
        openTopTab(tabName);
    }

    private void openTopTab(String name) {
        var tab = $x("//li[.//span[normalize-space()='" + name + "']]");
        tab.shouldBe(visible).click();
        tab.shouldHave(cssClass("ts-tabpanel-active-item"));
    }

    // ══════════════════════════════════════
    // D4.1-D4.3 — ЗАЁМЩИК (проверенный 2-шаговый путь)
    // Двушаговость сохранена намеренно: openDetailStable кликает span,
    // повторное раскрытие свернуло бы уже открытую деталь.
    // ══════════════════════════════════════

    @Step("Подготовка деталей документов заёмщика")
    public void prepareBorrowerDetails() {
        log.info("Prepare borrower document details");
        openSubTab(DocumentOwner.BORROWER.uiTab);
        prepareDetails(DocumentOwner.BORROWER);
    }

    @Step("Загрузка документов заёмщика через API")
    public void uploadBorrowerDocuments() {
        log.info("Upload borrower documents via API");

        DocumentUploadMap map =
                new DocumentUploadMap().collect(DocumentOwner.BORROWER.containerId);

        log.info("Income   → {}", map.get("Income"));
        log.info("INN      → {}", map.get("INN"));
        log.info("Passport → {}", map.get("Passport"));

        upload("Income.pdf",   map.get("Income"));
        upload("INN.pdf",      map.get("INN"));
        upload("Passport.pdf", map.get("Passport"));
    }

    // ══════════════════════════════════════
    // D4.4 — УНИВЕРСАЛЬНЫЙ ДВИЖОК (поручитель / залогодатель)
    // ══════════════════════════════════════

    /**
     * Единый путь: открыть вкладку владельца → раскрыть детали →
     * собрать тип→UUID в скоупе контейнера → загрузить все документы.
     * Подходит для GUARANTOR (3 файла) и PLEDGER (1 файл — FATCA).
     */
    @Step("Загрузка документов владельца")
    public void uploadDocuments(DocumentOwnerData ownerData) {
        DocumentOwner owner = ownerData.getOwner();
        log.info("Upload documents for owner: {}", owner);

        openSubTab(owner.uiTab);
        prepareDetails(owner);

        DocumentUploadMap map = new DocumentUploadMap().collect(owner.containerId);
        log.info("DocumentUploadMap[{}] size={}", owner, map.size());

        for (DocumentData doc : ownerData.getDocuments()) {
            String uuid = map.get(doc.getType());
            log.info("  {} → type='{}' file='{}' uuid='{}'",
                    owner, doc.getType(), doc.getFileName(), uuid);
            upload(doc.getFileName(), uuid);
        }
    }

    @Step("Загрузка документов всех участников")
    public void uploadAllParticipantDocuments(List<DocumentOwnerData> owners) {
        log.info("Upload documents for {} participant(s)", owners.size());
        for (DocumentOwnerData ownerData : owners) {
            uploadDocuments(ownerData);
        }
    }

    /**
     * Раскрывает детали в зависимости от роли — единый источник правды
     * для всех владельцев.
     *  BORROWER / GUARANTOR — «Финансовое досье» + «Досье клиента».
     *  PLEDGER — «Досье клиента» (⚠ предположение, проверить по DOM #DocPledgerTab).
     */
    private void prepareDetails(DocumentOwner owner) {
        log.info("Prepare details for owner: {}", owner);
        switch (owner) {
            case BORROWER, GUARANTOR -> {
                detailPage.openDetailStable("Финансовое досье");
                detailPage.openDetailStable("Досье клиента");
            }
            case PLEDGER -> detailPage.openDetailStable("Досье клиента");
        }
        basePage.waitForPage();
    }

    // ══════════════════════════════════════
    // D4.5 — ЗАКРЫТИЕ АКТИВНОСТИ
    // Паттерн hover→Execute→Result→Save, проверен в PreliminaryCheckStageFlow.
    // ══════════════════════════════════════

    @Step("Закрытие активности 'Вложить документы и отправить на рассмотрение'")
    public void completeDocumentsActivity() {
        log.info("Close documents activity");
        String title = "Вложить документы и отправить на рассмотрение";

        // hover — кнопка Execute видна только при наведении
        $x("//div[contains(@class,'dashboard-item-content')]" +
                "[.//text()[contains(.,'" + title + "')]]")
                .shouldBe(visible, Duration.ofSeconds(60))
                .hover();

        var row = $x("//div[contains(@class,'dashboard-container-item')]" +
                "[.//text()[contains(.,'" + title + "')]]")
                .shouldBe(visible, Duration.ofSeconds(60));
        row.hover();

        var executeBtn = row.$x(".//span[@data-item-marker='Execute']")
                .shouldBe(visible, Duration.ofSeconds(60));
        executeJavaScript("arguments[0].click();", executeBtn);
        log.debug("Execute clicked");

        $x("//div[@data-item-marker='ActivityMiniPageContainer']")
                .shouldBe(visible, Duration.ofSeconds(60));

        $x("//div[@data-item-marker='Result']")
                .shouldBe(visible, Duration.ofSeconds(60)).click();
        $x("//div[@data-item-marker='Result']//li[@data-item-marker='Выполнена']")
                .shouldBe(visible, Duration.ofSeconds(30)).click();
        log.debug("Result set to 'Выполнена'");

        executeJavaScript("arguments[0].click();",
                $x("//span[@data-item-marker='SaveEditButton']")
                        .shouldBe(visible, Duration.ofSeconds(60)));

        basePage.waitForPage();
        log.info("Documents activity closed");
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

    private void upload(String fileName, String parentColumnValue) {
        if (parentColumnValue == null) {
            throw new RuntimeException("parentColumnValue null для файла: " + fileName);
        }
        FileUploadApiHelper.uploadFile(fileName, parentColumnValue);
    }
}