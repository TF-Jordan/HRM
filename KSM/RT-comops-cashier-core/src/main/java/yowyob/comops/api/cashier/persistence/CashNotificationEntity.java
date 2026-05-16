package yowyob.comops.api.cashier.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("cashier.cash_notification")
public record CashNotificationEntity(
        @Id UUID id,
        @Column("organization_id") UUID organizationId,
        String channel,
        String subject,
        String recipient,
        String status,
        @Column("created_at") Instant createdAt) {
}
