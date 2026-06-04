package yowyob.comops.api.payroll.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LookupTableTest {

    /** RAV-style stepped forfait (subset of the legacy scale). */
    private static LookupTable rav() {
        return LookupTable.create(UUID.randomUUID(), "RAV_CM_TEST", "RAV test", "CM",
                LocalDate.of(2026, 1, 1), null, List.of(
                        new LookupTableEntry(1, new BigDecimal("0"), new BigDecimal("50000"), new BigDecimal("0")),
                        new LookupTableEntry(2, new BigDecimal("50001"), new BigDecimal("100000"), new BigDecimal("750")),
                        new LookupTableEntry(3, new BigDecimal("100001"), new BigDecimal("200000"), new BigDecimal("1950")),
                        new LookupTableEntry(4, new BigDecimal("200001"), null, new BigDecimal("3250"))));
    }

    @Test
    void resolvesForfaitFromMatchingStep() {
        assertThat(rav().resolve(new BigDecimal("40000"))).isEqualByComparingTo("0");
        assertThat(rav().resolve(new BigDecimal("80000"))).isEqualByComparingTo("750");
        assertThat(rav().resolve(new BigDecimal("150000"))).isEqualByComparingTo("1950");
    }

    @Test
    void resolvesOpenTopStep() {
        assertThat(rav().resolve(new BigDecimal("5000000"))).isEqualByComparingTo("3250");
    }

    @Test
    void returnsZeroWhenNoStepMatches() {
        LookupTable table = LookupTable.create(UUID.randomUUID(), "T", "t", "CM",
                LocalDate.of(2026, 1, 1), null, List.of(
                        new LookupTableEntry(1, new BigDecimal("100000"), new BigDecimal("200000"), new BigDecimal("500"))));
        assertThat(table.resolve(new BigDecimal("50000"))).isEqualByComparingTo("0");
        assertThat(table.resolve(null)).isEqualByComparingTo("0");
    }
}
