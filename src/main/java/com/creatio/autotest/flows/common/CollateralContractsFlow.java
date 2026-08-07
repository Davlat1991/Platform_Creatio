package com.creatio.autotest.flows.common;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.creatio.autotest.components.ButtonsComponent;
import com.creatio.autotest.components.MenuComponent;
import com.creatio.autotest.components.MessageBoxComponent;
import com.creatio.autotest.components.PrintComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.testdata.models.CollateralType;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static com.codeborne.selenide.Selenide.*;

/**
 * Регистрация договоров обеспечения в АБС — общий флоу для розницы, Агро/Микро
 * и юрлиц. Вызывается на открытом кредитном договоре.
 * <p>
 * Обходит деталь «Договоры обеспечения», проваливается в каждый временный
 * договор по ссылке номера и внутри него выполняет «Действия → Создание
 * договора обеспечения».
 * <p>
 * Ветвление по полю «Тип договора» самой карточки, а не по данным сценария —
 * поэтому состав детали (сколько залогов, есть ли поручительство) значения
 * не имеет:
 * <ul>
 *   <li>«Поручительства» — только создание, без печати и должностного лица;</li>
 *   <li>«Договор залога» — тип определяется по имени объекта во вкладке
 *       «Залоговые ценности» ({@link CollateralType#fromObjectName(String)}),
 *       далее печатная форма и должностное лицо, если
 *       {@link CollateralType#hasPrintForm()}.</li>
 * </ul>
 * Определить залог по колонке «Тип залога» нельзя: в справочнике и хлопок,
 * и будущий урожай заведены как «Будущий урожай».
 * <p>
 * Опоры выбраны переносимыми между страницами-владельцами: маркеры схемы
 * детали ({@code BnzPledgeAgreementDetailDetailContainer}), а не id с префиксом
 * конкретной страницы. Строка детали несёт номер договора прямо в
 * {@code data-item-marker}. По заголовку детали не кликаем: он сворачивает
 * деталь и уходит под липкий PageHeaderContainer.
 */
public class CollateralContractsFlow {

    private static final Logger log =
            LoggerFactory.getLogger(CollateralContractsFlow.class);

    /** Деталь «Договоры обеспечения» (маркер схемы — без префикса страницы). */
    private static final String PLEDGE_DETAIL =
            "[data-item-marker='BnzPledgeAgreementDetailDetailContainer']";
    /** Деталь «Объекты обеспечения» на карточке временного договора. */
    private static final String OBJECTS_DETAIL =
            "[data-item-marker='BnzReadonlyFinAppPledgeDetailDetailContainer']";
    /** Строка грида Creatio: и «плиточного», и «списочного». */
    private static final String ROW = " div.grid-active-selectable";
    /** Карточка временного договора обеспечения. */
    private static final String ENSURING_PAGE =
            "//*[@data-item-marker='BnzContractEnsuringPageContainer']";
    /** Скрытый readonly-лукап поля «Тип договора». */
    private static final String TYPE_INPUT =
            "input#BnzContractEnsuringPageTypeLookupEdit-el";

    private static final String PARAMETERS_TAB     = "Параметры договора";
    private static final String PLEDGE_VALUES_TAB  = "Залоговые ценности";
    private static final String TYPE_GUARANTEE     = "Поручительства";
    private static final String CONFIDANT_POSITION = "Сардор дар Идораи амалиёт";
    /** Печатная форма договора поручительства (тип договора, а не вид залога —
     *  поэтому в CollateralType не заводится). */
    private static final String GUARANTEE_PRINT_FORM = "Шартномаи замонат";

    /** Номер договора обеспечения. */
    private static final Pattern CONTRACT_NUMBER = Pattern.compile("\\d{5,10}");

    /** Защита от зацикливания: договоров обеспечения в заявке заведомо меньше. */
    private static final int MAX_CONTRACTS = 20;
    /** Защита от зацикливания «Показать больше» (страниц грида заведомо меньше). */
    private static final int MAX_LOAD_MORE_CLICKS = 10;

    private final BasePage            basePage = new BasePage();
    private final ButtonsComponent    buttons  = new ButtonsComponent();
    private final MenuComponent       menu     = new MenuComponent();
    private final MessageBoxComponent msgBox   = new MessageBoxComponent();
    private final ClickService        click    = new ClickService();
    private final PrintComponent print = new PrintComponent();

    // ══════════════════════════════════════
    // MAIN
    // ══════════════════════════════════════

