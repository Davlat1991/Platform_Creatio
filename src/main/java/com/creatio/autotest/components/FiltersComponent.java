package com.creatio.autotest.components;

import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.core.base.BaseComponent;
import io.qameta.allure.Step;
import org.openqa.selenium.Keys;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

/**
 * Быстрый фильтр раздела (QuickFilterModuleV2).
 * <p>
 * Единственная реализация в проекте. Раньше их было две: дефектная в этом
 * компоненте (не вызывалась ниоткуда) и приватная в {@code ApplicationSearchFlow}.
 *
 * <h3>Три состояния модуля — подтверждены живой разметкой</h3>
 * <table border="1">
 *   <caption>Состояния быстрого фильтра</caption>
 *   <tr><th>Состояние</th><th>Кнопка</th><th>Содержимое модуля</th></tr>
 *   <tr>
 *     <td><b>A. Чисто</b></td>
 *     <td>текстовая, «Фильтры/группы»</td>
 *     <td>только {@code customFilterButtonContainer}</td>
 *   </tr>
 *   <tr>
 *     <td><b>B. Ввод условия</b></td>
 *     <td>текстовая, «Фильтры/группы»</td>
 *     <td>+ {@code simpleFilterEditContainer} с полями
 *         {@code columnEdit} и {@code searchEdit}</td>
 *   </tr>
 *   <tr>
 *     <td><b>C. Фильтр наложен</b></td>
 *     <td><b>иконочная, текста нет</b></td>
 *     <td>+ контейнер условия с метками «Номер / Содержит / значение»
 *         и крестиком {@code filter-remove-button}</td>
 *   </tr>
 * </table>
 * <p>
 * Прежние реализации разбивались об это трижды:
 * <ul>
 *   <li>ожидание «одного из двух состояний» ждало текст «Фильтры/группы» как
 *       признак чистого раздела, но проверяло его раньше, чем раздел применял
 *       фильтр из профиля, — и вставало на 30 с;</li>
 *   <li>версия через {@code simpleFilterEditContainer} путала состояния B и C:
 *       в C этого контейнера нет, метод уходил создавать условие и кликал по
 *       несуществующему тексту;</li>
 *   <li>ни одна не имела якоря готовности, привязанного к загрузке раздела.</li>
 * </ul>
 *
 * <h3>Порядок работы</h3>
 * <ol>
 *   <li>дождаться готовности раздела — грид отрисован, лоадеры ушли;</li>
 *   <li>снять наложенные фильтры крестиками (C → A), если они есть;</li>
 *   <li>открыть редактор условия, если он ещё не открыт (A → B);</li>
 *   <li>заполнить колонку и значение, применить.</li>
 * </ol>
 * Каждый шаг определяет состояние размером коллекции — исключений это не
 * бросает, пустая коллекция есть корректный немедленный ответ. Ожиданий
 * отрицательных условий не осталось ни одного.
 */
public class FiltersComponent extends BaseComponent {

    /** Модуль быстрого фильтра. В DOM может быть несколько — берём видимый. */
    private static final String MODULE_XPATH =
            "//*[contains(@id,'QuickFilterModuleV2Container')]";

    /** Крестик снятия наложенного фильтра. Признак состояния C. */
    private static final String CHIP_XPATH =
            ".//*[contains(@class,'filter-remove-button')]";

    /** Контейнер редактора условия. Признак состояния B. */
    private static final String EDITOR_XPATH =
            ".//*[contains(@id,'simpleFilterEditContainer')]";

    /** Текстовая кнопка меню фильтров. Есть в состояниях A и B, нет в C. */
    private static final String BUTTON_TEXT_XPATH =
            ".//span[normalize-space(.)='Фильтры/группы']";

    private static final String COLUMN_MARKER = "columnEdit";
    private static final String VALUE_MARKER  = "searchEdit";
    private static final String APPLY_MARKER  = "applyButton";

