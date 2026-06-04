package yowyob.comops.api.payroll.application.service;

import org.junit.jupiter.api.Test;
import yowyob.comops.api.payroll.domain.model.CalculationMethod;
import yowyob.comops.api.payroll.domain.model.LookupTable;
import yowyob.comops.api.payroll.domain.model.LookupTableEntry;
import yowyob.comops.api.payroll.domain.model.PayElement;
import yowyob.comops.api.payroll.domain.model.PayElementCategory;
import yowyob.comops.api.payroll.domain.model.TaxBracket;
import yowyob.comops.api.payroll.domain.model.TaxBracketTable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Golden test: the configurable engine must reproduce, to the franc, the numbers the legacy
 * hard-coded hrm-core engine produced for a representative Cameroonian salary (brut 400 000 XAF).
 */
class PayrollCalculationEngineTest {

    private static final UUID T = UUID.randomUUID();
    private static final LocalDate FROM = LocalDate.of(2026, 1, 1);

    private static TaxBracketTable irppTable() {
        return TaxBracketTable.create(T, "IRPP_CM", "IRPP Cameroun", "CM", FROM, null, List.of(
                new TaxBracket(1, bd("0"), bd("166667"), bd("0.10")),
                new TaxBracket(2, bd("166667"), bd("250000"), bd("0.15")),
                new TaxBracket(3, bd("250000"), bd("416667"), bd("0.25")),
                new TaxBracket(4, bd("416667"), null, bd("0.35"))));
    }

    private static LookupTable ravTable() {
        return LookupTable.create(T, "RAV_CM", "RAV", "CM", FROM, null, List.of(
                new LookupTableEntry(1, bd("0"), bd("50000"), bd("0")),
                new LookupTableEntry(2, bd("50001"), bd("100000"), bd("750")),
                new LookupTableEntry(3, bd("100001"), bd("200000"), bd("1950")),
                new LookupTableEntry(4, bd("200001"), bd("300000"), bd("3250")),
                new LookupTableEntry(5, bd("300001"), bd("400000"), bd("4550")),
                new LookupTableEntry(6, bd("400001"), null, bd("5850"))));
    }

    /** TDL on base salary, monthly = annual/12. For base ≤ 500 000 → 27 000/12 = 2 250. */
    private static LookupTable tdlTable() {
        return LookupTable.create(T, "TDL_CM", "TDL", "CM", FROM, null, List.of(
                new LookupTableEntry(1, bd("0"), bd("61999"), bd("0")),
                new LookupTableEntry(2, bd("62000"), bd("500000"), bd("2250")),
                new LookupTableEntry(3, bd("500001"), null, bd("2500"))));
    }

    private static PayElement rate(String code, String label, PayElementCategory cat, String baseRef,
                                   String rate, String ceiling, String exemption, int order) {
        return PayElement.create(T, code, label, cat, CalculationMethod.RATE, baseRef,
                bd(rate), ceiling == null ? null : bd(ceiling), null,
                exemption == null ? null : bd(exemption), null, null, null,
                cat == PayElementCategory.DEDUCTION, true, "CM", order, FROM, null);
    }

    private static PayElement bracket(String code, String label, String baseRef, String table, int order) {
        return PayElement.create(T, code, label, PayElementCategory.DEDUCTION, CalculationMethod.BRACKET,
                baseRef, null, null, null, bd("62000"), null, table, null, true, true, "CM", order, FROM, null);
    }

    private static PayElement lookup(String code, String label, String baseRef, String table, int order) {
        return PayElement.create(T, code, label, PayElementCategory.DEDUCTION, CalculationMethod.LOOKUP_TABLE,
                baseRef, null, null, null, null, null, null, table, true, false, "CM", order, FROM, null);
    }

