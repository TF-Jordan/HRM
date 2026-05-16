package yowyob.comops.api.organization.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ApplyCommercialSubscriptionRequest(
        @NotBlank String planCode,
        List<String> addOnCodes) {
}
