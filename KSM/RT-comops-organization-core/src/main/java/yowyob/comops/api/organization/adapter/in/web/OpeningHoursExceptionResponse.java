package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.organization.domain.model.OpeningHoursExceptionRule;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record OpeningHoursExceptionResponse(
        UUID id,
        UUID organizationId,
        UUID agencyId,
        LocalDate exceptionDate,
        String label,
        LocalTime opensAt,
        LocalTime closesAt,
        boolean closed) {

    public static OpeningHoursExceptionResponse from(OpeningHoursExceptionRule rule) {
        return new OpeningHoursExceptionResponse(rule.id(), rule.organizationId(), rule.agencyId(), rule.exceptionDate(),
                rule.label(), rule.opensAt(), rule.closesAt(), rule.closed());
    }
}
