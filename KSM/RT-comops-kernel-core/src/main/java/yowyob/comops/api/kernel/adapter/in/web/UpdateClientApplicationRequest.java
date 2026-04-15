package yowyob.comops.api.kernel.adapter.in.web;

public record UpdateClientApplicationRequest(
        String name,
        String description,
        java.util.List<String> allowedServices) {
}
