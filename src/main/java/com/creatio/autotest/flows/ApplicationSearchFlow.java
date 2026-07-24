package com.creatio.autotest.flows;

import com.creatio.autotest.components.FiltersComponent;
import com.creatio.autotest.components.GridComponent;
import com.creatio.autotest.core.services.ClickService;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Открытие записи раздела по номеру через фильтр.
 * <p>
 * Вся работа с фильтром вынесена в {@link FiltersComponent} — приватные
 * {@code removeFilterIfExists()} и {@code applyNumberFilter()} удалены как
 * дубли, вместе с закомментированными старыми версиями. Флоу остался тем,
 * чем должен быть: последовательностью шагов.
 * <p>
 * Публичный контракт не изменился: {@code withApplicationNumber()},
 * {@code openByNumber()}, {@code openContractByNumber()} — как были.
 * Одиннадцать зависимых флоу (розница и Агро) правок не требуют.
 */
public class ApplicationSearchFlow {

    private static final Logger log =
            LoggerFactory.getLogger(ApplicationSearchFlow.class);

    /** Грид раздела «Заявки». Служит якорем готовности раздела для фильтра. */
    private static final String APPLICATIONS_GRID =
            "grid-FinApplicationSectionDataGridGrid-wrap";

    /** Грид раздела «Договоры». */
    private static final String CONTRACTS_GRID =
            "grid-ContractSectionV2DataGridGrid-wrap";

    /** Колонка, по которой строится условие фильтра. */
    private static final String NUMBER_COLUMN = "Номер";

    private final GridComponent    grid    = new GridComponent();
    private final ClickService     click   = new ClickService();
    private final FiltersComponent filters = new FiltersComponent();

    // Сохранённый номер — передаётся снаружи
    private String savedApplicationNumber;

    public ApplicationSearchFlow withApplicationNumber(String number) {
        this.savedApplicationNumber = number;
        return this;
    }

    // ══════════════════════════════════════
    // OPEN BY NUMBER
    // ══════════════════════════════════════

    @Step("Открыть заявку по номеру: {savedApplicationNumber}")
    public void openByNumber() {
        log.info("Открытие заявки по номеру: '{}'", savedApplicationNumber);

        click.clickByMarker("Заявки");
        openByNumberInSection(APPLICATIONS_GRID);
    }

    // ══════════════════════════════════════
    // OPEN BY CONTRACT NUMBER
    // ══════════════════════════════════════

    @Step("Открыть договор по номеру: {savedApplicationNumber}")
    public void openContractByNumber() {
        log.info("Открытие договора по номеру: '{}'", savedApplicationNumber);

        click.clickByMarker("Договоры");
        openByNumberInSection(CONTRACTS_GRID);
    }

    // ══════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════

    /**
     * Общий сценарий для обоих разделов: фильтр по номеру и открытие найденной
     * строки. Разделы отличаются только id грида — раньше это были два почти
     * одинаковых метода.
     */
    private void openByNumberInSection(String sectionGridId) {
        filters.filterBy(sectionGridId, NUMBER_COLUMN, savedApplicationNumber);

        grid.clickFirstRowContaining(sectionGridId, savedApplicationNumber);

        click.clickByMarker("Открыть");
    }
}