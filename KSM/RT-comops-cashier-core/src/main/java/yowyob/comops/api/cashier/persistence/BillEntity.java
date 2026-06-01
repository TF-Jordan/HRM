package yowyob.comops.api.cashier.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("cashier.bill")
public record BillEntity(
        @Id UUID id,
        @Column("organization_id") UUID organizationId,
        @Column("customer_id") UUID customerId,
        String reference,
        @Column("total_amount") BigDecimal totalAmount,
        @Column("paid_amount") BigDecimal paidAmount,
        @Column("linked_service_code") String linkedServiceCode,
        @Column("linked_document_type") String linkedDocumentType,
        @Column("linked_document_id") UUID linkedDocumentId,
        @Column("linked_synced_amount") BigDecimal linkedSyncedAmount,
        String currency,
        String status,
        @Column("created_at") Instant createdAt) {
}
