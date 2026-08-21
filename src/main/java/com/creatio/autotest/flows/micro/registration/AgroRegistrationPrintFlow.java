package com.creatio.autotest.flows.micro.registration;

import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.MenuComponent;
import com.creatio.autotest.components.MessageBoxComponent;
import com.creatio.autotest.components.PrintComponent;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.pages.DetailPage;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Стадия «Оформление» (Агро/Микро) — сохранение и печать анкеты-заявки.
 * <p>
 * Изолированная Микро-копия розничного {@code RegistrationPrintFlow}.
 * Тело содержит только UI-метки/навигацию (значений тест-данных нет),
 * поэтому фабрика здесь не задействована. Общие компоненты
 * ({@link ButtonsComponent}, {@link MenuComponent}, {@link MessageBoxComponent},
 * {@link PrintComponent}, {@link DetailPage}, {@link ClickService}) —
 * из Core, продукт-агностик.
 */
public class AgroRegistrationPrintFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroRegistrationPrintFlow.class);

    private final ButtonsComponent    buttons    = new ButtonsComponent();
    private final MenuComponent       menu       = new MenuComponent();
    private final DetailPage          detailPage = new DetailPage();
    private final ClickService        click      = new ClickService();
    private final MessageBoxComponent msgBox     = new MessageBoxComponent();
    private final PrintComponent      print      = new PrintComponent();

    @Step("Агро: сохранение и печать анкеты-заявки")
    public void printApplication() {
        log.info("Agro: print application");

        click.clickByName("Сохранить");
        // диалог смены типа занятости (может появиться)
        msgBox.confirmIfPresent("Да", "При изменении типа занятости");
        click.clickByName("Закрыть");

        // диалог смены типа занятости (может появиться)
        msgBox.confirmIfPresent("Да", "При изменении типа занятости");
        buttons.clickTabByName("Участники заявки");
        click.clickByName("Заемщик");

        detailPage.openDetailMenu("Участники заявки");
        menu.clickByName("Печать Анкеты-заявки");

        // Ждём завершения скачивания (общий метод PrintComponent)
        print.waitForDownload();

        log.info("Agro: application printed");
    }
}