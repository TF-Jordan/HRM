package yowyob.comops.api.organization.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import yowyob.comops.api.organization.adapter.out.persistence.InMemoryAgencyRepository;
import yowyob.comops.api.organization.adapter.out.persistence.InMemoryOrganizationRepository;
import yowyob.comops.api.organization.adapter.out.persistence.InMemoryOrganizationSecondaryStructureRepository;
import yowyob.comops.api.organization.domain.model.Agency;
import yowyob.comops.api.organization.domain.model.Organization;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class OrganizationSecondaryStructureApplicationServiceTest {

    @Test
    void createsBusinessDomainAndOrganizationLinks() {
        InMemoryOrganizationSecondaryStructureRepository secondaryRepository = new InMemoryOrganizationSecondaryStructureRepository();
        InMemoryOrganizationRepository organizationRepository = new InMemoryOrganizationRepository();
        InMemoryAgencyRepository agencyRepository = new InMemoryAgencyRepository();
        OrganizationSecondaryStructureApplicationService service = new OrganizationSecondaryStructureApplicationService(
                secondaryRepository, organizationRepository, agencyRepository);

        UUID tenantId = UUID.randomUUID();
        UUID businessActorId = UUID.randomUUID();
        Organization organization = Organization.create(tenantId, businessActorId, "ORG-01", "SALES", false,
                "org@example.com", "Org", "Organization", null, null, null, null, null, null, null, null, null,
                null, Set.of("sales"), 12, "SARL", true, "ACTIVE");
        Agency agency = Agency.create(tenantId, organization.id(), "AG-01", "Agency 1", "BRANCH");

        organizationRepository.save(organization).block();
        agencyRepository.save(agency).block();

        var domain = service.createBusinessDomain(tenantId, "FOOD", "SALES", null, "Food", null, null, "CATALOG",
                "Catalog", "Food domain").block();
        var organizationDomain = service.linkOrganizationDomain(tenantId, organization.id(), domain.id()).block();
        var agencyDomain = service.linkAgencyDomain(tenantId, organization.id(), agency.id(), domain.id()).block();

        assertThat(domain).isNotNull();
        assertThat(organizationDomain.organizationId()).isEqualTo(organization.id());
        assertThat(agencyDomain.agencyId()).isEqualTo(agency.id());
        assertThat(service.listBusinessDomains(tenantId).collectList().block()).hasSize(1);
    }
}
