package yowyob.comops.api.payroll.application.service;

import org.junit.jupiter.api.Test;
import yowyob.comops.api.payroll.domain.model.PayPeriod;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ProrationCalculatorTest {

    private final PayPeriod october = PayPeriod.parse("2026-10"); // 31 days

    @Test
    void fullMonthWhenSpanningEntirePeriod() {
        assertThat(ProrationCalculator.workedDays(october, null, null)).isEqualTo(31);
        assertThat(ProrationCalculator.workedDays(october, LocalDate.of(2020, 1, 1), null)).isEqualTo(31);
        assertThat(ProrationCalculator.prorate(new BigDecimal("310000"), 31, 31))
                .isEqualByComparingTo("310000");
    }

    @Test
    void midMonthArrivalCountsFromHireDate() {
        // hired Oct 17 → days 17..31 inclusive = 15 days
        assertThat(ProrationCalculator.workedDays(october, LocalDate.of(2026, 10, 17), null))
                .isEqualTo(15);
        // 310000 * 15/31 = 150000
        assertThat(ProrationCalculator.prorate(new BigDecimal("310000"), 15, 31))
                .isEqualByComparingTo("150000");
    }

    @Test
    void midMonthDepartureCountsUntilDepartureDate() {
        // left Oct 10 → days 1..10 inclusive = 10 days
        assertThat(ProrationCalculator.workedDays(october, null, LocalDate.of(2026, 10, 10)))
                .isEqualTo(10);
    }

    @Test
    void noOverlapYieldsZero() {
        assertThat(ProrationCalculator.workedDays(october, LocalDate.of(2026, 11, 1), null)).isZero();
        assertThat(ProrationCalculator.prorate(new BigDecimal("310000"), 0, 31))
                .isEqualByComparingTo("0");
    }
}
