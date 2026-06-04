package yowyob.comops.api.payroll.domain.model;

/**
 * Statutory declaration produced from a period's payroll.
 *
 * <ul>
 *   <li>{@link #CNPS} — social-security contributions declaration (employee + employer).</li>
 *   <li>{@link #DIPE} — Déclaration Individuelle et Périodique des Employés (CNPS, individual).</li>
 *   <li>{@link #IRPP_CAC} — income tax + council surcharge withheld, declared to the tax authority.</li>
 * </ul>
 */
public enum DeclarationType {
    CNPS,
    DIPE,
    IRPP_CAC
}
