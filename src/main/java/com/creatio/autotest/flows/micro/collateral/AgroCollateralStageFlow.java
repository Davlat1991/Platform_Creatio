package com.creatio.autotest.flows.micro.collateral;

import com.codeborne.selenide.Condition;
import com.creatio.autotest.components.DashboardComponent;
import com.creatio.autotest.components.MenuComponent;
import com.creatio.autotest.components.collateral.CollateralTabsComponent;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.core.verify.VerificationCollector;
import com.creatio.autotest.utils.FieldUtils;
import com.creatio.autotest.testdata.models.CollateralData;
import com.creatio.autotest.testdata.models.CollateralType;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.creatio.autotest.testdata.models.ActivityResult;

import java.time.Duration;
import java.util.List;

import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

/**
 * Стадия «Обеспечение» (Агро/Микро) — цельный процесс: заполнение залогов
 * заявки и завершение стадии с переходом на «Сбор документов».
 * <p>
 * Изолированная Микро-копия розничного {@code CollateralStageFlow}.
 * Поддерживаемые для Агро типы залога — «Хлопок» и «Будущий урожай»
 * (данные: {@code CollateralDataFactory.agroCotton()/agroFutureHarvest()}).
 * Прочие типы намеренно не подключены: маршрут Агро их не использует, при
 * появлении — добавляются отдельными Микро-флоу.
 */
public class AgroCollateralStageFlow {

    private static final Logger log =
            LoggerFactory.getLogger(AgroCollateralStageFlow.class);

    /** Активность стадии «Обеспечение». */
    private static final String COLLATERAL_ACTIVITY = "Заполните данные обеспечения и поручительства";

    /** Грид «Залоги» на вкладке «Обеспечение» (общесистемный, обе линейки). */
    private static final String PLEDGE_GRID_WRAP =
            "grid-FinAppPledgeDetailDataGridGrid-wrap";

    /** Результат выполнения активности (happy-path). */
    private static final String ACTIVITY_RESULT     = ActivityResult.COMPLETED.getUiText();

    /** Якорь — активность, появляющаяся только на стадии «Сбор документов». */
    private static final String DOCUMENTS_STAGE_ANCHOR = "Вложить документы и отправить на рассмотрение";

    private final DashboardComponent        dashboard  = new DashboardComponent();
    private final MenuComponent             menu       = new MenuComponent();
    private final ClickService              click      = new ClickService();
    private final CollateralTabsComponent   tabs       = new CollateralTabsComponent();

    // ══════════════════════════════════════
    // MAIN — заполнение + завершение стадии
    // ══════════════════════════════════════

    @Step("Агро: стадия «Обеспечение» (залоги + завершение стадии)")
    public void completeCollateralStage(List<CollateralData> collaterals) {
        log.info("Agro: complete collateral stage, count={}",
                collaterals == null ? 0 : collaterals.size());

        boolean hasCollateral = collaterals != null && !collaterals.isEmpty();

        if (hasCollateral) {
            fillCollaterals(collaterals);
        } else {
            log.info("Agro: no collaterals in scenario — only completing stage");
        }

        // Верификация обеспечения (V5): грид «Залоги» на вкладке «Обеспечение».
        // До завершения активности — грид ещё в DOM (нюанс ленивых вкладок).
        verifyCollaterals(collaterals);

        completeCollateralTask(hasCollateral);

        log.info("Agro: collateral stage completed");
    }

    // ══════════════════════════════════════
    // OVERLOAD — только завершение стадии (без обеспечения)
    // Стадию всё равно нужно завершить, иначе процесс не идёт дальше.
    // ══════════════════════════════════════

    @Step("Агро: завершение стадии «Обеспечение» без залогов")
    public void completeCollateralStage() {
        log.info("Agro: complete collateral stage without collaterals");
        completeCollateralTask(false);
    }

    // ══════════════════════════════════════
    // FILL — только заполнение залогов
    // ══════════════════════════════════════

    @Step("Агро: заполнение залогов (без завершения стадии)")
    public void fillCollaterals(List<CollateralData> collaterals) {
        log.info("Agro: fill collaterals, count={}", collaterals.size());

        for (CollateralData data : collaterals) {
            resolveFlow(data.getType()).fill(data);
        }
    }

