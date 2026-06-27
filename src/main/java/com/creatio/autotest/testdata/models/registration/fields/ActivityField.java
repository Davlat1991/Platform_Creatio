package com.creatio.autotest.testdata.models.registration.fields;

/**
 * Поля детали "Деятельность физ. лица".
 * marker = data-item-marker в DOM Creatio.
 * Поля каскадные: SECTOR -> SEGMENT -> SUB_SEGMENT.
 */
public enum ActivityField {

    SECTOR("BnzActivitySector"),
    SEGMENT("BnzActivitySegment"),
    SUB_SEGMENT("BnzActivitySubSegment"),
    SALES_TYPE("BnzSaleType"),
    MARKET("BnzSaleMarket");

    public final String marker;

    ActivityField(String marker) {
        this.marker = marker;
    }
}