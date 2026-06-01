package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.organization.application.port.in.CommercialPlanCatalogEntry;
import java.util.List;

public record CommercialPlanCatalogResponse(
        String code,
        String displayName,
        String description,
        List<String> packCodes,
        List<String> serviceCodes,
        List<String> compatibleAddOnCodes,
        List<CommercialServiceQuotaResponse> serviceQuotas) {

    static CommercialPlanCatalogResponse from(CommercialPlanCatalogEntry entry) {
        return new CommercialPlanCatalogResponse(
                entry.code(),
                entry.displayName(),
                entry.description(),
                entry.packCodes(),
                entry.serviceCodes(),
                entry.compatibleAddOnCodes(),
                entry.serviceQuotas().stream().map(CommercialServiceQuotaResponse::from).toList());
    }
}
