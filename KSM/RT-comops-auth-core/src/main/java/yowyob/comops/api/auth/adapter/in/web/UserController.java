package yowyob.comops.api.auth.adapter.in.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import yowyob.comops.api.auth.application.port.in.GetCurrentUserProfileUseCase;
import yowyob.comops.api.auth.application.port.in.RegisterUserCommand;
import yowyob.comops.api.auth.application.port.in.RegisterUserUseCase;
import yowyob.comops.api.auth.application.port.in.UpdateCurrentUserOnboardingUseCase;
import yowyob.comops.api.auth.application.port.in.UpdateCurrentUserPlanUseCase;
import yowyob.comops.api.auth.application.port.out.UserAccountRepository;
import yowyob.comops.api.auth.application.service.AuthApplicationService;
import yowyob.comops.api.auth.application.service.AuthEmailDeliveryService;
import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/users")
@PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
public class UserController {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnopqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String SYMBOLS = "@#%&!";
    private static final String ALL = UPPER + LOWER + DIGITS + SYMBOLS;

    private final GetCurrentUserProfileUseCase getCurrentUserProfileUseCase;
    private final UpdateCurrentUserPlanUseCase updateCurrentUserPlanUseCase;
    private final UpdateCurrentUserOnboardingUseCase updateCurrentUserOnboardingUseCase;
    private final AuthApplicationService authApplicationService;
    private final AuthUserViewAssembler authUserViewAssembler;
    private final UserAccountRepository userAccountRepository;
    private final RegisterUserUseCase registerUserUseCase;
    private final AuthEmailDeliveryService emailDeliveryService;
    private final ObjectMapper objectMapper;

    public UserController(GetCurrentUserProfileUseCase getCurrentUserProfileUseCase,
            UpdateCurrentUserPlanUseCase updateCurrentUserPlanUseCase,
            UpdateCurrentUserOnboardingUseCase updateCurrentUserOnboardingUseCase,
            AuthApplicationService authApplicationService,
            AuthUserViewAssembler authUserViewAssembler,
            UserAccountRepository userAccountRepository,
            RegisterUserUseCase registerUserUseCase,
            AuthEmailDeliveryService emailDeliveryService,
            ObjectMapper objectMapper) {
        this.getCurrentUserProfileUseCase = getCurrentUserProfileUseCase;
        this.updateCurrentUserPlanUseCase = updateCurrentUserPlanUseCase;
        this.updateCurrentUserOnboardingUseCase = updateCurrentUserOnboardingUseCase;
        this.authApplicationService = authApplicationService;
        this.authUserViewAssembler = authUserViewAssembler;
        this.userAccountRepository = userAccountRepository;
        this.registerUserUseCase = registerUserUseCase;
        this.emailDeliveryService = emailDeliveryService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<ApiResponse<UserAccountResponse>>> getMe() {
        return getCurrentUserProfileUseCase.getCurrentUserProfile()
                .flatMap(authUserViewAssembler::toUserAccountResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Current user profile retrieved.")));
    }

    @GetMapping
    @PreAuthorize("@businessAccessPolicy.canManageIdentity(authentication)")
    public Mono<ResponseEntity<ApiResponse<List<UserSummaryResponse>>>> listUsers() {
        return ReactiveRequestContextHolder.getRequiredContext()
                .flatMapMany(ctx -> userAccountRepository.findByTenantId(ctx.tenantId()))
                .map(u -> new UserSummaryResponse(u.id(), u.actorId(), u.username(), u.email(),
                        u.phoneNumber(), u.status(), u.createdAt()))
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success(list, "Users fetched.")));
    }

