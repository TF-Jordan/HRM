package yowyob.comops.api.organization.application.port.in;

import java.util.List;

public record CommercialAddOnCatalogEntry(
        String code,
        String displayName,
        String description,
        List<String> serviceCodes,
        List<String> compatiblePlanCodes,
        List<CommercialServiceQuota> serviceQuotas) {
}
