package yowyob.comops.api.hrm.application.service;

import org.springframework.context.annotation.Profile;
import yowyob.comops.api.hrm.application.port.in.*;
import yowyob.comops.api.hrm.application.port.out.*;
import yowyob.comops.api.hrm.domain.model.*;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;

import java.util.UUID;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Profile("!test-memory")
@Service
public class RecruitmentService implements ManageRecruitmentUseCase {

    private final JobOfferRepository jobOfferRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final OnboardingTaskRepository onboardingTaskRepository;

    public RecruitmentService(JobOfferRepository jobOfferRepository,
                              ApplicationRepository applicationRepository,
                              InterviewRepository interviewRepository,
                              OnboardingTaskRepository onboardingTaskRepository) {
        this.jobOfferRepository = jobOfferRepository;
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
        this.onboardingTaskRepository = onboardingTaskRepository;
    }

    @Override
    public Mono<JobOffer> createJobOffer(CreateJobOfferCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> {
                    JobOffer offer = JobOffer.create(ctx.tenantId(), ctx.organizationId(),
                            command.agencyId(), command.poste(), command.departement(),
                            command.localisation(), command.competencesRequises(),
                            command.dateLimite(), command.packageSalarial());
                    return jobOfferRepository.save(offer);
                });
    }

    @Override
    public Mono<JobOffer> publishJobOffer(UUID jobOfferId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> jobOfferRepository.findById(ctx.tenantId(), jobOfferId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Job offer not found")))
                        .map(JobOffer::publish)
                        .flatMap(jobOfferRepository::save));
    }

    @Override
    public Mono<JobOffer> closeJobOffer(UUID jobOfferId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> jobOfferRepository.findById(ctx.tenantId(), jobOfferId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Job offer not found")))
                        .map(JobOffer::close)
                        .flatMap(jobOfferRepository::save));
    }

    @Override
    public Mono<JobOffer> getJobOffer(UUID jobOfferId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> jobOfferRepository.findById(ctx.tenantId(), jobOfferId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Job offer not found"))));
    }

    @Override
    public Flux<JobOffer> listJobOffersByOrganization(UUID organizationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> jobOfferRepository.findByOrganizationId(ctx.tenantId(), organizationId));
    }

    @Override
    public Mono<Application> createApplication(CreateApplicationCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> jobOfferRepository.findById(ctx.tenantId(), command.jobOfferId())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Job offer not found")))
                        .flatMap(offer -> {
                            Application app = Application.create(ctx.tenantId(), command.jobOfferId(),
                                    command.candidatNom(), command.candidatPrenom(), command.candidatEmail(),
                                    command.candidatTelephone(), command.cvFileId(), command.lettreMotivationFileId());
                            return applicationRepository.save(app);
                        }));
    }

    @Override
    public Mono<Application> shortlistApplication(UUID applicationId) {
        return updateApplication(applicationId, Application::shortlist);
    }

    @Override
    public Mono<Application> interviewApplication(UUID applicationId) {
        return updateApplication(applicationId, Application::interview);
    }

    @Override
    public Mono<Application> offerApplication(UUID applicationId) {
        return updateApplication(applicationId, Application::offer);
    }

    @Override
    public Mono<Application> rejectApplication(UUID applicationId) {
        return updateApplication(applicationId, Application::reject);
    }

    @Override
    public Mono<Application> hireApplication(UUID applicationId) {
        return updateApplication(applicationId, Application::hire);
    }

    @Override
    public Mono<Application> getApplication(UUID applicationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> applicationRepository.findById(ctx.tenantId(), applicationId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Application not found"))));
    }

    @Override
    public Flux<Application> listApplicationsByJobOffer(UUID jobOfferId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> applicationRepository.findByJobOfferId(ctx.tenantId(), jobOfferId));
    }

    @Override
    public Mono<Interview> scheduleInterview(ScheduleInterviewCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> applicationRepository.findById(ctx.tenantId(), command.applicationId())
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Application not found")))
                        .flatMap(app -> {
                            Interview interview = Interview.create(ctx.tenantId(), command.applicationId(),
                                    command.type(), command.dateHeure(), command.lieu(),
                                    command.interviewerPartyId(), command.interviewerDisplayName());
                            return interviewRepository.save(interview);
                        }));
    }

    @Override
    public Mono<Interview> completeInterview(UUID interviewId, String notes, InterviewResult resultat) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> interviewRepository.findById(ctx.tenantId(), interviewId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Interview not found")))
                        .map(i -> i.complete(notes, resultat))
                        .flatMap(interviewRepository::save));
    }

    @Override
    public Flux<Interview> listInterviewsByApplication(UUID applicationId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> interviewRepository.findByApplicationId(ctx.tenantId(), applicationId));
    }

    @Override
    public Mono<OnboardingTask> createOnboardingTask(CreateOnboardingTaskCommand command) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> {
                    OnboardingTask task = OnboardingTask.create(ctx.tenantId(), command.employeeId(),
                            command.titre(), command.description(), command.assignedToPartyId(), command.echeance());
                    return onboardingTaskRepository.save(task);
                });
    }

    @Override
    public Mono<OnboardingTask> startOnboardingTask(UUID taskId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> onboardingTaskRepository.findById(ctx.tenantId(), taskId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Onboarding task not found")))
                        .map(OnboardingTask::start)
                        .flatMap(onboardingTaskRepository::save));
    }

    @Override
    public Mono<OnboardingTask> completeOnboardingTask(UUID taskId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> onboardingTaskRepository.findById(ctx.tenantId(), taskId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Onboarding task not found")))
                        .map(OnboardingTask::complete)
                        .flatMap(onboardingTaskRepository::save));
    }

    @Override
    public Flux<OnboardingTask> listOnboardingTasksByEmployee(UUID employeeId) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> onboardingTaskRepository.findByEmployeeId(ctx.tenantId(), employeeId));
    }

    private Mono<Application> updateApplication(UUID applicationId, java.util.function.Function<Application, Application> transition) {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMap(ctx -> applicationRepository.findById(ctx.tenantId(), applicationId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Application not found")))
                        .map(transition)
                        .flatMap(applicationRepository::save));
    }
}
