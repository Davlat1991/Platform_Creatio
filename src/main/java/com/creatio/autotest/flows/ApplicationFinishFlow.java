package com.creatio.autotest.flows;

import com.codeborne.selenide.Condition;
import com.creatio.autotest.components.*;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.pages.ConsultationPanelPage;
import com.creatio.autotest.utils.AllureAttachments;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.codeborne.selenide.Selenide.*;

public class ApplicationFinishFlow {

    private static final Logger log =
            LoggerFactory.getLogger(ApplicationFinishFlow.class);

    private static final String CONTRACT_PAGE_MARKER =
            "BnzContractCreditPageContainer";
    private static final String APPLICATION_PAGE_MARKER =
            "FinApplicationPageContainer";

    private final BasePage                 basePage     = new BasePage();
    private final ButtonsComponent         buttons      = new ButtonsComponent();
    private final DashboardComponent       dashboard    = new DashboardComponent();
    private final MenuComponent            menu         = new MenuComponent();
    private final CreditApplicationAssertions assertions = new CreditApplicationAssertions();
    private final ConsultationPanelPage    consultation = new ConsultationPanelPage();
    private final ClickService click    = new ClickService();

    // ══════════════════════════════════════
    // MAIN
    // ══════════════════════════════════════

    @Step("Завершение выдачи кредита")
    public void completeApplicationFinish() {
        log.info("Complete application finish");

        consultation.closeIfOpened();

        $x("//*[@data-item-marker='" + CONTRACT_PAGE_MARKER + "']")
                .shouldBe(Condition.visible);

        openApplicationFromContract();
        refresh();
        openConfirmReceiptContract();

        buttons.clickTabByName("Операции по договору");

        checkConfirmedIncomingOrder();
        checkConfirmedOutgoingOrder();

        refresh();

        buttons.clickTabByName("Файлы и примечания");
        checkFileUploaded("Справка о доходах.pdf");

        openApplicationFromContract();
        completeConfirmReceiptDocument();
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

    @Step("Проверить файл загружен: {fileName}")
    private void checkFileUploaded(String fileName) {
        log.info("Check file uploaded: '{}'", fileName);
        basePage.waitForPage();
        $x("//div[@data-item-marker='" + fileName + "']")
                .shouldBe(Condition.visible);
        log.info("File found: '{}'", fileName);
    }

    @Step("Открыть заявку из поля 'Заявка'")
    private void openApplicationFromContract() {
        $x("//div[@data-item-marker='FinApplication']//a")
                .shouldBe(Condition.visible)
                .scrollIntoView(false)
                .click();

        $x("//*[@data-item-marker='" + APPLICATION_PAGE_MARKER + "']")
                .shouldBe(Condition.visible);
    }

    @Step("Открыть договор для подтверждения получения документа")
    private void openConfirmReceiptContract() {
        dashboard.clickByName("Подтвердить получение документа");
        $x("//*[@data-item-marker='" + CONTRACT_PAGE_MARKER + "']")
                .shouldBe(Condition.visible);
    }

    @Step("Завершить активность 'Подтвердить получение документа'")
    private void completeConfirmReceiptDocument() {
        dashboard.clickAndWaitMiniPage(
                "Подтвердить получение документа", "Execute"
        );

        $x("//div[@data-item-marker='Result']/input")
                .setValue("Подтвердить получение документа");
        menu.clickByName("Подтвердить получение документа");
        click.clickByMarker("SaveEditButton");
    }
}