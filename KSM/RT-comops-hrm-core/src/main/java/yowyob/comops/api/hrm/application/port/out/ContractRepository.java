package yowyob.comops.api.hrm.application.port.out;

import yowyob.comops.api.hrm.domain.model.Contract;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ContractRepository {

    Mono<Contract> save(Contract contract);

    Mono<Contract> findActiveByEmployeeId(UUID tenantId, UUID employeeId);

    Flux<Contract> findByEmployeeId(UUID tenantId, UUID employeeId);

    Mono<Contract> findById(UUID tenantId, UUID contractId);
}
