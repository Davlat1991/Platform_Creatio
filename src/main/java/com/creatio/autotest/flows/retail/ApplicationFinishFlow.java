package com.creatio.autotest.flows.retail;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.components.ActivityComponent;
import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.CreditApplicationAssertions;
import com.creatio.autotest.components.PrintComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.flows.common.ApplicationSearchFlow;
import com.creatio.autotest.pages.ConsultationPanelPage;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import com.creatio.autotest.testdata.models.ActivityResult;
import com.creatio.autotest.components.GridAssertions;
import com.creatio.autotest.core.verify.VerificationCollector;

import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.util.Set;

import static com.codeborne.selenide.Selenide.*;

public class ApplicationFinishFlow {

    private static final Logger log = LoggerFactory.getLogger(ApplicationFinishFlow.class);
    private static final String CONTRACT_PAGE_MARKER ="BnzContractCreditPageContainer";
    private static final String FILES_DIR = "src/test/resources/files/";
    private static final String ACTIVITIES_GRID_WRAP   = "grid-ActivityDetailV2DataGridGrid-wrap";
    private static final String ACTIVITIES_DETAIL_SCOPE = "ActivityDetailV2DetailControlGroup-wrap";
    /** Ссылка «Заявка» на странице кредитного договора (переход на карточку заявки). */
    private static final String APPLICATION_LINK_ID =
            "BnzContractCreditPageFinApplicationLookupEdit-link-el";




    private final BasePage                    basePage      = new BasePage();
    private final ButtonsComponent            buttons       = new ButtonsComponent();
    private final CreditApplicationAssertions assertions    = new CreditApplicationAssertions();
    private final ConsultationPanelPage       consultation  = new ConsultationPanelPage();
    private final ClickService                click         = new ClickService();
    private final ApplicationSearchFlow       searchFlow    = new ApplicationSearchFlow();
    private final PrintComponent              print         = new PrintComponent();
    private final GridAssertions             gridAssertions = new GridAssertions();
    private final ActivityComponent                activity = new ActivityComponent();

    // ══════════════════════════════════════
    // MAIN
    // ══════════════════════════════════════

    // ══════════════════════════════════════
    // SIMPLIFIED (ИКОК)
    // ══════════════════════════════════════

    @Step("Завершение заявки: маршрут SIMPLIFIED (ИКОК)")
    public void completeSimplifiedFinish() {
        String contractNumber =
                ScenarioContextProvider.get().getCreditContractNumber();
        log.info("Complete simplified finish (ИКОК): contract='{}'", contractNumber);

        consultation.closeIfOpened();

        // 1. Открыть договор по номеру кредитного договора
        searchFlow.withApplicationNumber(contractNumber).openContractByNumber();

        $x("//*[@data-item-marker='" + CONTRACT_PAGE_MARKER + "']")
                .shouldBe(Condition.visible);

        // 2. Операции по договору → проверка ордеров
        buttons.clickTabByName("Операции по договору");
        checkConfirmedIncomingOrder();
        checkConfirmedOutgoingOrder();
        VerificationCollector.flush("Завершение");   // ордера — свой шов, до активностей

        // V-финал: проверка активностей на заявке (переход по ссылке), затем завершение.
        verifyActivitiesCompleted();

        // 3. Завершить заявку через консультационную панель
        finishViaConsultation();
    }

    @Step("Завершить заявку через консультационную панель")
    private void finishViaConsultation() {
        consultation.ensureOpened();
        consultation.completeConsultation();
    }


    @Step("Завершение (ИКОК-ГО): прикрепление документа Розничным менеджером")
    public void completeManagerAttach() {
        String contractNumber =
                ScenarioContextProvider.get().getCreditContractNumber();
        log.info("ИКОК-ГО attach (RM): contract='{}'", contractNumber);

        consultation.closeIfOpened();

        searchFlow.withApplicationNumber(contractNumber).openContractByNumber();
        $x("//*[@data-item-marker='" + CONTRACT_PAGE_MARKER + "']")
                .shouldBe(Condition.visible);


        buttons.clickTabByName("Файлы и примечания");
        attachFile("Расходный кассовый ордер.docx");

        activity.completeActivityWaitingOverlay("Прикрепите документы для завершения заявки", ActivityResult.COMPLETED.getUiText());

    }


    @Step("Завершение (ИКОК-ГО): Подтверждение получения документа")
    public void completeIkokGoFinish() {
        String contractNumber =
                ScenarioContextProvider.get().getCreditContractNumber();
        log.info("ИКОК-ГО finish: contract='{}'", contractNumber);

        consultation.closeIfOpened();

        // 1. Открыть договор по номеру
        searchFlow.withApplicationNumber(contractNumber).openContractByNumber();
        $x("//*[@data-item-marker='" + CONTRACT_PAGE_MARKER + "']")
                .shouldBe(Condition.visible);

        // 2. Файлы и примечания → физическое скачивание документа
        buttons.clickTabByName("Файлы и примечания");
        downloadFile("Расходный кассовый ордер.docx");

        // 3. Операции по договору → проверка ордеров (переиспользуем)
        // Пока что для ИКОК-ГО не настроены появления ордеров но по сценарию они должны быть!
        //buttons.clickTabByName("Операции по договору");
        //checkConfirmedIncomingOrder();
        //checkConfirmedOutgoingOrder();

        // 4. Закрыть активность "Подтвердить получение документа"

        activity.completeActivityWaitingOverlay("Подтвердить получение документа", "Подтвердить получение документа");


    }

