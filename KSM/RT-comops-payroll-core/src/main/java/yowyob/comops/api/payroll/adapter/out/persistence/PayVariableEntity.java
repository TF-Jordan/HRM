package yowyob.comops.api.payroll.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "payroll_pay_variable")
public record PayVariableEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        UUID employeeId,
        String periode,
        BigDecimal overtimeHoursDay,
        BigDecimal overtimeHoursNight,
        BigDecimal overtimeHoursSundayHoliday,
        BigDecimal bonuses,
        BigDecimal unpaidAbsenceDays,
        BigDecimal advances,
        Integer workedDaysOverride,
        boolean locked) implements PersistableEntity {
}
