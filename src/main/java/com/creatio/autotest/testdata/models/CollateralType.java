package com.creatio.autotest.testdata.models;

public enum CollateralType {

    VEHICLE          ("Транспортное средство",
            "Шартномаи гарави воситаи нақлиёт",       true),
    GOODS            ("Товары в обороте",
            "Шартномаи гарави молхои дар муомилот",    true),
    EQUIPMENT        ("Оборудование",
            "Шартномаи гарави амволи манкул таxчизот", true),
    CASH_DEPOSIT     ("Гарави пасандоз",
            "Гарави пасандоз",                         true),
    REAL_ESTATE      ("Недвижимость",
            null,                                       false),
    GOLD             ("Золотые изделия",
            "Шартномаи гарави дорои",                  true),
    MOVABLE_PROPERTY ("Домашнее имущество",
            null,                                       false),
    ACQUIRED_PROPERTY("Приобретаемый товар",
            "Шартномаи гарави молу мулки ба даст омада", true),
    COTTON           ("Будущий урожай",
            "Шартномаи гарави пахта",                  true),
    FUTURE_HARVEST   ("Будущий урожай",
            "Шартномаи гарави хосили оянда",           true);

    private final String  uiName;
    private final String  printForm;
    private final boolean hasPrintForm;

    CollateralType(String uiName, String printForm, boolean hasPrintForm) {
        this.uiName       = uiName;
        this.printForm    = printForm;
        this.hasPrintForm = hasPrintForm;
    }

    public String  getUiName()      { return uiName; }
    public String  getPrintForm()   { return printForm; }
    public boolean hasPrintForm()   { return hasPrintForm; }
}