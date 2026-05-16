package yowyob.comops.api.tp.adapter.in.web;

import yowyob.comops.api.tp.domain.model.ThirdPartyBankAccount;
import java.util.UUID;

public record ThirdPartyBankAccountResponse(
        UUID id,
        UUID thirdPartyId,
        String label,
        String bankName,
        String iban,
        String swiftBic,
        String currency,
        boolean primary) {

    public static ThirdPartyBankAccountResponse from(ThirdPartyBankAccount bankAccount) {
        return new ThirdPartyBankAccountResponse(bankAccount.id(), bankAccount.thirdPartyId(), bankAccount.label(),
                bankAccount.bankName(), bankAccount.iban(), bankAccount.swiftBic(), bankAccount.currency(),
                bankAccount.primary());
    }
}
