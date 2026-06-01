package yowyob.comops.api.hrm.application.port.in;

public record UpdateEmergencyContactCommand(
        String nom,
        String prenom,
        String relation,
        String telephone,
        String email,
        int priorite) {
}
