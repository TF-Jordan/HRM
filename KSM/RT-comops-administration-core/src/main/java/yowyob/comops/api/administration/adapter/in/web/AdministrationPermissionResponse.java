package yowyob.comops.api.administration.adapter.in.web;

import yowyob.comops.api.administration.domain.model.PermissionCatalogEntry;

public record AdministrationPermissionResponse(String code, String name, String description, String module,
        String scope, boolean system, boolean assignable, boolean deprecated) {

    public static AdministrationPermissionResponse from(PermissionCatalogEntry entry) {
        return new AdministrationPermissionResponse(entry.code(), entry.name(), entry.description(), entry.module(),
                entry.scope(), entry.system(), entry.assignable(), entry.deprecated());
    }
}
