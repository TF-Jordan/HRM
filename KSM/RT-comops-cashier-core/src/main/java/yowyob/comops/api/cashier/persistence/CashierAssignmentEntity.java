package yowyob.comops.api.cashier.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("cashier.cashier_assignment")
public record CashierAssignmentEntity(
        @Id UUID id,
        @Column("organization_id") UUID organizationId,
        @Column("agency_id") UUID agencyId,
        @Column("cashier_id") UUID cashierId,
        @Column("assigned_at") Instant assignedAt) {
}
