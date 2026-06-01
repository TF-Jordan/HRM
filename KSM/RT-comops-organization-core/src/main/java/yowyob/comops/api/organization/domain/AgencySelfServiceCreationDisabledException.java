package yowyob.comops.api.organization.domain;

public class AgencySelfServiceCreationDisabledException extends RuntimeException {

    public AgencySelfServiceCreationDisabledException() {
        super("Agency self-service creation is disabled by platform options.");
    }
}
