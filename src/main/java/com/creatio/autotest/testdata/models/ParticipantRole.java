package com.creatio.autotest.testdata.models;

/**
 * Роль участника заявки. Помимо UI-значения роли ({@code uiValue}) несёт
 * координаты вкладки документов участника на стадии «Сбор документов»:
 * {@code uiTab} — название вкладки, {@code containerId} — id её контейнера.
 * Прежде это жило в отдельном enum DocumentOwner (одно понятие в двух
 * перечислениях); слито сюда в Ш15.
 */
public enum ParticipantRole {

    BORROWER  ("Заемщик",      "Документы заемщика",     "DocBorrowerTab"),
    GUARANTOR ("Поручитель",   "Документы поручителя",   "DocGuarantorTab"),
    PLEDGER   ("Залогодатель", "Документы залогодателя", "DocPledgerTab");

    private final String uiValue;
    public final  String uiTab;
    public final  String containerId;

    ParticipantRole(String uiValue, String uiTab, String containerId) {
        this.uiValue     = uiValue;
        this.uiTab       = uiTab;
        this.containerId = containerId;
    }

    public String getUiValue() {
        return uiValue;
    }
}