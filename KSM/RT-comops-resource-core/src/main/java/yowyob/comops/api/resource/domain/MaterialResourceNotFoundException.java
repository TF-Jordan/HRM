package yowyob.comops.api.resource.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public final class MaterialResourceNotFoundException extends DomainException {
    public MaterialResourceNotFoundException(UUID resourceId) {
        super("Material resource not found: " + resourceId);
    }
}
