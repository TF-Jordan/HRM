package yowyob.comops.api.tp.application.port.out;

import yowyob.comops.api.tp.domain.model.ThirdPartySearchResult;
import java.util.UUID;
import reactor.core.publisher.Flux;

public interface ThirdPartySearchGateway {

    Flux<ThirdPartySearchResult> search(UUID tenantId, UUID organizationId, String query, String role, Boolean prospect,
            String segment, Integer minimumQualificationScore, Boolean active, String followUpStatus, int page,
            int size);
}
