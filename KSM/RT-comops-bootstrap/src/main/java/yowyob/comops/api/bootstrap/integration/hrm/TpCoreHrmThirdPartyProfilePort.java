package yowyob.comops.api.bootstrap.integration.hrm;

import yowyob.comops.api.hrm.application.port.out.ThirdPartyProfilePort;
import yowyob.comops.api.tp.application.port.in.EnsureActorFinancialProfileCommand;
import yowyob.comops.api.tp.application.port.in.EnsureActorFinancialProfileUseCase;

import java.util.UUID;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class TpCoreHrmThirdPartyProfilePort implements ThirdPartyProfilePort {

    private static final String EMPLOYEE_ROLE = "EMPLOYEE";

    private final EnsureActorFinancialProfileUseCase ensureActorFinancialProfileUseCase;

    public TpCoreHrmThirdPartyProfilePort(EnsureActorFinancialProfileUseCase ensureActorFinancialProfileUseCase) {
        this.ensureActorFinancialProfileUseCase = ensureActorFinancialProfileUseCase;
    }

    @Override
    public Mono<ThirdPartyProfile> ensureEmployeeFinancialProfile(UUID tenantId, UUID organizationId, UUID actorId,
            String referenceCode, String displayName) {
        return ensureActorFinancialProfileUseCase.ensureActorFinancialProfile(
                        new EnsureActorFinancialProfileCommand(tenantId, organizationId, actorId, EMPLOYEE_ROLE,
                                referenceCode, displayName))
                .map(thirdParty -> new ThirdPartyProfile(thirdParty.id(), thirdParty.referenceCode(),
                        thirdParty.accountingAccount()));
    }
}
