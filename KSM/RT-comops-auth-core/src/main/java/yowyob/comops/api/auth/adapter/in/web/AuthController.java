package yowyob.comops.api.auth.adapter.in.web;

import yowyob.comops.api.auth.application.port.in.LoginCommand;
import yowyob.comops.api.auth.application.port.in.LoginUseCase;
import yowyob.comops.api.auth.application.port.in.RegisterUserCommand;
import yowyob.comops.api.auth.application.port.in.RegisterUserUseCase;
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
    private final RegisterUserUseCase registerUserUseCase;
    private final ReactivePermissionResolver permissionResolver;
    private final UserSessionTokenService userSessionTokenService;
    private final AuthUserViewAssembler authUserViewAssembler;

    public AuthController(LoginUseCase loginUseCase,
            RegisterUserUseCase registerUserUseCase,
            ReactivePermissionResolver permissionResolver,
            UserSessionTokenService userSessionTokenService,
            AuthUserViewAssembler authUserViewAssembler) {
        this.loginUseCase = loginUseCase;
        this.registerUserUseCase = registerUserUseCase;
        this.permissionResolver = permissionResolver;
        this.userSessionTokenService = userSessionTokenService;
        this.authUserViewAssembler = authUserViewAssembler;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<ApiResponse<LoginResponse>>> login(@Valid @RequestBody Mono<LoginRequest> requestMono) {
        return requestMono
                .zipWith(ReactiveRequestContextHolder.getRequiredContext())
                .flatMap(tuple -> loginUseCase.login(new LoginCommand(
                        tuple.getT2().tenantId(),
                        tuple.getT1().principal(),
                        tuple.getT1().password())))
                .flatMap(userAccount -> permissionResolver.resolvePermissions(userAccount.tenantId(), userAccount.id())
                        .defaultIfEmpty(Set.of())
                        .flatMap(authorities -> authUserViewAssembler.toLoginResponse(userAccount,
                                userSessionTokenService.issue(userAccount.tenantId(), userAccount.id(),
                                        userAccount.actorId(), authorities),
                                userSessionTokenService.getAccessTokenTtl(),
                                authorities)))
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "User authenticated.")));
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
                        tuple.getT1().password(),
                        tuple.getT1().authProvider())))
                .flatMap(authUserViewAssembler::toUserAccountResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(response, "User registered.")));
    }
}
