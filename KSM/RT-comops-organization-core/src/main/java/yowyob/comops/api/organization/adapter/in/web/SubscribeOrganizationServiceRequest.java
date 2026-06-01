package yowyob.comops.api.organization.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record SubscribeOrganizationServiceRequest(
        @NotBlank String serviceCode,
        @Positive Long requestQuotaLimit,
        @Positive Long requestQuotaWindowSeconds) {
}
