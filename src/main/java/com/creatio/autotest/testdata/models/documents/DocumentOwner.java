package com.creatio.autotest.testdata.models.documents;

/**
 * Владелец документов на стадии "Сбор документов".
 * uiTab — название вкладки документов в Creatio.
 */
public enum DocumentOwner {

    BORROWER ("Документы заемщика"),
    GUARANTOR("Документы поручителя"),
    PLEDGER  ("Документы залогодателя");

    public final String uiTab;

    DocumentOwner(String uiTab) {
        this.uiTab = uiTab;
    }
}