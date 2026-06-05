package yowyob.comops.api.bootstrap.integration.payroll;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import yowyob.comops.api.organization.application.port.in.GetOrganizationUseCase;
import yowyob.comops.api.payroll.application.port.out.EmployerInfo;
import yowyob.comops.api.payroll.application.port.out.EmployerInfoPort;

import java.util.UUID;

/**
 * Bridges payroll-core's {@link EmployerInfoPort} onto organization-core, projecting the
 * employer header (legal name, RCCM, taxpayer number, CEO) used on legal payroll documents.
 * The employer CNPS number is not held by organization-core, so it is left empty.
 */
@Component
public class OrganizationPayrollEmployerInfoPort implements EmployerInfoPort {

    private final GetOrganizationUseCase getOrganizationUseCase;

    public OrganizationPayrollEmployerInfoPort(GetOrganizationUseCase getOrganizationUseCase) {
        this.getOrganizationUseCase = getOrganizationUseCase;
    }

    @Override
    public Mono<EmployerInfo> find(UUID tenantId, UUID organizationId) {
        return getOrganizationUseCase.getOrganization(organizationId)
                .map(o -> new EmployerInfo(o.longName(), o.shortName(), o.businessRegistrationNumber(),
                        o.taxNumber(), o.cnpsEmployerNumber(), o.ceoName(), o.email()))
                .defaultIfEmpty(new EmployerInfo("", "", null, null, null, null, null));
    }
}
