package yowyob.comops.api.hrm.domain;

import yowyob.comops.api.common.domain.DomainException;

/**
 * Raised when creating a skill whose name already exists in the same tenant,
 * compared case-insensitively (e.g. "Gérer" == "gérer"). Skill names are the
 * referential identity, so duplicates would fragment the competency taxonomy.
 */
public final class DuplicateSkillException extends DomainException {
    public DuplicateSkillException(String name) {
        super("A skill with this name already exists: " + name);
    }
}
