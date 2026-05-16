package yowyob.comops.api.organization.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateOrganizationServiceQuotaRequest(
        @NotNull @Positive Long requestQuotaLimit,
        @NotNull @Positive Long requestQuotaWindowSeconds) {
}
