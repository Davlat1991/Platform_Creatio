package com.creatio.autotest.flows.registration.client;

/**
 * Стратегия заполнения анкеты заёмщика в зависимости от типа занятости.
 * Конкретные реализации дозаполняют специфичные для типа поля поверх общей регистрации.
 */
public abstract class BaseClientFlow {

    public abstract void fill();
}