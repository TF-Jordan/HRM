package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.organization.domain.model.PhysicalSpace;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PhysicalSpaceResponse(
        UUID id,
        UUID tenantId,
        UUID organizationId,
        UUID agencyId,
        UUID parentSpaceId,
        String code,
        String name,
        String spaceType,
        String description,
        Integer levelNumber,
        Integer capacity,
        boolean active,
        List<PhysicalSpaceResponse> children) {

    public static PhysicalSpaceResponse from(PhysicalSpace physicalSpace) {
        return new PhysicalSpaceResponse(physicalSpace.id(), physicalSpace.tenantId(), physicalSpace.organizationId(),
                physicalSpace.agencyId(), physicalSpace.parentSpaceId(), physicalSpace.code(), physicalSpace.name(),
                physicalSpace.spaceType(), physicalSpace.description(), physicalSpace.levelNumber(),
                physicalSpace.capacity(), physicalSpace.active(), List.of());
    }

    public static List<PhysicalSpaceResponse> tree(List<PhysicalSpace> spaces) {
        Map<UUID, List<PhysicalSpace>> childrenByParent = new LinkedHashMap<>();
        for (PhysicalSpace space : spaces) {
            childrenByParent.computeIfAbsent(space.parentSpaceId(), ignored -> new ArrayList<>()).add(space);
        }
        childrenByParent.values().forEach(children -> children.sort(Comparator
                .comparing((PhysicalSpace space) -> space.levelNumber() == null ? Integer.MAX_VALUE : space.levelNumber())
                .thenComparing(PhysicalSpace::code, String.CASE_INSENSITIVE_ORDER)));
        return build(childrenByParent, null);
    }

    private static List<PhysicalSpaceResponse> build(Map<UUID, List<PhysicalSpace>> childrenByParent, UUID parentId) {
        List<PhysicalSpace> children = childrenByParent.getOrDefault(parentId, List.of());
        List<PhysicalSpaceResponse> responses = new ArrayList<>(children.size());
        for (PhysicalSpace child : children) {
            responses.add(new PhysicalSpaceResponse(child.id(), child.tenantId(), child.organizationId(),
                    child.agencyId(), child.parentSpaceId(), child.code(), child.name(), child.spaceType(),
                    child.description(), child.levelNumber(), child.capacity(), child.active(),
                    build(childrenByParent, child.id())));
        }
        return responses;
    }
}
