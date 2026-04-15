package yowyob.comops.api.accounting.application.port.out;

import java.util.UUID;
import reactor.core.publisher.Flux;

public interface SupplierExposureProvider {

    Flux<SupplierExposureSnapshot> listSupplierExposures(UUID tenantId, UUID organizationId);
}
