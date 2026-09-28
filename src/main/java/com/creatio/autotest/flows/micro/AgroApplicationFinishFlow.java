package com.creatio.autotest.flows.micro;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.components.*;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.flows.common.ApplicationSearchFlow;
import com.creatio.autotest.pages.ConsultationPanelPage;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import com.creatio.autotest.testdata.models.ActivityResult;
import com.creatio.autotest.core.verify.VerificationCollector;
import com.creatio.autotest.utils.FieldUtils;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.util.Set;

import static com.codeborne.selenide.Selenide.*;

/**
 * Завершение заявки Агро/Микро — три финальных шага после кассовых операций:
 * <ol>
 *   <li>прикрепление расходного кассового ордера и закрытие активности
 *       «Прикрепите документы для завершения заявки»;</li>
 *   <li>скачивание ордера под ИКОК-ГО и подтверждение получения документа;</li>
 *   <li>завершение заявки через консультационную панель.</li>
 * </ol>
 * Флоу не зависит от роли: логин и рабочее место выбираются в тесте. Поэтому
 * идущая сейчас доработка процесса Микро, в которой ответственным за
 * прикрепление документа вместо кредитного эксперта становится мидл-контролер,
 * потребует изменения только теста — сам флоу останется прежним.
 * <p>
 * Микро-копия соответствующих методов розничного {@code ApplicationFinishFlow}.
 * Отличие: скачивание проверяется механизмом {@link PrintComponent}
 * (фильтрует промежуточные {@code .crdownload}/{@code .tmp}), а не
 * самописным счётчиком файлов.
 */
