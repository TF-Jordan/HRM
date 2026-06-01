package yowyob.comops.api.organization.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record UpdateWarehouseRequest(@NotBlank String code, @NotBlank String name) {
}
