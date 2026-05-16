package yowyob.comops.api.organization.application.port.in;

public record OrganizationServiceRuntimePolicy(
        String serviceCode,
        boolean effective,
        Long requestQuotaLimit,
        Long requestQuotaWindowSeconds) {
}
