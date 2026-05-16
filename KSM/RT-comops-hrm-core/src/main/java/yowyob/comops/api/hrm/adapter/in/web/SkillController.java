package yowyob.comops.api.hrm.adapter.in.web;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.hrm.application.port.in.CreateEmployeeSkillCommand;
import yowyob.comops.api.hrm.application.port.in.CreateSkillCommand;
import yowyob.comops.api.hrm.application.port.in.ManageSkillUseCase;
import yowyob.comops.api.hrm.domain.model.EmployeeSkill;
import yowyob.comops.api.hrm.domain.model.Skill;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@RestController
@RequestMapping("/api/v1/hrm/skills")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class SkillController {

    private final ManageSkillUseCase skillUseCase;

    public SkillController(ManageSkillUseCase skillUseCase) {
        this.skillUseCase = skillUseCase;
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:skill:create')")
    public Mono<ResponseEntity<ApiResponse<SkillResponse>>> createSkill(
            @Valid @RequestBody Mono<CreateSkillRequest> requestMono) {
        return requestMono.map(CreateSkillRequest::toCommand)
                .flatMap(skillUseCase::createSkill)
                .map(SkillResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(r, "Skill created.")));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:skill:read')")
    public Mono<ResponseEntity<ApiResponse<SkillResponse>>> getSkill(@PathVariable UUID id) {
        return skillUseCase.getSkill(id).map(SkillResponse::from)
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Skill fetched.")));
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:skill:read')")
    public Mono<ResponseEntity<ApiResponse<List<SkillResponse>>>> listSkills() {
        return skillUseCase.listSkills()
                .map(SkillResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Skills fetched.")));
    }

    @PostMapping("/employee-skills")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:skill:create')")
    public Mono<ResponseEntity<ApiResponse<EmployeeSkillResponse>>> createEmployeeSkill(
            @Valid @RequestBody Mono<CreateEmployeeSkillRequest> requestMono) {
        return requestMono.map(CreateEmployeeSkillRequest::toCommand)
                .flatMap(skillUseCase::createEmployeeSkill)
                .map(EmployeeSkillResponse::from)
                .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(r, "Employee skill created.")));
    }

    @GetMapping("/employees/{employeeId}/skills")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'hrm:skill:read')")
    public Mono<ResponseEntity<ApiResponse<List<EmployeeSkillResponse>>>> listByEmployee(
            @PathVariable UUID employeeId) {
        return skillUseCase.listEmployeeSkillsByEmployee(employeeId)
                .map(EmployeeSkillResponse::from).collectList()
                .map(l -> ResponseEntity.ok(ApiResponse.success(l, "Employee skills fetched.")));
    }

    public record CreateSkillRequest(String name, String categorie, String description) {
        CreateSkillCommand toCommand() {
            return new CreateSkillCommand(name, categorie, description);
        }
    }

    public record SkillResponse(UUID id, String name, String categorie, String description) {
        static SkillResponse from(Skill s) {
            return new SkillResponse(s.id(), s.name(), s.categorie(), s.description());
        }
    }

    public record CreateEmployeeSkillRequest(UUID employeeId, UUID skillId, int niveauActuel,
            int niveauAttendu, LocalDate dateEvaluation) {
        CreateEmployeeSkillCommand toCommand() {
            return new CreateEmployeeSkillCommand(employeeId, skillId, niveauActuel, niveauAttendu, dateEvaluation);
        }
    }

    public record EmployeeSkillResponse(UUID id, UUID employeeId, UUID skillId, int niveauActuel,
            int niveauAttendu, LocalDate dateEvaluation) {
        static EmployeeSkillResponse from(EmployeeSkill es) {
            return new EmployeeSkillResponse(es.id(), es.employeeId(), es.skillId(),
                    es.niveauActuel(), es.niveauAttendu(), es.dateEvaluation());
        }
    }
}
