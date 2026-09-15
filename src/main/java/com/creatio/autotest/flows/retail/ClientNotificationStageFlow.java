package com.creatio.autotest.flows.retail;

import com.creatio.autotest.components.DashboardComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.components.MenuComponent;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.flows.common.ApplicationSearchFlow;
import com.creatio.autotest.pages.ConsultationPanelPage;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.core.verify.VerificationCollector;
import com.creatio.autotest.utils.FieldUtils;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.$x;

public class ClientNotificationStageFlow {

    private static final Logger log =
            LoggerFactory.getLogger(ClientNotificationStageFlow.class);

    /** Скоуп утверждённых параметров заявки (подтверждён probe; тот же, что в verifyRoute). */
    private static final String APPROVED_SCOPE  = "TsiApprovedFinApplicationSpecDetailContainer";

    /** Скоуп запрошенных параметров заявки. */
    private static final String REQUESTED_SCOPE = "FinAppSpecificationFieldsDetailContainer";


    private final DashboardComponent    dashboard    = new DashboardComponent();
    private final LookupComponent       lookup       = new LookupComponent();
    private final MenuComponent         menu         = new MenuComponent();
    private final ClickService          click        = new ClickService();
    private final ConsultationPanelPage consultation = new ConsultationPanelPage();
    private final ApplicationSearchFlow searchFlow   = new ApplicationSearchFlow();
    private final ButtonsComponent      buttons      = new ButtonsComponent();

    // ══════════════════════════════════════
    // MAIN — без поиска (SIMPLIFIED: менеджер уже на заявке)
    // ══════════════════════════════════════

    @Step("Client Notification: информирование клиента (без поиска)")
    public void completeClientNotification(String responsiblePerson) {
        log.info("Complete client notification (no search)");

        consultation.closeIfOpened();

        // V6: утверждённые параметры окончательны в начале «Информирования» —
        // читаем и кладём в контекст для проверки графика на «Подписании».
        captureAndVerifyApprovedParams();

        openNotificationMiniPage();
        setClientAgreement();
        assignResponsible(responsiblePerson);
        confirmNotification();

        log.info("Client notification completed");
    }

    // ══════════════════════════════════════
    // OVERLOAD — с поиском заявки (STANDARD: менеджер зашёл заново)
    // ══════════════════════════════════════

    @Step("Client Notification: найти заявку по номеру и информировать клиента")
    public void completeClientNotificationWithSearch(String responsiblePerson) {
        String applicationNumber =
                ScenarioContextProvider.get().getApplicationNumber();
        log.info("Complete client notification with search: number='{}'",
                applicationNumber);

        consultation.closeIfOpened();

        // Менеджер зашёл заново — открываем нужную заявку по номеру из контекста;
        // дальше тело общее с версией без поиска (дедуп).
        searchFlow.withApplicationNumber(applicationNumber).openByNumber();

        completeClientNotification(responsiblePerson);
    }

    // ══════════════════════════════════════
    // STEPS
    // ══════════════════════════════════════

    @Step("Открыть MiniPage 'Информирование клиента'")
    private void openNotificationMiniPage() {
        consultation.closeIfOpened();
        dashboard.clickAndWaitMiniPage(
                "Информирование клиента", "Execute"
        );
    }

    @Step("Установить согласие клиента")
    private void setClientAgreement() {
        $x("//div[@data-item-marker='Result']/input")
                .setValue("Клиент согласен");
        menu.clickByName("Клиент согласен");
        click.clickByMarker("SaveEditButton");
    }

    @Step("Назначить ответственного: {person}")
    private void assignResponsible(String person) {
        lookup.select("Ответственный за подписание", person);
    }

    @Step("Подтвердить информирование")
    private void confirmNotification() {
        click.clickByName("Подтвердить");
    }

    @Step("V6: чтение утверждённых параметров + сверка запрошено/одобрено")
    private void captureAndVerifyApprovedParams() {
        // Вкладка «Выбранный продукт» — там обе детали (запрошенные/утверждённые).
        buttons.clickTabByName("Выбранный продукт");

        String approvedTerm   = FieldUtils.getDetailInputValueScopedSafe(APPROVED_SCOPE,  "Срок, мес.");
        String approvedAmount = FieldUtils.getDetailInputValueScopedSafe(APPROVED_SCOPE,  "Сумма");
        String requestedTerm  = FieldUtils.getDetailInputValueScopedSafe(REQUESTED_SCOPE, "Срок, мес.");
        String requestedAmount= FieldUtils.getDetailInputValueScopedSafe(REQUESTED_SCOPE, "Сумма");

        // Стэш утверждённых — expected для графика на «Подписании».
        ScenarioContextProvider.get().setApprovedTermMonths(approvedTerm);
        ScenarioContextProvider.get().setApprovedAmount(approvedAmount);

        // Пункт B: запрошено vs одобрено. НЕ equals-падение (расхождение легально
        // при «Предложить сумму»/правке верификатора) — только лог + флаг.
        boolean termSame   = !requestedTerm.isBlank()   && requestedTerm.equals(approvedTerm);
        boolean amountSame = !requestedAmount.isBlank() && requestedAmount.equals(approvedAmount);
        log.info("Параметры: запрошено срок='{}'/сумма='{}' | одобрено срок='{}'/сумма='{}' | " +
                        "срок совпал={}, сумма совпала={}",
                requestedTerm, requestedAmount, approvedTerm, approvedAmount, termSame, amountSame);

        // Честный лёгкий чек: утверждённые сформированы и читаемы.
        VerificationCollector.checkNonEmpty("Параметры: утверждённый срок непуст", approvedTerm);
        VerificationCollector.checkNonEmpty("Параметры: утверждённая сумма непуста", approvedAmount);
        VerificationCollector.flush("Параметры заявки");
    }
}