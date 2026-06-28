package com.creatio.autotest.testdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ThreadLocal-провайдер runtime-контекста сценария.
 * Каждый поток (параллельный тест) имеет собственный ScenarioContext —
 * номера заявок/договоров/счетов не пересекаются между потоками.
 *
 * Паттерн повторяет DriverProvider: init() в setUp, remove() в tearDown.
 */
public final class ScenarioContextProvider {

    private static final Logger log =
            LoggerFactory.getLogger(ScenarioContextProvider.class);

    private static final ThreadLocal<ScenarioContext> holder = new ThreadLocal<>();

    private ScenarioContextProvider() {}

    // ══════════════════════════════════════
    // INIT / GET / REMOVE
    // ══════════════════════════════════════

    /** Создаёт свежий контекст для текущего потока. Вызывать в setUp. */
    public static void init() {
        holder.set(new ScenarioContext());
        log.info("ScenarioContext initialized for thread: {}",
                Thread.currentThread().getName());
    }

    /** Контекст текущего потока. Бросает, если init() не вызывался. */
    public static ScenarioContext get() {
        ScenarioContext context = holder.get();
        if (context == null) {
            throw new IllegalStateException(
                    "ScenarioContext not initialized. " +
                            "Call ScenarioContextProvider.init() first (в BaseTest.setUp)."
            );
        }
        return context;
    }

    /** Очищает контекст текущего потока. Вызывать в tearDown. */
    public static void remove() {
        holder.remove();
        log.info("ScenarioContext removed for thread: {}",
                Thread.currentThread().getName());
    }

    public static boolean isInitialized() {
        return holder.get() != null;
    }
}