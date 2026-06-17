package com.creatio.autotest.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class Config {

    private static final Logger log = LoggerFactory.getLogger(Config.class);
    private static final Config INSTANCE = new Config();

    private final Environment environment;
    private final Properties props = new Properties();

    private Config() {
        // 1. Какая среда (по умолчанию TEST)
        this.environment = Environment.from(System.getProperty("env"));
        log.info("Активная среда: {} → {}", environment, environment.baseUrl);

        // 2. Общие настройки (browser/headless/timeout) — без секретов
        load("config.properties");

        // 3. Учётки текущей среды — поверх общих
        load(environment.credentialsFile());
    }

    public static Config getInstance() {
        return INSTANCE;
    }

    public Environment getEnvironment() {
        return environment;
    }

    // ── загрузка (ВАЖНО: явный UTF-8, иначе кириллица = ??????) ──
    private void load(String fileName) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) {
                throw new RuntimeException(fileName + " не найден в resources");
            }
            props.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            log.info("{} загружен", fileName);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить " + fileName, e);
        }
    }

    // ── core ──
    /** URL из Environment; при необходимости переопределить -Dbase.url=... */
    public String getBaseUrl() {
        String override = System.getProperty("base.url");
        return (override != null && !override.isBlank()) ? override : environment.baseUrl;
    }

    public String getBrowser()       { return get("browser"); }
    public boolean isHeadless()      { return Boolean.parseBoolean(get("headless")); }
    public long getTimeout()         { return Long.parseLong(get("timeout")); }
    public long getPageLoadTimeout() { return Long.parseLong(get("page.load.timeout")); }

    // ── generic: сначала -Dkey, потом файлы ──
    public String get(String key) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.trim().isEmpty()) {
            return systemValue;
        }
        String propValue = props.getProperty(key);
        if (propValue == null) {
            throw new RuntimeException("Ключ конфига не найден: " + key);
        }
        return propValue;
    }
}