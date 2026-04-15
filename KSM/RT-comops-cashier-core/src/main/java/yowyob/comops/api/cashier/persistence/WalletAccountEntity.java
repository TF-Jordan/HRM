package yowyob.comops.api.cashier.persistence;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("cashier.wallet_account")
public record WalletAccountEntity(
        @Id UUID id,
        @Column("organization_id") UUID organizationId,
        @Column("owner_id") UUID ownerId,
        @Column("owner_name") String ownerName,
        String number,
        BigDecimal balance,
        String currency,
        String type,
        @Column("linked_third_party_id") UUID linkedThirdPartyId) {
}
