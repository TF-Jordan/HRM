package yowyob.comops.api.administration.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignAdministrativeRoleRequest(@NotNull UUID roleId, String scopeType, UUID scopeId,
        @NotBlank String scope) {
}
