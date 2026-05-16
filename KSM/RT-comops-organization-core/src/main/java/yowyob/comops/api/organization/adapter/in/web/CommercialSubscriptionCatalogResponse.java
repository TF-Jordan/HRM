package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.organization.application.port.in.CommercialSubscriptionCatalog;
import java.util.List;

public record CommercialSubscriptionCatalogResponse(
        List<CommercialPlanCatalogResponse> plans,
        List<CommercialAddOnCatalogResponse> addOns) {

    static CommercialSubscriptionCatalogResponse from(CommercialSubscriptionCatalog catalog) {
        return new CommercialSubscriptionCatalogResponse(
                catalog.plans().stream().map(CommercialPlanCatalogResponse::from).toList(),
                catalog.addOns().stream().map(CommercialAddOnCatalogResponse::from).toList());
    }
}
