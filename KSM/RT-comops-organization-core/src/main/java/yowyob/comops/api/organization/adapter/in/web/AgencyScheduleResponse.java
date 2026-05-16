package yowyob.comops.api.organization.adapter.in.web;

import yowyob.comops.api.organization.application.service.AgencyScheduleView;
import java.util.List;

public record AgencyScheduleResponse(
        List<OpeningHoursResponse> regularRules,
        List<OpeningHoursExceptionResponse> upcomingExceptions) {

    public static AgencyScheduleResponse from(AgencyScheduleView view) {
        return new AgencyScheduleResponse(view.regularRules().stream().map(OpeningHoursResponse::from).toList(),
                view.upcomingExceptions().stream().map(OpeningHoursExceptionResponse::from).toList());
    }
}
