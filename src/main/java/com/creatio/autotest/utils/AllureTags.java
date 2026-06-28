package com.creatio.autotest.utils;

/**
 * Единый источник имён Allure Epic/Feature — без хардкода строк по тестам.
 * Аналог ScenarioFactory, но для меток отчёта.
 * Значения — compile-time константы, поэтому подставляются прямо в аннотации.
 */
public final class AllureTags {

    private AllureTags() {}

    public static final class Epics {
        public static final String PLATFORM = "Платформа и доступ";
        public static final String RETAIL   = "Розничный кредит — Happy Path";
        public static final String AGRO     = "Агро/Микро (Q3)";
        private Epics() {}
    }

    public static final class Features {
        public static final String AUTH           = "Аутентификация";
        public static final String PRODUCT_SELECT = "Подбор продукта";
        public static final String APP_CREATION   = "Создание заявки";
        public static final String REGISTRATION   = "Регистрация";
        public static final String CLIENT_TYPE    = "Тип клиента";
        public static final String PARTICIPANTS   = "Участники";
        public static final String COLLATERAL     = "Залоги";
        public static final String DOCUMENTS      = "Сбор документов";
        public static final String E2E            = "E2E сценарий";
        private Features() {}
    }
}