package yowyob.comops.api.resource.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignMaterialResourceRequest(@NotBlank String assigneeType, @NotNull UUID assigneeId) {
}
