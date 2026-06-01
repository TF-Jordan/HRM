package yowyob.comops.api.resource.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record RecordMaintenanceRequest(@NotBlank String maintenanceType, @NotBlank String description, String status) {
}
