# Composability — журнал выполнения

Канонический журнал (хронология). План/статус: claude/composability-roadmap.md.

## РОЗНИЧНЫЙ ХРЕБЕТ (Ш1–13) ✅. Оба маршрута scenario-driven (Ш14) ✅. Агро-карта (Ш17) ✅.

- Ш1 Авторизация ✅ — блок `authenticate()`; `authOnly()`; AuthRouteTest. PASS (84 c).
- Ш2 Создание заявки ✅ — CompositionExamplesTest (5 рецептов). test-compile OK.
- Ш3–6 ✅ — Оформление / Предв.проверка / Участники / Обеспечение (оценка+документация, кода нет).
- Ш7 Сбор документов ✅ (КРАЕУГОЛЬНЫЙ) — ParticipantData(+attachDocuments/toBuilder/withoutDocuments);
  новый ParticipantDocuments; RetailRoute 1 строка; ScenarioFactory −3 метода; DocumentCollectionTest.
  Поведение happyPath 1:1. standardRouteHappyPath ✅ (19:12). Отчёты Jira/Notion выданы.
- Ш8–13 ✅ — Рассмотрение / Утверждение / Информирование / Подписание (неделимый) / Касса / Завершение.
- Ш14 ✅ — managerSimplifiedUpToNotification на scenario-driven стадии; удалены collateralStageClose/
  documentCollectionBorrowerOnly. simplifiedRouteHappyPath ✅ (9:38). Отчёты выданы.
- Ш17 ✅ — карта Агро/Микро.

## Шаг 15 — сквозной рефакторинг ✅ ЗАКРЫТ (2026-09-01)
- 15.1 ✅ — удалён мёртвый no-arg CollateralStageFlow.completeCollateralStage(). test-compile OK.
- 15.2 ✅ — Микро registration → AgroRegistrationStageFlow (8 подфлоу), маршрут → делегат; −9/+1 импорт.
  agroMicroRegressionRoute ✅ (17:36).
- 15.3 ✅ — MicroSegmentRoute.runFull() (паритет с runStandard). compile-only.
- 15.4 ✅ — ActivityComponent: миграция 5/5 флоу. Методы: completeActivityWaitingOverlay /
  completeActivity / completeActivityAwaitingMiniPageClose / completeActivityWaitingOverlayById.
  Инкременты: ApplicationFinishFlow (standard 18:00); DocumentCollectionStageFlow (standard 19:19);
  AgroApplicationFinishFlow (agro 18:09); AgroDocumentCollectionStageFlow (agro 19:01);
  PreliminaryCheckStageFlow (частичная; 1-й прогон флейк, перезапуск standard 17:13).
- 15.5 ✅ — tidy: ядро openAndSubmit(saveTimeout)+hoverItemContent; 4 метода тонкие. Вербатим.
  standardRouteHappyPath ✅ (19:30) + agroMicroRegressionRoute ✅ (19:26). ActivityComponent консолидирован.
- 15.6 (решение, без кода) — мёртвый движок AgroDocumentCollectionStageFlow.completeDocumentCollectionStage(List)
  + uploadAllDocuments/uploadDocuments/prepareDetails/upload: РЕШЕНО ОСТАВИТЬ как задокументированный задел
  (javadoc: «готов к использованию, если состав сценария изменится»). Владелец: Davlat. Кода не трогаем.
- 15.7 ✅ — убран неиспользуемый параметр expectedScoringDecision из ReviewStageRetailFlow.completeReview
  + правка вызова в RetailRoute.review. Вставлено, mvn test-compile ЗЕЛЁНЫЙ.
