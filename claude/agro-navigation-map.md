# Карта-навигатор — маршрут Агро/Микро (LEGO)

Аналог розничной карты (claude/retail-navigation-map.md) для сегмента «Агро/Микро».
Сборка — `MicroSegmentRoute extends CreditApplicationRoute`; данные — AgroScenarioFactory.

## Общее с розницей (переиспользуется, НЕ дублируется)
База CreditApplicationRoute (as/createApplication/resumeAt); модели TestScenario/ParticipantData/
CollateralData/DocumentData; роль владельца документов — enum ParticipantRole (Ш15.8: сюда влит
бывший DocumentOwner, поля uiTab/containerId); компоненты Core (Click/Wait/Type/Scroll, Dashboard/Menu/…),
общий ActivityComponent (Ш15.4/15.5).

## Роли/рабочие места
creditExpert→CREDIT_EXPERT (ведёт продукт вместо менеджера); underwriter→UNDERWRITER;
middleController→IKOK (мидл-контролер); ikokGo→IKOK_GO; microCashier→CASHIER.

## Сценарии [AgroScenarioFactory]
agroMicroHappyPath() — поручитель+залогодатель, Cotton+FutureHarvest.
agroMicroRegression() — поручитель, FutureHarvest (agroMicroRegressionRoute, enabled).

## Сборка
- **runFull()** (Ш15.3) — полный маршрут одной строкой (паритет с RetailRoute.runStandard):
  creditExpertUpToReview → underwriterApprove → clientNotification → middleControllerDocuments →
  ikokGoSigning → cashierOperations → finishApplication.

## Ролевые сегменты
- creditExpertUpToReview() [CREDIT_EXPERT]: createApplication→registration→preliminaryCheck→
  participantsStage→collateralStage→documentCollection→review.
- underwriterApprove() [UNDERWRITER]: approveDecision.
- clientNotification() [CREDIT_EXPERT]: notifyClient.
- middleControllerDocuments() [IKOK]: attachBorrowerDocuments.
- ikokGoSigning() [IKOK_GO]: signing.
- cashierOperations() [CASHIER]: confirmCashOrders.
- finishApplication(): attachOrder [IKOK] → ikokGoConfirm [IKOK_GO] → finishViaConsultation [CREDIT_EXPERT].

## Каталог блоков (акцент на отличиях от розницы)
- **Создание заявки** `createApplication()` [общий] — как в рознице.
- **Оформление** `registration()` [MicroSegmentRoute → AgroRegistrationStageFlow] — ИНКАПСУЛИРОВАНО (Ш15.2;
  раньше 8 флоу инлайн в маршруте). Данные scenario.getRegistration(); Агро-специфика из AgroScenarioFactory внутри стадии.
- **Предв.проверка** `preliminaryCheck()` [AgroPreliminaryCheckFlow].
- **Участники** `participantsStage()` [AgroParticipantsStageFlow] — scenario-driven; детали из AgroGuarantorData/AgroPledgerData.
- **Обеспечение** `collateralStage()` [AgroCollateralStageFlow] — scenario-driven (Cotton/FutureHarvest).
- **Сбор документов** `documentCollection()` [AgroDocumentCollectionStageFlow] — документы участников НЕ грузятся
  (заёмщик вкладывается позже мидл-контролером); флаг attachDocuments на Агро не влияет. Заёмщик — на общем
  движке uploadDocuments(ownerData) (в отличие от розницы). Владелец — ParticipantRole (Ш15.8).
- **Рассмотрение** `review()` [AgroReviewStageFlow] — пишет номер заявки в контекст.
- **Утверждение** `approveDecision()` [AgroReviewStageUnderwriterFlow] — agroDecisionData() data-driven
  («Вид кредита»/«Кредитный лимит»); розница после Ш15.8 тоже через константу DECISION_CREDIT_TYPE.
  🔸полиш: структура «взять в работу→ApproveButton→утвердить» дублируется с розничным underwriter.
- **Информирование** `notifyClient()` [AgroClientNotificationStageFlow] — ответственные из displayName учёток.
- **Вложение документов заёмщика** `attachBorrowerDocuments()` [AgroMiddleControllerDocumentsFlow] — чего нет в рознице.
- **Подписание (НЕДЕЛИМЫЙ)** `signing()` [AgroSigningStageFlow] — completeSigningStage()+signAndIssueCredit();
  jsClickWithRetry общий; verifyOrders припаркован (ИКОК-ГО). PLANNING_TYPE/CREDIT_RECEIPT_TYPE уже константами
  (розница подтянута к этому образцу в Ш15.8).
- **Касса** `confirmCashOrders()` [AgroCashierFlow].
- **Завершение** `finishApplication()` (3 подсессии) [AgroApplicationFinishFlow] — Агро-копия
  patchFileUploadService (оставлена осознанно). ⚠ DEF-DOWNLOAD, DEF-LOGOUT (см. журнал).

## Долги Агро
✅ registration инлайн → инкапсулирован (15.2). ✅ нет runFull → добавлен (15.3).
✅ DocumentOwner/ParticipantRole слиты (15.8).
Осталось: мёртвый движок completeDocumentCollectionStage(List)+upload (РЕШЕНО оставить как задел, 15.6);
розница/Агро signing и underwriter делят структуру (тяжёлые кандидаты на консолидацию — задел после Ш15).

## Легенда
⚠ — известный дефект/риск среды. 🔸полиш — осознанный технический задел (поведение 1:1), в бэклог/Ш16.
