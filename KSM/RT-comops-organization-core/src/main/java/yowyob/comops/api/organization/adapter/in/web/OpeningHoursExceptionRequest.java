package yowyob.comops.api.organization.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record OpeningHoursExceptionRequest(
        @NotNull LocalDate exceptionDate,
        String label,
        LocalTime opensAt,
        LocalTime closesAt,
        boolean closed) {
}
