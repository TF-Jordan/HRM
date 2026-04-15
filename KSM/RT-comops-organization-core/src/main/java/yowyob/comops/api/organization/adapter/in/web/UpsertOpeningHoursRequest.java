package yowyob.comops.api.organization.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record UpsertOpeningHoursRequest(
        @NotNull UUID organizationId,
        @NotNull UUID agencyId,
        @NotNull DayOfWeek dayOfWeek,
        LocalTime opensAt,
        LocalTime closesAt,
        boolean closed) {
}
