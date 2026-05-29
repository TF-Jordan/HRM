package yowyob.comops.api.hrm.application.port.out;

import java.time.LocalDate;
import java.util.UUID;

import reactor.core.publisher.Mono;

public interface ActorPort {

    Mono<ActorInfo> resolveActor(UUID tenantId, UUID actorId);

    Mono<ActorInfo> resolveManager(UUID tenantId, UUID employeeActorId);

    /**
     * Personal identity information about an actor, sourced from actor-core.
     * Enriches the HRM employee 360° profile without duplicating data on the
     * employee row.
     */
    record ActorInfo(
            UUID actorId,
            String displayName,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            String gender,
            String nationality,
            LocalDate birthDate,
            String photoUri,
            UUID photoId) {

        /** Backward-compatible minimal factory (display name only). */
        public static ActorInfo of(UUID actorId, String displayName) {
            return new ActorInfo(actorId, displayName, null, null, null, null, null, null, null, null, null);
        }
    }
}
