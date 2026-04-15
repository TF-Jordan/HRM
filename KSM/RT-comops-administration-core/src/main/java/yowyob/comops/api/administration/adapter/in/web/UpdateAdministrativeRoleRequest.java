package yowyob.comops.api.administration.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record UpdateAdministrativeRoleRequest(@NotBlank String name) {
}
