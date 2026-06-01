package yowyob.comops.api.organization.domain;

public class OrganizationSelfServiceCreationDisabledException extends RuntimeException {

    public OrganizationSelfServiceCreationDisabledException() {
        super("Organization self-service creation is disabled by platform options.");
    }
}
