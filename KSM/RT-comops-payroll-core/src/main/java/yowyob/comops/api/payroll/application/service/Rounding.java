package yowyob.comops.api.payroll.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Monetary rounding helper. XAF (and CFA francs generally) have no minor unit, so amounts are
 * rounded to the nearest whole unit with HALF_UP — consistent with Cameroonian payroll practice
 * and the legacy engine.
 */
public final class Rounding {

    private Rounding() {}

    public static BigDecimal money(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(0, RoundingMode.HALF_UP);
    }

    /** Clamps a base into {@code [floor, ceiling]}; null bounds mean unbounded. */
    public static BigDecimal clamp(BigDecimal base, BigDecimal floor, BigDecimal ceiling) {
        BigDecimal result = base;
        if (floor != null && result.compareTo(floor) < 0) {
            result = floor;
        }
        if (ceiling != null && result.compareTo(ceiling) > 0) {
            result = ceiling;
        }
        return result;
    }
}
