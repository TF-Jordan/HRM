package yowyob.comops.api.organization.domain;

public class UserAccountNotFoundInOrganizationContextException extends RuntimeException {

    public UserAccountNotFoundInOrganizationContextException(String email) {
        super("User account with email " + email + " was not found in the current tenant.");
    }
}