- 15.8 ✅ — хвост Ш15, батч из 3 поведение-сохраняющих полишей (вставлялись по одному, test-compile после каждого):
  (1) дедуп ClientNotificationStageFlow — completeClientNotificationWithSearch делегирует в
      completeClientNotification после поиска заявки (тело перегрузки больше не дублируется);
  (2) литералы→константы: SigningStageFlow (PLANNING_TYPE «Аннуитетный_005», CREDIT_RECEIPT_TYPE
      «Получает сегодня»), ReviewStageUnderwriterFlow (DECISION_CREDIT_TYPE «Кредит») — по образцу Агро;
  (3) дедуп enum: DocumentOwner влит в ParticipantRole (+uiTab/+containerId), файл DocumentOwner УДАЛЁН
      (179 файлов вместо 180); DocumentOwnerData.owner → ParticipantRole; ParticipantDocuments.toOwner()
      удалён как тождественный (.owner(role)); правки в DocumentCollectionStageFlow /
      AgroDocumentCollectionStageFlow / AgroScenarioFactory. ScenarioFactory НЕ трогали (documentOwners()
      удалён ещё в Ш7).
  ВАЖНО (урок): доки-снапшот проекта отставал от рабочего дерева (documentOwners() уже не было;
  ParticipantDocuments не был виден в доках) — правки по живому коду + компилятор как ground truth
  (удаление DocumentOwner превращает каждый недочищенный хвост в жёсткую ошибку компиляции с адресом).
  ПРОГОНЫ (контрольная точка): 1-й standardRouteHappyPath упал на 19:30 на логауте (флейка DEF-LOGOUT,
  все стадии батча прошли); перезапуск standardRouteHappyPath ✅ (17:25) + agroMicroRegressionRoute ✅ (17:35).
  Ш15 закрыт.
- ОСТАЛОСЬ ПОСЛЕ Ш15 (осознанный задел, НЕ входило в 3-пунктовый батч — в бэклог/Ш16):
  • «заёмщик на общий движок» — розничный DocumentCollectionStageFlow всё ещё гонит заёмщика
    двушаговой связкой prepareBorrowerDetails()+uploadBorrowerDocuments() с захардкоженными именами
    файлов; Агро гонит заёмщика через универсальный uploadDocuments(ownerData). Поведенчески 1:1,
    но розница не на общем движке.
  • «дубль розница/Агро underwriter» — ReviewStageUnderwriterFlow и AgroReviewStageUnderwriterFlow
    делят паттерн «взять в работу → якорь ApproveButton → утвердить»; литерал «Кредит» убран (15.8),
    но сам дубль тел остаётся (тяжёлый кандидат на консолидацию).
  • прочие ⚠ карт, не входившие в батч: тема консультации литералом в createApplication;
    дубль перегрузок/хардкод подфлоу в Оформлении.

## Дефекты / бэклог (чинить отдельно)
- DEF-DOWNLOAD (плавающий, редкий) — скачивание на завершении заявки иногда не стартует:
  ApplicationFinishFlow.downloadFile() (и Агро-аналог) кликает по ссылке файла ДО полной загрузки
  страницы/детали «Файлы и ссылки» → download не запускается → waitForNewDownload таймаут 90с.
  Фикс: перед кликом дождаться готовности (waitForPage + стабильная видимость ссылки) и/или ретрай.
- DEF-LOGOUT (плавающий) — логаут иногда не находит кнопку «Выход»:
  Element not found //*[@data-item-marker='Выход'], таймаут 60с. Наблюдён на standardRouteHappyPath
  (2026-09-01, упал на 19:30 — полное время маршрута, т.е. все стадии прошли, споткнулся на финальном
  логауте; приложение живое, висело на дашборде «Аналитика по заявкам»). Перезапуск прошёл зелёным →
  флейка. НЕ связан с батчем Ш15.8 — логаут вне зоны правок. Фикс (позже, в духе DEF-DOWNLOAD):
  перед кликом по «Выход» открыть меню профиля и дождаться стабильной видимости пункта + ретрай.
  Владелец решения о фиксе: Davlat.

## Наблюдения
- standard E2E иногда флейкает (NoSuchSessionException залог→документы; DEF-DOWNLOAD; DEF-LOGOUT) — среда/хрупкие места, не рефактор.
- enabled agroMicroRegressionRoute можно перевести на runFull() при следующем прогоне (равнозначно).
