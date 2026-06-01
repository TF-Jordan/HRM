package yowyob.comops.api.bootstrap.integration.auth;

import yowyob.comops.api.auth.application.port.out.UserOrganizationAccess;
import yowyob.comops.api.auth.application.port.out.UserOrganizationAccessDirectory;
import yowyob.comops.api.organization.application.port.in.ListUserOrganizationAccessUseCase;
import org.springframework.stereotype.Component;
import java.util.UUID;
import reactor.core.publisher.Flux;

@Component
public class OrganizationCoreUserOrganizationAccessDirectory implements UserOrganizationAccessDirectory {

    private final ListUserOrganizationAccessUseCase listUserOrganizationAccessUseCase;

    public OrganizationCoreUserOrganizationAccessDirectory(
            ListUserOrganizationAccessUseCase listUserOrganizationAccessUseCase) {
        this.listUserOrganizationAccessUseCase = listUserOrganizationAccessUseCase;
    }

    @Override
    public Flux<UserOrganizationAccess> listUserOrganizations(UUID tenantId, UUID userId) {
        return listUserOrganizationAccessUseCase.listUserOrganizationAccess(tenantId, userId)
                .map(view -> new UserOrganizationAccess(view.organizationId(), view.organizationCode(),
                        view.shortName(), view.longName(), view.services()));
    }
}