    @Step("Регистрация договоров обеспечения в АБС")
    public void processAllCollateralContracts() {
        int processed = 0;
        boolean allRegistered = false;

        for (int i = 0; i < MAX_CONTRACTS; i++) {
            openParametersTab();

            List<String> pending = collectContractNumbers();
            log.info("Осталось зарегистрировать: {} → {}", pending.size(), pending);

            if (pending.isEmpty()) {
                allRegistered = true;
                break;
            }

            processContract(pending.get(0));
            processed++;

            // После регистрации номер меняется (370295 → Z-001-000586);
            // деталь обновляется только рефрешем страницы кредитного договора.
            refresh();
            basePage.waitForPage();
        }

        if (processed == 0) {
            throw new IllegalStateException(
                    "Деталь «Договоры обеспечения» пуста — нечего регистрировать");
        }

        // Цикл мог завершиться двумя путями: деталь опустела (все договоры
        // зарегистрированы) ИЛИ исчерпан предохранитель MAX_CONTRACTS. Второй путь
        // раньше молча логировался как успех — теперь это явная ошибка: договоров
        // в заявке больше предела, часть осталась незарегистрированной.
        if (!allRegistered) {
            throw new IllegalStateException(
                    "Достигнут предел MAX_CONTRACTS=" + MAX_CONTRACTS +
                            " (обработано " + processed + "), а деталь «Договоры обеспечения» " +
                            "не опустела — часть договоров не зарегистрирована. " +
                            "Увеличьте предел, если договоров в заявке заведомо больше.");
        }

        log.info("Все договоры обеспечения зарегистрированы: {}", processed);
    }
    // ══════════════════════════════════════
    // COLLECT
    // ══════════════════════════════════════

    /** Номер договора лежит в data-item-marker строки — читаем без разбора текста. */
    private List<String> collectContractNumbers() {
        ElementsCollection rows = expandAllRows();

        Set<String> numbers = new LinkedHashSet<>();
        for (SelenideElement row : rows) {
            String marker = row.getAttribute("data-item-marker");
            if (marker != null && CONTRACT_NUMBER.matcher(marker.trim()).matches()) {
                numbers.add(marker.trim());
            } else {
                log.debug("Строка уже зарегистрирована (маркер '{}') — пропускаем", marker);
            }
        }
        return new ArrayList<>(numbers);
    }


    /**
     * Раскрывает деталь «Договоры обеспечения» полностью.
     * <p>
     * Грид Creatio отдаёт строки страницами (~5), остальные скрыты за кнопкой
     * «Показать больше» (data-item-marker='loadMore'). Без раскрытия
     * collectContractNumbers() видит только первую страницу, и часть договоров
     * не регистрируется (11 договоров → видно 5).
     * <p>
     * Универсально и безопасно: если строк мало и кнопки нет — ни одного клика.
     * Кнопка перерисовывается после каждой подгрузки, поэтому ищем её заново на
     * каждой итерации (защита от stale). Останавливаемся, когда кнопки нет,
     * строки перестали расти, либо достигнут предел кликов.
     *
     * @return ленивая коллекция всех строк детали (уже раскрытых)
     */
    private ElementsCollection expandAllRows() {
        String loadMore = PLEDGE_DETAIL + " [data-item-marker='loadMore']";

        ElementsCollection rows = $$(PLEDGE_DETAIL + ROW)
                .shouldHave(com.codeborne.selenide.CollectionCondition.sizeGreaterThan(0),
                        Duration.ofSeconds(30));

        for (int i = 1; i <= MAX_LOAD_MORE_CLICKS; i++) {
            SelenideElement button = $(loadMore);
            if (!button.is(Condition.visible)) {
                return rows;                       // всё загружено (или строк мало)
            }

            int before = rows.size();
            button.scrollIntoView("{block: 'center'}").click();

            try {
                rows.shouldHave(
                        com.codeborne.selenide.CollectionCondition.sizeGreaterThan(before),
                        Duration.ofSeconds(15));
            } catch (Exception | AssertionError stalled) {
                log.warn("«Показать больше»: число строк не выросло ({}) — считаем деталь раскрытой",
                        before);
                return rows;
            }
            log.info("«Показать больше» (клик {}): строк стало {}", i, rows.size());
        }

        log.warn("«Показать больше»: достигнут предел {} кликов — деталь могла раскрыться не полностью",
                MAX_LOAD_MORE_CLICKS);
        return rows;
    }

    // ══════════════════════════════════════
    // PROCESS ONE
    // ══════════════════════════════════════

