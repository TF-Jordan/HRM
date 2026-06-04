package yowyob.comops.api.payroll.domain.model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

/**
 * A payroll period expressed as a year-month (format {@code YYYY-MM}).
 *
 * Immutable value object. Persisted as the canonical {@code YYYY-MM} string to stay
 * compatible with the legacy {@code periode} columns and human-readable in events.
 */
public record PayPeriod(int year, int month) {

    public PayPeriod {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be in 1..12, got " + month);
        }
        if (year < 1900 || year > 9999) {
            throw new IllegalArgumentException("year out of range: " + year);
        }
    }

    /** Parses a {@code YYYY-MM} string (e.g. {@code "2026-10"}). */
    public static PayPeriod parse(String value) {
        Objects.requireNonNull(value, "period is required");
        YearMonth ym = YearMonth.parse(value);
        return new PayPeriod(ym.getYear(), ym.getMonthValue());
    }

    public static PayPeriod of(YearMonth yearMonth) {
        return new PayPeriod(yearMonth.getYear(), yearMonth.getMonthValue());
    }

    public YearMonth toYearMonth() {
        return YearMonth.of(year, month);
    }

    public LocalDate firstDay() {
        return toYearMonth().atDay(1);
    }

    public LocalDate lastDay() {
        return toYearMonth().atEndOfMonth();
    }

    public int lengthInDays() {
        return toYearMonth().lengthOfMonth();
    }

    public PayPeriod previous() {
        return of(toYearMonth().minusMonths(1));
    }

    public PayPeriod next() {
        return of(toYearMonth().plusMonths(1));
    }

    /** Canonical {@code YYYY-MM} representation, used for persistence and events. */
    public String format() {
        return String.format("%04d-%02d", year, month);
    }

    @Override
    public String toString() {
        return format();
    }
}
