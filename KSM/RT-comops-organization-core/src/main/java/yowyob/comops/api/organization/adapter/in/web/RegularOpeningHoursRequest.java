package yowyob.comops.api.organization.adapter.in.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record RegularOpeningHoursRequest(@NotEmpty List<@Valid UpsertOpeningHoursRequest> rules) {
}
