package com.creatio.autotest.testdata.models;

/**
 * Единый источник справочных значений Creatio для формы залога.
 * Аналог CurrencyType — все строки справочника UI живут здесь,
 * Flow-классы их не хардкодят.
 *
 * uiName     — значение, используемое SigningStageFlow.openCollateralByName()
 *              для поиска строки залога в гриде (значения оригинальные, не менялись).
 * category   — значение поля "Тип" в форме залога (справочник Creatio).
 * subType    — значение поля "Подтип" в форме залога (справочник Creatio).
 * condition  — значение поля "Состояние"; null, если для этого типа не заполняется.
 * ownership  — значение поля "Право владения".
 * printForm  — печатная форма договора залога; null, если не печатается.
 */
public enum CollateralType {

    VEHICLE(
            "Транспортное средство",
            "Движимое имущество",
            "Гарави наклиёт",
            "Новое",
            "Собственный",
            "Шартномаи гарави воситаи нақлиёт",
            true
    ),
    GOODS(
            "Товары в обороте",
            "Движимое имущество",
            "Гарави амволи дар муомилотбуда",
            null,
            "Собственный",
            "Шартномаи гарави молхои дар муомилот ва коркард",
            true
    ),
    EQUIPMENT(
            "Оборудование",
            "Движимое имущество",
            "Гарави тачхизот",
            null,
            "Собственный",
            "Шартномаи гарави амволи манкул таxчизот",
            true
    ),
    CASH_DEPOSIT(
            "Гарави пасандоз",
            "Денежные средства (депозитные счета)",
            "Гарави пасандоз",
            null,
            "Третьего лица",
            "Шартномаи гарави пасандози пули",
            true
    ),
    REAL_ESTATE(
            "Недвижимость",
            "Недвижимое имущество",
            "Гарави амволи гайриманкул",
            "Новое",
            "Собственный",
            null,
            false
    ),
    GOLD(
            "Золотые изделия",
            "Движимое имущество",
            "Гарави дорои",
            null,
            "Собственный",
            "Шартномаи гарави дорои",
            true
    ),
    MOVABLE_PROPERTY(
            "Домашнее имущество",
            "Движимое имущество",
            "Гарави амволи манкул",
            null,
            "Собственный",
            null,
            false
    ),
    ACQUIRED_PROPERTY(
            "Приобретаемый товар",
            "Движимое имущество",
            "Моли бадастомада",
            null,
            "Собственный",
            "Шартномаи гарави молу мулки ба даст омада",
            true
    ),
    COTTON(
            "Залог хлопка",
            "Движимое имущество",
            "Гарави пахта",
            null,
            "Собственный",
            "Шартномаи гарави хосили оянда",  //Шартномаи гарави пахта
            true
    ),
    FUTURE_HARVEST(
            "Будущий урожай",
            "Движимое имущество",
            "Гарави хосили оянда",
            null,
            "Собственный",
            "Шартномаи гарави хосили оянда",
            true
    );

    private final String  uiName;
    private final String  category;
    private final String  subType;
    private final String  condition;
    private final String  ownership;
    private final String  printForm;
    private final boolean hasPrintForm;

    CollateralType(String uiName, String category, String subType,
                   String condition, String ownership,
                   String printForm, boolean hasPrintForm) {
        this.uiName       = uiName;
        this.category     = category;
        this.subType      = subType;
        this.condition    = condition;
        this.ownership    = ownership;
        this.printForm    = printForm;
        this.hasPrintForm = hasPrintForm;
    }

    public String  getUiName()      { return uiName; }
    public String  getCategory()    { return category; }
    public String  getSubType()     { return subType; }
    public String  getCondition()   { return condition; }
    public String  getOwnership()   { return ownership; }
    public String  getPrintForm()   { return printForm; }
    public boolean hasPrintForm()   { return hasPrintForm; }


    /** Поиск типа по имени объекта в гриде «Объекты обеспечения» (колонка «Обеспечение»). */
    public static CollateralType fromObjectName(String objectName) {
        String name = objectName.trim();

        // Основной путь — точное совпадение по uiName (для всех типов).
        for (CollateralType type : values()) {
            if (type.uiName.equalsIgnoreCase(name)) {
                return type;
            }
        }

        // Денежные средства: к имени объекта система дописывает номер депозитного
        // счёта («Гарави пасандоз 2022097…»), поэтому точное сравнение не проходит.
        // Депозит опознаём по префиксу имени — что бы ни было дописано после.
        if (name.regionMatches(true, 0,
                CASH_DEPOSIT.uiName, 0, CASH_DEPOSIT.uiName.length())) {
            return CASH_DEPOSIT;
        }

        throw new IllegalStateException(
                "Неизвестный объект обеспечения: '" + objectName + "'");
    }
}