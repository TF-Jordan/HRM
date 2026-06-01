package yowyob.comops.api.bootstrap.integration.accounting;

import yowyob.comops.api.accounting.application.port.out.SupplierExposureProvider;
import yowyob.comops.api.accounting.application.port.out.SupplierExposureSnapshot;
import yowyob.comops.api.tp.application.port.out.ThirdPartyRepository;
import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class TpCoreSupplierExposureProvider implements SupplierExposureProvider {

    private final ThirdPartyRepository thirdPartyRepository;

    public TpCoreSupplierExposureProvider(ThirdPartyRepository thirdPartyRepository) {
        this.thirdPartyRepository = thirdPartyRepository;
    }

    @Override
    public Flux<SupplierExposureSnapshot> listSupplierExposures(UUID tenantId, UUID organizationId) {
        return thirdPartyRepository.findByOrganizationId(tenantId, organizationId)
                .filter(thirdParty -> thirdParty.hasRole("SUPPLIER"))
                .map(thirdParty -> new SupplierExposureSnapshot(
                        thirdParty.organizationId(),
                        thirdParty.id(),
                        thirdParty.code(),
                        thirdParty.name(),
                        thirdParty.operationsBalance(),
                        null));
    }
}
