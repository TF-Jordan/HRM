package yowyob.comops.api.tp.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import yowyob.comops.api.common.domain.model.PartyRef;
import yowyob.comops.api.common.domain.model.PartyType;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ThirdPartyCanonicalAliasTest {

    @Test
    void derivesLegacyAliasesFromCanonicalState() {
        ThirdParty thirdParty = ThirdParty.create(UUID.randomUUID(), UUID.randomUUID(),
                new PartyRef(PartyType.ORGANIZATION, UUID.randomUUID()), "cli-01", "Legacy Label", Set.of("CUSTOMER"),
                false, "411100", "A", 90, true, "CUSTOMER", "SARL", "UIN-1", "TRN-1", "Canonical Client", "CC",
                "Canonical Client Long", null, null, List.of("411100", "411101"), List.of("CASH"), null, null, false,
                null, null, null, null, "CUSTOMER", "{}", "M021");

        assertThat(thirdParty.code()).isEqualTo("CLI-01");
        assertThat(thirdParty.referenceCode()).isEqualTo("CLI-01");
        assertThat(thirdParty.name()).isEqualTo("Canonical Client");
        assertThat(thirdParty.displayName()).isEqualTo("Canonical Client");
        assertThat(thirdParty.accountingAccount()).isEqualTo("411100");
        assertThat(thirdParty.accountingAccountNumbers()).containsExactly("411100", "411101");
    }

    @Test
    void assignsPrimaryAccountingAccountFromGeneratedAccounts() {
        ThirdParty thirdParty = ThirdParty.create(UUID.randomUUID(), UUID.randomUUID(),
                new PartyRef(PartyType.ORGANIZATION, UUID.randomUUID()), "cli-02", "Legacy Label", Set.of("CUSTOMER"),
                false, null, "A", 90, true, "CUSTOMER", "SARL", "UIN-2", "TRN-2", "Canonical Client", "CC",
                "Canonical Client Long", null, null, null, List.of("CASH"), null, null, false,
                null, null, null, null, "CUSTOMER", "{}", "M022");

        ThirdParty assigned = thirdParty.assignAccountingAccounts(List.of("411102", "411103"));

        assertThat(assigned.accountingAccount()).isEqualTo("411102");
        assertThat(assigned.accountingAccountNumbers()).containsExactly("411102", "411103");
    }
}
