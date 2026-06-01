package yowyob.comops.api.organization.application.port.in;

import java.util.List;

public record CommercialPlanCatalogEntry(
        String code,
        String displayName,
        String description,
        List<String> packCodes,
        List<String> serviceCodes,
        List<String> compatibleAddOnCodes,
        List<CommercialServiceQuota> serviceQuotas) {
}
