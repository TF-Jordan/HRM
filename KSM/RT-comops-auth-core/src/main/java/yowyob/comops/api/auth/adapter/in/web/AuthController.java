package yowyob.comops.api.auth.adapter.in.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import yowyob.comops.api.auth.application.port.in.DiscoverLoginContextsCommand;
import yowyob.comops.api.auth.application.port.in.DiscoverLoginContextsUseCase;
import yowyob.comops.api.auth.application.port.in.DiscoverSignUpContextsCommand;
import yowyob.comops.api.auth.application.port.in.DiscoverSignUpContextsUseCase;
import yowyob.comops.api.auth.application.port.in.IdentifyAccountCommand;
import yowyob.comops.api.auth.application.port.in.IdentifyAccountUseCase;
import yowyob.comops.api.auth.application.port.in.LoginCommand;
import yowyob.comops.api.auth.application.port.in.LoginUseCase;
import yowyob.comops.api.auth.application.port.in.PublicSignUpCommand;
import yowyob.comops.api.auth.application.port.in.PublicSignUpUseCase;
import yowyob.comops.api.auth.application.port.in.RegisterUserCommand;
import yowyob.comops.api.auth.application.port.in.RegisterUserUseCase;
import yowyob.comops.api.auth.application.port.in.SelectLoginContextCommand;
import yowyob.comops.api.auth.application.port.in.SelectLoginContextUseCase;
import yowyob.comops.api.auth.application.service.AuthApplicationService;
import yowyob.comops.api.auth.application.service.AuthChallengeTokenService;
import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.application.port.out.ReactivePermissionResolver;
import yowyob.comops.api.kernel.application.service.ReactiveRequestContextHolder;
import yowyob.comops.api.kernel.config.UserSessionTokenService;
import jakarta.validation.Valid;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final DiscoverLoginContextsUseCase discoverLoginContextsUseCase;
    private final DiscoverSignUpContextsUseCase discoverSignUpContextsUseCase;
    private final SelectLoginContextUseCase selectLoginContextUseCase;
    private final IdentifyAccountUseCase identifyAccountUseCase;
    private final PublicSignUpUseCase publicSignUpUseCase;
    private final RegisterUserUseCase registerUserUseCase;
    private final AuthApplicationService authApplicationService;
    private final ReactivePermissionResolver permissionResolver;
    private final UserSessionTokenService userSessionTokenService;
    private final AuthUserViewAssembler authUserViewAssembler;
    private final ObjectMapper objectMapper;

    public AuthController(LoginUseCase loginUseCase,
            DiscoverLoginContextsUseCase discoverLoginContextsUseCase,
            DiscoverSignUpContextsUseCase discoverSignUpContextsUseCase,
            SelectLoginContextUseCase selectLoginContextUseCase,
            IdentifyAccountUseCase identifyAccountUseCase,
            PublicSignUpUseCase publicSignUpUseCase,
            RegisterUserUseCase registerUserUseCase,
            AuthApplicationService authApplicationService,
            ReactivePermissionResolver permissionResolver,
            UserSessionTokenService userSessionTokenService,
            AuthUserViewAssembler authUserViewAssembler,
            ObjectMapper objectMapper) {
        this.loginUseCase = loginUseCase;
        this.discoverLoginContextsUseCase = discoverLoginContextsUseCase;
        this.discoverSignUpContextsUseCase = discoverSignUpContextsUseCase;
        this.selectLoginContextUseCase = selectLoginContextUseCase;
        this.identifyAccountUseCase = identifyAccountUseCase;
        this.publicSignUpUseCase = publicSignUpUseCase;
        this.registerUserUseCase = registerUserUseCase;
        this.authApplicationService = authApplicationService;
        this.permissionResolver = permissionResolver;
        this.userSessionTokenService = userSessionTokenService;
        this.authUserViewAssembler = authUserViewAssembler;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/identify")
    public Mono<ResponseEntity<ApiResponse<IdentifyAccountResponse>>> identify(
            @Valid @RequestBody Mono<IdentifyAccountRequest> requestMono) {
        return requestMono
                .flatMap(request -> identifyAccountUseCase.identify(new IdentifyAccountCommand(request.principal())))
                .map(IdentifyAccountResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Account identification completed.")));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<ApiResponse<Object>>> login(@Valid @RequestBody Mono<LoginRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> loginUseCase.login(new LoginCommand(
                        tuple.getT2().tenantId(),
                        tuple.getT1().principal(),
                        tuple.getT1().password())))
                .flatMap(userAccount -> userAccount.mfaEnabled()
                        ? authApplicationService.issueLoginMfaForUser(userAccount)
                        .map(issued -> ResponseEntity.status(HttpStatus.ACCEPTED)
                                .body(ApiResponse.success((Object) new MfaRequiredResponse(
                                        "CONFIRM_MFA",
                                        issued.token(),
                                        userAccount.mfaChannel(),
                                        issued.codePreview(),
                                        issued.expiresInSeconds()), "MFA required.")))
                        : toLoginResponse(userAccount)
                        .map(response -> ResponseEntity.ok(ApiResponse.success((Object) response, "User authenticated."))));
    }

    @PostMapping("/login/mfa/confirm")
    public Mono<ResponseEntity<ApiResponse<LoginResponse>>> confirmLoginMfa(
            @Valid @RequestBody Mono<ConfirmMfaLoginRequest> requestMono) {
        return requestMono
                .flatMap(request -> authApplicationService.confirmLoginMfa(request.mfaToken(), request.code()))
                .flatMap(this::toLoginResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "User authenticated.")));
    }

    @PostMapping("/discover-contexts")
    public Mono<ResponseEntity<ApiResponse<DiscoverLoginContextsResponse>>> discoverContexts(
            @Valid @RequestBody Mono<LoginRequest> requestMono) {
        return requestMono
                .flatMap(request -> discoverLoginContextsUseCase.discover(
                        new DiscoverLoginContextsCommand(request.principal(), request.password())))
                .map(DiscoverLoginContextsResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Login contexts discovered.")));
    }

    @PostMapping("/discover-sign-up-contexts")
    public Mono<ResponseEntity<ApiResponse<DiscoverSignUpContextsResponse>>> discoverSignUpContexts(
            @Valid @RequestBody Mono<DiscoverSignUpContextsRequest> requestMono) {
        return requestMono
                .flatMap(request -> discoverSignUpContextsUseCase.discover(
                        new DiscoverSignUpContextsCommand(request.organizationCode())))
                .map(DiscoverSignUpContextsResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Sign-up contexts discovered.")));
    }

    @PostMapping("/select-context")
    public Mono<ResponseEntity<ApiResponse<ContextualLoginResponse>>> selectContext(
            @Valid @RequestBody Mono<SelectLoginContextRequest> requestMono) {
        return requestMono
                .flatMap(request -> selectLoginContextUseCase.select(new SelectLoginContextCommand(
                                request.selectionToken(),
                                request.contextId(),
                                request.organizationId()))
                        .flatMap(selectedContext -> permissionResolver
                                .resolvePermissions(selectedContext.userAccount().tenantId(),
                                        selectedContext.userAccount().id())
                                .defaultIfEmpty(Set.of())
                                .flatMap(authorities -> authUserViewAssembler.toLoginResponse(
                                                selectedContext.userAccount(),
                                                userSessionTokenService.issue(
                                                        selectedContext.userAccount().tenantId(),
                                                        selectedContext.organizationId(),
                                                        null,
                                                        selectedContext.userAccount().id(),
                                                        selectedContext.userAccount().actorId(),
                                                        authorities),
                                                userSessionTokenService.getAccessTokenTtl(),
                                                authorities)
                                        .map(loginResponse -> ContextualLoginResponse.from(
                                                selectedContext.userAccount().tenantId(),
                                                selectedContext.organizationId(),
                                                loginResponse)))))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Login context selected.")));
    }

    @PostMapping("/sign-up")
    public Mono<ResponseEntity<ApiResponse<LoginResponse>>> signUp(
            @Valid @RequestBody Mono<PublicSignUpRequest> requestMono) {
        return requestMono
                .flatMap(request -> publicSignUpUseCase.signUp(new PublicSignUpCommand(
                                request.tenantId(),
                                request.signUpSelectionToken(),
                                request.contextId(),
                                request.firstName(),
                                request.lastName(),
                                request.username(),
                                request.email(),
                                request.phoneNumber(),
                                request.password(),
                                request.socialProvider(),
                                request.externalSubject(),
                                request.captchaVerificationToken(),
                                request.accountType(),
                                request.businessType(),
                                toJson(request.onboardingData())))
                        .flatMap(userAccount -> permissionResolver.resolvePermissions(userAccount.tenantId(),
                                        userAccount.id())
                                .defaultIfEmpty(Set.of())
                                .flatMap(authorities -> authUserViewAssembler.toLoginResponse(
                                        userAccount,
                                        userSessionTokenService.issue(userAccount.tenantId(), userAccount.id(),
                                                userAccount.actorId(), authorities),
                                        userSessionTokenService.getAccessTokenTtl(),
                                        authorities))))
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .<ApiResponse<LoginResponse>>body(ApiResponse.success(response, "User signed up.")));
    }

    @PostMapping("/forgot-password")
    public Mono<ResponseEntity<ApiResponse<ForgotPasswordResponse>>> forgotPassword(
            @Valid @RequestBody Mono<ForgotPasswordRequest> requestMono) {
        return requestMono
                .flatMap(request -> authApplicationService.forgotPassword(request.principal()))
                .map(payload -> ForgotPasswordResponse.of(
                        payload.principal(),
                        payload.matchingAccountCount(),
                        payload.selectionToken(),
                        payload.selectionTokenExpiresInSeconds(),
                        payload.contexts()))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Password reset contexts discovered.")));
    }

    @PostMapping("/captcha")
    public Mono<ResponseEntity<ApiResponse<CaptchaChallengeResponse>>> issueCaptcha() {
        AuthChallengeTokenService.IssuedCaptchaChallenge issued = authApplicationService.issueCaptcha();
        return Mono.just(ResponseEntity.ok(ApiResponse.success(new CaptchaChallengeResponse(
                issued.token(),
                issued.prompt(),
                issued.answerPreview(),
                issued.expiresInSeconds()), "Captcha issued.")));
    }

    @PostMapping("/captcha/verify")
    public Mono<ResponseEntity<ApiResponse<CaptchaVerificationResponse>>> verifyCaptcha(
            @Valid @RequestBody Mono<VerifyCaptchaRequest> requestMono) {
        return requestMono
                .flatMap(request -> authApplicationService.verifyCaptcha(request.captchaToken(), request.answer()))
                .map(issued -> ResponseEntity.ok(ApiResponse.success(new CaptchaVerificationResponse(
                        issued.token(),
                        issued.expiresInSeconds()), "Captcha verified.")));
    }

    @PostMapping("/otp")
    public Mono<ResponseEntity<ApiResponse<OtpChallengeResponse>>> issueOtp(
            @Valid @RequestBody Mono<IssueOtpRequest> requestMono) {
        return requestMono
                .map(request -> authApplicationService.issueOtp(request.channel(), request.recipient(), request.purpose()))
                .map(issued -> ResponseEntity.ok(ApiResponse.success(new OtpChallengeResponse(
                        "PREVIEW_ONLY",
                        issued.token(),
                        issued.codePreview(),
                        issued.expiresInSeconds()), "OTP issued.")));
    }

    @PostMapping("/otp/verify")
    public Mono<ResponseEntity<ApiResponse<OtpVerificationResponse>>> verifyOtp(
            @Valid @RequestBody Mono<VerifyOtpRequest> requestMono) {
        return requestMono
                .flatMap(request -> authApplicationService.verifyOtp(request.challengeToken(), request.code(), request.purpose()))
                .map(verified -> ResponseEntity.ok(ApiResponse.success(new OtpVerificationResponse(
                        true,
                        verified.channel(),
                        verified.recipient(),
                        verified.purpose()), "OTP verified.")));
    }

    @PostMapping("/password-reset/issue")
    public Mono<ResponseEntity<ApiResponse<IssuedAuthChallengeResponse>>> issuePasswordReset(
            @Valid @RequestBody Mono<IssuePasswordResetRequest> requestMono) {
        return requestMono
                .flatMap(request -> authApplicationService.issuePasswordReset(
                        request.selectionToken(),
                        request.contextId()))
                .map(issued -> new IssuedAuthChallengeResponse(
                        issued.deliveryMode(),
                        issued.challengeTokenPreview(),
                        issued.expiresInSeconds()))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Password reset issued.")));
    }

    @PostMapping("/reset-password")
    public Mono<ResponseEntity<ApiResponse<UserAccountResponse>>> resetPassword(
            @Valid @RequestBody Mono<ResetPasswordRequest> requestMono) {
        return requestMono
                .flatMap(request -> authApplicationService.resetPassword(request.resetToken(), request.newPassword()))
                .flatMap(authUserViewAssembler::toUserAccountResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Password reset completed.")));
    }

    @PostMapping("/change-password")
    @PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
    public Mono<ResponseEntity<ApiResponse<UserAccountResponse>>> changePassword(
            @Valid @RequestBody Mono<ChangePasswordRequest> requestMono) {
        return requestMono
                .flatMap(request -> authApplicationService.changeCurrentUserPassword(
                        request.currentPassword(), request.newPassword()))
                .flatMap(authUserViewAssembler::toUserAccountResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Password changed.")));
    }

    @PostMapping("/email-verification/request")
    @PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
    public Mono<ResponseEntity<ApiResponse<IssuedAuthChallengeResponse>>> requestEmailVerification() {
        return authApplicationService.issueCurrentEmailVerification()
                .map(issued -> new IssuedAuthChallengeResponse(
                        issued.deliveryMode(),
                        issued.challengeTokenPreview(),
                        issued.expiresInSeconds()))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Email verification issued.")));
    }

    @PostMapping("/email-verification/confirm")
    public Mono<ResponseEntity<ApiResponse<UserAccountResponse>>> confirmEmailVerification(
            @Valid @RequestBody Mono<ConfirmEmailVerificationRequest> requestMono) {
        return requestMono
                .flatMap(request -> authApplicationService.confirmEmailVerification(request.verificationToken()))
                .flatMap(authUserViewAssembler::toUserAccountResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Email verified.")));
    }

    @PostMapping("/phone-verification/request")
    @PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
    public Mono<ResponseEntity<ApiResponse<OtpChallengeResponse>>> requestPhoneVerification(
            @Valid @RequestBody Mono<IssueOtpRequest> requestMono) {
        return requestMono
                .flatMap(request -> authApplicationService.issueCurrentUserPhoneVerification(
                        request.recipient(),
                        request.channel()))
                .map(issued -> ResponseEntity.ok(ApiResponse.success(new OtpChallengeResponse(
                        "PREVIEW_ONLY",
                        issued.token(),
                        issued.codePreview(),
                        issued.expiresInSeconds()), "Phone verification issued.")));
    }

    @PostMapping("/phone-verification/confirm")
    @PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
    public Mono<ResponseEntity<ApiResponse<UserAccountResponse>>> confirmPhoneVerification(
            @Valid @RequestBody Mono<ConfirmMfaRequest> requestMono) {
        return requestMono
                .flatMap(request -> authApplicationService.confirmCurrentUserPhoneVerification(
                        request.challengeToken(),
                        request.code()))
                .flatMap(authUserViewAssembler::toUserAccountResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Phone verified.")));
    }

    @PostMapping("/mfa/enable")
    @PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
    public Mono<ResponseEntity<ApiResponse<OtpChallengeResponse>>> enableMfa(
            @Valid @RequestBody Mono<EnableMfaRequest> requestMono) {
        return requestMono
                .flatMap(request -> authApplicationService.issueCurrentUserMfaEnable(request.channel()))
                .map(issued -> ResponseEntity.ok(ApiResponse.success(new OtpChallengeResponse(
                        "PREVIEW_ONLY",
                        issued.token(),
                        issued.codePreview(),
                        issued.expiresInSeconds()), "MFA enable challenge issued.")));
    }

    @PostMapping("/mfa/confirm")
    @PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
    public Mono<ResponseEntity<ApiResponse<UserAccountResponse>>> confirmMfa(
            @Valid @RequestBody Mono<ConfirmMfaRequest> requestMono) {
        return requestMono
                .flatMap(request -> authApplicationService.confirmCurrentUserMfaEnable(
                        request.challengeToken(),
                        request.code()))
                .flatMap(authUserViewAssembler::toUserAccountResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "MFA enabled.")));
    }

    @PostMapping("/mfa/disable")
    @PreAuthorize("@businessAccessPolicy.hasUserContext(authentication)")
    public Mono<ResponseEntity<ApiResponse<UserAccountResponse>>> disableMfa() {
        return authApplicationService.disableCurrentUserMfa()
                .flatMap(authUserViewAssembler::toUserAccountResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "MFA disabled.")));
    }

    @PostMapping("/register")
    @PreAuthorize("@businessAccessPolicy.canManageIdentity(authentication)")
    public Mono<ResponseEntity<ApiResponse<UserAccountResponse>>> register(
            @Valid @RequestBody Mono<RegisterUserRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> registerUserUseCase.register(new RegisterUserCommand(
                        tuple.getT2().tenantId(),
                        tuple.getT1().actorId(),
                        tuple.getT1().username(),
                        tuple.getT1().email(),
                        tuple.getT1().phoneNumber(),
                        tuple.getT1().password(),
                        tuple.getT1().authProvider(),
                        tuple.getT1().externalSubject())))
                .flatMap(authUserViewAssembler::toUserAccountResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "User registered.")));
    }

    private Mono<LoginResponse> toLoginResponse(yowyob.comops.api.auth.domain.model.UserAccount userAccount) {
        return permissionResolver.resolvePermissions(userAccount.tenantId(), userAccount.id())
                .defaultIfEmpty(Set.of())
                .flatMap(authorities -> authUserViewAssembler.toLoginResponse(userAccount,
                        userSessionTokenService.issue(userAccount.tenantId(), userAccount.id(),
                                userAccount.actorId(), authorities),
                        userSessionTokenService.getAccessTokenTtl(),
                        authorities));
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
