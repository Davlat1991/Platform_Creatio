package com.creatio.autotest.testdata.models;

import lombok.Builder;
import lombok.Getter;

/**
 * Данные анкеты залогодателя (Агро/Микро).
 * <p>
 * Анкета залогодателя короче анкеты поручителя: без документов, доходов/расходов,
 * полного скоринга и вкладки «Оценка информации».
 * <p>
 * В рознице значения захардкожены в теле {@code PledgerFlow}; для Агро вынесены
 * в модель и подаются из {@code AgroScenarioFactory.agroPledgerData()}.
 * Значения посеяны розничными и подлежат проверке по live DOM.
 */
@Getter
@Builder
public class AgroPledgerData {

    /** Средство связи (BnzAffiliation). */
    private final String communicationMeans;

    /** Дата регистрации (BnzRegistrationDate). */
    private final String registrationDate;

    /** Вид связи. */
    private final String relationType;

    /** Национальность. */
    private final String nationality;

    /** Семейное положение (вкладка «Дополнительная информация»). */
    private final String maritalStatus;

    /** Уровень риска (вкладка «Дополнительная информация»). */
    private final String riskLevel;
}