public class AgroApplicationFinishFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroApplicationFinishFlow.class);

    private static final String CONTRACT_PAGE_MARKER = "BnzContractCreditPageContainer";
    private static final String FILES_DIR            = "src/test/resources/files/";
    private static final String FILES_TAB            = "Файлы и примечания";

    /** Расходный кассовый ордер — прикрепляется из resources, не из папки загрузок. */
    private static final String ORDER_FILE = "Расходный кассовый ордер.docx";

    private static final String ATTACH_ACTIVITY  = "Прикрепите документы для завершения заявки";
    private static final String ATTACH_RESULT    = ActivityResult.COMPLETED.getUiText();
    private static final String CONFIRM_ACTIVITY = "Подтвердить получение документа";
    private static final String CONFIRM_RESULT   = "Подтвердить получение документа";

    private static final String ACTIVITIES_GRID_WRAP   = "grid-ActivityDetailV2DataGridGrid-wrap";
    private static final String ACTIVITIES_DETAIL_SCOPE = "ActivityDetailV2DetailControlGroup-wrap";

    /** Ссылка «Заявка» на странице кредитного договора (переход на карточку заявки). */
    private static final String APPLICATION_LINK_ID =
            "BnzContractCreditPageFinApplicationLookupEdit-link-el";

    private final BasePage                      basePage     = new BasePage();
    private final ButtonsComponent              buttons      = new ButtonsComponent();
    private final PrintComponent                print        = new PrintComponent();
    private final ConsultationPanelPage         consultation = new ConsultationPanelPage();
    private final ApplicationSearchFlow         searchFlow   = new ApplicationSearchFlow();
    private final ActivityComponent             activity     = new ActivityComponent();
    private final GridAssertions              gridAssertions = new GridAssertions();
    private final CreditApplicationAssertions     assertions = new CreditApplicationAssertions();

    // ══════════════════════════════════════
    // MAIN
    // ══════════════════════════════════════

    /**
     * Шаг 1: прикрепление расходного кассового ордера и закрытие активности.
     * Выполняется под ролью, назначенной ответственной за прикрепление
     * (по текущей доработке — мидл-контролер).
     */
    @Step("Агро: прикрепление ордера и закрытие активности")
    public void completeAttachOrder() {
        openContract();

        buttons.clickTabByName(FILES_TAB);
        attachFile(ORDER_FILE);
        assertions.assertFileInFilesDetail(ORDER_FILE);                              // V7.2
        VerificationCollector.flush("Артефакт — Файлы и ссылки (Агро)");       // V7.2

        activity.completeActivityAwaitingMiniPageClose(ATTACH_ACTIVITY, ATTACH_RESULT);


        log.info("Agro: order attached, activity completed");
    }

    /** Шаг 2: ИКОК-ГО скачивает ордер и подтверждает получение документа. */
    @Step("Агро: подтверждение получения документа (ИКОК-ГО)")
    public void completeIkokGoConfirm() {
        openContract();

        buttons.clickTabByName(FILES_TAB);
        downloadFile(ORDER_FILE);

        // Проверка ордеров пропущена: отображение под ИКОК-ГО не настроено
        // (то же ограничение, что и в розничном ApplicationFinishFlow).
        log.info("Проверка ордеров пропущена: отображение для ИКОК-ГО не настроено");

        activity.completeActivityAwaitingMiniPageClose(CONFIRM_ACTIVITY, CONFIRM_RESULT);

        log.info("Agro: document receipt confirmed");
    }

    /** Шаг 3: завершение заявки через консультационную панель. */
    @Step("Агро: завершение заявки через консультационную панель")
    public void completeApplicationViaConsultation() {
        openContract();

        // V-финал: проверка активностей на заявке (переход по ссылке), затем завершение.
        verifyActivitiesCompleted();

        verifyTerminalStatus();   // V8.2

        consultation.ensureOpened();
        consultation.completeConsultation();



        log.info("Agro: application finished");
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

    /** Открытие кредитного договора по номеру из контекста. */
    private void openContract() {
        String contractNumber =
                ScenarioContextProvider.get().getCreditContractNumber();
        log.info("Agro finish: contract='{}'", contractNumber);

        consultation.closeIfOpened();
        searchFlow.withApplicationNumber(contractNumber).openContractByNumber();

        $x("//*[@data-item-marker='" + CONTRACT_PAGE_MARKER + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30));
    }

    @Step("Прикрепить файл: {fileName}")
    private void attachFile(String fileName) {
        log.info("Attach file to FileDetailV2: '{}'", fileName);

        File file = new File(FILES_DIR + fileName);
        if (!file.exists() || file.length() == 0) {
            throw new IllegalStateException(
                    "Файл отсутствует или пуст: " + file.getAbsolutePath());
        }

        patchFileUploadService();

        $("#FileDetailV2AddRecordButtonButton-fileupload").uploadFile(file);

        // Если сервер отклонил загрузку — Creatio показывает messagebox. Падаем сразу.
        SelenideElement errorBox = $x("//div[contains(@class,'ts-messagebox-caption')" +
                " and contains(normalize-space(.),'Bad Request')]");
        if (errorBox.is(Condition.visible)) {
            throw new AssertionError(
                    "Creatio отклонил загрузку файла '" + fileName + "' (Bad Request)");
        }

        basePage.waitForPage();
        log.info("File attached: '{}'", fileName);
    }

    @Step("Скачать файл: {fileName}")
    private void downloadFile(String fileName) {
        log.info("Download file via row link: '{}'", fileName);
        print.downloadViaRowLink(fileName);
    }

    /**
     * Патч Creatio: программная загрузка по умолчанию уходит в FileApiService
     * (HTTP 400) — переписываем URL на банковский TsiFileApiService.
     */
    private void patchFileUploadService() {
        log.info("Patch XHR upload URL: FileApiService -> TsiFileApiService");
        executeJavaScript(
                "if (!window.__tsiUploadPatched) {" +
                        "  var originalOpen = XMLHttpRequest.prototype.open;" +
                        "  XMLHttpRequest.prototype.open = function(method, url) {" +
                        "    if (typeof url === 'string' && url.indexOf('FileApiService') !== -1) {" +
                        "      url = url.replace(/(?:Tsi)*FileApiService/, 'TsiFileApiService');" +
                        "      arguments[1] = url;" +
                        "    }" +
                        "    return originalOpen.apply(this, arguments);" +
                        "  };" +
                        "  window.__tsiUploadPatched = true;" +
                        "}"
        );
    }

    @Step("Агро финал: проверка, что все активности заявки завершены")
    private void verifyActivitiesCompleted() {
        openApplicationFromContract();
        buttons.clickTabByName("История");
        gridAssertions.verifyActivitiesCompleted(ACTIVITIES_GRID_WRAP, ACTIVITIES_DETAIL_SCOPE);
        VerificationCollector.flush("Активности (Агро)");
    }

    @Step("Агро финал: проверка терминального состояния заявки (до завершения)")
    private void verifyTerminalStatus() {
        log.info("Verify terminal status: состояние «Закрыта» на вкладке «История»");
        VerificationCollector.check("Состояние заявки (терминал)",
                "Закрыта",
                FieldUtils.getValueByMarkerSafe("Status"));
        VerificationCollector.flush("Терминал (Агро)");
    }


    /** Переход со страницы кредитного договора на карточку заявки по ссылке «Заявка». */
    @Step("Открыть заявку по ссылке «Заявка» с договора")
    private void openApplicationFromContract() {
        log.info("Open application via contract link");
        $("#" + APPLICATION_LINK_ID)
                .shouldBe(Condition.visible, Duration.ofSeconds(15))
                .click();
        // Ждём ухода со страницы договора: пока её контейнер в DOM, виден и её
        // собственный таб-бар со своей вкладкой «История» — clickTabByName
        // активировал бы ЕЁ (ложный успех), а деталь «Активности» заявки не нашлась бы.
        $x("//*[@data-item-marker='" + CONTRACT_PAGE_MARKER + "']")
                .shouldBe(Condition.hidden, Duration.ofSeconds(30));
        basePage.waitForPage();
    }

}