    @Step("Завершение (ИКОК-ГО): Розничный менеджер завершает заявку")
    public void completeIkokGoManagerFinish() {
        String contractNumber =
                ScenarioContextProvider.get().getCreditContractNumber();
        log.info("ИКОК-ГО manager finish: contract='{}'", contractNumber);

        consultation.closeIfOpened();

        // 1. Открыть договор по номеру
        searchFlow.withApplicationNumber(contractNumber).openContractByNumber();
        $x("//*[@data-item-marker='" + CONTRACT_PAGE_MARKER + "']")
                .shouldBe(Condition.visible);

        // V-финал: проверка активностей на заявке (переход по ссылке), затем завершение.
        verifyActivitiesCompleted();

        // 2. Завершить заявку через консультационную панель (как у ИКОК)
        finishViaConsultation();
    }

    // ══════════════════════════════════════
    // STEPS
    // ══════════════════════════════════════

    @Step("Проверка приходного ордера (Подтвержден)")
    private void checkConfirmedIncomingOrder() {
        buttons.doubleClickByName("Приходный");
        basePage.checkPage("BnzOrderPageContainer");
        assertions.assertOrderState("Подтвержден");
        click.clickByName("Закрыть");
    }

    @Step("Проверка расходного ордера (Подтвержден)")
    private void checkConfirmedOutgoingOrder() {
        buttons.doubleClickByName("Расходный");
        basePage.checkPage("BnzOrderPageContainer");
        assertions.assertOrderState("Подтвержден");
        click.clickByName("Закрыть");
    }

    @Step("Прикрепить файл '{fileName}' в деталь 'Файлы и ссылки'")
    private void attachFile(String fileName) {
        log.info("Attach file to FileDetailV2: '{}'", fileName);

        File file = new File(FILES_DIR + fileName);
        if (!file.exists() || file.length() == 0) {
            throw new IllegalStateException(
                    "Файл отсутствует или пуст: " + file.getAbsolutePath()
                            + " (size=" + (file.exists() ? file.length() : "нет файла") + ")");
        }

        patchFileUploadService();

        $("#FileDetailV2AddRecordButtonButton-fileupload").uploadFile(file);

        // Если сервер отклонил загрузку — Creatio показывает messagebox. Падаем сразу.
        SelenideElement errorBox = $x("//div[contains(@class,'ts-messagebox-caption')" +
                " and contains(normalize-space(.),'Bad Request')]");
        if (errorBox.is(Condition.visible)) {
            throw new AssertionError(
                    "Creatio отклонил загрузку файла '" + fileName + "' (Bad Request). "
                            + "Проверь валидность файла в src/test/resources/files/");
        }

        $x("//div[@data-item-marker='" + fileName + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30));
        log.info("File attached and visible: '{}'", fileName);
    }


    @Step("Патч Creatio: нормализовать сервис загрузки (FileApiService → TsiFileApiService)")
    private void patchFileUploadService() {
        log.info("Patch XHR upload URL: FileApiService -> TsiFileApiService");
        executeJavaScript(
                "if (!window.__tsiUploadPatched) {" +
                        "  var originalOpen = XMLHttpRequest.prototype.open;" +
                        "  XMLHttpRequest.prototype.open = function(method, url) {" +
                        "    if (typeof url === 'string' && url.indexOf('FileApiService') !== -1) {" +
                        "      url = url.replace(/(?:Tsi)*FileApiService/, 'TsiFileApiService');" +
                        "      arguments[1] = url;" +   // явно, чтобы работало и в strict-режиме
                        "    }" +
                        "    return originalOpen.apply(this, arguments);" +
                        "  };" +
                        "  window.__tsiUploadPatched = true;" +
                        "}"
        );
    }


    @Step("Скачать файл '{fileName}' (клик по ссылке файла)")
    private void downloadFile(String fileName) {
        log.info("Download file via row link: '{}'", fileName);

        Set<String> before = print.snapshotDownloads();

        // Клик по ССЫЛКЕ файла в строке грида — инициирует скачивание в downloads.
        // (SaveToDiscButton — это upload-input, он открывает OS-диалог, поэтому не он.)
        $x("//div[@data-item-marker='" + fileName + "']//a")
                .shouldBe(Condition.visible, Duration.ofSeconds(15))
                .scrollIntoView(false)
                .click();

        print.waitForNewDownload(before, fileName);
        log.info("File downloaded: '{}'", fileName);
    }


    @Step("Финал: проверка, что все активности заявки завершены")
    private void verifyActivitiesCompleted() {
        openApplicationFromContract();
        buttons.clickTabByName("История");
        gridAssertions.verifyActivitiesCompleted(ACTIVITIES_GRID_WRAP, ACTIVITIES_DETAIL_SCOPE);
        VerificationCollector.flush("Активности");
    }

    /** Переход со страницы кредитного договора на карточку заявки по ссылке «Заявка». */
    @Step("Открыть заявку по ссылке «Заявка» с договора")
    private void openApplicationFromContract() {
        log.info("Open application via contract link");
        $("#" + APPLICATION_LINK_ID)
                .shouldBe(Condition.visible, Duration.ofSeconds(15))
                .click();
        basePage.waitForPage();
    }




}