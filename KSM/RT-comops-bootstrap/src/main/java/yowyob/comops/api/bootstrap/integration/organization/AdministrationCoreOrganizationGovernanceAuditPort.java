package yowyob.comops.api.bootstrap.integration.organization;

import yowyob.comops.api.administration.application.port.out.AdminAuditRepository;
import yowyob.comops.api.administration.domain.model.AdminAuditEntry;
import yowyob.comops.api.organization.application.port.out.OrganizationGovernanceAuditPort;
import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AdministrationCoreOrganizationGovernanceAuditPort implements OrganizationGovernanceAuditPort {

    private final AdminAuditRepository adminAuditRepository;

    public AdministrationCoreOrganizationGovernanceAuditPort(AdminAuditRepository adminAuditRepository) {
        this.adminAuditRepository = adminAuditRepository;
    }

    @Override
    public Mono<Void> recordAction(UUID tenantId, UUID organizationId, UUID actorUserId, String action,
            String targetType, UUID targetId, String payloadSummary) {
        return adminAuditRepository.save(AdminAuditEntry.record(tenantId, organizationId, actorUserId, action,
                targetType, targetId.toString(), payloadSummary))
                .then();
    }
}
