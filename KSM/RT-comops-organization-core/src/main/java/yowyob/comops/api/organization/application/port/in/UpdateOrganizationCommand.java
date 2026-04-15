package yowyob.comops.api.organization.application.port.in;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record UpdateOrganizationCommand(
        UUID tenantId,
        UUID organizationId,
        UUID currentBusinessActorId,
        String code,
        String service,
        boolean isIndividualBusiness,
        String email,
        String shortName,
        String longName,
        String description,
        String logoUri,
        UUID logoId,
        String websiteUrl,
        String socialNetwork,
        String businessRegistrationNumber,
        String taxNumber,
        BigDecimal capitalShare,
        String ceoName,
        Integer yearFounded,
        Set<String> keywords,
        Integer numberOfEmployees,
        String legalForm,
        boolean isActive,
        String status) {
}
