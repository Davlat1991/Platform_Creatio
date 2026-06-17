package com.creatio.autotest.core.config;

/**
 * Среды тестирования. Здесь и только здесь живут URL стендов.
 */
public enum Environment {

    TEST    ("test",    "http://10.10.202.254"),
    PRE_PROD("preprod", "http://10.10.202.245");

    public final String code;     // что передаём в -Denv
    public final String baseUrl;  // URL стенда

    Environment(String code, String baseUrl) {
        this.code = code;
        this.baseUrl = baseUrl;
    }

    /** Файл с учётками этой среды: config.test.properties / config.preprod.properties */
    public String credentialsFile() {
        return "config." + code + ".properties";
    }

    /** Разбор -Denv. Пусто → TEST по умолчанию. */
    public static Environment from(String code) {
        if (code == null || code.isBlank()) {
            return TEST;
        }
        for (Environment env : values()) {
            if (env.code.equalsIgnoreCase(code.trim())) {
                return env;
            }
        }
        throw new IllegalArgumentException(
                "Неизвестная среда: '" + code + "'. Допустимо: test, preprod");
    }
}