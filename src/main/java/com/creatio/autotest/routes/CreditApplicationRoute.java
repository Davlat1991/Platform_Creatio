package com.creatio.autotest.routes;

import com.creatio.autotest.core.config.Config;
import com.creatio.autotest.flows.ApplicationCreationFlow;
import com.creatio.autotest.flows.AuthFlow;
import com.creatio.autotest.flows.ClientSearchFlow;
import com.creatio.autotest.flows.ConsultationStartFlow;
import com.creatio.autotest.flows.ProductSelectionFlow;
import com.creatio.autotest.flows.WorkspaceFlow;
import com.creatio.autotest.testdata.ScenarioContextProvider;
import com.creatio.autotest.testdata.models.TestScenario;
import com.creatio.autotest.testdata.models.UserCredentials;
import com.creatio.autotest.testdata.models.Workspace;
import io.qameta.allure.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Общий сборочный слой кредитного маршрута для всех продуктовых линий
 * (розница, Микро и будущие типы).
 * <p>
 * Держит session-механику, единую для всех линий: сессию под ролью
 * {@link #as(UserCredentials, Workspace, Runnable)}, тихий логаут на упавшем
 * сегменте, точки входа в середину маршрута ({@link #resumeAt(String)},
 * {@link #resumeWithContract(String)}) и стадию создания заявки
 * {@link #createApplication()}. Продуктоспецифичные стадии и ролевые сегменты
 * живут в подклассах.
 * <p>
 * Fluent-цепочки resume* сохранены через self-type {@code T}: методы
 * возвращают конкретный подкласс, поэтому {@code route.resumeAt(n).segment()}
 * собирается.
 * <p>
 * Состояния нет: поле — только неизменяемая ссылка на сценарий плюс
 * инфраструктурные флоу без состояния. Объект создаётся локально в тест-методе,
 * параллельный запуск не ломается (драйвер и {@code ScenarioContext} —
 * ThreadLocal).
 *
 * @param <T> конкретный тип маршрута-наследника (для fluent resume*)
 */
public abstract class CreditApplicationRoute<T extends CreditApplicationRoute<T>> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final Config        config    = Config.getInstance();
    protected final AuthFlow      auth      = new AuthFlow();
    protected final WorkspaceFlow workspace = new WorkspaceFlow();

    protected final TestScenario scenario;

    protected CreditApplicationRoute(TestScenario scenario) {
        this.scenario = scenario;
    }

    /** Возврат конкретного подкласса — для fluent-цепочек resume*. */
    protected abstract T self();

    // ══════════════════════════════════════
    // УРОВЕНЬ 2 — СЕССИЯ ПОД РОЛЬЮ
    // ══════════════════════════════════════

    /**
     * Выполняет тело под указанной учётной записью и рабочим местом.
     * Логаут вызывается всегда: на зелёном пути — обычный, на упавшем
     * сегменте — «тихий» ({@link #logoutQuietly()}), чтобы собственное падение
     * логаута не подменило исходную причину падения в отчёте.
     */
    @Step("Сессия под ролью {0}, рабочее место {1}")
    public void as(UserCredentials user, Workspace ws, Runnable body) {
        log.info("── Сегмент: {} / рабочее место «{}» ──", user, ws.getUiName());

        auth.login(user);
        boolean failed = false;
        try {
            workspace.select(ws);
            body.run();
        } catch (Throwable t) {
            failed = true;
            throw t;
        } finally {
            if (failed) {
                logoutQuietly();
            } else {
                auth.logout();
            }
        }
    }

    /**
     * Логаут на уже падающем сегменте.
     * <p>
     * Ловится {@code Throwable}, а не {@code Exception}: Selenide бросает
     * наследников {@code AssertionError}, и {@code catch (Exception)} их
     * пропустил бы мимо.
     */
    private void logoutQuietly() {
        try {
            auth.logout();
        } catch (Throwable t) {
            log.warn("Логаут после падения сегмента не выполнен: {}", t.toString());
        }
    }

    // ══════════════════════════════════════
    // УРОВЕНЬ 1 — ОБЩАЯ СТАДИЯ: СОЗДАНИЕ ЗАЯВКИ
    // ══════════════════════════════════════

    /** Поиск клиента, консультация, подбор продукта, создание заявки. */
    @Step("Стадия: поиск клиента → консультация → подбор продукта → создание заявки")
    public void createApplication() {
        log.info("Стадия «Создание заявки»");

        new ClientSearchFlow().searchClient(
                scenario.getContact().getLastName(),
                scenario.getContact().getFirstName(),
                scenario.getContact().getMiddleName()
        );

        new ConsultationStartFlow()
                .startConsultation(config.getConsultationTheme("retail"));

        new ProductSelectionFlow()
                .selectProduct(scenario.getProduct());

        new ApplicationCreationFlow()
                .createApplication(scenario.getProduct());
    }

    // ══════════════════════════════════════
    // ТОЧКИ ВХОДА В СЕРЕДИНУ МАРШРУТА
    // ══════════════════════════════════════

    /**
     * Засев номера заявки в контекст — вход в сегменты, которым нужна уже
     * созданная заявка.
     */
    @Step("Контекст: номер заявки = {0}")
    public T resumeAt(String applicationNumber) {
        ScenarioContextProvider.get().setApplicationNumber(applicationNumber);
        log.info("Контекст засеян номером заявки: {}", applicationNumber);
        return self();
    }

    /**
     * Засев номера кредитного договора — вход в сегменты, которым нужен уже
     * созданный договор.
     */
    @Step("Контекст: номер кредитного договора = {0}")
    public T resumeWithContract(String contractNumber) {
        ScenarioContextProvider.get().setCreditContractNumber(contractNumber);
        log.info("Контекст засеян номером договора: {}", contractNumber);
        return self();
    }
}