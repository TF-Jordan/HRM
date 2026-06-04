package yowyob.comops.api.payroll.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PayPeriodTest {

    @Test
    void parsesAndFormatsCanonicalString() {
        PayPeriod period = PayPeriod.parse("2026-10");
        assertThat(period.year()).isEqualTo(2026);
        assertThat(period.month()).isEqualTo(10);
        assertThat(period.format()).isEqualTo("2026-10");
        assertThat(period).hasToString("2026-10");
    }

    @Test
    void exposesBoundsAndLength() {
        PayPeriod february = PayPeriod.of(java.time.YearMonth.of(2024, 2));
        assertThat(february.firstDay()).isEqualTo(LocalDate.of(2024, 2, 1));
        assertThat(february.lastDay()).isEqualTo(LocalDate.of(2024, 2, 29)); // leap year
        assertThat(february.lengthInDays()).isEqualTo(29);
    }

    @Test
    void navigatesAcrossYearBoundaries() {
        assertThat(PayPeriod.parse("2026-01").previous().format()).isEqualTo("2025-12");
        assertThat(PayPeriod.parse("2026-12").next().format()).isEqualTo("2027-01");
    }

    @Test
    void rejectsInvalidMonth() {
        assertThatThrownBy(() -> new PayPeriod(2026, 13))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
