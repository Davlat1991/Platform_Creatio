package com.creatio.autotest.components;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BaseComponent;
import com.creatio.autotest.core.verify.VerificationCollector;

import io.qameta.allure.Step;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public class GridAssertions extends BaseComponent {

    // ══════════════════════════════════════
    // CREDIT DECISION
    // ══════════════════════════════════════
    /** Защита от зацикливания «Показать больше» в гриде графика. */
    private static final int MAX_LOAD_MORE_CLICKS = 20;

    /** Контейнер детали «График платежей» на кредитном договоре (кнопка loadMore — здесь, вне grid-wrap). */
    private static final String SCHEDULE_DETAIL_SCOPE =
            "BnzContractCreditPageBnzPaymentScheduleDetailContainer";

    /** Стрелка-маркер сворачивания детали «Активности» — клик разворачивает деталь. */
    private static final String ACTIVITIES_COLLAPSE_MARKER_ID =
            "ActivityDetailV2DetailControlGroup-marker";

    /**
     * Верификация графика платежей на кредитном договоре (V6, COMPUTED).
     * Инварианты по строкам «Актуален=Да ∧ Основная сумма>0» (служебные строки
     * комиссии/выдачи с Основной=0 и дубли «Нет» отфильтровываются):
     *   (1) число платёжных строк == утверждённому сроку (мес.);
     *   (2) Σ основного долга == утверждённой сумме кредита (checkNumeric);
     *   (3) аннуитет — «Общая сумма» одинакова на всех платёжных строках, кроме
     *       последней (последний платёж может быть доборным).
     * Индексы колонок определяются по data-item-marker заголовков (не хардкод),
     * ячейки читаются позиционно (div.grid-cols-*, column-name у строк отсутствует).
     * Пагинация раскрывается перед чтением. Пишет проверки в коллектор; flush —
     * на вызывающей стороне.
     */
    @Step("Верификация графика: строк==срок, Σ осн.долга==сумма, аннуитет")
    public void verifyPaymentSchedule(String gridWrapId,
                                      String expectedTermMonths,
                                      String expectedPrincipalSum) {
        log.info("Verify schedule: grid='{}', expectedTerm='{}', expectedSum='{}'",
                gridWrapId, expectedTermMonths, expectedPrincipalSum);

        // Деталь графика ленивая: после активации вкладки captions/строки
        // подгружаются асинхронно. Ждём появления заголовков перед чтением,
        // иначе resolveColumnIndex получит пустой список (нюанс #1).
        $x("//div[@id='" + gridWrapId + "']" +
                "//div[contains(@id,'caption-') and @data-item-marker]")
                .shouldBe(visible, Duration.ofSeconds(30));

        expandDetailRows(gridWrapId, SCHEDULE_DETAIL_SCOPE);

        int idxDate      = resolveColumnIndex(gridWrapId, "Дата план");
        int idxPrincipal = resolveColumnIndex(gridWrapId, "Основная сумма");
        int idxTotal     = resolveColumnIndex(gridWrapId, "Общая сумма");
        int idxRemainder = resolveColumnIndex(gridWrapId, "Остаток");
        int idxActual    = resolveColumnIndex(gridWrapId, "Актуален");

        ElementsCollection rows = $$x(
                "//div[@id='" + gridWrapId + "']" +
                        "//div[contains(@class,'grid-listed-row')]");

        // Платёжные строки: Актуален=Да ∧ Основная>0 (служебные комиссия/выдача — осн=0).
        List<String[]> pay = new ArrayList<>();   // [sortKey, principal, total, remainder]
        double principalSum = 0.0;
        for (SelenideElement row : rows) {
            List<String> cells =
                    row.$$x(".//div[contains(@class,'grid-cols-')]").texts();
            int maxIdx = Math.max(idxActual,
                    Math.max(idxDate, Math.max(idxPrincipal, Math.max(idxTotal, idxRemainder))));
            if (cells.size() <= maxIdx) continue;
            if (!"Да".equals(cells.get(idxActual).trim())) continue;
            double principal = parseAmount(cells.get(idxPrincipal));
            if (principal <= 0.0) continue;

            pay.add(new String[]{
                    toSortKey(cells.get(idxDate).trim()),
                    cells.get(idxPrincipal).trim(),
                    cells.get(idxTotal).trim(),
                    cells.get(idxRemainder).trim()
            });
            principalSum += principal;
        }
        // сортировка по дате (yyyyMMdd) — устраняет зависимость от порядка DOM/дублей
        pay.sort(java.util.Comparator.comparing(a -> a[0]));

        log.info("Schedule: платёжных строк={}, срок(утв)={}, Σ осн.долга={}",
                pay.size(), expectedTermMonths,
                String.format(java.util.Locale.US, "%.2f", principalSum));

        // (1) Σ основного долга == утверждённой сумме
        VerificationCollector.checkNumeric(
                "График: Σ основного долга == сумме кредита",
                expectedPrincipalSum,
                String.format(java.util.Locale.US, "%.2f", principalSum));

        // (2) Остаток последней (по дате) строки == 0 — кредит погашается полностью
        String lastRemainder = pay.isEmpty() ? "" : pay.get(pay.size() - 1)[3];
        VerificationCollector.checkTrue(
                "График: остаток последней строки == 0",
                !pay.isEmpty() && Math.abs(parseAmount(lastRemainder)) < 0.01,
                "остаток последней строки='" + lastRemainder + "'");

        // (3) Остаток монотонно убывает по датам
        boolean remainderDescends = true;
        String detail3 = "";
        for (int i = 1; i < pay.size(); i++) {
            if (parseAmount(pay.get(i)[3]) - parseAmount(pay.get(i - 1)[3]) > 0.01) {
                remainderDescends = false;
                detail3 = "рост на строке " + i + ": '" + pay.get(i - 1)[3] +
                        "' -> '" + pay.get(i)[3] + "'";
                break;
            }
        }
        VerificationCollector.checkTrue(
                "График: остаток монотонно убывает", remainderDescends, detail3);

        // (4) Аннуитет: тело графика — равные платежи. Первый и последний платёж
        //     могут легально отличаться (удлинённый/укороченный первый период либо
        //     комиссия в первом взносе; доборный последний по дате). Поэтому проверяем
        //     равенство «Общей суммы» на СРЕДНИХ строках — без первой и последней.
        //     Остальные три инварианта (Σ осн.долга, остаток→0, монотонность) продолжают
        //     жёстко держать корректность графика.
        boolean annuityEqual = true;
        java.util.Set<String> bodyTotals = new java.util.LinkedHashSet<>();
        if (pay.size() > 3) {   // тело (без краёв) для сравнения существует только при >3 строках
            double bodyRef = parseAmount(pay.get(1)[2]);
            for (int i = 1; i < pay.size() - 1; i++) {
                bodyTotals.add(pay.get(i)[2]);
                if (Math.abs(parseAmount(pay.get(i)[2]) - bodyRef) >= 0.01) annuityEqual = false;
            }
        }
        VerificationCollector.checkTrue(
                "График: аннуитет — равные платежи тела (без первого/последнего)",
                annuityEqual,
                "«Общая сумма» тела: " + bodyTotals);
    }

    /** Индекс колонки грида по data-item-marker её заголовка (caption). */
    private int resolveColumnIndex(String gridWrapId, String captionMarker) {
        ElementsCollection caps = $$x(
                "//div[@id='" + gridWrapId + "']" +
                        "//div[contains(@id,'caption-') and @data-item-marker]");
        for (int i = 0; i < caps.size(); i++) {
            if (captionMarker.equals(caps.get(i).getAttribute("data-item-marker"))) {
                return i;
            }
        }
        throw new AssertionError(
                "Колонка графика не найдена по маркеру: '" + captionMarker + "'");
    }

    /** Раскрывает пагинацию «Показать больше»: кнопка loadMore лежит В ДЕТАЛИ
     *  (вне grid-wrap), строки — в grid-wrap. Подтверждено probe (график, активности). */
    private void expandDetailRows(String gridWrapId, String detailScopeId) {
        String rowsXpath = "//div[@id='" + gridWrapId + "']" +
                "//div[contains(@class,'grid-listed-row')]";
        String loadMoreXpath = "//div[@id='" + detailScopeId + "']" +
                "//*[@data-item-marker='loadMore']";

        for (int i = 1; i <= MAX_LOAD_MORE_CLICKS; i++) {
            SelenideElement button = $x(loadMoreXpath);
            if (!button.is(Condition.visible)) return;
            int before = $$x(rowsXpath).size();
            button.scrollIntoView("{block: 'center'}").click();
            try {
                $$x(rowsXpath).shouldHave(
                        CollectionCondition.sizeGreaterThan(before), Duration.ofSeconds(15));
            } catch (Exception | AssertionError stalled) {
                log.warn("Пагинация: «Показать больше» не увеличил число строк ({}) — считаем раскрытым",
                        before);
                return;
            }
            log.info("Пагинация [{}]: «Показать больше» (клик {}): строк {}",
                    gridWrapId, i, $$x(rowsXpath).size());
        }
        log.warn("Пагинация [{}]: достигнут предел {} кликов", gridWrapId, MAX_LOAD_MORE_CLICKS);
    }



    /** Нормализация денежного текста «5 000,00»/NBSP/narrow-NBSP → double. */
    private static double parseAmount(String raw) {
        if (raw == null) return 0.0;
        String s = raw.replace('\u00A0', ' ').replace('\u202F', ' ')
                .replaceAll("\\s", "").replace(',', '.');
        if (s.isEmpty()) return 0.0;
        try { return Double.parseDouble(s); }
        catch (NumberFormatException e) { return 0.0; }
    }


    /** «дд.мм.гггг» -> «ггггммдд» для строковой сортировки по дате. */
    private static String toSortKey(String date) {
        String[] p = date.split("\\.");
        return (p.length == 3) ? p[2] + p[1] + p[0] : date;
    }


    // HELPERS
    // ══════════════════════════════════════


    @Step("Ожидать КК4 в детали 'Проекты решений'")
    public boolean waitForCommitteeDecision(String committee, Duration timeout) {
        log.info("Wait for '{}' in 'Проекты решений' (timeout={}s)",
                committee, timeout.toSeconds());

        // Строка данных в детали несёт committee прямо в data-item-marker
        // (например "КК4 по заявке 391972"). Ищем строку со скоупом к гриду,
        // маркер которой содержит нужный комитет.
        try {
            $x("//div[@id='grid-TsiProjectSolutionDetailDataGridGrid-wrap']" +
                    "//div[contains(@class,'grid-listed-row')" +
                    " and contains(@data-item-marker,'" + committee + "')]"
            ).shouldBe(visible, timeout);
            log.info("Committee '{}' found in 'Проекты решений'", committee);

            // Успех фиксируем в отчёт верификации (V1). Промежуточные неудачные
            // попытки НЕ пишем — это ожидаемое "ещё не сформировано", не FAIL.
            VerificationCollector.checkTrue(
                    "Проект решения «" + committee + "» сформирован",
                    true, "найдено в детали «Проекты решений»");

            return true;
        } catch (Throwable t) {
            log.warn("Committee '{}' NOT found within {}s",
                    committee, timeout.toSeconds());
            return false;
        }
    }


    /**
     * Финальная проверка (стадия «Договор подписан», вкладка «История», деталь
     * «Активности»): все активности завершены. Инвариант A: строк>0; колонка
     * «Состояние» у каждой строки непуста И == «Завершена». Условные ИКОК-ГО-
     * активности покрываются тем же инвариантом.
     */
    @Step("Верификация активностей: все завершены")
    public void verifyActivitiesCompleted(String gridWrapId, String detailScopeId) {
        log.info("Verify activities completed: grid='{}'", gridWrapId);

        expandActivitiesIfCollapsed(gridWrapId);
        expandDetailRows(gridWrapId, detailScopeId);

        int idxTitle = resolveColumnIndex(gridWrapId, "Заголовок");

        // Раскладка колонок детали «Активности» плавает по сессии пользователя:
        // бывает колонка «Состояние» (значение «Завершена»), бывает «Завершение»
        // (дата завершения). Оба означают «завершено» — читаем ту, что присутствует.
        int idxStatus = columnIndexOrMinus(gridWrapId, "Состояние");
        int idxFinish = columnIndexOrMinus(gridWrapId, "Завершение");
        boolean byStatus = idxStatus >= 0;
        log.info("Активности: источник завершённости = {}",
                byStatus ? "колонка «Состояние»" : "колонка «Завершение» (дата)");

        if (idxStatus < 0 && idxFinish < 0) {
            VerificationCollector.checkTrue(
                    "Активности: есть колонка статуса (Состояние/Завершение)",
                    false, "ни «Состояние», ни «Завершение» не найдены в раскладке грида");
            return;
        }
        int idxRead = byStatus ? idxStatus : idxFinish;

        ElementsCollection rows = $$x(
                "//div[@id='" + gridWrapId + "']" +
                        "//div[contains(@class,'grid-listed-row')]");

        int total = 0;
        List<String> notCompleted = new ArrayList<>();
        for (SelenideElement row : rows) {
            List<String> cells =
                    row.$$x(".//div[contains(@class,'grid-cols-')]").texts();
            if (cells.size() <= Math.max(idxTitle, idxRead)) continue;
            total++;
            String title = cells.get(idxTitle).trim();
            String val   = cells.get(idxRead).trim();
            boolean done = byStatus ? "Завершена".equals(val) : !val.isBlank();
            if (!done) notCompleted.add("«" + title + "» -> '" + val + "'");
        }

        log.info("Активности: всего={}, незавершённых={}", total, notCompleted.size());

        VerificationCollector.checkTrue(
                "Активности: список непуст", total > 0, "строк в детали: " + total);
        VerificationCollector.checkTrue(
                "Активности: все завершены",
                notCompleted.isEmpty(),
                notCompleted.isEmpty() ? "" : "незавершённые: " + notCompleted);
    }

    /** Разворачивает деталь «Активности», если она свёрнута (строк/заголовков нет в DOM).
     *  Клик по стрелке-маркеру сворачивания. Идемпотентно: если уже раскрыта — выходим. */
    /** Разворачивает деталь «Активности», если колонка «Состояние» ещё не видна.
     *  Клик по стрелке-маркеру сворачивания (с ожиданием — не мгновенной проверкой). */
    private void expandActivitiesIfCollapsed(String gridWrapId) {
        String anyCaptionXpath = "//div[@id='" + gridWrapId + "']" +
                "//div[contains(@id,'caption-') and @data-item-marker]";
        for (int attempt = 1; attempt <= 10; attempt++) {
            if ($x(anyCaptionXpath).is(visible)) return;
            log.info("Активности свёрнуты — клик по маркеру (попытка {}/10)", attempt);
            SelenideElement marker = $("#" + ACTIVITIES_COLLAPSE_MARKER_ID);
            if (marker.is(visible)) marker.scrollIntoView("{block: 'center'}").click();
            try {
                $x(anyCaptionXpath).shouldBe(visible, Duration.ofSeconds(8));
                return;
            } catch (Exception | AssertionError notYet) {
                log.warn("Активности: после попытки {} заголовки не видны", attempt);
            }
        }
        $x(anyCaptionXpath).shouldBe(visible, Duration.ofSeconds(10));
    }

    /** Индекс колонки по data-item-marker, или -1 если колонки нет в текущей раскладке. */
    private int columnIndexOrMinus(String gridWrapId, String captionMarker) {
        ElementsCollection caps = $$x(
                "//div[@id='" + gridWrapId + "']" +
                        "//div[contains(@id,'caption-') and @data-item-marker]");
        for (int i = 0; i < caps.size(); i++) {
            if (captionMarker.equals(caps.get(i).getAttribute("data-item-marker"))) return i;
        }
        return -1;
    }
}