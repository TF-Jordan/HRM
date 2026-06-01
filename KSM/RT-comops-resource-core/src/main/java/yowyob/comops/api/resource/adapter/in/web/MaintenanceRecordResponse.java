package yowyob.comops.api.resource.adapter.in.web;

import yowyob.comops.api.resource.domain.model.MaintenanceRecord;
import java.time.Instant;
import java.util.UUID;

public record MaintenanceRecordResponse(UUID id, UUID resourceId, String maintenanceType, String description,
        String status, Instant completedAt) {
    public static MaintenanceRecordResponse from(MaintenanceRecord record) {
        return new MaintenanceRecordResponse(record.id(), record.resourceId(), record.maintenanceType(),
                record.description(), record.status(), record.completedAt());
    }
}
