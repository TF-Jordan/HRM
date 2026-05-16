package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.organization.domain.model.EmployeeMembership;
import java.util.UUID;

public record EmployeeMembershipResponse(
        UUID id,
        UUID organizationId,
        UUID userId,
        UUID actorId,
        String email,
        UUID agencyId,
        UUID roleId,
        String status) {

    public static EmployeeMembershipResponse from(EmployeeMembership membership) {
        return new EmployeeMembershipResponse(membership.id(), membership.organizationId(), membership.userId(),
                membership.actorId(), membership.email(), membership.agencyId(), membership.roleId(), membership.status());
    }
}