    @PostMapping
    @PreAuthorize("@businessAccessPolicy.canManageIdentity(authentication)")
    public Mono<ResponseEntity<ApiResponse<AdminCreateUserResponse>>> adminCreateUser(
            @Valid @RequestBody Mono<AdminCreateUserRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> {
                    AdminCreateUserRequest req = tuple.getT1();
                    UUID tenantId = tuple.getT2().tenantId();
                    String tempPassword = req.password() != null && !req.password().isBlank()
                            ? req.password()
                            : generatePassword();
                    return registerUserUseCase.register(new RegisterUserCommand(
                                    tenantId, req.actorId(), req.username(), req.email(),
                                    req.phoneNumber(), tempPassword, "LOCAL", null))
                            .flatMap(saved -> {
                                if (Boolean.TRUE.equals(req.sendWelcomeEmail())) {
                                    return emailDeliveryService.deliverWelcomeMail(
                                                    saved.email(), saved.username(), tempPassword)
                                            .thenReturn(toAdminCreateResponse(saved, tempPassword, true));
                                }
                                return Mono.just(toAdminCreateResponse(saved, tempPassword, false));
                            });
                })
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "User created.")));
    }

    @PutMapping("/me/plan")
    public Mono<ResponseEntity<ApiResponse<UserAccountResponse>>> updateMyPlan(
            @Valid @RequestBody Mono<UpdatePlanRequest> requestMono) {
        return requestMono.flatMap(request -> updateCurrentUserPlanUseCase.updateCurrentUserPlan(request.plan()))
                .flatMap(authUserViewAssembler::toUserAccountResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "User plan updated.")));
    }

    @PutMapping("/me/onboarding")
    public Mono<ResponseEntity<ApiResponse<UserAccountResponse>>> updateOnboarding(
            @Valid @RequestBody Mono<UpdateOnboardingRequest> requestMono) {
        return requestMono.flatMap(request -> updateCurrentUserOnboardingUseCase
                        .updateCurrentUserOnboarding(request.step(), request.status()))
                .flatMap(authUserViewAssembler::toUserAccountResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "User onboarding updated.")));
    }

    @PostMapping("/me/change-password")
    public Mono<ResponseEntity<ApiResponse<UserAccountResponse>>> changeMyPassword(
            @Valid @RequestBody Mono<ChangePasswordRequest> requestMono) {
        return requestMono.flatMap(request -> authApplicationService.changeCurrentUserPassword(
                        request.oldPassword(), request.newPassword()))
                .flatMap(authUserViewAssembler::toUserAccountResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Password changed.")));
    }

    @PutMapping("/me/identity-onboarding")
    public Mono<ResponseEntity<ApiResponse<UserAccountResponse>>> updateIdentityOnboarding(
            @Valid @RequestBody Mono<UpdateIdentityOnboardingRequest> requestMono) {
        return requestMono.flatMap(request -> authApplicationService.updateCurrentIdentityOnboarding(
                        request.accountType(),
                        request.businessType(),
                        toJson(request.data()),
                        request.step(),
                        request.status()))
                .flatMap(authUserViewAssembler::toUserAccountResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Identity onboarding updated.")));
    }

    private static AdminCreateUserResponse toAdminCreateResponse(
            yowyob.comops.api.auth.domain.model.UserAccount saved,
            String tempPassword, boolean emailSent) {
        return new AdminCreateUserResponse(saved.id(), saved.actorId(), saved.username(),
                saved.email(), saved.status(), tempPassword, emailSent);
    }

    private static String generatePassword() {
        // Always include at least 1 of each character class to satisfy
        // the auth-core password policy (length>=10 + upper/lower/digit/symbol).
        char[] chars = new char[12];
        chars[0] = UPPER.charAt(RANDOM.nextInt(UPPER.length()));
        chars[1] = LOWER.charAt(RANDOM.nextInt(LOWER.length()));
        chars[2] = DIGITS.charAt(RANDOM.nextInt(DIGITS.length()));
        chars[3] = SYMBOLS.charAt(RANDOM.nextInt(SYMBOLS.length()));
        for (int i = 4; i < chars.length; i++) {
            chars[i] = ALL.charAt(RANDOM.nextInt(ALL.length()));
        }
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }

    public record UpdatePlanRequest(@NotBlank String plan) {
    }

    public record UpdateOnboardingRequest(@Min(0) int step, String status) {
    }

    public record UserSummaryResponse(UUID id, UUID actorId, String username, String email,
            String phoneNumber, String status, Instant createdAt) {
    }

    public record ChangePasswordRequest(
            @NotBlank String oldPassword,
            @NotBlank String newPassword) {
    }

    public record AdminCreateUserRequest(
            @NotNull UUID actorId,
            @NotBlank String username,
            @Email @NotBlank String email,
            String phoneNumber,
            String password,
            Boolean sendWelcomeEmail) {
    }

    public record AdminCreateUserResponse(UUID id, UUID actorId, String username, String email,
            String status, String temporaryPassword, boolean emailSent) {
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid onboarding data.", exception);
        }
    }
}
