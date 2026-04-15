package yowyob.comops.api.organization.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "organization", name = "agency")
public record AgencyEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID organizationId,
        String governanceStatus,
        UUID governedByUserId,
        Instant governedAt,
        String governanceReason,
        String code,
        UUID ownerId,
        UUID managerId,
        String name,
        String location,
        String description,
        boolean transferable,
        boolean active,
        String logoUri,
        UUID logoId,
        String shortName,
        String longName,
        boolean isIndividualBusiness,
        boolean isHeadquarter,
        String country,
        String city,
        Double latitude,
        Double longitude,
        String openTime,
        String closeTime,
        String phone,
        String email,
        String whatsapp,
        String greetingMessage,
        BigDecimal averageRevenue,
        BigDecimal capitalShare,
        String registrationNumber,
        String socialNetwork,
        String taxNumber,
        Set<String> keywords,
        boolean isPublic,
        boolean isBusiness,
        Integer totalAffiliatedCustomers,
        Instant deletedAt,
        String agencyType) implements PersistableEntity {
}
