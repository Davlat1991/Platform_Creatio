package com.creatio.autotest.flows;

import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.core.services.WaitService;
import com.creatio.autotest.pages.ConsultationPanelPage;
import com.creatio.autotest.pages.FieldPage;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsultationStartFlow {

    private static final Logger log = LoggerFactory.getLogger(ConsultationStartFlow.class);

    private final ConsultationPanelPage consultationPanel    = new ConsultationPanelPage();
    private final FieldPage fieldPage = new FieldPage();
    private final BasePage  basePage  = new BasePage();
    private final ClickService click = new ClickService();
    private final WaitService wait  = new WaitService();
    private final FieldPage    field = new FieldPage();

    // ══════════════════════════════════════
    // START CONSULTATION
    // ══════════════════════════════════════

    @Step("Старт консультации: тема={consultationThemeDim}")
    public void startConsultation(String consultationThemeDim) {
        log.info("Start consultation: dim={}", consultationThemeDim);

        // 1. Нажать "Начать консультацию"
        click.clickByMarker("Начать консультацию");

        // 2. Выбрать продукт по DIM
        consultationPanel.registerProductByDIM(consultationThemeDim);

        // 3. Ждём загрузки
        basePage.waitForPage();

        // 4. Закрыть консультационную панель
        consultationPanel.closeIfOpened();

        log.info("Consultation started successfully");
    }
}