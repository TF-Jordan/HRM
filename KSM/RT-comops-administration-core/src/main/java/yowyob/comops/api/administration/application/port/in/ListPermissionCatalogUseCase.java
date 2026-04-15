package yowyob.comops.api.administration.application.port.in;

import yowyob.comops.api.administration.domain.model.PermissionCatalogEntry;
import reactor.core.publisher.Flux;

public interface ListPermissionCatalogUseCase {
    Flux<PermissionCatalogEntry> listPermissions();
}
