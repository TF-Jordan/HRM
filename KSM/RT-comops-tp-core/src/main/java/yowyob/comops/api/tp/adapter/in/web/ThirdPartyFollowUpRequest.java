package yowyob.comops.api.tp.adapter.in.web;

import java.time.Instant;

public record ThirdPartyFollowUpRequest(
        Instant contactedAt,
        Instant nextFollowUpAt) {
}
