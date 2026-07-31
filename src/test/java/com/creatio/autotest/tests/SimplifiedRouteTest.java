package com.creatio.autotest.tests;

import com.creatio.autotest.base.BaseTest;
import com.creatio.autotest.routes.RetailRoute;
import com.creatio.autotest.testdata.ScenarioFactory;
import com.creatio.autotest.testdata.models.TestScenario;
import com.creatio.autotest.utils.AllureTags;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

/**
 * Упрощённый маршрут (500–30000 сомони).
 * Тип клиента: «Имеет другой источник дохода» (OTHER_INCOME, дефолт фабрики).
 * БЕЗ участников (поручитель/залогодатель) и БЕЗ залогов.
 * На стадии «Сбор документов» — только документы заёмщика.
 * Скоринг обязан дать «Одобрить»; заявка авто-уходит на «Информирование клиента».
 * Верификатор и кредитный комитет НЕ участвуют.
 * <p>
 * Маршрут собран из ролевых сегментов в {@link RetailRoute} — тело теста
 * содержит только выбор сценария и запуск сборки.
 */
@Epic(AllureTags.Epics.RETAIL)
@Feature(AllureTags.Features.E2E)
@Owner("Davlat")
public class SimplifiedRouteTest extends BaseTest {

    @Test(groups = "regression")
    @Story("Упрощённый маршрут — без участников, залогов и верификатора")
    @Description("Упрощённый маршрут — без участников, без залогов, без верификатора")
    @Severity(SeverityLevel.BLOCKER)
    public void simplifiedRouteHappyPath() {
        TestScenario scenario = ScenarioFactory.simplePath();
        new RetailRoute(scenario).runSimplified();
    }

    /**
     * Ручной вход в середину упрощённого маршрута: засеваем контекст номером уже
     * созданной заявки (или договора) и запускаем нужный сегмент, минуя предыдущие.
     * Отключён — включать точечно при отладке конкретной стадии.
     */
    @Test(groups = "debug", enabled = false)
    @Story("Debug — вход в середину упрощённого маршрута по номеру заявки/договора")
    @Description("Засев контекста и запуск сегмента без прохождения всего маршрута")
    @Severity(SeverityLevel.NORMAL)
    public void debugResumeMidRoute() {
        TestScenario scenario = ScenarioFactory.simplePath();
        RetailRoute route = new RetailRoute(scenario);

        // Заявка уже создана — старт с выдачи под ИКОК:
        route.resumeAt("391990").ikokIssuance();

        // Договор уже создан — вместо строки выше:
        // route.resumeWithContract("02-005-033405").cashierOperations();
    }
}