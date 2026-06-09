package yowyob.comops.api.payroll.application.port.out;

/**
 * Employer header information for a legal payslip / document, projected from organization-core.
 *
 * @param legalName              registered legal name (raison sociale)
 * @param displayName            short/display name
 * @param registrationNumber     trade register number (RCCM)
 * @param taxNumber              taxpayer number (numéro de contribuable)
 * @param cnpsEmployerNumber     employer CNPS number, or {@code null} if unknown
 * @param ceoName                legal representative / CEO
 * @param email                  contact email
 * @param address                registered address (siège social)
 * @param city                   city
 * @param postalCode             postal code / BP
 * @param phone                  phone number
 * @param legalForm              legal form (SA, SARL, etc.)
 * @param capitalShare           share capital
 * @param conventionCollective   applicable collective agreement
 */
public record EmployerInfo(
        String legalName,
        String displayName,
        String registrationNumber,
        String taxNumber,
        String cnpsEmployerNumber,
        String ceoName,
        String email,
        String address,
        String city,
        String postalCode,
        String phone,
        String legalForm,
        java.math.BigDecimal capitalShare,
        String conventionCollective) {
}