    @Step("Договор обеспечения {number}")
    private void processContract(String number) {
        log.info("── Договор обеспечения '{}' ──", number);

        openTempContract(number);

        String contractType = readContractType();
        log.info("Тип договора '{}': '{}'", number, contractType);

        if (TYPE_GUARANTEE.equalsIgnoreCase(contractType)) {
            createContractInAbs();
            printContract(GUARANTEE_PRINT_FORM);

        } else {

            CollateralType type = resolveCollateralType();
            log.info("'{}' — залог: {} (печатная форма: {})",
                    number, type, type.hasPrintForm());

            createContractInAbs();

            if (type.hasPrintForm()) {
                printContract(type.getPrintForm());

            }
        }

        closeTempContract();
        log.info("── Договор '{}' обработан ──", number);
    }

    /** Клик по ссылке номера внутри строки → карточка временного договора. */
    private void openTempContract(String number) {
        SelenideElement link = $x(
                "//*[@data-item-marker='BnzPledgeAgreementDetailDetailContainer']" +
                        "//div[@data-item-marker='" + number + "']" +
                        "//a[@data-column='Number']"
        ).shouldBe(Condition.exist, Duration.ofSeconds(30));

        link.scrollIntoView("{block: 'center'}");
        click.jsClick(link);

        $x(ENSURING_PAGE).shouldBe(Condition.visible, Duration.ofSeconds(30));
        basePage.waitForPage();
    }

    /** «Тип договора» — скрытый readonly-лукап, значение только в атрибуте. */
    private String readContractType() {
        SelenideElement input = $(TYPE_INPUT)
                .should(Condition.exist, Duration.ofSeconds(30));
        input.shouldHave(Condition.attributeMatching("value", ".+"),
                Duration.ofSeconds(30));
        return String.valueOf(input.getAttribute("value")).trim();
    }

    /** Тип залога — по имени объекта в детали «Объекты обеспечения». */
    private CollateralType resolveCollateralType() {
        buttons.clickTabByName(PLEDGE_VALUES_TAB);

        SelenideElement firstRow = $$(OBJECTS_DETAIL + ROW)
                .shouldHave(com.codeborne.selenide.CollectionCondition.sizeGreaterThan(0),
                        Duration.ofSeconds(30))
                .first();

        String objectName = readFirstCellText(firstRow);
        log.info("Объект обеспечения: '{}'", objectName);

        return CollateralType.fromObjectName(objectName);
    }

    /** Первая колонка строки: ссылка, либо текстовая ячейка. */
    private String readFirstCellText(SelenideElement row) {
        ElementsCollection links = row.$$("a").filter(Condition.exist);
        if (!links.isEmpty()) {
            return links.first().getText().trim();
        }
        return row.$$("[grid-data-type='text']")
                .shouldHave(com.codeborne.selenide.CollectionCondition.sizeGreaterThan(0),
                        Duration.ofSeconds(10))
                .first()
                .getText()
                .trim();
    }


