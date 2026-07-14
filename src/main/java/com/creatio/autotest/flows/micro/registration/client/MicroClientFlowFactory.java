package com.creatio.autotest.flows.micro.registration.client;

import com.creatio.autotest.testdata.models.registration.ClientProfileData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Фабрика стратегий заполнения анкеты клиента сегмента «Микро».
 * По типу занятости из {@link ClientProfileData} создаёт нужную реализацию
 * {@link MicroBaseClientFlow}.
 * <p>
 * Параллель розничному {@code ClientFlowFactory}, изолированная в ветке
 * {@code flows.micro} — розничная фабрика не затрагивается. Тест остаётся
 * декларативным: тип клиента определяется данными сценария, а не кодом теста.
 * <p>
 * На текущем этапе реализован только {@code SELF_EMPLOYED} (фермер) —
 * единственный актуальный для Агро тип. Остальные типы занятости Микро
 * добавляются по мере появления продуктов, требующих их.
 */
public final class MicroClientFlowFactory {

    private static final Logger log =
            LoggerFactory.getLogger(MicroClientFlowFactory.class);

    private MicroClientFlowFactory() {}

    public static MicroBaseClientFlow from(ClientProfileData profile) {
        if (profile == null || profile.getEmploymentType() == null) {
            throw new IllegalArgumentException(
                    "ClientProfileData / employmentType не заданы в сценарии");
        }

        log.info("MicroClientFlowFactory: создаём флоу для типа {}",
                profile.getEmploymentType());

        switch (profile.getEmploymentType()) {
            case SELF_EMPLOYED:
                return new MicroSelfEmployedClientFlow(profile);
            default:
                throw new UnsupportedOperationException(
                        "Тип клиента Микро пока не реализован: "
                                + profile.getEmploymentType());
        }
    }
}