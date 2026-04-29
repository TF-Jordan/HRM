package yowyob.comops.api.hrm.application.service;

import yowyob.comops.api.hrm.application.port.in.CreateSocialDeclarationCommand;
import yowyob.comops.api.hrm.application.port.in.ManageSocialDeclarationUseCase;
import yowyob.comops.api.hrm.application.port.out.SocialDeclarationRepository;
import yowyob.comops.api.hrm.domain.model.DeclarationType;
import yowyob.comops.api.hrm.domain.model.SocialDeclaration;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SocialDeclarationService implements ManageSocialDeclarationUseCase {

    private final SocialDeclarationRepository socialDeclarationRepository;

    public SocialDeclarationService(SocialDeclarationRepository socialDeclarationRepository) {
        this.socialDeclarationRepository = socialDeclarationRepository;
    }

    @Override
    public Mono<SocialDeclaration> create(CreateSocialDeclarationCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> {
                    SocialDeclaration declaration = SocialDeclaration.create(ctx.tenantId(),
                            command.organizationId(), DeclarationType.valueOf(command.type()),
                            command.periode(), command.format());
                    return socialDeclarationRepository.save(declaration);
                });
    }

    @Override
    public Mono<SocialDeclaration> generate(UUID id, UUID fichierId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> socialDeclarationRepository.findById(ctx.tenantId(), id)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Social declaration not found")))
                        .map(d -> d.generate(fichierId))
                        .flatMap(socialDeclarationRepository::save));
    }

    @Override
    public Mono<SocialDeclaration> submit(UUID id) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> socialDeclarationRepository.findById(ctx.tenantId(), id)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Social declaration not found")))
                        .map(SocialDeclaration::submit)
                        .flatMap(socialDeclarationRepository::save));
    }

    @Override
    public Mono<SocialDeclaration> acknowledge(UUID id) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> socialDeclarationRepository.findById(ctx.tenantId(), id)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Social declaration not found")))
                        .map(SocialDeclaration::acknowledge)
                        .flatMap(socialDeclarationRepository::save));
    }

    @Override
    public Mono<SocialDeclaration> get(UUID id) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> socialDeclarationRepository.findById(ctx.tenantId(), id)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Social declaration not found"))));
    }

    @Override
    public Flux<SocialDeclaration> listByOrganization(UUID orgId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> socialDeclarationRepository.findByOrganizationId(ctx.tenantId(), orgId));
    }
}
