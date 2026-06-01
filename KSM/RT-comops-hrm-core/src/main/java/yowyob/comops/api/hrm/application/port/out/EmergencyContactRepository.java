package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.EmergencyContact;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmergencyContactRepository {

    Mono<EmergencyContact> save(EmergencyContact contact);

    Flux<EmergencyContact> findByEmployeeId(UUID tenantId, UUID employeeId);

    Mono<EmergencyContact> findById(UUID tenantId, UUID contactId);

    Mono<Void> deleteById(UUID tenantId, UUID contactId);
}
