package com.creatio.autotest.testdata.models.documents;

/**
 * Владелец документов на стадии "Сбор документов".
 * uiTab — название вкладки документов в Creatio.
 */
public enum DocumentOwner {

    BORROWER ("Документы заемщика",      "DocBorrowerTab"),
    GUARANTOR("Документы поручителя",    "DocGuarantorTab"),
    PLEDGER  ("Документы залогодателя",  "DocPledgerTab");

    public final String uiTab;
    public final String containerId;

    DocumentOwner(String uiTab, String containerId) {
        this.uiTab = uiTab;
        this.containerId = containerId;
    }
}