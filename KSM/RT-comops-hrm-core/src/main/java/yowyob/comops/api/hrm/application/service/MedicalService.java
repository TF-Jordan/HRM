package yowyob.comops.api.hrm.application.service;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.hrm.application.port.in.CreateMedicalCertificateCommand;
import yowyob.comops.api.hrm.application.port.in.CreateMedicalVisitCommand;
import yowyob.comops.api.hrm.application.port.in.ManageMedicalUseCase;
import yowyob.comops.api.hrm.application.port.out.MedicalCertificateRepository;
import yowyob.comops.api.hrm.application.port.out.MedicalVisitRepository;
import yowyob.comops.api.hrm.domain.model.MedicalCertificate;
import yowyob.comops.api.hrm.domain.model.MedicalVisit;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@Service
public class MedicalService implements ManageMedicalUseCase {

    private final MedicalVisitRepository medicalVisitRepository;
    private final MedicalCertificateRepository medicalCertificateRepository;

    public MedicalService(MedicalVisitRepository medicalVisitRepository,
                          MedicalCertificateRepository medicalCertificateRepository) {
        this.medicalVisitRepository = medicalVisitRepository;
        this.medicalCertificateRepository = medicalCertificateRepository;
    }

    @Override
    public Mono<MedicalVisit> createVisit(CreateMedicalVisitCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> {
                    MedicalVisit visit = MedicalVisit.create(ctx.tenantId(), command.employeeId(),
                            command.dateVisite(), command.medecin(), command.resultatAptitude(),
                            command.restrictions(), command.prochaineEcheance(), command.certificatFileId());
                    return medicalVisitRepository.save(visit);
                });
    }

    @Override
    public Mono<MedicalVisit> getVisit(UUID visitId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> medicalVisitRepository.findById(ctx.tenantId(), visitId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Medical visit not found"))));
    }

    @Override
    public Flux<MedicalVisit> listVisitsByEmployee(UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> medicalVisitRepository.findByEmployeeId(ctx.tenantId(), employeeId));
    }

    @Override
    public Flux<MedicalVisit> listVisits(UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> medicalVisitRepository.findAll(ctx.tenantId()));
    }

    @Override
    public Mono<MedicalCertificate> createCertificate(CreateMedicalCertificateCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> {
                    MedicalCertificate cert = MedicalCertificate.create(ctx.tenantId(), command.employeeId(),
                            command.typeCertificat(), command.dateEmission(), command.dateExpiration(),
                            command.statut(), command.fichierId());
                    return medicalCertificateRepository.save(cert);
                });
    }

    @Override
    public Mono<MedicalCertificate> getCertificate(UUID certificateId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> medicalCertificateRepository.findById(ctx.tenantId(), certificateId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Medical certificate not found"))));
    }

    @Override
    public Flux<MedicalCertificate> listCertificatesByEmployee(UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> medicalCertificateRepository.findByEmployeeId(ctx.tenantId(), employeeId));
    }

    @Override
    public Flux<MedicalCertificate> listCertificates(UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> medicalCertificateRepository.findAll(ctx.tenantId()));
    }
}
