package yowyob.comops.api.hrm.adapter.out.persistence;

import yowyob.comops.api.common.adapter.out.persistence.PersistableEntity;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "hrm_application")
public record ApplicationEntity(
        @Id UUID id,
        UUID tenantId,
        Instant createdAt,
        Instant updatedAt,
        UUID jobOfferId,
        String candidatNom,
        String candidatPrenom,
        String candidatEmail,
        String candidatTelephone,
        UUID cvFileId,
        UUID lettreMotivationFileId,
        String status) implements PersistableEntity {
}
