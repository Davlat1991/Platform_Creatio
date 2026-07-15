package com.creatio.autotest.flows.micro;

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

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.executeJavaScript;

/**
 * Стадия «Сбор документов» (Агро/Микро) — цельный процесс.
 * <p>
 * Изолированная Микро-копия розничного {@code DocumentCollectionStageFlow}.
 * Общие компоненты ({@link DocumentUploadMap}, {@link FileUploadApiHelper},
 * {@link DetailPage}, {@link BasePage}) и нейтральные модели документов
 * ({@link DocumentOwner}, {@link DocumentOwnerData}, {@link DocumentData})
 * переиспользуются из Core.
 * <p>
 * Отличие от розничного образца: заёмщик обрабатывается тем же универсальным
 * движком {@link #uploadDocuments(DocumentOwnerData)}, что поручитель и
 * залогодатель. В рознице для заёмщика оставлена историческая двушаговая связка
 * {@code prepareBorrowerDetails() + uploadBorrowerDocuments()} с захардкоженными
 * именами файлов в теле флоу; шаги при этом идентичны универсальному движку.
 * В Микро заёмщик — обычный элемент списка владельцев, состав документов
 * приходит из {@code AgroScenarioFactory.agroDocumentOwners()}.
 */
public class AgroDocumentCollectionStageFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroDocumentCollectionStageFlow.class);

    /** Активность стадии «Сбор документов». */
    private static final String DOCUMENTS_ACTIVITY = "Вложить документы и отправить на рассмотрение";
    /** Результат выполнения активности (happy-path). */
    private static final String ACTIVITY_RESULT    = "Выполнена";

    private final BasePage   basePage   = new BasePage();
    private final DetailPage detailPage = new DetailPage();

    // ══════════════════════════════════════
    // MAIN
    // ══════════════════════════════════════

    @Step("Агро: стадия «Сбор документов»")
    public void completeDocumentCollectionStage(List<DocumentOwnerData> owners) {
        log.info("Agro: complete document collection stage, owners={}",
                owners == null ? 0 : owners.size());

        openDocumentsTab();

        if (owners != null && !owners.isEmpty()) {
            uploadAllDocuments(owners);
        } else {
            log.info("Agro: no document owners in scenario — only completing stage");
        }

        completeDocumentsActivity();

        log.info("Agro: document collection stage completed");
    }

    // ══════════════════════════════════════
    // НАВИГАЦИЯ
    // ══════════════════════════════════════

    @Step("Агро: открыть вкладку «Документы»")
    public void openDocumentsTab() {
        log.info("Agro: open Documents tab");
        openTopTab("Документы");
        basePage.waitForPage();
    }

    @Step("Агро: открыть вложенную вкладку «{tabName}»")
    public void openSubTab(String tabName) {
        log.info("Agro: open sub-tab: {}", tabName);
        openTopTab(tabName);
    }

    private void openTopTab(String name) {
        var tab = $x("//li[.//span[normalize-space()='" + name + "']]");
        tab.shouldBe(visible, Duration.ofSeconds(60)).click();
        tab.shouldHave(cssClass("ts-tabpanel-active-item"), Duration.ofSeconds(30));
    }

    // ══════════════════════════════════════
    // ЗАГРУЗКА ДОКУМЕНТОВ
    // ══════════════════════════════════════

    /**
     * Единый движок для любого владельца: открыть вкладку владельца → раскрыть
     * детали → собрать маппинг «тип документа → UUID» в скоупе контейнера →
     * загрузить все документы владельца через API.
     */
    @Step("Агро: загрузка документов владельца")
    public void uploadDocuments(DocumentOwnerData ownerData) {
        DocumentOwner owner = ownerData.getOwner();
        log.info("Agro: upload documents for owner: {}", owner);

        openSubTab(owner.uiTab);
        prepareDetails(owner);

        DocumentUploadMap map = new DocumentUploadMap().collect(owner.containerId);
        log.info("Agro: DocumentUploadMap[{}] size={}", owner, map.size());

        for (DocumentData doc : ownerData.getDocuments()) {
            String uuid = map.get(doc.getType());
            log.info("  {} → type='{}' file='{}' uuid='{}'",
                    owner, doc.getType(), doc.getFileName(), uuid);
            upload(doc.getFileName(), uuid);
        }
    }

    @Step("Агро: загрузка документов всех владельцев")
    public void uploadAllDocuments(List<DocumentOwnerData> owners) {
        log.info("Agro: upload documents for {} owner(s)", owners.size());
        for (DocumentOwnerData ownerData : owners) {
            uploadDocuments(ownerData);
        }
    }

    /**
     * Раскрывает детали в зависимости от роли владельца.
     * BORROWER / GUARANTOR — «Финансовое досье» + «Досье клиента».
     * PLEDGER — «Досье клиента».
     */
    private void prepareDetails(DocumentOwner owner) {
        log.info("Agro: prepare details for owner: {}", owner);

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
    // ЗАКРЫТИЕ АКТИВНОСТИ
    // Паттерн hover → Execute → Result → Save (подтверждён в
    // AgroPreliminaryCheckFlow и AgroCollateralStageFlow).
    // ══════════════════════════════════════

    @Step("Агро: закрытие активности «Вложить документы и отправить на рассмотрение»")
    public void completeDocumentsActivity() {
        log.info("Agro: close documents activity");

        // hover — кнопка Execute видна только при наведении
        $x("//div[contains(@class,'dashboard-item-content')]" +
                "[.//text()[contains(.,'" + DOCUMENTS_ACTIVITY + "')]]")
                .shouldBe(visible, Duration.ofSeconds(60))
                .hover();

        var row = $x("//div[contains(@class,'dashboard-container-item')]" +
                "[.//text()[contains(.,'" + DOCUMENTS_ACTIVITY + "')]]")
                .shouldBe(visible, Duration.ofSeconds(60));
        row.hover();

        var executeBtn = row.$x(".//span[@data-item-marker='Execute']")
                .shouldBe(visible, Duration.ofSeconds(60));
        executeJavaScript("arguments[0].click();", executeBtn);
        log.debug("Execute clicked");

        $x("//div[@data-item-marker='ActivityMiniPageContainer']")
                .shouldBe(visible, Duration.ofSeconds(60));

        // Список результатов подгружается только после обращения к полю
        $x("//div[@data-item-marker='Result']")
                .shouldBe(visible, Duration.ofSeconds(60)).click();
        $x("//div[@data-item-marker='Result']//li[@data-item-marker='" + ACTIVITY_RESULT + "']")
                .shouldBe(visible, Duration.ofSeconds(30)).click();
        log.debug("Result set to '{}'", ACTIVITY_RESULT);

        executeJavaScript("arguments[0].click();",
                $x("//span[@data-item-marker='SaveEditButton']")
                        .shouldBe(visible, Duration.ofSeconds(60)));

        basePage.waitForPage();

        // Оверлей мини-страницы держится после сохранения и перехватывает
        // последующие клики (в т.ч. logout). Ждём его снятия.
        $x("//div[@id='AlignableMiniPageContainer-overlay']")
                .should(disappear, Duration.ofSeconds(60));

        log.info("Agro: documents activity closed");
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

    private void upload(String fileName, String parentColumnValue) {
        if (parentColumnValue == null) {
            throw new RuntimeException(
                    "parentColumnValue null для файла: " + fileName);
        }
        FileUploadApiHelper.uploadFile(fileName, parentColumnValue);
    }

    /**
     * Сценарий Агро/Микро: на стадии «Сбор документов» документы не
     * прикрепляются — активность просто закрывается. Документы появляются
     * позже, на стадии «Информирование клиента», под ролью «Мидл контролер».
     * <p>
     * Движок загрузки ({@link #uploadDocuments}) сохранён в классе и готов
     * к использованию, если состав сценария изменится.
     */
    @Step("Агро: стадия «Сбор документов» — завершение без вложений")
    public void completeDocumentCollectionStage() {
        log.info("Agro: complete document collection stage without attachments");
        completeDocumentsActivity();
    }
}