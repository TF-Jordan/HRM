package yowyob.comops.api.payroll.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TaxBracketTableTest {

    /** Monthly IRPP-style scale mirroring the legacy engine's 4 tranches. */
    private static TaxBracketTable cameroonMonthlyIrpp() {
        return TaxBracketTable.create(UUID.randomUUID(), "IRPP_CM_TEST", "IRPP test", "CM",
                LocalDate.of(2026, 1, 1), null, List.of(
                        new TaxBracket(1, new BigDecimal("0"), new BigDecimal("166667"), new BigDecimal("0.10")),
                        new TaxBracket(2, new BigDecimal("166667"), new BigDecimal("250000"), new BigDecimal("0.15")),
                        new TaxBracket(3, new BigDecimal("250000"), new BigDecimal("416667"), new BigDecimal("0.25")),
                        new TaxBracket(4, new BigDecimal("416667"), null, new BigDecimal("0.35"))));
    }

    @Test
    void zeroBaseYieldsZeroTax() {
        assertThat(cameroonMonthlyIrpp().apply(BigDecimal.ZERO)).isEqualByComparingTo("0");
        assertThat(cameroonMonthlyIrpp().apply(new BigDecimal("-5000"))).isEqualByComparingTo("0");
    }

    @Test
    void taxesWithinFirstBracketOnly() {
        // 100000 * 10% = 10000
        assertThat(cameroonMonthlyIrpp().apply(new BigDecimal("100000")))
                .isEqualByComparingTo("10000");
    }

    @Test
    void taxesAcrossMultipleBrackets() {
        // base 300000:
        //  tranche1: 166667 * 10% = 16666.7
        //  tranche2: (250000-166667)=83333 * 15% = 12499.95
        //  tranche3: (300000-250000)=50000 * 25% = 12500
        //  total = 41666.65 → HALF_UP → 41667
        assertThat(cameroonMonthlyIrpp().apply(new BigDecimal("300000")))
                .isEqualByComparingTo("41667");
    }

    @Test
    void taxesIntoOpenTopBracket() {
        // base 500000:
        //  t1: 16666.7 ; t2: 12499.95 ; t3: 166667*25%=41666.75 ; t4: (500000-416667)=83333*35%=29166.55
        //  total = 99999.95 → 100000
        assertThat(cameroonMonthlyIrpp().apply(new BigDecimal("500000")))
                .isEqualByComparingTo("100000");
    }

    @Test
    void effectivityRespectsDates() {
        TaxBracketTable table = cameroonMonthlyIrpp();
        assertThat(table.isEffectiveOn(LocalDate.of(2026, 6, 1))).isTrue();
        assertThat(table.isEffectiveOn(LocalDate.of(2025, 12, 31))).isFalse();
    }

    @Test
    void bracketsAreSortedRegardlessOfInputOrder() {
        TaxBracketTable table = TaxBracketTable.create(UUID.randomUUID(), "T", "t", "CM",
                LocalDate.of(2026, 1, 1), null, List.of(
                        new TaxBracket(2, new BigDecimal("100000"), null, new BigDecimal("0.20")),
                        new TaxBracket(1, new BigDecimal("0"), new BigDecimal("100000"), new BigDecimal("0.10"))));
        assertThat(table.brackets().get(0).ordre()).isEqualTo(1);
        // 150000: 100000*10% + 50000*20% = 10000 + 10000 = 20000
        assertThat(table.apply(new BigDecimal("150000"))).isEqualByComparingTo("20000");
    }
}
