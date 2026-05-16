package yowyob.comops.api.organization.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import yowyob.comops.api.organization.adapter.out.persistence.InMemoryEmployeeMembershipRepository;
import yowyob.comops.api.organization.adapter.out.persistence.InMemoryOrganizationRepository;
import yowyob.comops.api.organization.adapter.out.persistence.InMemoryOrganizationServiceSubscriptionRepository;
import yowyob.comops.api.organization.config.OrganizationServiceSubscriptionQuotaProperties;
import yowyob.comops.api.organization.domain.model.Organization;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrganizationServiceSubscriptionApplicationServiceTest {

    private InMemoryOrganizationRepository organizationRepository;
    private InMemoryOrganizationServiceSubscriptionRepository subscriptionRepository;
    private OrganizationServiceSubscriptionApplicationService service;
    private UUID tenantId;
    private UUID organizationId;

    @BeforeEach
    void setUp() {
        organizationRepository = new InMemoryOrganizationRepository();
        subscriptionRepository = new InMemoryOrganizationServiceSubscriptionRepository();
        OrganizationServiceSubscriptionQuotaProperties properties = new OrganizationServiceSubscriptionQuotaProperties();
        properties.setDefaultRequestQuotaLimit(100);
        properties.setDefaultRequestQuotaWindow(Duration.ofMinutes(2));
        properties.setProvisionSubscribableServicesOnCreate(false);
        service = new OrganizationServiceSubscriptionApplicationService(
                organizationRepository,
                subscriptionRepository,
                new InMemoryEmployeeMembershipRepository(),
                (ignoredTenantId, ignoredUserId) -> reactor.core.publisher.Mono.just(UUID.randomUUID()),
                properties);

        tenantId = UUID.randomUUID();
        Organization organization = Organization.create(tenantId, UUID.randomUUID(), "ORG-MOD", "PRIVATE_COMPANY", false,
                "org@example.com", "Org Mod", "Organization Modulaire", null, null, null, null, null, null, null, null,
                null, null, Set.of(), 12, "SARL", true, "ACTIVE");
        organizationRepository.save(organization).block();
        organizationId = organization.id();
    }

    @Test
    void newOrganizationExposesMandatoryServicesOnlyWhenAutoProvisionIsDisabled() {
        var entitlements = service.getOrganizationServiceEntitlements(tenantId, organizationId).block();

        assertThat(entitlements).isNotNull();
        assertThat(entitlements.subscribedServices()).isEmpty();
        assertThat(entitlements.effectiveServices()).containsExactly("ORGANIZATION", "SETTINGS");
        assertThat(entitlements.dependencyIssues()).isEmpty();
    }

    @Test
    void rejectsIncoherentSubscriptionsAndUnsubscriptions() {
        assertThatThrownBy(() -> service.subscribeOrganizationService(tenantId, organizationId, "SALES", null, null).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("COMMERCIAL")
                .hasMessageContaining("PRODUCT");

        service.subscribeOrganizationService(tenantId, organizationId, "COMMERCIAL", null, null).block();
        service.subscribeOrganizationService(tenantId, organizationId, "PRODUCT", null, null).block();
        var entitlements = service.subscribeOrganizationService(tenantId, organizationId, "SALES", null, null).block();

        assertThat(entitlements.subscribedServices()).containsExactly("COMMERCIAL", "PRODUCT", "SALES");
        assertThatThrownBy(() -> service.unsubscribeOrganizationService(tenantId, organizationId, "PRODUCT").block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SALES");
    }

    @Test
    void catalogExposesDependenciesAndExplicitPacks() {
        var catalog = service.listPlatformServices().collectList().block();
        var packs = service.listPlatformServicePacks().collectList().block();

        assertThat(catalog).isNotNull();
        assertThat(catalog).anySatisfy(entry -> {
            if ("INVENTORY".equals(entry.code())) {
                assertThat(entry.requiredDependencies()).containsExactly("PRODUCT");
                assertThat(entry.packs()).contains("OPERATIONS_PACK");
            }
        });
        assertThat(catalog).anySatisfy(entry -> {
            if ("TREASURY".equals(entry.code())) {
                assertThat(entry.requiredDependencies()).containsExactly("ACCOUNTING", "BANKING");
            }
        });
        assertThat(packs).isNotNull();
        assertThat(packs).extracting(pack -> pack.code()).containsExactly("COMMERCIAL_PACK", "FINANCE_PACK", "OPERATIONS_PACK");
        assertThat(packs).anySatisfy(pack -> {
            if ("OPERATIONS_PACK".equals(pack.code())) {
                assertThat(pack.serviceCodes()).contains("PRODUCT", "INVENTORY", "RESOURCE", "HRM", "BLOCKCHAIN");
            }
        });
    }

    @Test
    void commercialCatalogExposesPlansAddOnsAndQuotas() {
        var catalog = service.listCommercialSubscriptionCatalog().block();

        assertThat(catalog).isNotNull();
        assertThat(catalog.plans()).extracting(plan -> plan.code())
                .containsExactly("STARTER", "COMMERCE", "FINANCE", "OPERATIONS", "ENTERPRISE");
        assertThat(catalog.addOns()).extracting(addOn -> addOn.code())
                .contains("POINT_OF_SALE_ADDON", "TREASURY_SETTLEMENTS_ADDON", "BLOCKCHAIN_AUDIT_ADDON");
        assertThat(catalog.plans()).anySatisfy(plan -> {
            if ("COMMERCE".equals(plan.code())) {
                assertThat(plan.packCodes()).containsExactly("COMMERCIAL_PACK");
                assertThat(plan.serviceCodes()).containsExactly("COMMERCIAL", "PRODUCT", "SALES", "BILLING");
                assertThat(plan.compatibleAddOnCodes()).contains("POINT_OF_SALE_ADDON");
                assertThat(plan.serviceQuotas()).anySatisfy(quota -> {
                    if ("BILLING".equals(quota.serviceCode())) {
                        assertThat(quota.requestQuotaLimit()).isEqualTo(50_000L);
                    }
                });
            }
        });
    }

    @Test
    void appliesCommercialPlanWithCompatibleAddOnsAndAttachedQuotas() {
        var subscription = service.applyOrganizationCommercialSubscription(tenantId, organizationId,
                "COMMERCE", java.util.List.of("POINT_OF_SALE_ADDON", "BLOCKCHAIN_AUDIT_ADDON")).block();

        assertThat(subscription).isNotNull();
        assertThat(subscription.planCode()).isEqualTo("COMMERCE");
        assertThat(subscription.addOnCodes()).containsExactlyInAnyOrder("POINT_OF_SALE_ADDON", "BLOCKCHAIN_AUDIT_ADDON");
        assertThat(subscription.serviceCodes()).containsExactly("COMMERCIAL", "PRODUCT", "SALES", "BILLING",
                "ACCOUNTING", "CASHIER", "BLOCKCHAIN");
        assertThat(subscription.entitlements().subscribedServices()).containsExactlyElementsOf(subscription.serviceCodes());
        assertThat(subscription.entitlements().effectiveServices()).contains("ORGANIZATION", "SETTINGS", "CASHIER");
        assertThat(subscription.entitlements().serviceQuotas()).anySatisfy(quota -> {
            if ("CASHIER".equals(quota.serviceCode())) {
                assertThat(quota.requestQuotaLimit()).isEqualTo(60_000L);
            }
        });
    }

    @Test
    void rejectsCommercialAddOnThatIsNotCompatibleWithPlan() {
        assertThatThrownBy(() -> service.applyOrganizationCommercialSubscription(tenantId, organizationId,
                "STARTER", java.util.List.of("POINT_OF_SALE_ADDON")).block())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("POINT_OF_SALE_ADDON")
                .hasMessageContaining("STARTER");
    }

    @Test
    void applyingPlanReplacesPreviousCommercialServiceSet() {
        service.applyOrganizationCommercialSubscription(tenantId, organizationId,
                "ENTERPRISE", java.util.List.of()).block();

        var downgraded = service.applyOrganizationCommercialSubscription(tenantId, organizationId,
                "STARTER", java.util.List.of()).block();

        assertThat(downgraded.serviceCodes()).containsExactly("COMMERCIAL");
        assertThat(downgraded.entitlements().subscribedServices()).containsExactly("COMMERCIAL");
        assertThat(downgraded.entitlements().effectiveServices()).containsExactly("ORGANIZATION", "SETTINGS",
                "COMMERCIAL");
    }
}
