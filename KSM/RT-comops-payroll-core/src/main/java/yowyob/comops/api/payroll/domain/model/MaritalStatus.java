package yowyob.comops.api.payroll.domain.model;

/**
 * Marital status, an input to the family-quotient computation of the IRPP.
 * Combined with the number of dependent children it yields the number of fiscal parts.
 */
public enum MaritalStatus {
    SINGLE,
    MARRIED,
    DIVORCED,
    WIDOWED
}
