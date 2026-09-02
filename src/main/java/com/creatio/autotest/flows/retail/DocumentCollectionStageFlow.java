package com.creatio.autotest.flows.retail;

import com.creatio.autotest.components.ActivityComponent;
import com.creatio.autotest.core.api.files.DocumentUploadMap;
import com.creatio.autotest.core.api.files.FileUploadApiHelper;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.pages.DetailPage;
import com.creatio.autotest.testdata.models.documents.DocumentData;
import com.creatio.autotest.testdata.models.ParticipantRole;
import com.creatio.autotest.testdata.models.documents.DocumentOwnerData;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.codeborne.selenide.Condition.cssClass;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$x;

/**
 * Стадия "Сбор документов".
 * D4.1-D4.3: документы заёмщика (проверенный путь).
 * D4.4: универсальный uploadDocuments(ownerData) — единый движок для всех владельцев.
 * D4.5: закрытие активности "Вложить документы и отправить на рассмотрение".
 */
public class DocumentCollectionStageFlow {

    private static final Logger log =
            LoggerFactory.getLogger(DocumentCollectionStageFlow.class);

    private final BasePage            basePage   = new BasePage();
    private final DetailPage          detailPage = new DetailPage();
    private final ActivityComponent   activity   = new ActivityComponent();

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
        openSubTab(ParticipantRole.BORROWER.uiTab);
        prepareDetails(ParticipantRole.BORROWER);
    }

    @Step("Загрузка документов заёмщика через API")
    public void uploadBorrowerDocuments() {
        log.info("Upload borrower documents via API");

        DocumentUploadMap map =
                        new DocumentUploadMap().collect(ParticipantRole.BORROWER.containerId);

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
        ParticipantRole owner = ownerData.getOwner();
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
    private void prepareDetails(ParticipantRole owner) {
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
        activity.completeActivity("Вложить документы и отправить на рассмотрение", "Выполнена");
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