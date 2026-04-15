package yowyob.comops.api.inventory.application.service;

import yowyob.comops.api.inventory.application.port.out.GeneralizedInventoryCampaignRepository;
import yowyob.comops.api.inventory.domain.model.GeneralizedInventoryCampaign;
import yowyob.comops.api.organization.application.port.out.AgencyRepository;
import yowyob.comops.api.organization.application.port.out.OrganizationRepository;
import yowyob.comops.api.organization.application.port.out.PhysicalSpaceRepository;
import yowyob.comops.api.settings.application.service.OperationalPolicyApplicationService;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class GeneralizedInventoryCampaignApplicationService {

    private final GeneralizedInventoryCampaignRepository generalizedInventoryCampaignRepository;
    private final OrganizationRepository organizationRepository;
    private final AgencyRepository agencyRepository;
    private final PhysicalSpaceRepository physicalSpaceRepository;
    private final OperationalPolicyApplicationService operationalPolicyApplicationService;

    public GeneralizedInventoryCampaignApplicationService(
            GeneralizedInventoryCampaignRepository generalizedInventoryCampaignRepository,
            OrganizationRepository organizationRepository,
            AgencyRepository agencyRepository,
            PhysicalSpaceRepository physicalSpaceRepository,
            OperationalPolicyApplicationService operationalPolicyApplicationService) {
        this.generalizedInventoryCampaignRepository = generalizedInventoryCampaignRepository;
        this.organizationRepository = organizationRepository;
        this.agencyRepository = agencyRepository;
        this.physicalSpaceRepository = physicalSpaceRepository;
        this.operationalPolicyApplicationService = operationalPolicyApplicationService;
    }

    public Mono<GeneralizedInventoryCampaign> plan(UUID tenantId, UUID organizationId,
            PlanGeneralizedInventoryCampaignCommand command) {
        return validateScope(tenantId, organizationId, command.agencyId(), command.physicalSpaceId())
                .then(operationalPolicyApplicationService.get(tenantId, organizationId, command.agencyId()))
                .flatMap(policy -> generalizedInventoryCampaignRepository.findByOrganizationId(tenantId, organizationId)
                        .filter(campaign -> campaign.agencyId() == null || command.agencyId() == null
                                || command.agencyId().equals(campaign.agencyId()))
                        .filter(campaign -> "PLANNED".equals(campaign.status()) || "IN_PROGRESS".equals(campaign.status())
                                || "PENDING_APPROVAL".equals(campaign.status()))
                        .count()
                        .flatMap(openCount -> openCount >= policy.maxOpenInventoryCampaigns()
                                ? Mono.error(new IllegalArgumentException("maximum open inventory campaigns reached"))
                                : generalizedInventoryCampaignRepository.save(GeneralizedInventoryCampaign.plan(
                                        tenantId, organizationId, command.agencyId(), command.warehouseId(),
                                        command.physicalSpaceId(), command.supervisorActorId(), command.campaignCode(),
                                        command.campaignType(), policy.requireInventorySupervisorApproval(),
                                        command.scopeType(), command.scheduledAt(), command.notes()))));
    }

    public Flux<GeneralizedInventoryCampaign> list(UUID tenantId, UUID organizationId, UUID agencyId) {
        Flux<GeneralizedInventoryCampaign> source = agencyId == null
                ? generalizedInventoryCampaignRepository.findByOrganizationId(tenantId, organizationId)
                : generalizedInventoryCampaignRepository.findByAgencyId(tenantId, organizationId, agencyId);
        return source.sort(Comparator.comparing(GeneralizedInventoryCampaign::scheduledAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
    }

    public Mono<GeneralizedInventoryCampaign> start(UUID tenantId, UUID campaignId) {
        return generalizedInventoryCampaignRepository.findById(tenantId, campaignId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("campaign not found")))
                .map(GeneralizedInventoryCampaign::start)
                .flatMap(generalizedInventoryCampaignRepository::save);
    }

    public Mono<GeneralizedInventoryCampaign> submit(UUID tenantId, UUID campaignId, BigDecimal variancePercent) {
        return generalizedInventoryCampaignRepository.findById(tenantId, campaignId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("campaign not found")))
                .map(campaign -> campaign.submit(variancePercent))
                .flatMap(generalizedInventoryCampaignRepository::save);
    }

    public Mono<GeneralizedInventoryCampaign> approve(UUID tenantId, UUID campaignId) {
        return generalizedInventoryCampaignRepository.findById(tenantId, campaignId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("campaign not found")))
                .map(GeneralizedInventoryCampaign::approve)
                .flatMap(generalizedInventoryCampaignRepository::save);
    }

    private Mono<Void> validateScope(UUID tenantId, UUID organizationId, UUID agencyId, UUID physicalSpaceId) {
        Mono<Void> organization = organizationRepository.findById(tenantId, organizationId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("organization not found")))
                .then();
        Mono<Void> agency = agencyId == null ? Mono.empty()
                : agencyRepository.findById(tenantId, agencyId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("agency not found")))
                        .flatMap(value -> value.organizationId().equals(organizationId)
                                ? Mono.<Void>empty()
                                : Mono.error(new IllegalArgumentException(
                                        "agency does not belong to organization")));
        Mono<Void> space = physicalSpaceId == null ? Mono.empty()
                : physicalSpaceRepository.findById(tenantId, physicalSpaceId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("physical space not found")))
                        .flatMap(value -> value.organizationId().equals(organizationId)
                                && (agencyId == null || agencyId.equals(value.agencyId()))
                                ? Mono.<Void>empty()
                                : Mono.error(new IllegalArgumentException(
                                        "physical space does not belong to scope")));
        return Mono.when(organization, agency, space);
    }

    public record PlanGeneralizedInventoryCampaignCommand(UUID agencyId, UUID warehouseId, UUID physicalSpaceId,
            UUID supervisorActorId, String campaignCode, String campaignType, String scopeType,
            java.time.Instant scheduledAt, String notes) {
    }
}