    private CalculationRequest cameroonRequest(BigDecimal baseSalary, BigDecimal benefits) {
        GrossComponents gross = new GrossComponents(baseSalary, baseSalary, benefits,
                BigDecimal.ZERO, BigDecimal.ZERO);

        List<PayElement> elements = List.of(
                rate("CNPS_PV_EE", "CNPS (part salariale)", PayElementCategory.DEDUCTION,
                        PayrollCalculationContext.GROSS, "0.042", "750000", null, 10),
                bracket("IRPP", "IRPP", PayrollCalculationContext.TAXABLE_NET, "IRPP_CM", 11),
                rate("CAC", "CAC", PayElementCategory.DEDUCTION, "IRPP", "0.10", null, null, 12),
                rate("CFC_EE", "CFC (part salariale)", PayElementCategory.DEDUCTION,
                        PayrollCalculationContext.GROSS, "0.01", null, "62000", 13),
                lookup("RAV", "RAV", PayrollCalculationContext.GROSS, "RAV_CM", 14),
                lookup("TDL", "TDL", PayrollCalculationContext.BASE_SALARY, "TDL_CM", 15),
                // employer charges (informational on the payslip, excluded from net)
                rate("CNPS_PV_ER", "CNPS PV (patronale)", PayElementCategory.EMPLOYER_CHARGE,
                        PayrollCalculationContext.GROSS, "0.042", "750000", null, 20),
                rate("CNPS_AF_ER", "CNPS Allocations familiales", PayElementCategory.EMPLOYER_CHARGE,
                        PayrollCalculationContext.GROSS, "0.07", null, null, 21),
                rate("CNPS_AT_ER", "CNPS Accidents du travail", PayElementCategory.EMPLOYER_CHARGE,
                        PayrollCalculationContext.GROSS, "0.0175", null, null, 22));

        return new CalculationRequest(gross, bd("0.30"), bd("400000"), elements,
                Map.of("IRPP_CM", irppTable()),
                Map.of("RAV_CM", ravTable(), "TDL_CM", tdlTable()),
                BigDecimal.ZERO, Set.of("IRPP", "CAC"));
    }

    @Test
    void reproducesLegacyNumbersForBrut400k() {
        CalculationResult r = PayrollCalculationEngine.calculate(
                cameroonRequest(bd("400000"), BigDecimal.ZERO));

        assertThat(r.gross()).isEqualByComparingTo("400000");
        assertThat(r.taxableNet()).isEqualByComparingTo("280000");

        // CNPS 16800 + IRPP 36667 + CAC 3667 + CFC 4000 + RAV 4550 + TDL 2250 = 67934
        assertThat(r.totalDeductions()).isEqualByComparingTo("67934");
        assertThat(r.incomeTax()).isEqualByComparingTo("40334");   // IRPP 36667 + CAC 3667
        assertThat(r.employerCharges()).isEqualByComparingTo("51800"); // 16800 + 28000 + 7000
        assertThat(r.net()).isEqualByComparingTo("332066");        // 400000 - 67934
    }

    @Test
    void individualLinesMatchLegacyComponents() {
        CalculationResult r = PayrollCalculationEngine.calculate(
                cameroonRequest(bd("400000"), BigDecimal.ZERO));

        assertThat(amount(r, "CNPS_PV_EE")).isEqualByComparingTo("16800");
        assertThat(amount(r, "IRPP")).isEqualByComparingTo("36667");
        assertThat(amount(r, "CAC")).isEqualByComparingTo("3667");
        assertThat(amount(r, "CFC_EE")).isEqualByComparingTo("4000");
        assertThat(amount(r, "RAV")).isEqualByComparingTo("4550");
        assertThat(amount(r, "TDL")).isEqualByComparingTo("2250");
        assertThat(amount(r, "CNPS_AF_ER")).isEqualByComparingTo("28000");
    }

    @Test
    void exemptsLowEarnersFromIrppAndCfc() {
        // brut 50 000 < 62 000 → IRPP, CAC and CFC are zero; CNPS and RAV(0 step) still apply
        CalculationResult r = PayrollCalculationEngine.calculate(
                cameroonRequest(bd("50000"), BigDecimal.ZERO));

        assertThat(amount(r, "IRPP")).isEqualByComparingTo("0");
        assertThat(amount(r, "CAC")).isEqualByComparingTo("0");
        assertThat(amount(r, "CFC_EE")).isEqualByComparingTo("0");
        assertThat(amount(r, "CNPS_PV_EE")).isEqualByComparingTo("2100"); // 50000 * 4.2%
    }

    @Test
    void capsCnpsAtCeilingForHighSalaries() {
        // base 1 000 000 > 750 000 ceiling → CNPS = 750000 * 4.2% = 31500
        CalculationResult r = PayrollCalculationEngine.calculate(
                cameroonRequest(bd("1000000"), BigDecimal.ZERO));
        assertThat(amount(r, "CNPS_PV_EE")).isEqualByComparingTo("31500");
    }

    @Test
    void benefitsInKindRaiseGrossAndAppearAsEarning() {
        CalculationResult r = PayrollCalculationEngine.calculate(
                cameroonRequest(bd("400000"), bd("50000")));
        assertThat(r.gross()).isEqualByComparingTo("450000");
        assertThat(r.linesOf(PayElementCategory.EARNING))
                .anyMatch(l -> "AVANTAGES_NATURE".equals(l.code())
                        && l.amount().compareTo(bd("50000")) == 0);
    }

    private static BigDecimal amount(CalculationResult r, String code) {
        return r.lines().stream()
                .filter(l -> code.equals(l.code()))
                .map(ComputedLine::amount)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no line for " + code));
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }
}
