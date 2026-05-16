package yowyob.comops.api.kernel.adapter.in.web;

public record CreateClientApplicationRequest(
        String clientId,
        String name,
        String description,
        String clientSecret,
        java.util.List<String> allowedServices) {
}
