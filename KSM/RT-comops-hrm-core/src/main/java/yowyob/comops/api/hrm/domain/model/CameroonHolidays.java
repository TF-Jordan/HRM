package yowyob.comops.api.hrm.domain.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Cameroon public holidays per the labour code.
 *
 * <p>Fixed holidays:
 * <ul>
 *   <li>01-01 Jour de l'An</li>
 *   <li>02-11 Fete de la Jeunesse</li>
 *   <li>05-01 Fete du Travail</li>
 *   <li>05-20 Fete Nationale</li>
 *   <li>08-15 Assomption</li>
 *   <li>10-01 Jour de la Reunification</li>
 *   <li>12-25 Noel</li>
 * </ul>
 *
 * <p>Movable Christian holidays (Easter-based):
 * Vendredi Saint, Lundi de Paques, Ascension.
 *
 * <p>The 3 Islamic holidays (Aid el-Fitr, Aid el-Kebir, Mouloud) are set
 * annually by presidential decree and must be configured per tenant in
 * settings-core when known.
 */
public final class CameroonHolidays {

    private CameroonHolidays() {}

    public static Set<LocalDate> forYear(int year) {
        Set<LocalDate> holidays = new HashSet<>();
        holidays.add(LocalDate.of(year, 1, 1));
        holidays.add(LocalDate.of(year, 2, 11));
        holidays.add(LocalDate.of(year, 5, 1));
        holidays.add(LocalDate.of(year, 5, 20));
        holidays.add(LocalDate.of(year, 8, 15));
        holidays.add(LocalDate.of(year, 10, 1));
        holidays.add(LocalDate.of(year, 12, 25));

        LocalDate easter = easterSunday(year);
        holidays.add(easter.minusDays(2));   // Vendredi Saint
        holidays.add(easter.plusDays(1));     // Lundi de Paques
        holidays.add(easter.plusDays(39));    // Ascension

        return holidays;
    }

    /**
     * Computus (Gauss algorithm) — Gregorian Easter Sunday for the given year.
     */
    static LocalDate easterSunday(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(year, month, day);
    }

    /**
     * Counts <em>jours ouvrables</em> (Mon–Sat) between two dates (inclusive),
     * excluding Sundays and Cameroon public holidays.
     * This matches the Cameroon labour code definition of working days.
     */
    public static long joursOuvrables(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) return 0;
        Set<LocalDate> holidays = new HashSet<>();
        for (int y = start.getYear(); y <= end.getYear(); y++) {
            holidays.addAll(forYear(y));
        }
        long count = 0;
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            if (cursor.getDayOfWeek() != DayOfWeek.SUNDAY && !holidays.contains(cursor)) {
                count++;
            }
            cursor = cursor.plusDays(1);
        }
        return count;
    }
}