    /**
     * Переход на «Параметры договора» с защитой от авто-переключения вкладок.
     * <p>
     * После сохранения (в частности после привязки счёта) Creatio асинхронно
     * возвращает активную вкладку на «Основную информацию» — клик проходит,
     * класс active успевает выставиться, а через мгновение мы снова на чужой
     * вкладке. Поэтому якорь не класс вкладки, а деталь «Договоры обеспечения»:
     * она есть только на нужной вкладке. Проверяем её дважды — до и после
     * waitForPage(), чтобы поймать переброс, случившийся уже после клика.
     */
    private void openParametersTab() {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                buttons.clickTabByName(PARAMETERS_TAB);

                $(PLEDGE_DETAIL).shouldBe(Condition.visible, Duration.ofSeconds(15));
                basePage.waitForPage();
                $(PLEDGE_DETAIL).shouldBe(Condition.visible, Duration.ofSeconds(5));

                log.info("Вкладка «{}» открыта и удержана (попытка {}/3)",
                        PARAMETERS_TAB, attempt);
                return;

            } catch (Exception | AssertionError e) {
                log.warn("Попытка {}/3: вкладка «{}» не удержалась ({}) — повторяем",
                        attempt, PARAMETERS_TAB, e.getClass().getSimpleName());
            }
        }
        throw new AssertionError(
                "Не удалось удержать вкладку «" + PARAMETERS_TAB +
                        "»: Creatio возвращает активную вкладку после сохранения");
    }

    // ══════════════════════════════════════
    // ACTIONS
    // ══════════════════════════════════════

    private void createContractInAbs() {
        click.clickByName("Действия");
        menu.clickByName("Создание договора обеспечения");

        msgBox.shouldSeeModal("Договор успешно создан");
        click.clickByMarker("ОК");
        basePage.waitForPage();
    }

    private void printContract(String formName) {
        log.info("Печатная форма: '{}'", formName);

        Set<String> before = print.snapshotDownloads();

        click.clickByName("Печать");
        $x("//ul[@data-item-marker='PrintButton']" +
                "//li[normalize-space()='" + formName + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .click();

        // Модалка должностного лица сейчас не открывается — форма скачивается
        // сразу. Когда настройки поправят, выбор лица встанет между кликом
        // и ожиданием файла.
        selectConfidantIfShown(CONFIDANT_POSITION);
        print.waitForNewDownload(before, formName);
    }

    /**
     * Выбор должностного лица.
     * <p>
     * По сценарию после выбора печатной формы должно открываться модальное окно
     * выбора должностного лица, и только затем скачиваться файл. Сейчас из-за
     * настроек системы окно не открывается ни для одного вида обеспечения —
     * форма скачивается сразу. Шаг реализован и ждёт грид ограниченно (5 с,
     * чтобы не тратить время на каждом договоре); когда настройки поправят,
     * он заработает без изменений кода. Пропуск фиксируется в логе.
     */
    private void selectConfidantIfShown(String position) {
        try {
            $("[data-item-marker='Должностные лица']")
                    .shouldBe(Condition.visible, Duration.ofSeconds(5));
        } catch (AssertionError noGrid) {
            log.info("Грид «Должностные лица» не появился (ожидаемо) — форма скачана сразу");
            return;
        }

        $x("//span[@grid-data-type='text' and normalize-space()='" + position + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .click();

        $x("//span[text()='Выбрать']")
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .click();

        basePage.waitForPage();
        log.info("Должностное лицо выбрано: '{}'", position);
    }

    /**
     * Возврат на кредитный договор по кнопке «Закрыть» карточки
     * (data-item-marker='CloseButton'). SaveButton на этой странице нет —
     * розничный приём с двумя SaveButton здесь неприменим.
     */
    private void closeTempContract() {
        click.clickByMarker("CloseButton");

        $x(ENSURING_PAGE).should(Condition.disappear, Duration.ofSeconds(30));
        $(PLEDGE_DETAIL).shouldBe(Condition.visible, Duration.ofSeconds(30));
        basePage.waitForPage();
    }


    /**
     * ВРЕМЕННЫЙ отладочный проход: договоры обеспечения уже зарегистрированы
     * (номера Z-001-…), создание в АБС пропускается. Нужен, чтобы проверять
     * печать и закрытие карточки, не заводя заявку заново. Удалить после
     * зелёного основного прогона.
     */
    @Step("DEBUG: печать форм по уже созданным договорам обеспечения")
    public void debugPrintExistingContracts() {
        buttons.clickTabByName(PARAMETERS_TAB);

        List<String> rows = collectAllRowMarkers();
        log.info("Строк в детали: {} → {}", rows.size(), rows);

        for (String number : rows) {
            openTempContract(number);

            String contractType = readContractType();
            log.info("Тип договора '{}': '{}'", number, contractType);

            if (TYPE_GUARANTEE.equalsIgnoreCase(contractType)) {
                printContract(GUARANTEE_PRINT_FORM);
            } else {
                CollateralType type = resolveCollateralType();
                if (type.hasPrintForm()) {
                    printContract(type.getPrintForm());
                } else {
                    log.info("'{}' — {}: печатной формы нет", number, type);
                }
            }

            closeTempContract();
        }

        log.info("DEBUG-проход завершён: обработано {}", rows.size());
    }

    /** Все строки детали, включая уже зарегистрированные (Z-001-…). */
    private List<String> collectAllRowMarkers() {
        ElementsCollection rows = $$(PLEDGE_DETAIL + ROW)
                .shouldHave(com.codeborne.selenide.CollectionCondition.sizeGreaterThan(0),
                        Duration.ofSeconds(30));

        Set<String> numbers = new LinkedHashSet<>();
        for (SelenideElement row : rows) {
            String marker = row.getAttribute("data-item-marker");
            if (marker != null && !marker.isBlank()) {
                numbers.add(marker.trim());
            }
        }
        return new ArrayList<>(numbers);
    }
}