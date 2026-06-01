package yowyob.comops.api.hrm.domain.model;

import java.util.UUID;

/** Lightweight projection representing a (tenantId, organizationId) pair. */
public record TenantOrgPair(UUID tenantId, UUID organizationId) {}
