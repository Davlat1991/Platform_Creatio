package com.creatio.autotest.flows.micro.registration;

import com.creatio.autotest.components.LookupComponent;
import com.creatio.autotest.core.verify.VerificationCollector;
import com.creatio.autotest.testdata.models.RegistrationData;
import com.creatio.autotest.utils.FieldUtils;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Флоу «Общая информация» стадии «Оформление» для продуктов сегмента «Микро»
 * (первый продукт — «Агро»).
 * <p>
 * Самостоятельный класс в ветке {@code flows.micro}, изолированный от
 * розничного {@code RegistrationGeneralInfoFlow}. Отличие от розницы:
 * дополнительное обязательное поле «Сектор экономики» (для Агро).
 */
public class AgroRegistrationGeneralInfoFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroRegistrationGeneralInfoFlow.class);

    private final LookupComponent lookup = new LookupComponent();

    /**
     * @param data           общие данные регистрации (регион, источник)
     * @param economicSector значение поля «Сектор экономики» (Агро-специфично,
     *                       передаётся отдельно, чтобы не тащить его в
     *                       розничную {@link RegistrationData})
     */
    @Step("Заполнение общей информации регистрации (Агро/Микро)")
    public void fillGeneralInfo(RegistrationData data, String economicSector, String OKONH) {
        log.info("Fill general info (Agro/Micro): region='{}' source='{}' sector='{}'",
                data.getRegion(), data.getInfoSource(), economicSector);

        lookup
                .select("Регион использования кредита",  data.getRegion())
                .select("Источник информации",           data.getInfoSource())
                .select("Сектор экономики",              economicSector)
                .select("ОКОНХ",                         OKONH);

        verifyGeneralInfoRoundTrip(data, economicSector, OKONH);
    }

    /**
     * ROUND-TRIP общей информации (V3-Агро). Все 4 поля читаются по лейблу
     * (getValueByLabelSafe), подтверждено live-прогоном: Регион, Источник,
     * Сектор экономики, ОКОНХ. flush — на уровне маршрута (MicroSegmentRoute),
     * чтобы собрать всю стадию в один отчёт.
     */
    @Step("Round-trip общей информации (Агро/Микро)")
    private void verifyGeneralInfoRoundTrip(RegistrationData data,
                                            String economicSector, String OKONH) {
        VerificationCollector.check("Регион использования кредита",
                data.getRegion(),
                FieldUtils.getValueByLabelSafe("Регион использования кредита"));
        VerificationCollector.check("Источник информации",
                data.getInfoSource(),
                FieldUtils.getValueByLabelSafe("Источник информации"));

        // Сектор экономики / ОКОНХ — читаемость по лейблу подтверждена live-прогоном
        // (getValueByLabelSafe возвращает значение целиком, включая длинный ОКОНХ).
        VerificationCollector.check("Сектор экономики",
                economicSector,
                FieldUtils.getValueByLabelSafe("Сектор экономики"));
        VerificationCollector.check("ОКОНХ",
                OKONH,
                FieldUtils.getValueByLabelSafe("ОКОНХ"));
    }
}