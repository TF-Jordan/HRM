package yowyob.comops.api.organization.application.port.in;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface UpdateOrganizationServiceQuotaUseCase {

    Mono<OrganizationServiceEntitlements> updateOrganizationServiceQuota(UUID tenantId, UUID organizationId,
            String serviceCode, Long requestQuotaLimit, Long requestQuotaWindowSeconds);
}
