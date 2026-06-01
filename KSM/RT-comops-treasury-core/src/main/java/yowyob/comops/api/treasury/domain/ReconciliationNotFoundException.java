package yowyob.comops.api.treasury.domain;

import yowyob.comops.api.common.domain.DomainException;
import java.util.UUID;

public final class ReconciliationNotFoundException extends DomainException {
    public ReconciliationNotFoundException(UUID reconciliationId) { super("Reconciliation not found: " + reconciliationId); }
}
