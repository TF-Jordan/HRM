package yowyob.comops.api.tp.adapter.in.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ThirdPartyQualificationRequest(
        String segment,
        @Min(0) @Max(100) Integer qualificationScore) {
}
