package yowyob.comops.api.hrm.domain;

import java.util.UUID;

public class EmergencyContactNotFoundException extends RuntimeException {

    public EmergencyContactNotFoundException(UUID contactId) {
        super("Emergency contact not found: " + contactId);
    }
}
