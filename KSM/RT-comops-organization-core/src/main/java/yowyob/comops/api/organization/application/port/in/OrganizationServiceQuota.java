package yowyob.comops.api.organization.application.port.in;

public record OrganizationServiceQuota(
        String serviceCode,
        long requestQuotaLimit,
        long requestQuotaWindowSeconds) {
}
