package yowyob.comops.api.tp.adapter.in.web;

import yowyob.comops.api.common.domain.model.ApiResponse;
import yowyob.comops.api.tp.application.port.in.CreateThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.in.DeleteThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.in.GetThirdPartyStatisticsUseCase;
import yowyob.comops.api.tp.application.port.in.GetThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.in.ListThirdPartiesUseCase;
import yowyob.comops.api.tp.application.port.in.LookupThirdPartyUseCase;
import yowyob.comops.api.tp.application.port.in.ManageThirdPartyBankAccountsUseCase;
import yowyob.comops.api.tp.application.port.in.ManageThirdPartyLifecycleUseCase;
import yowyob.comops.api.tp.application.port.in.SearchThirdPartiesUseCase;
import yowyob.comops.api.tp.application.port.in.UpdateThirdPartyUseCase;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/prospects")
public class ProspectController extends CommercialThirdPartyController {

    private final ManageThirdPartyLifecycleUseCase manageThirdPartyLifecycleUseCase;
    private final GetThirdPartyStatisticsUseCase getThirdPartyStatisticsUseCase;

    public ProspectController(CreateThirdPartyUseCase createThirdPartyUseCase,
            GetThirdPartyUseCase getThirdPartyUseCase,
            ListThirdPartiesUseCase listThirdPartiesUseCase,
            SearchThirdPartiesUseCase searchThirdPartiesUseCase,
            UpdateThirdPartyUseCase updateThirdPartyUseCase,
            DeleteThirdPartyUseCase deleteThirdPartyUseCase,
            ManageThirdPartyLifecycleUseCase manageThirdPartyLifecycleUseCase,
            ManageThirdPartyBankAccountsUseCase manageThirdPartyBankAccountsUseCase,
            LookupThirdPartyUseCase lookupThirdPartyUseCase,
            GetThirdPartyStatisticsUseCase getThirdPartyStatisticsUseCase) {
        super(createThirdPartyUseCase, getThirdPartyUseCase, listThirdPartiesUseCase, searchThirdPartiesUseCase,
                updateThirdPartyUseCase, deleteThirdPartyUseCase, manageThirdPartyLifecycleUseCase,
                manageThirdPartyBankAccountsUseCase, lookupThirdPartyUseCase, getThirdPartyStatisticsUseCase,
                "PROSPECT", "Prospect", true);
        this.manageThirdPartyLifecycleUseCase = manageThirdPartyLifecycleUseCase;
        this.getThirdPartyStatisticsUseCase = getThirdPartyStatisticsUseCase;
    }

    @PostMapping("/{thirdPartyId}/convert")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
    public Mono<ResponseEntity<ApiResponse<ThirdPartyResponse>>> convertToCustomer(@PathVariable UUID thirdPartyId) {
        return manageThirdPartyLifecycleUseCase.convertProspectToCustomer(thirdPartyId)
                .map(ThirdPartyResponse::from)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Prospect converted to customer.")));
    }

    @GetMapping("/statistics/conversions")
    @PreAuthorize("@businessAccessPolicy.hasPermission(authentication, 'third-parties:write')")
    public Mono<ResponseEntity<ApiResponse<Long>>> conversionCount(@RequestParam("organizationId") UUID organizationId) {
        return getThirdPartyStatisticsUseCase.getProspectConversionCount(organizationId)
                .map(count -> ResponseEntity.ok(ApiResponse.success(count, "Prospect conversion count retrieved.")));
    }
}
