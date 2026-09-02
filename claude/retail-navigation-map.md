# Карта-навигатор — розничный маршрут (LEGO)

Быстро понять, из каких блоков собирается сценарий, что каждый делает и как вызвать.
Розничный хребет задокументирован полностью; оба маршрута scenario-driven.

## Слои
Tests (выбор сценария+запуск) · Routes (СБОРКА: as/стадии/сегменты; база CreditApplicationRoute,
розница RetailRoute, Агро MicroSegmentRoute) · Flows (common|retail|micro) · Pages · Components · Core.

## Модель сборки
1) Данные — TestScenario из ScenarioFactory.* (пусто = нет).
2) Сессия — route.as(user, ws, тело): login→рабочее место→тело→logout.
3) Блоки/сегменты. Главный приём: `route.as(user, ws, route::блок)`. Рецепты: CompositionExamplesTest.
Вход в середину: resumeAt()/resumeWithContract().

## Ролевые сегменты [RetailRoute]
- STANDARD runStandard(): managerUpToReview → underwriterApprove → managerNotification →
  ikokGoIssuance → cashierOperations → finishApplication.
- SIMPLIFIED runSimplified(): managerSimplifiedUpToNotification → ikokIssuance → cashierOperations →
  simplifiedFinish. (Ш14: упрощённый зовёт те же scenario-driven стадии, что стандарт.)

## Каталог блоков
- **Авторизация** `authenticate(user,ws)` [CreditApplicationRoute] — логин→рабочее место→выход.
  Сценарий authOnly(). Тест AuthRouteTest ✅.
- **Создание заявки** `createApplication()` [CreditApplicationRoute] — поиск клиента→консультация→
  подбор→создание. Данные: contact, product. 🔸полиш: тема консультации литералом getConsultationTheme("retail").
- **Оформление** `registration()` [RegistrationStageFlow, 8 подшагов] — данные scenario.getRegistration()
  + тип клиента ClientFlowFactory.from(). 🔸полиш: дубль перегрузок; хардкод подфлоу; пакет ClientFlowFactory.
- **Предв.проверка** `preliminaryCheck()` [PreliminaryCheckStageFlow] — анкеты→«Проверка клиента»→ожидание.
- **Участники** `participantsStage()` [ParticipantsStageFlow] — для каждого из scenario.participants добавить+анкета
  по роли. scenario-driven. Флаг attachDocuments (дефолт true; withoutDocuments()).
- **Обеспечение** `collateralStage()` [CollateralStageFlow] — залоги из scenario.collaterals (resolveFlow(type)→10 флоу)→
  закрыть; пусто→закрыть без залогов; fillCollaterals(list) — без закрытия.
- **Сбор документов** `documentCollection()` [DocumentCollectionStageFlow] — заёмщик→участники (ФИКС Ш7:
  ParticipantDocuments.forParticipants(scenario.participants); пусто→только заёмщик)→закрыть.
  Владелец документов — enum ParticipantRole (Ш15.8: DocumentOwner влит сюда, +uiTab/+containerId, файл удалён).
  🔸полиш (осознанный задел): заёмщик всё ещё двушаговой связкой prepareBorrowerDetails()+uploadBorrowerDocuments()
  с хардкодом имён файлов, не на общем движке uploadDocuments (в Агро — на общем).
- **Рассмотрение** `review()` [ReviewStageRetailFlow] — verifyRoute→STANDARD «КК4»/SIMPLIFIED «Информирование»→
  номер в контекст. scenario-driven по CreditRoute. (Ш15.7: неиспользуемый expectedScoringDecision убран из completeReview.)
- **Утверждение (STANDARD)** `approveDecision()` [ReviewStageUnderwriterFlow] — «КК4 по заявке»→взять в работу
  (якорь ApproveButton)→«Кредит»→утвердить. Ш15.8: «Кредит» → константа DECISION_CREDIT_TYPE.
  🔸полиш: дубль структуры с Агро underwriter (кандидат на консолидацию).
- **Информирование** `notifyClient()`/`notifyClientSimplified()` [ClientNotificationStageFlow] — WithSearch
  (STANDARD, ИКОК-ГО)/без поиска (SIMPLIFIED, ИКОК). Ответственный data-driven.
  Ш15.8: completeClientNotificationWithSearch делегирует в completeClientNotification (дубль тела убран).
- **Подписание и выдача (НЕДЕЛИМЫЙ)** `issueLoan(...)` [SigningStageFlow] — КК→договор в АБС→счета/график→
  *(обеспечение)*→подписание+выдача→оферта→*(ордера)*. Параметры registerCollateralContracts=hasSecuredItems,
  verifyOrders. jsClickWithRetry (общий ClickService). Ш15.8: «Аннуитетный_005»/«Получает сегодня» → константы
  PLANNING_TYPE/CREDIT_RECEIPT_TYPE.
- **Касса** `cashierOrders()` [CashierFlow] — приходный+расходный (ассерт «Новый»→«Подтверждён», печать). Реально проверяет.
- **Завершение** `finishApplication()` (3 подсессии) [ApplicationFinishFlow] — менеджер прикрепляет+закрывает
  активность → ИКОК-ГО скачивает+подтверждает → менеджер завершает через консультацию.
  ⚠ розничная копия patchFileUploadService (оставлена осознанно). ⚠ DEF-DOWNLOAD, DEF-LOGOUT (см. журнал).

## Закрытие активности — общий ActivityComponent (Ш15.4/15.5)
Паттерн «hover→Execute→Result→Save» вынесен в `components/ActivityComponent`: ядро
`openAndSubmit(title, result, saveTimeout)` + `hoverItemContent(title)`, 4 варианта хвоста:
completeActivityWaitingOverlay / completeActivity / completeActivityAwaitingMiniPageClose /
completeActivityWaitingOverlayById. Заменил ~6 инлайн-копий во флоу (розница+Агро).

## Легенда
⚠ — известный дефект/риск среды. 🔸полиш — осознанный технический задел (поведение уже 1:1), в бэклог/Ш16.
Все метки ⚠Ш15 сняты — Ш15 закрыт (см. журнал).
