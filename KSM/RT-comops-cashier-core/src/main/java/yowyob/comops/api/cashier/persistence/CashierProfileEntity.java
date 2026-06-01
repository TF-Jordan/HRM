package yowyob.comops.api.cashier.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("cashier.cashier_profile")
public record CashierProfileEntity(
        @Id UUID id,
        @Column("organization_id") UUID organizationId,
        @Column("agency_id") UUID agencyId,
        @Column("kernel_user_id") UUID kernelUserId,
        String email,
        @Column("full_name") String fullName,
        String kind,
        boolean active,
        @Column("created_at") Instant createdAt) {
}
