package com.creatio.autotest.testdata;

import com.creatio.autotest.core.config.Config;
import com.creatio.autotest.testdata.models.UserCredentials;
import com.creatio.autotest.testdata.models.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class UserPool {

    private static final Logger log =
            LoggerFactory.getLogger(UserPool.class);

    private static final Config config = Config.getInstance();

    // Thread-safe счётчики для каждой роли
    private static final ConcurrentHashMap<UserRole, AtomicInteger> counters
            = new ConcurrentHashMap<>();

    private UserPool() {}

    // ══════════════════════════════════════
    // PUBLIC API
    // ══════════════════════════════════════

    public static UserCredentials acquire(UserRole role) {
        int index    = nextIndex(role);
        String prefix = role.configPrefix + "." + index;

        log.info("Acquire user: role={} index={} thread={}",
                role, index, Thread.currentThread().getName());

        return new UserCredentials(
                role,
                config.get(prefix + ".login"),
                config.get(prefix + ".password"),
                config.get(prefix + ".displayName")
        );
    }

    // ══════════════════════════════════════
    // SHORTCUTS
    // ══════════════════════════════════════

    public static UserCredentials manager() {
        return acquire(UserRole.MANAGER);
    }

    public static UserCredentials underwriter() {
        return acquire(UserRole.UNDERWRITER);
    }

    public static UserCredentials ikok() {
        return acquire(UserRole.IKOK);
    }

    public static UserCredentials ikokGo() {
        return acquire(UserRole.IKOK_GO);
    }

    public static UserCredentials cashier() {
        return acquire(UserRole.CASHIER);
    }

    // ══════════════════════════════════════
    // INTERNAL
    // ══════════════════════════════════════

    private static int nextIndex(UserRole role) {
        counters.putIfAbsent(role, new AtomicInteger(0));
        AtomicInteger counter = counters.get(role);

        int max   = getMaxIndex(role);
        int index = counter.incrementAndGet();

        // Round-robin — возвращаемся к 1 если превысили максимум
        if (index > max) {
            counter.set(1);
            index = 1;
        }

        return index;
    }

    private static int getMaxIndex(UserRole role) {
        int max = 1;
        while (true) {
            try {
                config.get(role.configPrefix + "." + (max + 1) + ".login");
                max++;
            } catch (Exception e) {
                return max;
            }
        }
    }
}