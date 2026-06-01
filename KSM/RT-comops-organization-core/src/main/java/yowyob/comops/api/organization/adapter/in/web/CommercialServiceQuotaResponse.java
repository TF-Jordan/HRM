package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.organization.application.port.in.CommercialServiceQuota;

public record CommercialServiceQuotaResponse(
        String serviceCode,
        long requestQuotaLimit,
        long requestQuotaWindowSeconds) {

    static CommercialServiceQuotaResponse from(CommercialServiceQuota quota) {
        return new CommercialServiceQuotaResponse(
                quota.serviceCode(),
                quota.requestQuotaLimit(),
                quota.requestQuotaWindowSeconds());
    }
}
