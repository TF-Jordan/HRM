package yowyob.comops.api.organization.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CreatePhysicalSpaceRequest(
        UUID parentSpaceId,
        @NotBlank String code,
        @NotBlank String name,
        @NotBlank String spaceType,
        String description,
        Integer levelNumber,
        Integer capacity,
        Boolean active) {
}
