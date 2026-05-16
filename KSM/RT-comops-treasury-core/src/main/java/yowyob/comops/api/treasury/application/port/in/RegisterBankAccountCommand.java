package yowyob.comops.api.treasury.application.port.in;

import java.util.UUID;

public record RegisterBankAccountCommand(
        UUID tenantId,
        UUID organizationId,
        UUID bankThirdPartyId,
        UUID ownerThirdPartyId,
        String bankName,
        String accountNumber,
        String iban,
        String currency) {
}
