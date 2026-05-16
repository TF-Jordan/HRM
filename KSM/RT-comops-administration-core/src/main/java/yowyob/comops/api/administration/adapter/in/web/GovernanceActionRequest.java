package yowyob.comops.api.administration.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record GovernanceActionRequest(@NotBlank String action, String reason) {
}