    /**
     * Текст кнопки меню фильтров. Кликается по тексту, не по маркеру: клик по
     * {@code data-item-marker='filterButton'} проверен живьём и не сработал —
     * маркер висит на внешней обёртке, обработчик на внутреннем текстовом span.
     */
    private static final String FILTER_BUTTON_TEXT = "Фильтры/группы";

    /** Пункт меню создания условия. */
    private static final String ADD_CONDITION_ITEM = "Добавить условие";

    /** Предохранитель цикла снятия: столько условий руками никто не ставит. */
    private static final int MAX_FILTERS = 5;

    private static final Duration SECTION_READY = Duration.ofSeconds(30);
    private static final Duration FIELD_READY   = Duration.ofSeconds(15);
    private static final Duration LOADERS_GONE  = Duration.ofSeconds(15);

    private final MenuComponent   menu   = new MenuComponent();
    private final LookupComponent lookup = new LookupComponent();

    // ══════════════════════════════════════
    // ФИЛЬТРАЦИЯ РАЗДЕЛА
    // ══════════════════════════════════════

    /**
     * Накладывает на раздел условие «колонка = значение».
     * <p>
     * Идемпотентен относительно исходного состояния: неважно, чистый раздел,
     * брошенный на полпути редактор или фильтр, оставшийся в профиле
     * пользователя от прошлого прогона.
     *
     * @param sectionGridId id грида раздела, например
     *                      {@code grid-ContractSectionV2DataGridGrid-wrap}.
     *                      Якорь готовности: фильтр из профиля применяется
     *                      вместе с загрузкой грида, до этого момента судить
     *                      о состоянии раздела рано
     * @param column        название колонки, например {@code Номер}
     * @param value         значение фильтра
     */
    @Step("Фильтр раздела: {column} = {value}")
    public void filterBy(String sectionGridId, String column, String value) {
        log.info("Фильтр раздела '{}': {} = {}", sectionGridId, column, value);

        waitSectionReady(sectionGridId);
        removeAppliedFilters();
        openEditorIfNeeded();

        lookup.selectByMarker(COLUMN_MARKER, column);

        // Поле могло остаться заполненным: фильтры хранятся в профиле
        // пользователя и переживают логаут.
        lookup.clearByMarkerIfPresent(VALUE_MARKER);

        setValue(value);
        apply();

        waitLoaders();
        log.info("Фильтр применён: {} = {}", column, value);
    }

    // ══════════════════════════════════════
    // СОСТОЯНИЕ C → A: СНЯТИЕ ФИЛЬТРОВ
    // ══════════════════════════════════════

    /**
     * Снимает все наложенные фильтры крестиками.
     * <p>
     * Элемент перечитывается на каждой итерации: после снятия фильтра Creatio
     * перерисовывает модуль, и закэшированная ссылка даёт
     * {@code StaleElementReferenceException}.
     */
    private void removeAppliedFilters() {
        int count = chips().size();

        if (count == 0) {
            log.info("Наложенных фильтров нет — снимать нечего");
            return;
        }

        for (int attempt = 1; attempt <= MAX_FILTERS && count > 0; attempt++) {
            log.info("Наложенных фильтров: {} — снимаю крестик (итерация {}/{})",
                    count, attempt, MAX_FILTERS);

            SelenideElement chip = chips().first()
                    .shouldBe(Condition.visible, FIELD_READY);

            // JS-клик: нативный физически двигает курсор и оставляет его на
            // элементе, что провоцирует hover-эффекты соседних контролов.
            click.jsClick(chip);

            // Подтверждение: фильтров стало меньше. Без этого промах мимо
            // крестика неотличим от успешного снятия.
            chips().shouldHave(CollectionCondition.size(count - 1), FIELD_READY);
            waitLoaders();

            count = chips().size();
        }

        if (count > 0) {
            throw new AssertionError(
                    "Не удалось снять фильтры раздела за " + MAX_FILTERS +
                            " итераций — осталось: " + count);
        }

        // Пост-условие: раздел вернулся в состояние с текстовой кнопкой.
        // Именно её отсутствие роняло прошлую версию на шаге «Добавить условие».
        filterModule().$$x(BUTTON_TEXT_XPATH)
                .filter(Condition.visible)
                .shouldHave(CollectionCondition.sizeGreaterThan(0), FIELD_READY);

        log.info("Фильтры сняты, кнопка «Фильтры/группы» снова текстовая");
    }

