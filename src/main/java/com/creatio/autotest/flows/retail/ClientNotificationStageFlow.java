package com.creatio.autotest.flows.retail;

import com.creatio.autotest.components.DashboardComponent;
import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.components.MenuComponent;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.flows.common.ApplicationSearchFlow;
import com.creatio.autotest.pages.ConsultationPanelPage;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.$x;

public class ClientNotificationStageFlow {

    private static final Logger log =
            LoggerFactory.getLogger(ClientNotificationStageFlow.class);

    private final DashboardComponent    dashboard    = new DashboardComponent();
    private final LookupComponent       lookup       = new LookupComponent();
    private final MenuComponent         menu         = new MenuComponent();
    private final ClickService          click        = new ClickService();
    private final ConsultationPanelPage consultation = new ConsultationPanelPage();
    private final ApplicationSearchFlow searchFlow = new ApplicationSearchFlow();

    // ══════════════════════════════════════
    // MAIN — без поиска (SIMPLIFIED: менеджер уже на заявке)
    // ══════════════════════════════════════

    @Step("Client Notification: информирование клиента (без поиска)")
    public void completeClientNotification(String responsiblePerson) {
        log.info("Complete client notification (no search)");

        consultation.closeIfOpened();

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

        // Менеджер зашёл заново — открываем нужную заявку по номеру из контекста.
        searchFlow.withApplicationNumber(applicationNumber).openByNumber();

        openNotificationMiniPage();
        setClientAgreement();
        assignResponsible(responsiblePerson);
        confirmNotification();

        log.info("Client notification completed");
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
}