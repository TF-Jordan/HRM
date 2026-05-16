package yowyob.comops.api.tp.domain.model;

import yowyob.comops.api.common.domain.model.PartyType;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record ThirdPartySearchResult(
                UUID id,
                UUID tenantId,
                UUID organizationId,
                PartyType partyType,
                UUID partyId,
                String code,
                String name,
                String type,
                String longName,
                Set<String> roles,
                boolean prospect,
                String accountingAccount,
                String segment,
                Integer qualificationScore,
                boolean enabled,
                Instant lastContactedAt,
                Instant nextFollowUpAt,
                String followUpStatus,
                Instant convertedAt) {

        public String referenceCode() {
                return code;
        }

        public String displayName() {
                return name;
        }

        public boolean active() {
                return enabled;
        }
}
