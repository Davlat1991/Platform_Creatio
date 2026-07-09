package com.creatio.autotest.flows.micro.registration;

import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.testdata.models.RegistrationData;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Флоу «Общая информация» стадии «Оформление» для продуктов сегмента «Микро»
 * (первый продукт — «Агро»).
 * <p>
 * Самостоятельный класс в ветке {@code flows.micro}, изолированный от
 * розничного {@code RegistrationGeneralInfoFlow}. Изоляция осознанная:
 * «Микро» — растущая линейка продуктов, и общий класс с розницей создал бы
 * скрытое зацепление (любое розничное изменение полей автоматически потекло
 * бы в Микро). Логика заполнения на данный момент совпадает с розничной
 * (подтверждено: до стадии «Информирование клиента» процесс Агро идентичен
 * рознице), но класс развивается независимо.
 */
public class AgroRegistrationGeneralInfoFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroRegistrationGeneralInfoFlow.class);

    private final LookupComponent lookup = new LookupComponent();

    @Step("Заполнение общей информации регистрации (Агро/Микро)")
    public void fillGeneralInfo(RegistrationData data) {
        log.info("Fill general info (Agro/Micro): region='{}' source='{}'",
                data.getRegion(), data.getInfoSource());

        lookup
                .select("Регион использования кредита", data.getRegion())
                .select("Источник информации",           data.getInfoSource());
    }
}