package yowyob.comops.api.organization.adapter.in.web;

public record AgencyOpenStatusResponse(boolean open, String status) {

    public static AgencyOpenStatusResponse from(boolean open) {
        return new AgencyOpenStatusResponse(open, open ? "OPEN" : "CLOSED");
    }
}