    // ══════════════════════════════════════
    // VERIFY (V5) — round-trip залогов в гриде «Залоги»
    // ══════════════════════════════════════

    /**
     * Верификация обеспечения (V5) по гриду «Залоги» вкладки «Обеспечение»:
     * (A) число строк == числу залогов сценария;
     * (B) каждый залог присутствует по колонке «Обеспечение» (= uiName).
     * Пустой сценарий пропускается.
     */
    @Step("Агро: верификация обеспечения (количество + типы залогов)")
    private void verifyCollaterals(List<CollateralData> collaterals) {
        if (collaterals == null || collaterals.isEmpty()) {
            log.info("Agro: нет залогов в сценарии — верификация обеспечения пропущена");
            return;
        }
        log.info("Agro: verify collaterals, expected count={}", collaterals.size());

        // Приводим грид «Залоги» в DOM детерминированно.
        tabs.openCollateralTab();

        // (B) присутствие по типу — ждёт появления строки (робастно к дозагрузке).
        for (CollateralData data : collaterals) {
            String uiName = data.getType().getUiName();
            boolean present =
                    FieldUtils.gridHasRowContainingSafe(PLEDGE_GRID_WRAP, uiName);
            VerificationCollector.checkTrue(
                    "Обеспечение (Агро): залог «" + uiName + "» присутствует в гриде",
                    present, "тип " + data.getType());
        }

        // (A) число строк — после подтверждения видимости ожидаемых строк.
        int actualRows = FieldUtils.getGridRowCount(PLEDGE_GRID_WRAP);
        VerificationCollector.check(
                "Обеспечение (Агро): число залогов в гриде",
                collaterals.size(), actualRows);

        VerificationCollector.flush("Обеспечение (Агро)");
    }

    // ══════════════════════════════════════
    // RESOLVE FLOW
    // ══════════════════════════════════════

    private AgroBaseCollateralFlow resolveFlow(CollateralType type) {
        return switch (type) {
            case COTTON         -> new AgroCottonCollateralFlow();
            case FUTURE_HARVEST -> new AgroFutureHarvestCollateralFlow();
            default -> throw new IllegalArgumentException(
                    "Unsupported collateral type for Agro/Micro: " + type);
        };
    }

    // ══════════════════════════════════════
    // COMPLETE TASK
    // ══════════════════════════════════════

    @Step("Агро: завершение задачи по залогу и поручительству")
    private void completeCollateralTask(boolean hasCollateral) {
        log.info("Agro: complete collateral task (hasCollateral={})", hasCollateral);

        // Двойной клик по подтипу — только при наличии хотя бы одного залога
        // (открывает строку добавленного обеспечения перед закрытием активности).
        if (hasCollateral) {

            tabs.openCollateralTab();

            click.doubleClickByMarker("Обеспечение.Подтип");
        }

        dashboard.clickAndWaitMiniPage(COLLATERAL_ACTIVITY, "Execute");
        openResultDropdown();
        menu.clickByName(ACTIVITY_RESULT);
        click.clickByMarker("SaveEditButton");

        // Оверлей мини-страницы держится после сохранения и перехватывает
        // последующие клики. Ждём его снятия, чтобы стадия завершалась
        // со свободным интерфейсом.
        $x("//div[@id='AlignableMiniPageContainer-overlay']")
                .should(Condition.disappear, Duration.ofSeconds(100));

        // Ждём перехода на стадию «Сбор документов»
        $x("//label[normalize-space()='" + DOCUMENTS_STAGE_ANCHOR + "']")
                .shouldBe(Condition.visible, Duration.ofSeconds(100));

        log.info("Agro: documents stage reached");
    }

    /**
     * Открывает дропдаун результата в попапе активности.
     * <p>
     * Без этого список результатов не существует в DOM и menu.clickByName()
     * не находит пункт. В рознице роль «открывашки» играл
     * setFieldByMarker("ProcessResult", ...) через setValue (запрещён правилом
     * проекта) — здесь дропдаун открывается кликом по полю.
     */
    private void openResultDropdown() {
        log.debug("Open activity result dropdown");

        $$x("//div[@data-item-marker='Result' or @data-item-marker='ProcessResult']//input")
                .filter(Condition.visible)
                .first()
                .shouldBe(Condition.visible, Duration.ofSeconds(30))
                .click();

        log.debug("Result dropdown opened");
    }
}