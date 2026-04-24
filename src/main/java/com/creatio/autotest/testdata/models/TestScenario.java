package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TestScenario {

    // ── Пользователи ──────────────────────
    private UserCredentials manager;
    private UserCredentials underwriter;
    private UserCredentials ikok;
    private UserCredentials ikokGo;
    private UserCredentials cashier;

    // ── Клиент ────────────────────────────
    private ContactData contact;

    // ── Продукт ───────────────────────────
    private ProductData product;

    // ── Регистрация ───────────────────────
    private RegistrationData registration;

    // ── Участники ─────────────────────────
    private ParticipantsData participants;
}