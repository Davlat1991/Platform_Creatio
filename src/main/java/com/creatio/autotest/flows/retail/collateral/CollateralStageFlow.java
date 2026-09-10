package com.creatio.autotest.flows.retail.collateral;

import com.creatio.autotest.components.DashboardComponent;
import com.creatio.autotest.components.MenuComponent;
import com.creatio.autotest.components.collateral.CollateralTabsComponent;
import com.creatio.autotest.core.verify.VerificationCollector;
import com.creatio.autotest.utils.FieldUtils;
import com.creatio.autotest.core.services.ClickService;
import com.creatio.autotest.testdata.models.ActivityResult;
import com.creatio.autotest.testdata.models.CollateralData;
import com.creatio.autotest.testdata.models.CollateralType;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.codeborne.selenide.Selenide.$x;

public class CollateralStageFlow {

    private static final Logger log =
            LoggerFactory.getLogger(CollateralStageFlow.class);

    /** Грид «Залоги» на вкладке «Обеспечение» (общесистемный, обе линейки). */
    private static final String PLEDGE_GRID_WRAP =
            "grid-FinAppPledgeDetailDataGridGrid-wrap";

    private final DashboardComponent      dashboard = new DashboardComponent();
    private final MenuComponent           menu      = new MenuComponent();
    private final ClickService            click     = new ClickService();
    private final CollateralTabsComponent tabs      = new CollateralTabsComponent();

    // ══════════════════════════════════════
    // MAIN — заполнение + завершение стадии
    // ══════════════════════════════════════

    @Step("Этап залога: заполнение залогов + завершение стадии")
    public void completeCollateralStage(List<CollateralData> collaterals) {
        log.info("Complete collateral stage: count={}", collaterals.size());
        fillCollaterals(collaterals);

        // Верификация обеспечения (V5): грид «Залоги» на вкладке «Обеспечение».
        // До завершения активности — грид ещё в DOM (нюанс ленивых вкладок).
        verifyCollaterals(collaterals);

        // Двойной клик по подтипу нужен только при наличии залога
        // (открывает строку добавленного обеспечения перед закрытием активности).
        completeCollateralTask(!collaterals.isEmpty());
    }

    // ══════════════════════════════════════
    // OVERLOAD — только завершение стадии (без данных)
    // Для маршрутов без обеспечения: стадию "Обеспечение" всё равно
    // нужно завершить, иначе процесс не идёт к "Сбор документов".
    // ══════════════════════════════════════


    // ══════════════════════════════════════
    // FILL — только заполнение залогов (без завершения стадии)
    // ══════════════════════════════════════

    @Step("Заполнить залоги (без завершения стадии)")
    public void fillCollaterals(List<CollateralData> collaterals) {
        log.info("Fill collaterals: count={}", collaterals.size());
        for (CollateralData data : collaterals) {
            BaseCollateralFlow flow = resolveFlow(data.getType());
            flow.fill(data);
        }
    }

    // ══════════════════════════════════════
    // VERIFY (V5) — round-trip залогов в гриде «Залоги»
    // ══════════════════════════════════════

    /**
     * Верификация обеспечения (V5) по гриду «Залоги» вкладки «Обеспечение»:
     * (A) число строк == числу залогов сценария;
     * (B) каждый залог присутствует по колонке «Обеспечение» (= uiName).
     * Пустой сценарий пропускается (упрощённый маршрут не трогаем).
     */
    @Step("Верификация обеспечения: количество + типы залогов")
    private void verifyCollaterals(List<CollateralData> collaterals) {
        if (collaterals == null || collaterals.isEmpty()) {
            log.info("Нет залогов в сценарии — верификация обеспечения пропущена");
            return;
        }
        log.info("Verify collaterals: expected count={}", collaterals.size());

        // Приводим грид «Залоги» в DOM детерминированно.
        tabs.openCollateralTab();

        // (B) присутствие по типу — ждёт появления строки (робастно к дозагрузке).
        for (CollateralData data : collaterals) {
            String uiName = data.getType().getUiName();
            boolean present =
                    FieldUtils.gridHasRowContainingSafe(PLEDGE_GRID_WRAP, uiName);
            VerificationCollector.checkTrue(
                    "Обеспечение: залог «" + uiName + "» присутствует в гриде",
                    present, "тип " + data.getType());
        }

        // (A) число строк — после подтверждения видимости ожидаемых строк.
        int actualRows = FieldUtils.getGridRowCount(PLEDGE_GRID_WRAP);
        VerificationCollector.check(
                "Обеспечение: число залогов в гриде",
                collaterals.size(), actualRows);

        VerificationCollector.flush("Обеспечение");
    }

    // ══════════════════════════════════════
    // RESOLVE FLOW
    // ══════════════════════════════════════

    private BaseCollateralFlow resolveFlow(CollateralType type) {
        switch (type) {
            case VEHICLE:           return new VehicleCollateralFlow();
            case GOODS:             return new GoodsCollateralFlow();
            case EQUIPMENT:         return new EquipmentCollateralFlow();
            case CASH_DEPOSIT:      return new CashDepositCollateralFlow();
            case REAL_ESTATE:       return new RealEstateCollateralFlow();
            case GOLD:              return new GoldCollateralFlow();
            case MOVABLE_PROPERTY:  return new MovablePropertyCollateralFlow();
            case ACQUIRED_PROPERTY: return new AcquiredPropertyCollateralFlow();
            case COTTON:            return new CottonCollateralFlow();
            case FUTURE_HARVEST:    return new FutureHarvestCollateralFlow();
            default:
                throw new IllegalArgumentException(
                        "Unknown collateral type: " + type
                );
        }
    }

    // ══════════════════════════════════════
    // COMPLETE TASK
    // ══════════════════════════════════════

    @Step("Завершение задачи по залогу и поручительству")
    private void completeCollateralTask(boolean hasCollateral) {
        log.info("Complete collateral task (hasCollateral={})", hasCollateral);

        // Двойной клик по подтипу — только при наличии хотя бы одного залога.
        if (hasCollateral) {
            click.doubleClickByMarker("Обеспечение.Подтип");
        }

        dashboard.clickAndWaitMiniPage(
                "Заполните данные обеспечения и поручительства",
                "Execute"
        );

        // Открываем дропдаун результата кликом по полю — setValue(Result)
        // не запускает onChange, и список пунктов не появляется в DOM.
        menu.openResultDropdown();
        menu.clickByName(ActivityResult.COMPLETED.getUiText());
        click.clickByMarker("SaveEditButton");

        // Сохранение активности асинхронное: мини-страница держит overlay, пока
        // идёт сохранение, и переход на «Сбор документов» наступает только после
        // его снятия. Ждём снятия overlay (= «сохранение завершилось»), и лишь
        // затем — якорь стадии документов. Паттерн из AgroCollateralStageFlow;
        // таймаут больше агрошного (100с), т.к. розничная активность с 10 залогами
        // и участниками сохраняется дольше — по прогонам иногда >100с.
        $x("//div[@id='AlignableMiniPageContainer-overlay']")
                .should(com.codeborne.selenide.Condition.disappear,
                        java.time.Duration.ofSeconds(180));

        // Ждём перехода на этап документов
        $x("//label[normalize-space()=" +
                "'Вложить документы и отправить на рассмотрение']")
                .shouldBe(com.codeborne.selenide.Condition.visible,
                        java.time.Duration.ofSeconds(100));


    }
}