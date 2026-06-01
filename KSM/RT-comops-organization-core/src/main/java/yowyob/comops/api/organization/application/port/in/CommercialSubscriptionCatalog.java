package yowyob.comops.api.organization.application.port.in;

import java.util.List;

public record CommercialSubscriptionCatalog(
        List<CommercialPlanCatalogEntry> plans,
        List<CommercialAddOnCatalogEntry> addOns) {
}
