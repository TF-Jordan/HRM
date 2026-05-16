package yowyob.comops.api.organization.application.service;

import yowyob.comops.api.organization.application.port.in.AddOpeningHoursExceptionCommand;
import yowyob.comops.api.organization.application.port.in.AddOpeningHoursExceptionUseCase;
import yowyob.comops.api.organization.application.port.in.GetAgencyOpenStatusUseCase;
import yowyob.comops.api.organization.application.port.in.GetAgencyScheduleUseCase;
import yowyob.comops.api.organization.application.port.in.ListOpeningHoursUseCase;
import yowyob.comops.api.organization.application.port.in.RemoveOpeningHoursExceptionUseCase;
import yowyob.comops.api.organization.application.port.in.ReplaceRegularOpeningHoursUseCase;
import yowyob.comops.api.organization.application.port.in.UpsertOpeningHoursCommand;
import yowyob.comops.api.organization.application.port.in.UpsertOpeningHoursUseCase;
import yowyob.comops.api.organization.application.port.out.AgencyRepository;
import yowyob.comops.api.organization.application.port.out.OpeningHoursExceptionRepository;
import yowyob.comops.api.organization.application.port.out.OpeningHoursRepository;
import yowyob.comops.api.organization.domain.AgencyNotFoundException;
import yowyob.comops.api.organization.domain.model.OpeningHoursExceptionRule;
import yowyob.comops.api.organization.domain.model.OpeningHoursRule;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

@Service
public class OpeningHoursApplicationService implements UpsertOpeningHoursUseCase, ListOpeningHoursUseCase,
        ReplaceRegularOpeningHoursUseCase, AddOpeningHoursExceptionUseCase, RemoveOpeningHoursExceptionUseCase,
        GetAgencyScheduleUseCase, GetAgencyOpenStatusUseCase {

    private final OpeningHoursRepository openingHoursRepository;
    private final OpeningHoursExceptionRepository openingHoursExceptionRepository;
    private final AgencyRepository agencyRepository;

    public OpeningHoursApplicationService(OpeningHoursRepository openingHoursRepository,
            OpeningHoursExceptionRepository openingHoursExceptionRepository,
            AgencyRepository agencyRepository) {
        this.openingHoursRepository = openingHoursRepository;
        this.openingHoursExceptionRepository = openingHoursExceptionRepository;
        this.agencyRepository = agencyRepository;
    }

    @Override
    public Mono<OpeningHoursRule> upsert(UpsertOpeningHoursCommand command) {
        Objects.requireNonNull(command, "command is required");
        return validateAgencyScope(command.tenantId(), command.organizationId(), command.agencyId())
                .then(openingHoursRepository.findByScopeAndDay(command.tenantId(), command.organizationId(),
                                command.agencyId(), command.dayOfWeek())
                        .map(rule -> rule.update(command.opensAt(), command.closesAt(), command.closed()))
                        .switchIfEmpty(Mono.fromSupplier(() -> OpeningHoursRule.create(command.tenantId(),
                                command.organizationId(), command.agencyId(), command.dayOfWeek(),
                                command.opensAt(), command.closesAt(), command.closed())))
                        .flatMap(openingHoursRepository::save));
    }

    @Override
    public Flux<OpeningHoursRule> listByAgency(java.util.UUID organizationId, java.util.UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(context -> openingHoursRepository.findByAgencyId(context.tenantId(), organizationId, agencyId));
    }

    @Override
    public Flux<OpeningHoursRule> replaceRegularHours(java.util.UUID tenantId, java.util.UUID organizationId,
            java.util.UUID agencyId, List<UpsertOpeningHoursCommand> commands) {
        return validateAgencyScope(tenantId, organizationId, agencyId)
                .then(openingHoursRepository.deleteAllByAgencyId(tenantId, organizationId, agencyId))
                .thenMany(Flux.fromIterable(commands)
                        .flatMap(this::upsert))
                .sort(Comparator.comparing(OpeningHoursRule::dayOfWeek));
    }

    @Override
    public Mono<OpeningHoursExceptionRule> addException(AddOpeningHoursExceptionCommand command) {
        Objects.requireNonNull(command, "command is required");
        return validateAgencyScope(command.tenantId(), command.organizationId(), command.agencyId())
                .then(openingHoursExceptionRepository.save(OpeningHoursExceptionRule.create(command.tenantId(),
                        command.organizationId(), command.agencyId(), command.exceptionDate(), command.label(),
                        command.opensAt(), command.closesAt(), command.closed())));
    }

    @Override
    public Mono<Void> removeException(java.util.UUID exceptionId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> openingHoursExceptionRepository.deleteById(context.tenantId(), exceptionId));
    }

    @Override
    public Mono<AgencyScheduleView> getSchedule(java.util.UUID organizationId, java.util.UUID agencyId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> Mono.zip(
                        openingHoursRepository.findByAgencyId(context.tenantId(), organizationId, agencyId)
                                .sort(Comparator.comparing(OpeningHoursRule::dayOfWeek))
                                .collectList(),
                        openingHoursExceptionRepository.findFutureByAgencyId(context.tenantId(), organizationId, agencyId,
                                        LocalDate.now())
                                .sort(Comparator.comparing(OpeningHoursExceptionRule::exceptionDate))
                                .collectList()))
                .map(tuple -> new AgencyScheduleView(tuple.getT1(), tuple.getT2()));
    }

    @Override
    public Mono<Boolean> isOpen(java.util.UUID organizationId, java.util.UUID agencyId, LocalDateTime at) {
        LocalDateTime reference = at == null ? LocalDateTime.now() : at;
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(context -> openingHoursExceptionRepository.findByAgencyId(context.tenantId(), organizationId, agencyId)
                        .filter(rule -> rule.exceptionDate().equals(reference.toLocalDate()))
                        .sort(Comparator.comparing(OpeningHoursExceptionRule::createdAt))
                        .collectList()
                        .flatMap(exceptions -> {
                            if (!exceptions.isEmpty()) {
                                return Mono.just(exceptions.stream().anyMatch(rule -> rule.appliesAt(reference)));
                            }
                            return openingHoursRepository.findByScopeAndDay(context.tenantId(), organizationId, agencyId,
                                            reference.getDayOfWeek())
                                    .map(rule -> !rule.closed()
                                            && !reference.toLocalTime().isBefore(rule.opensAt())
                                            && reference.toLocalTime().isBefore(rule.closesAt()))
                                    .defaultIfEmpty(false);
                        }));
    }

    private Mono<Void> validateAgencyScope(java.util.UUID tenantId, java.util.UUID organizationId, java.util.UUID agencyId) {
        return agencyRepository.findById(tenantId, agencyId)
                .switchIfEmpty(Mono.error(new AgencyNotFoundException(agencyId)))
                .flatMap(agency -> {
                    if (!agency.organizationId().equals(organizationId)) {
                        return Mono.error(new IllegalArgumentException("agency does not belong to the organization"));
                    }
                    return Mono.empty();
                });
    }
}
