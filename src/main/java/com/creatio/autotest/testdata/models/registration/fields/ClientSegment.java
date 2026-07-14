package com.creatio.autotest.testdata.models.registration.fields;

/**
 * Сегмент клиента в банке — предустановленное свойство физ.лица,
 * определяющее, под какие продукты может создаваться заявка и кто её ведёт.
 * <p>
 * Соответствие роль→сегмент (предусловие теста):
 *  - RETAIL → ведёт розничный менеджер;
 *  - MICRO  → ведёт кредитный эксперт (наравне с Юр.лицами и организациями).
 * <p>
 * Сегмент НЕ заполняется тестом — он уже проставлен в карточке клиента.
 * Используется только для fail-fast проверки на анкете клиента
 * (поле «Сегмент клиента в банке», data-item-marker='BnzCustomerSegmentInBank').
 */
public enum ClientSegment {

    RETAIL("Розница"),
    MICRO("Микро");

    private final String uiName;

    ClientSegment(String uiName) {
        this.uiName = uiName;
    }

    public String getUiName() {
        return uiName;
    }
}