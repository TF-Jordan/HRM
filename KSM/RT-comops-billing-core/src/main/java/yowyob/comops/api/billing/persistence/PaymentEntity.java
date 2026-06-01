package yowyob.comops.api.billing.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("billing.payment")
public record PaymentEntity(
        @Id
        UUID id,
        @Column("organization_id")
        UUID organizationId,
        @Column("billing_document_id")
        UUID billingDocumentId,
        @Column("invoice_id")
        UUID invoiceId,
        @Column("supplier_invoice_id")
        UUID supplierInvoiceId,
        @Column("counterparty_third_party_id")
        UUID counterpartyThirdPartyId,
        @Column("reference")
        String reference,
        @Column("amount")
        BigDecimal amount,
        @Column("currency")
        String currency,
        @Column("status")
        String status,
        @Column("linked_service_code")
        String linkedServiceCode,
        @Column("linked_document_type")
        String linkedDocumentType,
        @Column("linked_document_id")
        UUID linkedDocumentId,
        @Column("paid_at")
        Instant paidAt) {
}
