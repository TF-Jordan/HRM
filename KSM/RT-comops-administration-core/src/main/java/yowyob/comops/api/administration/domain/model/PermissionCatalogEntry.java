package yowyob.comops.api.administration.domain.model;

public record PermissionCatalogEntry(
        String code,
        String name,
        String description,
        String module,
        String scope,
        boolean system,
        boolean assignable,
        boolean deprecated) {
}
