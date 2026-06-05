package yowyob.comops.api.payroll.application.port.in;

import yowyob.comops.api.payroll.domain.model.TaxBracket;

import java.time.LocalDate;
import java.util.List;

/**
 * Command to define a new {@link yowyob.comops.api.payroll.domain.model.TaxBracketTable}
 * (a versioned, country-scoped progressive scale). The tenant is taken from the request context.
 */
public record CreateTaxBracketTableCommand(
        String code,
        String label,
        String countryCode,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        List<TaxBracket> brackets) {
}
