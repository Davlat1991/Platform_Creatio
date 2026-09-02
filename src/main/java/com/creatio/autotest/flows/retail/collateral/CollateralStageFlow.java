package com.creatio.autotest.flows.retail.collateral;

import com.creatio.autotest.components.DashboardComponent;
import com.creatio.autotest.components.MenuComponent;
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

    private final DashboardComponent dashboard = new DashboardComponent();
    private final MenuComponent      menu      = new MenuComponent();
    private final ClickService       click     = new ClickService();

    // ══════════════════════════════════════
    // MAIN — заполнение + завершение стадии
    // ══════════════════════════════════════

    @Step("Этап залога: заполнение залогов + завершение стадии")
    public void completeCollateralStage(List<CollateralData> collaterals) {
        log.info("Complete collateral stage: count={}", collaterals.size());
        fillCollaterals(collaterals);
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