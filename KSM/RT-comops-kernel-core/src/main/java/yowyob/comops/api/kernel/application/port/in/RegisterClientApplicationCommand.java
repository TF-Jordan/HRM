package yowyob.comops.api.kernel.application.port.in;

public record RegisterClientApplicationCommand(
        String clientId,
        String name,
        String description,
        String clientSecret,
        java.util.List<String> allowedServices,
        boolean systemManaged) {
}
