package com.creatio.autotest.core.verify;

import com.creatio.autotest.utils.AllureAttachments;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ThreadLocal soft-assert аккумулятор верификации (инициатива Verification, V0).
 *
 * <p>Внутри стадии копит именованные проверки, НЕ бросая на первом несовпадении —
 * один сбойный чек не должен убивать 20-минутный E2E и прятать остальные проверки.
 * На границе стадии {@link #flush(String)} даёт вердикт: либо log.info «N/N пройдено»
 * + текстовый аттач в Allure, либо один AssertionError с полным отчётом по каждому полю.
 *
 * <p>Параллель-безопасен: список чеков — ThreadLocal (как ScenarioContextProvider),
 * без общего мутабельного статик-состояния (правило #4). flush() всегда очищает
 * список текущего потока, чтобы следующая стадия начиналась чистой.
 *
 * <p>Использование:
 * <pre>
 *   VerificationCollector.check("Номер заявки", expected, actual);
 *   VerificationCollector.checkNonEmpty("Скор-балл", scoreValue);
 *   VerificationCollector.flush("Рассмотрение");   // вердикт по стадии
 * </pre>
 */
public final class VerificationCollector {

    private static final Logger log =
            LoggerFactory.getLogger(VerificationCollector.class);

    private static final ThreadLocal<List<Check>> holder =
            ThreadLocal.withInitial(ArrayList::new);

    private VerificationCollector() {}

    /** Одна проверка: expected vs actual (сравнение по trimmed-строкам). */
    private record Check(String name, String expected, String actual, boolean passed) {}

    // ══════════════════════════════════════
    // CHECKS (копят, не бросают)
    // ══════════════════════════════════════

    /** Сверка ожидаемого и фактического значений. */
    public static void check(String name, Object expected, Object actual) {
        String exp = normalize(expected);
        String act = normalize(actual);
        boolean passed = Objects.equals(exp, act);

        holder.get().add(new Check(name, exp, act, passed));
        log.info("Verify [{}] expected='{}' actual='{}' -> {}",
                name, exp, act, passed ? "PASS" : "FAIL");
    }

    /** Проверка условия (для нетекстовых инвариантов: размер грида, наличие строки и т.п.). */
    public static void checkTrue(String name, boolean condition, String detail) {
        holder.get().add(new Check(name, "true", condition + " (" + detail + ")", condition));
        log.info("Verify [{}] condition -> {} ({})",
                name, condition ? "PASS" : "FAIL", detail);
    }

    /** Проверка, что значение непустое (для вычисляемых полей: скор-балл, оценка залога). */
    public static void checkNonEmpty(String name, Object actual) {
        String act = normalize(actual);
        boolean passed = !act.isEmpty();

        holder.get().add(new Check(name, "<непустое>", act, passed));
        log.info("Verify [{}] non-empty actual='{}' -> {}",
                name, act, passed ? "PASS" : "FAIL");
    }

    // ══════════════════════════════════════
    // FLUSH (вердикт по стадии)
    // ══════════════════════════════════════

    @Step("Верификация стадии «{stage}»")
    public static void flush(String stage) {
        List<Check> checks = holder.get();
        int total = checks.size();
        long failed = checks.stream().filter(c -> !c.passed()).count();

        String report = buildReport(stage, checks);
        AllureAttachments.attachText("Верификация: " + stage, report);

        // Очищаем список потока ДО возможного throw — следующая стадия стартует чистой.
        holder.remove();

        if (failed > 0) {
            log.error("Stage '{}' verification: {}/{} FAILED", stage, failed, total);
            throw new AssertionError(report);
        }
        log.info("Stage '{}' verification: {}/{} passed", stage, total, total);
    }

    /** Сброс чеков текущего потока без вердикта (страховка в tearDown). */
    public static void reset() {
        holder.remove();
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

    private static String buildReport(String stage, List<Check> checks) {
        StringBuilder sb = new StringBuilder();
        sb.append("Верификация стадии: ").append(stage).append('\n');
        long failed = checks.stream().filter(c -> !c.passed()).count();
        sb.append("Итог: ").append(checks.size() - failed).append('/')
                .append(checks.size()).append(" пройдено");
        if (failed > 0) sb.append(", ").append(failed).append(" НЕ пройдено");
        sb.append("\n\n");
        for (Check c : checks) {
            sb.append(c.passed() ? "[PASS] " : "[FAIL] ").append(c.name());
            if (!c.passed()) {
                sb.append("\n        ожидалось: '").append(c.expected()).append('\'')
                        .append("\n        фактически: '").append(c.actual()).append('\'');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private static String normalize(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}