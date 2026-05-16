package yowyob.comops.api.bootstrap.integration.inventory;

import java.util.UUID;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import yowyob.comops.api.inventory.application.port.out.ResourceOccupancyGateway;
import yowyob.comops.api.resource.application.port.in.ListResourceAssignmentsUseCase;
import yowyob.comops.api.resource.application.port.in.ListResourceReservationsUseCase;

@Component
public class ResourceCoreInventoryResourceOccupancyGateway implements ResourceOccupancyGateway {

    private final ListResourceAssignmentsUseCase listResourceAssignmentsUseCase;
    private final ListResourceReservationsUseCase listResourceReservationsUseCase;

    public ResourceCoreInventoryResourceOccupancyGateway(
            ListResourceAssignmentsUseCase listResourceAssignmentsUseCase,
            ListResourceReservationsUseCase listResourceReservationsUseCase) {
        this.listResourceAssignmentsUseCase = listResourceAssignmentsUseCase;
        this.listResourceReservationsUseCase = listResourceReservationsUseCase;
    }

    @Override
    public Mono<Long> countActiveAssignments(UUID tenantId, String targetType, UUID targetId) {
        return listResourceAssignmentsUseCase.listAssignments(tenantId, targetType, targetId)
                .filter(assignment -> "ACTIVE".equals(assignment.status()))
                .count();
    }

    @Override
    public Mono<Long> countActiveReservations(UUID tenantId, String targetType, UUID targetId) {
        return listResourceReservationsUseCase.listReservations(tenantId, targetType, targetId)
                .filter(reservation -> !"RELEASED".equals(reservation.status()))
                .count();
    }
}
