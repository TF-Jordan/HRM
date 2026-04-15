package yowyob.comops.api.accounting.extension.persistence;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("accounting.accounting_extension_invoice_accounting")
public record AccountingExtensionInvoiceAccountingEntity(
        @Id
        @Column("id")
        UUID id,
        @Column("organization_id")
        UUID organizationId,
        @Column("invoice_id")
        UUID invoiceId,
        @Column("customer_third_party_id")
        UUID customerThirdPartyId,
        @Column("customer_accounting_account")
        String customerAccountingAccount,
        @Column("accounting_status")
        String accountingStatus,
        @Column("created_at")
        Instant createdAt) {
}
