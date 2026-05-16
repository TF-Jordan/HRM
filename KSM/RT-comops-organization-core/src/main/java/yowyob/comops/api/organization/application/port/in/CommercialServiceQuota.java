package yowyob.comops.api.organization.application.port.in;

public record CommercialServiceQuota(
        String serviceCode,
        long requestQuotaLimit,
        long requestQuotaWindowSeconds) {
}
