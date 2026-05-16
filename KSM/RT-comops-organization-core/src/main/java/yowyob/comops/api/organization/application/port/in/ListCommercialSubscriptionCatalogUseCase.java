package yowyob.comops.api.organization.application.port.in;

import reactor.core.publisher.Mono;

public interface ListCommercialSubscriptionCatalogUseCase {

    Mono<CommercialSubscriptionCatalog> listCommercialSubscriptionCatalog();
}
