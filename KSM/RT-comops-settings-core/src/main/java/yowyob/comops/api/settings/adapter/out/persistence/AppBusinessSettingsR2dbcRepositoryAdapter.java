package yowyob.comops.api.settings.adapter.out.persistence;

import yowyob.comops.api.settings.application.port.out.AppBusinessSettingsRepository;
import yowyob.comops.api.settings.domain.model.AppBusinessSettings;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class AppBusinessSettingsR2dbcRepositoryAdapter implements AppBusinessSettingsRepository {

    private final AppBusinessSettingsSpringDataRepository repository;

    public AppBusinessSettingsR2dbcRepositoryAdapter(AppBusinessSettingsSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<AppBusinessSettings> findByScope(java.util.UUID tenantId, java.util.UUID organizationId,
            java.util.UUID agencyId) {
        if (agencyId == null) {
            return findOrganizationDefault(tenantId, organizationId);
        }
        return repository.findByTenantIdAndOrganizationIdAndAgencyId(tenantId, organizationId, agencyId).map(this::toDomain);
    }

    @Override
    public Mono<AppBusinessSettings> findOrganizationDefault(java.util.UUID tenantId, java.util.UUID organizationId) {
        return repository.findByTenantIdAndOrganizationIdAndAgencyIdIsNull(tenantId, organizationId).map(this::toDomain);
    }

    @Override
    public Mono<AppBusinessSettings> save(AppBusinessSettings settings) {
        return repository.save(toEntity(settings)).map(this::toDomain);
    }

    private AppBusinessSettingsEntity toEntity(AppBusinessSettings settings) {
        return new AppBusinessSettingsEntity(settings.id(), settings.tenantId(), settings.createdAt(), settings.updatedAt(),
                settings.organizationId(), settings.agencyId(), settings.negotiateSellingPrice(),
                settings.sellingPriceIncludeVat(), settings.authorizeExceptionalDiscount(),
                settings.grantableDiscountRate(), settings.printLogo(), settings.paperFormat(),
                settings.lengthOfVatInvoiceNumber(), settings.prefixOfVatInvoiceNumber(), settings.lowStockAlert(),
                settings.preventiveMaintenanceAlert(), settings.defaultCurrency(), settings.legalIdentity(),
                settings.taxIdentifier(), settings.requireSalesOrderApproval(), settings.requireReturnApproval());
    }

    private AppBusinessSettings toDomain(AppBusinessSettingsEntity entity) {
        return AppBusinessSettings.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(), entity.updatedAt(),
                entity.organizationId(), entity.agencyId(), entity.negotiateSellingPrice(),
                entity.sellingPriceIncludeVat(), entity.authorizeExceptionalDiscount(), entity.grantableDiscountRate(),
                entity.printLogo(), entity.paperFormat(), entity.lengthOfVatInvoiceNumber(),
                entity.prefixOfVatInvoiceNumber(), entity.lowStockAlert(), entity.preventiveMaintenanceAlert(),
                entity.defaultCurrency(), entity.legalIdentity(), entity.taxIdentifier(),
                entity.requireSalesOrderApproval(), entity.requireReturnApproval());
    }
}
