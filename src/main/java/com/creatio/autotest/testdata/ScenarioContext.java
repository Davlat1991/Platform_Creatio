package com.creatio.autotest.testdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runtime-данные сценария — рождаются ВО ВРЕМЯ теста.
 *
 * Жизненный цикл номеров:
 *   1. applicationNumber  — рождается на этапе Рассмотрения (ReviewStageRetailFlow)
 *                           используется до создания договора
 *   2. creditContractNumber — рождается при создании договора (LoanIssuanceFlow)
 *                           используется ПОСЛЕ создания договора (Кассир, завершение)
 *   3. depositAccountNumber — рождается при создании сберегательного счёта (LoanIssuanceFlow)
 */
public class ScenarioContext {

    private static final Logger log =
            LoggerFactory.getLogger(ScenarioContext.class);

    private String applicationNumber;     // номер заявки (5 знаков)
    private String creditContractNumber;  // номер кредитного договора (10 знаков)
    private String depositAccountNumber;  // номер депозитного счёта

    // ══════════════════════════════════════
    // applicationNumber
    // ══════════════════════════════════════

    public void setApplicationNumber(String number) {
        log.info("Context: applicationNumber = '{}'", number);
        this.applicationNumber = number;
    }

    public String getApplicationNumber() {
        return require(applicationNumber, "applicationNumber",
                "ReviewStageRetailFlow.completeReview()");
    }

    // ══════════════════════════════════════
    // creditContractNumber
    // ══════════════════════════════════════

    public void setCreditContractNumber(String number) {
        log.info("Context: creditContractNumber = '{}'", number);
        this.creditContractNumber = number;
    }

    public String getCreditContractNumber() {
        return require(creditContractNumber, "creditContractNumber",
                "LoanIssuanceFlow.createAbsContract()");
    }

    // ══════════════════════════════════════
    // depositAccountNumber
    // ══════════════════════════════════════

    public void setDepositAccountNumber(String number) {
        log.info("Context: depositAccountNumber = '{}'", number);
        this.depositAccountNumber = number;
    }

    public String getDepositAccountNumber() {
        return require(depositAccountNumber, "depositAccountNumber",
                "LoanIssuanceFlow (создание сберегательного счёта)");
    }

    // ══════════════════════════════════════
    // INTERNAL
    // ══════════════════════════════════════

    private String require(String value, String fieldName, String source) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "'" + fieldName + "' не задан. " +
                            "Убедись что '" + source + "' был вызван ранее."
            );
        }
        return value;
    }

    @Override
    public String toString() {
        return "ScenarioContext{" +
                "applicationNumber='" + applicationNumber + '\'' +
                ", creditContractNumber='" + creditContractNumber + '\'' +
                ", depositAccountNumber='" + depositAccountNumber + '\'' +
                '}';
    }
}