package yowyob.comops.api.tp.adapter.in.web;

import yowyob.comops.api.common.domain.model.PartyType;
import yowyob.comops.api.tp.domain.model.ThirdPartySearchResult;
import java.util.Set;
import java.util.UUID;

public record ThirdPartySearchResponse(
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
        String segment,
        Integer qualificationScore,
        boolean enabled,
        String followUpStatus,
        String referenceCode,
        String displayName,
        boolean active) {

    public static ThirdPartySearchResponse from(ThirdPartySearchResult result) {
        return new ThirdPartySearchResponse(result.id(), result.tenantId(), result.organizationId(),
                result.partyType(), result.partyId(), result.code(), result.name(), result.type(),
                result.longName(), result.roles(), result.prospect(), result.segment(),
                result.qualificationScore(), result.enabled(), result.followUpStatus(),
                result.referenceCode(), result.displayName(), result.active());
    }
}
