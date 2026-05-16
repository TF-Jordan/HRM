package yowyob.comops.api.kernel.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.kernel.adapter.in.web.ClientApplicationServiceEntitlementWebFilter;
import yowyob.comops.api.kernel.adapter.in.web.OrganizationServiceEntitlementWebFilter;
import yowyob.comops.api.kernel.adapter.in.web.PlatformServiceRouteResolver;
import yowyob.comops.api.kernel.application.port.in.AuthenticateClientApplicationUseCase;
import yowyob.comops.api.kernel.application.port.out.OrganizationServiceRuntimeEntitlementDirectory;
import yowyob.comops.api.kernel.application.port.out.ReactivePermissionResolver;
import java.nio.charset.StandardCharsets;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class KernelSecurityConfiguration {

    @Bean
    @Order(0)
    SecurityWebFilterChain managementSecurityWebFilterChain(ServerHttpSecurity http,
            ManagementSecurityProperties managementSecurityProperties,
            ObjectMapper objectMapper) {
        ReactiveAuthenticationManager managementApiKeyAuthenticationManager = managementApiKeyAuthenticationManager(
                managementSecurityProperties);
        AuthenticationWebFilter managementApiKeyFilter = new AuthenticationWebFilter(
                managementApiKeyAuthenticationManager);
        managementApiKeyFilter.setServerAuthenticationConverter(new ManagementApiKeyAuthenticationConverter());
        managementApiKeyFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/actuator/**"));
        managementApiKeyFilter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance());

        return http
                .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/actuator/**"))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((exchange, ex) -> writeFailure(exchange, objectMapper,
                                HttpStatus.UNAUTHORIZED, ex.getMessage(), "UNAUTHORIZED"))
                        .accessDeniedHandler((exchange, ex) -> writeFailure(exchange, objectMapper,
                                HttpStatus.FORBIDDEN, ex.getMessage(), "FORBIDDEN")))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        .pathMatchers("/actuator/**").hasAuthority("management:read")
                        .anyExchange().denyAll())
                .addFilterAt(managementApiKeyFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    @Order(1)
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
            SecurityRuntimeProperties securityRuntimeProperties,
            AuthenticateClientApplicationUseCase authenticateClientApplicationUseCase,
            OrganizationServiceRuntimeEntitlementDirectory organizationServiceRuntimeEntitlementDirectory,
            ReactivePermissionResolver permissionResolver,
            UserSessionTokenService userSessionTokenService,
            ObjectProvider<ReactiveStringRedisTemplate> redisTemplateProvider,
            OrganizationServiceRequestQuotaProperties organizationServiceRequestQuotaProperties,
            MeterRegistry meterRegistry,
            ObjectMapper objectMapper) {
        ReactiveAuthenticationManager apiKeyAuthenticationManager = apiKeyAuthenticationManager(
                securityRuntimeProperties, authenticateClientApplicationUseCase, permissionResolver);
        AuthenticationWebFilter apiKeyFilter = new AuthenticationWebFilter(apiKeyAuthenticationManager);
        apiKeyFilter.setServerAuthenticationConverter(new ApiKeyServerAuthenticationConverter(userSessionTokenService));
        apiKeyFilter.setRequiresAuthenticationMatcher(apiAuthenticationMatcher());
        apiKeyFilter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance());
        PlatformServiceRouteResolver routeResolver = new PlatformServiceRouteResolver();
        ClientApplicationServiceEntitlementWebFilter clientApplicationServiceEntitlementWebFilter =
                new ClientApplicationServiceEntitlementWebFilter(routeResolver, objectMapper);
        OrganizationServiceEntitlementWebFilter organizationServiceEntitlementWebFilter =
                new OrganizationServiceEntitlementWebFilter(organizationServiceRuntimeEntitlementDirectory, routeResolver,
                        redisTemplateProvider.getIfAvailable(), organizationServiceRequestQuotaProperties,
                        objectMapper, meterRegistry);

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((exchange, ex) -> writeFailure(exchange, objectMapper,
                                HttpStatus.UNAUTHORIZED, ex.getMessage(), "UNAUTHORIZED"))
                        .accessDeniedHandler((exchange, ex) -> writeFailure(exchange, objectMapper,
                                HttpStatus.FORBIDDEN, ex.getMessage(), "FORBIDDEN")))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.GET, "/.well-known/jwks.json").permitAll()
                        .pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**")
                        .permitAll()
                        .pathMatchers("/api/**").authenticated()
                        .anyExchange().permitAll())
                .addFilterAt(apiKeyFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterAfter(clientApplicationServiceEntitlementWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .addFilterAt(organizationServiceEntitlementWebFilter, SecurityWebFiltersOrder.AUTHORIZATION)
                .build();
    }

    private ReactiveAuthenticationManager apiKeyAuthenticationManager(
            SecurityRuntimeProperties securityRuntimeProperties,
            AuthenticateClientApplicationUseCase authenticateClientApplicationUseCase,
            ReactivePermissionResolver permissionResolver) {
        requireValidTokenMechanism(securityRuntimeProperties);
        return new ApiKeyReactiveAuthenticationManager(authenticateClientApplicationUseCase, permissionResolver);
    }

    private ReactiveAuthenticationManager managementApiKeyAuthenticationManager(
            ManagementSecurityProperties managementSecurityProperties) {
        String apiKey = requireValidManagementApiKey(managementSecurityProperties);
        return new ManagementApiKeyReactiveAuthenticationManager(apiKey);
    }

    private void requireValidTokenMechanism(SecurityRuntimeProperties props) {
        if (!props.isJwtConfigured()) {
            throw new IllegalStateException(
                    "JWT signing is required. Configure 'iwm.security.jwt.private-key-path' or "
                            + "'iwm.security.jwt.auto-generate-key-pair=true'.");
        }
    }

    private ServerWebExchangeMatcher apiAuthenticationMatcher() {
        return exchange -> {
            String path = exchange.getRequest().getPath().pathWithinApplication().value();
            return path.startsWith("/api/")
                    ? ServerWebExchangeMatcher.MatchResult.match()
                    : ServerWebExchangeMatcher.MatchResult.notMatch();
        };
    }

    private String requireValidManagementApiKey(ManagementSecurityProperties managementSecurityProperties) {
        String apiKey = managementSecurityProperties.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("iwm.management.security.api-key must be configured.");
        }
        if ("change-me".equals(apiKey)) {
            throw new IllegalStateException(
                    "iwm.management.security.api-key must not use the default insecure value 'change-me'.");
        }
        return apiKey;
    }

    private reactor.core.publisher.Mono<Void> writeFailure(
            org.springframework.web.server.ServerWebExchange exchange,
            ObjectMapper objectMapper,
            HttpStatus status,
            String message,
            String errorCode) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] payload;
        try {
            payload = objectMapper.writeValueAsBytes(ApiResponse.failure(message, errorCode));
        } catch (com.fasterxml.jackson.core.JsonProcessingException exception) {
            payload = ("{\"success\":false,\"message\":\"" + sanitize(message)
                    + "\",\"errorCode\":\"" + errorCode + "\"}")
                    .getBytes(StandardCharsets.UTF_8);
        }
        return exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(payload)));
    }

    private String sanitize(String value) {
        if (value == null) {
            return "Request denied.";
        }
        return value.replace("\"", "'");
    }
}
