package yowyob.comops.api.hrm.application.port.in;

public record CreateSkillCommand(
        String name,
        String categorie,
        String description) {
}
