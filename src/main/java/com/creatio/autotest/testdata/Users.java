package com.creatio.autotest.testdata;

import com.creatio.autotest.core.config.Config;
import com.creatio.autotest.testdata.models.UserCredentials;
import com.creatio.autotest.testdata.models.UserRole;

public final class Users {

    private static final Config config = Config.getInstance();

    // ══════════════════════════════════════
    // ПОЛЬЗОВАТЕЛИ
    // ══════════════════════════════════════

    public static UserCredentials MANAGER() {
        return build(UserRole.MANAGER);
    }

    public static UserCredentials UNDERWRITER() {
        return build(UserRole.UNDERWRITER);
    }

    public static UserCredentials IKOK() {
        return build(UserRole.IKOK);
    }

    public static UserCredentials IKOK_GO() {
        return build(UserRole.IKOK_GO);
    }

    public static UserCredentials CASHIER() {
        return build(UserRole.CASHIER);
    }

    // ══════════════════════════════════════
    // INTERNAL
    // ══════════════════════════════════════

    private static UserCredentials build(UserRole role) {
        String prefix = role.configPrefix + ".1";   // ← фиксированный юзер №1
        return new UserCredentials(
                role,
                config.get(prefix + ".login"),
                config.get(prefix + ".password")
        );
    }

    private Users() {}
}