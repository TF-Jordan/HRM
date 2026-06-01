package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.organization.domain.model.OpeningHoursRule;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record OpeningHoursResponse(UUID id, UUID tenantId, UUID organizationId, UUID agencyId, DayOfWeek dayOfWeek,
        LocalTime opensAt, LocalTime closesAt, boolean closed) {
    public static OpeningHoursResponse from(OpeningHoursRule rule) {
        return new OpeningHoursResponse(rule.id(), rule.tenantId(), rule.organizationId(), rule.agencyId(), rule.dayOfWeek(),
                rule.opensAt(), rule.closesAt(), rule.closed());
    }
}