    // ══════════════════════════════════════
    // СОСТОЯНИЕ A → B: ОТКРЫТИЕ РЕДАКТОРА
    // ══════════════════════════════════════

    /**
     * Открывает редактор условия, если он ещё не открыт.
     * <p>
     * Брошенный на полпути редактор (прогон упал между «Добавить условие» и
     * применением) переиспользуется — второе условие поверх первого не
     * создаётся.
     */
    private void openEditorIfNeeded() {
        if (!editors().isEmpty()) {
            log.info("Редактор условия уже открыт — переиспользую");
            return;
        }

        log.info("Открываю меню фильтров → «{}»", ADD_CONDITION_ITEM);
        click.clickByName(FILTER_BUTTON_TEXT);
        menu.clickByName(ADD_CONDITION_ITEM);

        editors().shouldHave(CollectionCondition.sizeGreaterThan(0), FIELD_READY);
    }

    // ══════════════════════════════════════
    // ЗАПОЛНЕНИЕ УСЛОВИЯ
    // ══════════════════════════════════════

    /**
     * Ввод значения: {@code sendKeys + TAB}.
     * {@code setValue()} не поднимает {@code onChange}/{@code onBlur},
     * и фильтр значения не видит.
     */
    private void setValue(String value) {
        SelenideElement input = $x(
                "//*[@data-item-marker='" + VALUE_MARKER + "']//input"
        ).shouldBe(Condition.visible, FIELD_READY);

        input.click();
        input.sendKeys(value);
        input.sendKeys(Keys.TAB);
    }

    private void apply() {
        $x("//*[@data-item-marker='" + APPLY_MARKER + "']")
                .shouldBe(Condition.visible, FIELD_READY)
                .click();
    }

    // ══════════════════════════════════════
    // INTERNAL — СОСТОЯНИЕ
    // ══════════════════════════════════════

    /**
     * Модуль фильтра текущего раздела.
     * Именно видимый: Creatio держит в DOM модули ранее открытых разделов,
     * и невидимая копия перехватила бы поиск.
     */
    private SelenideElement filterModule() {
        return $$x(MODULE_XPATH)
                .filter(Condition.visible)
                .first();
    }

    /** Крестики наложенных фильтров (состояние C). */
    private ElementsCollection chips() {
        return filterModule()
                .$$x(CHIP_XPATH)
                .filter(Condition.visible);
    }

    /** Открытые редакторы условия (состояние B). */
    private ElementsCollection editors() {
        return filterModule()
                .$$x(EDITOR_XPATH)
                .filter(Condition.visible);
    }

    /**
     * Готовность раздела: модуль грида отрисован, модуль фильтра виден,
     * лоадеры ушли.
     * <p>
     * Грид проверяется на {@code exist}, а не на {@code visible}: оставшийся
     * от прошлого прогона чужой фильтр может дать пустую выдачу, и требование
     * видимости завалило бы проверку до того, как дошло дело до снятия фильтра.
     */
    private void waitSectionReady(String sectionGridId) {
        $x("//div[@id='" + sectionGridId + "']")
                .should(Condition.exist, SECTION_READY);

        filterModule().shouldBe(Condition.visible, SECTION_READY);

        waitLoaders();
    }

    /** Ожидание исчезновения лоадеров и масок Creatio. */
    private void waitLoaders() {
        $$(".ts-loader,.mask,.loading-indicator")
                .filter(Condition.visible)
                .shouldHave(CollectionCondition.size(0), LOADERS_GONE);
    }
}