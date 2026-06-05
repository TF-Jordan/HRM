package yowyob.comops.api.payroll.application.port.in;

import yowyob.comops.api.payroll.domain.model.LookupTableEntry;

import java.time.LocalDate;
import java.util.List;

/**
 * Command to define a new {@link yowyob.comops.api.payroll.domain.model.LookupTable}
 * (a versioned, country-scoped stepped forfait scale, e.g. RAV/TDL). The tenant is taken
 * from the request context.
 */
public record CreateLookupTableCommand(
        String code,
        String label,
        String countryCode,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        List<LookupTableEntry> entries) {
}
