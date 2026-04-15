package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.organization.application.port.in.OrganizationServiceQuota;

public record OrganizationServiceQuotaResponse(
        String serviceCode,
        long requestQuotaLimit,
        long requestQuotaWindowSeconds) {

    public static OrganizationServiceQuotaResponse from(OrganizationServiceQuota quota) {
        return new OrganizationServiceQuotaResponse(quota.serviceCode(), quota.requestQuotaLimit(),
                quota.requestQuotaWindowSeconds());
    }
}
