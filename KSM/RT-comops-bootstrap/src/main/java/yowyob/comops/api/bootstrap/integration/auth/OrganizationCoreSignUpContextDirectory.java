package yowyob.comops.api.bootstrap.integration.auth;

import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import yowyob.comops.api.auth.application.port.out.SignUpContextDirectory;
import yowyob.comops.api.organization.application.port.out.OrganizationRepository;

@Component
public class OrganizationCoreSignUpContextDirectory implements SignUpContextDirectory {

    private final OrganizationRepository organizationRepository;

    public OrganizationCoreSignUpContextDirectory(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Override
    public Flux<SignUpContext> findByOrganizationCode(String organizationCode) {
        return organizationRepository.findByCode(organizationCode)
                .filter(organization -> organization.isActive())
                .map(organization -> new SignUpContext(
                        organization.id().toString(),
                        organization.tenantId(),
                        organization.id(),
                        organization.code(),
                        organization.displayName(),
                        organization.organizationType()));
    }
}
