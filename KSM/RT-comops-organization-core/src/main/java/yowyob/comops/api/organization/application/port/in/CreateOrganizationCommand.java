package yowyob.comops.api.organization.application.port.in;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record CreateOrganizationCommand(
        UUID tenantId,
        UUID businessActorId,
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

    public CreateOrganizationCommand(
            UUID tenantId,
            UUID businessActorId,
            String code,
            String legalName,
            String displayName,
            String organizationType) {
        this(
                tenantId,
                businessActorId,
                code,
                organizationType,
                false,
                null,
                displayName,
                legalName,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Set.of(),
                null,
                null,
                true,
                null);
    }
}
