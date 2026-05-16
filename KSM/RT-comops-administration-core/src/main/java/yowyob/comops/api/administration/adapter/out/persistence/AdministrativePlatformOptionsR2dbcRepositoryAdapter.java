package yowyob.comops.api.administration.adapter.out.persistence;

import yowyob.comops.api.administration.application.port.out.AdministrativePlatformOptionsRepository;
import yowyob.comops.api.administration.domain.model.AdministrativePlatformOptions;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Profile("r2dbc")
public class AdministrativePlatformOptionsR2dbcRepositoryAdapter implements AdministrativePlatformOptionsRepository {

    private final AdministrativePlatformOptionsSpringDataRepository repository;

    public AdministrativePlatformOptionsR2dbcRepositoryAdapter(AdministrativePlatformOptionsSpringDataRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<AdministrativePlatformOptions> findByTenantId(UUID tenantId) {
        return repository.findByTenantId(tenantId).map(this::toDomain);
    }

    @Override
    public Mono<AdministrativePlatformOptions> save(AdministrativePlatformOptions options) {
        return repository.save(toEntity(options)).map(this::toDomain);
    }

    private AdministrativePlatformOptionsEntity toEntity(AdministrativePlatformOptions options) {
        return new AdministrativePlatformOptionsEntity(options.id(), options.tenantId(), options.createdAt(),
                options.updatedAt(), options.requireBusinessActorApproval(), options.requireOrganizationApproval(),
                options.allowOrganizationSelfServiceCreation(), options.allowAgencySelfServiceCreation(),
                options.allowRoleCloning(), options.allowAgencyScopedCustomRoles(),
                options.allowOrganizationAdminsToGovernAgencies(), options.allowBusinessActorSelfReactivation());
    }

    private AdministrativePlatformOptions toDomain(AdministrativePlatformOptionsEntity entity) {
        return AdministrativePlatformOptions.rehydrate(entity.id(), entity.tenantId(), entity.createdAt(),
                entity.updatedAt(), entity.requireBusinessActorApproval(), entity.requireOrganizationApproval(),
                entity.allowOrganizationSelfServiceCreation(), entity.allowAgencySelfServiceCreation(),
                entity.allowRoleCloning(), entity.allowAgencyScopedCustomRoles(),
                entity.allowOrganizationAdminsToGovernAgencies(), entity.allowBusinessActorSelfReactivation());
    }
}
