package yowyob.comops.api.inventory.application.port.out;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface ResourceOccupancyGateway {

    Mono<Long> countActiveAssignments(UUID tenantId, String targetType, UUID targetId);

    Mono<Long> countActiveReservations(UUID tenantId, String targetType, UUID targetId);
}
