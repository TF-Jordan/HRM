package yowyob.comops.api.billing.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("billing.commercial_document")
public record CommercialDocumentEntity(
        @Id
        UUID id,
        @Column("organization_id")
        UUID organizationId,
        @Column("type")
        String type,
        @Column("document_number")
        String documentNumber,
        @Column("counterparty_third_party_id")
        UUID counterpartyThirdPartyId,
        @Column("currency")
        String currency,
        @Column("status")
        String status,
        @Column("linked_accounting_invoice_id")
        UUID linkedAccountingInvoiceId,
        @Column("linked_cashier_bill_id")
        UUID linkedCashierBillId,
        @Column("created_at")
        Instant createdAt) {
}
