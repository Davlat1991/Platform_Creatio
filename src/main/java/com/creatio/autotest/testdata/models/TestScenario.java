package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TestScenario {

    // ── Пользователи ──────────────────────
    private UserCredentials manager;
    private UserCredentials creditExpert;
    private UserCredentials middleController;
    private UserCredentials underwriter;
    private UserCredentials ikok;
    private UserCredentials ikokGo;
    private UserCredentials cashier;

    // ── Клиент ────────────────────────────
    private ContactData contact;

    // ── Продукт ───────────────────────────
    private ProductData product;

    // ── Маршрут и ожидаемое решение скоринга ──
    private CreditRoute route;
    private ScoringDecision expectedScoringDecision;

    // ── Регистрация ───────────────────────
    private RegistrationData registration;

    // ── Участники заявки ──────────────────
    private List<ParticipantData> participants;  // пустой список = нет участников

    // ── Залоговые обеспечения ─────────────
    private List<CollateralData> collaterals;    // пустой список = нет залогов
}