package yowyob.comops.api.accounting.extension.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("accounting.accounting_extension_exchange_rate")
public record AccountingExtensionExchangeRateEntity(
        @Id
        @Column("id")
        UUID id,
        @Column("organization_id")
        UUID organizationId,
        @Column("source_currency")
        String sourceCurrency,
        @Column("target_currency")
        String targetCurrency,
        @Column("rate")
        BigDecimal rate,
        @Column("rate_date")
        LocalDate rateDate,
        @Column("created_at")
        Instant createdAt) {
}
