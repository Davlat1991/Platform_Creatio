package com.creatio.autotest.flows.micro.registration;

import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.MenuComponent;
import com.creatio.autotest.core.services.ClickService;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Флоу «Участники заявки» стадии «Оформление» для продуктов сегмента «Микро».
 * <p>
 * Самостоятельный класс в ветке {@code flows.micro.registration}, изолированный
 * от розничного {@code RegistrationParticipantsFlow}. Логика добавления
 * заёмщика идентична рознице (подтверждено: id кнопки «+» тот же).
 */
public class AgroRegistrationParticipantsFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroRegistrationParticipantsFlow.class);

    private final ButtonsComponent buttons = new ButtonsComponent();
    private final MenuComponent    menu    = new MenuComponent();
    private final ClickService     click   = new ClickService();

    /**
     * Открывает вкладку «Участники заявки», нажимает «+» и выбирает «Заемщик»
     * из меню — после чего открывается анкета заёмщика.
     */
    @Step("Добавление заемщика (Агро/Микро)")
    public void addBorrower() {
        log.info("Add borrower (Agro/Micro)");

        buttons.clickTabByName("Участники заявки");
        click.clickById("BnzVwFinApplicationAllParticipantDetailAddTypedRecordButtonButton-imageEl");
        menu.clickByName("Заемщик");
    }
}