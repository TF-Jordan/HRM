package yowyob.comops.api.organization.application.port.in;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record UpsertOpeningHoursCommand(
        UUID tenantId,
        UUID organizationId,
        UUID agencyId,
        DayOfWeek dayOfWeek,
        LocalTime opensAt,
        LocalTime closesAt,
        boolean closed) {
}
