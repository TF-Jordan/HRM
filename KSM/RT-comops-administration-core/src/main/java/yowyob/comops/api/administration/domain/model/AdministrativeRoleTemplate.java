package yowyob.comops.api.administration.domain.model;

import java.util.Set;

public record AdministrativeRoleTemplate(String code, String name, String scopeType, Set<String> permissions,
        boolean protectedTemplate) {
}
