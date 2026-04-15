package yowyob.comops.api.organization.application.service;

import yowyob.comops.api.organization.domain.model.OpeningHoursExceptionRule;
import yowyob.comops.api.organization.domain.model.OpeningHoursRule;
import java.util.List;

public record AgencyScheduleView(List<OpeningHoursRule> regularRules, List<OpeningHoursExceptionRule> upcomingExceptions) {
}
