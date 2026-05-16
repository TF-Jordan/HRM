package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.organization.application.port.in.CommercialAddOnCatalogEntry;
import java.util.List;

public record CommercialAddOnCatalogResponse(
        String code,
        String displayName,
        String description,
        List<String> serviceCodes,
        List<String> compatiblePlanCodes,
        List<CommercialServiceQuotaResponse> serviceQuotas) {

    static CommercialAddOnCatalogResponse from(CommercialAddOnCatalogEntry entry) {
        return new CommercialAddOnCatalogResponse(
                entry.code(),
                entry.displayName(),
                entry.description(),
                entry.serviceCodes(),
                entry.compatiblePlanCodes(),
                entry.serviceQuotas().stream().map(CommercialServiceQuotaResponse::from).toList());
    }
}
