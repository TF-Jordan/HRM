package yowyob.comops.api.payroll.domain.model;

/**
 * Disbursement channel for an employee's net pay. Owned by payroll-core so the module
 * stays decoupled from hrm-core; the integration adapter maps the HR value onto this enum.
 */
public enum PaymentChannel {
    BANK_TRANSFER,
    MTN_MOBILE_MONEY,
    ORANGE_MONEY,
    CASH
}
