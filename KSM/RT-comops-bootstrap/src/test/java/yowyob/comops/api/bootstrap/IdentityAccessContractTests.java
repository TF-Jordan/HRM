package yowyob.comops.api.bootstrap;

import yowyob.comops.api.actor.application.port.in.CreateActorCommand;
import yowyob.comops.api.actor.application.port.in.CreateActorUseCase;
import yowyob.comops.api.auth.application.port.in.RegisterUserCommand;
import yowyob.comops.api.auth.application.port.in.RegisterUserUseCase;
import yowyob.comops.api.roles.application.port.in.AssignRoleToUserCommand;
import yowyob.comops.api.roles.application.port.in.AssignRoleToUserUseCase;
import yowyob.comops.api.roles.application.port.in.CreateRoleCommand;
import yowyob.comops.api.roles.application.port.in.CreateRoleUseCase;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {                "spring.r2dbc.url=r2dbc:postgresql://localhost:54329/iwm",
                "spring.r2dbc.username=iwm",
                "spring.r2dbc.password=iwm",
                "spring.liquibase.url=jdbc:postgresql://localhost:54329/iwm",
                "spring.liquibase.user=iwm",
                "spring.liquibase.password=iwm",
                "spring.liquibase.change-log=classpath:db/changelog/contracts/identity-access-contract.yaml",
                "spring.liquibase.drop-first=false",
                "iwm.outbox.delivery.type=recording",
                "iwm.outbox.consumers.mode=inline"
        })
@ActiveProfiles("r2dbc")
@EnabledIfSystemProperty(named = "iwm.tests.r2dbc.enabled", matches = "true")
class IdentityAccessContractTests extends AbstractContractR2dbcIntegrationTest {

    private static final String TENANT_ID = "24010000-0000-0000-0000-000000000001";
    private static final String USER_ID = "24010000-0000-0000-0000-000000000011";

    @Autowired
    private CreateActorUseCase createActorUseCase;

    @Autowired
    private RegisterUserUseCase registerUserUseCase;

    @Autowired
    private CreateRoleUseCase createRoleUseCase;

    @Autowired
    private AssignRoleToUserUseCase assignRoleToUserUseCase;

    @Test
    void identityAndAccessContractIsStableOnDedicatedDataset() {
        AtomicReference<String> userId = new AtomicReference<>();
        UUID tenantId = UUID.fromString(TENANT_ID);
        UUID actorId = Objects.requireNonNull(createActorUseCase.createActor(new CreateActorCommand(
                tenantId,
                "Identity",
                "Contract",
                null,
                "identity.contract@example.com",
                "M",
                "CM",
                null,
                null,
                null)).block()).id();
        UUID registeredUserId = Objects.requireNonNull(registerUserUseCase.register(new RegisterUserCommand(
                tenantId,
                actorId,
                "identity-contract",
                "identity.contract@example.com",
                "Password!123",
                "LOCAL")).block()).id();
        userId.set(registeredUserId.toString());
        UUID roleId = Objects.requireNonNull(createRoleUseCase.createRole(new CreateRoleCommand(
                tenantId,
                "ROLE-IDENTITY-CONTRACT",
                "Identity Contract Role",
                Set.of("organizations:write"))).block()).id();
        assignRoleToUserUseCase.assign(new AssignRoleToUserCommand(
                tenantId,
                registeredUserId,
                roleId,
                "TENANT")).block();

        userClient(TENANT_ID, userId.get()).post()
                .uri("/api/organizations")
                .bodyValue(Map.of(
                        "businessActorId", actorId.toString(),
                        "code", "ORG-IDENTITY-CONTRACT",
                        "legalName", "Identity Contract Org",
                        "displayName", "Identity Contract Org",
                        "organizationType", "PRIVATE_COMPANY"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.code").isEqualTo("ORG-IDENTITY-CONTRACT");
    }
}
