package yowyob.comops.api.organization.application.port.in;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AddOpeningHoursExceptionCommand(
        UUID tenantId,
        UUID organizationId,
        UUID agencyId,
        LocalDate exceptionDate,
        String label,
        LocalTime opensAt,
        LocalTime closesAt,
        boolean closed) {
}
