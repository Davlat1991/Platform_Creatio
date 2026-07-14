package com.creatio.autotest.flows.micro;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.testdata.models.registration.fields.ClientSegment;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$x;

/**
 * Проверка сегмента клиента на анкете физ.лица (сразу после «Начать
 * консультацию»).
 * <p>
 * Fail-fast guard: продукты линейки «Микро» доступны только клиентам
 * сегмента «Микро». Если в сценарий по ошибке попал клиент другого сегмента,
 * тест падает сразу с понятной ошибкой — до подбора продукта.
 * <p>
 * Сегмент НЕ заполняется — он предустановлен в карточке клиента; флоу только
 * читает и сверяет поле «Сегмент клиента в банке».
 * <p>
 * Якорь — контейнер значения по стабильному id
 * ({@code ContactPageV2BnzCustomerSegmentInBankContainer_Control}).
 * Label НЕ используется: его data-item-marker содержит код вместе с текстом
 * («BnzCustomerSegmentInBank Сегмент клиента в банке»), что делает точный
 * поиск по marker ненадёжным.
 */
public class MicroClientVerificationFlow {

    private static final Logger log =
            LoggerFactory.getLogger(MicroClientVerificationFlow.class);

    private static final String SEGMENT_CONTROL_ID =
            "ContactPageV2BnzCustomerSegmentInBankContainer_Control";

    @Step("Проверка сегмента клиента: ожидается {expected}")
    public void verifyClientSegment(ClientSegment expected) {
        log.info("Verify client segment: expected='{}'", expected.getUiName());

        // После «Начать консультацию» анкета клиента грузится не мгновенно —
        // ждём появления контейнера поля «Сегмент клиента в банке».
        SelenideElement control = $x("//div[@id='" + SEGMENT_CONTROL_ID + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30));

        String actual = readValue(control);

        if (actual == null || actual.isBlank()) {
            throw new AssertionError(
                    "Не удалось прочитать поле «Сегмент клиента в банке» — значение пустое. " +
                            "Проверьте, что открыта анкета клиента.");
        }

        if (!expected.getUiName().equalsIgnoreCase(actual.trim())) {
            throw new AssertionError(String.format(
                    "Сегмент клиента не соответствует продукту Микро: " +
                            "ожидался «%s», фактический «%s». " +
                            "Убедитесь, что в сценарии выбран клиент сегмента Микро.",
                    expected.getUiName(), actual.trim()));
        }

        log.info("Client segment OK: '{}'", actual.trim());
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

    /**
     * Read-only lookup в Creatio может рендерить значение как в {@code <input>}
     * (value), так и текстом. Пробуем оба источника.
     */
    private String readValue(SelenideElement control) {
        SelenideElement input = control.$x(".//input");
        if (input.exists()) {
            String v = input.getValue();
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return control.getText();
    }
}