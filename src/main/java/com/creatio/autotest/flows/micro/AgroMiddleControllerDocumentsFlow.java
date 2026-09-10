package com.creatio.autotest.flows.micro;

import com.creatio.autotest.core.api.files.DocumentUploadMap;
import com.creatio.autotest.core.api.files.FileUploadApiHelper;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.verify.VerificationCollector;
import com.creatio.autotest.flows.common.ApplicationSearchFlow;
import com.creatio.autotest.pages.ConsultationPanelPage;
import com.creatio.autotest.pages.DetailPage;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import com.creatio.autotest.testdata.models.documents.DocumentData;
import com.creatio.autotest.testdata.models.documents.DocumentOwnerData;
import com.creatio.autotest.utils.FieldUtils;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Вложение документов под ролью «Мидл контролер» (Агро/Микро).
 * <p>
 * По сценарию Микро на стадии «Сбор документов» документы не прикрепляются
 * ({@link AgroDocumentCollectionStageFlow#completeDocumentCollectionStage()}
 * лишь закрывает активность). Единственный документ заёмщика вкладывается
 * позже — после «Информирования клиента», под мидл-контролером (вход через
 * рабочее место ИКОК).
 * <p>
 * Собственного движка загрузки не имеет: переиспользует публичную навигацию
 * {@link AgroDocumentCollectionStageFlow} и Core-компоненты
 * ({@link DocumentUploadMap}, {@link FileUploadApiHelper}, {@link DetailPage}).
 * От универсального {@code uploadDocuments(owner)} отличается тем, что
 * раскрывает только «Досье клиента» — «Финансовое досье» здесь не участвует.
 * <p>
 * Логин выполняет тест (как в {@code AgroClientNotificationStageFlow}); флоу
 * открывает заявку по номеру из {@link ScenarioContextProvider}.
 */
public class AgroMiddleControllerDocumentsFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroMiddleControllerDocumentsFlow.class);

    /** Единственная деталь заёмщика, где лежит документ (Шиносномаи ЧТ). */
    private static final String BORROWER_DOSSIER_DETAIL = "Досье клиента";

    private final ConsultationPanelPage            consultation  = new ConsultationPanelPage();
    private final ApplicationSearchFlow            searchFlow    = new ApplicationSearchFlow();
    private final AgroDocumentCollectionStageFlow  documents     = new AgroDocumentCollectionStageFlow();
    private final DetailPage                       detailPage    = new DetailPage();
    private final BasePage                         basePage      = new BasePage();

    @Step("Агро: мидл-контролер — вложение документов заёмщика")
    public void attachBorrowerDocuments(DocumentOwnerData ownerData) {
        String applicationNumber =
                ScenarioContextProvider.get().getApplicationNumber();
        log.info("Agro: middle controller attach documents, number='{}'",
                applicationNumber);

        if (applicationNumber == null || applicationNumber.isBlank()) {
            throw new IllegalStateException(
                    "Номер заявки отсутствует в контексте — сначала должен " +
                            "отработать AgroReviewStageFlow.completeReview()");
        }

        consultation.closeIfOpened();
        searchFlow.withApplicationNumber(applicationNumber).openByNumber();

        // Документы → вкладка владельца («Документы заемщика»)
        documents.openDocumentsTab();
        documents.openSubTab(ownerData.getOwner().uiTab);

        // Раскрываем «Досье клиента», чтобы карточка документа отрендерилась в DOM
        detailPage.openDetailStable(BORROWER_DOSSIER_DETAIL);
        basePage.waitForPage();

        // Тип→UUID в скоупе контейнера заёмщика (DocBorrowerTab)
        DocumentUploadMap map = new DocumentUploadMap()
                .collect(ownerData.getOwner().containerId);
        log.info("Agro: DocumentUploadMap[{}] size={}",
                ownerData.getOwner(), map.size());

        for (DocumentData doc : ownerData.getDocuments()) {
            String uuid = map.get(doc.getType());
            log.info("  {} → type='{}' file='{}' uuid='{}'",
                    ownerData.getOwner(), doc.getType(), doc.getFileName(), uuid);
            FileUploadApiHelper.uploadFile(doc.getFileName(), uuid);
        }

        // ROUND-TRIP (V4, Агро): число записей документов в контейнере заёмщика ==
        // ожидаемому (1 док — Passport в «Досье клиента»). Деталь раскрыта выше
        // (openDetailStable), контейнер owner.containerId (DocBorrowerTab).
        // Ридер универсален с розницей — считает счётчики «Файлы (N)».
        VerificationCollector.check(
                "Документы — число записей [" + ownerData.getOwner().getUiValue() + "]",
                String.valueOf(ownerData.getDocuments().size()),
                String.valueOf(FieldUtils.getDocumentEntryCount(
                        ownerData.getOwner().containerId)));
        VerificationCollector.flush("Документы (Агро, мидл-контролер)");

        log.info("Agro: middle controller documents attached");
    }
}