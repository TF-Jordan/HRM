package yowyob.comops.api.administration.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record CloneAdministrativeRoleRequest(
        @NotBlank String code,
        @NotBlank String name,
        String scopeType) {
}
