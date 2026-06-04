package yowyob.comops.api.payroll.application.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OvertimeCalculatorTest {

    // 173.33 monthly hours ≈ 40h/week. Base 173330 → hourly rate ≈ 1000.
    private static final BigDecimal LEGAL_HOURS = new BigDecimal("173.33");

    @Test
    void hourlyRateDerivedFromBase() {
        assertThat(OvertimeCalculator.hourlyRate(new BigDecimal("173330"), LEGAL_HOURS))
                .isEqualByComparingTo(new BigDecimal("1000.000000"));
    }

    @Test
    void dayTierPaidAtMultiplier() {
        // 10h at +25% on a 1000/h rate = 10 * 1000 * 1.25 = 12500
        BigDecimal amount = OvertimeCalculator.compute(new BigDecimal("173330"), LEGAL_HOURS,
                new BigDecimal("10"), new BigDecimal("1.25"),
                BigDecimal.ZERO, new BigDecimal("1.50"),
                BigDecimal.ZERO, new BigDecimal("1.75"));
        assertThat(amount).isEqualByComparingTo("12500");
    }

    @Test
    void sumsAllThreeTiers() {
        // day 4h*1.25=5000 ; night 2h*1.50=3000 ; sunday 1h*1.75=1750 ; total 9750 (rate 1000)
        BigDecimal amount = OvertimeCalculator.compute(new BigDecimal("173330"), LEGAL_HOURS,
                new BigDecimal("4"), new BigDecimal("1.25"),
                new BigDecimal("2"), new BigDecimal("1.50"),
                new BigDecimal("1"), new BigDecimal("1.75"));
        assertThat(amount).isEqualByComparingTo("9750");
    }

    @Test
    void zeroHoursYieldsZero() {
        BigDecimal amount = OvertimeCalculator.compute(new BigDecimal("173330"), LEGAL_HOURS,
                BigDecimal.ZERO, new BigDecimal("1.25"),
                BigDecimal.ZERO, new BigDecimal("1.50"),
                BigDecimal.ZERO, new BigDecimal("1.75"));
        assertThat(amount).isEqualByComparingTo("0");
    }
}
