package com.creatio.autotest.core.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class Config {

    private static final Logger log = LoggerFactory.getLogger(Config.class);
    private static final Config INSTANCE = new Config();
    private final Properties props = new Properties();

    // ══════════════════════════════════════
    // INIT
    // ══════════════════════════════════════

    private Config() {
        loadProperties();
    }

    public static Config getInstance() {
        return INSTANCE;
    }

    private void loadProperties() {
        try (InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (is == null) {
                throw new RuntimeException("config.properties not found in resources");
            }

            props.load(is);
            log.info("config.properties loaded successfully");

        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    // ══════════════════════════════════════
    // CORE — переопределяется через -D
    // ══════════════════════════════════════

    public String getBaseUrl() {
        return get("base.url");
    }

    public String getBrowser() {
        return get("browser");
    }

    public boolean isHeadless() {
        return Boolean.parseBoolean(get("headless"));
    }

    public long getTimeout() {
        return Long.parseLong(get("timeout"));
    }

    public long getPageLoadTimeout() {
        return Long.parseLong(get("page.load.timeout"));
    }

    // ══════════════════════════════════════
    // CREDENTIALS
    // ══════════════════════════════════════

    public String getAdminLogin() {
        return get("admin.login");
    }

    public String getAdminPassword() {
        return get("admin.password");
    }

    // ══════════════════════════════════════
    // INTERNAL
    // ══════════════════════════════════════

    /**
     * Сначала смотрим System property (-Dkey=value),
     * потом config.properties
     */
    public String get(String key) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.trim().isEmpty()) {
            log.debug("Config override via -D: {}={}", key, systemValue);
            return systemValue;
        }
        String propValue = props.getProperty(key);
        if (propValue == null) {
            throw new RuntimeException("Config key not found: " + key);
        }
        return propValue;
    }
}
