package yowyob.comops.api.hrm.application.port.in;

import yowyob.comops.api.hrm.domain.model.*;

import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ManageRecruitmentUseCase {

    Mono<JobOffer> createJobOffer(CreateJobOfferCommand command);
    Mono<JobOffer> publishJobOffer(UUID jobOfferId);
    Mono<JobOffer> closeJobOffer(UUID jobOfferId);
    Mono<JobOffer> getJobOffer(UUID jobOfferId);
    Flux<JobOffer> listJobOffersByOrganization(UUID organizationId);

    Mono<Application> createApplication(CreateApplicationCommand command);
    Mono<Application> shortlistApplication(UUID applicationId);
    Mono<Application> interviewApplication(UUID applicationId);
    Mono<Application> offerApplication(UUID applicationId);
    Mono<Application> rejectApplication(UUID applicationId);
    Mono<Application> hireApplication(UUID applicationId);

    /**
     * End-to-end conversion: provisions an Actor from the candidate's identity,
     * creates the HRM Employee (matricule generated server-side) with an active
     * contract, and marks the application HIRED. Returns the new employee.
     */
    Mono<Employee> convertApplicationToEmployee(ConvertApplicationCommand command);

    Mono<Application> getApplication(UUID applicationId);
    Flux<Application> listApplicationsByJobOffer(UUID jobOfferId);

    Mono<Interview> scheduleInterview(ScheduleInterviewCommand command);
    Mono<Interview> completeInterview(UUID interviewId, String notes, InterviewResult resultat);
    Flux<Interview> listInterviewsByApplication(UUID applicationId);

    Mono<OnboardingTask> createOnboardingTask(CreateOnboardingTaskCommand command);
    Mono<OnboardingTask> startOnboardingTask(UUID taskId);
    Mono<OnboardingTask> completeOnboardingTask(UUID taskId);
    Flux<OnboardingTask> listOnboardingTasksByEmployee(UUID employeeId);
}
