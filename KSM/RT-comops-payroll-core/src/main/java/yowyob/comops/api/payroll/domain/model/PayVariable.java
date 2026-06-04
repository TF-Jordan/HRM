package yowyob.comops.api.payroll.domain.model;

import yowyob.comops.api.common.domain.model.BaseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Per-employee, per-period variable inputs captured before a run is calculated:
 * overtime, ad-hoc bonuses, unpaid absences, advances, and an optional worked-days
 * override that drives proration for mid-month arrivals/departures.
 */
public final class PayVariable extends BaseEntity {

    private final UUID organizationId;
    private final UUID employeeId;
    private final PayPeriod period;
    private final BigDecimal overtimeHoursDay;
    private final BigDecimal overtimeHoursNight;
    private final BigDecimal overtimeHoursSundayHoliday;
    private final BigDecimal bonuses;
    private final BigDecimal unpaidAbsenceDays;
    private final BigDecimal advances;
    private final Integer workedDaysOverride;
    private final boolean locked;

    private PayVariable(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                        UUID organizationId, UUID employeeId, PayPeriod period,
                        BigDecimal overtimeHoursDay, BigDecimal overtimeHoursNight,
                        BigDecimal overtimeHoursSundayHoliday, BigDecimal bonuses,
                        BigDecimal unpaidAbsenceDays, BigDecimal advances,
                        Integer workedDaysOverride, boolean locked) {
        super(id, tenantId, createdAt, updatedAt);
        this.organizationId = Objects.requireNonNull(organizationId, "organizationId is required");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId is required");
        this.period = Objects.requireNonNull(period, "period is required");
        this.overtimeHoursDay = nullToZero(overtimeHoursDay);
        this.overtimeHoursNight = nullToZero(overtimeHoursNight);
        this.overtimeHoursSundayHoliday = nullToZero(overtimeHoursSundayHoliday);
        this.bonuses = nullToZero(bonuses);
        this.unpaidAbsenceDays = nullToZero(unpaidAbsenceDays);
        this.advances = nullToZero(advances);
        this.workedDaysOverride = workedDaysOverride;
        this.locked = locked;
    }

    public static PayVariable create(UUID tenantId, UUID organizationId, UUID employeeId, PayPeriod period,
                                     BigDecimal overtimeHoursDay, BigDecimal overtimeHoursNight,
                                     BigDecimal overtimeHoursSundayHoliday, BigDecimal bonuses,
                                     BigDecimal unpaidAbsenceDays, BigDecimal advances,
                                     Integer workedDaysOverride) {
        Instant now = Instant.now();
        return new PayVariable(UUID.randomUUID(), tenantId, now, now, organizationId, employeeId, period,
                overtimeHoursDay, overtimeHoursNight, overtimeHoursSundayHoliday, bonuses,
                unpaidAbsenceDays, advances, workedDaysOverride, false);
    }

    public static PayVariable rehydrate(UUID id, UUID tenantId, Instant createdAt, Instant updatedAt,
                                        UUID organizationId, UUID employeeId, PayPeriod period,
                                        BigDecimal overtimeHoursDay, BigDecimal overtimeHoursNight,
                                        BigDecimal overtimeHoursSundayHoliday, BigDecimal bonuses,
                                        BigDecimal unpaidAbsenceDays, BigDecimal advances,
                                        Integer workedDaysOverride, boolean locked) {
        return new PayVariable(id, tenantId, createdAt, updatedAt, organizationId, employeeId, period,
                overtimeHoursDay, overtimeHoursNight, overtimeHoursSundayHoliday, bonuses,
                unpaidAbsenceDays, advances, workedDaysOverride, locked);
    }

    public PayVariable lock() {
        return new PayVariable(id(), tenantId(), createdAt(), Instant.now(), organizationId, employeeId,
                period, overtimeHoursDay, overtimeHoursNight, overtimeHoursSundayHoliday, bonuses,
                unpaidAbsenceDays, advances, workedDaysOverride, true);
    }

    private static BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public UUID organizationId() { return organizationId; }
    public UUID employeeId() { return employeeId; }
    public PayPeriod period() { return period; }
    public BigDecimal overtimeHoursDay() { return overtimeHoursDay; }
    public BigDecimal overtimeHoursNight() { return overtimeHoursNight; }
    public BigDecimal overtimeHoursSundayHoliday() { return overtimeHoursSundayHoliday; }
    public BigDecimal bonuses() { return bonuses; }
    public BigDecimal unpaidAbsenceDays() { return unpaidAbsenceDays; }
    public BigDecimal advances() { return advances; }
    public Integer workedDaysOverride() { return workedDaysOverride; }
    public boolean locked() { return locked; }
}
