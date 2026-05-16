package yowyob.comops.api.administration.adapter.in.web;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record ReplaceRolePermissionsRequest(@NotEmpty Set<String> permissions) {
}
