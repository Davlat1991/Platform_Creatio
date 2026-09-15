package com.creatio.autotest.utils;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

public final class FieldUtils {

    private static final Logger log =
            LoggerFactory.getLogger(FieldUtils.class);

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    /** Короткий probe-таймаут для safe-ридеров: read-back после сохранения быстрый. */
    private static final Duration PROBE = Duration.ofSeconds(5);

    private FieldUtils() {}

    // ══════════════════════════════════════
    // GET VALUE BY MARKER
    // ══════════════════════════════════════

    public static String getValueByMarker(String marker) {
        log.info("Get value by marker: '{}'", marker);

        SelenideElement input = $x(
                "//*[@data-item-marker='" + marker + "']//input"
        ).shouldBe(exist, TIMEOUT).shouldBe(visible, TIMEOUT);

        String value = input.getValue();

        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException(
                    "Field marker='" + marker + "' found but value is empty"
            );
        }
        return value;
    }

    // ══════════════════════════════════════
    // GET DETAIL INPUT VALUE (scoped, readonly input)
    // ══════════════════════════════════════

    /**
     * Читает value у readonly-input поля внутри детали со скоупом к контейнеру.
     * Маркер дублируется (label + control + input) — целимся строго в input.
     * Служит якорем "скоринг завершён": ждёт непустого value
     * (до скоринга поле пустое, placeholder="Не заполнено").
     */
    public static String getDetailInputValueScoped(String scopeId, String marker) {
        log.info("Get detail input value scoped: scope='{}', marker='{}'",
                scopeId, marker);

        SelenideElement input = $x(
                "//*[@id='" + scopeId + "']" +
                        "//*[@data-item-marker='" + marker + "']//input"
        ).shouldBe(exist, TIMEOUT)
                .shouldHave(Condition.attributeMatching("value", ".+"), TIMEOUT);

        String value = input.getValue();
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException(
                    "Detail input marker='" + marker +
                            "' is empty in scope '" + scopeId + "'"
            );
        }
        return value.trim();
    }


    /**
     * Safe-версия чтения value по data-item-marker в скоупе контейнера.
     * В отличие от getDetailInputValueScoped НЕ бросает — возвращает "" за
     * probe-таймаут (вердикт выносит VerificationCollector). Нужна для стэша
     * утверждённых параметров на «Информировании»: промах не должен ронять
     * рабочий флоу, только провалить проверку.
     */
    public static String getDetailInputValueScopedSafe(String scopeId, String marker) {
        try {
            String value = $x(
                    "//*[@id='" + scopeId + "']" +
                            "//*[@data-item-marker='" + marker + "']//input"
            ).shouldBe(visible, PROBE).getValue();
            String result = value == null ? "" : value.trim();
            log.info("Safe read scope='{}' marker='{}' -> '{}'", scopeId, marker, result);
            return result;
        } catch (Exception | AssertionError e) {
            log.warn("Safe read scope='{}' marker='{}' not found in {}s -> ''",
                    scopeId, marker, PROBE.toSeconds());
            return "";
        }
    }

    // ══════════════════════════════════════
    // SAFE READ-BACK (для soft-assert: возвращают "" вместо throw)
    // ══════════════════════════════════════

    /**
     * Безопасное чтение value по data-item-marker. Возвращает trimmed value
     * или "" если поле не найдено/пустое за probe-таймаут. Не бросает —
     * вердикт выносит VerificationCollector.
     */
    public static String getValueByMarkerSafe(String marker) {
        try {
            String value = $x("//*[@data-item-marker='" + marker + "']//input")
                    .shouldBe(visible, PROBE)
                    .getValue();
            String result = value == null ? "" : value.trim();
            log.info("Safe read marker='{}' -> '{}'", marker, result);
            return result;
        } catch (Exception | AssertionError e) {
            log.warn("Safe read marker='{}' not found in {}s -> ''",
                    marker, PROBE.toSeconds());
            return "";
        }
    }

    // ══════════════════════════════════════
    // GET VALUE BY LABEL
    // ══════════════════════════════════════

    /**
     * Безопасное чтение value поля по тексту его label.
     * XPath-паттерн подтверждён рабочим кодом (CreditApplicationAssertions.getOrderState):
     * label(text) → ancestor label-wrap → following-sibling control-wrap → input.
     * Возвращает "" если поле не найдено за probe-таймаут. Не бросает.
     */
    public static String getValueByLabelSafe(String label) {
        try {
            String value = $x(
                    "//label[contains(text(),'" + label + "')]" +
                            "/ancestor::div[contains(@class,'label-wrap')]" +
                            "/following-sibling::div[contains(@class,'control-wrap')]" +
                            "//input"
            ).shouldBe(visible, PROBE).getValue();
            String result = value == null ? "" : value.trim();
            log.info("Safe read label='{}' -> '{}'", label, result);
            return result;
        } catch (Exception | AssertionError e) {
            log.warn("Safe read label='{}' not found in {}s -> ''",
                    label, PROBE.toSeconds());
            return "";
        }
    }

    /**
     * Безопасное чтение value поля по label внутри контейнера (детали) со скоупом.
     * Тот же подтверждённый label-wrap паттерн, но ограничен id-скоупом.
     * Возвращает "" если не найдено. Не бросает.
     */
    public static String getDetailValueByLabelSafe(String scopeId, String label) {
        try {
            String value = $x(
                    "//*[@id='" + scopeId + "']" +
                            "//label[contains(text(),'" + label + "')]" +
                            "/ancestor::div[contains(@class,'label-wrap')]" +
                            "/following-sibling::div[contains(@class,'control-wrap')]" +
                            "//input"
            ).shouldBe(visible, PROBE).getValue();
            String result = value == null ? "" : value.trim();
            log.info("Safe read scope='{}' label='{}' -> '{}'",
                    scopeId, label, result);
            return result;
        } catch (Exception | AssertionError e) {
            log.warn("Safe read scope='{}' label='{}' not found in {}s -> ''",
                    scopeId, label, PROBE.toSeconds());
            return "";
        }
    }

    // ══════════════════════════════════════
    // GRID READERS
    // ══════════════════════════════════════

    /**
     * Число строк данных в гриде/детали. Паттерн подтверждён рабочим кодом
     * (GridAssertions): строки данных несут класс 'grid-listed-row' внутри
     * контейнера грида. Не ждёт и не бросает — возвращает 0, если строк нет.
     *
     * @param gridWrapId id контейнера грида, напр. 'grid-...-wrap'
     */
    public static int getGridRowCount(String gridWrapId) {
        int count = $$x(
                "//div[@id='" + gridWrapId + "']" +
                        "//div[contains(@class,'grid-listed-row')]"
        ).size();
        log.info("Grid '{}' row count -> {}", gridWrapId, count);
        return count;
    }

    /**
     * Безопасно читает видимый ТЕКСТ control-wrap по data-item-marker его label.
     * Для lookup-полей, отображающихся ссылкой &lt;a&gt; (input.value пустой) —
     * напр. «Клиент» (ClientInProfile). Возвращает "" если не найдено. Не бросает.
     */
    public static String getTextByLabelMarkerSafe(String labelMarker) {
        try {
            String text = $x(
                    "//label[@data-item-marker='" + labelMarker + "']" +
                            "/ancestor::div[contains(@class,'label-wrap')]" +
                            "/following-sibling::div[contains(@class,'control-wrap')]"
            ).shouldBe(visible, PROBE).getText().trim();
            log.info("Safe read text by label-marker='{}' -> '{}'", labelMarker, text);
            return text;
        } catch (Exception | AssertionError e) {
            log.warn("Safe read text by label-marker='{}' not found in {}s -> ''",
                    labelMarker, PROBE.toSeconds());
            return "";
        }
    }

    /**
     * Безопасно читает текст сохранённой grid-ячейки по column-name, со скоупом
     * к контейнеру детали. Для inline-деталей (доход/расход и т.п.), где после
     * сохранения строки inputs исчезают, а значения лежат в &lt;span&gt; ячеек.
     * Возвращает "" если не найдено. Не бросает.
     */
    public static String getGridCellTextSafe(String container, String columnName) {
        try {
            String text = $x(
                    "//div[@data-item-marker='" + container + "']" +
                            "//div[@column-name='" + columnName + "']//span"
            ).shouldBe(visible, PROBE).getText().trim();
            log.info("Safe read grid cell container='{}' column='{}' -> '{}'",
                    container, columnName, text);
            return text;
        } catch (Exception | AssertionError e) {
            log.warn("Safe read grid cell container='{}' column='{}' not found in {}s -> ''",
                    container, columnName, PROBE.toSeconds());
            return "";
        }
    }

    /**
     * Безопасно проверяет наличие строки данных в гриде, текст которой содержит
     * ВСЕ переданные фрагменты одновременно (например ФИО и роль участника).
     * Строки грида несут класс 'grid-listed-row'; структурированных ячеек
     * (column-name) в гриде участников нет — сверяем по тексту строки.
     * Возвращает false если не найдено. Не бросает.
     *
     * @param gridWrapId id контейнера грида, напр. 'grid-...-wrap'
     * @param fragments  фрагменты текста, все должны присутствовать в одной строке
     */
    public static boolean gridHasRowContainingSafe(String gridWrapId, String... fragments) {
        StringBuilder xpath = new StringBuilder(
                "//div[@id='" + gridWrapId + "']" +
                        "//div[contains(@class,'grid-listed-row')]");
        for (String f : fragments) {
            xpath.append("[contains(., '").append(f).append("')]");
        }
        try {
            boolean found = $x(xpath.toString()).shouldBe(visible, PROBE).exists();
            log.info("Grid '{}' has row containing {} -> {}",
                    gridWrapId, java.util.Arrays.toString(fragments), found);
            return found;
        } catch (Exception | AssertionError e) {
            log.warn("Grid '{}' row containing {} not found in {}s -> false",
                    gridWrapId, java.util.Arrays.toString(fragments), PROBE.toSeconds());
            return false;
        }
    }

    /**
     * Число записей документов в контейнере вкладки роли (V4). Деталь документов
     * рендерится НЕ как grid-listed-row, а как карточки; у каждой записи —
     * счётчик «Файлы (N)» (live-подтверждено). Считаем эти счётчики в скоупе
     * контейнера роли (DocBorrowerTab / DocGuarantorTab / DocPledgerTab).
     * Не ждёт и не бросает — возвращает фактическое число (0, если нет).
     *
     * @param containerId id контейнера вкладки роли (ParticipantRole.containerId)
     */
    public static int getDocumentEntryCount(String containerId) {
        int count = $$x(
                "//div[@id='" + containerId + "']" +
                        "//span[starts-with(normalize-space(.),'Файлы (')]"
        ).size();
        log.info("Document entries in container '{}' -> {}", containerId, count);
        return count;
    }


}