package yowyob.comops.api.payroll.application.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * The mutable slot table threaded through a single employee's calculation.
 *
 * A pay element's {@code baseReference} is resolved against this table, which holds:
 * <ul>
 *   <li>reserved slots seeded up-front: {@link #GROSS}, {@link #BASE_SALARY},
 *       {@link #BENEFITS_IN_KIND}, {@link #TAXABLE_NET};</li>
 *   <li>dynamic slots registered as elements are evaluated — keyed by element code — so a
 *       later element (e.g. CAC) can reference an earlier result (e.g. IRPP).</li>
 * </ul>
 * Unknown references resolve to zero, keeping the engine total-function and null-safe.
 */
public final class PayrollCalculationContext {

    public static final String GROSS = "GROSS";
    public static final String BASE_SALARY = "BASE_SALARY";
    public static final String BENEFITS_IN_KIND = "BENEFITS_IN_KIND";
    public static final String TAXABLE_NET = "TAXABLE_NET";

    private final Map<String, BigDecimal> slots = new HashMap<>();

    public PayrollCalculationContext(BigDecimal gross, BigDecimal baseSalary,
                                     BigDecimal benefitsInKind, BigDecimal taxableNet) {
        slots.put(GROSS, nz(gross));
        slots.put(BASE_SALARY, nz(baseSalary));
        slots.put(BENEFITS_IN_KIND, nz(benefitsInKind));
        slots.put(TAXABLE_NET, nz(taxableNet));
    }

    public BigDecimal resolve(String reference) {
        if (reference == null) {
            return BigDecimal.ZERO;
        }
        return slots.getOrDefault(reference, BigDecimal.ZERO);
    }

    public void register(String code, BigDecimal amount) {
        if (code != null) {
            slots.put(code, nz(amount));
        }
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
