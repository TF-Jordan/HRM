package yowyob.comops.api.tp.application.port.in;

import java.util.UUID;

public record AddThirdPartyBankAccountCommand(
        UUID tenantId,
        UUID thirdPartyId,
        String label,
        String bankName,
        String iban,
        String swiftBic,
        String currency,
        boolean primary) {
}
