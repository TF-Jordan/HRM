package yowyob.comops.api.organization.application.port.in;

import reactor.core.publisher.Flux;

public interface ListPlatformServicePacksUseCase {

    Flux<OrganizationServicePackEntry> listPlatformServicePacks();
}
