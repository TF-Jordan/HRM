package yowyob.comops.api.organization.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

public record InviteEmployeeRequest(@Email @NotBlank String email, UUID roleId, UUID agencyId, List<String> permissions) {
}
