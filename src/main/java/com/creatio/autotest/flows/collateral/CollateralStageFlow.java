package com.creatio.autotest.flows.collateral;

import com.creatio.autotest.components.DashboardComponent;
import com.creatio.autotest.components.MenuComponent;
import com.creatio.autotest.core.base.BasePage;
import com.creatio.autotest.core.services.ClickService;
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
    private final BasePage           basePage  = new BasePage();
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

    @Step("Этап залога: завершение стадии без обеспечения")
    public void completeCollateralStage() {
        log.info("Complete collateral stage without collaterals");
        completeCollateralTask(false);
    }

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

        setFieldByMarker("ProcessResult", "Выполнена");
        menu.clickByName("Выполнена");
        click.clickByMarker("SaveEditButton");

        // Ждём перехода на этап документов
        $x("//label[normalize-space()=" +
                "'Вложить документы и отправить на рассмотрение']")
                .shouldBe(com.codeborne.selenide.Condition.visible,
                        java.time.Duration.ofSeconds(100));
    }

    private void setFieldByMarker(String marker, String value) {
        $x("//div[@data-item-marker='" + marker + "']/input")
                .setValue(value);
    }
}