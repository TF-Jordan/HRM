package yowyob.comops.api.payroll.application.port.out;

/**
 * Employer header information for a legal payslip / document, projected from organization-core.
 *
 * @param legalName           registered legal name (raison sociale)
 * @param displayName         short/display name
 * @param registrationNumber  trade register number (RCCM)
 * @param taxNumber           taxpayer number (numéro de contribuable)
 * @param cnpsEmployerNumber  employer CNPS number, or {@code null} if unknown
 * @param ceoName             legal representative / CEO
 * @param email               contact email
 */
public record EmployerInfo(
        String legalName,
        String displayName,
        String registrationNumber,
        String taxNumber,
        String cnpsEmployerNumber,
        String ceoName,
        String email) {
}
